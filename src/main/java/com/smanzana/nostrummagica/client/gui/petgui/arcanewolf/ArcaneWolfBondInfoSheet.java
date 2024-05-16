package com.smanzana.nostrummagica.client.gui.petgui.arcanewolf;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.WolfBondCapability;
import com.smanzana.nostrummagica.utils.RenderFuncs;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class ArcaneWolfBondInfoSheet implements IPetGUISheet<EntityArcaneWolf> {

	private EntityArcaneWolf wolf;
	
	public ArcaneWolfBondInfoSheet(EntityArcaneWolf wolf) {
		this.wolf = wolf;
	}
	
	@Override
	public void showSheet(EntityArcaneWolf wolf, PlayerEntity player, IPetContainer<EntityArcaneWolf> container, int width, int height, int offsetX, int offsetY) {
		
	}

	@Override
	public void hideSheet(EntityArcaneWolf wolf, PlayerEntity player, IPetContainer<EntityArcaneWolf> container) {
		
	}

	@Override
	public void draw(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
		FontRenderer fonter = mc.fontRenderer;
		int x = 0;
		int y = 5;
		final int infoColor = 0xFFFFFFFF;
		final int capabilityColor = 0xFFFFA0FF;
		final int h = fonter.FONT_HEIGHT;
		String str;
		String untrans;
		
		final List<WolfBondCapability> capabilities = WolfBondCapability.GetSortedLowHigh();
		
		// Get summary string based on last thing that we match
		untrans = "info.tamed_arcane_wolf.bond_summary.wild";
		for (WolfBondCapability cap : capabilities) {
			if (wolf.hasWolfCapability(cap)) {
				untrans = "info.tamed_arcane_wolf.bond_summary." + cap.getKey();
			} else {
				break;
			}
		}
		
		str = I18n.format(untrans, new Object[0]);
		x = 5;
		
		RenderFuncs.drawSplitString(matrixStackIn, fonter, str, x, y, width - (x * 2), infoColor);
		
		x = 10;
		y += (h * 4) + 10;
		
		for (WolfBondCapability cap : capabilities) {
			if (wolf.hasWolfCapability(cap)) {
				str = I18n.format("info.tamed_arcane_wolf.capability." + cap.getKey());
				fonter.drawString(matrixStackIn, str, x, y, capabilityColor);
				y += h + 2;
			} else {
				break;
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return false;
	}

	@Override
	public void handleMessage(CompoundNBT data) {
		
	}

	@Override
	public String getButtonText() {
		return "Bonding";
	}

	@Override
	public boolean shouldShow(EntityArcaneWolf wolf, IPetContainer<EntityArcaneWolf> container) {
		return true; // always show
	}

	@Override
	public void overlay(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
	}

}
