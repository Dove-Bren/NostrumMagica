package com.smanzana.nostrummagica.listener;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.block.PortalBlock;
import com.smanzana.nostrummagica.capabilities.IBonusJumpCapability;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientPlayerListener extends PlayerListener {
	
	// Whether jump was or wasn't pressed last frame
	private boolean jumpPressedLastFrame;
	
	// Whether the last false->true jump state transition was cancelled
	private boolean jumpConsumedThisPress;
	
	// Whether this current frame has a jump that was unconsumed
	private boolean hasJump;
	
	public ClientPlayerListener() {
		super();
	}
	
	@SubscribeEvent
	public void onInputUpdate(InputUpdateEvent event) {
		final boolean newPress = !jumpPressedLastFrame && event.getMovementInput().jumping;
		jumpPressedLastFrame = event.getMovementInput().jumping;
		// 
		
		if (newPress) {
			jumpConsumedThisPress = false;
			final PlayerJumpEvent.Pre jumpEvent = new PlayerJumpEvent.Pre(event.getPlayer());
			MinecraftForge.EVENT_BUS.post(jumpEvent);
			
			if (jumpEvent.isConsumed()) {
				jumpConsumedThisPress = true;
			} else {
				hasJump = true;
			}
		}
		
		if (jumpConsumedThisPress) {
			// Keep eating the jump so that it never appears to transition to on in the regular player loop
			event.getMovementInput().jumping = false;
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		final @Nullable LocalPlayer player = mc.player;
		
		if (event.phase == Phase.START) {
			if (mc.player != null) {
				PortalBlock.clientTick();
				//TeleportRune.tick();
			}
		} else if (event.phase == Phase.END) {
			if (hasJump) {
				MinecraftForge.EVENT_BUS.post(new PlayerJumpEvent.Post(player));
			}
			hasJump = false;
		}
	}
	
	@SubscribeEvent
	public void onJumpInput(PlayerJumpEvent.Pre event) {
		// Evaluate double jump
		final LocalPlayer player = (LocalPlayer) event.getPlayer();
		if (!event.isConsumed() && !player.isOnGround()) {
			final int extraJumps = (int) event.getPlayer().getAttributeValue(NostrumAttributes.bonusJump);
			if (extraJumps > 0) {
				final @Nullable IBonusJumpCapability jumps = NostrumMagica.getBonusJump(player);
				if (jumps != null) {
					if (jumps.getCount() < extraJumps) {
						jumps.incrCount();
						player.jumpFromGround();
						event.consume();
					}
				}
			}
		}
	}
}
