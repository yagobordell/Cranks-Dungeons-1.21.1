package com.cranks.dungeons.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpatialGrid<T> {
    private final double cellSize;
    private final Map<Long, List<T>> grid = new HashMap<>();

    public SpatialGrid(double cellSize) {
        this.cellSize = cellSize;
    }

    public void clear() {
        grid.clear();
    }

    public void insert(T item, double x, double y) {
        long key = getCellKey(x, y);
        grid.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
    }

    public void insertLine(T item, double x1, double y1, double x2, double y2) {
        int minX = (int) Math.floor(Math.min(x1, x2) / cellSize);
        int maxX = (int) Math.floor(Math.max(x1, x2) / cellSize);
        int minY = (int) Math.floor(Math.min(y1, y2) / cellSize);
        int maxY = (int) Math.floor(Math.max(y1, y2) / cellSize);

        for (int cx = minX; cx <= maxX; cx++) {
            for (int cy = minY; cy <= maxY; cy++) {
                long key = packKey(cx, cy);
                grid.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
            }
        }
    }

    public List<T> query(double minX, double minY, double maxX, double maxY) {
        List<T> result = new ArrayList<>();
        int startX = (int) Math.floor(minX / cellSize);
        int endX = (int) Math.floor(maxX / cellSize);
        int startY = (int) Math.floor(minY / cellSize);
        int endY = (int) Math.floor(maxY / cellSize);

        for (int cx = startX; cx <= endX; cx++) {
            for (int cy = startY; cy <= endY; cy++) {
                long key = packKey(cx, cy);
                List<T> cell = grid.get(key);
                if (cell != null) {
                    for (T item : cell) {
                        if (!result.contains(item)) {
                            result.add(item);
                        }
                    }
                }
            }
        }
        return result;
    }

    private long getCellKey(double x, double y) {
        int cx = (int) Math.floor(x / cellSize);
        int cy = (int) Math.floor(y / cellSize);
        return packKey(cx, cy);
    }

    private long packKey(int cx, int cy) {
        return (((long) cx) << 32) | (cy & 0xFFFFFFFFL);
    }
}
