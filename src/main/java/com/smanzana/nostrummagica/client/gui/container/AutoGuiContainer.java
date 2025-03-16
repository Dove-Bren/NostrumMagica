package com.smanzana.nostrummagica.client.gui.container;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;

/**
 * Just adds the default render that used to be in GuiContainer
 * @author Skyler
 *
 */
public abstract class AutoGuiContainer<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

	protected final Minecraft mc;
	
	public AutoGuiContainer(T inventorySlotsIn, Inventory playerInv, Component name) {
		super(inventorySlotsIn, playerInv, name);
		mc = Minecraft.getInstance();
	}
	
	@Override
	public void render(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStackIn);
		super.render(matrixStackIn, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStackIn, mouseX, mouseY);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		// Try passing to widgets first
		for (AbstractWidget child : this.buttons) { // should be children?
			if (child.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
				//return true;
			}
		}
		
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

}
