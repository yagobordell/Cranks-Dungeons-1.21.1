package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.HorseCargoScrollAccess;
import com.cranks.dungeons.mount.MountHorseCargoSlot;
import com.cranks.dungeons.mount.MountManager;
import com.cranks.dungeons.mount.MountKind;
import com.cranks.dungeons.mount.SlotPositionAccess;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps large donkey/mule cargo inventories inside the vanilla horse UI. */
@Mixin(HorseScreen.class)
public abstract class HorseScreenCargoScrollMixin extends Screen implements HorseCargoScrollAccess {
    private static final Identifier HORSE_TEXTURE = Identifier.ofVanilla("textures/gui/container/horse.png");
    private static final Identifier SLOT = Identifier.ofVanilla("container/slot");
    private static final Identifier SCROLLER = Identifier.ofVanilla("container/villager/scroller");
    private static final int VISIBLE_COLUMNS = 5;
    private static final int VISIBLE_ROWS = 3;

    @Shadow @Final private AbstractHorseEntity entity;
    @Unique private int cranks$cargoScrollRow;
    @Unique private boolean cranks$draggingScrollbar;

    protected HorseScreenCargoScrollMixin() {
        super(null);
    }

    @Override
    public int cranksdungeons$getCargoScrollRow() {
        return cranks$cargoScrollRow;
    }

    @Override
    public int cranksdungeons$getCargoMaxScrollRow() {
        return cranks$maxScrollRow();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void cranks$layoutCargoSlots(DrawContext context, int mouseX, int mouseY,
                                         float delta, CallbackInfo ci) {
        if (MountKind.of(entity) != MountKind.UNSUPPORTED) cranks$layoutSlots();
    }

    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void cranks$drawScrollableBackground(DrawContext context, float delta,
                                                  int mouseX, int mouseY, CallbackInfo ci) {
        var accessor = (com.cranks.dungeons.mixin.HandledScreenAccessor) (Object) this;
        if (MountKind.of(entity) == MountKind.UNSUPPORTED) return;
        int capacity = entity instanceof com.cranks.dungeons.mount.MountDataProvider provider
                && provider.cranksdungeons$hasMountChest()
                ? MountManager.cargoCapacity(entity) : 0;
        int x = accessor.getX();
        int y = accessor.getY();
        int width = accessor.getBackgroundWidth();
        int height = accessor.getBackgroundHeight();
        context.drawTexture(HORSE_TEXTURE, x, y, 0, 0, width, height, 256, 256);
        context.drawGuiTexture(Identifier.ofVanilla("container/horse/saddle_slot"), x + 7, y + 17, 18, 18);
        context.drawGuiTexture(Identifier.ofVanilla("container/horse/armor_slot"),
                x + 7, y + 35, 18, 18);
        if (entity instanceof com.cranks.dungeons.mount.MountDataProvider) {
            // The normal HorseScreen tail injector is skipped when this
            // method is cancelled, so draw the mount-chest equipment slot as
            // part of the replacement background as well.
            context.drawGuiTexture(Identifier.ofVanilla("container/slot"),
                    x + 7, y + 53, 18, 18);
        }
        // Draw only real cargo slots. This avoids presenting empty/disabled
        // slots for a mount without a chest or for the unused tail of a
        // genetic cargo capacity.
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int column = 0; column < VISIBLE_COLUMNS; column++) {
                int cargoIndex = cranks$cargoScrollRow * VISIBLE_COLUMNS
                        + row * VISIBLE_COLUMNS + column;
                if (cargoIndex < capacity) {
                    context.drawGuiTexture(SLOT,
                            x + 79 + column * 18, y + 17 + row * 18, 18, 18);
                }
            }
        }

        // HorseScreen's vanilla background method also draws the live entity
        // preview.  Since the method is cancelled below to replace the cargo
        // grid, draw that preview explicitly as well.
        InventoryScreen.drawEntity(context, x + 26, y + 18, x + 78, y + 70,
                17, 0.25f, mouseX, mouseY, entity);

        int maxScroll = cranks$maxScrollRow();
        if (maxScroll > 0) {
            int trackX = x + 169;
            int trackY = y + 17;
            int trackHeight = VISIBLE_ROWS * 18;
            int totalRows = Math.max(VISIBLE_ROWS,
                    (MountManager.cargoCapacity(entity) + VISIBLE_COLUMNS - 1) / VISIBLE_COLUMNS);
            int thumbHeight = Math.max(10, trackHeight * VISIBLE_ROWS / totalRows);
            int thumbY = trackY + (trackHeight - thumbHeight) * cranks$cargoScrollRow / maxScroll;
            context.drawGuiTexture(SCROLLER, trackX, thumbY, 6, thumbHeight);
        }
        ci.cancel();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double horizontalAmount, double verticalAmount) {
        var accessor = (com.cranks.dungeons.mixin.HandledScreenAccessor) (Object) this;
        int x = accessor.getX();
        int y = accessor.getY();
        if (MountKind.of(entity) == MountKind.UNSUPPORTED) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (mouseX >= x + 79 && mouseX < x + 175
                && mouseY >= y + 17 && mouseY < y + 71
                && cranks$maxScrollRow() > 0 && verticalAmount != 0.0) {
            cranks$cargoScrollRow = Math.max(0, Math.min(cranks$maxScrollRow(),
                    cranks$cargoScrollRow - (verticalAmount > 0 ? 1 : -1)));
            cranks$layoutSlots();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var accessor = (com.cranks.dungeons.mixin.HandledScreenAccessor) (Object) this;
        int x = accessor.getX();
        int y = accessor.getY();
        if (MountKind.of(entity) == MountKind.UNSUPPORTED) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int maxScroll = cranks$maxScrollRow();
        if (button == 0 && maxScroll > 0 && mouseX >= x + 168 && mouseX < x + 177
                && mouseY >= y + 17 && mouseY < y + 71) {
            cranks$draggingScrollbar = true;
            cranks$setScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double deltaX, double deltaY) {
        if (cranks$draggingScrollbar && button == 0) {
            cranks$setScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) cranks$draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Unique
    private int cranks$maxScrollRow() {
        if (!(entity instanceof com.cranks.dungeons.mount.MountDataProvider provider)
                || !provider.cranksdungeons$hasMountChest()) return 0;
        int rows = (MountManager.cargoCapacity(entity) + VISIBLE_COLUMNS - 1) / VISIBLE_COLUMNS;
        return Math.max(0, rows - VISIBLE_ROWS);
    }

    @Unique
    private void cranks$layoutSlots() {
        int max = cranks$maxScrollRow();
        if (cranks$cargoScrollRow > max) cranks$cargoScrollRow = max;
        var handler = ((com.cranks.dungeons.mixin.HandledScreenAccessor) (Object) this).getHandler();
        for (Slot slot : handler.slots) {
            if (!(slot instanceof MountHorseCargoSlot cargo)
                    || !(slot instanceof SlotPositionAccess position)) continue;
            int index = cargo.cranksdungeons$getCargoIndex();
            int row = index / VISIBLE_COLUMNS;
            int column = index % VISIBLE_COLUMNS;
            int capacity = entity instanceof com.cranks.dungeons.mount.MountDataProvider provider
                    && provider.cranksdungeons$hasMountChest()
                    ? MountManager.cargoCapacity(entity) : 0;
            boolean visible = index < capacity && row >= cranks$cargoScrollRow
                    && row < cranks$cargoScrollRow + VISIBLE_ROWS;
            position.cranksdungeons$setPosition(
                    80 + column * 18,
                    visible ? 18 + (row - cranks$cargoScrollRow) * 18 : -1000);
        }
    }

    @Unique
    private void cranks$setScrollFromMouse(double mouseY) {
        int maxScroll = cranks$maxScrollRow();
        if (maxScroll <= 0) return;
        var accessor = (com.cranks.dungeons.mixin.HandledScreenAccessor) (Object) this;
        int trackY = accessor.getY() + 17;
        int trackHeight = VISIBLE_ROWS * 18;
        int totalRows = Math.max(VISIBLE_ROWS,
                (MountManager.cargoCapacity(entity) + VISIBLE_COLUMNS - 1) / VISIBLE_COLUMNS);
        int thumbHeight = Math.max(10, trackHeight * VISIBLE_ROWS / totalRows);
        double travel = Math.max(1, trackHeight - thumbHeight);
        double thumbCenter = mouseY - trackY;
        double fraction = (thumbCenter - thumbHeight / 2.0) / travel;
        fraction = Math.max(0.0, Math.min(1.0, fraction));
        cranks$cargoScrollRow = (int) Math.round(fraction * maxScroll);
        cranks$layoutSlots();
    }
}
