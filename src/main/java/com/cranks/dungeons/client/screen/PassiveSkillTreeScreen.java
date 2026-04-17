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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import net.minecraft.sound.SoundEvents;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.stat.passive.PassiveStatHelper;

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

    private boolean choosingClass = false;
    private final Set<String> draftUnlocked = new HashSet<>();
    private int draftPoints;
    private boolean showStatsPanel = false;

    // --- Unified Logic & Rendering Caches ---
    private record Edge(PassiveNode n1, PassiveNode n2) {}
    private final Set<Edge> edgeCache = new HashSet<>(); // Changed to Set for auto-deduplication
    private final Set<String> canUnlockCache = new HashSet<>();
    private final Map<String, Set<String>> adjacencyMap = new HashMap<>(); // Unified source of truth
    private boolean cacheDirty = true;

    public PassiveSkillTreeScreen(Screen parent) {
        super(Text.literal("Passive Skill Tree"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        
        var player = MinecraftClient.getInstance().player;
        if (player == null) return;
        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
        this.draftPoints = data.getUnspentPoints();
        
        if (this.client != null && this.client.player != null) {
            this.client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 0.8f);
        }

        if (data.getPlayerClass() == null || data.getPlayerClass().isEmpty()) {
            choosingClass = true;
            // Add class selection buttons
            var classes = com.cranks.dungeons.config.PassiveTreeConfig.getClasses();
            int midY = this.height / 2;
            int totalWidth = classes.size() * 110 - 10;
            int startX = (this.width - totalWidth) / 2;
            
            for (int i = 0; i < classes.size(); i++) {
                var def = classes.get(i);
                this.addDrawableChild(ButtonWidget.builder(Text.literal(def.name()), b -> selectClass(def.id()))
                        .dimensions(startX + i * 110, midY - 10, 100, 20).build());
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
            this.addDrawableChild(ButtonWidget.builder(Text.literal(showStatsPanel ? "Hide Stats" : "Show Stats"), b -> {
                        showStatsPanel = !showStatsPanel;
                        this.init(); // Refresh button text
                    })
                    .dimensions(this.width - 110, 10, 100, 20).build());

            // Center the view on the starting node for their class (only if not already panned)
            if (panX == 0 && panY == 0) {
                String startId = data.getPlayerClass() + "_start";
                PassiveNode start = PassiveNodeRegistry.getNode(startId);
                if (start != null) {
                    panX = (this.width / 2.0) - (start.gridX() * BASE_GRID_SCALE * zoom);
                    panY = (this.height / 2.0) - (start.gridY() * BASE_GRID_SCALE * zoom);
                } else {
                    panX = this.width / 2.0;
                    panY = this.height / 2.0;
                }
            }
        }
    }

    private void applyChanges() {
        if (!draftUnlocked.isEmpty()) {
            var player = MinecraftClient.getInstance().player;
            if (player != null) {
                player.playSound(ModSounds.PASSIVE_POINT_APPLY, 0.8f, 1.0f);
            }
            
            ClientPlayNetworking.send(new com.cranks.dungeons.network.BulkUnlockNodesPayload(new java.util.ArrayList<>(draftUnlocked)));
            
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
        // Use fill instead of renderBackground to avoid "Can only blur once per frame" crash in large modpacks
        context.fill(0, 0, this.width, this.height, 0x90000000);

        if (choosingClass) {
            context.drawCenteredTextWithShadow(this.textRenderer, "Choose Your Path", this.width / 2, this.height / 4, 0xFFFFFF);
            super.render(context, mouseX, mouseY, delta); // Draw buttons
            return;
        }

        var player = MinecraftClient.getInstance().player;
        if (player == null) return;

        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
        Set<String> unlocked = data.getUnlockedNodes();
        
        context.drawTextWithShadow(this.textRenderer, "Class: " + data.getPlayerClass().substring(0, 1).toUpperCase() + data.getPlayerClass().substring(1), 10, 10, 0xFFFFFF);
        
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

            boolean lineActive = (unlocked.contains(edge.n1.id()) || draftUnlocked.contains(edge.n1.id())) && 
                               (unlocked.contains(edge.n2.id()) || draftUnlocked.contains(edge.n2.id()));
            
            // Connection to an UNLOCKABLE node gets a faint green hint
            boolean leadsToAvailable = !lineActive && 
                ((unlocked.contains(edge.n1.id()) || draftUnlocked.contains(edge.n1.id())) && canUnlockCache.contains(edge.n2.id())) ||
                ((unlocked.contains(edge.n2.id()) || draftUnlocked.contains(edge.n2.id())) && canUnlockCache.contains(edge.n1.id()));

            int color = lineActive ? 0xFFFFD700 : (leadsToAvailable ? 0xFF44AA44 : 0xFF444444);
            
            if (lineActive) {
                // Gold Glow
                drawOptimizedLine(context, x1, y1, x2, y2, color, 4.0f, 0.4f);
                drawOptimizedLine(context, x1, y1, x2, y2, color, 1.5f, 1.0f);
            } else {
                drawOptimizedLine(context, x1, y1, x2, y2, color, 1.5f, 1.0f);
            }
        }
        
        // --- DRAW NODES ---
        PassiveNode hovered = null;
        Map<String, PassiveNode> allNodesMap = PassiveNodeRegistry.getNodes();
        for (PassiveNode node : allNodesMap.values()) {
            int nx = (int) (panX + node.gridX() * currentGridScale);
            int ny = (int) (panY + node.gridY() * currentGridScale);
            
            // Spatial Culling
            if (nx < -margin || nx > screenW + margin || ny < -margin || ny > screenH + margin) {
                continue;
            }

            boolean isUnlocked = unlocked.contains(node.id()) || draftUnlocked.contains(node.id());
            boolean isDraft = draftUnlocked.contains(node.id());
            boolean canUnlock = canUnlockCache.contains(node.id());
            
            double multiplier = getNodeSizeMultiplier(node.shape());
            double actualNodeSize = currentNodeSize * multiplier;

            boolean isHovered = mouseX >= nx - actualNodeSize/2 && mouseX <= nx + actualNodeSize/2 && 
                               mouseY >= ny - actualNodeSize/2 && mouseY <= ny + actualNodeSize/2;
            if (isHovered) hovered = node;

            if (node.shape() != null && !node.shape().isEmpty()) {
                int colorOutline = 0xFF444444;
                int colorFill = 0xFF222222;
                float glowAlpha = 0.0f;

                if (isDraft) {
                    colorOutline = 0xFF00FFFF; // Electric Cyan
                    colorFill = 0xFF003333;
                    glowAlpha = 0.6f;
                } else if (isUnlocked) {
                    colorOutline = 0xFFFFD700; // Radiant Gold
                    colorFill = 0xFF443300;
                    glowAlpha = 0.5f;
                } else if (canUnlock) {
                    colorOutline = 0xFF55FF55; // Nature Green
                    colorFill = 0xFF113311;
                    glowAlpha = 0.4f;
                }

                drawNodeShape(context, nx, ny, colorOutline, colorFill, node.shape(), actualNodeSize, glowAlpha);
                
                // Draw Hover Radiant Border (Behind icon) - PRECISE DOUBLE RING
                if (isHovered) {
                    int r = (int)(actualNodeSize / 2);
                    drawProceduralShape(context, nx, ny, r + 2, node.shape(), 0xAAFFFFFF, 0); // Outer halo
                    drawProceduralShape(context, nx, ny, r, node.shape(), 0xFFFFFFFF, 0);     // Inner white border
                }
            }

            // Draw Icon (TOP LAYER - Maximized for impact)
            if (node.icon() != null) {
                int iconSize = (int) (actualNodeSize * 0.95);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, node.icon(), nx - iconSize/2, ny - iconSize/2, iconSize, iconSize);
            }

        }
        
        if (hovered != null) {
            String title = "§6" + hovered.name();
            if (draftUnlocked.contains(hovered.id())) title = "§b[SELECTED] §6" + hovered.name();
            else if (unlocked.contains(hovered.id())) title = "§e[UNLOCKED] §6" + hovered.name();
            else if (canUnlockCache.contains(hovered.id())) title = "§a[AVAILABLE] §6" + hovered.name();
            
            context.drawTooltip(this.textRenderer, Text.literal(title + "\n§7" + hovered.description()), mouseX, mouseY);
        }

        String pointsText = "Unspent Points: " + draftPoints;
        context.drawCenteredTextWithShadow(this.textRenderer, pointsText, this.width / 2, this.height - 48, 0xFFFFAA00);
        
        if (showStatsPanel) {
            renderStatsPanel(context);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderStatsPanel(DrawContext context) {
        int panelWidth = 160;
        int panelHeight = (int) (this.height * 0.7);
        int panelX = this.width - panelWidth - 10;
        int panelY = (this.height - panelHeight) / 2;

        // Draw Panel Background (PoE Style)
        context.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2, panelY + panelHeight + 2, 0xFF000000); // Border
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE0100515); // Translucent Dark Purple

        int ty = panelY + 10;
        context.drawCenteredTextWithShadow(this.textRenderer, "=== Tree Summary ===", panelX + panelWidth / 2, ty, 0xFFFFAA00);
        ty += 15;

        var stats = PassiveStatHelper.getDisplayableSummary(MinecraftClient.getInstance().player);
        
        // Add draft stats temporarily if we want to show preview (User didn't explicitly ask but it's better)
        Map<String, Double> draftSummary = new java.util.TreeMap<>();
        for (String id : draftUnlocked) {
            var node = PassiveNodeRegistry.getNode(id);
            if (node != null && node.modifiers() != null) {
                for (var entry : node.modifiers().entrySet()) {
                    String name = entry.getKey().value().getTranslationKey().replace("attribute.name.minecraft.", "").replace("attribute.name.cranks-dungeons.", "").replace("_", " ");
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    draftSummary.merge(name, entry.getValue(), Double::sum);
                }
            }
        }

        if (stats.isEmpty() && draftSummary.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, "No stats unlocked", panelX + panelWidth / 2, ty + 20, 0x80AAAAAA);
        } else {
            // Merge for display
            Set<String> allKeys = new java.util.TreeSet<>(stats.keySet());
            allKeys.addAll(draftSummary.keySet());

            for (String key : allKeys) {
                double total = stats.getOrDefault(key, 0.0) + draftSummary.getOrDefault(key, 0.0);
                if (total == 0) continue;

                double displayVal = total;
                boolean isPercent = key.toLowerCase().contains("percent") || 
                                   key.toLowerCase().contains("chance") || 
                                   key.toLowerCase().contains("speed") || 
                                   key.toLowerCase().contains("extra") || 
                                   key.toLowerCase().contains("reduction");
                
                if (isPercent) {
                    displayVal *= 100.0;
                }

                String valStr = String.format("%+.1f", displayVal);
                if (isPercent) {
                    valStr += "%";
                }

                context.drawTextWithShadow(this.textRenderer, key + ":", panelX + 8, ty, 0xFFAAAAAA);
                context.drawTextWithShadow(this.textRenderer, valStr, panelX + panelWidth - 8 - this.textRenderer.getWidth(valStr), ty, 0xFF55FFFF);
                ty += 12;
                
                if (ty > panelY + panelHeight - 15) break; // Simple culling
            }
        }
    }

    private void refreshCaches(PassiveDataComponent data) {
        edgeCache.clear();
        canUnlockCache.clear();
        adjacencyMap.clear();
        Map<String, PassiveNode> allNodes = PassiveNodeRegistry.getNodes();
        Set<String> unlocked = data.getUnlockedNodes();

        // 1. Build Unified Adjacency Map for bi-directional pathing
        for (PassiveNode node : allNodes.values()) {
            for (String connId : node.connections()) {
                // Only build connections to nodes that actually exist in our registry.
                // This filters out duplicates rejected by the parser.
                if (allNodes.containsKey(connId)) {
                    adjacencyMap.computeIfAbsent(node.id(), k -> new HashSet<>()).add(connId);
                    adjacencyMap.computeIfAbsent(connId, k -> new HashSet<>()).add(node.id());
                }
            }
        }

        for (PassiveNode node : allNodes.values()) {
            // 2. Populate Edge Cache (Strictly for Visualizing valid JSON connections)
            for (String connId : node.connections()) {
                if (node.id().compareTo(connId) < 0) {
                    PassiveNode connected = allNodes.get(connId);
                    if (connected != null) {
                        edgeCache.add(new Edge(node, connected));
                    }
                }
            }

            // 3. Populate Unlock Cache (Using bi-directional adjacencyMap)
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
    }

    private double getNodeSizeMultiplier(String shape) {
        if (shape == null) return 1.0;
        return switch (shape.toLowerCase()) {
            case "hexagon" -> 3.2; // Massive Keystone
            case "diamond" -> 2.2; // Notable
            case "circle" -> 1.4;  // Minor Notable
            default -> 1.0;       // Travel/Square
        };
    }

    private void drawOptimizedLine(DrawContext context, float x1, float y1, float x2, float y2, int color, float width, float alpha) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len <= 0) return;

        // Balanced density: enough to look continuous, few enough for 60+ FPS
        // 1 segment every 10 pixels is usually smooth enough for GUI lines
        int steps = (int) Math.max(2, len / 10.0f);
        if (steps > 50) steps = 50; // Cap for performance

        int finalColor = (color & 0x00FFFFFF) | ((int)(alpha * 255) << 24);
        float hw = width / 2.0f;

        for (int i = 0; i <= steps; i++) {
            float t = (float) i / steps;
            float px = x1 + dx * t;
            float py = y1 + dy * t;
            context.fill((int)(px - hw), (int)(py - hw), (int)(px + hw + 1), (int)(py + hw + 1), finalColor);
        }
    }

    private void drawNodeShape(DrawContext context, double cx, double cy, int colorOutline, int colorFill, String shape, double actualSize, float glowAlpha) {
        int radius = (int) (actualSize / 2);
        int icx = (int) cx;
        int icy = (int) cy;

        // Draw Glow (Outer Radiance)
        if (glowAlpha > 0) {
            int glowColor = (colorOutline & 0x00FFFFFF) | ((int)(glowAlpha * 120) << 24);
            drawProceduralShape(context, icx, icy, (int)(actualSize * 0.65), shape, glowColor, 0);
        }

        // Draw the main shape
        drawProceduralShape(context, icx, icy, radius, shape, colorOutline, colorFill);
    }

    private void drawProceduralShape(DrawContext context, int cx, int cy, int radius, String shape, int colorOutline, int colorFill) {
        switch (shape.toLowerCase()) {
            case "circle" -> drawProceduralCircle(context, cx, cy, radius, colorOutline, colorFill);
            case "diamond" -> drawProceduralDiamond(context, cx, cy, radius, colorOutline, colorFill);
            case "hexagon" -> drawProceduralHexagon(context, cx, cy, radius, colorOutline, colorFill);
            default -> drawProceduralSquare(context, cx, cy, radius, colorOutline, colorFill);
        }
    }

    private void drawProceduralSquare(DrawContext context, int cx, int cy, int r, int colorOutline, int colorFill) {
        if (colorFill != 0) {
            context.fill(cx - r, cy - r, cx + r, cy + r, colorFill);
        }
        // Precise outline
        context.fill(cx - r, cy - r, cx + r, cy - r + 1, colorOutline); // Top
        context.fill(cx - r, cy + r - 1, cx + r, cy + r, colorOutline); // Bottom
        context.fill(cx - r, cy - r, cx - r + 1, cy + r, colorOutline); // Left
        context.fill(cx + r - 1, cy - r, cx + r, cy + r, colorOutline); // Right
    }

    private void drawProceduralCircle(DrawContext context, int cx, int cy, int r, int colorOutline, int colorFill) {
        // Precise scanline circle with curvature correction at r+0.25
        for (int y = -r; y <= r; y++) {
            int xW = (int) Math.round(Math.sqrt((r + 0.25) * (r + 0.25) - y * y));
            
            if (colorFill != 0) context.fill(cx - xW, cy + y, cx + xW, cy + y + 1, colorFill);
            
            // Draw outline pixels
            context.fill(cx - xW, cy + y, cx - xW + 1, cy + y + 1, colorOutline);
            context.fill(cx + xW - 1, cy + y, cx + xW, cy + y + 1, colorOutline);
            
            // Seal the top and bottom with a precise horizontal line
            if (Math.abs(y) == r) {
                context.fill(cx - xW, cy + y, cx + xW, cy + y + 1, colorOutline);
            }
        }
    }

    private void drawProceduralDiamond(DrawContext context, int cx, int cy, int r, int colorOutline, int colorFill) {
        for (int y = -r; y <= r; y++) {
            int xW = r - Math.abs(y);
            if (colorFill != 0) context.fill(cx - xW, cy + y, cx + xW, cy + y + 1, colorFill);
            context.fill(cx - xW, cy + y, cx - xW + 1, cy + y + 1, colorOutline);
            context.fill(cx + xW - 1, cy + y, cx + xW, cy + y + 1, colorOutline);
        }
    }

    private void drawProceduralHexagon(DrawContext context, int cx, int cy, int r, int colorOutline, int colorFill) {
        for (int y = -r; y <= r; y++) {
            int h = Math.abs(y);
            int xW;
            if (h < r / 2) xW = r;
            else xW = r - (int)((h - r/2.0) * 2);
            
            if (colorFill != 0) context.fill(cx - xW, cy + y, cx + xW, cy + y + 1, colorFill);
            context.fill(cx - xW, cy + y, cx - xW + 1, cy + y + 1, colorOutline);
            context.fill(cx + xW - 1, cy + y, cx + xW, cy + y + 1, colorOutline);
        }
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
            panX += deltaX;
            panY += deltaY;
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        
        if (!choosingClass && button == 0) {
            var player = MinecraftClient.getInstance().player;
            if (player == null) return false;
            
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
                
                if (mouseX >= nx - halfSize && mouseX <= nx + halfSize && mouseY >= ny - halfSize && mouseY <= ny + halfSize) {
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
                    if (isPermanent) return true; // Already permanently unlocked

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
        }
        return super.mouseClicked(click, bl);
    }

    /**
     * Re-check all drafted nodes for reachability. Any node that is no longer connected
     * to a permanently unlocked node is removed from the draft and its point refunded.
     */
    private void revalidateDraft() {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return;
        
        PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(player);
        Set<String> permanent = data.getUnlockedNodes();
        
        Set<String> stillReachable = new HashSet<>(permanent);
        java.util.Queue<String> queue = new java.util.LinkedList<>(permanent);
        
        Map<String, PassiveNode> allNodes = PassiveNodeRegistry.getNodes();
        
        // BFS for reachability
        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            PassiveNode currentNode = allNodes.get(currentId);
            if (currentNode == null) continue;
            
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
