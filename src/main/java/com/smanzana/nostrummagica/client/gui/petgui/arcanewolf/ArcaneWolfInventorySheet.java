package com.smanzana.nostrummagica.client.gui.petgui.arcanewolf;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.WolfBondCapability;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.sheet.PetInventorySheet;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

public class ArcaneWolfInventorySheet extends PetInventorySheet<EntityArcaneWolf> {
	
	public ArcaneWolfInventorySheet(EntityArcaneWolf wolf) {
		super(wolf, wolf.getInventory());
	}
	
	@Override
	public void showSheet(EntityArcaneWolf wolf, PlayerEntity player, IPetContainer<EntityArcaneWolf> container, int width, int height, int offsetX, int offsetY) {
		super.showSheet(wolf, player, container, width, height, offsetX, offsetY);
	}

	@Override
	public void draw(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		super.draw(matrixStackIn, mc, partialTicks, width, height, mouseX, mouseY);
	}

	@Override
	public boolean shouldShow(EntityArcaneWolf wolf, IPetContainer<EntityArcaneWolf> container) {
		return wolf.hasWolfCapability(WolfBondCapability.INVENTORY);
	}

}
