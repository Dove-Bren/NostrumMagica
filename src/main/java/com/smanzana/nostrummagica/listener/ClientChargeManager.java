package com.smanzana.nostrummagica.listener;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.SpellChargeTracker.SpellCharge;

import net.minecraft.world.item.ItemStack;

/**
 * Wrap up tracking of us (the client) charging and then casting a spell
 */
public class ClientChargeManager {
	
	public static class ClientSpellCharge {
		public final SpellCharge charge;
		public final ItemStack mainhandItem;
		public final ItemStack offhandItem;
		public final float chargeSpeed;
		
		public ClientSpellCharge(SpellCharge charge, ItemStack mainhand, ItemStack offhand, float displayRate) {
			this.charge = charge;
			this.mainhandItem = mainhand.copy();
			this.offhandItem = offhand.copy();
			this.chargeSpeed = displayRate;
		}
	}
	
	protected @Nullable ClientSpellCharge currentCharge;
	protected long chargeStartMS; // 
	
	public ClientChargeManager() {
		currentCharge = null;
	}
	
	public @Nullable ClientSpellCharge getCurrentCharge() {
		return this.currentCharge;
	}
	
	public void cancelCharge(boolean interrupted) {
		this.setCharge(null);
	}
	
	public void startCharging(ClientSpellCharge charge) {
		this.setCharge(charge);
	}
	
	protected void setCharge(@Nullable ClientSpellCharge charge) {
		final @Nullable ClientSpellCharge oldCharge = currentCharge;
		this.currentCharge = charge;
		this.chargeStartMS = System.currentTimeMillis();
		
		NostrumMagica.spellChargeTracker.setCharging(NostrumMagica.Proxy.getPlayer(), charge == null ? null : charge.charge);
		if (oldCharge == null && charge != null) {
			// start charging
			// FX?
		} else {
			// stop
			// FX?
		}
	}
	
	protected final long getFinishMS() {
		if (currentCharge == null) {
			return Long.MAX_VALUE;
		}
		
		// Adjust to MS. There are 20 ticks per second (divide by 20) and 1000 ms in a second (* by 1000)
		return chargeStartMS + (currentCharge.charge.duration() * (1000 / 20)); 
	}
	
	public boolean isDoneCharging() {
		return currentCharge != null && System.currentTimeMillis() >= getFinishMS();
	}
	
	public float getChargePercent() {
		if (currentCharge == null) {
			return 0f;
		}
		
		final long elapsedMS = System.currentTimeMillis() - chargeStartMS;
		return (float) ((double) elapsedMS / (double) (currentCharge.charge.duration() * (1000 / 20)));
	}
	
	public int getRemainingTicks() {
		if (currentCharge == null) {
			return 0;
		}
		
		final long remainingMS = getFinishMS() - System.currentTimeMillis();
		final int remainingTicks = (int) ((remainingMS + ((1000 / 20)-1)) / (1000 / 20));
		return remainingTicks;
	}

}
