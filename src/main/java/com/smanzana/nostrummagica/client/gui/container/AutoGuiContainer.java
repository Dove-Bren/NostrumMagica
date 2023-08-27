package com.smanzana.nostrummagica.client.gui.container;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

/**
 * Just adds the default render that used to be in GuiContainer
 * @author Skyler
 *
 */
public abstract class AutoGuiContainer extends GuiContainer {

	public AutoGuiContainer(Container inventorySlotsIn) {
		super(inventorySlotsIn);
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.render(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

}
