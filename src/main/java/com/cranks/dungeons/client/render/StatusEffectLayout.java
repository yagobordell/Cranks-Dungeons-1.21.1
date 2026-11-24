package com.cranks.dungeons.client.render;

import com.cranks.dungeons.config.StatusEffectOrientation;

import java.util.ArrayList;
import java.util.List;

/** Positions effects in pages containing at most two rows or columns. */
public final class StatusEffectLayout {
    public record Cell(int x, int y, int width, int height) {}
    public record Layout(List<Cell> cells, int width, int height, int anchorX, int anchorY) {}
    public record Page(int start, int end, Layout layout) {}
    public record Pages(List<Page> pages, int width, int height, int anchorX, int anchorY) {}

    private StatusEffectLayout() {}

    public static Pages paginate(List<Integer> heights, int size, int spacing, int maxPerLine,
                                 StatusEffectOrientation orientation, int maxWidth, int maxHeight,
                                 int columnDirection) {
        var full = arrange(heights, size, spacing, maxPerLine, orientation, maxWidth, maxHeight, columnDirection);
        var pages = new ArrayList<Page>();
        int start = 0, lines = 0, previousCross = Integer.MIN_VALUE;
        for (int index = 0; index < full.cells().size(); index++) {
            var cell = full.cells().get(index);
            int cross = orientation.isVertical() ? cell.x() : cell.y();
            if (cross != previousCross) {
                if (++lines > 2) {
                    pages.add(new Page(start, index, arrange(heights.subList(start, index), size, spacing,
                            maxPerLine, orientation, maxWidth, maxHeight, columnDirection)));
                    start = index;
                    lines = 1;
                }
                previousCross = cross;
            }
        }
        if (start < heights.size()) {
            pages.add(new Page(start, heights.size(), arrange(heights.subList(start, heights.size()), size, spacing,
                    maxPerLine, orientation, maxWidth, maxHeight, columnDirection)));
        }
        int left = 0, right = 0, top = 0, bottom = 0;
        for (var page : pages) {
            var layout = page.layout();
            left = Math.max(left, layout.anchorX());
            right = Math.max(right, layout.width() - layout.anchorX());
            top = Math.max(top, layout.anchorY());
            bottom = Math.max(bottom, layout.height() - layout.anchorY());
        }
        return new Pages(pages, left + right, top + bottom, left, top);
    }

    public static Layout arrange(List<Integer> heights, int size, int spacing, int maxPerLine,
                                 StatusEffectOrientation orientation, int maxWidth, int maxHeight,
                                 int columnDirection) {
        if (heights.isEmpty()) return new Layout(List.of(), 0, 0, 0, 0);
        int pitch = Math.max(size + 2, spacing);
        int gap = pitch - size;
        int limit = Math.max(1, maxPerLine);
        var cells = new ArrayList<Cell>(heights.size());
        int column = 0, rowY = 0, rowHeight = 0, withinLine = 0;
        int usedHeight = 0, columnBaseHeight = 0;
        int minX = 0, minY = 0, maxX = 0, maxY = 0;
        for (int height : heights) {
            int x, y;
            if (orientation.isHorizontal()) {
                if (withinLine >= limit || withinLine > 0 && withinLine * pitch + size > maxWidth) {
                    rowY += rowHeight + gap;
                    rowHeight = 0;
                    withinLine = 0;
                }
                x = withinLine * pitch * (orientation == StatusEffectOrientation.RIGHT_TO_LEFT ? -1 : 1);
                y = rowY;
                rowHeight = Math.max(rowHeight, height);
            } else {
                if (withinLine >= limit || withinLine > 0 && usedHeight + gap + height > maxHeight) {
                    column += columnDirection * pitch;
                    usedHeight = 0;
                    withinLine = 0;
                }
                if (withinLine == 0) columnBaseHeight = height;
                else usedHeight += gap;
                x = column;
                y = orientation == StatusEffectOrientation.BOTTOM_TO_TOP ? columnBaseHeight - usedHeight - height : usedHeight;
                usedHeight += height;
            }
            cells.add(new Cell(x, y, size, height));
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + size);
            maxY = Math.max(maxY, y + height);
            withinLine++;
        }
        var normalized = new ArrayList<Cell>(cells.size());
        for (var cell : cells) normalized.add(new Cell(cell.x() - minX, cell.y() - minY, size, cell.height()));
        return new Layout(normalized, maxX - minX, maxY - minY, -minX, -minY);
    }
}
