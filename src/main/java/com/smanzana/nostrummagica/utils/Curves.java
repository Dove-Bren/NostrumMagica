package com.smanzana.nostrummagica.utils;

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
				blendStart.xCoord * (1 - progress) + blendEnd.xCoord * progress,
				blendStart.yCoord * (1 - progress) + blendEnd.yCoord * progress,
				blendStart.zCoord * (1 - progress) + blendEnd.zCoord * progress
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
	
}
