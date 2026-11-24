package com.cranks.dungeons.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigFileUtil {
    private ConfigFileUtil() {}

    static void installDefault(Path target) throws IOException {
        String resource = "/data/cranks-dungeons/defaults/" + target.getFileName();
        try (var stream = ConfigFileUtil.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IOException("Missing bundled config " + resource);
            atomicWrite(target, new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    public static void atomicWrite(Path target, String content) throws IOException {
        Path parent = target.toAbsolutePath().getParent();
        if (parent == null) throw new IOException("Config path has no parent: " + target);
        Files.createDirectories(parent);

        Path temporary = Files.createTempFile(parent, target.getFileName().toString(), ".tmp");
        try {
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            if (Files.exists(target)) {
                Files.copy(target, target.resolveSibling(target.getFileName() + ".bak"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
