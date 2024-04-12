package com.smanzana.nostrummagica.client.effects;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifier;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Effect made by displaying different effects based on the time
 * @author Skyler
 *
 */
public class ClientEffectAnimated extends ClientEffect {
	
	public static final class AnimationFrame {
		public final ClientEffect effect;
		public final float endTime;
		
		public AnimationFrame(ClientEffect effect, float endTime) {
			super();
			this.effect = effect;
			this.endTime = endTime;
		}
		
		protected static final AnimationFrame[] MakeFrames(ClientEffect[] effects, float[] timings) {
			if (effects == null || timings == null || effects.length != timings.length) {
				throw new RuntimeException("Cannot make animated effect frames: effects and timings arrays are different sizes!");
			}
			
			final AnimationFrame[] frames = new AnimationFrame[effects.length];
			for (int i = 0; i < effects.length; i++) {
				if (timings[i] < 0f || timings[i] > 1f) {
					throw new RuntimeException("Cannot make animated effect frames: frame end time " + i + " out of bounds (" + timings[i] + ")");
				}
				
				frames[i] = new AnimationFrame(effects[i], timings[i]);
			}
			return frames;
		}
	}

	private final AnimationFrame[] frames;
	
	public ClientEffectAnimated(Vector3d origin, int ticks, AnimationFrame ... frames) {
		super(origin, null, ticks);
		this.frames = frames;
	}
	
	public ClientEffectAnimated(Vector3d origin, int ticks, ClientEffect[] effects, float[] timings) {
		this(origin, ticks, AnimationFrame.MakeFrames(effects, timings));
	}
	
	public ClientEffectAnimated(Vector3d origin, long ms, AnimationFrame[] frames) {
		super(origin, null, ms);
		this.frames = frames;
	}
	
	public ClientEffectAnimated(Vector3d origin, long ms, ClientEffect[] effects, float[] timings) {
		this(origin, ms, AnimationFrame.MakeFrames(effects, timings));
	}
	
	protected @Nullable AnimationFrame getFrame(float progress) {
		for (int i = 0; i < frames.length; i++) {
			if (frames[i].endTime >= progress) {
				return frames[i];
			}
		}
		
		return null;
	}
	
	@Override
	protected void drawForm(ClientEffectRenderDetail detail, Minecraft mc, float progress, float partialTicks) {
		// Figure out which frame we're on.
		// I'm tempted to make this keep a little local int so we don't have to scan through each time, but am also interested
		// in keeping 'progress' absolute. Maybe a little int of the last index AND the last float progress we cached off of?
		// Although... I don't remember whether a new instance of these is created for each render?
		final @Nullable AnimationFrame frame = getFrame(progress);
		if (frame == null) {
			return;
		}
		
		if (!this.modifiers.isEmpty())
		for (ClientEffectModifier mod : modifiers) {
			mod.apply(detail, progress, partialTicks);
		}
		
		frame.effect.drawForm(detail, mc, partialTicks, detail.getColor());
	}
	
}
