package com.smanzana.nostrummagica.spells;

import com.smanzana.nostrummagica.spells.components.SpellAction;

/**
 * Describes a spell action that's about to take place.
 * When used with the PlayerListener interface, allows modification of effect efficiency.
 * @author Skyler
 *
 */
public class SpellActionSummary {

	private SpellAction action;
	
	private float efficiency;
	
	private boolean cancelled;
	
	public SpellActionSummary(SpellAction action, float efficiency) {
		this.action = action;
		this.efficiency = efficiency;
		
		cancelled = false;
	}
	
	public void cancel() {
		this.cancelled = true;
	}
	
	public void addEfficiency(float delta) {
		this.efficiency += delta;
	}
	
	public void setEfficiency(float newValue) {
		this.efficiency = newValue;
	}
	
	public final SpellAction getAction() {
		return this.action;
	}
	
	public boolean wasCancelled() {
		return this.cancelled;
	}
	
	public float getEfficiency() {
		return this.efficiency;
	}
	
}
