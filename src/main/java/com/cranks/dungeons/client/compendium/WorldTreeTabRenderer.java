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
    private CompendiumScreen parent;
    private int contentX, contentY, contentW, contentH;

    private double panX = 0;
    private double panY = 0;
    private double zoom = 0.75;
    private static final int BASE_NODE_SIZE = 10;
    private static final int BASE_GRID_SCALE = 60; // Pixels per grid unit



    private final Map<String, String> draftUnlocked = new HashMap<>();
    private int draftPoints;
    private boolean showStatsPanel = false;

    private String activeChoiceNodeId = null;
    private int choicePopupX = 0;
    private int choicePopupY = 0;

    // --- Unified Logic & Rendering Caches ---
    private record Edge(PassiveNode n1, PassiveNode n2) {}

    private final Set<Edge> edgeCache = new HashSet<>();
    private final Set<String> canUnlockCache = new HashSet<>();
    private final Map<String, Set<String>> adjacencyMap = new HashMap<>();
    private final Map<String, Integer> distanceMap = new HashMap<>();
    private final Set<String> nodesOnHoverPath = new HashSet<>();
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

        var player = MinecraftClient.getInstance().player;
        if (player == null) return;
        WorldTreeDataComponent data = ModComponents.WORLD_TREE_DATA.get(player);
        this.draftPoints = data.getUnspentPoints();

        if (WorldNodeRegistry.getNodes().isEmpty()) {
            PassiveNode startNode = new PassiveNode("world_start", "World Start", "The beginning of your world journey.", 0.0, 0.0, new java.util.ArrayList<>(), java.util.Map.of(), null, "square", false, 0.0);
            WorldNodeRegistry.getNodes().put("world_start", startNode);
            com.cranks.dungeons.config.WorldTreeConfig.saveNodesToFile(WorldNodeRegistry.getNodes().values());
        }

        if (data.getUnlockedNodes().isEmpty()) { data.unlockNode("world_start"); }

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
                        .dimensions(contentX + contentW - 135, contentY + 5, 100, 20).build());

        // Close Button removed (handled by CompendiumScreen)

        if (panX == 0 && panY == 0) {
            panX = contentX + contentW / 2.0;
            panY = contentY + contentH / 2.0;
        }

        // --- designer MODE BUTTONS ---
        parent.addTabWidget(CompendiumButtonWidget.builder(Text.literal(editMode ? "Designer: ON" : "Designer: OFF"), b -> {
            editMode = !editMode;
            if (!editMode) draggedNode = null;
            parent.refreshTab();
        }).dimensions(contentX + 10, contentY + contentH - 30, 90, 20).build());

        if (editMode) {
            parent.addTabWidget(CompendiumButtonWidget.builder(Text.literal("Save to Config"), b -> {
                com.cranks.dungeons.config.WorldTreeConfig.saveNodesToFile(WorldNodeRegistry.getNodes().values());
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("Changes saved to world_tree.json!").formatted(Formatting.GREEN), false);
                }
            }).dimensions(contentX + 105, contentY + contentH - 30, 100, 20).build());

            parent.addTabWidget(CompendiumButtonWidget.builder(
                            Text.literal(snapToGrid ? "Grid Snap: ON" : "Grid Snap: OFF"),
                            b -> toggleSnapToGrid())
                    .dimensions(contentX + 210, contentY + contentH - 30, 100, 20).build());
        }
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

                net.minecraft.client.network.ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                        new com.cranks.dungeons.network.BulkUnlockWorldNodesPayload(new java.util.HashMap<>(draftUnlocked)));

                for (String id : draftUnlocked.keySet()) {
                    data.unlockNode(id);
                    String choice = draftUnlocked.get(id);
                    if (choice != null && !choice.isEmpty()) {
                        data.setNodeChoice(id, choice);
                    }
                    data.addUnspentPoints(-1);
                }

                draftUnlocked.clear();
                draftPoints = data.getUnspentPoints();
                parent.refreshTab();
                this.cacheDirty = true;
            }
        }
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
        // Background fill removed to allow CompendiumScreen's dimmed world to show through

        var player = MinecraftClient.getInstance().player;
        if (player == null) return;

        WorldTreeDataComponent data = ModComponents.WORLD_TREE_DATA.get(player);
        this.draftPoints = data.getUnspentPoints() - draftUnlocked.size();
        Set<String> unlocked = data.getUnlockedNodes();

        var textRenderer = MinecraftClient.getInstance().textRenderer;

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

                double currentGridScale = BASE_GRID_SCALE * zoom;
                double sx = panX + connectionSource.gridX() * currentGridScale;
                double sy = panY + connectionSource.gridY() * currentGridScale;
                drawOptimizedLineGhost(context, (float)sx, (float)sy, (float)mouseX, (float)mouseY, 0x800674BB, 2.0f, 0.5f);
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

            if (nx < contentX - margin || nx > screenW + margin || ny < contentY - margin || ny > screenH + margin) {
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

        context.draw();
        context.enableScissor(contentX, contentY, contentX + contentW, contentY + contentH);

        // --- BATCH RENDER CONNECTIONS & NODES IN 1 GPU CALL ---
        ShapeRenderer.beginBatch(context);

        for (Edge edge : edgeCache) {
            float x1 = (float) (panX + edge.n1.gridX() * currentGridScale);
            float y1 = (float) (panY + edge.n1.gridY() * currentGridScale);
            float x2 = (float) (panX + edge.n2.gridX() * currentGridScale);
            float y2 = (float) (panY + edge.n2.gridY() * currentGridScale);

            if ((x1 < contentX - margin && x2 < contentX - margin) || (x1 > screenW + margin && x2 > screenW + margin) ||
                    (y1 < contentY - margin && y2 < contentY - margin) || (y1 > screenH + margin && y2 > screenH + margin)) {
                continue;
            }

            int innerColor = 0xFF18181C; // Deepslate inert core
            int outerColor = 0xFF424248; // Deepslate chiseled border
            float baseWidth = 0.75f;
            boolean isNeon = false;

            boolean n1Unlocked = unlocked.contains(edge.n1.id());
            boolean n2Unlocked = unlocked.contains(edge.n2.id());
            boolean n1Drafted = draftUnlocked.containsKey(edge.n1.id());
            boolean n2Drafted = draftUnlocked.containsKey(edge.n2.id());

            boolean n1Active = n1Unlocked || n1Drafted;
            boolean n2Active = n2Unlocked || n2Drafted;

            if (n1Unlocked && n2Unlocked) {
                innerColor = 0xFFFFFDE7; // Radiant white-gold
                outerColor = 0xFFFFAA00; // Vanilla gold aura
                baseWidth = 0.85f;
                isNeon = true;
            } else if (n1Active && n2Active) {
                innerColor = 0xFFFFB74D; // Forge ember
                outerColor = 0xFF8C3B12; // Raw copper
                baseWidth = 0.75f;
                isNeon = true;
            } else if ((n1Active && canUnlockCache.contains(edge.n2.id()))
                    || (n2Active && canUnlockCache.contains(edge.n1.id()))) {
                innerColor = 0xFF4CAF50; // Muted Emerald core
                outerColor = 0xFF1B381E; // Deep Moss glow
                baseWidth = 0.75f;
                isNeon = true;
            }

            boolean n1OnPath = nodesOnHoverPath.contains(edge.n1.id());
            boolean n2OnPath = nodesOnHoverPath.contains(edge.n2.id());

            if (n1OnPath && n2OnPath) {
                int d1 = distanceMap.getOrDefault(edge.n1.id(), -1);
                int d2 = distanceMap.getOrDefault(edge.n2.id(), -1);
                if (Math.abs(d1 - d2) == 1) {
                    innerColor = 0xFF7DD3FC; // Beacon sparkle
                    outerColor = 0xFF0369A1; // Lapis glow
                    baseWidth = 0.75f;
                    isNeon = true;
                }
            }

            float finalWidth = (float) (baseWidth * Math.sqrt(zoom));

            if (isNeon) {
                ShapeRenderer.drawBatchedLine(x1, y1, x2, y2, finalWidth * 2.5f,
                        (outerColor & 0x00FFFFFF) | 0xDD000000);
                ShapeRenderer.drawBatchedLine(x1, y1, x2, y2, finalWidth,
                        innerColor);
            } else {
                ShapeRenderer.drawBatchedLine(x1, y1, x2, y2, finalWidth + 1.2f,
                        outerColor);
                ShapeRenderer.drawBatchedLine(x1, y1, x2, y2, finalWidth,
                        innerColor);
            }
        }

        // --- DRAW NODE SHAPES (BATCHED) ---
        for (PassiveNode node : allNodesMap.values()) {
            double nx = panX + node.gridX() * currentGridScale;
            double ny = panY + node.gridY() * currentGridScale;

            if (nx < contentX - margin || nx > screenW + margin || ny < contentY - margin || ny > screenH + margin) {
                continue;
            }

            boolean isUnlocked = unlocked.contains(node.id()) || draftUnlocked.containsKey(node.id());
            boolean isDraft = draftUnlocked.containsKey(node.id());
            boolean canUnlock = canUnlockCache.contains(node.id());

            double multiplier = getNodeSizeMultiplier(node.shape());
            double actualNodeSize = currentNodeSize * multiplier;

            boolean isHovered = (hovered != null && node.id().equals(hovered.id()));

            if (node.shape() != null && !node.shape().isEmpty()) {
                int nodeInner = 0xFF18181C;
                int nodeOuter = 0xFF424248;
                boolean nodeNeon = false;

                if (isHovered) {
                    nodeInner = 0xFF7DD3FC;
                    nodeOuter = 0xFF0369A1;
                    nodeNeon = true;
                } else if (isDraft) {
                    nodeInner = 0xFFFFB74D;
                    nodeOuter = 0xFF8C3B12;
                    nodeNeon = true;
                } else if (unlocked.contains(node.id())) {
                    nodeInner = 0xFFFFFDE7;
                    nodeOuter = 0xFFFFAA00;
                    nodeNeon = true;
                } else if (canUnlock) {
                    nodeInner = 0xFF4CAF50; // Muted Emerald
                    nodeOuter = 0xFF1B381E; // Deep Moss
                    nodeNeon = true;
                }

                if (nodeNeon) {
                    // Subtle soft alpha-gradient radial bloom/glow
                    double glowSpread = isHovered ? 2.8 : 1.6;
                    int glowAlpha = isHovered ? 0x99000000 : 0x77000000;
                    ShapeRenderer.drawBatchedShapeGlow(nx, ny, actualNodeSize, glowSpread, node.shape(), (nodeOuter & 0x00FFFFFF) | glowAlpha);

                    // Outer 1px frame
                    ShapeRenderer.drawBatchedShape(nx, ny, actualNodeSize, node.shape(), 0xFF101014);
                    // Proportional state outer rim
                    ShapeRenderer.drawBatchedShape(nx, ny, actualNodeSize * 0.90, node.shape(), nodeOuter);
                    // Proportional inner luminous core
                    ShapeRenderer.drawBatchedShape(nx, ny, actualNodeSize * 0.76, node.shape(), nodeInner);
                    // Dark center socket for icon
                    ShapeRenderer.drawBatchedShape(nx, ny, actualNodeSize * 0.58, node.shape(), 0xFF141418);
                } else {
                    // Locked: 1:1 match with 3D tactile stone button bevels
                    ShapeRenderer.drawBatchedShape(nx, ny, actualNodeSize, node.shape(), 0xFF101014);
                    ShapeRenderer.drawBatchedShape(nx, ny, actualNodeSize * 0.90, node.shape(), 0xFF585864);
                    ShapeRenderer.drawBatchedShape(nx + 0.3, ny + 0.3, actualNodeSize * 0.82, node.shape(), 0xFF18181E);
                    ShapeRenderer.drawBatchedShape(nx, ny, actualNodeSize * 0.76, node.shape(), 0xFF24242C);
                    ShapeRenderer.drawBatchedShape(nx, ny, actualNodeSize * 0.58, node.shape(), 0xFF141418);
                }

                // Draw selection highlight with subtle soft bloom
                if (selectedNodeIds.contains(node.id())) {
                    ShapeRenderer.drawBatchedShapeGlow(nx, ny, actualNodeSize, 3.0, node.shape(), 0xAAFFAA00);
                }
            }
        }

        // Flush all lines and shapes to GPU in ONE single draw call
        ShapeRenderer.endBatch();

        // --- DRAW NODE ICONS (ALL ZOOM LEVELS) ---
        for (PassiveNode node : allNodesMap.values()) {
            double nx = panX + node.gridX() * currentGridScale;
            double ny = panY + node.gridY() * currentGridScale;

            if (nx < contentX - margin || nx > screenW + margin || ny < contentY - margin || ny > screenH + margin) {
                continue;
            }

            double multiplier = getNodeSizeMultiplier(node.shape());
            double actualNodeSize = currentNodeSize * multiplier;

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

            if (iconToDraw != null) {
                String iconPath = iconToDraw.getPath();
                if (!iconPath.equals("passive/circle") && !iconPath.equals("passive/square") &&
                        !iconPath.equals("passive/diamond") && !iconPath.equals("passive/hexagon") &&
                        !iconPath.equals("passive/generic")) {

                    double iconSize = actualNodeSize * 0.56;
                    drawPrecisionTexture(context, iconToDraw, nx, ny, iconSize, 0xFFFFFFFF);
                }
            }
        }

        context.draw();
        context.disableScissor();

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

            PassiveNode activeNode = WorldNodeRegistry.getNodes().get(activeChoiceNodeId);
            if (activeNode != null && activeNode.modifiers() != null) {
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

                    CompendiumButtonWidget.drawButton(context, cx, btnY, 150, 20, hoverBtn, true, "+" + amountStr + " " + displayName);
                    i++;
                }
            }
        }
    }

    private void renderStatsPanel(DrawContext context) {
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int panelWidth = 230;
        int panelHeight = (int) (contentH * 0.7);
        int panelX = contentX + contentW - panelWidth - 10;
        int panelY = contentY + (contentH - panelHeight) / 2;

        // Draw 3D Beveled Stone Panel
        ShapeRenderer.drawBeveledPanel(context, panelX, panelY, panelWidth, panelHeight);

        int ty = panelY + 10;
        context.drawCenteredTextWithShadow(textRenderer, "§6Tree Summary", panelX + panelWidth / 2, ty, 0xFFFFAA00);
        // Gold underline
        context.fill(panelX + 10, ty + 10, panelX + panelWidth - 10, ty + 11, 0xFFFFAA00);
        ty += 18;

        com.cranks.dungeons.component.WorldTreeDataComponent wtData = ModComponents.WORLD_TREE_DATA.get(MinecraftClient.getInstance().player);
        int level = wtData.getGatheringLevel();
        int currentXp = wtData.getGatheringXp();
        int nextXp = wtData.getNextLevelXp();

        context.drawCenteredTextWithShadow(textRenderer, "§6World Lvl: §a" + level, panelX + panelWidth / 2, ty, 0xFFFFFFFF);
        ty += 16;

        Identifier expBg = Identifier.of("minecraft", "hud/experience_bar_background");
        Identifier expFg = Identifier.of("minecraft", "hud/experience_bar_progress");

        int barWidth = 182;
        int barX = panelX + (panelWidth - barWidth) / 2;

        context.drawGuiTexture(expBg, barX, ty + 2, 182, 5);

        int progressWidth = nextXp > 0 ? (int) (182.0 * currentXp / nextXp) : 0;
        if (progressWidth > 0) {
            context.drawGuiTexture(expFg, 182, 5, 0, 0, barX, ty + 2, progressWidth, 5);
        }

        String xpText = nextXp == 0 ? "MAX" : (currentXp + " / " + nextXp);
        context.drawCenteredTextWithShadow(textRenderer, xpText, panelX + panelWidth / 2, ty, 0xFFFFFFFF);
        ty += 15;
        
        var stats = PassiveStatHelper.getDisplayableSummary(MinecraftClient.getInstance().player, draftUnlocked);
        var baseStats = PassiveStatHelper.getDisplayableSummary(MinecraftClient.getInstance().player, null);

        if (stats.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, "No stats unlocked", panelX + panelWidth / 2, ty + 20, 0x80AAAAAA);
        } else {
            for (Map.Entry<String, Double> entry : stats.entrySet()) {
                String key = entry.getKey();
                double total = entry.getValue();
                if (total == 0) continue;

                double baseTotal = baseStats.getOrDefault(key, 0.0);
                boolean isDrafted = (total != baseTotal);
                int valColor = isDrafted ? 0xFF66FF66 : 0xFFFFFFFF;
                int keyColor = isDrafted ? 0xFF88FF88 : 0xFFAAAAFF;

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

                if (ty > panelY + panelHeight - 15) break; 
            }
        }
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
            for (String connId : node.connections()) {
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

    private void traceHoverPath(String nodeId, int dist) {
        nodesOnHoverPath.add(nodeId);
        if (dist <= 0) return;

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

    private double getNodeSizeMultiplier(String shape) {
        if (shape == null) return 1.0;
        return switch (shape.toLowerCase()) {
            case "hexagon" -> 3.2; 
            case "diamond" -> 2.2; 
            case "circle" -> 1.4; 
            default -> 1.0; 
        };
    }

    private void drawOptimizedLineGhost(DrawContext context, float x1, float y1, float x2, float y2, int color, float width, float alpha) {
        ShapeRenderer.drawLine(context, x1, y1, x2, y2, width, (color & 0x00FFFFFF) | ((int) (alpha * 255) << 24));
    }

    private void drawPrecisionTexture(DrawContext context, Identifier texture, double cx, double cy, double size, int color) {
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate((float) cx, (float) cy, 0.0f);

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
        double zoomFactor = verticalAmount > 0 ? 1.1 : 0.9;
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

        if (keyCode == 258) { 
            editMode = !editMode;
            if (!editMode) {
                draggedNode = null;
                isConnecting = false;
                connectionSource = null;
                selectedNodeIds.clear();
            }
            parent.init();
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
        var player = MinecraftClient.getInstance().player;
        if (player == null) return false;

        if (editMode) {
            double currentGridScale = BASE_GRID_SCALE * zoom;
            double currentNodeSize = BASE_NODE_SIZE * zoom;
            Map<String, PassiveNode> nodesMap = WorldNodeRegistry.getNodes();
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

        if (activeChoiceNodeId != null) {
            int cx = choicePopupX;
            int cy = choicePopupY;

            PassiveNode activeNode = WorldNodeRegistry.getNodes().get(activeChoiceNodeId);
            if (activeNode != null && activeNode.modifiers() != null) {
                if (mouseX >= cx && mouseX <= cx + 150) {
                    int i = 0;
                    for (Map.Entry<RegistryEntry<EntityAttribute>, Double> entry : activeNode.modifiers().entrySet()) {
                        int btnY = cy + (i * 22);
                        if (mouseY >= btnY && mouseY <= btnY + 20) {
                            String modId = entry.getKey().getKey().map(k -> k.getValue().getPath()).orElse("unknown");
                            draftUnlocked.put(activeChoiceNodeId, modId);
                            draftPoints--;
                            MinecraftClient.getInstance().player.playSound(com.cranks.dungeons.registry.ModSounds.PASSIVE_NODE_SELECT, 0.6f, 1.2f);
                            activeChoiceNodeId = null;
                            this.cacheDirty = true;
                            return true;
                        }
                        i++;
                    }
                }
            }

            activeChoiceNodeId = null;
            return true;
        }

        if (button == 0) {
            WorldTreeDataComponent data = ModComponents.WORLD_TREE_DATA.get(player);
            Set<String> unlocked = data.getUnlockedNodes();

            double currentGridScale = BASE_GRID_SCALE * zoom;
            double currentNodeSize = BASE_NODE_SIZE * zoom;

            Map<String, PassiveNode> nodes = WorldNodeRegistry.getNodes();
            for (PassiveNode node : nodes.values()) {
                double multiplier = getNodeSizeMultiplier(node.shape());
                double actualNodeSize = currentNodeSize * multiplier;
                int halfSize = (int) (actualNodeSize / 2.0);

                double nx = panX + node.gridX() * currentGridScale;
                double ny = panY + node.gridY() * currentGridScale;

                if (mouseX >= nx - halfSize && mouseX <= nx + halfSize && mouseY >= ny - halfSize
                        && mouseY <= ny + halfSize) {
                    if (draftUnlocked.containsKey(node.id())) {
                        draftUnlocked.remove(node.id());
                        draftPoints++;
                        player.playSound(ModSounds.PASSIVE_NODE_SELECT, 0.5f, 0.8f); 
                        this.cacheDirty = true;
                        revalidateDraft();
                        return true;
                    }

                    boolean isPermanent = unlocked.contains(node.id());
                    if (isPermanent) return true;

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
                        double sz = currentNodeSize * getNodeSizeMultiplier(node.shape()) / 2.0;

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
