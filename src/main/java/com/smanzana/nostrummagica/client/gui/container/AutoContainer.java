package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.util.ContainerUtil.IAutoContainerInventory;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Container that automatically detects changes in backing inventory's fields and syncs them.
 * @author Skyler
 *
 */
public abstract class AutoContainer extends AbstractContainerMenu {

	private final @Nullable IAutoContainerInventory inventory;
	
	public AutoContainer(MenuType<? extends AutoContainer> type, int windowId, @Nullable IAutoContainerInventory inventory) {
		super(type, windowId);
		this.inventory = inventory;
		if (inventory != null) {
			this.addDataSlots(inventory);
		}
	}
	
	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void setData(int id, int data) {
		super.setData(id, data);
	}
}
