/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 */
package punchy.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_746;
import punchy.client.state.AttackActionTracker;
import punchy.compat.FirstPersonModelCompat;
import punchy.config.PunchyConfig;

public final class BetterCombatCompat {
    private static final String EVENTS_CLASS = "net.bettercombat.api.client.BetterCombatClientEvents";
    private static final String ATTACK_START_LISTENER = "net.bettercombat.api.client.BetterCombatClientEvents$PlayerAttackStart";
    private static final String ATTACK_HIT_LISTENER = "net.bettercombat.api.client.BetterCombatClientEvents$PlayerAttackHit";
    private static final String CLIENT_MOD_CLASS = "net.bettercombat.client.BetterCombatClientMod";
    private static final String CLIENT_API_CLASS = "net.bettercombat.api.MinecraftClient_BetterCombat";
    private static final String PLAYER_ANIM_ACCESS = "com.zigythebird.playeranim.api.PlayerAnimationAccess";
    private static final String WEAPON_REGISTRY_CLASS = "net.bettercombat.logic.WeaponRegistry";
    private static boolean available = false;
    private static boolean initialized = false;
    private static Object previousFirstPersonMode;
    private static Boolean previousShowArms;
    private static Boolean previousShowOtherHand;
    private static Boolean vanillaItemDisplayApplied;
    private static boolean clientConfigLookupDone;
    private static Object clientConfigObject;
    private static Field firstPersonAnimationsField;
    private static Field showArmsField;
    private static Field showOtherHandField;
    private static Object noFirstPersonAnimationsValue;
    private static Method weaponAttributesMethod;
    private static Method clientApiSwingProgressMethod;
    private static Method clientApiWeaponSwingInProgressMethod;
    private static boolean playerAnimLookupDone;
    private static Class<?> playerAnimAccessClass;
    private static Method playerAnimGetLayerMethod;
    private static Method playerAnimGetLayersMapMethod;
    private static final Map<Class<?>, Method> STOP_LAYER_METHODS;
    private static final Map<Class<?>, Method> SET_POSE_METHODS;
    private static final long ATTACK_VISUAL_BRIDGE_NANOS = 120000000L;
    private static volatile long lastAttackVisualNanos;

    private BetterCombatCompat() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        available = BetterCombatCompat.isClassPresent(EVENTS_CLASS);
        if (!available) {
            return;
        }
        BetterCombatCompat.applyVanillaItemDisplay(PunchyConfig.isBetterCombatCompatEnabled());
        try {
            Class<?> events = Class.forName(EVENTS_CLASS);
            Field attackStartField = events.getField("ATTACK_START");
            Object startPublisher = attackStartField.get(null);
            Class<?> startListenerType = Class.forName(ATTACK_START_LISTENER);
            Method startRegister = BetterCombatCompat.findRegisterMethod(startPublisher, startListenerType);
            if (startRegister == null) {
                available = false;
                return;
            }
            Object startListener = Proxy.newProxyInstance(startListenerType.getClassLoader(), new Class[]{startListenerType}, (proxy, method, args) -> {
                if ("onPlayerAttackStart".equals(method.getName()) && args != null && args.length >= 2) {
                    BetterCombatCompat.handleAttackStart(args[0], args[1]);
                }
                return null;
            });
            startRegister.invoke(startPublisher, startListener);
            Field attackHitField = events.getField("ATTACK_HIT");
            Object hitPublisher = attackHitField.get(null);
            Class<?> hitListenerType = Class.forName(ATTACK_HIT_LISTENER);
            Method hitRegister = BetterCombatCompat.findRegisterMethod(hitPublisher, hitListenerType);
            if (hitRegister == null) {
                available = false;
                return;
            }
            Object hitListener = Proxy.newProxyInstance(hitListenerType.getClassLoader(), new Class[]{hitListenerType}, (proxy, method, args) -> {
                if ("onPlayerAttackStart".equals(method.getName()) && args != null && args.length >= 2) {
                    BetterCombatCompat.handleAttackHit(args[0], args[1]);
                }
                return null;
            });
            hitRegister.invoke(hitPublisher, hitListener);
        }
        catch (Exception ignored) {
            available = false;
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public static boolean isWeaponStack(class_1799 stack) {
        if (!available || stack == null || stack.method_7960()) {
            return false;
        }
        try {
            Object attrs;
            if (weaponAttributesMethod == null) {
                Class<?> registry = Class.forName(WEAPON_REGISTRY_CLASS);
                weaponAttributesMethod = registry.getMethod("getAttributes", class_1799.class);
            }
            return (attrs = weaponAttributesMethod.invoke(null, stack)) != null;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    public static boolean isAttackVisualActive(class_746 player) {
        Boolean inProgress;
        if (!available || player == null) {
            return false;
        }
        class_310 client = class_310.method_1551();
        if (client != null && (inProgress = BetterCombatCompat.queryClientAttackInProgress(client)) != null) {
            return inProgress;
        }
        try {
            Object attackLayer;
            Method getLayer = BetterCombatCompat.resolvePlayerAnimGetLayerMethod();
            if (getLayer != null && BetterCombatCompat.isLayerPlaying(attackLayer = getLayer.invoke(null, player, BetterCombatCompat.createIdentifier("bettercombat", "attack", getLayer)))) {
                return true;
            }
        }
        catch (Exception getLayer) {
            // empty catch block
        }
        long stamp = lastAttackVisualNanos;
        return stamp > 0L && System.nanoTime() - stamp <= 120000000L;
    }

    public static void forceStopAttackAnimation(class_746 player) {
        if (!available || player == null) {
            return;
        }
        if (!PunchyConfig.isBetterCombatCompatEnabled()) {
            return;
        }
        if (FirstPersonModelCompat.isRenderingPlayer()) {
            return;
        }
        if (FirstPersonModelCompat.shouldApplyThirdPersonAnimations(class_310.method_1551())) {
            return;
        }
        if (BetterCombatCompat.isPlayerBlacklisted((class_1657)player)) {
            return;
        }
        BetterCombatCompat.stopBetterCombatAnimation(player);
    }

    public static void suppressItemPoses(class_746 player) {
        if (!available || player == null) {
            return;
        }
        if (!PunchyConfig.isBetterCombatCompatEnabled()) {
            return;
        }
        if (FirstPersonModelCompat.isRenderingPlayer()) {
            return;
        }
        if (FirstPersonModelCompat.shouldApplyThirdPersonAnimations(class_310.method_1551())) {
            return;
        }
        if (BetterCombatCompat.isPlayerBlacklisted((class_1657)player)) {
            return;
        }
        try {
            String[] extraLayers;
            Map<?, ?> layers = BetterCombatCompat.getPlayerAnimationLayers(player);
            if (layers != null) {
                BetterCombatCompat.suppressLayerMap(layers, player.method_6068() == class_1306.field_6182);
                return;
            }
            Method getLayer = BetterCombatCompat.resolvePlayerAnimGetLayerMethod();
            if (getLayer == null) {
                return;
            }
            Object attackLayer = getLayer.invoke(null, player, BetterCombatCompat.createIdentifier("bettercombat", "attack", getLayer));
            BetterCombatCompat.stopLayerAnimation(attackLayer);
            Object mainBody = getLayer.invoke(null, player, BetterCombatCompat.createIdentifier("bettercombat", "pose_main_hand_body", getLayer));
            Object mainItem = getLayer.invoke(null, player, BetterCombatCompat.createIdentifier("bettercombat", "pose_main_hand_item", getLayer));
            Object offBody = getLayer.invoke(null, player, BetterCombatCompat.createIdentifier("bettercombat", "pose_off_hand_body", getLayer));
            Object offItem = getLayer.invoke(null, player, BetterCombatCompat.createIdentifier("bettercombat", "pose_off_hand_item", getLayer));
            boolean leftHanded = player.method_6068() == class_1306.field_6182;
            BetterCombatCompat.clearPoseLayer(mainBody, leftHanded);
            BetterCombatCompat.clearPoseLayer(mainItem, leftHanded);
            BetterCombatCompat.clearPoseLayer(offBody, leftHanded);
            BetterCombatCompat.clearPoseLayer(offItem, leftHanded);
            for (String layerId : extraLayers = new String[]{"use", "use_item", "item_use", "pose_use", "pose_use_item", "pose_use_main_hand", "pose_use_off_hand", "pose_main_hand_use", "pose_off_hand_use", "pose_main_hand_item_use", "pose_off_hand_item_use"}) {
                Object layer = getLayer.invoke(null, player, BetterCombatCompat.createIdentifier("bettercombat", layerId, getLayer));
                BetterCombatCompat.stopLayerAnimation(layer);
                BetterCombatCompat.clearPoseLayer(layer, leftHanded);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void applyVanillaItemDisplay(boolean enabled) {
        if (!available) {
            return;
        }
        if (vanillaItemDisplayApplied != null && vanillaItemDisplayApplied == enabled) {
            return;
        }
        try {
            Object config = BetterCombatCompat.resolveClientConfig();
            if (config == null) {
                return;
            }
            if (enabled) {
                Boolean value;
                Object current;
                if (previousFirstPersonMode == null) {
                    previousFirstPersonMode = firstPersonAnimationsField.get(config);
                }
                if (previousShowArms == null) {
                    current = showArmsField.get(config);
                    Boolean bl = previousShowArms = current instanceof Boolean ? (value = (Boolean)current) : null;
                }
                if (previousShowOtherHand == null) {
                    current = showOtherHandField.get(config);
                    Boolean bl = previousShowOtherHand = current instanceof Boolean ? (value = (Boolean)current) : null;
                }
                if (noFirstPersonAnimationsValue != null) {
                    firstPersonAnimationsField.set(config, noFirstPersonAnimationsValue);
                }
                showArmsField.set(config, false);
                showOtherHandField.set(config, true);
                vanillaItemDisplayApplied = true;
            } else if (previousFirstPersonMode != null) {
                firstPersonAnimationsField.set(config, previousFirstPersonMode);
                previousFirstPersonMode = null;
                if (previousShowArms != null) {
                    showArmsField.set(config, previousShowArms);
                }
                if (previousShowOtherHand != null) {
                    showOtherHandField.set(config, previousShowOtherHand);
                }
                previousShowArms = null;
                previousShowOtherHand = null;
                vanillaItemDisplayApplied = false;
            } else {
                vanillaItemDisplayApplied = false;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static Object resolveClientConfig() {
        if (clientConfigLookupDone) {
            return clientConfigObject;
        }
        clientConfigLookupDone = true;
        try {
            Class<?> clientMod = Class.forName(CLIENT_MOD_CLASS);
            Field configField = clientMod.getField("config");
            Object config = configField.get(null);
            if (config == null) {
                return null;
            }
            firstPersonAnimationsField = config.getClass().getField("firstPersonAnimations");
            showArmsField = config.getClass().getField("isShowingArmsInFirstPerson");
            showOtherHandField = config.getClass().getField("isShowingOtherHandFirstPerson");
            noFirstPersonAnimationsValue = BetterCombatCompat.findEnumValue(firstPersonAnimationsField.getType(), "NO");
            clientConfigObject = config;
            return config;
        }
        catch (Throwable ignored) {
            clientConfigObject = null;
            firstPersonAnimationsField = null;
            showArmsField = null;
            showOtherHandField = null;
            noFirstPersonAnimationsValue = null;
            return null;
        }
    }

    private static void handleAttackStart(Object playerObj, Object attackHandObj) {
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1724 == null) {
            return;
        }
        BetterCombatCompat.markAttackVisualNow();
        if (!PunchyConfig.isBetterCombatCompatEnabled()) {
            return;
        }
        if (BetterCombatCompat.isPlayerBlacklisted((class_1657)client.field_1724)) {
            return;
        }
        if (!client.field_1690.method_31044().method_31034()) {
            return;
        }
        AttackActionTracker.recordCriticalCandidate(client);
        if (FirstPersonModelCompat.isRenderingPlayer()) {
            return;
        }
        if (FirstPersonModelCompat.shouldApplyThirdPersonAnimations(client)) {
            return;
        }
        BetterCombatCompat.stopBetterCombatAnimation(client.field_1724);
    }

    private static void handleAttackHit(Object playerObj, Object attackHandObj) {
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1724 == null) {
            return;
        }
        BetterCombatCompat.markAttackVisualNow();
        if (!PunchyConfig.isBetterCombatCompatEnabled()) {
            return;
        }
        if (BetterCombatCompat.isPlayerBlacklisted((class_1657)client.field_1724)) {
            return;
        }
        if (!client.field_1690.method_31044().method_31034()) {
            return;
        }
        class_1268 hand = BetterCombatCompat.readIsOffHand(attackHandObj) ? class_1268.field_5810 : class_1268.field_5808;
        AttackActionTracker.recordAttack(hand);
        if (FirstPersonModelCompat.isRenderingPlayer()) {
            return;
        }
        if (FirstPersonModelCompat.shouldApplyThirdPersonAnimations(client)) {
            return;
        }
        BetterCombatCompat.stopBetterCombatAnimation(client.field_1724);
    }

    private static boolean isPlayerBlacklisted(class_1657 player) {
        if (player == null) {
            return false;
        }
        return PunchyConfig.isHandBlacklisted(player, class_1268.field_5808) || PunchyConfig.isHandBlacklisted(player, class_1268.field_5810);
    }

    private static boolean readIsOffHand(Object attackHandObj) {
        if (attackHandObj == null) {
            return false;
        }
        try {
            Boolean value;
            Method isOffHand = attackHandObj.getClass().getMethod("isOffHand", new Class[0]);
            Object result = isOffHand.invoke(attackHandObj, new Object[0]);
            return result instanceof Boolean && (value = (Boolean)result) != false;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    private static void markAttackVisualNow() {
        lastAttackVisualNanos = System.nanoTime();
    }

    private static Boolean queryClientAttackInProgress(class_310 client) {
        try {
            Object state;
            if (clientApiWeaponSwingInProgressMethod == null || clientApiSwingProgressMethod == null) {
                Class<?> api = Class.forName(CLIENT_API_CLASS);
                clientApiWeaponSwingInProgressMethod = api.getMethod("isWeaponSwingInProgress", new Class[0]);
                clientApiSwingProgressMethod = api.getMethod("getSwingProgress", new Class[0]);
            }
            if ((state = clientApiWeaponSwingInProgressMethod.invoke((Object)client, new Object[0])) instanceof Boolean) {
                Boolean b = (Boolean)state;
                return b;
            }
            Object progressObj = clientApiSwingProgressMethod.invoke((Object)client, new Object[0]);
            if (progressObj instanceof Number) {
                Number n = (Number)progressObj;
                return n.floatValue() < 0.999f;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private static boolean isLayerPlaying(Object layer) {
        if (layer == null) {
            return false;
        }
        try {
            Object result;
            String n;
            for (Method method : layer.getClass().getMethods()) {
                Boolean state;
                if (method.getParameterCount() != 0 || method.getReturnType() != Boolean.TYPE || !(n = method.getName().toLowerCase(Locale.ROOT)).contains("active") && !n.contains("playing") && !n.contains("running") && !n.contains("animat") || !((result = method.invoke(layer, new Object[0])) instanceof Boolean) || !(state = (Boolean)result).booleanValue()) continue;
                return true;
            }
            for (Method method : layer.getClass().getMethods()) {
                if (method.getParameterCount() != 0 || !(n = method.getName().toLowerCase(Locale.ROOT)).contains("animation") && !n.contains("anim") && !n.contains("current") || (result = method.invoke(layer, new Object[0])) == null) continue;
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private static void stopBetterCombatAnimation(class_746 player) {
        try {
            Method stop = player.getClass().getMethod("stopAttackAnimation", Float.TYPE);
            stop.invoke((Object)player, Float.valueOf(0.0f));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void suppressAllBetterCombatLayers(class_746 player) {
        if (player == null) {
            return;
        }
        try {
            Map<?, ?> map = BetterCombatCompat.getPlayerAnimationLayers(player);
            BetterCombatCompat.suppressLayerMap(map, player.method_6068() == class_1306.field_6182);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void suppressExternalUseLayers(class_746 player) {
        if (player == null) {
            return;
        }
        try {
            Map<?, ?> map = BetterCombatCompat.getPlayerAnimationLayers(player);
            BetterCombatCompat.suppressLayerMap(map, player.method_6068() == class_1306.field_6182);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void suppressLayerMap(Map<?, ?> map, boolean leftHanded) {
        if (map == null || map.isEmpty()) {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            boolean suppress;
            Object key = entry.getKey();
            Object layer = entry.getValue();
            if (key == null || layer == null) continue;
            String id = key.toString();
            String lower = id.toLowerCase(Locale.ROOT);
            boolean bl = suppress = id.startsWith("bettercombat:") || !id.startsWith("punchy:") && (lower.contains("use") || lower.contains("cast") || lower.contains("spell") || lower.contains("magic") || lower.contains("ability") || lower.contains("skill") || lower.contains("hotkey"));
            if (!suppress) continue;
            BetterCombatCompat.stopLayerAnimation(layer);
            BetterCombatCompat.clearPoseLayer(layer, leftHanded);
        }
    }

    private static Map<?, ?> getPlayerAnimationLayers(class_746 player) {
        Method method = BetterCombatCompat.resolvePlayerAnimLayersMapMethod();
        if (method == null || player == null) {
            return null;
        }
        try {
            Map map;
            Object result = method.invoke(null, player);
            return result instanceof Map ? (map = (Map)result) : null;
        }
        catch (Exception ignored) {
            return null;
        }
    }

    private static Method resolvePlayerAnimGetLayerMethod() {
        BetterCombatCompat.ensurePlayerAnimLookup();
        return playerAnimGetLayerMethod;
    }

    private static Method resolvePlayerAnimLayersMapMethod() {
        BetterCombatCompat.ensurePlayerAnimLookup();
        return playerAnimGetLayersMapMethod;
    }

    private static void ensurePlayerAnimLookup() {
        if (playerAnimLookupDone) {
            return;
        }
        playerAnimLookupDone = true;
        try {
            playerAnimAccessClass = Class.forName(PLAYER_ANIM_ACCESS);
            playerAnimGetLayerMethod = BetterCombatCompat.findMethod(playerAnimAccessClass, "getPlayerAnimationLayer", 2);
            for (Method method : playerAnimAccessClass.getMethods()) {
                String name;
                Class<?> param;
                if (method.getParameterCount() != 1 || !class_1657.class.isAssignableFrom(param = method.getParameterTypes()[0]) || !Map.class.isAssignableFrom(method.getReturnType()) || !(name = method.getName().toLowerCase(Locale.ROOT)).contains("layer")) continue;
                playerAnimGetLayersMapMethod = method;
                break;
            }
        }
        catch (Throwable ignored) {
            playerAnimAccessClass = null;
            playerAnimGetLayerMethod = null;
            playerAnimGetLayersMapMethod = null;
        }
    }

    private static Method findRegisterMethod(Object publisher, Class<?> listenerType) {
        if (publisher == null || listenerType == null) {
            return null;
        }
        for (Method method : publisher.getClass().getMethods()) {
            Class<?> param;
            if (method.getParameterCount() != 1 || !(param = method.getParameterTypes()[0]).isAssignableFrom(listenerType) && !listenerType.isAssignableFrom(param)) continue;
            return method;
        }
        return null;
    }

    private static boolean isClassPresent(String name) {
        try {
            Class.forName(name);
            return true;
        }
        catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static Object createIdentifier(String namespace, String path, Method getLayer) {
        if (getLayer == null) {
            return null;
        }
        Class<?> idType = getLayer.getParameterTypes()[1];
        try {
            if ("net.minecraft.resources.ResourceLocation".equals(idType.getName())) {
                return class_2960.method_60655((String)namespace, (String)path);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if ("net.minecraft.util.Identifier".equals(idType.getName())) {
                Method of = idType.getMethod("of", String.class, String.class);
                return of.invoke(null, namespace, path);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private static void clearPoseLayer(Object layer, boolean leftHanded) {
        if (layer == null) {
            return;
        }
        try {
            Method setPose = SET_POSE_METHODS.computeIfAbsent(layer.getClass(), type -> BetterCombatCompat.findMethod(type, "setPose", 2));
            if (setPose != null) {
                setPose.invoke(layer, null, leftHanded);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void stopLayerAnimation(Object layer) {
        if (layer == null) {
            return;
        }
        try {
            Method stop = STOP_LAYER_METHODS.computeIfAbsent(layer.getClass(), type -> BetterCombatCompat.findMethod(type, "stopTriggeredAnimation", 0));
            if (stop != null) {
                stop.invoke(layer, new Object[0]);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static Method findMethod(Class<?> type, String name, int paramCount) {
        if (type == null || name == null) {
            return null;
        }
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != paramCount) continue;
            return method;
        }
        return null;
    }

    private static Object findEnumValue(Class<?> type, String name) {
        if (type == null || name == null || name.isEmpty()) {
            return null;
        }
        if (!type.isEnum()) {
            return null;
        }
        ?[] constants = type.getEnumConstants();
        if (constants == null) {
            return null;
        }
        for (Object constant : constants) {
            if (constant == null || !name.equalsIgnoreCase(constant.toString())) continue;
            return constant;
        }
        return null;
    }

    static {
        STOP_LAYER_METHODS = new ConcurrentHashMap();
        SET_POSE_METHODS = new ConcurrentHashMap();
    }
}

