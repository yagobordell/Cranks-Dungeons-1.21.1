package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.component.PassiveDataComponent;
import com.cranks.dungeons.network.SelectClassPayload;
import com.cranks.dungeons.network.UnlockNodePayload;
import com.cranks.dungeons.stat.passive.PassiveNode;
import com.cranks.dungeons.stat.passive.PassiveNodeRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Set;

public class PassiveSkillTreeScreen extends Screen {
    private final Screen parent;
    private double panX = 0;
    private double panY = 0;
    private double zoom = 1.0;
    private static final int BASE_NODE_SIZE = 24;
    private static final int BASE_GRID_SCALE = 60; // Pixels per grid unit

    private boolean choosingClass = false;
    private final java.util.Set<String> draftUnlocked = new java.util.HashSet<>();
    private int draftPoints;

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
            ClientPlayNetworking.send(new com.cranks.dungeons.network.BulkUnlockNodesPayload(new java.util.ArrayList<>(draftUnlocked)));
            draftUnlocked.clear();
            // We'll update points on next tick or just let server sync handle it. 
            // Better: update client-side points now.
            var data = ModComponents.PASSIVE_DATA.get(MinecraftClient.getInstance().player);
            data.setUnspentPoints(draftPoints);
            for (String id : draftUnlocked) data.unlockNode(id);
            this.init(); 
        }
    }

    private void resetDraft() {
        draftUnlocked.clear();
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
        
        this.clearChildren();
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
        
        // Draw Connections
        Map<String, PassiveNode> nodes = PassiveNodeRegistry.getNodes();
        java.util.Collection<PassiveNode> allNodes = nodes.values();
        
        double currentGridScale = BASE_GRID_SCALE * zoom;
        double currentNodeSize = BASE_NODE_SIZE * zoom;
        int margin = 50;
        int screenW = this.width;
        int screenH = this.height;

        for (PassiveNode node : allNodes) {
            int x1 = (int) (panX + node.gridX() * currentGridScale);
            int y1 = (int) (panY + node.gridY() * currentGridScale);
            
            for (String connectedId : node.connections()) {
                if (node.id().compareTo(connectedId) >= 0) continue;

                PassiveNode connected = nodes.get(connectedId);
                if (connected != null) {
                    int x2 = (int) (panX + connected.gridX() * currentGridScale);
                    int y2 = (int) (panY + connected.gridY() * currentGridScale);
                    
                    if ((x1 < -margin && x2 < -margin) || (x1 > screenW + margin && x2 > screenW + margin) ||
                        (y1 < -margin && y2 < -margin) || (y1 > screenH + margin && y2 > screenH + margin)) {
                        continue;
                    }

                    boolean lineActive = (unlocked.contains(node.id()) || draftUnlocked.contains(node.id())) && 
                                       (unlocked.contains(connectedId) || draftUnlocked.contains(connectedId));
                    int color = lineActive ? 0xFFffd700 : 0xFF444444;
                    
                    drawLine(context, x1, y1, x2, y2, color);
                }
            }
        }
        
        // Draw Nodes
        PassiveNode hovered = null;
        for (PassiveNode node : allNodes) {
            int nx = (int) (panX + node.gridX() * currentGridScale);
            int ny = (int) (panY + node.gridY() * currentGridScale);
            
            if (nx < -margin || nx > screenW + margin || ny < -margin || ny > screenH + margin) {
                continue;
            }

            boolean isUnlocked = unlocked.contains(node.id()) || draftUnlocked.contains(node.id());
            boolean isDraft = draftUnlocked.contains(node.id());
            boolean canUnlock = false;
            
            if (!isUnlocked && draftPoints > 0) {
                for (String conn : node.connections()) {
                    if (unlocked.contains(conn) || draftUnlocked.contains(conn)) {
                        canUnlock = true;
                        break;
                    }
                }
            }
            
            int drawX = (int) (nx - currentNodeSize / 2);
            int drawY = (int) (ny - currentNodeSize / 2);
            
            // Draw shape only if it exists
            if (node.shape() != null && !node.shape().isEmpty()) {
                int colorOutline = isDraft ? 0xFF00FFFF : (isUnlocked ? 0xFFffd700 : (canUnlock ? 0xFF00FF00 : 0xFF444444));
                int colorFill = isUnlocked ? 0xFFaa8800 : 0xFF222222;
                drawNodeShape(context, nx, ny, colorOutline, colorFill, node.shape(), currentNodeSize);
            }

            // Draw Icon (now scaled to full node size)
            if (node.icon() != null) {
                int iconSize = (int) currentNodeSize;
                context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, node.icon(), nx - iconSize/2, ny - iconSize/2, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
            }

            // Highlight on hover
            if (mouseX >= drawX && mouseX <= drawX + currentNodeSize && mouseY >= drawY && mouseY <= drawY + currentNodeSize) {
                hovered = node;
                // Use square as default hover highlight if no shape is specified
                String hoverShape = (node.shape() != null && !node.shape().isEmpty()) ? node.shape() : "square";
                drawNodeShape(context, nx, ny, 0xFFFFFFFF, -1, hoverShape, currentNodeSize);
            }
        }
        
        if (hovered != null) {
            context.drawTooltip(this.textRenderer, Text.literal("§6" + hovered.name() + (draftUnlocked.contains(hovered.id()) ? " §b[PENDING]" : "") + "\n§7" + hovered.description()), mouseX, mouseY);
        }

        // Draw points at the bottom center (using opaque orange 0xFFFFAA00)
        String pointsText = "Unspent Points: " + draftPoints;
        context.drawCenteredTextWithShadow(this.textRenderer, pointsText, this.width / 2, this.height - 48, 0xFFFFAA00);
        
        super.render(context, mouseX, mouseY, delta); // Draw buttons and children
    }

    private void drawNodeShape(DrawContext context, int cx, int cy, int colorOutline, int colorFill, String shape, double nodeSize) {
        int half = (int) (nodeSize / 2);
        if ("diamond".equals(shape)) {
            if (colorOutline != -1) {
                for (int i = 0; i <= half + 2; i += 2) {
                    int w = Math.min(2, half + 2 - i + 1);
                    context.fill(cx - i, cy - (half + 2 - i), cx + i + 1, cy - (half + 2 - i) + w, colorOutline);
                    context.fill(cx - i, cy + (half + 2 - i), cx + i + 1, cy + (half + 2 - i) + w, colorOutline);
                }
            }
            if (colorFill != -1) {
                for (int i = 0; i <= half - 2; i++) {
                    context.fill(cx - i, cy - (half - 2 - i), cx + i + 1, cy - (half - 2 - i) + 1, colorFill);
                    context.fill(cx - i, cy + (half - 2 - i), cx + i + 1, cy + (half - 2 - i) + 1, colorFill);
                }
            }
        } else if ("circle".equals(shape)) {
            if (colorOutline != -1) {
                context.fill(cx - half, cy - half + 4, cx + half, cy + half - 4, colorOutline);
                context.fill(cx - half + 4, cy - half, cx + half - 4, cy + half, colorOutline);
                context.fill(cx - half + 2, cy - half + 2, cx + half - 2, cy + half - 2, colorOutline);
            }
            if (colorFill != -1) {
                context.fill(cx - half + 2, cy - half + 4, cx + half - 2, cy + half - 4, colorFill);
                context.fill(cx - half + 4, cy - half + 2, cx + half - 4, cy + half - 2, colorFill);
            }
        } else { // "square"
            if (colorOutline != -1) context.fill(cx - half, cy - half, cx + half, cy + half, colorOutline);
            if (colorFill != -1) context.fill(cx - half + 2, cy - half + 2, cx + half - 2, cy + half - 2, colorFill);
        }
    }
    
    // Previous drawLine (kept for reuse)
    private void drawLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        if (x1 == x2) {
            context.fill(x1 - 1, Math.min(y1, y2), x1 + 2, Math.max(y1, y2), color);
            return;
        }
        if (y1 == y2) {
            context.fill(Math.min(x1, x2), y1 - 1, Math.max(x1, x2), y1 + 2, color);
            return;
        }
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy)) / 4;
        if (steps < 1) steps = 1;
        for (int i = 0; i <= steps; i++) {
            float t = (float) i / steps;
            int x = (int) (x1 + t * dx);
            int y = (int) (y1 + t * dy);
            context.fill(x - 1, y - 1, x + 2, y + 2, color);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!choosingClass) {
            double zoomFactor = verticalAmount > 0 ? 1.1 : 0.9;
            double nextZoom = Math.max(0.2, Math.min(2.0, zoom * zoomFactor));
            
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
            PassiveDataComponent data = ModComponents.PASSIVE_DATA.get(MinecraftClient.getInstance().player);
            Set<String> unlocked = data.getUnlockedNodes();
            
            double currentGridScale = BASE_GRID_SCALE * zoom;
            double currentNodeSize = BASE_NODE_SIZE * zoom;
            int halfSize = (int) (currentNodeSize / 2);

            Map<String, PassiveNode> nodes = PassiveNodeRegistry.getNodes();
            for (PassiveNode node : nodes.values()) {
                int nx = (int) (panX + node.gridX() * currentGridScale);
                int ny = (int) (panY + node.gridY() * currentGridScale);
                
                if (mouseX >= nx - halfSize && mouseX <= nx + halfSize && mouseY >= ny - halfSize && mouseY <= ny + halfSize) {
                    if (draftUnlocked.contains(node.id())) {
                        // Toggle OFF: Unselect drafted node
                        draftUnlocked.remove(node.id());
                        draftPoints++;
                        revalidateDraft();
                        return true;
                    }

                    boolean isPermanent = unlocked.contains(node.id());
                    if (isPermanent) return true; // Already permanently unlocked

                    // Node is locked and not in draft: try to select it
                    boolean canUnlock = false;
                    for (String conn : node.connections()) {
                        if (unlocked.contains(conn) || draftUnlocked.contains(conn)) {
                            canUnlock = true;
                            break;
                        }
                    }

                    if (draftPoints > 0 && canUnlock) {
                        draftUnlocked.add(node.id());
                        draftPoints--;
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
        
        Set<String> stillReachable = new java.util.HashSet<>(permanent);
        java.util.Queue<String> queue = new java.util.LinkedList<>(permanent);
        
        Map<String, PassiveNode> allNodes = PassiveNodeRegistry.getNodes();
        
        // BFS for reachability
        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            PassiveNode currentNode = allNodes.get(currentId);
            if (currentNode == null) continue;
            
            for (String neighborId : currentNode.connections()) {
                if (!stillReachable.contains(neighborId) && draftUnlocked.contains(neighborId)) {
                    stillReachable.add(neighborId);
                    queue.add(neighborId);
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
            }
        }
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(this.parent);
    }
}
