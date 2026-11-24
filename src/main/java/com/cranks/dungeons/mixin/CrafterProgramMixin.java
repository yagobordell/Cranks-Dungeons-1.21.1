package com.cranks.dungeons.mixin;

import com.cranks.dungeons.inventory.ProgrammedCrafter;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import java.util.*;

@Mixin(CrafterBlockEntity.class)
public abstract class CrafterProgramMixin implements ProgrammedCrafter {
    @Shadow @Final protected PropertyDelegate propertyDelegate;
    @Unique private Identifier cranksdungeons$recipe;
    @Unique private List<ItemStack> cranksdungeons$choices=List.of();
    @Unique private int cranksdungeons$revision;
    @Override public Identifier cranksdungeons$recipe(){return cranksdungeons$recipe;}
    @Override public List<ItemStack> cranksdungeons$choices(){return cranksdungeons$choices.stream().map(ItemStack::copy).toList();}
    @Override public int cranksdungeons$revision(){return cranksdungeons$revision;}
    @Override public void cranksdungeons$program(Identifier recipe,List<ItemStack> choices){
        if(choices.size()>9)throw new IllegalArgumentException("Too many Crafter variant inputs");
        cranksdungeons$recipe=recipe;
        cranksdungeons$choices=choices.stream().filter(s->!s.isEmpty()).map(s->s.copyWithCount(1)).toList();
        cranksdungeons$revision++;
        ((CrafterBlockEntity)(Object)this).markDirty();
    }
    @Inject(method="writeNbt",at=@At("TAIL"))
    private void saveProgram(NbtCompound nbt,RegistryWrapper.WrapperLookup registries,CallbackInfo ci){
        if(cranksdungeons$recipe==null){nbt.remove("cranks_dungeons_recipe");nbt.remove("cranks_dungeons_variant");return;}
        nbt.putString("cranks_dungeons_recipe",cranksdungeons$recipe.toString());
        var inputs=new NbtList();for(var choice:cranksdungeons$choices)inputs.add(choice.encode(registries));
        nbt.put("cranks_dungeons_variant",inputs);
    }
    @Inject(method="readNbt",at=@At("TAIL"))
    private void loadProgram(NbtCompound nbt,RegistryWrapper.WrapperLookup registries,CallbackInfo ci){
        cranksdungeons$recipe=Identifier.tryParse(nbt.getString("cranks_dungeons_recipe"));
        var inputs=nbt.getList("cranks_dungeons_variant",NbtElement.COMPOUND_TYPE);
        var choices=new ArrayList<ItemStack>();
        if(inputs.size()<=9)for(int i=0;i<inputs.size();i++)ItemStack.fromNbt(registries,inputs.getCompound(i)).filter(s->!s.isEmpty()).ifPresent(s->choices.add(s.copyWithCount(1)));
        else cranksdungeons$recipe=null;
        if(choices.size()!=inputs.size())cranksdungeons$recipe=null;
        cranksdungeons$choices=List.copyOf(choices);cranksdungeons$revision++;
        // Legacy grid-disabled slots are material storage now; their items stay untouched.
        for(int i=0;i<9;i++)propertyDelegate.set(i,0);
    }
    @Inject(method="isValid",at=@At("HEAD"),cancellable=true)
    private void materialInsertion(int slot,ItemStack stack,CallbackInfoReturnable<Boolean> ci){ci.setReturnValue(slot>=0&&slot<9);}
    @Inject(method="isSlotDisabled",at=@At("HEAD"),cancellable=true)
    private void allSlotsEnabled(int slot,CallbackInfoReturnable<Boolean> ci){ci.setReturnValue(false);}
    @Inject(method="setSlotEnabled",at=@At("HEAD"),cancellable=true)
    private void noGridToggles(int slot,boolean enabled,CallbackInfo ci){ci.cancel();}
}
