package com.cranks.dungeons.screen;

import com.cranks.dungeons.inventory.*;
import com.cranks.dungeons.network.CraftMaterialsResultPayload;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.screen.Property;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import java.util.*;

/** Material buffer plus read-only program data, alongside the complete player loadout. */
public final class LoadoutCrafterHandler extends LoadoutStationHandler {
    public static final int MATERIALS=PLAYER_SLOT_END, MATERIAL_END=MATERIALS+9, TEMPLATE=MATERIAL_END;
    private final Inventory materials;
    private final SimpleInventory template=new SimpleInventory(9);
    private final List<RecipeEntry<CraftingRecipe>> recipes;
    private final Property recipeIndex,revisionLow,revisionHigh,status,powered;
    public LoadoutCrafterHandler(int id,PlayerInventory player){this(id,player,new SimpleInventory(9));}
    public LoadoutCrafterHandler(int id,PlayerInventory player,Inventory materials){
        super(ModScreenHandlers.LOADOUT_CRAFTER,id,player);
        checkSize(materials,9);this.materials=materials;
        recipes=AutomaticCrafting.recipes(player.player.getWorld());
        for(int i=0;i<9;i++)addSlot(new Slot(materials,i,0,0));
        for(int i=0;i<9;i++)addSlot(new Slot(template,i,-10000,-10000){
            @Override public boolean canInsert(ItemStack stack){return false;}
            @Override public boolean canTakeItems(PlayerEntity player){return false;}
            @Override public boolean isEnabled(){return false;}
        });
        recipeIndex=property(0);revisionLow=property(1);revisionHigh=property(2);status=property(3);powered=property(4);
    }
    private Property property(int kind){return addProperty(new Property(){private int value;
        @Override public int get(){
            if(!(materials instanceof CrafterBlockEntity crafter))return value;
            var program=(ProgrammedCrafter)crafter;
            return switch(kind){
                case 0->{int found=-1;for(int i=0;i<recipes.size();i++)if(recipes.get(i).id().equals(program.cranksdungeons$recipe())){found=i;break;}yield found+1;}
                case 1->program.cranksdungeons$revision()&0xffff;
                case 2->program.cranksdungeons$revision()>>>16;
                case 3->{var id=program.cranksdungeons$recipe();var entry=AutomaticCrafting.recipe(crafter.getWorld(),id);yield id==null?0:entry==null?1:AutomaticCrafting.plan(crafter,entry,crafter.getWorld(),program.cranksdungeons$choices())==null?2:3;}
                default->crafter.isTriggered()?1:0;
            };
        }
        @Override public void set(int next){value=next;}
    });}
    public Inventory materials(){return materials;}
    public List<ItemStack> snapshot(){return java.util.stream.IntStream.range(0,9).mapToObj(i->materials.getStack(i).copy()).toList();}
    public int revision(){return revisionLow.get()&0xffff|(revisionHigh.get()&0xffff)<<16;}
    public int status(){return status.get();}
    public boolean powered(){return powered.get()!=0;}
    public RecipeEntry<CraftingRecipe> programmed(){int index=recipeIndex.get()-1;return index>=0&&index<recipes.size()?recipes.get(index):null;}
    public List<ItemStack> choices(){return java.util.stream.IntStream.range(0,9).mapToObj(template::getStack).filter(s->!s.isEmpty()).map(ItemStack::copy).toList();}
    @Override public void sendContentUpdates(){
        if(materials instanceof ProgrammedCrafter program){var choices=program.cranksdungeons$choices();for(int i=0;i<9;i++)template.setStack(i,i<choices.size()?choices.get(i).copy():ItemStack.EMPTY);}
        super.sendContentUpdates();
    }
    @Override public void syncState(){sendContentUpdates();super.syncState();}
    @Override public boolean canUse(PlayerEntity player){return materials.canPlayerUse(player);}
    public int program(ServerPlayerEntity player,int expectedRevision,Identifier id,List<ItemStack> expected){
        if(player.currentScreenHandler!=this||!player.isAlive()||player.isSpectator()||!canUse(player)||!(materials instanceof ProgrammedCrafter program))return CraftMaterialsResultPayload.UNAVAILABLE;
        if(revision()!=expectedRevision||expected.size()!=9)return CraftMaterialsResultPayload.INVENTORY_CHANGED;
        for(int i=0;i<9;i++)if(!ItemStack.areEqual(expected.get(i),materials.getStack(i)))return CraftMaterialsResultPayload.INVENTORY_CHANGED;
        if(id==null){program.cranksdungeons$program(null,List.of());sendContentUpdates();return 1;}
        var entry=AutomaticCrafting.recipe(player.getWorld(),id);
        if(entry==null)return CraftMaterialsResultPayload.UNAVAILABLE;
        if(!MaterialCrafting.allowed(player,entry))return CraftMaterialsResultPayload.LOCKED;
        var choices=MaterialCrafting.usesChoices(entry.value())?AutomaticCrafting.sample(materials):List.<ItemStack>of();
        if(MaterialCrafting.usesChoices(entry.value())&&AutomaticCrafting.plan(materials,entry,player.getWorld(),choices)==null)return CraftMaterialsResultPayload.UNAVAILABLE;
        program.cranksdungeons$program(id,choices);sendContentUpdates();return 1;
    }
    @Override public ItemStack quickMove(PlayerEntity player,int index){
        if(index<5||index>=MATERIAL_END||!canUse(player))return ItemStack.EMPTY;
        var slot=getSlot(index);if(!slot.hasStack()||!slot.canTakeItems(player))return ItemStack.EMPTY;
        var stack=slot.getStack();var before=stack.copy();
        if(index>=MATERIALS)PlayerLoadout.of(player.getInventory()).insert(stack);
        else if(!insertItem(stack,MATERIALS,MATERIAL_END,false))return ItemStack.EMPTY;
        if(stack.getCount()==before.getCount())return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setStack(ItemStack.EMPTY);else slot.markDirty();slot.onTakeItem(player,stack);return before;
    }
}
