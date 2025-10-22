package main.java.com.team.game.target;

/**
 * Provides utility methods for projectile motion calculations used
 * in the Target game mode.
 * <p>
 * All methods in this class are static and purely mathematical,
 * offering physics-based formulas for computing projectile speed,
 * height, and hit detection.
 */
public final class TargetPhysics {

    /** Private constructor to prevent instantiation. */
    private TargetPhysics() {}

    /**
     * Calculates the initial speed required to hit a target at a given
     * horizontal distance and height, based on launch angle and gravity.
     *
     * @param g         gravitational acceleration (m/s²)
     * @param angleRad  launch angle in radians
     * @param x         horizontal distance to target (m)
     * @param y         vertical height of target (m)
     * @return the required initial launch speed (m/s)
     * @throws IllegalArgumentException if the given angle, x, or y
     *                                  make the target physically unreachable
     */
    public static double requiredSpeed(double g, double angleRad, double x, double y) {
        double cos_component = Math.cos(angleRad);
        double tan_component = Math.tan(angleRad);
        double denom = 2.0 * cos_component * cos_component * (x * tan_component - y);
        if (denom <= 0.0) {
            throw new IllegalArgumentException("Unreachable target for that angle/x/y");
        }
        return Math.sqrt((g * x * x) / denom);
    }

    /**
     * Computes the vertical position (y) of a projectile at a given horizontal
     * distance (x), based on launch velocity and angle.
     *
     * @param v         initial velocity (m/s)
     * @param angleRad  launch angle in radians
     * @param x         horizontal distance (m)
     * @param y0        starting height (m)
     * @param g         gravitational acceleration (m/s²)
     * @return the vertical position (m) at the given x position
     */
    public static double yAtX(double v, double angleRad, double x, double y0, double g) {
        double cos_component = Math.cos(angleRad);
        double sin_component = Math.sin(angleRad);

        double t = x / (v * cos_component);
        return y0 + v * sin_component * t - 0.5 * g * t * t;
    }

    /**
     * Determines whether a projectile's y-position intersects a circular target.
     *
     * @param yPxAtWall       the projectile’s y-coordinate at the wall (in pixels)
     * @param targetCenterPx  the target’s center y-coordinate (in pixels)
     * @param targetRadiusPx  the target’s radius (in pixels)
     * @return {@code true} if the projectile hits within the target radius; {@code false} otherwise
     */
    public static boolean isHit(double yPxAtWall, double targetCenterPx, double targetRadiusPx) {
        return Math.abs(yPxAtWall - targetCenterPx) <= targetRadiusPx;
    }
}
