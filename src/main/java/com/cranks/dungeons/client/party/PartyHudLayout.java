package com.cranks.dungeons.client.party;

/** Shared viewport fitting for gameplay party cards and their layout-editor preview. */
public final class PartyHudLayout {
    public record Layout(int x, int y, int rows, int columns, int width, int height, float scale) {}
    private PartyHudLayout() {}
    public static Layout fit(int screenWidth, int screenHeight, int x, int y,
                             int configuredRows, int configuredColumns, int members, float requestedScale) {
        int columns = Math.max(1, configuredColumns);
        int rows = Math.max(Math.max(1, configuredRows), Math.ceilDiv(Math.max(0, members), columns));
        int baseWidth = columns * 115, baseHeight = rows * 38;
        int availableWidth = Math.max(1, screenWidth - 4), availableHeight = Math.max(1, screenHeight - 4);
        float scale = Math.min(Math.max(.01f, requestedScale), Math.min((float)availableWidth / baseWidth, (float)availableHeight / baseHeight));
        int width = Math.min(availableWidth, (int)Math.ceil(baseWidth * scale));
        int height = Math.min(availableHeight, (int)Math.ceil(baseHeight * scale));
        return new Layout(Math.clamp(x < 0 ? 5 : x, 0, Math.max(0, screenWidth - width)),
                Math.clamp(y < 0 ? 190 : y, 0, Math.max(0, screenHeight - height)), rows, columns, width, height, scale);
    }
}
