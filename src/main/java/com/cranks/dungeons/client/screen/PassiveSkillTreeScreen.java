package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.component.PassiveDataComponent;
import com.cranks.dungeons.network.SelectClassPayload;
import com.cranks.dungeons.network.UnlockNodePayload;
import com.cranks.dungeons.stat.passive.PassiveNode;
import com.cranks.dungeons.stat.passive.PassiveNodeRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.stat.passive.PassiveStatHelper;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

public class PassiveSkillTreeScreen extends Screen {
    private final Screen parent;
    private double panX = 0;
    private double panY = 0;
    private double zoom = 1.0;
    private static final int BASE_NODE_SIZE = 28;
    private static final int BASE_GRID_SCALE = 60; // Pixels per grid unit

    private static final Identifier LINE_NORMAL = Identifier.of(com.cranks.dungeons.CranksDungeons.MOD_ID,
            "passive/line_normal");
    private static final Identifier LINE_AVAILABLE = Identifier.of("cranks-dungeons", "passive/line_available");
    private static final Identifier LINE_SELECTED = Identifier.of("cranks-dungeons", "passive/line_selected");
    private static final Identifier LINE_UNLOCKED = Identifier.of("cranks-dungeons", "passive/line_unlocked");
    private static final Identifier LINE_HOVER = Identifier.of("cranks-dungeons", "passive/line_hover");

    // NODE SHAPES (Premium Textures)
    private static final Identifier NODE_SQUARE_LOCKED = Identifier.of("cranks-dungeons", "passive/node_square_locked");
    private static final Identifier NODE_SQUARE_AVAILABLE = Identifier.of("cranks-dungeons",
            "passive/node_square_available");
    private static final Identifier NODE_SQUARE_DRAFTED = Identifier.of("cranks-dungeons",
            "passive/node_square_drafted");
    private static final Identifier NODE_SQUARE_UNLOCKED = Identifier.of("cranks-dungeons",
            "passive/node_square_unlocked");

    private static final Identifier NODE_CIRCLE_LOCKED = Identifier.of("cranks-dungeons", "passive/node_circle_locked");
    private static final Identifier NODE_CIRCLE_AVAILABLE = Identifier.of("cranks-dungeons",
            "passive/node_circle_available");
    private static final Identifier NODE_CIRCLE_DRAFTED = Identifier.of("cranks-dungeons",
            "passive/node_circle_drafted");
    private static final Identifier NODE_CIRCLE_UNLOCKED = Identifier.of("cranks-dungeons",
            "passive/node_circle_unlocked");

    private static final Identifier NODE_DIAMOND_LOCKED = Identifier.of("cranks-dungeons",
            "passive/node_diamond_locked");
    private static final Identifier NODE_DIAMOND_AVAILABLE = Identifier.of("cranks-dungeons",
            "passive/node_diamond_available");
    private static final Identifier NODE_DIAMOND_DRAFTED = Identifier.of("cranks-dungeons",
            "passive/node_diamond_drafted");
    private static final Identifier NODE_DIAMOND_UNLOCKED = Identifier.of("cranks-dungeons",
            "passive/node_diamond_unlocked");

    private static final Identifier NODE_HEXAGON_LOCKED = Identifier.of("cranks-dungeons",
            "passive/node_hexagon_locked");
    private static final Identifier NODE_HEXAGON_AVAILABLE = Identifier.of("cranks-dungeons",
            "passive/node_hexagon_available");
    private static final Identifier NODE_HEXAGON_DRAFTED = Identifier.of("cranks-dungeons",
            "passive/node_hexagon_drafted");
    private static final Identifier NODE_HEXAGON_UNLOCKED = Identifier.of("cranks-dungeons",
            "passive/node_hexagon_unlocked");

    // SELECTION HIGHLIGHTS (Pristine White Glows)
    private static final Identifier NODE_SQUARE_SELECTION = Identifier.of("cranks-dungeons",
            "passive/node_square_selection");
    private static final Identifier NODE_CIRCLE_SELECTION = Identifier.of("cranks-dungeons",
            "passive/node_circle_selection");
    private static final Identifier NODE_DIAMOND_SELECTION = Identifier.of("cranks-dungeons",
            "passive/node_diamond_selection");
    private static final Identifier NODE_HEXAGON_SELECTION = Identifier.of("cranks-dungeons",
            "passive/node_hexagon_selection");

    // UI BACKGROUNDS
    private static final Identifier MENU_BACKGROUND = Identifier.of("cranks-dungeons", "passive/menu_background");
    private static final Identifier STATS_BACKGROUND = Identifier.of("cranks-dungeons", "passive/stats_background");

    private boolean choosingClass = false;
    private final Set<String> draftUnlocked = new HashSet<>();
    private int draftPoints;
    private boolean showStatsPanel = false;

    // --- Unified Logic & Rendering Caches ---
    private record Edge(PassiveNode n1, PassiveNode n2) {
    }

    private final Set<Edge> edgeCache = new HashSet<>(); // Changed to Set for auto-deduplication
    private final Set<String> canUnlockCache = new HashSet<>();
    private final Map<String, Set<String>> adjacencyMap = new HashMap<>(); // Unified source of truth
    private final Map<String, Integer> distanceMap = new HashMap<>();
    private final Set<String> nodesOnHoverPath = new HashSet<>();
    private boolean cacheDirty = true;

    // --- Tabbed Selection UI ---
    private int selectedClassTab = 0;
    private boolean showingConfirmPopup = false;
    private String classPendingConfirmation = null;
    private ButtonWidget confirmBtn;
    private ButtonWidget cancelBtn;
    
    // designer MODE fields
    private boolean editMode = false;
    private PassiveNode draggedNode = null;
    private PassiveNode connectionSource = null;
    private boolean isConnecting = false;

    public PassiveSkillTreeScreen(Screen parent) {
        super(Text.literal("Passive Skill Tree"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        super.init();

        var player = MinecraftClient.getInstance().player;
        if (player == null)
            return;
        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
        this.draftPoints = data.getUnspentPoints();

        if (this.client != null && this.client.player != null) {
            // Player class was set or resumed - no sound on init to keep button clicks
            // clean
        }

        if (data.getPlayerClass() == null || data.getPlayerClass().isEmpty()) {
            choosingClass = true;
            
            int panelW = 340;
            int panelH = 220;
            int px = (this.width - panelW) / 2;
            int py = (this.height - panelH) / 2;
            int sidebarW = 100;

            // Close Button for Class Selection Panel
            this.addDrawableChild(ButtonWidget.builder(Text.literal("✕"), b -> this.close())
                    .dimensions(px + panelW - 25, py + 5, 20, 20).build()).active = !showingConfirmPopup;

            // Sidebar Tabs (Always visible, but disabled during popup)
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Overview"), b -> {
                selectedClassTab = 0;
                this.init();
            }).dimensions(px + 5, py + 10, sidebarW - 10, 20).build()).active = !showingConfirmPopup;

            // Class Tabs
            var classes = com.cranks.dungeons.config.PassiveTreeConfig.getClasses();
            for (int i = 0; i < classes.size(); i++) {
                int index = i + 1;
                var def = classes.get(i);
                this.addDrawableChild(ButtonWidget.builder(Text.literal(def.name()), b -> {
                    selectedClassTab = index;
                    this.init();
                }).dimensions(px + 5, py + 35 + i * 25, sidebarW - 10, 20).build()).active = !showingConfirmPopup;
            }

            // Selection Button (only on class tabs)
            if (selectedClassTab > 0 && selectedClassTab <= classes.size()) {
                var currentDef = classes.get(selectedClassTab - 1);
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Select This Path"), b -> {
                    showingConfirmPopup = true;
                    classPendingConfirmation = currentDef.id();
                    this.init();
                }).dimensions(px + sidebarW + 20, py + panelH - 40, panelW - sidebarW - 40, 20).build()).active = !showingConfirmPopup;
            }

            if (showingConfirmPopup) {
                // Confirmation Popup Buttons
                int btnW = 120;
                int centerX = (this.width - btnW) / 2;
                int ppy = (this.height - 120) / 2;

                this.confirmBtn = this.addDrawableChild(ButtonWidget.builder(Text.literal("Confirm Path"), b -> {
                    if (classPendingConfirmation != null) {
                        selectClass(classPendingConfirmation);
                    }
                }).dimensions(centerX, ppy + 65, btnW, 20).build());

                this.cancelBtn = this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> {
                    showingConfirmPopup = false;
                    this.init();
                }).dimensions(centerX, ppy + 90, btnW, 20).build());
            }
        } else {
            choosingClass = false;

            // Apply Button
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Apply Changes"), b -> applyChanges())
                    .dimensions(this.width / 2 - 105, this.height - 30, 100, 20).build());

            // Reset Button
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset Draft"), b -> resetDraft())
                    .dimensions(this.width / 2 + 5, this.height - 30, 100, 20).build());

            // Stats Toggle Button
            this.addDrawableChild(
                    ButtonWidget.builder(Text.literal(showStatsPanel ? "Hide Stats" : "Show Stats"), b -> {
                        showStatsPanel = !showStatsPanel;
                        this.init(); // Refresh button text
                    })
                            .dimensions(this.width - 110, 10, 100, 20).build());

            // Close Button for Passive Tree
            this.addDrawableChild(ButtonWidget.builder(Text.literal("✕"), b -> this.close())
                    .dimensions(this.width - 135, 10, 20, 20).build());

            // Center the view on the starting node for their class (only if not already
            // panned)
            if (panX == 0 && panY == 0) {
                String startId = data.getPlayerClass() + "_start";
                PassiveNode start = PassiveNodeRegistry.getNode(startId);
                if (start != null) {
                    panX = (this.width / 2.0) - (start.gridX() * BASE_GRID_SCALE * zoom);
                    panY = (this.height / 2.0) - (start.gridY() * BASE_GRID_SCALE * zoom);
                } else {
                    panY = this.height / 2.0;
                }
            }

            // --- designer MODE BUTTONS ---
            this.addDrawableChild(ButtonWidget.builder(Text.literal(editMode ? "Designer: ON" : "Designer: OFF"), b -> {
                editMode = !editMode;
                if (!editMode) draggedNode = null;
                this.init();
            }).dimensions(10, this.height - 30, 90, 20).build());

            if (editMode) {
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Save to Config"), b -> {
                    com.cranks.dungeons.config.PassiveTreeConfig.saveNodesToFile(PassiveNodeRegistry.getNodes().values());
                    if (this.client != null && this.client.player != null) {
                        this.client.player.sendMessage(Text.literal("Changes saved to passive_tree.json!").formatted(Formatting.GREEN), false);
                    }
                }).dimensions(105, this.height - 30, 100, 20).build());
            }
        }
    }

    public void refreshCachesProxy() {
        this.cacheDirty = true;
    }

    private void applyChanges() {
        if (!draftUnlocked.isEmpty()) {
            var player = MinecraftClient.getInstance().player;
            if (player != null) {
                player.playSound(ModSounds.PASSIVE_POINT_APPLY, 0.8f, 1.0f);
            }

            ClientPlayNetworking.send(
                    new com.cranks.dungeons.network.BulkUnlockNodesPayload(new java.util.ArrayList<>(draftUnlocked)));

            // Update client-side points and unlocked nodes BEFORE clearing the draft
            var data = ModComponents.PASSIVE_DATA.get(player);
            data.setUnspentPoints(draftPoints);
            for (String id : draftUnlocked) {
                data.unlockNode(id);
            }

            draftUnlocked.clear();
            this.init();
            this.cacheDirty = true;
        }
    }

    private void resetDraft() {
        draftUnlocked.clear();
        this.cacheDirty = true;
        this.init();
    }

    private void selectClass(String className) {
        ClientPlayNetworking.send(new SelectClassPayload(className));

        // Optimistically apply client-side so UI updates immediately
        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(MinecraftClient.getInstance().player);
        data.setPlayerClass(className);

        var def = com.cranks.dungeons.config.PassiveTreeConfig.getClasses().stream()
                .filter(c -> c.id().equals(className)).findFirst().orElse(null);
        if (def != null) {
            data.unlockNode(def.startNode());
        }

        var playerEntity = MinecraftClient.getInstance().player;
        if (playerEntity != null) {
            playerEntity.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.0f);
        }

        this.clearChildren();
        this.cacheDirty = true;
        this.init(); // Reinitialize to hide buttons and show tree
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Use fill instead of renderBackground to avoid "Can only blur once per frame"
        // crash in large modpacks
        context.fill(0, 0, this.width, this.height, 0x90000000);

        if (choosingClass) {
            int panelW = 340;
            int panelH = 220;
            int px = (this.width - panelW) / 2;
            int py = (this.height - panelH) / 2;
            int sidebarW = 100;

            // Draw Panel Background
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, MENU_BACKGROUND, px, py, panelW, panelH);

            // Content Area Rendering
            var classes = com.cranks.dungeons.config.PassiveTreeConfig.getClasses();
            if (selectedClassTab == 0) {
                int centerX = px + sidebarW + (panelW - sidebarW) / 2;
                Text title = Text.literal("Path Selection").formatted(Formatting.DARK_BLUE, Formatting.BOLD);
                context.drawText(this.textRenderer, title, centerX - this.textRenderer.getWidth(title) / 2, py + 15, 0xFF000000, false);

                String intro = "Choose a starting class to begin your journey. Each class provides a unique starting point on the passive tree and specialized early attributes.\n\nSelecting a class is permanent!";
                drawWrappedText(context, Text.literal(intro), px + sidebarW + 15, py + 45, panelW - sidebarW - 30, 0xFF333333, false);
            } else if (selectedClassTab > 0 && selectedClassTab <= classes.size()) {
                var def = classes.get(selectedClassTab - 1);
                int centerX = px + sidebarW + (panelW - sidebarW) / 2;
                int classColor = parseHexColor(def.color());
                
                // Title
                Text title = Text.literal(def.name()).formatted(Formatting.BOLD);
                context.drawText(this.textRenderer, title, centerX - this.textRenderer.getWidth(title) / 2, py + 15, classColor, false);
                
                // Subtitle (Wrapped & Centered)
                drawWrappedText(context, Text.literal(def.subtitle()).formatted(Formatting.DARK_GRAY, Formatting.ITALIC), 
                        centerX, py + 28, panelW - sidebarW - 30, 0xFF000000, true);

                // Detail Description (Left Aligned)
                drawWrappedText(context, Text.literal(def.description()), px + sidebarW + 15, py + 52, panelW - sidebarW - 30,
                        0xFF000000, false);

                // Recommended Stats Section
                int recY = py + 115;
                context.drawText(this.textRenderer, Text.literal("Recommended Stats:").formatted(Formatting.BOLD), px + sidebarW + 15, recY, classColor, false);
                drawWrappedText(context, Text.literal(def.recommendedStats()), px + sidebarW + 15, recY + 12, panelW - sidebarW - 30,
                        0xFF444444, false);
            }

            super.render(context, mouseX, mouseY, delta); // Draw background buttons (tabs + select)

            if (showingConfirmPopup) {
                context.fill(0, 0, this.width, this.height, 0x90000000); // Darken background more for popup

                // Draw centered popup panel (smaller than main menu)
                int popupW = 200;
                int popupH = 120;
                int ppx = (this.width - popupW) / 2;
                int ppy = (this.height - popupH) / 2;
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, MENU_BACKGROUND, ppx, ppy, popupW, popupH);

                int centerX = this.width / 2;
                String classTitle = (classPendingConfirmation != null ? classPendingConfirmation.substring(0, 1).toUpperCase() + classPendingConfirmation.substring(1) : "Path");
                Text popupTitle = Text.literal("Path of the " + classTitle).formatted(Formatting.DARK_RED, Formatting.BOLD);
                context.drawText(this.textRenderer, popupTitle, centerX - this.textRenderer.getWidth(popupTitle) / 2, ppy + 15, 0xFF000000, false);
                
                String msg = "Are you sure? This decision is PERMANENT!";
                drawWrappedText(context, Text.literal(msg), ppx + 15, ppy + 40, popupW - 30, 0xFF000000, false);

                // Re-render ONLY the popup buttons on the top layer
                if (confirmBtn != null)
                    confirmBtn.render(context, mouseX, mouseY, delta);
                if (cancelBtn != null)
                    cancelBtn.render(context, mouseX, mouseY, delta);
            }
            return;
        }

        var player = MinecraftClient.getInstance().player;
        if (player == null)
            return;

        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
        Set<String> unlocked = data.getUnlockedNodes();

        context.drawTextWithShadow(this.textRenderer,
                "Class: " + data.getPlayerClass().substring(0, 1).toUpperCase() + data.getPlayerClass().substring(1),
                10, 10, 0xFFFFFF);

        if (editMode) {
            context.drawTextWithShadow(this.textRenderer, "§b[DESIGNER MODE ACTIVE]", 10, 22, 0x55FFFF);
            if (draggedNode != null) {
                String coordText = String.format("Dragging: %s (X: %.2f, Y: %.2f)", draggedNode.name(), draggedNode.gridX(), draggedNode.gridY());
                context.drawTextWithShadow(this.textRenderer, coordText, 10, 34, 0xAAFFAA);
            } else if (isConnecting && connectionSource != null) {
                context.drawTextWithShadow(this.textRenderer, "Connecting: " + connectionSource.id() + " -> Click another node", 10, 34, 0xFFFFAA);
                
                // Draw Ghost Line
                double currentGridScale = BASE_GRID_SCALE * zoom;
                double sx = panX + connectionSource.gridX() * currentGridScale;
                double sy = panY + connectionSource.gridY() * currentGridScale;
                drawOptimizedLine(context, (float)sx, (float)sy, (float)mouseX, (float)mouseY, LINE_NORMAL, 0x80FFFFFF, 2.0f, 0.5f);
            } else {
                context.drawTextWithShadow(this.textRenderer, "L-Click: Drag | R-Click: Create/Edit | Shift+R-Click: Delete", 10, 34, 0xAAAAAA);
            }
        }

        // --- SPATIAL CULLING SETUP ---
        double currentGridScale = BASE_GRID_SCALE * zoom;
        double currentNodeSize = BASE_NODE_SIZE * zoom;
        int margin = (int) (currentNodeSize + 10);
        int screenW = this.width;
        int screenH = this.height;

        // --- CACHE REFRESH ---
        if (cacheDirty) {
            refreshCaches(data);
            cacheDirty = false;
        }

        // --- PRE-CALCULATE HOVERED NODE ---
        PassiveNode hovered = null;
        Map<String, PassiveNode> allNodesMap = PassiveNodeRegistry.getNodes();
        for (PassiveNode node : allNodesMap.values()) {
            double nx = panX + node.gridX() * currentGridScale;
            double ny = panY + node.gridY() * currentGridScale;

            // Spatial Culling Check
            if (nx < -margin || nx > screenW + margin || ny < -margin || ny > screenH + margin) {
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

        // --- DRAW CONNECTIONS ---
        for (Edge edge : edgeCache) {
            int x1 = (int) (panX + edge.n1.gridX() * currentGridScale);
            int y1 = (int) (panY + edge.n1.gridY() * currentGridScale);
            int x2 = (int) (panX + edge.n2.gridX() * currentGridScale);
            int y2 = (int) (panY + edge.n2.gridY() * currentGridScale);

            // Spatial Culling
            if ((x1 < -margin && x2 < -margin) || (x1 > screenW + margin && x2 > screenW + margin) ||
                    (y1 < -margin && y2 < -margin) || (y1 > screenH + margin && y2 > screenH + margin)) {
                continue;
            }

            // Determine State
            Identifier lineTex = LINE_NORMAL;
            int color = 0xFFFFFF; // Use texture colors primarily
            float baseWidth = 3.0f; // Thicker base width
            float alpha = 0.6f;

            boolean n1Unlocked = unlocked.contains(edge.n1.id());
            boolean n2Unlocked = unlocked.contains(edge.n2.id());
            boolean n1Drafted = draftUnlocked.contains(edge.n1.id());
            boolean n2Drafted = draftUnlocked.contains(edge.n2.id());

            boolean n1Active = n1Unlocked || n1Drafted;
            boolean n2Active = n2Unlocked || n2Drafted;

            if (n1Unlocked && n2Unlocked) {
                lineTex = LINE_UNLOCKED;
                baseWidth = 6.0f; // Thickest for unlocked
                alpha = 1.0f;
            } else if (n1Active && n2Active) {
                lineTex = LINE_SELECTED;
                baseWidth = 6.0f;
                alpha = 0.9f;
            } else if ((n1Active && canUnlockCache.contains(edge.n2.id()))
                    || (n2Active && canUnlockCache.contains(edge.n1.id()))) {
                lineTex = LINE_AVAILABLE;
                baseWidth = 4.5f;
                alpha = 0.8f;
            }

            // Full-Path Hover logic
            boolean n1OnPath = nodesOnHoverPath.contains(edge.n1.id());
            boolean n2OnPath = nodesOnHoverPath.contains(edge.n2.id());

            if (n1OnPath && n2OnPath) {
                int d1 = distanceMap.getOrDefault(edge.n1.id(), -1);
                int d2 = distanceMap.getOrDefault(edge.n2.id(), -1);
                if (Math.abs(d1 - d2) == 1) {
                    lineTex = LINE_HOVER;
                    baseWidth = 8.0f;
                    alpha = 1.0f;
                }
            }

            // Apply zoom scaling to width so lines don't vanish at distance
            float finalWidth = (float) (baseWidth * Math.sqrt(zoom));

            drawOptimizedLine(context, (float) x1, (float) y1, (float) x2, (float) y2, lineTex, color, finalWidth,
                    alpha);
        }

        // --- DRAW NODES ---
        for (PassiveNode node : allNodesMap.values()) {
            double nx = panX + node.gridX() * currentGridScale;
            double ny = panY + node.gridY() * currentGridScale;

            // Spatial Culling (Wide margin for large keystone glows)
            if (nx < -margin || nx > screenW + margin || ny < -margin || ny > screenH + margin) {
                continue;
            }

            boolean isUnlocked = unlocked.contains(node.id()) || draftUnlocked.contains(node.id());
            boolean isDraft = draftUnlocked.contains(node.id());
            boolean canUnlock = canUnlockCache.contains(node.id());

            double multiplier = getNodeSizeMultiplier(node.shape());
            double actualNodeSize = currentNodeSize * multiplier;

            boolean isHovered = (hovered != null && node.id().equals(hovered.id()));

            if (node.shape() != null && !node.shape().isEmpty()) {
                String state = "locked";
                if (isDraft) {
                    state = "drafted";
                } else if (unlocked.contains(node.id())) {
                    state = "unlocked";
                } else if (canUnlock) {
                    state = "available";
                }

                drawNodeShape(context, nx, ny, node.shape(), state, actualNodeSize);

                // Draw Hover Radiant Glow (Behind icon)
                if (isHovered) {
                    Identifier selectionSprite = switch (node.shape().toLowerCase()) {
                        case "circle" -> NODE_CIRCLE_SELECTION;
                        case "diamond" -> NODE_DIAMOND_SELECTION;
                        case "hexagon" -> NODE_HEXAGON_SELECTION;
                        default -> NODE_SQUARE_SELECTION;
                    };

                    double haloSize = actualNodeSize * 1.15;
                    drawPrecisionTexture(context, selectionSprite, nx, ny, haloSize, 0xCCFFFFFF);
                }
            }

            // Draw Icon (TOP LAYER)
            if (node.icon() != null) {
                String iconPath = node.icon().getPath();
                if (!iconPath.equals("passive/circle") && !iconPath.equals("passive/square") &&
                        !iconPath.equals("passive/diamond") && !iconPath.equals("passive/hexagon") &&
                        !iconPath.equals("passive/generic")) {

                    double iconSize = actualNodeSize * 0.95;
                    drawPrecisionTexture(context, node.icon(), nx, ny, iconSize, 0xFFFFFFFF);
                }
            }

        }

        if (hovered != null) {
            String title = "§6" + hovered.name();
            if (draftUnlocked.contains(hovered.id()))
                title = "§b[SELECTED] §6" + hovered.name();
            else if (unlocked.contains(hovered.id()))
                title = "§e[UNLOCKED] §6" + hovered.name();
            else if (canUnlockCache.contains(hovered.id()))
                title = "§a[AVAILABLE] §6" + hovered.name();

            context.drawTooltip(this.textRenderer, Text.literal(title + "\n§7" + hovered.description()), mouseX,
                    mouseY);
        }

        String pointsText = "Unspent Points: " + draftPoints;
        context.drawCenteredTextWithShadow(this.textRenderer, pointsText, this.width / 2, this.height - 48, 0xFFFFAA00);

        if (showStatsPanel) {
            renderStatsPanel(context);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    /**
     * Helper to draw multiline text with wrapping
     */
    private void drawWrappedText(DrawContext context, Text text, int x, int y, int maxWidth, int color, boolean centered) {
        var lines = this.textRenderer.wrapLines(text, maxWidth);
        int currentY = y;
        for (var line : lines) {
            int drawX = centered ? x - this.textRenderer.getWidth(line) / 2 : x;
            context.drawText(this.textRenderer, line, drawX, currentY, color, false);
            currentY += 10;
        }
    }

    private int parseHexColor(String hex) {
        try {
            return 0xFF000000 | Integer.parseInt(hex.replace("#", ""), 16);
        } catch (Exception e) {
            return 0xFF000000; // Default to Black
        }
    }

    private void renderStatsPanel(DrawContext context) {
        int panelWidth = 160;
        int panelHeight = (int) (this.height * 0.7);
        int panelX = this.width - panelWidth - 10;
        int panelY = (this.height - panelHeight) / 2;

        // Draw Panel Background
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, STATS_BACKGROUND, panelX, panelY, panelWidth, panelHeight);

        int ty = panelY + 10;
        context.drawCenteredTextWithShadow(this.textRenderer, "=== Tree Summary ===", panelX + panelWidth / 2, ty,
                0xFFFFAA00);
        ty += 15;

        // Get unified summary including both unlocked and drafted nodes
        var stats = PassiveStatHelper.getDisplayableSummary(MinecraftClient.getInstance().player, draftUnlocked);

        if (stats.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, "No stats unlocked", panelX + panelWidth / 2, ty + 20,
                    0x80AAAAAA);
        } else {
            for (Map.Entry<String, Double> entry : stats.entrySet()) {
                String key = entry.getKey();
                double total = entry.getValue();
                if (total == 0)
                    continue;

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

                context.drawTextWithShadow(this.textRenderer, key + ":", panelX + 8, ty, 0xFFAAAAAA);
                context.drawTextWithShadow(this.textRenderer, valStr,
                        panelX + panelWidth - 8 - this.textRenderer.getWidth(valStr), ty, 0xFF55FFFF);
                ty += 12;

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
            if (!unlocked.contains(node.id()) && !draftUnlocked.contains(node.id()) && draftPoints > 0) {
                Set<String> neighbors = adjacencyMap.get(node.id());
                if (neighbors != null) {
                    for (String conn : neighbors) {
                        if (unlocked.contains(conn) || draftUnlocked.contains(conn)) {
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
            if (unlocked.contains(id) || draftUnlocked.contains(id)) {
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

    /**
     * Traces the shortest path(s) back to the active tree for highlighting.
     */
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

    private void drawOptimizedLine(DrawContext context, float x1, float y1, float x2, float y2, Identifier sprite,
            int color, float width, float alpha) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len <= 0)
            return;

        float angle = (float) Math.atan2(dy, dx);
        float hw = width / 2.0f;

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x1, y1);
        matrices.rotate(angle);

        // Sprite approach: Correct for 1.21.2+ UI and handles color/alpha
        int finalColor = (color & 0x00FFFFFF) | ((int) (alpha * 255) << 24);
        context.drawGuiTexture(
                net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                sprite,
                0, (int) -hw, (int) len, (int) width,
                finalColor);

        matrices.popMatrix();
    }

    private void drawNodeShape(DrawContext context, double cx, double cy, String shape, String state,
            double actualSize) {
        Identifier sprite = switch (shape.toLowerCase()) {
            case "circle" -> switch (state) {
                case "unlocked" -> NODE_CIRCLE_UNLOCKED;
                case "drafted" -> NODE_CIRCLE_DRAFTED;
                case "available" -> NODE_CIRCLE_AVAILABLE;
                default -> NODE_CIRCLE_LOCKED;
            };
            case "diamond" -> switch (state) {
                case "unlocked" -> NODE_DIAMOND_UNLOCKED;
                case "drafted" -> NODE_DIAMOND_DRAFTED;
                case "available" -> NODE_DIAMOND_AVAILABLE;
                default -> NODE_DIAMOND_LOCKED;
            };
            case "hexagon" -> switch (state) {
                case "unlocked" -> NODE_HEXAGON_UNLOCKED;
                case "drafted" -> NODE_HEXAGON_DRAFTED;
                case "available" -> NODE_HEXAGON_AVAILABLE;
                default -> NODE_HEXAGON_LOCKED;
            };
            default -> switch (state) {
                case "unlocked" -> NODE_SQUARE_UNLOCKED;
                case "drafted" -> NODE_SQUARE_DRAFTED;
                case "available" -> NODE_SQUARE_AVAILABLE;
                default -> NODE_SQUARE_LOCKED;
            };
        };

        drawPrecisionTexture(context, sprite, cx, cy, actualSize, 0xFFFFFFFF);
    }

    /**
     * Draws a texture centered on a double coordinate with high precision rounding
     * to prevent displacement "jitter" during zooming.
     */
    private void drawPrecisionTexture(DrawContext context, Identifier texture, double cx, double cy, double size,
            int color) {
        int x = (int) Math.round(cx - size / 2.0);
        int y = (int) Math.round(cy - size / 2.0);
        int sx = (int) Math.round(cx + size / 2.0) - x;
        int sy = (int) Math.round(cy + size / 2.0) - y;

        context.drawGuiTexture(
                net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                texture,
                x, y, sx, sy,
                color);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!choosingClass) {
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
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (!choosingClass) {
            if (editMode && draggedNode != null) {
                double currentGridScale = BASE_GRID_SCALE * zoom;
                draggedNode.setGridX(draggedNode.gridX() + (deltaX / currentGridScale));
                draggedNode.setGridY(draggedNode.gridY() + (deltaY / currentGridScale));
                this.cacheDirty = true;
                return true;
            }
            panX += deltaX;
            panY += deltaY;
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0) {
            draggedNode = null;
        }
        return super.mouseReleased(click);
    }

    public boolean keyPressed(KeyInput input) {
        if (input.key() == 258) { // TAB key
            editMode = !editMode;
            if (!editMode) {
                draggedNode = null;
                isConnecting = false;
                connectionSource = null;
            }
            this.init();
            return true;
        }
        return super.keyPressed(input);
    }

    public void startConnecting(PassiveNode source) {
        this.connectionSource = source;
        this.isConnecting = true;
        this.client.setScreen(this);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        var player = MinecraftClient.getInstance().player;
        if (player == null) return false;

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
                        draggedNode = hitNode;
                        return true;
                    }
                }
            } else if (button == 1) { // Right Click: Open Editor
                if (hitNode != null) {
                    this.client.setScreen(new PassiveNodeEditScreen(this, hitNode));
                } else {
                    // CREATE NEW NODE
                    double gx = (mouseX - panX) / currentGridScale;
                    double gy = (mouseY - panY) / currentGridScale;
                    this.client.setScreen(new PassiveNodeEditScreen(this, gx, gy));
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
                    if (draftUnlocked.contains(node.id())) {
                        // Toggle OFF: Unselect drafted node
                        draftUnlocked.remove(node.id());
                        draftPoints++;
                        player.playSound(ModSounds.PASSIVE_NODE_SELECT, 0.5f, 0.8f); // Pitched down for deselect
                        this.cacheDirty = true;
                        revalidateDraft();
                        return true;
                    }

                    boolean isPermanent = unlocked.contains(node.id());
                    if (isPermanent)
                        return true; // Already permanently unlocked

                    // Node is locked and not in draft: try to select it (Unified check)
                    boolean canUnlock = false;
                    Set<String> neighbors = adjacencyMap.get(node.id());
                    if (neighbors != null) {
                        for (String conn : neighbors) {
                            if (unlocked.contains(conn) || draftUnlocked.contains(conn)) {
                                canUnlock = true;
                                break;
                            }
                        }
                    }

                    if (draftPoints > 0 && canUnlock) {
                        draftUnlocked.add(node.id());
                        draftPoints--;
                        player.playSound(ModSounds.PASSIVE_NODE_SELECT, 0.6f, 1.2f); // Pitched up for select
                        this.cacheDirty = true;
                    }
                    return true;
                }
            }
        } else if (!choosingClass && button == 1) { // Right Click (Designer Mode)
            if (editMode) {
                if (draggedNode != null) {
                    draggedNode = null; // Drop
                    if (this.client != null && this.client.player != null) {
                        this.client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4f, 0.5f);
                    }
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
                            if (this.client != null && this.client.player != null) {
                                this.client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4f, 1.5f);
                            }
                            return true;
                        }
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(click, bl);
    }

    /**
     * Re-check all drafted nodes for reachability. Any node that is no longer
     * connected
     * to a permanently unlocked node is removed from the draft and its point
     * refunded.
     */
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
                    if (!stillReachable.contains(neighborId) && draftUnlocked.contains(neighborId)) {
                        stillReachable.add(neighborId);
                        queue.add(neighborId);
                    }
                }
            }
        }

        // Prune orphans
        java.util.Iterator<String> it = draftUnlocked.iterator();
        while (it.hasNext()) {
            String draftId = it.next();
            if (!stillReachable.contains(draftId)) {
                it.remove();
                draftPoints++;
                this.cacheDirty = true;
            }
        }
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(this.parent);
    }
}
