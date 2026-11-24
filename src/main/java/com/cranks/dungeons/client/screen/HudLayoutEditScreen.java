package com.cranks.dungeons.client.screen;

import com.cranks.dungeons.CranksDungeons;
import com.cranks.dungeons.client.party.PartyHudRenderer;
import com.cranks.dungeons.client.HorseStaminaHudRenderer;
import com.cranks.dungeons.client.MountHealthHudRenderer;
import com.cranks.dungeons.client.render.HotbarHudRenderer;
import com.cranks.dungeons.client.render.StatBarHudRenderer;
import com.cranks.dungeons.client.render.StatusEffectHudRenderer;
import com.cranks.dungeons.config.StatusEffectOrientation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import com.cranks.dungeons.client.compendium.CompendiumButtonWidget;
import com.cranks.dungeons.client.compendium.CompendiumStyle;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class HudLayoutEditScreen extends Screen {

    private final Screen parent;
    private final List<DraggableElement> elements = new ArrayList<>();
    private DraggableElement draggingElement = null;
    private DraggableElement selectedElement;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean snapToGrid = true;
    private boolean controlsVisible = true;
    private int layoutWidth,layoutHeight;

    public static class DraggableElement {
        public final String id;
        public final Text displayName;
        public int x;
        public int y;
        public int width;
        public int height;
        public final int defaultX;
        public final int defaultY;
        public boolean isCustom;
        public final Consumer<DrawContext> previewRenderer;
        public final BiConsumer<Integer, Integer> positionSaver;
        public final Runnable positionResetter;

        public DraggableElement(String id, Text displayName, int x, int y, int width, int height,
                                int defaultX, int defaultY, boolean isCustom,
                                Consumer<DrawContext> previewRenderer,
                                BiConsumer<Integer, Integer> positionSaver,
                                Runnable positionResetter) {
            this.id = id;
            this.displayName = displayName;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.defaultX = defaultX;
            this.defaultY = defaultY;
            this.isCustom = isCustom;
            this.previewRenderer = previewRenderer;
            this.positionSaver = positionSaver;
            this.positionResetter = positionResetter;
        }

        public boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        public void reset() {
            this.x = defaultX;
            this.y = defaultY;
            this.isCustom = false;
            this.positionResetter.run();
        }
    }

    public HudLayoutEditScreen(Screen parent) {
        super(Text.literal("HUD Layout Editor"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Prevent default 1.21 screen Gaussian blur so HUD layout and game world remain crystal clear
    }

    @Override
    protected void init() {
        String selectedId=selectedElement==null?null:selectedElement.id;
        if (!elements.isEmpty()) saveChanges();
        clearChildren();
        initDraggableElements();
        selectedElement=elements.stream().filter(element->element.id.equals(selectedId)).findFirst().orElse(null);
        draggingElement=null;
        layoutWidth=width;layoutHeight=height;
        for(var element:elements)fit(element);
        int start=(width-292)/2, toolbarY=10;
        addDrawableChild(CompendiumButtonWidget.builder(Text.literal("Settings"),b->{saveChanges();client.setScreen(new CranksConfigScreen(parent));}).dimensions(start,toolbarY,62,20).build());
        addDrawableChild(CompendiumButtonWidget.builder(Text.literal("HUD Layout"),b->{}).selected(true).dimensions(start+66,toolbarY,70,20).build());
        addDrawableChild(CompendiumButtonWidget.builder(Text.literal("Snap: "+(snapToGrid?"On":"Off")),b->{snapToGrid=!snapToGrid;b.setMessage(Text.literal("Snap: "+(snapToGrid?"On":"Off")));}).dimensions(start+140,toolbarY,58,20).build());
        addDrawableChild(CompendiumButtonWidget.builder(Text.literal("Reset"),b->resetAllPositions()).dimensions(start+202,toolbarY,44,20).build());
        addDrawableChild(CompendiumButtonWidget.builder(Text.literal("Done"),b->close()).dimensions(start+250,toolbarY,42,20).build());
        for(var child:children())if(child instanceof net.minecraft.client.gui.widget.ClickableWidget widget)widget.visible=controlsVisible;
    }

    private void fit(DraggableElement element){
        int fittedX=Math.clamp(element.x,0,Math.max(0,width-element.width));
        int fittedY=Math.clamp(element.y,0,Math.max(0,height-element.height));
        if(fittedX!=element.x||fittedY!=element.y)element.isCustom=true;
        element.x=fittedX;element.y=fittedY;
    }

    private void initDraggableElements() {
        elements.clear();
        var config = CranksDungeons.CONFIG;
        MinecraftClient mc = MinecraftClient.getInstance();
        StatBarHudRenderer.ensurePositionReference(this.width, this.height);
        int referenceWidth = config.statBarLayoutReferenceWidth();
        int referenceHeight = config.statBarLayoutReferenceHeight();

        // 1. Party HUD
        var partyLayout = com.cranks.dungeons.client.party.PartyHudLayout.fit(width, height,
                config.partyHudX(), config.partyHudY(), config.partyHudMaxRows(), config.partyHudMaxColumns(),
                config.partyHudMaxRows() * config.partyHudMaxColumns(), config.partyHudSize());
        int partyMaxRows = partyLayout.rows(), partyMaxCols = partyLayout.columns();
        float partyScale = partyLayout.scale();
        int partyW = partyLayout.width(), partyH = partyLayout.height();
        int defaultPartyX = 5, defaultPartyY = 190;
        int partyX = partyLayout.x(), partyY = partyLayout.y();
        elements.add(new DraggableElement(
                "party_hud",
                Text.literal("Party HUD (Max " + (partyMaxRows * partyMaxCols) + ")"),
                partyX, partyY, partyW, partyH,
                defaultPartyX, defaultPartyY,
                config.partyHudX() >= 0 || config.partyHudY() >= 0,
                ctx -> PartyHudRenderer.renderPreview(ctx, mc, 0, 0, partyMaxRows, partyMaxCols, partyScale),
                (nx, ny) -> {
                    config.partyHudX(nx);
                    config.partyHudY(ny);
                },
                () -> {
                    config.partyHudX(-1);
                    config.partyHudY(-1);
                }
        ));

        // 2. Status Effects HUD
        StatusEffectOrientation statusOrientation = config.statusEffectsOrientation();
        float statusScale = config.statusEffectsScale();
        int statusMaxPerLine = config.statusEffectsMaxPerLine();
        int rawSpacing = config.statusEffectsSpacing();
        boolean statusDuration = config.statusEffectsShowDuration();
        var statusBounds = StatusEffectHudRenderer.previewBounds(
                statusOrientation, statusMaxPerLine, statusScale, rawSpacing, statusDuration);

        int defaultStatusX = statusOrientation == StatusEffectOrientation.LEFT_TO_RIGHT
                ? 5 : this.width - statusBounds.width() - 5;
        int defaultStatusY = statusOrientation == StatusEffectOrientation.BOTTOM_TO_TOP
                ? this.height - 60 - statusBounds.anchorY() : 1;
        int statusX = config.statusEffectsX() >= 0 ? config.statusEffectsX() - statusBounds.anchorX() : defaultStatusX;
        int statusY = config.statusEffectsY() >= 0 ? config.statusEffectsY() - statusBounds.anchorY() : defaultStatusY;

        String directionName = switch (statusOrientation) {
            case LEFT_TO_RIGHT -> "Left to Right";
            case RIGHT_TO_LEFT -> "Right to Left";
            case TOP_TO_BOTTOM -> "Top to Bottom";
            case BOTTOM_TO_TOP -> "Bottom to Top";
        };

        elements.add(new DraggableElement(
                "status_effects",
                Text.literal("Status Effects (" + directionName + ")"),
                statusX, statusY, statusBounds.width(), statusBounds.height(),
                defaultStatusX, defaultStatusY,
                config.statusEffectsX() >= 0 || config.statusEffectsY() >= 0,
                ctx -> StatusEffectHudRenderer.renderPreview(ctx, mc, 0, 0, statusOrientation, statusMaxPerLine, statusScale, rawSpacing, statusDuration),
                (nx, ny) -> {
                    config.statusEffectsX(nx + statusBounds.anchorX());
                    config.statusEffectsY(ny + statusBounds.anchorY());
                },
                () -> {
                    config.statusEffectsX(-1);
                    config.statusEffectsY(-1);
                }
        ));

        // 3. Experience Bar
        int defaultExperienceX = this.width / 2 - StatBarHudRenderer.EXPERIENCE_BAR_WIDTH / 2;
        int defaultExperienceY = this.height - 29 - HotbarHudRenderer.DEFAULT_LIFT;
        int experienceX = config.experienceBarX() >= 0
                ? StatBarHudRenderer.resolveConfiguredX(config.experienceBarX(), this.width,
                        StatBarHudRenderer.EXPERIENCE_BAR_WIDTH,
                        referenceWidth / 2 - StatBarHudRenderer.EXPERIENCE_BAR_WIDTH / 2, defaultExperienceX)
                : defaultExperienceX;
        int experienceY = config.experienceBarY() >= 0
                ? StatBarHudRenderer.resolveConfiguredY(config.experienceBarY(), this.height,
                        StatBarHudRenderer.EXPERIENCE_BAR_HEIGHT, referenceHeight - 29 - HotbarHudRenderer.DEFAULT_LIFT, defaultExperienceY)
                : defaultExperienceY;
        elements.add(new DraggableElement(
                "experience_bar",
                Text.literal("Experience Bar"),
                experienceX, experienceY,
                StatBarHudRenderer.EXPERIENCE_BAR_WIDTH, StatBarHudRenderer.EXPERIENCE_BAR_HEIGHT,
                defaultExperienceX, defaultExperienceY,
                config.experienceBarX() >= 0 || config.experienceBarY() >= 0,
                ctx -> StatBarHudRenderer.renderExperienceBarDirect(ctx, mc, 0, 0, 25, 50, 0.5f),
                (nx, ny) -> {
                    config.experienceBarX(nx);
                    config.experienceBarY(ny);
                },
                () -> {
                    config.experienceBarX(-1);
                    config.experienceBarY(-1);
                }
        ));

        // 4. Health Bar
        int defaultHealthX = this.width / 2 - 91;
        int defaultHealthY = this.height - 39 - HotbarHudRenderer.DEFAULT_LIFT;
        int healthX = config.healthBarX() >= 0
                ? StatBarHudRenderer.resolveConfiguredX(config.healthBarX(), this.width,
                        StatBarHudRenderer.BAR_WIDTH, referenceWidth / 2 - 91, defaultHealthX)
                : defaultHealthX;
        int healthY = config.healthBarY() >= 0
                ? StatBarHudRenderer.resolveConfiguredY(config.healthBarY(), this.height,
                        StatBarHudRenderer.BAR_HEIGHT, referenceHeight - 39 - HotbarHudRenderer.DEFAULT_LIFT, defaultHealthY)
                : defaultHealthY;
        elements.add(new DraggableElement(
                "health_bar",
                Text.literal("Health Bar"),
                healthX, healthY, StatBarHudRenderer.BAR_WIDTH, StatBarHudRenderer.BAR_HEIGHT,
                defaultHealthX, defaultHealthY,
                config.healthBarX() >= 0 || config.healthBarY() >= 0,
                ctx -> StatBarHudRenderer.renderHealthBarDirect(ctx, mc, null, 0, 0, 100, 100, 0, 100),
                (nx, ny) -> {
                    config.healthBarX(nx);
                    config.healthBarY(ny);
                },
                () -> {
                    config.healthBarX(-1);
                    config.healthBarY(-1);
                }
        ));

        // 4. Shield Bar
        int defaultShieldX = healthX;
        int defaultShieldY = healthY - 7;
        int referenceShieldX = config.healthBarX() >= 0 ? config.healthBarX() : referenceWidth / 2 - 91;
        int referenceShieldY = (config.healthBarY() >= 0 ? config.healthBarY() : referenceHeight - 39 - HotbarHudRenderer.DEFAULT_LIFT) - 7;
        int shieldX = config.shieldBarX() >= 0
                ? StatBarHudRenderer.resolveConfiguredX(config.shieldBarX(), this.width,
                        StatBarHudRenderer.BAR_WIDTH, referenceShieldX, defaultShieldX)
                : defaultShieldX;
        int shieldY = config.shieldBarY() >= 0
                ? StatBarHudRenderer.resolveConfiguredY(config.shieldBarY(), this.height,
                        StatBarHudRenderer.SHIELD_HEIGHT, referenceShieldY, defaultShieldY)
                : defaultShieldY;
        elements.add(new DraggableElement(
                "shield_bar",
                Text.literal("Energy Shield Bar"),
                shieldX, shieldY, StatBarHudRenderer.BAR_WIDTH, 6,
                defaultShieldX, defaultShieldY,
                config.shieldBarX() >= 0 || config.shieldBarY() >= 0,
                ctx -> StatBarHudRenderer.renderShieldBarDirect(ctx, mc, 0, 0, 50, 50, 50),
                (nx, ny) -> {
                    config.shieldBarX(nx);
                    config.shieldBarY(ny);
                },
                () -> {
                    config.shieldBarX(-1);
                    config.shieldBarY(-1);
                }
        ));

        // 5. Horse Stamina Bar
        int defaultHorseStaminaX = this.width / 2 - HorseStaminaHudRenderer.WIDTH / 2;
        int defaultHorseStaminaY = HorseStaminaHudRenderer.defaultY(this.height);
        int horseStaminaX = config.horseStaminaBarX() >= 0
                ? config.horseStaminaBarX() : defaultHorseStaminaX;
        int horseStaminaY = config.horseStaminaBarY() >= 0
                ? config.horseStaminaBarY() : defaultHorseStaminaY;
        elements.add(new DraggableElement(
                "horse_stamina_bar",
                Text.literal("Horse Stamina Bar"),
                horseStaminaX, horseStaminaY,
                HorseStaminaHudRenderer.WIDTH, HorseStaminaHudRenderer.HEIGHT,
                defaultHorseStaminaX, defaultHorseStaminaY,
                config.horseStaminaBarX() >= 0 || config.horseStaminaBarY() >= 0,
                ctx -> HorseStaminaHudRenderer.renderPreview(ctx, mc, 0, 0),
                (nx, ny) -> {
                    config.horseStaminaBarX(nx);
                    config.horseStaminaBarY(ny);
                },
                () -> {
                    config.horseStaminaBarX(-1);
                    config.horseStaminaBarY(-1);
                }
        ));

        // 6. Mounted Health Bar
        int defaultMountHealthX = horseStaminaX;
        int defaultMountHealthY = horseStaminaY - MountHealthHudRenderer.HEIGHT - 1;
        int mountHealthX = config.mountHealthBarX() >= 0
                ? config.mountHealthBarX() : defaultMountHealthX;
        int mountHealthY = config.mountHealthBarY() >= 0
                ? config.mountHealthBarY() : defaultMountHealthY;
        elements.add(new DraggableElement(
                "mount_health_bar",
                Text.literal("Mount Health Bar"),
                mountHealthX, mountHealthY,
                MountHealthHudRenderer.WIDTH, MountHealthHudRenderer.HEIGHT,
                defaultMountHealthX, defaultMountHealthY,
                config.mountHealthBarX() >= 0 || config.mountHealthBarY() >= 0,
                ctx -> MountHealthHudRenderer.renderPreview(ctx, mc, 0, 0),
                (nx, ny) -> {
                    config.mountHealthBarX(nx);
                    config.mountHealthBarY(ny);
                },
                () -> {
                    config.mountHealthBarX(-1);
                    config.mountHealthBarY(-1);
                }
        ));

        // 7. Mana Bar
        int defaultHungerX = this.width / 2 + 10;
        int defaultHungerY = this.height - 39 - HotbarHudRenderer.DEFAULT_LIFT;
        int referenceHungerX = referenceWidth / 2 + 10;
        int referenceHungerY = referenceHeight - 39 - HotbarHudRenderer.DEFAULT_LIFT;
        int hungerX = config.hungerBarX() >= 0
                ? StatBarHudRenderer.resolveConfiguredX(config.hungerBarX(), this.width,
                        StatBarHudRenderer.BAR_WIDTH, referenceHungerX, defaultHungerX)
                : defaultHungerX;
        int hungerY = config.hungerBarY() >= 0
                ? StatBarHudRenderer.resolveConfiguredY(config.hungerBarY(), this.height,
                        StatBarHudRenderer.BAR_HEIGHT, referenceHungerY, defaultHungerY)
                : defaultHungerY;
        int defaultManaX = hungerX;
        int defaultManaY = hungerY - 10;
        int referenceManaX = config.hungerBarX() >= 0 ? config.hungerBarX() : referenceHungerX;
        int referenceManaY = (config.hungerBarY() >= 0 ? config.hungerBarY() : referenceHungerY) - 10;
        int manaX = config.manaBarX() >= 0
                ? StatBarHudRenderer.resolveConfiguredX(config.manaBarX(), this.width,
                        StatBarHudRenderer.BAR_WIDTH, referenceManaX, defaultManaX)
                : defaultManaX;
        int manaY = config.manaBarY() >= 0
                ? StatBarHudRenderer.resolveConfiguredY(config.manaBarY(), this.height,
                        StatBarHudRenderer.BAR_HEIGHT, referenceManaY, defaultManaY)
                : defaultManaY;
        elements.add(new DraggableElement(
                "mana_bar",
                Text.literal("Mana Bar"),
                manaX, manaY, StatBarHudRenderer.BAR_WIDTH, StatBarHudRenderer.BAR_HEIGHT,
                defaultManaX, defaultManaY,
                config.manaBarX() >= 0 || config.manaBarY() >= 0,
                ctx -> StatBarHudRenderer.renderManaBarDirect(ctx, mc, 0, 0, 100, 100, 100),
                (nx, ny) -> {
                    config.manaBarX(nx);
                    config.manaBarY(ny);
                },
                () -> {
                    config.manaBarX(-1);
                    config.manaBarY(-1);
                }
        ));

        // 8. Hunger Bar
        elements.add(new DraggableElement(
                "hunger_bar",
                Text.literal("Hunger Bar"),
                hungerX, hungerY, StatBarHudRenderer.BAR_WIDTH, StatBarHudRenderer.BAR_HEIGHT,
                defaultHungerX, defaultHungerY,
                config.hungerBarX() >= 0 || config.hungerBarY() >= 0,
                ctx -> StatBarHudRenderer.renderHungerBarDirect(ctx, mc, 0, 0, 20),
                (nx, ny) -> {
                    config.hungerBarX(nx);
                    config.hungerBarY(ny);
                },
                () -> {
                    config.hungerBarX(-1);
                    config.hungerBarY(-1);
                }
        ));

        elements.add(new DraggableElement(
                "hotbar",
                Text.translatable("hud.cranks-dungeons.hotbar"),
                HotbarHudRenderer.resolveX(width), HotbarHudRenderer.resolveY(height),
                HotbarHudRenderer.WIDTH, HotbarHudRenderer.HEIGHT,
                HotbarHudRenderer.defaultX(width), HotbarHudRenderer.defaultY(height),
                config.hotbarX() >= 0 || config.hotbarY() >= 0,
                ctx -> HotbarHudRenderer.renderPreview(ctx, mc),
                (nx, ny) -> {
                    config.hotbarX(nx);
                    config.hotbarY(ny);
                },
                () -> {
                    config.hotbarX(-1);
                    config.hotbarY(-1);
                }
        ));
    }

    private void resetAllPositions() {
        for (DraggableElement elem : elements) {
            elem.reset();
            fit(elem);
        }
        if (client != null && client.player != null) {
            client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
        }
        saveChanges();
    }

    private void saveChanges() {
        for (DraggableElement elem : elements) {
            if (elem.isCustom) {
                elem.positionSaver.accept(elem.x, elem.y);
            } else {
                elem.positionResetter.run();
            }
        }
        CranksDungeons.CONFIG.statBarLayoutReferenceWidth(layoutWidth>0?layoutWidth:width);
        CranksDungeons.CONFIG.statBarLayoutReferenceHeight(layoutHeight>0?layoutHeight:height);
        CranksDungeons.CONFIG.save();
    }

    @Override
    public void close() {
        saveChanges();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Iterate elements in reverse (topmost first)
        for (int i = elements.size() - 1; i >= 0; i--) {
            DraggableElement elem = elements.get(i);
            if (elem.isHovered(mx, my)) {
                if (button == 0) {
                    // Left click: Start dragging
                    selectedElement=elem;
                    setFocused(null);
                    draggingElement = elem;
                    dragOffsetX = mx - elem.x;
                    dragOffsetY = my - elem.y;
                    return true;
                } else if (button == 1) {
                    // Right click: Reset element position
                    selectedElement=elem;
                    elem.reset();
                    fit(elem);
                    if (client != null && client.player != null) {
                        client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.2f);
                    }
                    return true;
                }
            }
        }

        selectedElement=null;
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingElement != null && button == 0) {
            int newX = (int) mouseX - dragOffsetX;
            int newY = (int) mouseY - dragOffsetY;

            if (snapToGrid) {
                // Snap to 5px grid
                newX = Math.round(newX / 5.0f) * 5;
                newY = Math.round(newY / 5.0f) * 5;

                // Center snapping
                int centerX = (this.width - draggingElement.width) / 2;
                if (Math.abs(newX - centerX) <= 8) {
                    newX = centerX;
                }

                // Edge snapping
                if (Math.abs(newX - 0) <= 6) newX = 0;
                if (Math.abs(newX - 5) <= 6) newX = 5;
                if (Math.abs(newX - (this.width - draggingElement.width)) <= 6) {
                    newX = this.width - draggingElement.width;
                }
                if (Math.abs(newX - (this.width - draggingElement.width - 5)) <= 6) {
                    newX = this.width - draggingElement.width - 5;
                }

                if (Math.abs(newY - 0) <= 6) newY = 0;
                if (Math.abs(newY - 1) <= 6) newY = 1;
                if (Math.abs(newY - 5) <= 6) newY = 5;
                if (Math.abs(newY - (this.height - draggingElement.height)) <= 6) {
                    newY = this.height - draggingElement.height;
                }
                if (Math.abs(newY - (this.height - draggingElement.height - 5)) <= 6) {
                    newY = this.height - draggingElement.height - 5;
                }
            }

            // Clamping: allow all the way to 0
            newX = Math.max(0, Math.min(this.width - draggingElement.width, newX));
            newY = Math.max(0, Math.min(this.height - draggingElement.height, newY));

            draggingElement.x = newX;
            draggingElement.y = newY;
            draggingElement.isCustom = true;
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingElement != null && button == 0) {
            draggingElement = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int key,int scan,int modifiers){
        if(key==72){controlsVisible=!controlsVisible;setFocused(null);for(var child:children())if(child instanceof net.minecraft.client.gui.widget.ClickableWidget widget)widget.visible=controlsVisible;return true;}
        if(selectedElement!=null&&(key==262||key==263||key==264||key==265)){
            int step=(modifiers&org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT)!=0?5:1;
            selectedElement.x+=key==262?step:key==263?-step:0;
            selectedElement.y+=key==264?step:key==265?-step:0;
            selectedElement.isCustom=true;
            fit(selectedElement);
            draggingElement=null;
            return true;
        }
        return super.keyPressed(key,scan,modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0,0,width,height,0x55000000);
        context.fill(width/2,42,width/2+1,height-12,0x25808080);
        context.fill(0,height/2,width,height/2+1,0x25808080);
        DraggableElement hovered=null;
        for(var element:elements){
            boolean over=element.isHovered(mouseX,mouseY),moving=element==draggingElement;
            if(over)hovered=element;
            context.getMatrices().push();context.getMatrices().translate(element.x,element.y,0);
            element.previewRenderer.accept(context);context.getMatrices().pop();
            int color=moving||element==selectedElement?CompendiumStyle.TEXT:over?CompendiumStyle.TEXT_MUTED:0x55696969;
            context.drawBorder(element.x-2,element.y-2,element.width+4,element.height+4,0xFF000000);
            context.drawBorder(element.x-1,element.y-1,element.width+2,element.height+2,color);
        }
        var focused=draggingElement!=null?draggingElement:selectedElement!=null?selectedElement:hovered;
        if(focused!=null){
            String text=focused.displayName.getString()+"  ·  "+focused.x+", "+focused.y;
            text=textRenderer.trimToWidth(text,width-24);int tagWidth=textRenderer.getWidth(text)+12;
            int tagX=Math.clamp(focused.x,6,Math.max(6,width-tagWidth-6));
            int tagY=focused.y>=60?focused.y-17:Math.min(height-22,focused.y+focused.height+4);
            CompendiumStyle.drawInsetPanel(context,tagX,tagY,tagWidth,15);
            context.drawText(textRenderer,text,tagX+6,tagY+4,CompendiumStyle.TEXT,false);
        }
        if(controlsVisible){
            CompendiumStyle.drawPanel(context,(width-304)/2,5,304,31);
            String help=draggingElement==null?(selectedElement==null?"Drag to move · Right-click resets · H hides tools":"Arrows: 1px · Shift: 5px · Right-click resets · H hides tools"):"Position: "+draggingElement.x+", "+draggingElement.y;
            help=textRenderer.trimToWidth(help,width-30);int helpWidth=textRenderer.getWidth(help)+16;
            CompendiumStyle.drawInsetPanel(context,(width-helpWidth)/2,42,helpWidth,17);
            context.drawCenteredTextWithShadow(textRenderer,help,width/2,47,CompendiumStyle.TEXT_MUTED);
        }else{
            context.drawText(textRenderer,"H: Show tools",6,height-13,CompendiumStyle.TEXT_MUTED,true);
        }
        super.render(context,mouseX,mouseY,delta);
    }
}
