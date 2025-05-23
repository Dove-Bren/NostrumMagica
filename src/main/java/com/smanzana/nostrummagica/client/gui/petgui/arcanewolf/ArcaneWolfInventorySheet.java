package com.smanzana.nostrummagica.client.gui.petgui.arcanewolf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.WolfBondCapability;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.sheet.PetInventorySheet;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ArcaneWolfInventorySheet extends PetInventorySheet<ArcaneWolfEntity> {
	
	public ArcaneWolfInventorySheet(ArcaneWolfEntity wolf) {
		super(wolf, wolf.getInventory());
	}
	
	@Override
	public void showSheet(ArcaneWolfEntity wolf, Player player, IPetContainer<ArcaneWolfEntity> container, int width, int height, int offsetX, int offsetY) {
		super.showSheet(wolf, player, container, width, height, offsetX, offsetY);
	}

	@Override
	public void draw(PoseStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		super.draw(matrixStackIn, mc, partialTicks, width, height, mouseX, mouseY);
	}

	@Override
	public boolean shouldShow(ArcaneWolfEntity wolf, IPetContainer<ArcaneWolfEntity> container) {
		return wolf.hasWolfCapability(WolfBondCapability.INVENTORY);
	}

}
