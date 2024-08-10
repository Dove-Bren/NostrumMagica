package com.smanzana.nostrummagica.listener;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.PortalBlock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientPlayerListener extends PlayerListener {
	
	private boolean jumpPressedLastFrame;
	private boolean hasJump;
	
	public ClientPlayerListener() {
		super();
	}
	
	@SubscribeEvent
	public void onInputUpdate(InputUpdateEvent event) {
		if (!jumpPressedLastFrame && event.getMovementInput().jump) {
			final PlayerJumpEvent.Pre jumpEvent = new PlayerJumpEvent.Pre(event.getPlayer());
			MinecraftForge.EVENT_BUS.post(jumpEvent);
			
			if (jumpEvent.isConsumed()) {
				event.getMovementInput().jump = false;
			} else {
				hasJump = true;
			}
		}
		jumpPressedLastFrame = event.getMovementInput().jump;
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		final @Nullable ClientPlayerEntity player = mc.player;
		
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
}
