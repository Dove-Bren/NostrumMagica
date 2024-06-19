package com.smanzana.nostrummagica.client.gui.petgui.reddragon;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class RedDragonBondInfoSheet implements IPetGUISheet<TameRedDragonEntity> {

	private TameRedDragonEntity dragon;
	
	public RedDragonBondInfoSheet(TameRedDragonEntity dragon) {
		this.dragon = dragon;
	}
	
	@Override
	public void showSheet(TameRedDragonEntity dragon, PlayerEntity player, IPetContainer<TameRedDragonEntity> container, int width, int height, int offsetX, int offsetY) {
		
	}

	@Override
	public void hideSheet(TameRedDragonEntity dragon, PlayerEntity player, IPetContainer<TameRedDragonEntity> container) {
		
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
		
		float bond = this.dragon.getBond();
		
		if (bond < TameRedDragonEntity.BOND_LEVEL_FOLLOW) {
			untrans = "info.tamed_dragon.red_bond.wild";
		} else if (bond < TameRedDragonEntity.BOND_LEVEL_PLAYERS) {
			untrans = "info.tamed_dragon.red_bond.follow";
		} else if (bond < TameRedDragonEntity.BOND_LEVEL_CHEST) {
			untrans = "info.tamed_dragon.red_bond.players";
		} else if (bond < TameRedDragonEntity.BOND_LEVEL_ALLOW_RIDE) {
			untrans = "info.tamed_dragon.red_bond.chest";
		} else if ((this.dragon.getCanUseMagic() && bond < TameRedDragonEntity.BOND_LEVEL_MAGIC) || (!this.dragon.getCanUseMagic() && bond < 0.9999f)) {
			untrans = "info.tamed_dragon.red_bond.ride";
		} else if (this.dragon.getCanUseMagic() && bond < 0.9999f) {
			untrans = "info.tamed_dragon.red_bond.magic";
		} else {
			untrans = "info.tamed_dragon.red_bond.full";
		}
		
		str = I18n.format(untrans, new Object[0]);
		x = 5;
		
		RenderFuncs.drawSplitString(matrixStackIn, fonter, str, x, y, width - (x * 2), infoColor);
		
		x = 10;
		y += (h * 4) + 10;
		
		if (bond >= TameRedDragonEntity.BOND_LEVEL_FOLLOW)
		{
			if (bond >= TameRedDragonEntity.BOND_LEVEL_FOLLOW) {
				str = "Follows";
				
				fonter.drawString(matrixStackIn, str, x, y, capabilityColor);
				y += h + 2;
			}
			if (bond >= TameRedDragonEntity.BOND_LEVEL_PLAYERS) {
				str = "Respects Other Players";
				
				fonter.drawString(matrixStackIn, str, x, y, capabilityColor);
				y += h + 2;
			}
			if (bond >= TameRedDragonEntity.BOND_LEVEL_CHEST) {
				str = "Can Hold Items";
				
				fonter.drawString(matrixStackIn, str, x, y, capabilityColor);
				y += h + 2;
			}
			if (bond >= TameRedDragonEntity.BOND_LEVEL_ALLOW_RIDE) {
				str = "Rideable";
				
				fonter.drawString(matrixStackIn, str, x, y, capabilityColor);
				y += h + 2;
			}
			if (dragon.getCanUseMagic() && bond >= TameRedDragonEntity.BOND_LEVEL_MAGIC) {
				str = "Spell Tactics";
				
				fonter.drawString(matrixStackIn, str, x, y, capabilityColor);
				y += h + 2;
			}
			if (dragon.getDragonMana() > 0 && bond >= TameRedDragonEntity.BOND_LEVEL_MANA ) {
				str = "Dragon Mana Bond";
				
				fonter.drawString(matrixStackIn, str, x, y, capabilityColor);
				y += h + 2;
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
	public boolean shouldShow(TameRedDragonEntity dragon, IPetContainer<TameRedDragonEntity> container) {
		return true; // always show
	}

	@Override
	public void overlay(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
	}

}
