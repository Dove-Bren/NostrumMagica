package com.smanzana.nostrummagica.client.gui.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class HideableSlot extends Slot {

	protected boolean hidden;
	
	public HideableSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}
	
	@Override
	public boolean isActive() {
		return !hidden && super.isActive();
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

}
