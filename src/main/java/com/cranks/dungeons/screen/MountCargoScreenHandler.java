package com.cranks.dungeons.screen;

import com.cranks.dungeons.inventory.PlayerLoadout;
import com.cranks.dungeons.mount.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;

/** Shared loadout plus native horse equipment or the existing custom mount/llama-pack inventory. */
public final class MountCargoScreenHandler extends LoadoutStationHandler {
    public record OpeningData(int entityId,int capacity,int packSize,int activeCapacity,boolean horseLayout,boolean carpet) {
        public OpeningData(int entityId){this(entityId,0,0,0,false,false);}
        public OpeningData(int entityId,int capacity){this(entityId,capacity,0,capacity,false,false);}
        public OpeningData(int entityId,int capacity,int packSize){this(entityId,capacity,packSize,capacity,false,false);}
        public OpeningData(int entityId,int capacity,int packSize,int activeCapacity){this(entityId,capacity,packSize,activeCapacity,false,false);}
        public static final PacketCodec<RegistryByteBuf,OpeningData> PACKET_CODEC=PacketCodec.tuple(
                PacketCodecs.VAR_INT,OpeningData::entityId,PacketCodecs.VAR_INT,OpeningData::capacity,
                PacketCodecs.VAR_INT,OpeningData::packSize,PacketCodecs.VAR_INT,OpeningData::activeCapacity,
                PacketCodecs.BOOL,OpeningData::horseLayout,PacketCodecs.BOOL,OpeningData::carpet,OpeningData::new);
    }
    private final LivingEntity mount;
    private final PlayerInventory playerInventory;
    private final int capacity,cargoStartIndex,chestSlotIndex,bodySlotIndex,packSize;
    private final LlamaPackInventory llamaPackInventory;
    private final HorseScreenHandler nativeHorse;
    private final boolean horseLayout;
    private int syncedActiveCapacity;
    private int syncedAttachmentStart,syncedAttachmentCount;
    public MountCargoScreenHandler(int id,PlayerInventory player,OpeningData data){this(id,player,findMount(player.player,data.entityId()),data.capacity(),data.packSize(),data.activeCapacity(),data.horseLayout(),data.carpet());}
    public MountCargoScreenHandler(int id,PlayerInventory player,LivingEntity mount){this(id,player,mount,MountManager.cargoScreenCapacity(mount),mount instanceof LlamaEntity llama?LlamaPackManager.members(llama).size():0,MountManager.cargoCapacity(mount),nativeHorseLayout(mount),mount instanceof LlamaEntity);}
    public static boolean nativeHorseLayout(LivingEntity mount){return mount instanceof AbstractHorseEntity&&!MountManager.usesCustomCargoScreen(mount);}
    private MountCargoScreenHandler(int id,PlayerInventory player,LivingEntity mount,int sentCapacity,int sentPack,int sentActive,boolean horseLayout,boolean carpet){
        super(ModScreenHandlers.MOUNT_CARGO,id,player);
        this.mount=mount;this.playerInventory=player;this.horseLayout=horseLayout;
        boolean client=player.player.getWorld().isClient();
        capacity=Math.max(0,sentCapacity);
        syncedActiveCapacity=Math.clamp(sentActive,0,capacity);
        if(horseLayout&&mount instanceof AbstractHorseEntity horse){
            var inventory=client?new SimpleInventory(1+capacity):AbstractHorseInventoryAccess.of(horse).cranksdungeons$getHorseItems();
            nativeHorse=new HorseScreenHandler(id,player,inventory,horse,capacity/3);
            llamaPackInventory=null;packSize=0;
            addSlot(new MountEquipmentSlot(nativeHorse.getSlot(0),mount,player.player));addSlot(new MountEquipmentSlot(nativeHorse.getSlot(1),mount,player.player));
            bodySlotIndex=PLAYER_SLOT_END+1;chestSlotIndex=PLAYER_SLOT_END+2;cargoStartIndex=PLAYER_SLOT_END+3;
            addSlot(new MountEquipmentSlot(new MountChestSlot(mount,0,0),mount,player.player));
            for(int i=0;i<capacity;i++)addSlot(nativeHorse.getSlot(2+i));
        } else {
            nativeHorse=null;
            Inventory cargo;
            if(mount instanceof LlamaEntity llama){llamaPackInventory=client?LlamaPackInventory.clientView(capacity):LlamaPackInventory.forPack(llama,capacity);packSize=sentPack;cargo=llamaPackInventory;}
            else {llamaPackInventory=null;packSize=0;cargo=mount==null?new SimpleInventory(capacity):MountManager.data(mount).cargo;}
            boolean body=carpet||horseLayout;
            bodySlotIndex=body?PLAYER_SLOT_END+1:-1;chestSlotIndex=PLAYER_SLOT_END+(body?2:1);cargoStartIndex=chestSlotIndex+1;
            addSlot(mount==null?new Slot(new SimpleInventory(1),0,0,0):new MountEquipmentSlot(new MountSaddleSlot(mount,0,0),mount,player.player));
            if(body)addSlot(mount instanceof LlamaEntity llama?new MountEquipmentSlot(new MountLlamaCarpetSlot(llama,0,0),mount,player.player):new Slot(new SimpleInventory(1),0,0,0));
            addSlot(mount==null?new Slot(new SimpleInventory(1),0,0,0):new MountEquipmentSlot(new MountChestSlot(mount,0,0),mount,player.player));
            for(int i=0;i<capacity;i++)addSlot(mount==null?new Slot(cargo,i,0,0):new MountStandaloneCargoSlot(cargo,i,0,0,mount,this::activeCapacity));
        }
        addProperty(new Property(){@Override public int get(){return client?syncedActiveCapacity:serverActiveCapacity();}@Override public void set(int value){syncedActiveCapacity=Math.clamp(value,0,capacity);}});
        addProperty(new Property(){@Override public int get(){return client?syncedAttachmentStart:attachmentStart();}@Override public void set(int value){syncedAttachmentStart=Math.clamp(value,0,capacity);}});
        addProperty(new Property(){@Override public int get(){return client?syncedAttachmentCount:attachmentCount();}@Override public void set(int value){syncedAttachmentCount=Math.clamp(value,0,capacity);}});
    }
    public LivingEntity mount(){return mount;}
    public int capacity(){return capacity;}
    public int activeCapacity(){return playerInventory.player.getWorld().isClient()?syncedActiveCapacity:serverActiveCapacity();}
    private int serverActiveCapacity(){return mount==null?0:Math.min(capacity,MountManager.cargoCapacity(mount));}
    public int saddleSlotIndex(){return PLAYER_SLOT_END;}
    public int bodySlotIndex(){return bodySlotIndex;}
    public int cargoStartIndex(){return cargoStartIndex;}
    public int chestSlotIndex(){return chestSlotIndex;}
    public int packSize(){return packSize;}
    public boolean horseLayout(){return horseLayout;}
    public boolean hasCargoAccess(){return activeCapacity()>0&&(llamaPackInventory!=null||getSlot(chestSlotIndex).hasStack());}
    private int attachmentStart(){
        if(playerInventory.player.getWorld().isClient())return syncedAttachmentStart;
        if(llamaPackInventory==null)return 0;
        for(int i=0;i<activeCapacity();i++)if(llamaPackInventory.belongsToAnchor(i))return i;
        return 0;
    }
    private int attachmentCount(){
        if(playerInventory.player.getWorld().isClient())return syncedAttachmentCount;
        return mount instanceof LlamaEntity llama?((MountDataProvider)mount).cranksdungeons$hasMountChest()?LlamaPackManager.memberCapacity(llama):0:activeCapacity();
    }
    public boolean attachmentOwnsCargo(int index){return index>=attachmentStart()&&index<attachmentStart()+attachmentCount();}
    public boolean attachmentHasContents(){
        if(!playerInventory.player.getWorld().isClient())return mount!=null&&!MountManager.cargoIsEmpty(mount);
        for(int i=0;i<capacity;i++)if(attachmentOwnsCargo(i)&&getSlot(cargoStartIndex+i).hasStack())return true;
        return false;
    }
    @Override public boolean canUse(PlayerEntity player){
        if(mount==null||!mount.isAlive()||!player.canInteractWithEntity(mount,4.0))return false;
        if(player.getWorld().isClient())return true;
        if(!MountManager.canAccessStorage(player,mount)||nativeHorse!=null&&!nativeHorse.canUse(player))return false;
        return llamaPackInventory==null||mount instanceof LlamaEntity llama&&LlamaPackManager.samePack(llama,llamaPackInventory.memberUuids())&&LlamaPackManager.canPlayerAccessPack(player,llama);
    }
    @Override public void onClosed(PlayerEntity player){super.onClosed(player);if(nativeHorse!=null)nativeHorse.onClosed(player);}
    @Override public ItemStack quickMove(PlayerEntity player,int index){
        if(index<5||index>=slots.size()||!canUse(player))return ItemStack.EMPTY;
        var slot=getSlot(index);if(!slot.hasStack()||!slot.canTakeItems(player))return ItemStack.EMPTY;
        var moving=slot.getStack();var original=moving.copy();
        if(index>=PLAYER_SLOT_END)PlayerLoadout.of(player.getInventory()).insert(moving);
        else {
            boolean equipped=false;
            for(int i=PLAYER_SLOT_END;i<cargoStartIndex;i++){
                var equipment=getSlot(i);
                if(equipment.isEnabled()&&!equipment.hasStack()&&equipment.canInsert(moving)&&insertItem(moving,i,i+1,false)){equipped=true;break;}
            }
            if(!equipped&&(!hasCargoAccess()||!insertItem(moving,cargoStartIndex,cargoStartIndex+capacity,false)))return ItemStack.EMPTY;
        }
        if(moving.getCount()==original.getCount())return ItemStack.EMPTY;
        slot.onTakeItem(player,original);
        if(moving.isEmpty())slot.setStack(ItemStack.EMPTY);else slot.markDirty();return original;
    }
    private static LivingEntity findMount(PlayerEntity player,int entityId){var entity=player.getWorld().getEntityById(entityId);return entity instanceof LivingEntity living?living:null;}
}
