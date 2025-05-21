package com.smanzana.nostrummagica.client.particles.ribbon;

import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonEmitter.EmitterData;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonEmitter.ISegment;
import com.smanzana.nostrummagica.client.particles.ribbon.RibbonEmitter.SegmentData;
import com.smanzana.nostrummagica.util.Color;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;

public final class RibbonSegmentTypes {

	public static record StaticSegment(Vec3 position, Vector3f spanDirection, Color color, float width, float v) implements ISegment {
		@Override
		public Vec3 getPosition(Camera camera, EmitterData emitter) {
			return position();
		}
		@Override
		public Vector3f getSpanDirection(Camera camera, EmitterData emitter, SegmentData ribbonData) {
			return spanDirection();
		}
		@Override
		public Color getColor(Camera camera, EmitterData emitter, SegmentData ribbonData) {
			return color();
		}
		@Override
		public float getWidth(Camera camera, EmitterData emitter, SegmentData ribbonData) {
			return width();
		}
		@Override
		public float getV(Camera camera, EmitterData emitter, SegmentData ribbonData) {
			return v();
		}
		@Override
		public float getU(Camera camera, EmitterData emitter, SegmentData ribbonData, boolean leftEdge) {
			return leftEdge ? 0 : 1;
		}
	}
	
	public static interface CameraFacingSegmentMixin extends ISegment {
		
		@Override
		public default Vector3f getSpanDirection(Camera camera, EmitterData emitter, SegmentData ribbonData) {
			// Camera look direction and posDiff direction can be cross'ed to find what we want the span direction to be
			Vector3f ret = new Vector3f(this.getPosition(camera, emitter).subtract(camera.getPosition()));
			Vector3f dir = ribbonData.segmentLength.copy();
			dir.normalize();
			ret.normalize();
			ret.cross(dir);
			return ret;
			//return camera.lo();
		}
	}
	
	public static abstract class LifetimeSegment implements ISegment {
		
		protected final float startTicks;
		protected final float startProg;

		protected final float lifetimeTicks;
		protected final float lifetimeProg;
		
		public LifetimeSegment(float startProg, float lifetimeProg, boolean roundDown) {
			this.startProg = startProg;
			this.lifetimeProg = roundDown ? Math.min(lifetimeProg, 1f - startProg) : lifetimeProg;
			
			this.startTicks = -1;
			this.lifetimeTicks = 0;
		}
		
		public LifetimeSegment(int startTicks, int lifetimeTicks, int totalTicks, float partialTicks, boolean roundDown) {
			this.startTicks = startTicks + partialTicks;
			this.lifetimeTicks = roundDown ? Math.min(lifetimeTicks, totalTicks - startTicks) : lifetimeTicks;
			
			this.startProg = -1f;
			this.lifetimeProg = 0;
		}

		public float getLifeProgress(EmitterData emitter) {
			if (this.startTicks == -1) {
				final float diff = emitter.emitterAgeProgress - this.startProg; // partial tick already factored in
				return Math.min(1f, diff / this.lifetimeProg);
			} else {
				final float diff = (emitter.emitterAge + emitter.partialTicks) - this.startTicks;
				return Math.min(1f, diff / (float) this.lifetimeTicks);
			}
		}
		
		@Override
		public boolean canRemove(EmitterData emitter) {
			// Remove once lifetime is over
			if (this.startTicks == -1) {
				final float diff = emitter.emitterAgeProgress - this.startProg;
				return diff >= lifetimeProg;
			} else {
				final float diff = (emitter.emitterAge + emitter.partialTicks) - this.startTicks;;
				return diff > this.lifetimeTicks;
			}
		}
	}
	
	/**
	 * Intended to be mixed on top of a LifetimeSegment. Fades color and width in and out according to fade params.
	 */
	public static interface FadingSegmentMixin extends ISegment {
		
		public Color getBaseColor(Camera camera, EmitterData emitter, SegmentData ribbonData);
		public float getBaseWidth(Camera camera, EmitterData emitter, SegmentData ribbonData);
		public float getLifeProgress(EmitterData emitter);
		
		public float getFadeInProg(EmitterData emitter, SegmentData ribbonData); // .2 => fade in during first 20% of life
		public float getFadeOutProg(EmitterData emitter, SegmentData ribbonData); // .2 => fade out over last 20% of life
		
		@Override
		public default Color getColor(Camera camera, EmitterData emitter, SegmentData ribbonData) {
			final Color base = this.getBaseColor(camera, emitter, ribbonData);
			final float prog = this.getLifeProgress(emitter);
			
			// First will always be 0
			if (ribbonData.segmentIdxFromTail == 0) {
				return base.scaleAlpha(0f);
			}
			
			final float fadeIn = getFadeInProg(emitter, ribbonData);
			final float fadeOut = getFadeOutProg(emitter, ribbonData);
			
			if (prog < fadeIn) {
				return base.scaleAlpha(prog / fadeIn);
			} else if (prog > 1f - fadeOut) {
				return base.scaleAlpha(1f - ((prog - (1f - fadeOut)) / fadeOut));
			}
			return base;
		}
		
		@Override
		public default float getWidth(Camera camera, EmitterData emitter, SegmentData ribbonData) {
			final float prog = this.getLifeProgress(emitter);
			if (ribbonData.segmentIdxFromTail == 0) {
				return 0f;
			}
			
			final float base = this.getBaseWidth(camera, emitter, ribbonData);
			final float fadeIn = getFadeInProg(emitter, ribbonData);
			final float fadeOut = getFadeOutProg(emitter, ribbonData);
			
			if (prog < fadeIn) {
				return base * (prog / fadeIn);
			} else if (prog > (1f - fadeOut)) {
				return base * (1f - ((prog - (1f - fadeOut)) / fadeOut));
			}
			return base;
		}
		
	}
	
	/**
	 * Provides v for texturing based on ribbon length.
	 * Specifically, based on distance from the head of the ribbon
	 */
	public static interface RibbonTailTexSegmentMixin extends ISegment {
		
		/**
		 * How many full V ranges of the texture per world block of ribbon?
		 * AKA 1f means for every block of ribbon rendered, draw the texture once.
		 * .5f means stretch over 2 blocks worth of ribbon.
		 * @param emitter
		 * @param ribbonData
		 * @return
		 */
		public float getVScale(EmitterData emitter, SegmentData ribbonData);
		
		public default float getVOffset(EmitterData emitter, SegmentData ribbonData) { return 0f; }
		
		@Override
		public default float getV(Camera camera, EmitterData emitter, SegmentData ribbonData) {
			return (float) (getVOffset(emitter, ribbonData) + (ribbonData.ribbonLengthFromHead * getVScale(emitter, ribbonData)));
		}
	}
	
}
