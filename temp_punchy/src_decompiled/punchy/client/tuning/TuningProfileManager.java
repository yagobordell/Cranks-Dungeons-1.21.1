/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 */
package punchy.client.tuning;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_310;
import punchy.config.PunchyTuningConfig;

public final class TuningProfileManager {
    private static final String EXT = ".json";
    private static final String ACTIVE_PROFILE_FILE = ".active_profile";

    private TuningProfileManager() {
    }

    static List<String> listProfiles() {
        ArrayList<String> out = new ArrayList<String>();
        Path dir = TuningProfileManager.profilesDir();
        try {
            Files.createDirectories(dir, new FileAttribute[0]);
            try (Stream<Path> stream = Files.list(dir);){
                stream.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).map(path -> path.getFileName().toString()).filter(name -> name.toLowerCase().endsWith(EXT)).map(name -> name.substring(0, name.length() - EXT.length())).sorted(Comparator.naturalOrder()).forEach(out::add);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return out;
    }

    static boolean saveCurrentAs(String name) {
        String safe = TuningProfileManager.sanitizeName(name);
        if (safe.isEmpty()) {
            return false;
        }
        Path target = TuningProfileManager.profilePath(safe);
        try {
            Files.createDirectories(target.getParent(), new FileAttribute[0]);
            boolean saved = PunchyTuningConfig.saveCurrentProfileDeltaToFile(target);
            if (!saved) {
                return false;
            }
            boolean ok = TuningProfileManager.applyProfileAsDefault(target, safe);
            return ok;
        }
        catch (IOException ignored) {
            return false;
        }
    }

    static boolean loadProfile(String name) {
        String safe = TuningProfileManager.sanitizeName(name);
        if (safe.isEmpty()) {
            return false;
        }
        Path source = TuningProfileManager.profilePath(safe);
        boolean ok = TuningProfileManager.applyProfileAsDefault(source, safe);
        return ok;
    }

    static boolean deleteProfile(String name) {
        String safe = TuningProfileManager.sanitizeName(name);
        if (safe.isEmpty()) {
            return false;
        }
        try {
            boolean deleted = Files.deleteIfExists(TuningProfileManager.profilePath(safe));
            return deleted;
        }
        catch (IOException ignored) {
            return false;
        }
    }

    static boolean importProfile(Path source, String targetName) {
        String safe;
        String fallback;
        if (source == null || !Files.isRegularFile(source, new LinkOption[0])) {
            return false;
        }
        String string = fallback = source.getFileName() == null ? "" : source.getFileName().toString();
        if (fallback.toLowerCase().endsWith(EXT)) {
            fallback = fallback.substring(0, fallback.length() - EXT.length());
        }
        if ((safe = TuningProfileManager.sanitizeName(targetName == null || targetName.isBlank() ? fallback : targetName)).isEmpty()) {
            return false;
        }
        Path target = TuningProfileManager.profilePath(safe);
        try {
            Files.createDirectories(target.getParent(), new FileAttribute[0]);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            boolean ok = TuningProfileManager.applyProfileAsDefault(target, safe);
            return ok;
        }
        catch (IOException ignored) {
            return false;
        }
    }

    static String getActiveProfileName() {
        Path marker = TuningProfileManager.activeProfilePath();
        try {
            if (!Files.isRegularFile(marker, new LinkOption[0])) {
                return null;
            }
            String raw = Files.readString(marker).trim();
            String safe = TuningProfileManager.sanitizeName(raw);
            return safe.isEmpty() ? null : safe;
        }
        catch (IOException ignored) {
            return null;
        }
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

    private static Path mainTuningPath() {
        return TuningProfileManager.punchyDir().resolve("punchy_tuning.json");
    }

    private static Path helperTuningPath() {
        return TuningProfileManager.punchyDir().resolve("creator_tuning_helper.json");
    }

    private static Path existingSourcePath() {
        Path main = TuningProfileManager.mainTuningPath();
        if (Files.exists(main, new LinkOption[0])) {
            return main;
        }
        Path helper = TuningProfileManager.helperTuningPath();
        if (Files.exists(helper, new LinkOption[0])) {
            return helper;
        }
        return null;
    }

    private static Path profilesDir() {
        return TuningProfileManager.punchyDir().resolve("profiles");
    }

    static Path profilesDirectoryPath() {
        Path dir = TuningProfileManager.profilesDir();
        try {
            Files.createDirectories(dir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return dir;
    }

    static boolean unloadActiveProfileToDefault() {
        Path main = TuningProfileManager.mainTuningPath();
        Path marker = TuningProfileManager.activeProfilePath();
        try {
            Files.deleteIfExists(main);
            Files.deleteIfExists(marker);
            class_310 mc = class_310.method_1551();
            PunchyTuningConfig.reloadActiveProfileFast(mc != null ? mc.method_1478() : null);
            return true;
        }
        catch (IOException ignored) {
            return false;
        }
    }

    private static Path activeProfilePath() {
        return TuningProfileManager.profilesDir().resolve(ACTIVE_PROFILE_FILE);
    }

    public static Path getActiveProfileFilePath() {
        String active = TuningProfileManager.getActiveProfileName();
        if (active == null || active.isBlank()) {
            return null;
        }
        Path profile = TuningProfileManager.profilePath(active);
        return Files.isRegularFile(profile, new LinkOption[0]) ? profile : null;
    }

    private static Path profilePath(String name) {
        return TuningProfileManager.profilesDir().resolve(name + EXT);
    }

    private static Path punchyDir() {
        class_310 mc = class_310.method_1551();
        Path game = mc != null ? mc.field_1697.toPath() : Path.of(".", new String[0]);
        return game.resolve("config").resolve("punchy");
    }

    private static boolean applyProfileAsDefault(Path sourceProfile, String profileName) {
        Path target = TuningProfileManager.mainTuningPath();
        try {
            if (sourceProfile == null || !Files.exists(sourceProfile, new LinkOption[0])) {
                return false;
            }
            Files.createDirectories(target.getParent(), new FileAttribute[0]);
            Files.copy(sourceProfile, target, StandardCopyOption.REPLACE_EXISTING);
            Files.createDirectories(TuningProfileManager.activeProfilePath().getParent(), new FileAttribute[0]);
            Files.writeString(TuningProfileManager.activeProfilePath(), (CharSequence)TuningProfileManager.sanitizeName(profileName), new OpenOption[0]);
            class_310 mc = class_310.method_1551();
            PunchyTuningConfig.reloadActiveProfileFast(mc != null ? mc.method_1478() : null);
            return true;
        }
        catch (IOException ignored) {
            return false;
        }
    }
}

