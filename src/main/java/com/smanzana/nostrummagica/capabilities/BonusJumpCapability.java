package com.smanzana.nostrummagica.capabilities;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BonusJumpCapability implements IBonusJumpCapability {
	
	private static final String NBT_COUNT = "count";
	
	protected int jumpCount;
	
	public BonusJumpCapability() {
		jumpCount = 0;
	}
	
	@Override
	public int getCount() {
		return jumpCount;
	}

	@Override
	public void incrCount() {
		jumpCount++;
	}

	@Override
	public void resetCount() {
		jumpCount = 0;
	}

	@Override
	public void copy(IBonusJumpCapability source) {
		this.jumpCount = source.getCount();
	}
	
	@SubscribeEvent
	public static final void onClientTick(ClientTickEvent event) {
		// Want to just use landing event, but it's not triggered with things like slowfall and creative
		//public static final void onEntityLand(LivingFallEvent event) {
		
		if (event.phase != ClientTickEvent.Phase.END) {
			return;
		}
		
		final Player player = NostrumMagica.instance.proxy.getPlayer();
		if (player == null || !player.isOnGround()) {
			return;
		}
		@Nullable IBonusJumpCapability jumps = NostrumMagica.getBonusJump(player);
		if (jumps != null) {
			jumps.resetCount();
		}
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		
		nbt.putInt(NBT_COUNT, getCount());
		
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		resetCount();
		jumpCount = nbt.getInt(NBT_COUNT);
	}
}
