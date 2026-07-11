/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.minecraft.class_310
 */
package punchy.client.tuning;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_310;
import punchy.config.PunchyConfig;
import punchy.config.PunchyTuningConfig;

final class MixProfileManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String EXT = ".json";
    private static final String ACTIVE_PROFILE_FILE = ".active_mix_profile";

    private MixProfileManager() {
    }

    static List<String> listProfiles() {
        ArrayList<String> out = new ArrayList<String>();
        Path dir = MixProfileManager.profilesDir();
        try {
            Files.createDirectories(dir, new FileAttribute[0]);
            try (Stream<Path> stream = Files.list(dir);){
                stream.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).map(path -> path.getFileName().toString()).filter(name -> name.toLowerCase(Locale.ROOT).endsWith(EXT)).map(name -> name.substring(0, name.length() - EXT.length())).sorted(Comparator.naturalOrder()).forEach(out::add);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return out;
    }

    static boolean saveCurrentAs(String name) {
        String safe = MixProfileManager.sanitizeName(name);
        if (safe.isEmpty()) {
            return false;
        }
        Path target = MixProfileManager.profilePath(safe);
        try {
            Files.createDirectories(target.getParent(), new FileAttribute[0]);
            MixProfileData data = MixProfileManager.readFromCurrentConfig();
            try (BufferedWriter writer = Files.newBufferedWriter(target, new OpenOption[0]);){
                GSON.toJson((Object)data, (Appendable)writer);
            }
            Files.createDirectories(MixProfileManager.activeProfilePath().getParent(), new FileAttribute[0]);
            Files.writeString(MixProfileManager.activeProfilePath(), (CharSequence)safe, new OpenOption[0]);
            return true;
        }
        catch (IOException ignored) {
            return false;
        }
    }

    static boolean loadProfile(String name) {
        String safe = MixProfileManager.sanitizeName(name);
        if (safe.isEmpty()) {
            return false;
        }
        Path source = MixProfileManager.profilePath(safe);
        return MixProfileManager.loadProfileInternal(safe, source, true);
    }

    static boolean deleteProfile(String name) {
        String safe = MixProfileManager.sanitizeName(name);
        if (safe.isEmpty()) {
            return false;
        }
        try {
            boolean deleted = Files.deleteIfExists(MixProfileManager.profilePath(safe));
            String active = MixProfileManager.getActiveProfileName();
            if (active != null && active.equals(safe)) {
                Files.deleteIfExists(MixProfileManager.activeProfilePath());
            }
            return deleted;
        }
        catch (IOException ignored) {
            return false;
        }
    }

    static String getActiveProfileName() {
        Path marker = MixProfileManager.activeProfilePath();
        try {
            if (!Files.isRegularFile(marker, new LinkOption[0])) {
                return null;
            }
            String raw = Files.readString(marker).trim();
            String safe = MixProfileManager.sanitizeName(raw);
            return safe.isEmpty() ? null : safe;
        }
        catch (IOException ignored) {
            return null;
        }
    }

    static Path profilesDirectoryPath() {
        Path dir = MixProfileManager.profilesDir();
        try {
            Files.createDirectories(dir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return dir;
    }

    static boolean unloadActiveProfileToDefault() {
        Path marker = MixProfileManager.activeProfilePath();
        try {
            Files.deleteIfExists(marker);
            class_310 mc = class_310.method_1551();
            PunchyTuningConfig.reloadActiveProfileFast(mc != null ? mc.method_1478() : null);
            return true;
        }
        catch (IOException ignored) {
            return false;
        }
    }

    static boolean restoreActiveProfileForStartup() {
        String active = MixProfileManager.getActiveProfileName();
        if (active == null || active.isBlank()) {
            return false;
        }
        Path source = MixProfileManager.profilePath(active);
        return MixProfileManager.loadProfileInternal(active, source, false);
    }

    private static boolean loadProfileInternal(String safeName, Path source, boolean reloadNow) {
        boolean bl;
        block10: {
            if (safeName == null || safeName.isBlank() || source == null || !Files.isRegularFile(source, new LinkOption[0])) {
                return false;
            }
            BufferedReader reader = Files.newBufferedReader(source);
            try {
                MixProfileData data = (MixProfileData)GSON.fromJson((Reader)reader, MixProfileData.class);
                MixProfileManager.applyToCurrentConfig(data);
                Files.createDirectories(MixProfileManager.activeProfilePath().getParent(), new FileAttribute[0]);
                Files.writeString(MixProfileManager.activeProfilePath(), (CharSequence)safeName, new OpenOption[0]);
                if (reloadNow) {
                    class_310 mc = class_310.method_1551();
                    PunchyTuningConfig.reloadActiveProfileFast(mc != null ? mc.method_1478() : null);
                }
                bl = true;
                if (reader == null) break block10;
            }
            catch (Throwable throwable) {
                try {
                    if (reader != null) {
                        try {
                            ((Reader)reader).close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception ignored) {
                    return false;
                }
            }
            ((Reader)reader).close();
        }
        return bl;
    }

    private static MixProfileData readFromCurrentConfig() {
        PunchyConfig.Data snapshot = PunchyConfig.snapshot();
        MixProfileData data = new MixProfileData();
        if (snapshot.animationPackSelections != null) {
            data.animationPackSelections.putAll(snapshot.animationPackSelections);
        }
        if (snapshot.animationPackTypeSelections != null) {
            data.animationPackTypeSelections.putAll(snapshot.animationPackTypeSelections);
        }
        if (snapshot.mixSpecificForceOverride != null) {
            data.mixSpecificForceOverride.putAll(snapshot.mixSpecificForceOverride);
        }
        return data;
    }

    private static void applyToCurrentConfig(MixProfileData data) {
        PunchyConfig.Data snapshot = PunchyConfig.snapshot();
        snapshot.animationPackSelections = new LinkedHashMap<String, String>();
        snapshot.animationPackTypeSelections = new LinkedHashMap<String, String>();
        snapshot.mixSpecificForceOverride = new LinkedHashMap<String, Boolean>();
        if (data != null) {
            if (data.animationPackSelections != null) {
                snapshot.animationPackSelections.putAll(data.animationPackSelections);
            }
            if (data.animationPackTypeSelections != null) {
                snapshot.animationPackTypeSelections.putAll(data.animationPackTypeSelections);
            }
            if (data.mixSpecificForceOverride != null) {
                snapshot.mixSpecificForceOverride.putAll(data.mixSpecificForceOverride);
            }
        }
        PunchyConfig.applyAndSave(snapshot);
    }

    private static String sanitizeName(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
        while (cleaned.startsWith(".")) {
            cleaned = cleaned.substring(1);
        }
        return cleaned;
    }

    private static Path profilesDir() {
        class_310 mc = class_310.method_1551();
        Path game = mc != null ? mc.field_1697.toPath() : Path.of(".", new String[0]);
        return game.resolve("config").resolve("punchy").resolve("mix_profiles");
    }

    private static Path profilePath(String name) {
        return MixProfileManager.profilesDir().resolve(name + EXT);
    }

    private static Path activeProfilePath() {
        return MixProfileManager.profilesDir().resolve(ACTIVE_PROFILE_FILE);
    }

    private static final class MixProfileData {
        LinkedHashMap<String, String> animationPackSelections = new LinkedHashMap();
        LinkedHashMap<String, String> animationPackTypeSelections = new LinkedHashMap();
        LinkedHashMap<String, Boolean> mixSpecificForceOverride = new LinkedHashMap();

        private MixProfileData() {
        }
    }
}

