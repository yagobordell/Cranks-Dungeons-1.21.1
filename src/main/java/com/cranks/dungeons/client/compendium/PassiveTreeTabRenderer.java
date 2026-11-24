package com.cranks.dungeons.client.compendium;

import com.cranks.dungeons.client.screen.PassiveNodeEditScreen;
import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.component.PassiveDataComponent;
import com.cranks.dungeons.config.PassiveTreeConfig;
import com.cranks.dungeons.network.SelectClassPayload;
import com.cranks.dungeons.stat.passive.PassiveNode;
import com.cranks.dungeons.stat.passive.PassiveNodeRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

import net.minecraft.client.gui.DrawContext;
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
    private final TreeNodeSearch search = new TreeNodeSearch(PassiveNodeRegistry::getNodes, true);
    private final com.cranks.dungeons.client.CharacterStatsPanel statsPanel = new com.cranks.dungeons.client.CharacterStatsPanel();
    private Map<String, Double> displayedDraftBonuses = Map.of();
    private Map<String, String> lastStatsDraft;
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

    private final CompendiumClassPicker classPicker = new CompendiumClassPicker();

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
        int statsWidth = Math.min(174, Math.max(100, contentW / 2));
        statsPanel.init(MinecraftClient.getInstance().textRenderer,
                contentX + contentW - statsWidth - 7, contentY + 31, statsWidth, Math.max(60, contentH - 107));
        lastStatsDraft = null;

        cacheDirty = true;

        var player = MinecraftClient.getInstance().player;
        if (player == null)
            return;
        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
        this.draftPoints = data.getUnspentPoints();

        choosingClass = data.getPlayerClass() == null || data.getPlayerClass().isEmpty();
        if (choosingClass) {
            search.detach();
            classPicker.init(parent, contentX, contentY, contentW, contentH, PassiveTreeConfig.getClasses(),
                    def -> centerOn(PassiveNodeRegistry.getNode(def.startNode())), def -> selectClass(def.id()));
        } else {
            initTreeButtons();
            search.init(parent, contentX, contentY, contentW,
                    () -> activeChoiceNodeId == null);

            // Stats Toggle Button
            parent.addTabWidget(
                    CompendiumButtonWidget.builder(Text.literal(showStatsPanel ? "Hide Stats" : "Show Stats"), b -> {
                                showStatsPanel = !showStatsPanel;
                                parent.refreshTab(); // Refresh button text
                            })
                            .dimensions(contentX + contentW - 105, contentY + 5, 100, 20).build());

            // Close Button removed (handled by CompendiumScreen)

            if (panX == 0 && panY == 0) {
                var def = PassiveTreeConfig.getClassDef(data.getPlayerClass());
                centerOn(def == null ? null : PassiveNodeRegistry.getNode(def.startNode()));
            }

        }
    }

    private void initTreeButtons() {
        boolean designerEnabled = com.cranks.dungeons.CranksDungeons.CONFIG.enableTreeDesigner();
        if (!designerEnabled) editMode = false;
        int bottom = contentY + contentH - 30;
        var apply = CompendiumButtonWidget.builder(Text.literal("Apply Changes"), b -> applyChanges());
        var reset = CompendiumButtonWidget.builder(Text.literal("Reset Draft"), b -> resetDraft());
        if (!designerEnabled) {
            parent.addTreeButtons(contentX, bottom, contentW, apply, reset);
            return;
        }

        var designer = CompendiumButtonWidget.builder(Text.literal(editMode ? "Designer: ON" : "Designer: OFF"),
                b -> toggleDesigner());
        if (!editMode) {
            parent.addTreeButtons(contentX, bottom, contentW, designer, apply, reset);
            return;
        }

        var save = CompendiumButtonWidget.builder(Text.literal("Save to Config"), b -> {
            com.cranks.dungeons.config.PassiveTreeConfig.saveNodesToFile(PassiveNodeRegistry.getNodes().values());
            var player = MinecraftClient.getInstance().player;
            if (player != null) {
                player.sendMessage(Text.literal("Changes saved to passive_tree.json!").formatted(Formatting.GREEN), false);
            }
        });
        var snap = CompendiumButtonWidget.builder(Text.literal(snapToGrid ? "Grid Snap: ON" : "Grid Snap: OFF"),
                b -> toggleSnapToGrid());
        parent.addTreeButtons(contentX, bottom - 25, contentW, designer, save, snap);
        parent.addTreeButtons(contentX, bottom, contentW, apply, reset);
    }

    private void toggleDesigner() {
        editMode = !editMode;
        if (!editMode) {
            draggedNode = null;
            isConnecting = false;
            connectionSource = null;
            isSelectingBox = false;
            selectedNodeIds.clear();
        }
        parent.refreshTab();
    }

    public void refreshCachesProxy() {
        this.cacheDirty = true;
    }

    private void toggleSnapToGrid() {
        snapToGrid = !snapToGrid;
        parent.refreshTab();
    }

    private void centerOn(PassiveNode start) {
        panX = contentX + contentW / 2.0;
        panY = contentY + contentH / 2.0;
        if (start != null) {
            panX -= start.gridX() * BASE_GRID_SCALE * zoom;
            panY -= start.gridY() * BASE_GRID_SCALE * zoom;
        }
        cacheDirty = true;
    }

    private void selectClass(String classId) {
        ClientPlayNetworking.send(new SelectClassPayload(classId));
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
        var player = MinecraftClient.getInstance().player;
        if (player == null) return;

        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
        boolean noClass = data.getPlayerClass() == null || data.getPlayerClass().isEmpty();
        if (noClass != choosingClass) {
            panX = 0;
            panY = 0;
            parent.refreshTab();
        }
        if (choosingClass && !classPicker.isPreviewing()) {
            classPicker.render(context);
            return;
        }

        CompendiumStyle.drawPanel(context, contentX, contentY, contentW, contentH);
        var textRenderer = MinecraftClient.getInstance().textRenderer;
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
                context.drawTextWithShadow(textRenderer, "Tree Designer", contentX + 10, contentY + 22, CompendiumStyle.TEXT);
                context.drawTextWithShadow(textRenderer,
                        "Grid Snap: " + (snapToGrid ? "On" : "Off") + " (Shift to toggle)",
                        contentX + 10, contentY + 34, 0xFFFFFF);
                if (draggedNode != null) {
                    String coordText = String.format("Dragging: %s (X: %.2f, Y: %.2f)", draggedNode.name(), draggedNode.gridX(), draggedNode.gridY());
                    context.drawTextWithShadow(textRenderer, coordText, contentX + 10, contentY + 46, CompendiumStyle.TEXT_MUTED);
                } else if (isConnecting && connectionSource != null) {
                    context.drawTextWithShadow(textRenderer, "Connecting: " + connectionSource.id() + " -> Click another node", contentX + 10, contentY + 46, CompendiumStyle.TEXT_MUTED);

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
        int treeTop = contentY;
        boolean mouseInTree = mouseX >= contentX && mouseX < screenW
                && mouseY >= treeTop && mouseY < screenH;

        if (cacheDirty) {
            refreshCaches(data);
            cacheDirty = false;
        }

        // --- FIND HOVERED NODE (For tooltips and highlighting) ---
        PassiveNode hovered = null;
        Map<String, PassiveNode> allNodesMap = PassiveNodeRegistry.getNodes();

        boolean hoveringClassToolbar = choosingClass && mouseY < contentY + 31;
        var previewClass = classPicker.previewClass();
        String previewStart = choosingClass && previewClass != null ? previewClass.startNode() : null;
        if ((!showStatsPanel || !statsPanel.contains(mouseX, mouseY)) && !search.contains(mouseX, mouseY) && activeChoiceNodeId == null && !hoveringClassToolbar && mouseInTree) {
            for (PassiveNode node : allNodesMap.values()) {
                double nx = panX + node.gridX() * currentGridScale;
                double ny = panY + node.gridY() * currentGridScale;

                // Culling for mouse check
                if (nx < contentX - margin || nx > screenW + margin || ny < treeTop - margin || ny > screenH + margin) {
                    continue;
                }

                double actualNodeSize = TreeSpriteBatch.nodeSize(node.shape(), currentNodeSize);

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
        if (hovered != null) TreePath.traceHover(nodesOnHoverPath, hovered.id(), adjacencyMap, distanceMap);
        Set<String> matchingNodes = choosingClass ? Set.of() : search.matches();

        context.draw();
        context.enableScissor(contentX + 2, treeTop + 2, screenW - 2, screenH - 2);

        try (TreeSpriteBatch batch = new TreeSpriteBatch(context)) {
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
                float startInset = (float) (TreeSpriteBatch.nodeSize(edge.n1.shape(), currentNodeSize) * 0.38);
                float endInset = (float) (TreeSpriteBatch.nodeSize(edge.n2.shape(), currentNodeSize) * 0.38);
                batch.connection(
                        CompendiumStyle.treeConnectionTexture(connectionState), x1, y1, x2, y2, zoom,
                        startInset, endInset, false);
                if (connectionHovered || connectionSelected) {
                    // A dedicated connection_selection.png is not present yet;
                    // use the hovered overlay until that optional asset is added.
                    batch.connection(
                            CompendiumStyle.treeConnectionTexture("hovered"), x1, y1, x2, y2, zoom,
                            startInset, endInset, true);
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

                boolean isDraft = draftUnlocked.containsKey(node.id());
                if (node.id().equals(previewStart)) {
                    isDraft = true;
                }
                boolean canUnlock = canUnlockCache.contains(node.id());

                double actualNodeSize = TreeSpriteBatch.nodeSize(node.shape(), currentNodeSize);

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

                    batch.node(
                            CompendiumStyle.treeNodeTexture(node.shape(), nodeState), nx, ny, actualNodeSize);
                    if (isHovered && !matchingNodes.contains(node.id())) {
                        batch.node(
                                CompendiumStyle.treeNodeTexture(node.shape(), "hovered"), nx, ny, actualNodeSize);
                    }
                }
                if (matchingNodes.contains(node.id())) {
                    batch.node(CompendiumStyle.treeNodeTexture(node.shape(), "hovered"), nx, ny, actualNodeSize,
                            TreeNodeSearch.MATCH_TINT);
                }
                if (selectedNodeIds.contains(node.id())) {
                    batch.node(CompendiumStyle.treeNodeSelectionTexture(node.shape()), nx, ny, actualNodeSize);
                }
            }

            // --- DRAW NODE ICONS (ALL ZOOM LEVELS) ---
            for (PassiveNode node : allNodesMap.values()) {
                double nx = panX + node.gridX() * currentGridScale;
                double ny = panY + node.gridY() * currentGridScale;

                if (nx < contentX - margin || nx > screenW + margin || ny < treeTop - margin || ny > screenH + margin) {
                    continue;
                }

                double actualNodeSize = TreeSpriteBatch.nodeSize(node.shape(), currentNodeSize);

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

                batch.icon(iconToDraw, nx, ny, actualNodeSize);
            }
        } finally {
            context.disableScissor();
        }


        if (!choosingClass) {
            var classDef = PassiveTreeConfig.getClassDef(data.getPlayerClass());
            String displayClass = "Class: " + (classDef == null ? data.getPlayerClass() : classDef.name());
            context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(displayClass,
                    Math.max(1, contentW - TreeNodeSearch.widthFor(contentW) - 126)), contentX + 10, contentY + 10, 0xFFFFFF);

            CompendiumStyle.drawTreePoints(context, textRenderer, contentX + contentW / 2,
                    contentY + contentH - (editMode ? 73 : 48), draftPoints);

            if (isSelectingBox) {
                int x1 = (int) selectionStartX;
                int y1 = (int) selectionStartY;
                int x2 = (int) selectionCurrentX;
                int y2 = (int) selectionCurrentY;

                int minX = Math.min(x1, x2);
                int maxX = Math.max(x1, x2);
                int minY = Math.min(y1, y2);
                int maxY = Math.max(y1, y2);

                context.fill(minX, minY, maxX, maxY, 0x30696969);
                context.fill(minX, minY, maxX, minY + 1, 0xFFAAAAAA);
                context.fill(minX, maxY - 1, maxX, maxY, 0xFFAAAAAA);
                context.fill(minX, minY, minX + 1, maxY, 0xFFAAAAAA);
                context.fill(maxX - 1, minY, maxX, maxY, 0xFFAAAAAA);
            }

            if (showStatsPanel) {
                renderStatsPanel(context, mouseX, mouseY);
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

        if (choosingClass) classPicker.render(context);

        // Render hovered node tooltip on top of everything
        if (hovered != null) {
            String title = "§6" + hovered.name();
            if (draftUnlocked.containsKey(hovered.id()))
                title = "§b[DRAFTED] §6" + hovered.name();
            else if (unlocked.contains(hovered.id()))
                title = "§e[UNLOCKED] §6" + hovered.name();
            else if (canUnlockCache.contains(hovered.id()))
                title = "§a[AVAILABLE] §6" + hovered.name();

            context.drawTooltip(textRenderer, com.cranks.dungeons.client.guide.GuideNodeTooltip.of(hovered,title,false), mouseX, mouseY);
        }
    }

    @Override
    public void renderOverlay(DrawContext context, int mouseX, int mouseY, float delta) {
        if (choosingClass) classPicker.renderConfirmation(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean overlayKeyPressed(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && statsPanel.ownsFocus(parent.getFocused())) {
            parent.setFocused(null);
            return true;
        }
        return !choosingClass && search.keyPressed(keyCode);
    }

    @Override
    public void onDeactivated() {
        search.blur();
        if (statsPanel.ownsFocus(parent.getFocused())) parent.setFocused(null);
        statsPanel.endPointerInteraction();
    }

    private void renderStatsPanel(DrawContext context, int mouseX, int mouseY) {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return;
        if (!draftUnlocked.equals(lastStatsDraft)) {
            lastStatsDraft = new HashMap<>(draftUnlocked);
            displayedDraftBonuses = PassiveStatHelper.getDraftBonuses(player, draftUnlocked, false);
        }
        statsPanel.render(context, player, displayedDraftBonuses, mouseX, mouseY);
    }

    private void refreshCaches(PassiveDataComponent data) {
        edgeCache.clear();
        canUnlockCache.clear();
        adjacencyMap.clear();
        distanceMap.clear();
        Map<String, PassiveNode> allNodes = PassiveNodeRegistry.getNodes();
        Set<String> unlocked = data.getUnlockedNodes();

        var previewClass = classPicker.previewClass();
        String previewStartNode = choosingClass && previewClass != null ? previewClass.startNode() : null;

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
            for (String connId : adjacencyMap.getOrDefault(node.id(), Set.of())) {
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

    private void drawOptimizedLineGhost(DrawContext context, float x1, float y1, float x2, float y2, int color, float width, float alpha) {
        ShapeRenderer.drawLine(context, x1, y1, x2, y2, width, (color & 0x00FFFFFF) | ((int) (alpha * 255) << 24));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (showStatsPanel && activeChoiceNodeId == null && statsPanel.mouseScrolled(mouseX, mouseY, verticalAmount)) return true;
        if (search.contains(mouseX, mouseY)) return true;
        if (choosingClass && !classPicker.isPreviewing()) return classPicker.scroll(mouseX, verticalAmount);
        if (choosingClass && mouseY < contentY + 31) return false;
        if (activeChoiceNodeId != null) return true;
        if (verticalAmount == 0) return false;
        double zoomFactor = Math.pow(1.1, Math.clamp(verticalAmount, -32, 32));
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
        if (statsPanel.mouseDragged(mouseY, button) || search.isFocused()) return true;
        if (suppressTreeDragUntilRelease || activeChoiceNodeId != null) {
            draggedNode = null;
            isSelectingBox = false;
            return true;
        }
        if (choosingClass && (!classPicker.isPreviewing() || mouseY < contentY + 31)) return false;
        if (!choosingClass && editMode) {
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
        if (statsPanel.mouseReleased(button)) return true;
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
        if (choosingClass) return classPicker.keyPressed(keyCode);
        var client = MinecraftClient.getInstance();

        if (editMode && (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            toggleSnapToGrid();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_TAB && com.cranks.dungeons.CranksDungeons.CONFIG.enableTreeDesigner()) {
            toggleDesigner();
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
        if (search.contains(mouseX, mouseY) && activeChoiceNodeId == null) return true;
        search.blur();
        if (choosingClass) return false;
        if (showStatsPanel && activeChoiceNodeId == null && statsPanel.mouseClicked(mouseX, mouseY, button, parent::setFocused)) return true;
        if (statsPanel.ownsFocus(parent.getFocused())) parent.setFocused(null);
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
                double sz = TreeSpriteBatch.nodeSize(node.shape(), currentNodeSize) / 2.0;

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
                double actualNodeSize = TreeSpriteBatch.nodeSize(node.shape(), currentNodeSize);
                double halfSize = actualNodeSize / 2.0;

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
                        double sz = TreeSpriteBatch.nodeSize(node.shape(), currentNodeSize) / 2.0;

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
