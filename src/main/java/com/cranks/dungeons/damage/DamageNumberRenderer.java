package com.cranks.dungeons.damage;

import com.cranks.dungeons.client.render.EntityStatBarRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Pixel outlined combat text, projected using the same camera matrix as entity plates. */
public final class DamageNumberRenderer {
    private record Glyph(Text text, int color, float scale, float x, float y, boolean critical) {}
    private record Layout(int revision, float width, float height, List<Glyph> glyphs) {}
    private record Rect(float x, float y, float w, float h) {
        boolean overlaps(Rect r) { return x < r.x+r.w+4 && x+w+4 > r.x && y < r.y+r.h+4 && y+h+4 > r.y; }
    }
    private static final Map<DamageNumber, Layout> LAYOUTS = new IdentityHashMap<>();
    private static int lastWidth, lastHeight;
    private DamageNumberRenderer() {}

    public static void renderHud(DrawContext context, float tickDelta) {
        var client = MinecraftClient.getInstance();
        long now = DamageNumberManager.now();
        var numbers = DamageNumberManager.getActiveNumbers();
        LAYOUTS.keySet().removeIf(n -> !numbers.contains(n));
        if (client.player == null || client.world == null || client.options.hudHidden) return;
        int width = context.getScaledWindowWidth(), height = context.getScaledWindowHeight();
        if (width != lastWidth || height != lastHeight) {
            for (var n : numbers) { n.placed=false; n.nextPlacementTime=0; }
            lastWidth=width; lastHeight=height;
        }
        var occupied = new ArrayList<Rect>();
        // Reserve the reticle and the target's face/nameplate area.
        occupied.add(new Rect(width/2f-14, height/2f-15, 28, 30));
        occupied.add(new Rect(0, height-52, width, 52));
        for (var number : numbers) {
            if (number.expired(now)) continue;
            var layout = LAYOUTS.get(number);
            if (layout == null || layout.revision != number.revision) {
                layout = layout(client, number); LAYOUTS.put(number, layout); number.nextPlacementTime=0;
            }
            boolean self = client.player.getUuid().equals(number.victimUuid);
            float anchorX, anchorY;
            if (self) {
                // Consistent self feedback in both camera perspectives.
                anchorX = width / 2f + (number.kind.healing() ? 78 : -78);
                anchorY = height / 2f + 42;
            } else {
                Entity entity = client.world.getEntityById(number.victimEntityId);
                if (entity != null && !entity.getUuid().equals(number.victimUuid)) entity = null;
                if (!number.detached && entity != null && entity.isAlive()) {
                    number.position = new Vec3d(MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()),
                            MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()) + entity.getHeight()*0.65,
                            MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()));
                } else number.detached = true;
                double range = number.important ? 128 : 32;
                if (client.gameRenderer.getCamera().getPos().squaredDistanceTo(number.position) > range*range) continue;
                var point = EntityStatBarRenderer.projectToScreen(client, number.position.x, number.position.y, number.position.z);
                if (point == null) continue;
                anchorX = point.x(); anchorY = point.y();
                // Avoid covering the face, name and ailment indicators of this target.
                if (entity != null) {
                    var head = EntityStatBarRenderer.projectToScreen(client, number.position.x,
                            entity.getY()+entity.getHeight()+0.28, number.position.z);
                    if (head != null) occupied.add(new Rect(head.x()-65, head.y()-45, 130, 48));
                }
            }
            float drift = (float)(Math.sqrt(number.age(now) / 1000.0) * 16);
            if (!number.placed) {
                if (now < number.nextPlacementTime && Math.abs(anchorX-number.waitingX) < 2 && Math.abs(anchorY-number.waitingY) < 2) continue;
                if (!place(number, layout, anchorX, anchorY, width, height, occupied, self)) {
                    number.nextPlacementTime=now+150; number.waitingX=anchorX; number.waitingY=anchorY; continue;
                }
            }
            float x = anchorX + number.offsetX, y = anchorY + number.offsetY - drift;
            var rect = new Rect(x-layout.width/2, y-layout.height/2, layout.width, layout.height);
            float travel = (float)(Math.sqrt(number.lifetime()/1000.0)*16);
            // Reserve the complete travel corridor, so the float never rises into a plate or another hit.
            occupied.add(new Rect(rect.x, anchorY+number.offsetY-layout.height/2-travel, rect.w, rect.h+travel));
            if (rect.x+rect.w < 0 || rect.x > width || rect.y+rect.h < 0 || rect.y > height) continue;
            float alpha = CombatNumberStyle.alpha(number.age(now), number.lifetime());
            if (alpha <= 0.02f) continue;
            float pop = CombatNumberStyle.entrance(number.age(now), number.critical);
            var matrices = context.getMatrices();
            matrices.push(); matrices.translate(Math.round(x), Math.round(y), 0); matrices.scale(pop,pop,1);
            for (var glyph : layout.glyphs) {
                matrices.push(); matrices.translate(glyph.x, glyph.y, 0); matrices.scale(glyph.scale,glyph.scale,1);
                int a = Math.max(1, (int)(alpha*255));
                int outline = (a << 24) | 0x08090A;
                for (int ox=-1;ox<=1;ox++) for (int oy=-1;oy<=1;oy++) {
                    if (ox != 0 || oy != 0) context.drawText(client.textRenderer, glyph.text, ox, oy, outline, false);
                }
                context.drawText(client.textRenderer,glyph.text,0,0,(a<<24)|glyph.color,false);
                matrices.pop();
            }
            matrices.pop();
        }
    }

    private static boolean place(DamageNumber number, Layout layout, float ax, float ay, int width, int height,
                              List<Rect> occupied, boolean self) {
        float bestX=0,bestY=0; int bestScore=Integer.MAX_VALUE;
        float travel = (float)(Math.sqrt(number.lifetime()/1000.0)*16);
        for (int i=0;i<32;i++) {
            int side = i%2==0 ? 1 : -1;
            int ring = i/8;
            float dx = self ? (i%2==0 ? 0 : side*18) : side*(24+layout.width/2+ring*(layout.width+8));
            float dy = switch (i/2%4) {
                case 0 -> -12;
                case 1 -> -layout.height-20;
                case 2 -> layout.height+9;
                default -> -2*layout.height-28;
            };
            float cx = MathHelper.clamp(ax+dx, layout.width/2+4, Math.max(layout.width/2+4,width-layout.width/2-4));
            float cy = MathHelper.clamp(ay+dy, layout.height/2+travel+4, Math.max(layout.height/2+travel+4,height-layout.height/2-4));
            Rect rect = new Rect(cx-layout.width/2,cy-layout.height/2-travel,layout.width,layout.height+travel);
            int score=0; for (Rect other:occupied) if (rect.overlaps(other)) score++;
            if (score<bestScore) { bestScore=score; bestX=cx-ax; bestY=cy-ay; }
            if (score==0) break;
        }
        // A saturated screen must not turn into overlapping unreadable digits.
        if (bestScore != 0) return false;
        number.offsetX=bestX; number.offsetY=bestY; number.placed=true;
        return true;
    }

    private static Layout layout(MinecraftClient client, DamageNumber number) {
        var glyphs = new ArrayList<Glyph>();
        float y=0,maxWidth=0;
        var entries = number.entries();
        int columns = entries.size() >= 4 ? 3 : 2;
        for (int row=0;row<entries.size();row+=columns) {
            float rowWidth=0,rowHeight=0;
            var rowGlyphs = new ArrayList<Glyph>();
            for (int i=row;i<Math.min(row+columns,entries.size());i++) {
                var entry=entries.get(i);
                Text text=Text.literal((entry.isHeal()?"+":"")+CombatNumberStyle.format(entry.damage()))
                        .styled(style->style.withBold(entry.isCrit()));
                float scale=CombatNumberStyle.scale(entry.damage(),entry.isCrit(),number.kind.periodic());
                rowGlyphs.add(new Glyph(text,color(entry.color(),entry.elementId(),entry.isHeal()),scale,rowWidth,y,entry.isCrit()));
                rowWidth+=(client.textRenderer.getWidth(text)+2)*scale+8;
                rowHeight=Math.max(rowHeight,(client.textRenderer.fontHeight+2)*scale);
            }
            rowWidth-=8;
            for (var g:rowGlyphs) glyphs.add(new Glyph(g.text,g.color,g.scale,g.x-rowWidth/2,g.y,g.critical));
            maxWidth=Math.max(maxWidth,rowWidth); y+=rowHeight+3;
        }
        float height=Math.max(1,y-3);
        var centered=new ArrayList<Glyph>();
        for(var g:glyphs) centered.add(new Glyph(g.text,g.color,g.scale,g.x,g.y-height/2,g.critical));
        return new Layout(number.revision,maxWidth+6,height+6,centered);
    }

    private static int color(int original, int element, boolean healing) {
        if (healing) return 0x8EED9C;
        // Preserve special event colors such as the red physical bleed burst.
        if (element < 0 || element >= DamageElement.values().length || original != DamageElement.values()[element].color) return original & 0xFFFFFF;
        return switch (DamageElement.values()[element]) {
            case PHYSICAL -> 0xF5F5F5; case FIRE -> 0xF5655B; case COLD -> 0x72D5F5;
            case LIGHTNING -> 0x4067D2; case VOID -> 0x9950D8; case POISON -> 0xACDD6E; case DROWN -> 0x75CFE5;
        };
    }
}
