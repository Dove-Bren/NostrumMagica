package com.smanzana.nostrummagica.util;

import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

public final class Curves {

	private static Vector3d bezierInternal(float progress, Vector3d points[], int start, int end) {
		// base case: 1 point, in which case we just return ourself
		if (start == end) {
			return points[start];
		}
		
		// Blend between bezier of 1 fewer node lower and 1 more higher
		Vector3d blendStart = bezierInternal(progress, points, start, end - 1);
		Vector3d blendEnd = bezierInternal(progress, points, start + 1, end);
		return new Vector3d(
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
	public static Vector3d bezier(float progress, Vector3d ... points) {
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
	public static Vector2f alignedArc2D(float progress, double radius, boolean flip) {
		double relX = Math.cos(progress * 2 * Math.PI);
		double relY = Math.sin(progress * 2 * Math.PI);
		
		if (flip) {
			double r = relX;
			relX = relY;
			relY = r;
		}
		return new Vector2f((float) (relX * radius), (float) (relY * radius));
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
	public static Vector2f alignedArc2D(float progress, Vector2f start, double radius, boolean flip) {
		// Find center that is 'radius' units less in X
		double radiusX = flip ? -radius : radius;
		Vector2f center = new Vector2f((float) (start.x - radiusX), start.y);
		return new Vector2f(
				center.x + (float) (Math.cos(progress * .5 * Math.PI) * radiusX),
				center.y + (float) (Math.sin(progress * .5 * Math.PI) * radius));
	}
	
	protected static double getMortarVerticalVelocity(Vector3d diff, double startHVelocity, double gravity) {
		// Distance of a projectile is hVel * timeAirborn. Solve for how long we want to be airborn.
		final double hDist = Vector3d.ZERO.distanceTo(new Vector3d(diff.x, 0, diff.z));
		final double desiredTime = hDist / startHVelocity;
		
		// y at any time t is  y0 + v0 * t - (1/2) * g * (t^2)
		// shifting to be "0 = " for quad equation, this is
		//  (y0 - y) + v0 * t - (1/2) * g * (t^2)
		// y0 - y is -diff.y
		//
		// Solving for v0 is
		// -v0 = ( (-diff.y) - (1/2) * g * (t^2) ) / t
		// which is
		// -v0 =  ((-diff.y) / t) - (1/2) * g * t
		
		return -((-diff.y / desiredTime) - (.5 * gravity * desiredTime));
	}
	
	/**
	 * Get what starting motion should be to shoot a projectile with gravity starting at start and ending at end.
	 * @param start
	 * @param end
	 * @param startHVelocity
	 * @param gravity
	 * @return
	 */
	public static Vector3d getMortarArcVelocity(Vector3d start, Vector3d end, double startHVelocity, double gravity) {
		// TODO have to adjust for tick time instead of continuous real time?
		
		if (gravity < 0) {
			gravity = -gravity; // should be magnitude of pull down
		}
		
		final Vector3d diff = end.subtract(start);
		
		final double vVel = getMortarVerticalVelocity(diff, startHVelocity, gravity);
		
		// Break hVel into x and z
		return (new Vector3d(diff.x, 0, diff.z).normalize().scale(startHVelocity)).add(0, vVel, 0);
	}
	
	public static Vector2f solveQuadraticEquation(float a, float b, float c) {
		// (-b +- sqrt(b^2 - 4ac))  /  2a
		final double sqrt = Math.sqrt(Math.pow(b, 2) - (4 * a * c));
		
		final double pos = (-b + sqrt) / (2 *a);
		final double neg = (-b - sqrt) / (2 *a);
		return new Vector2f((float) pos, (float) neg);
	}
	
	public static interface ICurve3d {
		public Vector3d getPosition(float progress);
	}
	
	public static class Bezier implements ICurve3d {
		public final Vector3d[] points;
		public Bezier(Vector3d ...points) {
			this.points = points;
		}
		
		@Override
		public Vector3d getPosition(float progress) {
			return Curves.bezier(progress, this.points);
		}
	}
	
	public static class Mortar implements ICurve3d {
		public final double startHVelocity;
		public final Vector3d diff;
		public final double gravity;
		
		private final double startingYVel;
		private final double flightTime;
		
		public Mortar(double startHVelocity, Vector3d diff, double gravity) {
			super();
			this.startHVelocity = startHVelocity;
			this.diff = diff;
			if (gravity < 0) {
				gravity = -gravity; // should be magnitude of pull down
			}
			this.gravity = gravity;
			this.startingYVel = getMortarVerticalVelocity(diff, startHVelocity, gravity);
			
			final double hDist = Vector3d.ZERO.distanceTo(new Vector3d(diff.x, 0, diff.z));
			this.flightTime = hDist / startHVelocity;
		}

		@Override
		public Vector3d getPosition(float progress) {
			// X and Z are easy as it's just linear interpolation based on progress.
			final double x = this.diff.getX() * progress;
			final double z = this.diff.getZ() * progress;
			
			// Y at any time t is  y0 + v0 * t - (1/2) * g * (t^2)
			final double time = flightTime * progress;
			final double y = 0 + (this.startingYVel * time) - (.5 * gravity * (time * time));
			return new Vector3d(x, y, z);
		}
		
	}
	
	public static class FlatEllipse implements ICurve3d {
		
		public final double radiusX;
		public final double radiusZ;
		
		public FlatEllipse(double radiusX, double radiusZ) {
			this.radiusX = radiusX;
			this.radiusZ = radiusZ;
		}

		@Override
		public Vector3d getPosition(float progress) {
			return new Vector3d(
					Math.cos(Math.PI * 2 * progress) * radiusX,
					0,
					Math.sin(Math.PI * 2 * progress) * radiusZ
					);
		}
	}
	
}
