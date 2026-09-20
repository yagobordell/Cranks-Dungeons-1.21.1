package com.cranks.dungeons.client.compendium;

import com.cranks.dungeons.client.screen.PassiveNodeEditScreen;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.component.PassiveDataComponent;
import com.cranks.dungeons.network.SelectClassPayload;
import com.cranks.dungeons.stat.passive.PassiveNode;
import com.cranks.dungeons.stat.passive.PassiveNodeRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.stat.passive.PassiveStatHelper;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

public class PassiveTreeTabRenderer implements CompendiumTabRenderer {
    // Extend only the top of the tree viewport; the side and bottom bounds
    // remain aligned with the existing inner panel.
    private static final int TREE_TOP_MARGIN = -6;
    private static final int CLASS_TAB_HEIGHT = 32;
    private static final int CLASS_TAB_GAP = 5;
    private static final int CLASS_TAB_MIN_WIDTH = 52;
    private static final int CLASS_TAB_MAX_WIDTH = 100;
    private static final int CLASS_TAB_SIDE_MARGIN = 10;
    private static final int CLASS_TAB_TOP_OFFSET = 16;
    private static final int CLASS_TAB_VERTICAL_NUDGE = 3;
    private static final int CLASS_TAB_FRAME_BALANCE = 4;
    private static final int CLASS_PANEL_MAX_WIDTH = 620;
    private CompendiumScreen parent;
    private int contentX, contentY, contentW, contentH;
    
    private double panX = 0;
    private double panY = 0;
    private double zoom = 0.75;
    private static final int BASE_NODE_SIZE = 10;
    private static final int BASE_GRID_SCALE = 60; // Pixels per grid unit



    private boolean choosingClass = false;
    private final Map<String, String> draftUnlocked = new HashMap<>();
    private int draftPoints;
    private boolean showStatsPanel = false;

    private String activeChoiceNodeId = null;
    private int choicePopupX = 0;
    private int choicePopupY = 0;
    private boolean suppressTreeDragUntilRelease = false;

    // --- Unified Logic & Rendering Caches ---
    private record Edge(PassiveNode n1, PassiveNode n2) {
    }

    private final Set<Edge> edgeCache = new HashSet<>();
    private final Set<String> canUnlockCache = new HashSet<>();
    private final Map<String, Set<String>> adjacencyMap = new HashMap<>();
    private final Map<String, Integer> distanceMap = new HashMap<>();
    private final Set<String> nodesOnHoverPath = new HashSet<>();
    private final Set<String> lastObservedUnlockedNodes = new HashSet<>();
    private int lastObservedUnspentPoints = Integer.MIN_VALUE;
    private boolean cacheDirty = true;

    // --- Tabbed Selection UI ---
    private int selectedClassTab = 0;
    private boolean showingConfirmPopup = false;
    private String classPendingConfirmation = null;
    private CompendiumButtonWidget confirmBtn;
    private CompendiumButtonWidget cancelBtn;
    private int classTabRows = 1;
    private int classTabAreaHeight = CLASS_TAB_HEIGHT;
    private int classLoreWidth = 320;

    // designer MODE fields
    private boolean editMode = false;
    private PassiveNode draggedNode = null;
    private PassiveNode connectionSource = null;
    private boolean isConnecting = false;
    private boolean snapToGrid = false;

    // Multi-Selection fields
    private final Set<String> selectedNodeIds = new HashSet<>();
    private double selectionStartX, selectionStartY;
    private double selectionCurrentX, selectionCurrentY;
    private boolean isSelectingBox = false;

    @Override
    public void init(CompendiumScreen parent, int contentX, int contentY, int contentW, int contentH) {
        this.parent = parent;
        this.contentX = contentX;
        this.contentY = contentY;
        this.contentW = contentW;
        this.contentH = contentH;

        var player = MinecraftClient.getInstance().player;
        if (player == null)
            return;
        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
        this.draftPoints = data.getUnspentPoints();

        if (data.getPlayerClass() == null || data.getPlayerClass().isEmpty()) {
            choosingClass = true;

            // Close Button removed (handled by CompendiumScreen)

            var classes = com.cranks.dungeons.config.PassiveTreeConfig.getClasses();
            classLoreWidth = calculateClassLoreWidth(classes);
            int loreWidth = classLoreWidth;
            int tabCount = classes.size() + 1;
            int availableTabWidth = Math.max(1, loreWidth - CLASS_TAB_SIDE_MARGIN * 2);
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            List<Integer> tabWidths = new ArrayList<>();
            tabWidths.add(getPreferredClassTabWidth(textRenderer, "Overview"));
            for (var def : classes) {
                tabWidths.add(getPreferredClassTabWidth(textRenderer, def.name()));
            }

            List<List<Integer>> tabRows = new ArrayList<>();
            List<Integer> currentRow = new ArrayList<>();
            int currentRowWidth = 0;
            for (int tabIndex = 0; tabIndex < tabCount; tabIndex++) {
                int width = Math.min(availableTabWidth, tabWidths.get(tabIndex));
                int nextWidth = currentRow.isEmpty() ? width
                        : currentRowWidth + CLASS_TAB_GAP + width;
                if (!currentRow.isEmpty() && nextWidth > availableTabWidth) {
                    tabRows.add(currentRow);
                    currentRow = new ArrayList<>();
                    currentRowWidth = 0;
                }
                currentRow.add(tabIndex);
                currentRowWidth += currentRow.size() == 1 ? width : CLASS_TAB_GAP + width;
            }
            if (!currentRow.isEmpty()) {
                tabRows.add(currentRow);
            }

            classTabRows = Math.max(1, tabRows.size());
            classTabAreaHeight = classTabRows * CLASS_TAB_HEIGHT;

            // Render the upper rows first so the rows directly below them
            // naturally sit on top where their tab edges meet.
            for (int row = tabRows.size() - 1; row >= 0; row--) {
                List<Integer> rowTabs = tabRows.get(row);
                int rowWidth = 0;
                for (int tabIndex : rowTabs) {
                    rowWidth += Math.min(availableTabWidth, tabWidths.get(tabIndex));
                }
                rowWidth += CLASS_TAB_GAP * Math.max(0, rowTabs.size() - 1);
                // Center shorter rows inside the same tab area so the first and
                // last tabs keep matching margins around the overview panel.
                int currentX = contentX + CLASS_TAB_SIDE_MARGIN
                        + Math.max(0, (availableTabWidth - rowWidth) / 2);
                // Keep the first row attached to the panel. Additional rows
                // grow upward so newly added tabs never hang below it.
                int visualRow = tabRows.size() - 1 - row;
                int tabY = contentY + CLASS_TAB_TOP_OFFSET + CLASS_TAB_VERTICAL_NUDGE
                        + visualRow * CLASS_TAB_HEIGHT;

                for (int tabIndex : rowTabs) {
                    int tabWidth = Math.min(availableTabWidth, tabWidths.get(tabIndex));
                    int tabX = currentX;
                    currentX += tabWidth + CLASS_TAB_GAP;

                    if (tabIndex == 0) {
                    CompendiumButtonWidget overviewBtn = CompendiumButtonWidget.builder(Text.literal("Overview"), b -> {
                        selectedClassTab = 0;
                        cacheDirty = true;
                        int previewCenterX = contentX + contentW / 2;
                        int previewCenterY = contentY + contentH / 2;
                        panX = previewCenterX;
                        panY = previewCenterY;
                        parent.refreshTab();
                    }).tab().selected(selectedClassTab == 0).selectedColor(CompendiumStyle.TEXT)
                            .dimensions(tabX, tabY, tabWidth, CLASS_TAB_HEIGHT).build();
                    overviewBtn.active = !showingConfirmPopup;
                    parent.addBackgroundTabWidget(overviewBtn);
                    } else {
                        int index = tabIndex;
                        var def = classes.get(index - 1);
                        CompendiumButtonWidget classBtn = CompendiumButtonWidget.builder(Text.literal(def.name()), b -> {
                        selectedClassTab = index;
                        cacheDirty = true;

                        // Pan to this class's starting node for preview
                        PassiveNode startNode = PassiveNodeRegistry.getNode(def.startNode());
                        if (startNode != null) {
                            int previewCenterX = contentX + loreWidth + (contentW - loreWidth) / 2;
                            int previewCenterY = contentY + contentH / 2;
                            panX = previewCenterX - (startNode.gridX() * BASE_GRID_SCALE * zoom);
                            panY = previewCenterY - (startNode.gridY() * BASE_GRID_SCALE * zoom);
                        }

                        parent.refreshTab();
                    }).tab().selected(selectedClassTab == index).selectedColor(CompendiumStyle.TEXT)
                            .dimensions(tabX, tabY, tabWidth, CLASS_TAB_HEIGHT).build();
                        classBtn.active = !showingConfirmPopup;
                        parent.addBackgroundTabWidget(classBtn);
                    }
                }
            }

            // Selection Button (only on class tabs)
            if (selectedClassTab > 0 && selectedClassTab <= classes.size()) {
                var currentDef = classes.get(selectedClassTab - 1);
                CompendiumButtonWidget selectBtn = CompendiumButtonWidget.builder(Text.literal("Select This Path"), b -> {
                    showingConfirmPopup = true;
                    classPendingConfirmation = currentDef.id();
                    parent.refreshTab();
                }).dimensions(contentX + 20, contentY + contentH - 46, loreWidth - 40, 25).build();
                selectBtn.active = !showingConfirmPopup;
                parent.addTabWidget(selectBtn);
            }

            if (showingConfirmPopup) {
                // Confirmation Popup Buttons
                int btnW = 120;
                int centerX = contentX + (contentW - btnW) / 2;
                int ppy = contentY + (contentH - 120) / 2;

                this.confirmBtn = parent.addTabWidget(CompendiumButtonWidget.builder(Text.literal("Confirm Path"), b -> {
                    if (classPendingConfirmation != null) {
                        selectClass(classPendingConfirmation);
                    }
                }).dimensions(centerX, ppy + 65, btnW, 20).build());

                this.cancelBtn = parent.addTabWidget(CompendiumButtonWidget.builder(Text.literal("Cancel"), b -> {
                    showingConfirmPopup = false;
                    parent.refreshTab();
                }).dimensions(centerX, ppy + 90, btnW, 20).build());
            }
        } else {
            choosingClass = false;

            // Apply Button
            parent.addTabWidget(CompendiumButtonWidget.builder(Text.literal("Apply Changes"), b -> applyChanges())
                    .dimensions(contentX + contentW / 2 - 105, contentY + contentH - 30, 100, 20).build());

            // Reset Button
            parent.addTabWidget(CompendiumButtonWidget.builder(Text.literal("Reset Draft"), b -> resetDraft())
                    .dimensions(contentX + contentW / 2 + 5, contentY + contentH - 30, 100, 20).build());

            // Stats Toggle Button
            parent.addTabWidget(
                    CompendiumButtonWidget.builder(Text.literal(showStatsPanel ? "Hide Stats" : "Show Stats"), b -> {
                                showStatsPanel = !showStatsPanel;
                                parent.refreshTab(); // Refresh button text
                            })
                            .dimensions(contentX + contentW - 105, contentY + 5, 100, 20).build());

            // Close Button removed (handled by CompendiumScreen)

            // Center the view on the starting node for their class (only if not already
            // panned)
            if (panX == 0 && panY == 0) {
                String startId = data.getPlayerClass() + "_start";
                PassiveNode start = PassiveNodeRegistry.getNode(startId);
                panX = contentX + contentW / 2.0;
                panY = contentY + contentH / 2.0;
            }

            // Designer tooling is an explicit local development opt-in.
            if (com.cranks.dungeons.CranksDungeons.CONFIG.enableTreeDesigner()) {
                parent.addTabWidget(CompendiumButtonWidget.builder(Text.literal(editMode ? "Designer: ON" : "Designer: OFF"), b -> {
                    editMode = !editMode;
                    if (!editMode) draggedNode = null;
                    parent.refreshTab();
                }).dimensions(contentX + 10, contentY + contentH - 30, 90, 20).build());
            } else {
                editMode = false;
            }

            if (editMode && com.cranks.dungeons.CranksDungeons.CONFIG.enableTreeDesigner()) {
                parent.addTabWidget(CompendiumButtonWidget.builder(Text.literal("Save to Config"), b -> {
                    com.cranks.dungeons.config.PassiveTreeConfig.saveNodesToFile(PassiveNodeRegistry.getNodes().values());
                    var client = MinecraftClient.getInstance();
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("Changes saved to passive_tree.json!").formatted(Formatting.GREEN), false);
                    }
                }).dimensions(contentX + 105, contentY + contentH - 30, 100, 20).build());

                parent.addTabWidget(CompendiumButtonWidget.builder(
                                Text.literal(snapToGrid ? "Grid Snap: ON" : "Grid Snap: OFF"),
                                b -> toggleSnapToGrid())
                        .dimensions(contentX + 210, contentY + contentH - 30, 100, 20).build());
            }
        }
    }

    public void refreshCachesProxy() {
        this.cacheDirty = true;
    }

    private void toggleSnapToGrid() {
        snapToGrid = !snapToGrid;
        parent.refreshTab();
    }

    private void selectClass(String classId) {
        // Send packet to server
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                new com.cranks.dungeons.network.SelectClassPayload(classId));

        // Optimistically update client
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
            data.setPlayerClass(classId);
            var def = com.cranks.dungeons.config.PassiveTreeConfig.getClasses().stream()
                    .filter(c -> c.id().equals(classId))
                    .findFirst().orElse(null);
            if (def != null && def.startNode() != null) {
                data.unlockNode(def.startNode());
            }
        }

        // Close the selection panel
        choosingClass = false;
        showingConfirmPopup = false;
        cacheDirty = true;

        // Reset pan so it centers on the new class start node
        panX = 0;
        panY = 0;

        parent.refreshTab();
    }

    private void applyChanges() {
        if (!draftUnlocked.isEmpty()) {
            var player = MinecraftClient.getInstance().player;
            if (player != null) {
                PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
                
                // Validation
                if (draftUnlocked.size() > data.getUnspentPoints()) {
                    player.sendMessage(Text.literal("Not enough points to apply draft!").formatted(Formatting.RED), false);
                    return;
                }

                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                        new com.cranks.dungeons.network.BulkUnlockNodesPayload(new java.util.HashMap<>(draftUnlocked)));

                // Keep the draft visible until the synced component confirms the unlock.
                // This avoids briefly reverting the tree to its pre-apply colors and also
                // leaves a rejected draft available for correction or retrying.
                cacheDirty = true;
            }
        }
    }

    private void selectActiveChoice(net.minecraft.entity.player.PlayerEntity player, String choice) {
        String nodeId = activeChoiceNodeId;
        if (nodeId == null) return;

        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
        if (data.hasNode(nodeId)) {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new com.cranks.dungeons.network.UpdateNodeChoicePayload(nodeId, choice, false));
        } else {
            boolean newlyDrafted = !draftUnlocked.containsKey(nodeId);
            draftUnlocked.put(nodeId, choice);
            if (newlyDrafted) draftPoints--;
        }

        player.playSound(ModSounds.PASSIVE_NODE_SELECT, 0.6f, 1.2f);
        this.cacheDirty = true;
    }

    private void resetDraft() {
        draftUnlocked.clear();
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
            draftPoints = data.getUnspentPoints();
        }
        cacheDirty = true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        
        // Dim the background if not choosing class, else completely transparent (we dim left side only later)
        // Background fill removed to allow CompendiumScreen's dimmed world to show through


        var player = MinecraftClient.getInstance().player;
        if (player == null)
            return;

        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
        Set<String> unlocked = data.getUnlockedNodes();

        boolean confirmedDraft = draftUnlocked.keySet().removeIf(unlocked::contains);
        if (confirmedDraft
                || lastObservedUnspentPoints != data.getUnspentPoints()
                || !lastObservedUnlockedNodes.equals(unlocked)) {
            lastObservedUnspentPoints = data.getUnspentPoints();
            lastObservedUnlockedNodes.clear();
            lastObservedUnlockedNodes.addAll(unlocked);
            cacheDirty = true;
        }
        this.draftPoints = data.getUnspentPoints() - draftUnlocked.size();

        if (!choosingClass) {
            if (editMode) {
                context.drawTextWithShadow(textRenderer, "§b[DESIGNER MODE ACTIVE]", contentX + 10, contentY + 22, 0x55FFFF);
                context.drawTextWithShadow(textRenderer,
                        "Grid Snap: " + (snapToGrid ? "§aON" : "§cOFF") + " §7(Shift to toggle)",
                        contentX + 10, contentY + 34, 0xFFFFFF);
                if (draggedNode != null) {
                    String coordText = String.format("Dragging: %s (X: %.2f, Y: %.2f)", draggedNode.name(), draggedNode.gridX(), draggedNode.gridY());
                    context.drawTextWithShadow(textRenderer, coordText, contentX + 10, contentY + 46, 0xAAFFAA);
                } else if (isConnecting && connectionSource != null) {
                    context.drawTextWithShadow(textRenderer, "Connecting: " + connectionSource.id() + " -> Click another node", contentX + 10, contentY + 46, 0xFFFFAA);

                    // Draw Ghost Line
                    double currentGridScale = BASE_GRID_SCALE * zoom;
                    double sx = panX + connectionSource.gridX() * currentGridScale;
                    double sy = panY + connectionSource.gridY() * currentGridScale;
                    drawOptimizedLineGhost(context, (float)sx, (float)sy, (float)mouseX, (float)mouseY, 0x80FFFFFF, 2.0f, 0.5f);
                } else {
                    context.drawTextWithShadow(textRenderer, "L-Click: Drag | R-Click: Create/Edit | Shift+R-Click: Delete", contentX + 10, contentY + 46, 0xAAAAAA);
                }
            }
        }

        // --- SPATIAL CULLING SETUP ---
        double currentGridScale = BASE_GRID_SCALE * zoom;
        double currentNodeSize = BASE_NODE_SIZE * zoom;
        int screenW = contentX + contentW;
        int screenH = contentY + contentH;
        int margin = (int) (currentNodeSize * 2.0);
        int treeTop = contentY + TREE_TOP_MARGIN;
        boolean mouseInTree = mouseX >= contentX && mouseX < screenW
                && mouseY >= treeTop && mouseY < screenH;

        if (cacheDirty) {
            refreshCaches(data);
            cacheDirty = false;
        }

        // --- FIND HOVERED NODE (For tooltips and highlighting) ---
        PassiveNode hovered = null;
        Map<String, PassiveNode> allNodesMap = PassiveNodeRegistry.getNodes();

        // Prevent hover interactions when a choice popup is active or when hovering the class tablet panel / top tabs / confirm popup
        boolean hoveringClassPanel = choosingClass
                && (mouseX < contentX + getClassLoreWidth()
                || mouseY < contentY + CLASS_TAB_TOP_OFFSET + classTabAreaHeight
                || showingConfirmPopup);
        if (activeChoiceNodeId == null && !hoveringClassPanel && mouseInTree) {
            for (PassiveNode node : allNodesMap.values()) {
                double nx = panX + node.gridX() * currentGridScale;
                double ny = panY + node.gridY() * currentGridScale;

                // Culling for mouse check
                if (nx < contentX - margin || nx > screenW + margin || ny < treeTop - margin || ny > screenH + margin) {
                    continue;
                }

                double multiplier = getNodeSizeMultiplier(node.shape());
                double actualNodeSize = currentNodeSize * multiplier;

                boolean isHovered = mouseX >= nx - actualNodeSize / 2 && mouseX <= nx + actualNodeSize / 2 &&
                        mouseY >= ny - actualNodeSize / 2 && mouseY <= ny + actualNodeSize / 2;
                if (isHovered) {
                    hovered = node;
                    break;
                }
            }
        }

        // --- CALCULATE HOVER PATH ---
        nodesOnHoverPath.clear();
        if (hovered != null) {
            String currentId = hovered.id();
            Integer dist = distanceMap.get(currentId);
            if (dist != null && dist > 0) {
                traceHoverPath(currentId, dist);
            } else if (dist != null && dist == 0) {
                nodesOnHoverPath.add(currentId);
            }
        }

        // Scissor for tree rendering so it doesn't bleed outside the compendium content area
        int scissorX = contentX;
        context.draw();
        context.enableScissor(scissorX, treeTop, contentX + contentW, contentY + contentH);

        // --- DRAW TEXTURED CONNECTIONS ---
        for (Edge edge : edgeCache) {
            float x1 = (float) (panX + edge.n1.gridX() * currentGridScale);
            float y1 = (float) (panY + edge.n1.gridY() * currentGridScale);
            float x2 = (float) (panX + edge.n2.gridX() * currentGridScale);
            float y2 = (float) (panY + edge.n2.gridY() * currentGridScale);

            // Spatial Culling
            if ((x1 < contentX - margin && x2 < contentX - margin) || (x1 > screenW + margin && x2 > screenW + margin) ||
                    (y1 < treeTop - margin && y2 < treeTop - margin) || (y1 > screenH + margin && y2 > screenH + margin)) {
                continue;
            }

            String connectionState = "locked";

            boolean n1Unlocked = unlocked.contains(edge.n1.id());
            boolean n2Unlocked = unlocked.contains(edge.n2.id());
            boolean n1Drafted = draftUnlocked.containsKey(edge.n1.id());
            boolean n2Drafted = draftUnlocked.containsKey(edge.n2.id());

            boolean n1Active = n1Unlocked || n1Drafted;
            boolean n2Active = n2Unlocked || n2Drafted;

            if (n1Unlocked && n2Unlocked) {
                connectionState = "unlocked";
            } else if (n1Active && n2Active) {
                connectionState = "drafted";
            } else if ((n1Active && canUnlockCache.contains(edge.n2.id()))
                    || (n2Active && canUnlockCache.contains(edge.n1.id()))) {
                connectionState = "available";
            }

            // Full-Path Hover logic
            boolean n1OnPath = nodesOnHoverPath.contains(edge.n1.id());
            boolean n2OnPath = nodesOnHoverPath.contains(edge.n2.id());
            boolean connectionHovered = false;

            if (n1OnPath && n2OnPath) {
                int d1 = distanceMap.getOrDefault(edge.n1.id(), -1);
                int d2 = distanceMap.getOrDefault(edge.n2.id(), -1);
                if (Math.abs(d1 - d2) == 1) {
                    connectionHovered = true;
                }
            }

            boolean connectionSelected = selectedNodeIds.contains(edge.n1.id())
                    || selectedNodeIds.contains(edge.n2.id());
            // Node sprites have transparent padding around their opaque art;
            // trim to the visible edge rather than the full sprite radius.
            float startInset = (float) (currentNodeSize * getNodeSizeMultiplier(edge.n1.shape()) * 0.38);
            float endInset = (float) (currentNodeSize * getNodeSizeMultiplier(edge.n2.shape()) * 0.38);
            CompendiumStyle.drawTreeConnectionTexture(context,
                    CompendiumStyle.treeConnectionTexture(connectionState), x1, y1, x2, y2, zoom,
                    startInset, endInset);
            if (connectionHovered || connectionSelected) {
                // A dedicated connection_selection.png is not present yet;
                // use the hovered overlay until that optional asset is added.
                CompendiumStyle.drawTreeConnectionTexture(context,
                        CompendiumStyle.treeConnectionTexture("hovered"), x1, y1, x2, y2, zoom,
                        startInset, endInset);
            }
        }

        // --- DRAW TEXTURED NODES ---
        for (PassiveNode node : allNodesMap.values()) {
            double nx = panX + node.gridX() * currentGridScale;
            double ny = panY + node.gridY() * currentGridScale;

            // Spatial Culling
            if (nx < contentX - margin || nx > screenW + margin || ny < treeTop - margin || ny > screenH + margin) {
                continue;
            }

            boolean isUnlocked = unlocked.contains(node.id()) || draftUnlocked.containsKey(node.id());
            boolean isDraft = draftUnlocked.containsKey(node.id());
            if (choosingClass && selectedClassTab > 0 && selectedClassTab <= com.cranks.dungeons.config.PassiveTreeConfig.getClasses().size()) {
                String startNodeId = com.cranks.dungeons.config.PassiveTreeConfig.getClasses().get(selectedClassTab - 1).startNode();
                if (node.id().equals(startNodeId)) {
                    isDraft = true;
                }
            }
            boolean canUnlock = canUnlockCache.contains(node.id());

            double multiplier = getNodeSizeMultiplier(node.shape());
            double actualNodeSize = currentNodeSize * multiplier;

            boolean isHovered = (hovered != null && node.id().equals(hovered.id()));

            if (node.shape() != null && !node.shape().isEmpty()) {
                String nodeState = "locked";
                if (isDraft) {
                    nodeState = "drafted";
                } else if (unlocked.contains(node.id())) {
                    nodeState = "unlocked";
                } else if (canUnlock) {
                    nodeState = "available";
                }

                CompendiumStyle.drawTreeNodeTexture(context,
                        CompendiumStyle.treeNodeTexture(node.shape(), nodeState), nx, ny, actualNodeSize);
                if (isHovered) {
                    CompendiumStyle.drawTreeNodeTexture(context,
                            CompendiumStyle.treeNodeTexture(node.shape(), "hovered"), nx, ny, actualNodeSize);
                }
                if (selectedNodeIds.contains(node.id())) {
                    CompendiumStyle.drawTreeNodeTexture(context,
                            CompendiumStyle.treeNodeSelectionTexture(node.shape()), nx, ny, actualNodeSize);
                }
            }
        }

        // --- DRAW NODE ICONS (ALL ZOOM LEVELS) ---
        for (PassiveNode node : allNodesMap.values()) {
            double nx = panX + node.gridX() * currentGridScale;
            double ny = panY + node.gridY() * currentGridScale;

            if (nx < contentX - margin || nx > screenW + margin || ny < treeTop - margin || ny > screenH + margin) {
                continue;
            }

            double multiplier = getNodeSizeMultiplier(node.shape());
            double actualNodeSize = currentNodeSize * multiplier;

            // Icons below this size are not readable and their draw calls are
            // disproportionately expensive when thousands of nodes are visible.
            if (zoom < 0.5 && actualNodeSize < 7.0) {
                continue;
            }

            Identifier iconToDraw = node.icon();
            if (node.isAttributeSelection()) {
                String choice = null;
                if (unlocked.contains(node.id())) {
                    choice = ModComponents.PASSIVE_DATA.get(player).getNodeChoice(node.id());
                } else if (draftUnlocked.containsKey(node.id())) {
                    choice = draftUnlocked.get(node.id());
                }

                if ("strength".equals(choice)) iconToDraw = Identifier.of("cranks-dungeons", "passive/strength");
                else if ("dexterity".equals(choice)) iconToDraw = Identifier.of("cranks-dungeons", "passive/dexterity");
                else if ("intelligence".equals(choice)) iconToDraw = Identifier.of("cranks-dungeons", "passive/intelligence");
            }

            if (iconToDraw != null) {
                String iconPath = iconToDraw.getPath();
                if (!iconPath.equals("passive/circle") && !iconPath.equals("passive/square") &&
                        !iconPath.equals("passive/diamond") && !iconPath.equals("passive/hexagon") &&
                        !iconPath.equals("passive/generic")) {

                    double iconSize = actualNodeSize * 0.90;
                    drawPrecisionTexture(context, iconToDraw, nx, ny, iconSize, 0xFFFFFFFF);
                }
            }
        }

        context.draw();
        context.disableScissor();

        if (!choosingClass) {
            String className = data.getPlayerClass();
            String displayClass = (className == null || className.isEmpty())
                    ? "None"
                    : className.substring(0, 1).toUpperCase() + className.substring(1);
            context.drawTextWithShadow(textRenderer,
                    "Class: " + displayClass,
                    contentX + 10, contentY + 10, 0xFFFFFF);

            String pointsText = "§6Unspent Points: §a" + draftPoints;
            context.drawCenteredTextWithShadow(textRenderer, pointsText, contentX + contentW / 2, contentY + contentH - 48, 0xFFFFAA00);

            if (isSelectingBox) {
                int x1 = (int) selectionStartX;
                int y1 = (int) selectionStartY;
                int x2 = (int) selectionCurrentX;
                int y2 = (int) selectionCurrentY;

                int minX = Math.min(x1, x2);
                int maxX = Math.max(x1, x2);
                int minY = Math.min(y1, y2);
                int maxY = Math.max(y1, y2);

                context.fill(minX, minY, maxX, maxY, 0x40FFAA00); // Amber selection fill
                context.fill(minX, minY, maxX, minY + 1, 0xFFFFAA00);
                context.fill(minX, maxY - 1, maxX, maxY, 0xFFFFAA00);
                context.fill(minX, minY, minX + 1, maxY, 0xFFFFAA00);
                context.fill(maxX - 1, minY, maxX, maxY, 0xFFFFAA00);
            }

            if (showStatsPanel) {
                renderStatsPanel(context);
            }

            if (activeChoiceNodeId != null) {
                int cx = choicePopupX;
                int cy = choicePopupY;

                double amount = 0.0;
                PassiveNode activeNode = PassiveNodeRegistry.getNodes().get(activeChoiceNodeId);
                if (activeNode != null) {
                    amount = activeNode.attributeSelectionAmount();
                }
                String amountStr = (amount == (long) amount) ? String.format("%d", (long) amount) : String.format("%s", amount);
                String currentChoice = "";
                var popupPlayer = MinecraftClient.getInstance().player;
                if (popupPlayer != null) {
                    PassiveDataComponent popupData = ModComponents.PASSIVE_DATA.get(popupPlayer);
                    currentChoice = popupData.hasNode(activeChoiceNodeId)
                            ? popupData.getNodeChoice(activeChoiceNodeId)
                            : draftUnlocked.getOrDefault(activeChoiceNodeId, "");
                }

                // Use the shared vanilla-style panel around the choice buttons.
                CompendiumStyle.drawInsetPanel(context, cx - 4, cy - 4, 118, 72);

                // Button STR
                boolean hoverStr = mouseX >= cx && mouseX <= cx + 110 && mouseY >= cy && mouseY <= cy + 20;
                CompendiumButtonWidget.drawButton(context, cx, cy, 110, 20, hoverStr, true,
                        "strength".equals(currentChoice), "+" + amountStr + " Strength");

                // Button DEX
                boolean hoverDex = mouseX >= cx && mouseX <= cx + 110 && mouseY >= cy + 22 && mouseY <= cy + 42;
                CompendiumButtonWidget.drawButton(context, cx, cy + 22, 110, 20, hoverDex, true,
                        "dexterity".equals(currentChoice), "+" + amountStr + " Dexterity");

                // Button INT
                boolean hoverInt = mouseX >= cx && mouseX <= cx + 110 && mouseY >= cy + 44 && mouseY <= cy + 64;
                CompendiumButtonWidget.drawButton(context, cx, cy + 44, 110, 20, hoverInt, true,
                        "intelligence".equals(currentChoice), "+" + amountStr + " Intelligence");
            }
        }

        if (choosingClass) {
            // Class tabs must sit behind the overview panel, while still
            // appearing above the tree that fills the surrounding space.
            parent.renderBackgroundTabWidgets(context, mouseX, mouseY, delta);

            int loreWidth = getClassLoreWidth();
            var classes = com.cranks.dungeons.config.PassiveTreeConfig.getClasses();

            int loreX = contentX + 6;
            int loreY = contentY + CLASS_TAB_TOP_OFFSET + classTabAreaHeight;
            int loreW = loreWidth - 12;
            int loreH = Math.max(1, contentH - classTabAreaHeight - CLASS_TAB_TOP_OFFSET - 6);
            int loreTextX = loreX + 14;
            int loreTextW = loreW - 28;
            int loreTitleX = loreX + 8;
            int loreTitleY = loreY + 6;
            int loreBottom = loreY + loreH - 14;
            CompendiumStyle.drawAdvancementWindow(context, loreX, loreY, loreW, loreH);

            if (selectedClassTab == 0) {
                Text title = Text.literal("Path Selection").formatted(Formatting.GOLD, Formatting.BOLD);
                context.drawText(textRenderer, title, loreTitleX, loreTitleY, 0xFFFFAA00, false);

                String intro = "Choose a starting class to begin your journey.\n\nEach class provides a unique starting point on the passive tree and specialized early attributes.\n\nYour choice is permanent!";
                drawWrappedText(context, Text.literal(intro), loreTextX, loreY + 30, loreTextW,
                        CompendiumStyle.TEXT_DARK, false, loreBottom);
            } else if (selectedClassTab > 0 && selectedClassTab <= classes.size()) {
                var def = classes.get(selectedClassTab - 1);
                int classColor = parseHexColor(def.color());

                // Title
                Text title = Text.literal(def.name()).formatted(Formatting.BOLD);
                context.drawText(textRenderer, title, loreTitleX, loreTitleY, classColor, false);

                // Subtitle
                int subtitleY = loreY + 24;
                int subtitleBottom = drawWrappedText(
                        context, Text.literal(def.subtitle()).formatted(Formatting.GRAY, Formatting.ITALIC),
                        loreTextX, subtitleY, loreTextW, CompendiumStyle.TEXT_DISABLED, false, loreBottom);

                // Detail Description
                int recommendedReserve = 40;
                int descriptionY = subtitleBottom + 6;
                int descriptionBottom = drawWrappedText(context, Text.literal(def.description()), loreTextX,
                        descriptionY, loreTextW, CompendiumStyle.TEXT_DARK, false,
                        Math.max(descriptionY, loreBottom - recommendedReserve));

                // Recommended Stats Section
                int recY = Math.min(descriptionBottom + 8, loreBottom - 28);
                context.drawText(textRenderer, Text.literal("Recommended Stats:").formatted(Formatting.BOLD),
                        loreTextX, recY, classColor, false);
                drawWrappedText(context, Text.literal(def.recommendedStats()), loreTextX, recY + 15, loreTextW,
                        CompendiumStyle.TEXT_DISABLED, false, loreBottom);
            }

            if (showingConfirmPopup) {
                context.fill(contentX, contentY, contentX + contentW, contentY + contentH, 0xAA000000); // Darken background

                // Use the same vanilla window frame for the confirmation modal.
                int popupW = 240;
                int popupH = 120;
                int ppx = contentX + (contentW - popupW) / 2;
                int ppy = contentY + (contentH - popupH) / 2;
                
                CompendiumStyle.drawAdvancementWindow(context, ppx, ppy, popupW, popupH);

                int centerX = contentX + contentW / 2;
                String classTitle = (classPendingConfirmation != null ? classPendingConfirmation.substring(0, 1).toUpperCase() + classPendingConfirmation.substring(1) : "Path");
                Text popupTitle = Text.literal("Path of the " + classTitle).formatted(Formatting.GOLD, Formatting.BOLD);
                context.drawText(textRenderer, popupTitle, centerX - textRenderer.getWidth(popupTitle) / 2, ppy + 15, 0xFFFFAA00, false);

                String msg = "Are you sure? This decision is PERMANENT!";
                drawWrappedText(context, Text.literal(msg), centerX, ppy + 40, popupW - 20,
                        CompendiumStyle.TEXT_DARK, true);
            }
        }

        // Render hovered node tooltip on top of everything
        if (hovered != null) {
            String title = "§6" + hovered.name();
            if (draftUnlocked.containsKey(hovered.id()))
                title = "§b[DRAFTED] §6" + hovered.name();
            else if (unlocked.contains(hovered.id()))
                title = "§e[UNLOCKED] §6" + hovered.name();
            else if (canUnlockCache.contains(hovered.id()))
                title = "§a[AVAILABLE] §6" + hovered.name();

            java.util.List<Text> tooltipTexts = new java.util.ArrayList<>();
            tooltipTexts.add(Text.literal(title));
            if (hovered.description() != null && !hovered.description().isEmpty()) {
                String[] lines = hovered.description().split("\n");
                for (String line : lines) {
                    tooltipTexts.add(Text.literal("§7" + line));
                }
            }
            context.drawTooltip(textRenderer, tooltipTexts, mouseX, mouseY);
        }
    }

    private int drawWrappedText(DrawContext context, Text text, int x, int y, int maxWidth, int color,
                                boolean centered) {
        return drawWrappedText(context, text, x, y, maxWidth, color, centered, Integer.MAX_VALUE);
    }

    private int drawWrappedText(DrawContext context, Text text, int x, int y, int maxWidth, int color,
                                boolean centered, int maxY) {
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        var lines = textRenderer.wrapLines(text, maxWidth);
        int currentY = y;
        for (var line : lines) {
            if (currentY + 9 > maxY) {
                break;
            }
            int drawX = centered ? x - textRenderer.getWidth(line) / 2 : x;
            context.drawText(textRenderer, line, drawX, currentY, color, false);
            currentY += 10;
        }
        return currentY;
    }

    private int getClassLoreWidth() {
        return Math.min(Math.max(1, contentW - 24), classLoreWidth);
    }

    private int getPreferredClassTabWidth(TextRenderer textRenderer, String label) {
        return Math.max(CLASS_TAB_MIN_WIDTH,
                Math.min(CLASS_TAB_MAX_WIDTH, textRenderer.getWidth(label) + 20));
    }

    private int calculateClassLoreWidth(
            List<com.cranks.dungeons.config.PassiveTreeConfig.ClassDefinition> classes) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int tabTotalWidth = getPreferredClassTabWidth(textRenderer, "Overview");
        for (var def : classes) {
            tabTotalWidth += CLASS_TAB_GAP + getPreferredClassTabWidth(textRenderer, def.name());
        }

        // Leave the same amount of space on both sides of the complete tab
        // strip. If the strip becomes too wide, the panel cap forces rows.
        int desiredWidth = tabTotalWidth + CLASS_TAB_SIDE_MARGIN * 2 + CLASS_TAB_FRAME_BALANCE;
        int availableWidth = Math.max(1, contentW - 24);
        return Math.min(availableWidth,
                Math.min(CLASS_PANEL_MAX_WIDTH, Math.max(320, desiredWidth)));
    }

    private int parseHexColor(String hex) {
        try {
            return 0xFF000000 | Integer.parseInt(hex.replace("#", ""), 16);
        } catch (Exception e) {
            return 0xFF000000; // Default to Black
        }
    }

    private void renderStatsPanel(DrawContext context) {
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int panelWidth = 230;
        int panelHeight = (int) (contentH * 0.7);
        int panelX = contentX + contentW - panelWidth - 10;
        int panelY = contentY + (contentH - panelHeight) / 2;

        // Use vanilla widget textures for the non-interactive stats menu.
        CompendiumStyle.drawVanillaMenuPanel(context, panelX, panelY, panelWidth, panelHeight);

        int ty = panelY + 10;
        context.drawCenteredTextWithShadow(textRenderer, "§6Tree Summary", panelX + panelWidth / 2, ty, 0xFFFFAA00);
        ty += 18;

        var stats = PassiveStatHelper.getDisplayableSummary(MinecraftClient.getInstance().player, draftUnlocked);
        var baseStats = PassiveStatHelper.getDisplayableSummary(MinecraftClient.getInstance().player, null);

        if (stats.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, "No stats unlocked", panelX + panelWidth / 2,
                    ty + 20, CompendiumStyle.TEXT_DISABLED);
        } else {
            for (Map.Entry<String, Double> entry : stats.entrySet()) {
                String key = entry.getKey();
                double total = entry.getValue();
                if (total == 0)
                    continue;

                double baseTotal = baseStats.getOrDefault(key, 0.0);
                boolean isDrafted = (total != baseTotal);
                
                // Aesthetic Compendium Colors
                int valColor = isDrafted ? CompendiumStyle.TEXT_SUCCESS : CompendiumStyle.TEXT;
                int keyColor = isDrafted ? CompendiumStyle.TEXT_SUCCESS : CompendiumStyle.TEXT_MUTED;

                double displayVal = total;
                boolean isPercent = key.toLowerCase().contains("percent") ||
                        key.toLowerCase().contains("chance") ||
                        key.toLowerCase().contains("speed") ||
                        key.toLowerCase().contains("extra") ||
                        key.toLowerCase().contains("reduction") ||
                        key.toLowerCase().contains("steal") ||
                        key.toLowerCase().contains("resistance") ||
                        key.toLowerCase().contains("multiplier") ||
                        key.toLowerCase().contains("exp") ||
                        key.toLowerCase().contains("knockback") ||
                        key.toLowerCase().contains("fortune");

                if (isPercent) {
                    displayVal *= 100.0;
                }

                String valStr = String.format("%+.1f", displayVal);
                if (isPercent) {
                    valStr += "%";
                } else {
                    valStr += PassiveStatHelper.getStatSuffix(key);
                }

                int valW = textRenderer.getWidth(valStr);
                int maxKeyWidth = panelWidth - 24 - valW - 6;
                String displayKey = textRenderer.trimToWidth(key, maxKeyWidth);
                if (!displayKey.equals(key) && displayKey.length() > 3) {
                    displayKey = textRenderer.trimToWidth(displayKey, maxKeyWidth - textRenderer.getWidth("...")) + "...";
                }

                context.drawTextWithShadow(textRenderer, displayKey + ":", panelX + 12, ty, keyColor);
                context.drawTextWithShadow(textRenderer, valStr,
                        panelX + panelWidth - 12 - valW, ty, valColor);
                ty += 14;

                if (ty > panelY + panelHeight - 15)
                    break; // Simple culling
            }
        }
    }

    private void refreshCaches(PassiveDataComponent data) {
        edgeCache.clear();
        canUnlockCache.clear();
        adjacencyMap.clear();
        distanceMap.clear();
        Map<String, PassiveNode> allNodes = PassiveNodeRegistry.getNodes();
        Set<String> unlocked = data.getUnlockedNodes();

        String previewStartNode = null;
        if (choosingClass && selectedClassTab > 0 && selectedClassTab <= com.cranks.dungeons.config.PassiveTreeConfig.getClasses().size()) {
            previewStartNode = com.cranks.dungeons.config.PassiveTreeConfig.getClasses().get(selectedClassTab - 1).startNode();
        }

        // 1. Build Unified Adjacency Map for bi-directional pathing
        for (PassiveNode node : allNodes.values()) {
            for (String connId : node.connections()) {
                if (allNodes.containsKey(connId)) {
                    adjacencyMap.computeIfAbsent(node.id(), k -> new HashSet<>()).add(connId);
                    adjacencyMap.computeIfAbsent(connId, k -> new HashSet<>()).add(node.id());
                }
            }
        }

        // 2. Populate Logic Caches (Edge + Unlock)
        for (PassiveNode node : allNodes.values()) {
            // Edge Cache (For visual lines)
            for (String connId : node.connections()) {
                if (node.id().compareTo(connId) < 0) {
                    PassiveNode connected = allNodes.get(connId);
                    if (connected != null) {
                        edgeCache.add(new Edge(node, connected));
                    }
                }
            }

            // Unlock Cache (Reachability logic)
            boolean isRootActive = unlocked.contains(node.id()) || draftUnlocked.containsKey(node.id()) || (previewStartNode != null && previewStartNode.equals(node.id()));
            if (!isRootActive && (draftPoints > 0 || choosingClass)) {
                Set<String> neighbors = adjacencyMap.get(node.id());
                if (neighbors != null) {
                    for (String conn : neighbors) {
                        if (unlocked.contains(conn) || draftUnlocked.containsKey(conn) || (previewStartNode != null && previewStartNode.equals(conn))) {
                            canUnlockCache.add(node.id());
                            break;
                        }
                    }
                }
            }
        }

        // 3. Calculate Distances from active tree (BFS)
        java.util.Queue<String> queue = new java.util.LinkedList<>();
        for (String id : allNodes.keySet()) {
            if (unlocked.contains(id) || draftUnlocked.containsKey(id) || (previewStartNode != null && previewStartNode.equals(id))) {
                distanceMap.put(id, 0);
                queue.add(id);
            }
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentDist = distanceMap.get(current);
            Set<String> neighbors = adjacencyMap.get(current);
            if (neighbors != null) {
                for (String neighbor : neighbors) {
                    if (!distanceMap.containsKey(neighbor)) {
                        distanceMap.put(neighbor, currentDist + 1);
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    private void traceHoverPath(String nodeId, int dist) {
        nodesOnHoverPath.add(nodeId);
        if (dist <= 0)
            return;

        Set<String> neighbors = adjacencyMap.get(nodeId);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                Integer nDist = distanceMap.get(neighbor);
                if (nDist != null && nDist == dist - 1) {
                    traceHoverPath(neighbor, nDist);
                }
            }
        }
    }

    private void drawOptimizedLineGhost(DrawContext context, float x1, float y1, float x2, float y2, int color, float width, float alpha) {
        ShapeRenderer.drawLine(context, x1, y1, x2, y2, width, (color & 0x00FFFFFF) | ((int) (alpha * 255) << 24));
    }

    private double getNodeSizeMultiplier(String shape) {
        if (shape == null)
            return 1.0;
        return switch (shape.toLowerCase()) {
            case "hexagon" -> 3.2; // Massive Keystone
            case "diamond" -> 2.2; // Notable
            case "circle" -> 1.4; // Minor Notable
            default -> 1.0; // Travel/Square
        };
    }


    private void drawPrecisionTexture(DrawContext context, Identifier texture, double cx, double cy, double size, int color) {
        var matrices = context.getMatrices();
        matrices.push();
        // Translate with full float precision
        matrices.translate((float) cx, (float) cy, 0.0f);

        // Scale a fixed integer base to achieve sub-pixel float size
        float baseSize = 256.0f;
        float scale = (float) (size / baseSize);
        matrices.scale(scale, scale, 1.0f);

        int drawW = (int) baseSize;
        int drawH = (int) baseSize;
        int drawX = -drawW / 2;
        int drawY = -drawH / 2;

        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = ((color >> 24) & 0xFF) / 255.0F;

        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(r, g, b, a);
        context.drawGuiTexture(
                texture,
                drawX, drawY, drawW, drawH);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        matrices.pop();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (showingConfirmPopup) return false;
        if (activeChoiceNodeId != null) return true;
        double zoomFactor = verticalAmount > 0 ? 1.1 : 0.9;
        double nextZoom = Math.max(0.1, Math.min(2.0, zoom * zoomFactor));

        // Zoom toward mouse
        double targetX = (mouseX - panX) / zoom;
        double targetY = (mouseY - panY) / zoom;

        zoom = nextZoom;
        panX = mouseX - targetX * zoom;
        panY = mouseY - targetY * zoom;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (showingConfirmPopup) return false;
        if (suppressTreeDragUntilRelease || activeChoiceNodeId != null) {
            draggedNode = null;
            isSelectingBox = false;
            return true;
        }
        if (choosingClass) {
            // If drag is on the left class panel or top tab bar, don't drag the tree
            if (mouseX < contentX + getClassLoreWidth()
                    || mouseY < contentY + CLASS_TAB_TOP_OFFSET + classTabAreaHeight) {
                return false;
            }
        } else if (editMode) {
            if (isSelectingBox) {
                selectionCurrentX = mouseX;
                selectionCurrentY = mouseY;
                return true;
            }
            if (draggedNode != null) {
                double currentGridScale = BASE_GRID_SCALE * zoom;
                double moveX = deltaX / currentGridScale;
                double moveY = deltaY / currentGridScale;

                if (selectedNodeIds.contains(draggedNode.id())) {
                    for (String id : selectedNodeIds) {
                        PassiveNode node = PassiveNodeRegistry.getNode(id);
                        if (node != null) {
                            node.setGridX(node.gridX() + moveX);
                            node.setGridY(node.gridY() + moveY);
                        }
                    }
                } else {
                    draggedNode.setGridX(draggedNode.gridX() + moveX);
                    draggedNode.setGridY(draggedNode.gridY() + moveY);
                }
                this.cacheDirty = true;
                return true;
            }
        }
        panX += deltaX;
        panY += deltaY;
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (suppressTreeDragUntilRelease || activeChoiceNodeId != null) {
            draggedNode = null;
            isSelectingBox = false;
            suppressTreeDragUntilRelease = false;
            return true;
        }
        if (button == 0) {
            if (isSelectingBox) {
                double minX = Math.min(selectionStartX, selectionCurrentX);
                double maxX = Math.max(selectionStartX, selectionCurrentX);
                double minY = Math.min(selectionStartY, selectionCurrentY);
                double maxY = Math.max(selectionStartY, selectionCurrentY);

                double currentGridScale = BASE_GRID_SCALE * zoom;
                Map<String, PassiveNode> nodesMap = PassiveNodeRegistry.getNodes();
                for (PassiveNode node : nodesMap.values()) {
                    double nx = panX + node.gridX() * currentGridScale;
                    double ny = panY + node.gridY() * currentGridScale;

                    if (nx >= minX && nx <= maxX && ny >= minY && ny <= maxY) {
                        selectedNodeIds.add(node.id());
                    }
                }
                isSelectingBox = false;
            }
            if (draggedNode != null) {
                if (snapToGrid) {
                    double snap = 0.1;
                    if (selectedNodeIds.contains(draggedNode.id())) {
                        for (String id : selectedNodeIds) {
                            PassiveNode node = PassiveNodeRegistry.getNode(id);
                            if (node != null) {
                                node.setGridX(Math.round(node.gridX() / snap) * snap);
                                node.setGridY(Math.round(node.gridY() / snap) * snap);
                            }
                        }
                    } else {
                        draggedNode.setGridX(Math.round(draggedNode.gridX() / snap) * snap);
                        draggedNode.setGridY(Math.round(draggedNode.gridY() / snap) * snap);
                    }
                    this.cacheDirty = true;
                }
                draggedNode = null;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        var client = MinecraftClient.getInstance();

        if (editMode && (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            toggleSnapToGrid();
            return true;
        }

        if (keyCode == 258 && com.cranks.dungeons.CranksDungeons.CONFIG.enableTreeDesigner()) { // TAB key
            editMode = !editMode;
            if (!editMode) {
                draggedNode = null;
                isConnecting = false;
                connectionSource = null;
                selectedNodeIds.clear();
            }
            parent.refreshTab();
            return true;
        }

        if (editMode && !selectedNodeIds.isEmpty()) {
            double snap = 0.1;
            if (keyCode == GLFW.GLFW_KEY_UP) {
                for (String id : selectedNodeIds) { PassiveNode n = PassiveNodeRegistry.getNode(id); if (n != null) n.setGridY(n.gridY() - snap); }
                this.cacheDirty = true; return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                for (String id : selectedNodeIds) { PassiveNode n = PassiveNodeRegistry.getNode(id); if (n != null) n.setGridY(n.gridY() + snap); }
                this.cacheDirty = true; return true;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                for (String id : selectedNodeIds) { PassiveNode n = PassiveNodeRegistry.getNode(id); if (n != null) n.setGridX(n.gridX() - snap); }
                this.cacheDirty = true; return true;
            }
            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                for (String id : selectedNodeIds) { PassiveNode n = PassiveNodeRegistry.getNode(id); if (n != null) n.setGridX(n.gridX() + snap); }
                this.cacheDirty = true; return true;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT_BRACKET || keyCode == GLFW.GLFW_KEY_RIGHT_BRACKET) {
                double angle = Math.toRadians(keyCode == GLFW.GLFW_KEY_RIGHT_BRACKET ? 15 : -15);

                // Calculate center
                double avgX = 0, avgY = 0;
                int count = 0;
                for (String id : selectedNodeIds) {
                    PassiveNode node = PassiveNodeRegistry.getNode(id);
                    if (node != null) {
                        avgX += node.gridX();
                        avgY += node.gridY();
                        count++;
                    }
                }

                if (count > 0) {
                    avgX /= count;
                    avgY /= count;

                    for (String id : selectedNodeIds) {
                        PassiveNode node = PassiveNodeRegistry.getNode(id);
                        if (node != null) {
                            double relX = node.gridX() - avgX;
                            double relY = node.gridY() - avgY;

                            double cos = Math.cos(angle);
                            double sin = Math.sin(angle);

                            double newX = avgX + (relX * cos - relY * sin);
                            double newY = avgY + (relX * sin + relY * cos);

                            node.setGridX(newX);
                            node.setGridY(newY);
                        }
                    }
                    this.cacheDirty = true;
                    return true;
                }
            }
        }

        return false;
    }

    public void startConnecting(PassiveNode source) {
        this.connectionSource = source;
        this.isConnecting = true;
        // Previously: this.client.setScreen(this);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var client = MinecraftClient.getInstance();
        var player = client.player;
        if (player == null) return false;

        // Popup input must be handled before designer/tree hit-testing so nodes
        // underneath the overlay cannot become drag targets.
        if (activeChoiceNodeId != null) {
            suppressTreeDragUntilRelease = true;
            int cx = choicePopupX;
            int cy = choicePopupY;
            if (mouseX >= cx && mouseX <= cx + 110 && mouseY >= cy && mouseY <= cy + 20) {
                selectActiveChoice(player, "strength");
                return true;
            }
            if (mouseX >= cx && mouseX <= cx + 110 && mouseY >= cy + 22 && mouseY <= cy + 42) {
                selectActiveChoice(player, "dexterity");
                return true;
            }
            if (mouseX >= cx && mouseX <= cx + 110 && mouseY >= cy + 44 && mouseY <= cy + 64) {
                selectActiveChoice(player, "intelligence");
                return true;
            }
            activeChoiceNodeId = null;
            return true;
        }

        // Since mouseReleased is not part of the standard interface, we can simulate it on Action_UP if we had it,
        // but for now, we'll handle just the logic we have in mouseClicked.
        // Wait, mouseReleased exists in standard Screen but not in CompendiumTabRenderer interface.
        // In the original, there was mouseReleased. Let me add mouseReleased to renderer? Or just wire it inside CompendiumScreen?
        // We will just keep it as a helper here, but it's not strictly part of the interface yet.
        // Actually wait, let me look at the interface again.

        if (editMode) {
            double currentGridScale = BASE_GRID_SCALE * zoom;
            double currentNodeSize = BASE_NODE_SIZE * zoom;
            Map<String, PassiveNode> nodesMap = PassiveNodeRegistry.getNodes();
            PassiveNode hitNode = null;

            for (PassiveNode node : nodesMap.values()) {
                double nx = panX + node.gridX() * currentGridScale;
                double ny = panY + node.gridY() * currentGridScale;
                double sz = currentNodeSize * getNodeSizeMultiplier(node.shape()) / 2.0;

                if (mouseX >= nx - sz && mouseX <= nx + sz && mouseY >= ny - sz && mouseY <= ny + sz) {
                    hitNode = node;
                    break;
                }
            }

            if (button == 0) { // Left Click: Select/Drag
                if (hitNode != null) {
                    if (isConnecting && connectionSource != null && connectionSource != hitNode) {
                        // Toggle Connection
                        if (connectionSource.connections().contains(hitNode.id())) {
                            connectionSource.connections().remove(hitNode.id());
                            hitNode.connections().remove(connectionSource.id());
                        } else {
                            connectionSource.connections().add(hitNode.id());
                            hitNode.connections().add(connectionSource.id());
                        }
                        isConnecting = false;
                        connectionSource = null;
                        this.cacheDirty = true;
                        player.playSound(SoundEvents.BLOCK_ANVIL_PLACE, 0.4f, 1.2f);
                    } else {
                        boolean shiftHeld = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ||
                                InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
                        if (shiftHeld) {
                            // Toggle selection
                            if (selectedNodeIds.contains(hitNode.id())) {
                                selectedNodeIds.remove(hitNode.id());
                            } else {
                                selectedNodeIds.add(hitNode.id());
                            }
                        } else {
                            // Normal click
                            if (!selectedNodeIds.contains(hitNode.id())) {
                                selectedNodeIds.clear();
                                selectedNodeIds.add(hitNode.id());
                            }
                            draggedNode = hitNode;
                        }
                        return true;
                    }
                } else {
                    // Clicking empty space
                    if (!(InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ||
                            InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT))) {
                        selectedNodeIds.clear();
                    }
                    isSelectingBox = true;
                    selectionStartX = mouseX;
                    selectionStartY = mouseY;
                    selectionCurrentX = mouseX;
                    selectionCurrentY = mouseY;
                    return true;
                }
            } else if (button == 1) { // Right Click: Open Editor
                if (isConnecting) {
                    isConnecting = false;
                    connectionSource = null;
                    return true;
                }
                if (hitNode != null) {
                    client.setScreen(new PassiveNodeEditScreen(parent, hitNode, this::refreshCachesProxy, node -> startConnecting(node)));
                } else {
                    // CREATE NEW NODE
                    double gx = (mouseX - panX) / currentGridScale;
                    double gy = (mouseY - panY) / currentGridScale;
                    client.setScreen(new PassiveNodeEditScreen(parent, gx, gy, this::refreshCachesProxy, node -> startConnecting(node)));
                }
                return true;
            }
        }

        if (!choosingClass && button == 0) {
            PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
            Set<String> unlocked = data.getUnlockedNodes();

            double currentGridScale = BASE_GRID_SCALE * zoom;
            double currentNodeSize = BASE_NODE_SIZE * zoom;

            Map<String, PassiveNode> nodes = PassiveNodeRegistry.getNodes();
            for (PassiveNode node : nodes.values()) {
                double multiplier = getNodeSizeMultiplier(node.shape());
                double actualNodeSize = currentNodeSize * multiplier;
                int halfSize = (int) (actualNodeSize / 2.0);

                double nx = panX + node.gridX() * currentGridScale;
                double ny = panY + node.gridY() * currentGridScale;

                if (mouseX >= nx - halfSize && mouseX <= nx + halfSize && mouseY >= ny - halfSize
                        && mouseY <= ny + halfSize) {
                    if (draftUnlocked.containsKey(node.id())) {
                        if (node.isAttributeSelection()
                                && !InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)
                                && !InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                            activeChoiceNodeId = node.id();
                            choicePopupX = (int) mouseX;
                            choicePopupY = (int) mouseY - 25;
                            player.playSound(ModSounds.PASSIVE_NODE_SELECT, 0.6f, 1.2f);
                            return true;
                        }
                        // Toggle OFF: Unselect drafted node
                        draftUnlocked.remove(node.id());
                        draftPoints++;
                        player.playSound(ModSounds.PASSIVE_NODE_SELECT, 0.5f, 0.8f); // Pitched down for deselect
                        this.cacheDirty = true;
                        revalidateDraft();
                        return true;
                    }

                    boolean isPermanent = unlocked.contains(node.id());
                    if (isPermanent) {
                        if (node.isAttributeSelection()) {
                            activeChoiceNodeId = node.id();
                            choicePopupX = (int) mouseX;
                            choicePopupY = (int) mouseY - 25;
                            player.playSound(ModSounds.PASSIVE_NODE_SELECT, 0.6f, 1.2f);
                        }
                        return true;
                    }

                    // Node is locked and not in draft: try to select it (Unified check)
                    boolean canUnlock = false;
                    Set<String> neighbors = adjacencyMap.get(node.id());
                    if (neighbors != null) {
                        for (String conn : neighbors) {
                            if (unlocked.contains(conn) || draftUnlocked.containsKey(conn)) {
                                canUnlock = true;
                                break;
                            }
                        }
                    }

                    if (draftPoints > 0 && canUnlock) {
                        if (node.isAttributeSelection()) {
                            activeChoiceNodeId = node.id();
                            choicePopupX = (int) mouseX;
                            choicePopupY = (int) mouseY - 25;
                            player.playSound(com.cranks.dungeons.registry.ModSounds.PASSIVE_NODE_SELECT, 0.6f, 1.2f);
                        } else {
                            draftUnlocked.put(node.id(), "");
                            draftPoints--;
                            player.playSound(com.cranks.dungeons.registry.ModSounds.PASSIVE_NODE_SELECT, 0.6f, 1.2f); // Pitched up for select
                            this.cacheDirty = true;
                        }
                    }
                    return true;
                }
            }
        } else if (!choosingClass && button == 1) { // Right Click (Designer Mode)
            if (editMode) {
                if (draggedNode != null) {
                    if (snapToGrid) {
                        double snap = 0.1;
                        if (selectedNodeIds.contains(draggedNode.id())) {
                            for (String id : selectedNodeIds) {
                                PassiveNode n = PassiveNodeRegistry.getNode(id);
                                if (n != null) {
                                    n.setGridX(Math.round(n.gridX() / snap) * snap);
                                    n.setGridY(Math.round(n.gridY() / snap) * snap);
                                }
                            }
                        } else {
                            draggedNode.setGridX(Math.round(draggedNode.gridX() / snap) * snap);
                            draggedNode.setGridY(Math.round(draggedNode.gridY() / snap) * snap);
                        }
                        this.cacheDirty = true;
                    }
                    draggedNode = null; // Drop
                    player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4f, 0.5f);
                } else {
                    // Pick up
                    double currentGridScale = BASE_GRID_SCALE * zoom;
                    double currentNodeSize = BASE_NODE_SIZE * zoom;
                    Map<String, PassiveNode> nodes = PassiveNodeRegistry.getNodes();
                    for (PassiveNode node : nodes.values()) {
                        double nx = panX + node.gridX() * currentGridScale;
                        double ny = panY + node.gridY() * currentGridScale;
                        double sz = currentNodeSize * getNodeSizeMultiplier(node.shape()) / 2.0;

                        if (mouseX >= nx - sz && mouseX <= nx + sz && mouseY >= ny - sz && mouseY <= ny + sz) {
                            draggedNode = node;
                            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4f, 1.5f);
                            return true;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void revalidateDraft() {
        var player = MinecraftClient.getInstance().player;
        if (player == null)
            return;

        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
        Set<String> permanent = data.getUnlockedNodes();

        Set<String> stillReachable = new HashSet<>(permanent);
        java.util.Queue<String> queue = new java.util.LinkedList<>(permanent);

        Map<String, PassiveNode> allNodes = PassiveNodeRegistry.getNodes();

        // BFS for reachability
        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            PassiveNode currentNode = allNodes.get(currentId);
            if (currentNode == null)
                continue;

            Set<String> neighbors = adjacencyMap.get(currentId);
            if (neighbors != null) {
                for (String neighborId : neighbors) {
                    if (!stillReachable.contains(neighborId) && draftUnlocked.containsKey(neighborId)) {
                        stillReachable.add(neighborId);
                        queue.add(neighborId);
                    }
                }
            }
        }

        // Prune orphans
        java.util.Iterator<Map.Entry<String, String>> it = draftUnlocked.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            if (!stillReachable.contains(entry.getKey())) {
                it.remove();
                draftPoints++;
                this.cacheDirty = true;
            }
        }
    }
}
