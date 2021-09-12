package com.smanzana.nostrummagica.utils;

import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public final class Curves {

	private static Vec3d bezierInternal(float progress, Vec3d points[], int start, int end) {
		// base case: 1 point, in which case we just return ourself
		if (start == end) {
			return points[start];
		}
		
		// Blend between bezier of 1 fewer node lower and 1 more higher
		Vec3d blendStart = bezierInternal(progress, points, start, end - 1);
		Vec3d blendEnd = bezierInternal(progress, points, start + 1, end);
		return new Vec3d(
				blendStart.x * (1 - progress) + blendEnd.x * progress,
				blendStart.y * (1 - progress) + blendEnd.y * progress,
				blendStart.z * (1 - progress) + blendEnd.z * progress
				);
	}
	
	/**
	 * Calculates a point along a bezier curve made up of the provided points.
	 * Progress is the % completion along the path.
	 * Points can have 2+ points in it. The first and last points will be the start and end, naturally.
	 * All other points are control points and may not be actually visitted.
	 * @param progress
	 * @param points
	 * @return
	 */
	public static Vec3d bezier(float progress, Vec3d ... points) {
		if (points == null || points.length < 2) {
			throw new RuntimeException("Invalid number of points for bezier curve");
		}
		
		return bezierInternal(progress, points, 0, points.length - 1);
	}
	
	/**
	 * Calculates a point along a perfect circle.
	 * The arc from 0 to 1 progress will form a circle from (radius, 0) and back in a CCW motion.
	 * If flip, produces the circle in a CW motion.
	 * @param progress
	 * @param start
	 * @param end
	 * @param flip
	 * @return
	 */
	public static Vec2f alignedArc2D(float progress, double radius, boolean flip) {
		double relX = Math.cos(progress * 2 * Math.PI);
		double relY = Math.sin(progress * 2 * Math.PI);
		
		if (flip) {
			double r = relX;
			relX = relY;
			relY = r;
		}
		return new Vec2f((float) (relX * radius), (float) (relY * radius));
	}
	
	/**
	 * Calculates points on a 90 degree arc from the start position around a circle of the provided radius.
	 * Defaults to moving CCW. Flip reverses this.
	 * @param progress
	 * @param start
	 * @param radius
	 * @param flip
	 * @return
	 */
	public static Vec2f alignedArc2D(float progress, Vec2f start, double radius, boolean flip) {
		// Find center that is 'radius' units less in X
		double radiusX = flip ? -radius : radius;
		Vec2f center = new Vec2f((float) (start.x - radiusX), start.y);
		return new Vec2f(
				center.x + (float) (Math.cos(progress * .5 * Math.PI) * radiusX),
				center.y + (float) (Math.sin(progress * .5 * Math.PI) * radius));
	}
	
}
