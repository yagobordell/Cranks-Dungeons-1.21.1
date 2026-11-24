package com.cranks.dungeons.client;

import com.cranks.dungeons.client.screen.NameTagScreen;
import com.cranks.dungeons.network.ApplyNameTagPayload;
import com.cranks.dungeons.network.OpenNameTagScreenPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import java.util.ArrayDeque;

/** Taming invites wait for an open menu instead of interrupting an inventory transaction. */
public final class NameTagClient {
    private record Pending(OpenNameTagScreenPayload payload,ClientWorld world,long expires){}
    private static final ArrayDeque<Pending> QUEUE=new ArrayDeque<>();
    private NameTagClient(){}
    public static void register(){
        ClientPlayNetworking.registerGlobalReceiver(OpenNameTagScreenPayload.ID,(payload,context)->context.client().execute(()->{
            var client=context.client();
            if(!payload.free()){client.setScreen(new NameTagScreen(payload.entityId(),payload.currentName(),payload.offHand()));return;}
            if(QUEUE.size()>=16)cancel(QUEUE.removeFirst().payload);
            QUEUE.addLast(new Pending(payload,client.world,System.nanoTime()+300_000_000_000L));
        }));
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            if(client.player==null||client.world==null){QUEUE.clear();return;}
            QUEUE.removeIf(pending->{
                boolean expired=pending.world!=client.world||System.nanoTime()>pending.expires||!client.player.isAlive();
                if(expired)cancel(pending.payload);return expired;
            });
            if(client.currentScreen!=null||QUEUE.isEmpty())return;
            var pending=QUEUE.peekFirst();var animal=client.world.getEntityById(pending.payload.entityId());
            if(animal==null)return;
            QUEUE.removeFirst();
            if(!animal.isAlive()||client.player.squaredDistanceTo(animal)>256){cancel(pending.payload);return;}
            var payload=pending.payload;
            client.setScreen(new NameTagScreen(payload.entityId(),payload.currentName(),payload.offHand(),payload.token()));
        });
    }
    private static void cancel(OpenNameTagScreenPayload payload){
        if(ClientPlayNetworking.canSend(ApplyNameTagPayload.ID))ClientPlayNetworking.send(new ApplyNameTagPayload(payload.entityId(),"",payload.offHand(),payload.token()));
    }
}
