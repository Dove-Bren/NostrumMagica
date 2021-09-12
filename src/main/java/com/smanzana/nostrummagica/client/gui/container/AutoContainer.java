package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nullable;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Container that automatically detects changes in backing inventory's fields and syncs them.
 * @author Skyler
 *
 */
public abstract class AutoContainer extends Container {

	private final @Nullable IInventory inventory;
	private final int[] oldValues;
	
	public AutoContainer(@Nullable IInventory inventory) {
		this.inventory = inventory;
		
		if (inventory == null) {
			this.oldValues = new int[0];
		} else {
			this.oldValues = new int[inventory.getFieldCount()];
			
			for (int i = 0; i < oldValues.length; i++) {
				oldValues[i] = inventory.getField(i) - 1; // lol force an update
			}
		}
	}
	
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		if (inventory != null) {
			for (int i = 0; i < oldValues.length; i++) {
				int value = inventory.getField(i);
				if (value != oldValues[i]) {
					for (IContainerListener listener : this.listeners) {
						listener.sendWindowProperty(this, i, value);
					}
					oldValues[i] = value;
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int id, int data) {
		if (inventory != null) {
			inventory.setField(id, data);
		}
	}
}
