package com.meteoraddon.addon.utils;

import net.minecraft.util.math.Vec3d;

public class MathUtils {
    /**
     * Clamps a value between min and max.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamps an integer value between min and max.
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Calculates the squared distance between two Vec3d points.
     */
    public static double distanceSquared(Vec3d a, Vec3d b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        double dz = a.z - b.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Calculates the distance between two Vec3d points.
     */
    public static double distance(Vec3d a, Vec3d b) {
        return Math.sqrt(distanceSquared(a, b));
    }

    /**
     * Checks if a value is within a range (inclusive).
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
}
