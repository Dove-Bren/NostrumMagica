package com.smanzana.nostrummagica.listener;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Emitted on client side when the jump input is pressed (not held down or repeated)
 */
public abstract class PlayerJumpEvent extends PlayerEvent {

	protected boolean consumed;
	
	public PlayerJumpEvent(PlayerEntity player) {
		super(player);
	}
	
	/**
	 * Sent right after updating movement input (so before regular client entity processing).
	 * If consumed, will actually remove the jump input on the client side, preventing it from seeing
	 * the jump press this tick. This is useful for doing things like prevent elytra flying or horse jumping.
	 */
	public static class Pre extends PlayerJumpEvent {
		public Pre(PlayerEntity player) {
			super(player);
		}

		public boolean isConsumed() {
			return consumed;
		}
		
		public void consume() {
			consumed = true;
		}
	}
	
	/**
	 * Called at the end of the client tick when jump as PRESSED this tick.
	 */
	public static class Post extends PlayerJumpEvent {
		public Post(PlayerEntity player) {
			super(player);
		}
	}
}
