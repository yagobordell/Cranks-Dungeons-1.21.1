/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.annotations.SerializedName
 *  net.minecraft.class_1268
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_7923
 */
package punchy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_7923;
import punchy.compat.TaczBlacklistCompat;

public final class PunchyConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "punchy_config.json";
    public static final float ANIMATION_SPEED_DEFAULT = 5.5f;
    public static final float ANIMATION_SPEED_MIN = 0.5f;
    public static final float ANIMATION_SPEED_MAX = 7.0f;
    public static final float FIRSTPERSON_HIDE_PITCH_DEFAULT = 60.0f;
    public static final float FIRSTPERSON_HIDE_PITCH_MIN = 0.0f;
    public static final float FIRSTPERSON_HIDE_PITCH_MAX = 90.0f;
    public static final float FIRSTPERSON_HIDE_PITCH_NEVER_THRESHOLD = 85.0f;
    public static final float SPRINT_ARM_SWING_INTENSITY_DEFAULT = 1.0f;
    public static final float SPRINT_ARM_SWING_INTENSITY_MIN = -1.0f;
    public static final float SPRINT_ARM_SWING_INTENSITY_MAX = 1.0f;
    public static final float SPRINT_ARM_SWING_SPEED_DEFAULT = 0.0f;
    public static final float SPRINT_ARM_SWING_SPEED_MIN = -1.0f;
    public static final float SPRINT_ARM_SWING_SPEED_MAX = 1.0f;
    private static Data DATA = new Data();

    private PunchyConfig() {
    }

    public static void load() {
        Data loaded;
        Path file;
        block8: {
            Path dir = PunchyConfig.getConfigDirectory().resolve("punchy");
            file = dir.resolve(FILE_NAME);
            loaded = null;
            try {
                Files.createDirectories(dir, new FileAttribute[0]);
                if (!Files.exists(file, new LinkOption[0])) break block8;
                try (FileReader reader = new FileReader(file.toFile());){
                    loaded = (Data)GSON.fromJson((Reader)reader, Data.class);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (loaded == null) {
            loaded = new Data();
            PunchyConfig.write(file, loaded);
        }
        DATA = PunchyConfig.sanitize(loaded);
    }

    public static boolean renderArmorArmsFP() {
        return DATA != null && PunchyConfig.DATA.renderArmorArmsFP;
    }

    public static boolean isInteractiveStuffCompatEnabled() {
        return DATA != null && PunchyConfig.DATA.interactiveStuffCompat;
    }

    public static boolean isBetterCombatCompatEnabled() {
        return DATA != null && PunchyConfig.DATA.bettercombatCompat;
    }

    public static boolean isSwordBlockingCompatEnabled() {
        return DATA != null && PunchyConfig.DATA.swordBlockingCompat;
    }

    public static float getAnimationSpeed() {
        if (DATA == null) {
            return 5.5f;
        }
        return PunchyConfig.DATA.animationSpeed;
    }

    public static float getFirstPersonModelHidePitch() {
        if (DATA == null) {
            return 60.0f;
        }
        return PunchyConfig.DATA.firstPersonModelHidePitch;
    }

    public static boolean isFirstPersonModelHideNever() {
        return PunchyConfig.getFirstPersonModelHidePitch() > 85.0f;
    }

    public static boolean isFirstPersonModelHideEnabled() {
        return DATA == null || PunchyConfig.DATA.firstPersonModelHideEnabled;
    }

    public static boolean isModEnabled() {
        return DATA == null || PunchyConfig.DATA.enableMod;
    }

    public static boolean isTuningEnabled() {
        return DATA != null && PunchyConfig.DATA.enableTuning;
    }

    public static boolean isItemBlacklisted(class_1799 stack) {
        if (DATA == null || PunchyConfig.DATA.itemBlacklist == null || stack == null || stack.method_7960()) {
            return false;
        }
        return PunchyConfig.findBlacklistMatch(stack) != null;
    }

    public static boolean isItemBlacklistDualHanded(class_1799 stack) {
        if (DATA == null || PunchyConfig.DATA.blacklistApplyDualHanded == null || stack == null || stack.method_7960()) {
            return false;
        }
        String match = PunchyConfig.findBlacklistMatch(stack);
        if (match == null) {
            return false;
        }
        return Boolean.TRUE.equals(PunchyConfig.DATA.blacklistApplyDualHanded.get(match));
    }

    public static boolean isHandBlacklisted(class_1657 player, class_1268 hand) {
        if (player == null) {
            return false;
        }
        class_1268 resolved = hand == null ? class_1268.field_5808 : hand;
        class_1799 handStack = player.method_5998(resolved);
        if (PunchyConfig.isItemBlacklisted(handStack)) {
            return true;
        }
        class_1268 opposite = resolved == class_1268.field_5808 ? class_1268.field_5810 : class_1268.field_5808;
        class_1799 oppositeStack = player.method_5998(opposite);
        return PunchyConfig.isItemBlacklistDualHanded(oppositeStack);
    }

    private static boolean matchesBlacklistEntry(String entry, String key, String namespace) {
        boolean hasNamespace;
        if (entry == null || entry.isBlank()) {
            return false;
        }
        String trimmed = entry;
        if (trimmed.endsWith(":")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.isEmpty()) {
            return false;
        }
        boolean hasWildcard = trimmed.indexOf(42) >= 0 || trimmed.indexOf(63) >= 0;
        boolean bl = hasNamespace = trimmed.indexOf(58) >= 0;
        if (hasNamespace) {
            if (!hasWildcard) {
                return key.equals(trimmed);
            }
            return PunchyConfig.globMatch(trimmed, key);
        }
        if (!hasWildcard) {
            return namespace.equals(trimmed);
        }
        return PunchyConfig.globMatch(trimmed, namespace);
    }

    private static boolean globMatch(String pattern, String value) {
        if (pattern == null || value == null) {
            return false;
        }
        StringBuilder regex = new StringBuilder(pattern.length() * 2);
        block16: for (int i = 0; i < pattern.length(); ++i) {
            char c = pattern.charAt(i);
            switch (c) {
                case '*': {
                    regex.append(".*");
                    continue block16;
                }
                case '?': {
                    regex.append('.');
                    continue block16;
                }
                case '.': {
                    regex.append("\\.");
                    continue block16;
                }
                case '\\': {
                    regex.append("\\\\");
                    continue block16;
                }
                case '+': {
                    regex.append("\\+");
                    continue block16;
                }
                case '(': {
                    regex.append("\\(");
                    continue block16;
                }
                case ')': {
                    regex.append("\\)");
                    continue block16;
                }
                case '^': {
                    regex.append("\\^");
                    continue block16;
                }
                case '$': {
                    regex.append("\\$");
                    continue block16;
                }
                case '|': {
                    regex.append("\\|");
                    continue block16;
                }
                case '{': {
                    regex.append("\\{");
                    continue block16;
                }
                case '}': {
                    regex.append("\\}");
                    continue block16;
                }
                case '[': {
                    regex.append("\\[");
                    continue block16;
                }
                case ']': {
                    regex.append("\\]");
                    continue block16;
                }
                default: {
                    regex.append(c);
                }
            }
        }
        return value.matches(regex.toString());
    }

    public static boolean disableResourcePackModelParts() {
        return DATA != null && PunchyConfig.DATA.disableResourcePackModelParts;
    }

    public static boolean disableArmPhysics() {
        return DATA != null && PunchyConfig.DATA.disableArmPhysics;
    }

    public static boolean disableNativeItemPhysics() {
        return DATA != null && PunchyConfig.DATA.disableNativeItemPhysics;
    }

    public static boolean disableBoatMinecartRaftModels() {
        return DATA != null && (PunchyConfig.DATA.disableBoatMinecartRaftModels || PunchyConfig.DATA.interactiveStuffCompat);
    }

    public static boolean disablePistonModels() {
        return DATA != null && (PunchyConfig.DATA.disablePistonModels || PunchyConfig.DATA.interactiveStuffCompat);
    }

    public static boolean disableChestModels() {
        return DATA != null && (PunchyConfig.DATA.disableChestModels || PunchyConfig.DATA.interactiveStuffCompat);
    }

    public static boolean disableEnchantingTableModels() {
        return DATA != null && (PunchyConfig.DATA.disableEnchantingTableModels || PunchyConfig.DATA.interactiveStuffCompat);
    }

    public static boolean disableBoatFirstPersonAnimations() {
        return DATA != null && PunchyConfig.DATA.disableBoatFirstPersonAnimations;
    }

    public static boolean disableEnhancedFireArmEffects() {
        return DATA != null && PunchyConfig.DATA.disableEnhancedFireArmEffects;
    }

    public static boolean enableCrawlAnimation() {
        return DATA != null && PunchyConfig.DATA.enableCrawlAnimation;
    }

    public static boolean enableClimbAnimation() {
        return DATA != null && PunchyConfig.DATA.enableClimbAnimation;
    }

    public static boolean enableSwimAnimation() {
        return DATA != null && PunchyConfig.DATA.enableSwimAnimation;
    }

    public static boolean enableElytraFlightAnimation() {
        return DATA == null || PunchyConfig.DATA.enableElytraFlightAnimation;
    }

    public static boolean enableCustomWalk() {
        return DATA == null || PunchyConfig.DATA.enableCustomWalk;
    }

    public static boolean enableSprintArmSwing() {
        return DATA == null || PunchyConfig.DATA.enableSprintArmSwing;
    }

    public static boolean enableFallArmAnimation() {
        return DATA == null || PunchyConfig.DATA.enableFallArmAnimation;
    }

    public static float getSprintArmSwingIntensity() {
        if (DATA == null) {
            return 1.0f;
        }
        return PunchyConfig.DATA.sprintArmSwingIntensity;
    }

    public static float getSprintArmSwingSpeed() {
        if (DATA == null) {
            return 0.0f;
        }
        return PunchyConfig.DATA.sprintArmSwingSpeed;
    }

    public static boolean enableFreezeShake() {
        return DATA == null || PunchyConfig.DATA.enableFreezeShake;
    }

    public static boolean enableFreezeArmOverlay() {
        return DATA == null || PunchyConfig.DATA.enableFreezeArmOverlay;
    }

    public static boolean enableMud() {
        return DATA == null || PunchyConfig.DATA.enableMud;
    }

    public static boolean enableSweat() {
        return false;
    }

    public static boolean enableBurn() {
        return DATA == null || PunchyConfig.DATA.enableBurn;
    }

    public static boolean enableParticles() {
        return DATA == null || PunchyConfig.DATA.enableParticles;
    }

    public static boolean enableModelPartsGlow() {
        return DATA == null || PunchyConfig.DATA.enableModelPartsGlow;
    }

    public static boolean enableBlurGlowAtDark() {
        return DATA != null && PunchyConfig.DATA.enableBlurGlowAtDark;
    }

    public static Data snapshot() {
        Data copy = new Data();
        if (DATA != null) {
            copy.renderArmorArmsFP = PunchyConfig.DATA.renderArmorArmsFP;
            copy.interactiveStuffCompat = PunchyConfig.DATA.interactiveStuffCompat;
            copy.bettercombatCompat = PunchyConfig.DATA.bettercombatCompat;
            copy.swordBlockingCompat = PunchyConfig.DATA.swordBlockingCompat;
            copy.animationSpeed = PunchyConfig.DATA.animationSpeed;
            copy.enableMod = PunchyConfig.DATA.enableMod;
            copy.enableTuning = PunchyConfig.DATA.enableTuning;
            copy.disableResourcePackModelParts = PunchyConfig.DATA.disableResourcePackModelParts;
            copy.disableArmPhysics = PunchyConfig.DATA.disableArmPhysics;
            copy.disableNativeItemPhysics = PunchyConfig.DATA.disableNativeItemPhysics;
            copy.disableBoatMinecartRaftModels = PunchyConfig.DATA.disableBoatMinecartRaftModels;
            copy.disablePistonModels = PunchyConfig.DATA.disablePistonModels;
            copy.disableChestModels = PunchyConfig.DATA.disableChestModels;
            copy.disableEnchantingTableModels = PunchyConfig.DATA.disableEnchantingTableModels;
            copy.disableBoatFirstPersonAnimations = PunchyConfig.DATA.disableBoatFirstPersonAnimations;
            copy.disableEnhancedFireArmEffects = PunchyConfig.DATA.disableEnhancedFireArmEffects;
            copy.enableCrawlAnimation = PunchyConfig.DATA.enableCrawlAnimation;
            copy.enableClimbAnimation = PunchyConfig.DATA.enableClimbAnimation;
            copy.enableSwimAnimation = PunchyConfig.DATA.enableSwimAnimation;
            copy.enableElytraFlightAnimation = PunchyConfig.DATA.enableElytraFlightAnimation;
            copy.enableCustomWalk = PunchyConfig.DATA.enableCustomWalk;
            copy.enableSprintArmSwing = PunchyConfig.DATA.enableSprintArmSwing;
            copy.enableFallArmAnimation = PunchyConfig.DATA.enableFallArmAnimation;
            copy.sprintArmSwingIntensity = PunchyConfig.DATA.sprintArmSwingIntensity;
            copy.sprintArmSwingSpeed = PunchyConfig.DATA.sprintArmSwingSpeed;
            copy.enableFreezeShake = PunchyConfig.DATA.enableFreezeShake;
            copy.enableFreezeArmOverlay = PunchyConfig.DATA.enableFreezeArmOverlay;
            copy.enableMud = PunchyConfig.DATA.enableMud;
            copy.enableSweat = PunchyConfig.DATA.enableSweat;
            copy.enableBurn = PunchyConfig.DATA.enableBurn;
            copy.enableParticles = PunchyConfig.DATA.enableParticles;
            copy.enableModelPartsGlow = PunchyConfig.DATA.enableModelPartsGlow;
            copy.enableBlurGlowAtDark = PunchyConfig.DATA.enableBlurGlowAtDark;
            copy.firstPersonModelHideEnabled = PunchyConfig.DATA.firstPersonModelHideEnabled;
            copy.firstPersonModelHidePitch = PunchyConfig.DATA.firstPersonModelHidePitch;
            copy.itemBlacklist = PunchyConfig.DATA.itemBlacklist == null ? new ArrayList<String>() : new ArrayList<String>(PunchyConfig.DATA.itemBlacklist);
            copy.blacklistApplyDualHanded = PunchyConfig.DATA.blacklistApplyDualHanded == null ? new LinkedHashMap<String, Boolean>() : new LinkedHashMap<String, Boolean>(PunchyConfig.DATA.blacklistApplyDualHanded);
            copy.animationPackSelections = PunchyConfig.DATA.animationPackSelections == null ? new LinkedHashMap<String, String>() : new LinkedHashMap<String, String>(PunchyConfig.DATA.animationPackSelections);
            copy.animationPackTypeSelections = PunchyConfig.DATA.animationPackTypeSelections == null ? new LinkedHashMap<String, String>() : new LinkedHashMap<String, String>(PunchyConfig.DATA.animationPackTypeSelections);
            copy.mixSpecificForceOverride = PunchyConfig.DATA.mixSpecificForceOverride == null ? new LinkedHashMap<String, Boolean>() : new LinkedHashMap<String, Boolean>(PunchyConfig.DATA.mixSpecificForceOverride);
        }
        return copy;
    }

    public static String getAnimationPackSelection(String animationName) {
        if (animationName == null || animationName.isBlank() || DATA == null || PunchyConfig.DATA.animationPackSelections == null) {
            return null;
        }
        return PunchyConfig.DATA.animationPackSelections.get(animationName);
    }

    public static void setAnimationPackSelection(String animationName, String optionId) {
        if (animationName == null || animationName.isBlank()) {
            return;
        }
        if (DATA == null) {
            DATA = new Data();
        }
        if (PunchyConfig.DATA.animationPackSelections == null) {
            PunchyConfig.DATA.animationPackSelections = new LinkedHashMap<String, String>();
        }
        if (optionId == null || optionId.isBlank()) {
            PunchyConfig.DATA.animationPackSelections.remove(animationName);
        } else {
            PunchyConfig.DATA.animationPackSelections.put(animationName, optionId.trim().toLowerCase(Locale.ROOT));
        }
        DATA = PunchyConfig.sanitize(DATA);
        PunchyConfig.write(PunchyConfig.getConfigFile(), DATA);
    }

    public static String getAnimationPackTypeSelection(String slotId, String typeId) {
        if (slotId == null || slotId.isBlank() || typeId == null || typeId.isBlank() || DATA == null || PunchyConfig.DATA.animationPackTypeSelections == null) {
            return null;
        }
        return PunchyConfig.DATA.animationPackTypeSelections.get(slotId + "|" + typeId.trim().toLowerCase(Locale.ROOT));
    }

    public static void setAnimationPackTypeSelection(String slotId, String typeId, String optionId) {
        if (slotId == null || slotId.isBlank() || typeId == null || typeId.isBlank()) {
            return;
        }
        if (DATA == null) {
            DATA = new Data();
        }
        if (PunchyConfig.DATA.animationPackTypeSelections == null) {
            PunchyConfig.DATA.animationPackTypeSelections = new LinkedHashMap<String, String>();
        }
        String key = slotId + "|" + typeId.trim().toLowerCase(Locale.ROOT);
        if (optionId == null || optionId.isBlank()) {
            PunchyConfig.DATA.animationPackTypeSelections.remove(key);
        } else {
            PunchyConfig.DATA.animationPackTypeSelections.put(key, optionId.trim().toLowerCase(Locale.ROOT));
        }
        DATA = PunchyConfig.sanitize(DATA);
        PunchyConfig.write(PunchyConfig.getConfigFile(), DATA);
    }

    public static void clearAnimationPackTypeSelections(String slotId) {
        if (slotId == null || slotId.isBlank() || DATA == null || PunchyConfig.DATA.animationPackTypeSelections == null) {
            return;
        }
        String prefix = slotId + "|";
        PunchyConfig.DATA.animationPackTypeSelections.entrySet().removeIf(entry -> entry != null && entry.getKey() != null && ((String)entry.getKey()).startsWith(prefix));
        DATA = PunchyConfig.sanitize(DATA);
        PunchyConfig.write(PunchyConfig.getConfigFile(), DATA);
    }

    public static void clearAnimationPackSelections() {
        if (DATA == null) {
            DATA = new Data();
        }
        if (PunchyConfig.DATA.animationPackSelections == null) {
            PunchyConfig.DATA.animationPackSelections = new LinkedHashMap<String, String>();
        } else {
            PunchyConfig.DATA.animationPackSelections.clear();
        }
        if (PunchyConfig.DATA.animationPackTypeSelections == null) {
            PunchyConfig.DATA.animationPackTypeSelections = new LinkedHashMap<String, String>();
        } else {
            PunchyConfig.DATA.animationPackTypeSelections.clear();
        }
        if (PunchyConfig.DATA.mixSpecificForceOverride == null) {
            PunchyConfig.DATA.mixSpecificForceOverride = new LinkedHashMap<String, Boolean>();
        } else {
            PunchyConfig.DATA.mixSpecificForceOverride.clear();
        }
        DATA = PunchyConfig.sanitize(DATA);
        PunchyConfig.write(PunchyConfig.getConfigFile(), DATA);
    }

    public static void replaceAnimationPackSelections(Map<String, String> selections, Map<String, String> typeSelections, Map<String, Boolean> forceOverrides) {
        if (DATA == null) {
            DATA = new Data();
        }
        PunchyConfig.DATA.animationPackSelections = selections == null ? new LinkedHashMap<String, String>() : new LinkedHashMap<String, String>(selections);
        PunchyConfig.DATA.animationPackTypeSelections = typeSelections == null ? new LinkedHashMap<String, String>() : new LinkedHashMap<String, String>(typeSelections);
        PunchyConfig.DATA.mixSpecificForceOverride = forceOverrides == null ? new LinkedHashMap<String, Boolean>() : new LinkedHashMap<String, Boolean>(forceOverrides);
        DATA = PunchyConfig.sanitize(DATA);
        PunchyConfig.write(PunchyConfig.getConfigFile(), DATA);
    }

    public static boolean isMixSpecificForceOverride(String slotId) {
        if (slotId == null || slotId.isBlank() || DATA == null || PunchyConfig.DATA.mixSpecificForceOverride == null) {
            return false;
        }
        return Boolean.TRUE.equals(PunchyConfig.DATA.mixSpecificForceOverride.get(slotId));
    }

    public static void setMixSpecificForceOverride(String slotId, boolean forced) {
        if (slotId == null || slotId.isBlank()) {
            return;
        }
        if (DATA == null) {
            DATA = new Data();
        }
        if (PunchyConfig.DATA.mixSpecificForceOverride == null) {
            PunchyConfig.DATA.mixSpecificForceOverride = new LinkedHashMap<String, Boolean>();
        }
        if (forced) {
            PunchyConfig.DATA.mixSpecificForceOverride.put(slotId, true);
        } else {
            PunchyConfig.DATA.mixSpecificForceOverride.remove(slotId);
        }
        DATA = PunchyConfig.sanitize(DATA);
        PunchyConfig.write(PunchyConfig.getConfigFile(), DATA);
    }

    public static void applyAndSave(Data data) {
        if (data == null) {
            return;
        }
        DATA = PunchyConfig.sanitize(data);
        PunchyConfig.write(PunchyConfig.getConfigFile(), DATA);
    }

    private static void write(Path file, Data data) {
        try (FileWriter writer = new FileWriter(file.toFile());){
            GSON.toJson((Object)data, (Appendable)writer);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static Path getConfigFile() {
        return PunchyConfig.getConfigDirectory().resolve("punchy").resolve(FILE_NAME);
    }

    private static Path getConfigDirectory() {
        class_310 client = class_310.method_1551();
        if (client != null) {
            return client.field_1697.toPath().resolve("config");
        }
        return Path.of("config", new String[0]);
    }

    /*
     * WARNING - void declaration
     */
    private static Data sanitize(Data data) {
        if (data == null) {
            return new Data();
        }
        data.animationSpeed = class_3532.method_15363((float)data.animationSpeed, (float)0.5f, (float)7.0f);
        data.firstPersonModelHidePitch = class_3532.method_15363((float)data.firstPersonModelHidePitch, (float)0.0f, (float)90.0f);
        data.sprintArmSwingIntensity = class_3532.method_15363((float)data.sprintArmSwingIntensity, (float)-1.0f, (float)1.0f);
        data.sprintArmSwingSpeed = class_3532.method_15363((float)data.sprintArmSwingSpeed, (float)-1.0f, (float)1.0f);
        if (data.itemBlacklist == null) {
            data.itemBlacklist = new ArrayList<String>();
        }
        if (data.blacklistApplyDualHanded == null) {
            data.blacklistApplyDualHanded = new LinkedHashMap<String, Boolean>();
        }
        if (data.animationPackSelections == null) {
            data.animationPackSelections = new LinkedHashMap<String, String>();
        }
        if (data.animationPackTypeSelections == null) {
            data.animationPackTypeSelections = new LinkedHashMap<String, String>();
        }
        if (data.mixSpecificForceOverride == null) {
            data.mixSpecificForceOverride = new LinkedHashMap<String, Boolean>();
        }
        LinkedHashSet<void> normalized = new LinkedHashSet<void>();
        for (String string : data.itemBlacklist) {
            void var4_6;
            if (string == null) continue;
            String string2 = string.trim().toLowerCase(Locale.ROOT);
            if (string2.endsWith(":")) {
                String string3 = string2.substring(0, string2.length() - 1);
            }
            if (var4_6.isEmpty()) continue;
            normalized.add(var4_6);
        }
        data.itemBlacklist = new ArrayList<String>(normalized);
        LinkedHashMap<void, Boolean> normalizedDualInput = new LinkedHashMap<void, Boolean>();
        for (Map.Entry<String, Boolean> entry : data.blacklistApplyDualHanded.entrySet()) {
            void var5_13;
            if (entry == null || entry.getKey() == null) continue;
            String string = entry.getKey().trim().toLowerCase(Locale.ROOT);
            if (string.endsWith(":")) {
                String string4 = string.substring(0, string.length() - 1);
            }
            if (var5_13.isEmpty()) continue;
            normalizedDualInput.put(var5_13, Boolean.TRUE.equals(entry.getValue()));
        }
        LinkedHashMap<String, Boolean> linkedHashMap = new LinkedHashMap<String, Boolean>();
        for (String string : data.itemBlacklist) {
            linkedHashMap.put(string, Boolean.TRUE.equals(normalizedDualInput.get(string)));
        }
        data.blacklistApplyDualHanded = linkedHashMap;
        LinkedHashMap<String, String> linkedHashMap2 = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : data.animationPackSelections.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            String string = entry.getKey().trim();
            String option = entry.getValue().trim().toLowerCase(Locale.ROOT);
            if (string.isEmpty() || option.isEmpty()) continue;
            linkedHashMap2.put(string, option);
        }
        data.animationPackSelections = linkedHashMap2;
        LinkedHashMap<String, String> linkedHashMap3 = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : data.animationPackTypeSelections.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            String key = entry.getKey().trim().toLowerCase(Locale.ROOT);
            String option = entry.getValue().trim().toLowerCase(Locale.ROOT);
            if (key.isEmpty() || option.isEmpty() || !key.contains("|")) continue;
            linkedHashMap3.put(key, option);
        }
        data.animationPackTypeSelections = linkedHashMap3;
        LinkedHashMap<String, Boolean> linkedHashMap4 = new LinkedHashMap<String, Boolean>();
        for (Map.Entry<String, Boolean> entry : data.mixSpecificForceOverride.entrySet()) {
            String key;
            if (entry == null || entry.getKey() == null || (key = entry.getKey().trim()).isEmpty() || !Boolean.TRUE.equals(entry.getValue())) continue;
            linkedHashMap4.put(key, true);
        }
        data.mixSpecificForceOverride = linkedHashMap4;
        return data;
    }

    private static String findBlacklistMatch(class_1799 stack) {
        if (DATA == null || PunchyConfig.DATA.itemBlacklist == null || stack == null || stack.method_7960()) {
            return null;
        }
        class_2960 id = class_7923.field_41178.method_10221((Object)stack.method_7909());
        if (id == null) {
            return null;
        }
        LinkedHashSet<String> candidateKeys = new LinkedHashSet<String>();
        String key = id.toString();
        candidateKeys.add(key);
        TaczBlacklistCompat.appendVirtualKeys(stack, candidateKeys);
        for (String entry : PunchyConfig.DATA.itemBlacklist) {
            Iterator iterator = candidateKeys.iterator();
            while (iterator.hasNext()) {
                String candidateKey;
                int separator = (candidateKey = (String)iterator.next()).indexOf(58);
                String namespace = separator >= 0 ? candidateKey.substring(0, separator) : candidateKey;
                if (!PunchyConfig.matchesBlacklistEntry(entry, candidateKey, namespace)) continue;
                return entry;
            }
        }
        return null;
    }

    public static final class Data {
        public boolean renderArmorArmsFP = true;
        public boolean interactiveStuffCompat = false;
        @SerializedName(value="bettercombatCompat", alternate={"BetterCombatCompat", "betterCombatCompat"})
        public boolean bettercombatCompat = false;
        @SerializedName(value="swordBlockingCompat", alternate={"SwordBlockingCompat", "swordblockingCompat"})
        public boolean swordBlockingCompat = false;
        public float animationSpeed = 5.5f;
        public boolean enableMod = true;
        public boolean enableTuning = false;
        public boolean disableResourcePackModelParts = false;
        public boolean disableArmPhysics = false;
        public boolean disableNativeItemPhysics = false;
        public boolean disableBoatMinecartRaftModels = false;
        public boolean disablePistonModels = false;
        public boolean disableChestModels = false;
        public boolean disableEnchantingTableModels = false;
        public boolean disableBoatFirstPersonAnimations = false;
        public boolean disableEnhancedFireArmEffects = false;
        public boolean enableCrawlAnimation = true;
        public boolean enableClimbAnimation = true;
        public boolean enableSwimAnimation = true;
        public boolean enableElytraFlightAnimation = true;
        public boolean enableCustomWalk = true;
        public boolean enableSprintArmSwing = true;
        public boolean enableFallArmAnimation = true;
        public float sprintArmSwingIntensity = 1.0f;
        public float sprintArmSwingSpeed = 0.0f;
        @SerializedName(value="enableFreezeShake", alternate={"disableFreezeShake"})
        public boolean enableFreezeShake = true;
        @SerializedName(value="enableFreezeArmOverlay", alternate={"disableFreezeArmOverlay"})
        public boolean enableFreezeArmOverlay = true;
        public boolean enableMud = true;
        public boolean enableSweat = true;
        public boolean enableBurn = true;
        public boolean enableParticles = true;
        public boolean enableModelPartsGlow = true;
        public boolean enableBlurGlowAtDark = false;
        public boolean firstPersonModelHideEnabled = true;
        public float firstPersonModelHidePitch = 60.0f;
        public List<String> itemBlacklist = new ArrayList<String>();
        public Map<String, Boolean> blacklistApplyDualHanded = new LinkedHashMap<String, Boolean>();
        public Map<String, String> animationPackSelections = new LinkedHashMap<String, String>();
        public Map<String, String> animationPackTypeSelections = new LinkedHashMap<String, String>();
        public Map<String, Boolean> mixSpecificForceOverride = new LinkedHashMap<String, Boolean>();
    }
}

