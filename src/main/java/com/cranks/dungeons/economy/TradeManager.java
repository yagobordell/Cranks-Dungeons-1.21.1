package com.cranks.dungeons.economy;

import com.cranks.dungeons.component.ModComponents;
import com.cranks.dungeons.network.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.*;

/** All actions run on the server thread. Offers reference owned items; cancellation moves nothing. */
public final class TradeManager {
    private record Offer(int slot,int count,ItemStack item) {}
    private static final class Side {
        final UUID player;final List<Offer> offers=new ArrayList<>();long coins;boolean ready,confirmed;
        Side(UUID player){this.player=player;}
    }
    private static final class Session {
        final UUID id=UUID.randomUUID();final Side a,b;final MinecraftServer server;final long expires;
        boolean active;int revision;
        Session(ServerPlayerEntity a,ServerPlayerEntity b){this.a=new Side(a.getUuid());this.b=new Side(b.getUuid());server=a.getServer();expires=server.getOverworld().getTime()+EconomyConfig.RULES.invitationSeconds*20L;}
        Side side(UUID id){return a.player.equals(id)?a:b;}
        Side other(UUID id){return a.player.equals(id)?b:a;}
    }
    private static final Map<UUID,Session> sessions=new HashMap<>();
    private static final Map<UUID,Long> requestCooldown=new HashMap<>();
    private record RequestPair(UUID sender,UUID recipient) {}
    private static final Map<RequestPair,Long> declinedRequests=new HashMap<>();
    public static boolean active(ServerPlayerEntity player){var session=sessions.get(player.getUuid());return session!=null&&session.active;}
    public static boolean closeEnough(ServerPlayerEntity a,ServerPlayerEntity b){return a!=b&&a.isAlive()&&b.isAlive()&&!a.isSpectator()&&!b.isSpectator()&&a.getWorld()==b.getWorld()&&a.squaredDistanceTo(b)<=EconomyConfig.RULES.tradeDistance*EconomyConfig.RULES.tradeDistance&&a.canSee(b)&&b.canSee(a);}
    private static boolean inventoryOpen(ServerPlayerEntity player){return player.currentScreenHandler==player.playerScreenHandler&&player.playerScreenHandler.getCursorStack().isEmpty();}
    private static void message(ServerPlayerEntity player,String message){ServerPlayNetworking.send(player,TradeStatePayload.idle(message));}
    private static ServerPlayerEntity player(Session session,Side side){return session.server.getPlayerManager().getPlayer(side.player);}
    private static void send(Session session,Side side,String message){
        var viewer=player(session,side);var other=session.other(side.player);var partner=player(session,other);if(viewer==null||partner==null)return;
        ServerPlayNetworking.send(viewer,new TradeStatePayload(session.active?TradeStatePayload.ACTIVE:side==session.a?TradeStatePayload.OUTGOING:TradeStatePayload.INCOMING,
                session.id,partner.getUuid(),partner.getName().getString(),session.revision,side.coins,other.coins,side.ready,other.ready,side.confirmed,other.confirmed,
                side.offers.stream().map(o->o.item.copyWithCount(o.count)).toList(),other.offers.stream().map(o->o.item.copyWithCount(o.count)).toList(),message));
    }
    private static void sendBoth(Session session,String message){send(session,session.a,message);send(session,session.b,message);}
    private static void changed(Session session){session.revision++;session.a.ready=session.b.ready=session.a.confirmed=session.b.confirmed=false;}
    private static boolean synchronizable(Session session,Side side,ServerPlayerEntity sender){
        var other=session.other(side.player);
        var probe=new TradeStatePayload(TradeStatePayload.ACTIVE,session.id,other.player,"",session.revision,side.coins,other.coins,
                false,false,false,false,side.offers.stream().map(o->o.item.copyWithCount(o.count)).toList(),other.offers.stream().map(o->o.item.copyWithCount(o.count)).toList(),"");
        var buffer=new net.minecraft.network.RegistryByteBuf(io.netty.buffer.Unpooled.buffer(256,900*1024),sender.getRegistryManager());
        try{TradeStatePayload.CODEC.encode(buffer,probe);return true;}catch(RuntimeException oversized){return false;}finally{buffer.release();}
    }
    public static void cancel(ServerPlayerEntity player,String reason){var session=sessions.get(player.getUuid());if(session!=null)cancel(session,reason);}
    private static void cancel(Session session,String reason){sessions.remove(session.a.player,session);sessions.remove(session.b.player,session);for(var side:List.of(session.a,session.b)){var player=player(session,side);if(player!=null)message(player,reason);}}
    public static void clear(){sessions.clear();requestCooldown.clear();declinedRequests.clear();}
    private static void declined(Session session) {
        declinedRequests.put(new RequestPair(session.a.player,session.b.player),
                session.server.getOverworld().getTime()+EconomyConfig.RULES.declinedRequestSeconds*20L);
        cancel(session,"Trade declined");
    }
    public static void action(ServerPlayerEntity sender,TradeActionPayload action){
        if(!sender.isAlive()||sender.isSpectator())return;
        var session=sessions.get(sender.getUuid());
        if(action.action()==TradeActionPayload.REQUESTS){
            if(action.coins()!=0&&action.coins()!=1){if(session==null)message(sender,"");else send(session,session.side(sender.getUuid()),"");return;}
            var wallet=ModComponents.WALLET.get(sender);wallet.incomingRequests(action.coins()==1);ModComponents.WALLET.sync(sender);
            if(!wallet.incomingRequests()&&session!=null&&!session.active&&session.b.player.equals(sender.getUuid()))declined(session);
            else if(session!=null)send(session,session.side(sender.getUuid()),"");
            else message(sender,"");
            return;
        }
        if(action.action()==TradeActionPayload.REQUEST){
            long now=sender.getServer().getOverworld().getTime();
            if(session!=null){send(session,session.side(sender.getUuid()),"You already have a trade open");return;}
            if(now<requestCooldown.getOrDefault(sender.getUuid(),Long.MIN_VALUE)){message(sender,"Wait a moment before requesting another trade");return;}
            requestCooldown.put(sender.getUuid(),now+40);
            var target=sender.getServer().getPlayerManager().getPlayer(action.target());
            if(target==null||!closeEnough(sender,target)){message(sender,"Stand nearby with a clear line of sight");return;}
            if(!ModComponents.WALLET.get(target).incomingRequests()){message(sender,"That player is not accepting trade requests");return;}
            long retryAt=declinedRequests.getOrDefault(new RequestPair(sender.getUuid(),target.getUuid()),Long.MIN_VALUE);
            if(now<retryAt){message(sender,"That player declined; wait "+((retryAt-now+19)/20)+" seconds before asking again");return;}
            if(!inventoryOpen(sender)||sessions.containsKey(target.getUuid())){message(sender,"Put down held items and wait until both players are available");return;}
            session=new Session(sender,target);sessions.put(sender.getUuid(),session);sessions.put(target.getUuid(),session);sendBoth(session,"");return;
        }
        if(session==null||!session.id.equals(action.session()))return;
        var self=session.side(sender.getUuid());var other=player(session,session.other(sender.getUuid()));
        if(action.action()==TradeActionPayload.CANCEL){if(!session.active&&self==session.b)declined(session);else cancel(session,"Trade cancelled");return;}
        if(other==null||!closeEnough(sender,other)){cancel(session,"Trade cancelled: players moved apart");return;}
        if(action.action()==TradeActionPayload.ACCEPT){
            if(session.active||self!=session.b){send(session,self,"");return;}
            if(!inventoryOpen(sender)||!inventoryOpen(other)){sendBoth(session,"Close other menus and put down held items first");return;}
            session.active=true;sendBoth(session,"");return;
        }
        if(!session.active||!inventoryOpen(sender)||!inventoryOpen(other)){send(session,self,"Put down held items before trading");return;}
        validate(session);
        if(action.revision()!=session.revision){send(session,self,"Offer changed; review it again");return;}
        switch(action.action()){
            case TradeActionPayload.OFFER -> {
                if(self.ready||!TradeInventory.sourceSlot(sender.getInventory(),action.slot())){send(session,self,"");return;}
                var stack=sender.getInventory().getStack(action.slot());if(stack.isEmpty()||action.count()<1||action.count()>stack.getCount()){send(session,self,"Item changed; choose it again");return;}
                var slot=sender.playerScreenHandler.slots.stream().filter(s->s.inventory==sender.getInventory()&&s.getIndex()==action.slot()).findFirst().orElse(null);
                if(slot==null||!slot.isEnabled()||!slot.canTakeItems(sender)){send(session,self,"");return;}
                var existing=self.offers.stream().filter(o->o.slot==action.slot()).findFirst().orElse(null);
                if(existing==null&&self.offers.size()>=9){send(session,self,"Nine different stacks fit in one trade");return;}
                var previous=new ArrayList<>(self.offers);
                if(existing!=null)self.offers.remove(existing);
                int count=Math.min(stack.getCount(),action.count()+(existing==null?0:existing.count));
                self.offers.add(new Offer(action.slot(),count,stack.copy()));
                if(!synchronizable(session,self,sender)){self.offers.clear();self.offers.addAll(previous);send(session,self,"Offer data is too large; trade fewer containers at once");return;}
                changed(session);
            }
            case TradeActionPayload.REMOVE -> {
                if(self.ready||action.slot()<0||action.slot()>=self.offers.size()){send(session,self,"");return;}
                var offer=self.offers.remove(action.slot());
                if(action.count()==1&&offer.count>1)self.offers.add(action.slot(),new Offer(offer.slot,offer.count-1,offer.item));changed(session);
            }
            case TradeActionPayload.COINS,TradeActionPayload.READY -> {
                if(self.ready||action.coins()<0||action.coins()>ModComponents.WALLET.get(sender).balance()){send(session,self,"You do not have that many coins");return;}
                if(self.coins!=action.coins()){self.coins=action.coins();changed(session);}
                if(action.action()==TradeActionPayload.READY)self.ready=true;
            }
            case TradeActionPayload.EDIT -> changed(session);
            case TradeActionPayload.CONFIRM -> {
                if(!session.a.ready||!session.b.ready){send(session,self,"Both players must be ready first");return;}self.confirmed=true;
                if(session.a.confirmed&&session.b.confirmed){complete(session);return;}
            }
            default -> {send(session,self,"");return;}
        }
        sendBoth(session,"");
    }
    private static void validate(Session session){
        boolean changed=false;
        for(var side:List.of(session.a,session.b)){
            var player=player(session,side);if(player==null)continue;
            changed|=side.offers.removeIf(offer->{var stack=player.getInventory().getStack(offer.slot);return !TradeInventory.sourceSlot(player.getInventory(),offer.slot)||stack.getCount()<offer.count||!ItemStack.areItemsAndComponentsEqual(stack,offer.item);});
            if(side.coins>ModComponents.WALLET.get(player).balance()){side.coins=0;changed=true;}
        }
        if(changed){changed(session);sendBoth(session,"An offered item or balance changed; review the trade");}
    }
    private static void complete(Session session){
        validate(session);if(!session.a.confirmed||!session.b.confirmed)return;
        var a=player(session,session.a);var b=player(session,session.b);
        if(a==null||b==null||!closeEnough(a,b)||!inventoryOpen(a)||!inventoryOpen(b)){cancel(session,"Trade cancelled");return;}
        if(session.a.offers.isEmpty()&&session.b.offers.isEmpty()&&session.a.coins==0&&session.b.coins==0){changed(session);sendBoth(session,"Add items or coins first");return;}
        var afterA=TradeInventory.snapshot(a.getInventory());var afterB=TradeInventory.snapshot(b.getInventory());
        for(var offer:session.a.offers)afterA.get(offer.slot).decrement(offer.count);
        for(var offer:session.b.offers)afterB.get(offer.slot).decrement(offer.count);
        if(!TradeInventory.insert(afterA,a.getInventory(),session.b.offers.stream().map(o->o.item.copyWithCount(o.count)).toList())
                ||!TradeInventory.insert(afterB,b.getInventory(),session.a.offers.stream().map(o->o.item.copyWithCount(o.count)).toList())){changed(session);sendBoth(session,"Make room in both inventories before confirming");return;}
        var state=EconomyState.get(a.getServer());long coinsA=state.balance(a.getUuid())-session.a.coins+session.b.coins,coinsB=state.balance(b.getUuid())-session.b.coins+session.a.coins;
        if(coinsA<0||coinsB<0||coinsA>EconomyState.MAX_COINS||coinsB>EconomyState.MAX_COINS){changed(session);sendBoth(session,"Coin balance is out of range");return;}
        long beforeA=state.balance(a.getUuid()),beforeB=state.balance(b.getUuid());
        if(!state.exchange(a,afterA,coinsA,b,afterB,coinsB)){changed(session);sendBoth(session,"Trade could not be saved; nothing was exchanged");return;}
        TradeAuditLog.completed(a,b,beforeA,beforeB,coinsA,coinsB,session.a.coins,session.b.coins,
                session.a.offers.stream().map(o->o.item.copyWithCount(o.count)).toList(),
                session.b.offers.stream().map(o->o.item.copyWithCount(o.count)).toList());
        cancel(session,"Trade completed");
    }
    public static void tick(MinecraftServer server){
        long now=server.getOverworld().getTime();
        if(now%20==0){requestCooldown.values().removeIf(expiry->expiry<=now);declinedRequests.values().removeIf(expiry->expiry<=now);}
        for(var session:new HashSet<>(sessions.values())){
            if(session.server!=server)continue;var a=player(session,session.a);var b=player(session,session.b);
            if(a==null||b==null||!closeEnough(a,b)){cancel(session,"Trade cancelled: player unavailable or out of reach");continue;}
            if(!session.active&&server.getOverworld().getTime()>session.expires){cancel(session,"Trade invitation expired");continue;}
            if(session.active){if(!inventoryOpen(a)||!inventoryOpen(b)){cancel(session,"Trade cancelled: another menu was opened");continue;}validate(session);}
        }
    }
}
