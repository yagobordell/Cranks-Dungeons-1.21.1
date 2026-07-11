/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.minecraft.class_310
 */
package punchy.client.config;

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
import java.util.stream.Stream;
import net.minecraft.class_310;
import punchy.config.PunchyConfig;

final class ConfigProfileManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String EXTENSION = ".json";
    private static final String ACTIVE_PROFILE_FILE = "active_profile.txt";

    private ConfigProfileManager() {
    }

    static Path profilesDirectoryPath() {
        class_310 client = class_310.method_1551();
        Path configDir = client != null ? client.field_1697.toPath().resolve("config") : Path.of("config", new String[0]);
        return configDir.resolve("punchy").resolve("config_profiles");
    }

    static List<String> listProfiles() {
        Path dir = ConfigProfileManager.profilesDirectoryPath();
        ArrayList<String> result = new ArrayList<String>();
        try {
            Files.createDirectories(dir, new FileAttribute[0]);
            try (Stream<Path> stream = Files.list(dir);){
                stream.filter(path -> Files.isRegularFile(path, new LinkOption[0]) && path.getFileName().toString().endsWith(EXTENSION)).map(path -> ConfigProfileManager.stripExtension(path.getFileName().toString())).filter(name -> !name.isBlank()).sorted(Comparator.naturalOrder()).forEach(result::add);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return result;
    }

    static boolean saveCurrentAs(String profileName, PunchyConfig.Data data) {
        String safe = ConfigProfileManager.sanitizeProfileName(profileName);
        if (safe.isBlank() || data == null) {
            return false;
        }
        Path file = ConfigProfileManager.profilePath(safe);
        try {
            Files.createDirectories(file.getParent(), new FileAttribute[0]);
            PunchyConfig.Data snapshot = (PunchyConfig.Data)GSON.fromJson(GSON.toJson((Object)data), PunchyConfig.Data.class);
            if (snapshot == null) {
                return false;
            }
            snapshot.animationPackSelections = new LinkedHashMap<String, String>();
            snapshot.animationPackTypeSelections = new LinkedHashMap<String, String>();
            snapshot.mixSpecificForceOverride = new LinkedHashMap<String, Boolean>();
            try (BufferedWriter writer = Files.newBufferedWriter(file, new OpenOption[0]);){
                GSON.toJson((Object)snapshot, (Appendable)writer);
            }
            return true;
        }
        catch (IOException ignored) {
            return false;
        }
    }

    static PunchyConfig.Data load(String profileName) {
        PunchyConfig.Data data;
        block10: {
            String safe = ConfigProfileManager.sanitizeProfileName(profileName);
            if (safe.isBlank()) {
                return null;
            }
            Path file = ConfigProfileManager.profilePath(safe);
            if (!Files.isRegularFile(file, new LinkOption[0])) {
                return null;
            }
            BufferedReader reader = Files.newBufferedReader(file);
            try {
                data = (PunchyConfig.Data)GSON.fromJson((Reader)reader, PunchyConfig.Data.class);
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
                    return null;
                }
            }
            ((Reader)reader).close();
        }
        return data;
    }

    static boolean deleteProfile(String profileName) {
        String safe = ConfigProfileManager.sanitizeProfileName(profileName);
        if (safe.isBlank()) {
            return false;
        }
        Path file = ConfigProfileManager.profilePath(safe);
        try {
            String active;
            boolean deleted = Files.deleteIfExists(file);
            if (deleted && safe.equals(active = ConfigProfileManager.getActiveProfileName())) {
                ConfigProfileManager.setActiveProfileName(null);
            }
            return deleted;
        }
        catch (IOException ignored) {
            return false;
        }
    }

    static String getActiveProfileName() {
        Path file = ConfigProfileManager.activeProfilePath();
        if (!Files.isRegularFile(file, new LinkOption[0])) {
            return null;
        }
        try {
            String raw = Files.readString(file);
            String safe = ConfigProfileManager.sanitizeProfileName(raw);
            return safe.isBlank() ? null : safe;
        }
        catch (IOException ignored) {
            return null;
        }
    }

    static void setActiveProfileName(String profileName) {
        Path file = ConfigProfileManager.activeProfilePath();
        String safe = ConfigProfileManager.sanitizeProfileName(profileName);
        try {
            Files.createDirectories(file.getParent(), new FileAttribute[0]);
            if (safe.isBlank()) {
                Files.deleteIfExists(file);
                return;
            }
            Files.writeString(file, (CharSequence)safe, new OpenOption[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    static String sanitizeProfileName(String input) {
        if (input == null) {
            return "";
        }
        String value = input.trim();
        if (value.isBlank()) {
            return "";
        }
        value = value.replaceAll("[\\\\/:*?\"<>|]", "_");
        if ((value = value.replaceAll("\\s+", "_")).length() > 64) {
            value = value.substring(0, 64);
        }
        return value;
    }

    private static Path profilePath(String safeName) {
        return ConfigProfileManager.profilesDirectoryPath().resolve(safeName + EXTENSION);
    }

    private static Path activeProfilePath() {
        return ConfigProfileManager.profilesDirectoryPath().resolve(ACTIVE_PROFILE_FILE);
    }

    private static String stripExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        if (fileName.endsWith(EXTENSION)) {
            return fileName.substring(0, fileName.length() - EXTENSION.length());
        }
        return fileName;
    }
}

