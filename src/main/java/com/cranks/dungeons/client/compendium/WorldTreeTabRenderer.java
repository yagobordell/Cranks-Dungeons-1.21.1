package com.cranks.dungeons.client.compendium;

import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.component.WorldTreeDataComponent;
import com.cranks.dungeons.stat.passive.PassiveNode;
import com.cranks.dungeons.stat.passive.WorldNodeRegistry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.stat.passive.PassiveStatHelper;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.attribute.EntityAttribute;
import org.lwjgl.glfw.GLFW;
import com.cranks.dungeons.client.screen.WorldNodeEditScreen;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

public class WorldTreeTabRenderer implements CompendiumTabRenderer {
    // Extend only the top of the tree viewport; the side and bottom bounds
    // remain aligned with the existing inner panel.
    private CompendiumScreen parent;
    private int contentX, contentY, contentW, contentH;

    private double panX = 0;
    private double panY = 0;
    private double zoom = 0.75;
    private static final int BASE_NODE_SIZE = 10;
    private static final int BASE_GRID_SCALE = 60; // Pixels per grid unit



    private final Map<String, String> draftUnlocked = new HashMap<>();
    private int draftPoints;
    private final TreeNodeSearch search = new TreeNodeSearch(WorldNodeRegistry::getNodes, false);
    private final com.cranks.dungeons.client.CharacterStatsPanel statsPanel = new com.cranks.dungeons.client.CharacterStatsPanel();
    private Map<String, Double> displayedDraftBonuses = Map.of();
    private Map<String, String> lastStatsDraft;
    private boolean showStatsPanel = false;

    private String activeChoiceNodeId = null;
    private int choicePopupX = 0;
    private int choicePopupY = 0;
    private boolean suppressTreeDragUntilRelease = false;

    // --- Unified Logic & Rendering Caches ---
    private record Edge(PassiveNode n1, PassiveNode n2) {}

    private final Set<Edge> edgeCache = new HashSet<>();
    private final Set<String> canUnlockCache = new HashSet<>();
    private final Map<String, Set<String>> adjacencyMap = new HashMap<>();
    private final Map<String, Integer> distanceMap = new HashMap<>();
    private final Set<String> nodesOnHoverPath = new HashSet<>();
    private final Set<String> lastObservedUnlockedNodes = new HashSet<>();
    private int lastObservedUnspentPoints = Integer.MIN_VALUE;
    private boolean cacheDirty = true;

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
        if (player == null) return;
        WorldTreeDataComponent data = ModComponents.WORLD_TREE_DATA.get(player);
        this.draftPoints = data.getUnspentPoints();

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
            panX = contentX + contentW / 2.0;
            panY = contentY + contentH / 2.0;
            PassiveNode start = WorldNodeRegistry.getNode("world_start");
            if (start != null) {
                panX -= start.gridX() * BASE_GRID_SCALE * zoom;
                panY -= start.gridY() * BASE_GRID_SCALE * zoom;
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
            com.cranks.dungeons.config.WorldTreeConfig.saveNodesToFile(WorldNodeRegistry.getNodes().values());
            var player = MinecraftClient.getInstance().player;
            if (player != null) {
                player.sendMessage(Text.literal("Changes saved to world_tree.json!").formatted(Formatting.GREEN), false);
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
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4f, snapToGrid ? 1.4f : 1.0f);
        }
        parent.init();
    }

    private void applyChanges() {
        if (!draftUnlocked.isEmpty()) {
            var player = MinecraftClient.getInstance().player;
            if (player != null) {
                WorldTreeDataComponent data = ModComponents.WORLD_TREE_DATA.get(player);
                if (draftUnlocked.size() > data.getUnspentPoints()) {
                    player.sendMessage(Text.literal("Not enough points to apply draft!").formatted(Formatting.RED), false);
                    return;
                }

                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                        new com.cranks.dungeons.network.BulkUnlockWorldNodesPayload(new java.util.HashMap<>(draftUnlocked)));

                // Keep the draft visible until the synced component confirms the unlock.
                // This avoids briefly reverting the tree to its pre-apply colors and also
                // leaves a rejected draft available for correction or retrying.
                this.cacheDirty = true;
            }
        }
    }

    private void selectActiveChoice(net.minecraft.entity.player.PlayerEntity player, String choice) {
        String nodeId = activeChoiceNodeId;
        if (nodeId == null) return;

        WorldTreeDataComponent data = ModComponents.WORLD_TREE_DATA.get(player);
        if (data.hasNode(nodeId)) {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new com.cranks.dungeons.network.UpdateNodeChoicePayload(nodeId, choice, true));
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
            WorldTreeDataComponent data = ModComponents.WORLD_TREE_DATA.get(player);
            draftPoints = data.getUnspentPoints();
        }
        this.cacheDirty = true;
        parent.refreshTab();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        CompendiumStyle.drawPanel(context, contentX, contentY, contentW, contentH);
        // Background fill removed to allow CompendiumScreen's dimmed world to show through

        var player = MinecraftClient.getInstance().player;
        if (player == null) return;

        WorldTreeDataComponent data = ModComponents.WORLD_TREE_DATA.get(player);
        Set<String> unlocked = data.getUnlockedNodes();

        boolean confirmedDraft = draftUnlocked.keySet().removeIf(unlocked::contains);
        if (confirmedDraft
                || lastObservedUnspentPoints != data.getUnspentPoints()
                || !lastObservedUnlockedNodes.equals(unlocked)) {
            lastObservedUnspentPoints = data.getUnspentPoints();
            lastObservedUnlockedNodes.clear();
            lastObservedUnlockedNodes.addAll(unlocked);
            this.cacheDirty = true;
        }
        this.draftPoints = data.getUnspentPoints() - draftUnlocked.size();

        var textRenderer = MinecraftClient.getInstance().textRenderer;

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

                double currentGridScale = BASE_GRID_SCALE * zoom;
                double sx = panX + connectionSource.gridX() * currentGridScale;
                double sy = panY + connectionSource.gridY() * currentGridScale;
                ShapeRenderer.drawLine(context, (float) sx, (float) sy, mouseX, mouseY, 2.0f, 0x80929292);
            } else {
                context.drawTextWithShadow(textRenderer, "L-Click: Drag | R-Click: Create/Edit | Shift+R-Click: Delete", contentX + 10, contentY + 46, 0xAAAAAA);
            }
        }

        // --- SPATIAL CULLING SETUP ---
        double currentGridScale = BASE_GRID_SCALE * zoom;
        double currentNodeSize = BASE_NODE_SIZE * zoom;
        int margin = (int) (currentNodeSize + 10);
        int screenW = contentX + contentW;
        int screenH = contentY + contentH;
        int treeTop = contentY;
        boolean mouseInTree = (!showStatsPanel || !statsPanel.contains(mouseX, mouseY)) && !search.contains(mouseX, mouseY) && mouseX >= contentX && mouseX < screenW
                && mouseY >= treeTop && mouseY < screenH;

        if (cacheDirty) {
            refreshCaches(data);
            cacheDirty = false;
        }

        // --- PRE-CALCULATE HOVERED NODE ---
        PassiveNode hovered = null;
        Map<String, PassiveNode> allNodesMap = WorldNodeRegistry.getNodes();
        for (PassiveNode node : allNodesMap.values()) {
            double nx = panX + node.gridX() * currentGridScale;
            double ny = panY + node.gridY() * currentGridScale;

            if (nx < contentX - margin || nx > screenW + margin || ny < treeTop - margin || ny > screenH + margin) {
                continue;
            }

            double actualNodeSize = TreeSpriteBatch.nodeSize(node.shape(), currentNodeSize);

            boolean isHovered = mouseInTree && mouseX >= nx - actualNodeSize / 2 && mouseX <= nx + actualNodeSize / 2 &&
                    mouseY >= ny - actualNodeSize / 2 && mouseY <= ny + actualNodeSize / 2;
            if (isHovered) {
                hovered = node;
                break;
            }
        }

        nodesOnHoverPath.clear();
        if (hovered != null) TreePath.traceHover(nodesOnHoverPath, hovered.id(), adjacencyMap, distanceMap);
        Set<String> matchingNodes = search.matches();

        context.draw();
        context.enableScissor(contentX + 2, treeTop + 2, screenW - 2, screenH - 2);

        try (TreeSpriteBatch batch = new TreeSpriteBatch(context)) {
            // --- DRAW TEXTURED CONNECTIONS ---
            for (Edge edge : edgeCache) {
                float x1 = (float) (panX + edge.n1.gridX() * currentGridScale);
                float y1 = (float) (panY + edge.n1.gridY() * currentGridScale);
                float x2 = (float) (panX + edge.n2.gridX() * currentGridScale);
                float y2 = (float) (panY + edge.n2.gridY() * currentGridScale);

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

                if (nx < contentX - margin || nx > screenW + margin || ny < treeTop - margin || ny > screenH + margin) {
                    continue;
                }

                boolean isDraft = draftUnlocked.containsKey(node.id());
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
                        choice = ModComponents.WORLD_TREE_DATA.get(player).getNodeChoice(node.id());
                    } else if (draftUnlocked.containsKey(node.id())) {
                        choice = draftUnlocked.get(node.id());
                    }

                    if (choice != null) {
                        Identifier customIcon = com.cranks.dungeons.stat.passive.PassiveIconManager.getIconForModifier(choice);
                        if (customIcon != null) {
                            iconToDraw = customIcon;
                        }
                    }
                }

                batch.icon(iconToDraw, nx, ny, actualNodeSize);
            }
        } finally {
            context.disableScissor();
        }


        if (hovered != null) {
            String title = "§6" + hovered.name();
            if (draftUnlocked.containsKey(hovered.id()))
                title = "§b[DRAFTED] §6" + hovered.name();
            else if (unlocked.contains(hovered.id()))
                title = "§e[UNLOCKED] §6" + hovered.name();
            else if (canUnlockCache.contains(hovered.id()))
                title = "§a[AVAILABLE] §6" + hovered.name();

            context.drawTooltip(textRenderer, com.cranks.dungeons.client.guide.GuideNodeTooltip.of(hovered,title,true), mouseX, mouseY);
        }

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

            PassiveNode activeNode = WorldNodeRegistry.getNodes().get(activeChoiceNodeId);
            if (activeNode != null && activeNode.modifiers() != null) {
                String currentChoice = "";
                var popupPlayer = MinecraftClient.getInstance().player;
                if (popupPlayer != null) {
                    WorldTreeDataComponent popupData = ModComponents.WORLD_TREE_DATA.get(popupPlayer);
                    currentChoice = popupData.hasNode(activeChoiceNodeId)
                            ? popupData.getNodeChoice(activeChoiceNodeId)
                            : draftUnlocked.getOrDefault(activeChoiceNodeId, "");
                }
                int count = activeNode.modifiers().size();
                int bgHeight = 8 + (count * 22);

                // Draw 3D Beveled Stone Container
                ShapeRenderer.drawBeveledPanel(context, cx - 4, cy - 4, 158, bgHeight);

                int i = 0;
                for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : activeNode.modifiers().entrySet()) {
                    int btnY = cy + (i * 22);
                    boolean hoverBtn = mouseX >= cx && mouseX <= cx + 150 && mouseY >= btnY && mouseY <= btnY + 20;

                    String modId = entry.getKey().getKey().map(k -> k.getValue().getPath()).orElse("unknown");
                    double amount = entry.getValue();
                    String amountStr = (amount == (long) amount) ? String.format("%d", (long) amount) : String.format("%s", amount);

                    String displayName = modId;
                    if (displayName.endsWith("_yield")) displayName = displayName.replace("_yield", " Yield");
                    if (displayName.endsWith("_speed")) displayName = displayName.replace("_speed", " Speed");
                    if (displayName.endsWith("_chance")) displayName = displayName.replace("_chance", " Chance");
                    if (displayName.endsWith("_experience")) displayName = displayName.replace("_experience", " Exp");
                    displayName = java.util.Arrays.stream(displayName.split("_")).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1)).collect(java.util.stream.Collectors.joining(" "));

                    CompendiumButtonWidget.drawButton(context, cx, btnY, 150, 20, hoverBtn, true,
                            modId.equals(currentChoice), "+" + amountStr + " " + displayName);
                    i++;
                }
            }
        }
    }

    @Override
    public boolean overlayKeyPressed(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && statsPanel.ownsFocus(parent.getFocused())) {
            parent.setFocused(null);
            return true;
        }
        return search.keyPressed(keyCode);
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
            displayedDraftBonuses = PassiveStatHelper.getDraftBonuses(player, draftUnlocked, true);
        }
        statsPanel.render(context, player, displayedDraftBonuses, mouseX, mouseY);
    }

    private void refreshCaches(WorldTreeDataComponent data) {
        edgeCache.clear();
        canUnlockCache.clear();
        adjacencyMap.clear();
        distanceMap.clear();
        Map<String, PassiveNode> allNodes = WorldNodeRegistry.getNodes();
        Set<String> unlocked = data.getUnlockedNodes();

        for (PassiveNode node : allNodes.values()) {
            for (String connId : node.connections()) {
                if (allNodes.containsKey(connId)) {
                    adjacencyMap.computeIfAbsent(node.id(), k -> new HashSet<>()).add(connId);
                    adjacencyMap.computeIfAbsent(connId, k -> new HashSet<>()).add(node.id());
                }
            }
        }

        for (PassiveNode node : allNodes.values()) {
            for (String connId : adjacencyMap.getOrDefault(node.id(), Set.of())) {
                if (node.id().compareTo(connId) < 0) {
                    PassiveNode connected = allNodes.get(connId);
                    if (connected != null) {
                        edgeCache.add(new Edge(node, connected));
                    }
                }
            }

            if (!unlocked.contains(node.id()) && !draftUnlocked.containsKey(node.id()) && draftPoints > 0) {
                Set<String> neighbors = adjacencyMap.get(node.id());
                if (neighbors != null) {
                    for (String conn : neighbors) {
                        if (unlocked.contains(conn) || draftUnlocked.containsKey(conn)) {
                            canUnlockCache.add(node.id());
                            break;
                        }
                    }
                }
            }
        }

        java.util.Queue<String> queue = new java.util.LinkedList<>();
        for (String id : allNodes.keySet()) {
            if (unlocked.contains(id) || draftUnlocked.containsKey(id)) {
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (showStatsPanel && activeChoiceNodeId == null && statsPanel.mouseScrolled(mouseX, mouseY, verticalAmount)) return true;
        if (search.contains(mouseX, mouseY)) return true;
        if (activeChoiceNodeId != null) return true;
        if (verticalAmount == 0) return false;
        double zoomFactor = Math.pow(1.1, Math.clamp(verticalAmount, -32, 32));
        double nextZoom = Math.max(0.1, Math.min(2.0, zoom * zoomFactor));

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
        if (button == 0 || button == 2) { 
            if (editMode && button == 0) {
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
                            PassiveNode node = WorldNodeRegistry.getNode(id);
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
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (com.cranks.dungeons.client.ModKeybinds.OPEN_WORLD_TREE.matchesKey(keyCode, scanCode)) {
            parent.close();
            return true;
        }

        if (com.cranks.dungeons.client.ModKeybinds.OPEN_PASSIVE_TREE.matchesKey(keyCode, scanCode)) {
            parent.switchToTab(CompendiumTabRegistry.PASSIVE_TREE_TAB_ID);
            return true;
        }

        if (editMode && (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            toggleSnapToGrid();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_TAB && com.cranks.dungeons.CranksDungeons.CONFIG.enableTreeDesigner()) {
            toggleDesigner();
            return true;
        }

        if (editMode && !selectedNodeIds.isEmpty()) {
            double snap = 0.5;
            if (keyCode == GLFW.GLFW_KEY_UP) {
                for (String id : selectedNodeIds) { PassiveNode n = WorldNodeRegistry.getNode(id); if (n != null) n.setGridY(n.gridY() - snap); }
                this.cacheDirty = true; return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                for (String id : selectedNodeIds) { PassiveNode n = WorldNodeRegistry.getNode(id); if (n != null) n.setGridY(n.gridY() + snap); }
                this.cacheDirty = true; return true;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                for (String id : selectedNodeIds) { PassiveNode n = WorldNodeRegistry.getNode(id); if (n != null) n.setGridX(n.gridX() - snap); }
                this.cacheDirty = true; return true;
            }
            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                for (String id : selectedNodeIds) { PassiveNode n = WorldNodeRegistry.getNode(id); if (n != null) n.setGridX(n.gridX() + snap); }
                this.cacheDirty = true; return true;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT_BRACKET || keyCode == GLFW.GLFW_KEY_RIGHT_BRACKET) {
                double angle = Math.toRadians(keyCode == GLFW.GLFW_KEY_RIGHT_BRACKET ? 15 : -15);

                double avgX = 0, avgY = 0;
                int count = 0;
                for (String id : selectedNodeIds) {
                    PassiveNode node = WorldNodeRegistry.getNode(id);
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
                        PassiveNode node = WorldNodeRegistry.getNode(id);
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
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (search.contains(mouseX, mouseY) && activeChoiceNodeId == null) return true;
        search.blur();
        if (showStatsPanel && activeChoiceNodeId == null && statsPanel.mouseClicked(mouseX, mouseY, button, parent::setFocused)) return true;
        if (statsPanel.ownsFocus(parent.getFocused())) parent.setFocused(null);
        var player = MinecraftClient.getInstance().player;
        if (player == null) return false;

        // Popup input must be handled before designer/tree hit-testing so nodes
        // underneath the overlay cannot become drag targets.
        if (activeChoiceNodeId != null) {
            suppressTreeDragUntilRelease = true;
            int cx = choicePopupX;
            int cy = choicePopupY;

            PassiveNode activeNode = WorldNodeRegistry.getNodes().get(activeChoiceNodeId);
            if (activeNode != null && activeNode.modifiers() != null
                    && mouseX >= cx && mouseX <= cx + 150) {
                int i = 0;
                for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : activeNode.modifiers().entrySet()) {
                    int btnY = cy + (i * 22);
                    if (mouseY >= btnY && mouseY <= btnY + 20) {
                        String modId = entry.getKey().getKey()
                                .map(k -> k.getValue().getPath()).orElse("unknown");
                        selectActiveChoice(player, modId);
                        return true;
                    }
                    i++;
                }
            }

            activeChoiceNodeId = null;
            return true;
        }

        if (editMode) {
            double currentGridScale = BASE_GRID_SCALE * zoom;
            double currentNodeSize = BASE_NODE_SIZE * zoom;
            Map<String, PassiveNode> nodesMap = WorldNodeRegistry.getNodes();
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

            if (button == 0) { 
                if (hitNode != null) {
                    if (isConnecting && connectionSource != null && connectionSource != hitNode) {
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
                        boolean shiftHeld = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ||
                                InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
                        if (shiftHeld) {
                            if (selectedNodeIds.contains(hitNode.id())) {
                                selectedNodeIds.remove(hitNode.id());
                            } else {
                                selectedNodeIds.add(hitNode.id());
                            }
                        } else {
                            if (!selectedNodeIds.contains(hitNode.id())) {
                                selectedNodeIds.clear();
                                selectedNodeIds.add(hitNode.id());
                            }
                            draggedNode = hitNode;
                        }
                        return true;
                    }
                } else {
                    if (!(InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ||
                            InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT))) {
                        selectedNodeIds.clear();
                    }
                    isSelectingBox = true;
                    selectionStartX = mouseX;
                    selectionStartY = mouseY;
                    selectionCurrentX = mouseX;
                    selectionCurrentY = mouseY;
                    return true;
                }
            } else if (button == 1) { 
                if (isConnecting) {
                    isConnecting = false;
                    connectionSource = null;
                    return true;
                }
                if (hitNode != null) {
                    MinecraftClient.getInstance().setScreen(new WorldNodeEditScreen(parent, hitNode, this::refreshCachesProxy, node -> startConnecting(node)));
                } else {
                    double scale = BASE_GRID_SCALE * zoom;
                    double gx = (mouseX - panX) / scale;
                    double gy = (mouseY - panY) / scale;
                    MinecraftClient.getInstance().setScreen(new WorldNodeEditScreen(parent, gx, gy, this::refreshCachesProxy, node -> startConnecting(node)));
                }
                return true;
            }
        }

        if (button == 0) {
            WorldTreeDataComponent data = ModComponents.WORLD_TREE_DATA.get(player);
            Set<String> unlocked = data.getUnlockedNodes();

            double currentGridScale = BASE_GRID_SCALE * zoom;
            double currentNodeSize = BASE_NODE_SIZE * zoom;

            Map<String, PassiveNode> nodes = WorldNodeRegistry.getNodes();
            for (PassiveNode node : nodes.values()) {
                double actualNodeSize = TreeSpriteBatch.nodeSize(node.shape(), currentNodeSize);
                double halfSize = actualNodeSize / 2.0;

                double nx = panX + node.gridX() * currentGridScale;
                double ny = panY + node.gridY() * currentGridScale;

                if (mouseX >= nx - halfSize && mouseX <= nx + halfSize && mouseY >= ny - halfSize
                        && mouseY <= ny + halfSize) {
                    if (draftUnlocked.containsKey(node.id())) {
                        if (node.isAttributeSelection()
                                && !InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)
                                && !InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                            activeChoiceNodeId = node.id();
                            choicePopupX = (int) mouseX;
                            choicePopupY = (int) mouseY - 25;
                            player.playSound(ModSounds.PASSIVE_NODE_SELECT, 0.6f, 1.2f);
                            return true;
                        }
                        draftUnlocked.remove(node.id());
                        draftPoints++;
                        player.playSound(ModSounds.PASSIVE_NODE_SELECT, 0.5f, 0.8f); 
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
                            player.playSound(com.cranks.dungeons.registry.ModSounds.PASSIVE_NODE_SELECT, 0.6f, 1.2f); 
                            this.cacheDirty = true;
                        }
                    }
                    return true;
                }
            }
        } else if (button == 1) { 
            if (editMode) {
                if (draggedNode != null) {
                    if (snapToGrid) {
                        double snap = 0.5;
                        if (selectedNodeIds.contains(draggedNode.id())) {
                            for (String id : selectedNodeIds) {
                                PassiveNode n = WorldNodeRegistry.getNode(id);
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
                    draggedNode = null;
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4f, 0.5f);
                    }
                } else {
                    double currentGridScale = BASE_GRID_SCALE * zoom;
                    double currentNodeSize = BASE_NODE_SIZE * zoom;
                    Map<String, PassiveNode> nodes = WorldNodeRegistry.getNodes();
                    for (PassiveNode node : nodes.values()) {
                        double nx = panX + node.gridX() * currentGridScale;
                        double ny = panY + node.gridY() * currentGridScale;
                        double sz = TreeSpriteBatch.nodeSize(node.shape(), currentNodeSize) / 2.0;

                        if (mouseX >= nx - sz && mouseX <= nx + sz && mouseY >= ny - sz && mouseY <= ny + sz) {
                            draggedNode = node;
                            if (MinecraftClient.getInstance().player != null) {
                                MinecraftClient.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4f, 1.5f);
                            }
                            return true;
                        }
                    }
                }
                return true;
            }
        }
        return false;
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
                Map<String, PassiveNode> nodesMap = WorldNodeRegistry.getNodes();
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
                    double snap = 0.5;
                    if (selectedNodeIds.contains(draggedNode.id())) {
                        for (String id : selectedNodeIds) {
                            PassiveNode node = WorldNodeRegistry.getNode(id);
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

    private void revalidateDraft() {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return;

        WorldTreeDataComponent data = ModComponents.WORLD_TREE_DATA.get(player);
        Set<String> permanent = data.getUnlockedNodes();

        Set<String> stillReachable = new HashSet<>(permanent);
        java.util.Queue<String> queue = new java.util.LinkedList<>(permanent);

        Map<String, PassiveNode> allNodes = WorldNodeRegistry.getNodes();

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            PassiveNode currentNode = allNodes.get(currentId);
            if (currentNode == null) continue;

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
