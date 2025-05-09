package com.smanzana.nostrummagica.listener;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.SpellChargeTracker.SpellCharge;

/**
 * Wrap up tracking of us (the client) charging and then casting a spell
 */
public class ClientChargeManager {
	
	protected @Nullable SpellCharge currentCharge;
	protected long chargeStartMS; // 
	
	public ClientChargeManager() {
		currentCharge = null;
	}
	
	public @Nullable SpellCharge getCurrentCharge() {
		return this.currentCharge;
	}
	
	public void cancelCharge(boolean interrupted) {
		this.setCharge(null);
	}
	
	public void startCharging(SpellCharge charge) {
		this.setCharge(charge);
	}
	
	protected void setCharge(@Nullable SpellCharge charge) {
		final @Nullable SpellCharge oldCharge = currentCharge;
		this.currentCharge = charge;
		this.chargeStartMS = System.currentTimeMillis();
		
		NostrumMagica.spellChargeTracker.setCharging(NostrumMagica.instance.proxy.getPlayer(), charge);
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
		return chargeStartMS + (currentCharge.duration() * (1000 / 20)); 
	}
	
	public boolean isDoneCharging() {
		return currentCharge != null && System.currentTimeMillis() >= getFinishMS();
	}
	
	public float getChargePercent() {
		if (currentCharge == null) {
			return 0f;
		}
		
		final long elapsedMS = System.currentTimeMillis() - chargeStartMS;
		return (float) ((double) elapsedMS / (double) (currentCharge.duration() * (1000 / 20)));
	}
	
	public int getRemainingTicks() {
		if (currentCharge == null) {
			return 0;
		}
		
		final long remainingMS = getFinishMS() - System.currentTimeMillis();
		return (int) ((remainingMS + 19) / 20); // +19 rounds up
	}

}
