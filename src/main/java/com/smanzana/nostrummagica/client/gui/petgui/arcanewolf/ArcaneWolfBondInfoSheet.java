package com.smanzana.nostrummagica.client.gui.petgui.arcanewolf;

import java.util.List;

import com.smanzana.nostrummagica.client.gui.petgui.IPetGUISheet;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetContainer;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.WolfBondCapability;

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
	public void showSheet(EntityArcaneWolf wolf, PlayerEntity player, PetContainer<EntityArcaneWolf> container, int width, int height, int offsetX, int offsetY) {
		
	}

	@Override
	public void hideSheet(EntityArcaneWolf wolf, PlayerEntity player, PetContainer<EntityArcaneWolf> container) {
		
	}

	@Override
	public void draw(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
		font fonter = mc.font;
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
		
		fonter.drawSplitString(str, x, y, width - (x * 2), infoColor);
		
		x = 10;
		y += (h * 4) + 10;
		
		for (WolfBondCapability cap : capabilities) {
			if (wolf.hasWolfCapability(cap)) {
				str = I18n.format("info.tamed_arcane_wolf.capability." + cap.getKey());
				fonter.drawString(str, x, y, capabilityColor);
				y += h + 2;
			} else {
				break;
			}
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
	}

	@Override
	public void handleMessage(CompoundNBT data) {
		
	}

	@Override
	public String getButtonText() {
		return "Bonding";
	}

	@Override
	public boolean shouldShow(EntityArcaneWolf wolf, PetContainer<EntityArcaneWolf> container) {
		return true; // always show
	}

	@Override
	public void overlay(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
	}

}
