package com.smanzana.nostrummagica.client.gui.container;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;

public class HideableSlot extends Slot {

	protected boolean hidden;
	
	public HideableSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
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
