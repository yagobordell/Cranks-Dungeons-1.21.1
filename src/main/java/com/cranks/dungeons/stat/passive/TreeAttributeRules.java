package com.cranks.dungeons.stat.passive;

import java.util.Locale;
import java.util.Set;

/** Tree modifiers use supported mod attributes; vanilla counterparts never bypass that policy. */
public final class TreeAttributeRules {
    private TreeAttributeRules() {}

    public static boolean supported(String namespace, String path, Set<String> allowed) {
        return "cranks-dungeons".equals(namespace) && allowed.contains(path);
    }

    /** Accept a bare mod path or an explicitly mod-qualified choice, preserving namespace checks. */
    public static String normalizeChoice(String raw) {
        String choice = raw == null ? "" : raw.strip().toLowerCase(Locale.ROOT);
        int separator = choice.indexOf(':');
        if (separator < 0) return choice;
        if (!choice.substring(0, separator).equals("cranks-dungeons")) return null;
        String path = choice.substring(separator + 1);
        return path.isEmpty() || path.indexOf(':') >= 0 ? null : path;
    }
}
