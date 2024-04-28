package com.smanzana.nostrummagica.client.gui.container;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;

/**
 * Just adds the default render that used to be in GuiContainer
 * @author Skyler
 *
 */
public abstract class AutoGuiContainer<T extends Container> extends ContainerScreen<T> {

	protected final Minecraft mc;
	
	public AutoGuiContainer(T inventorySlotsIn, PlayerInventory playerInv, ITextComponent name) {
		super(inventorySlotsIn, playerInv, name);
		mc = Minecraft.getInstance();
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStackIn);
		super.render(matrixStackIn, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStackIn, mouseX, mouseY);
	}

}
