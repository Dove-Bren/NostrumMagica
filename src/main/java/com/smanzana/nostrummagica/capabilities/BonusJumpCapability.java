package com.smanzana.nostrummagica.capabilities;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BonusJumpCapability implements IBonusJumpCapability {
	
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
	
	public static class Serializer implements IStorage<IBonusJumpCapability> {
		
		public static final Serializer INSTANCE = new Serializer();
		
		private static final String NBT_COUNT = "count";
		
		protected Serializer() {
			
		}
	
		@Override
		public INBT writeNBT(Capability<IBonusJumpCapability> capability, IBonusJumpCapability instanceIn, Direction side) {
			BonusJumpCapability instance = (BonusJumpCapability) instanceIn;
			CompoundNBT nbt = new CompoundNBT();
			
			nbt.putInt(NBT_COUNT, instance.getCount());
			
			return nbt;
		}

		@Override
		public void readNBT(Capability<IBonusJumpCapability> capability, IBonusJumpCapability instanceIn, Direction side, INBT nbtIn) {
			BonusJumpCapability instance = (BonusJumpCapability) instanceIn;
			
			instance.resetCount();
			if (nbtIn.getId() == NBT.TAG_COMPOUND) {
				instance.jumpCount = ((CompoundNBT) nbtIn).getInt(NBT_COUNT);
			}
		}
	}
	
	@SubscribeEvent
	public static final void onClientTick(ClientTickEvent event) {
		// Want to just use landing event, but it's not triggered with things like slowfall and creative
		//public static final void onEntityLand(LivingFallEvent event) {
		
		if (event.phase != ClientTickEvent.Phase.END) {
			return;
		}
		
		final PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		if (player == null || !player.isOnGround()) {
			return;
		}
		@Nullable IBonusJumpCapability jumps = NostrumMagica.getBonusJump(player);
		if (jumps != null) {
			jumps.resetCount();
		}
	}
}
