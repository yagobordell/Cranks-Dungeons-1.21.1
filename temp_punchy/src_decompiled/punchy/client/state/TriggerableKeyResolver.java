/*
 * Decompiled with CFR 0.152.
 */
package punchy.client.state;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class TriggerableKeyResolver {
    private static final Map<String, Integer> CACHE = new HashMap<String, Integer>();

    private TriggerableKeyResolver() {
    }

    static int resolveKeyCode(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return -1;
        }
        String normalized = TriggerableKeyResolver.normalize(rawKey);
        if (normalized.isEmpty()) {
            return -1;
        }
        Integer cached = CACHE.get(normalized);
        if (cached != null) {
            return cached;
        }
        int resolved = TriggerableKeyResolver.resolveNormalized(normalized);
        CACHE.put(normalized, resolved);
        return resolved;
    }

    static boolean matches(String rawKey, int keyCode) {
        if (keyCode < 0) {
            return false;
        }
        return TriggerableKeyResolver.resolveKeyCode(rawKey) == keyCode;
    }

    private static String normalize(String rawKey) {
        String key = rawKey.trim().toUpperCase(Locale.ROOT);
        if (key.startsWith("KEY.KEYBOARD.")) {
            key = key.substring("KEY.KEYBOARD.".length());
        } else if (key.startsWith("KEY.KEYPAD.")) {
            key = key.substring("KEY.KEYPAD.".length());
        }
        key = key.replace(" ", "").replace("-", "").replace("_", "");
        return key;
    }

    private static int resolveNormalized(String key) {
        if (key.length() == 1) {
            char c = key.charAt(0);
            if (c >= 'A' && c <= 'Z') {
                return 65 + (c - 65);
            }
            if (c >= '0' && c <= '9') {
                return 48 + (c - 48);
            }
        }
        if (key.startsWith("F")) {
            String num = key.substring(1);
            try {
                int n = Integer.parseInt(num);
                if (n >= 1 && n <= 25) {
                    return 290 + (n - 1);
                }
            }
            catch (NumberFormatException n) {
                // empty catch block
            }
        }
        if (key.startsWith("NUMPAD") || key.startsWith("KP")) {
            char c;
            String digits = key.replace("NUMPAD", "").replace("KP", "");
            if (digits.length() == 1 && (c = digits.charAt(0)) >= '0' && c <= '9') {
                return 320 + (c - 48);
            }
            if ("PLUS".equals(digits)) {
                return 334;
            }
            if ("MINUS".equals(digits)) {
                return 333;
            }
            if ("MULTIPLY".equals(digits)) {
                return 332;
            }
            if ("DIVIDE".equals(digits)) {
                return 331;
            }
            if ("DECIMAL".equals(digits) || "DOT".equals(digits)) {
                return 330;
            }
            if ("ENTER".equals(digits)) {
                return 335;
            }
        }
        return switch (key) {
            case "ESC", "ESCAPE" -> 256;
            case "TAB" -> 258;
            case "ENTER", "RETURN" -> 257;
            case "SPACE" -> 32;
            case "BACKSPACE" -> 259;
            case "INSERT", "INS" -> 260;
            case "DELETE", "DEL" -> 261;
            case "HOME" -> 268;
            case "END" -> 269;
            case "PAGEUP", "PGUP" -> 266;
            case "PAGEDOWN", "PGDN" -> 267;
            case "UP" -> 265;
            case "DOWN" -> 264;
            case "LEFT" -> 263;
            case "RIGHT" -> 262;
            case "LEFTSHIFT", "LSHIFT" -> 340;
            case "RIGHTSHIFT", "RSHIFT" -> 344;
            case "SHIFT" -> 340;
            case "LEFTCTRL", "LCTRL", "CONTROL" -> 341;
            case "RIGHTCTRL", "RCTRL" -> 345;
            case "LEFTALT", "LALT", "ALT" -> 342;
            case "RIGHTALT", "RALT" -> 346;
            case "CAPSLOCK" -> 280;
            case "SCROLLLOCK" -> 281;
            case "NUMLOCK" -> 282;
            case "PRINTSCREEN", "PRTSCR", "PRTSC" -> 283;
            case "PAUSE" -> 284;
            case "APOSTROPHE", "QUOTE" -> 39;
            case "COMMA" -> 44;
            case "PERIOD", "DOT" -> 46;
            case "SLASH" -> 47;
            case "SEMICOLON" -> 59;
            case "EQUAL", "PLUS" -> 61;
            case "MINUS", "DASH" -> 45;
            case "LEFTBRACKET", "LBRACKET" -> 91;
            case "RIGHTBRACKET", "RBRACKET" -> 93;
            case "BACKSLASH" -> 92;
            case "GRAVE", "TILDE" -> 96;
            default -> -1;
        };
    }
}

