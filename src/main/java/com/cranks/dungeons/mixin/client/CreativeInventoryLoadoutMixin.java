package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.PlayerLoadoutScreen;
import com.cranks.dungeons.client.InventoryStyle;
import com.cranks.dungeons.client.compendium.CompendiumStyle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import com.cranks.dungeons.client.compendium.CompendiumButtonWidget;
import com.cranks.dungeons.inventory.PlayerLoadout;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryScreen.class)
abstract class CreativeInventoryLoadoutMixin extends HandledScreen<ScreenHandler> {
    @Inject(method="onHotbarKeyPress",at=@At("HEAD"),cancellable=true)
    private static void useTenSlotPresets(net.minecraft.client.MinecraftClient client,int preset,boolean load,boolean save,CallbackInfo ci){
        com.cranks.dungeons.client.CreativeHotbarPresets.shortcut(client,preset,load,save);
        ci.cancel();
    }
    @Shadow private static ItemGroup selectedTab;
    @Shadow private TextFieldWidget searchBox;
    @Shadow private Slot deleteItemSlot;
    @Shadow private float scrollPosition;
    protected CreativeInventoryLoadoutMixin(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addLoadoutButton(CallbackInfo ci) {
        searchBox.setEditableColor(CompendiumStyle.TEXT);
        searchBox.setUneditableColor(CompendiumStyle.TEXT_DISABLED);
        int buttonX=x+backgroundWidth+9;
        int buttonY=y+7;
        if(buttonX+100>width-6){buttonX=x+(backgroundWidth-100)/2;buttonY=Math.min(height-21,y+backgroundHeight+36);}
        addDrawableChild(CompendiumButtonWidget.builder(Text.translatable("inventory.cranks-dungeons.title"), button ->
                client.setScreen(new PlayerLoadoutScreen(client.player)))
                .dimensions(buttonX, buttonY, 100, 18).build());
    }

    // Replace native surfaces at their draw calls so Fabric's category paging and other
    // creative interactions keep their original event paths and slot coordinates.
    @Redirect(method="drawBackground",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"))
    private void darkCreativePanel(DrawContext context,Identifier texture,int px,int py,int u,int v,int panelWidth,int panelHeight){
        InventoryStyle.drawPanel(context,px,py,panelWidth,panelHeight,false,InventoryStyle.Panel.STORAGE);
        for(var slot:handler.slots){
            if(!slot.isEnabled()||slot.x<0||slot.y<0||slot.x+16>panelWidth||slot.y+16>panelHeight)continue;
            InventoryStyle.drawCell(context,px+slot.x-1,py+slot.y-1,true);
        }
        if(searchBox.isVisible())CompendiumStyle.drawTextFieldFrame(context,searchBox.getX()-4,searchBox.getY()-3,searchBox.getWidth()+8,15,searchBox.isFocused());
        if(selectedTab.hasScrollbar()){
            CompendiumStyle.drawInsetPanel(context,px+174,py+17,14,114);
        }
        if(deleteItemSlot!=null&&selectedTab.getType()==ItemGroup.Type.INVENTORY)
            CompendiumStyle.drawJournalIcon(context,14,px+deleteItemSlot.x,py+deleteItemSlot.y);
    }

    @Redirect(method="drawBackground",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void darkCreativeScrollbar(DrawContext context,Identifier texture,int px,int py,int scrollbarWidth,int scrollbarHeight){
        boolean enabled=!texture.getPath().contains("disabled");
        context.fill(px+2,py+1,px+scrollbarWidth-2,py+scrollbarHeight-1,enabled?0xFF808080:0xFF383838);
        context.fill(px+4,py+6,px+scrollbarWidth-4,py+7,0xFF191919);
        context.fill(px+4,py+9,px+scrollbarWidth-4,py+10,0xFF191919);
    }

    @Redirect(method="renderTabIcon",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void darkCreativeTab(DrawContext context,Identifier texture,int px,int py,int tabWidth,int tabHeight,DrawContext originalContext,ItemGroup group){
        CompendiumStyle.drawRailPlaque(context,px,py,tabWidth,tabHeight,group==selectedTab,false);
    }

    @ModifyArg(method="drawForeground",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I"),index=4)
    private int creativeTitleColor(int color){return CompendiumStyle.TEXT;}

    @ModifyExpressionValue(method = "setSelectedTab", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/collection/DefaultedList;size()I", ordinal = 0))
    private int keepVanillaCatalogSlotLayout(int slots) {
        return Math.min(slots, PlayerLoadout.FIRST_HANDLER_SLOT);
    }
}
