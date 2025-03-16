package com.smanzana.nostrummagica.client.gui.petgui.reddragon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;

public class RedDragonBondInfoSheet implements IPetGUISheet<TameRedDragonEntity> {

	private TameRedDragonEntity dragon;
	
	public RedDragonBondInfoSheet(TameRedDragonEntity dragon) {
		this.dragon = dragon;
	}
	
	@Override
	public void showSheet(TameRedDragonEntity dragon, Player player, IPetContainer<TameRedDragonEntity> container, int width, int height, int offsetX, int offsetY) {
		
	}

	@Override
	public void hideSheet(TameRedDragonEntity dragon, Player player, IPetContainer<TameRedDragonEntity> container) {
		
	}

	@Override
	public void draw(PoseStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
		Font fonter = mc.font;
		int x = 0;
		int y = 5;
		final int infoColor = 0xFFFFFFFF;
		final int capabilityColor = 0xFFFFA0FF;
		final int h = fonter.lineHeight;
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
		
		str = I18n.get(untrans, new Object[0]);
		x = 5;
		
		RenderFuncs.drawSplitString(matrixStackIn, fonter, str, x, y, width - (x * 2), infoColor);
		
		x = 10;
		y += (h * 4) + 10;
		
		if (bond >= TameRedDragonEntity.BOND_LEVEL_FOLLOW)
		{
			if (bond >= TameRedDragonEntity.BOND_LEVEL_FOLLOW) {
				str = "Follows";
				
				fonter.draw(matrixStackIn, str, x, y, capabilityColor);
				y += h + 2;
			}
			if (bond >= TameRedDragonEntity.BOND_LEVEL_PLAYERS) {
				str = "Respects Other Players";
				
				fonter.draw(matrixStackIn, str, x, y, capabilityColor);
				y += h + 2;
			}
			if (bond >= TameRedDragonEntity.BOND_LEVEL_CHEST) {
				str = "Can Hold Items";
				
				fonter.draw(matrixStackIn, str, x, y, capabilityColor);
				y += h + 2;
			}
			if (bond >= TameRedDragonEntity.BOND_LEVEL_ALLOW_RIDE) {
				str = "Rideable";
				
				fonter.draw(matrixStackIn, str, x, y, capabilityColor);
				y += h + 2;
			}
			if (dragon.getCanUseMagic() && bond >= TameRedDragonEntity.BOND_LEVEL_MAGIC) {
				str = "Spell Tactics";
				
				fonter.draw(matrixStackIn, str, x, y, capabilityColor);
				y += h + 2;
			}
			if (dragon.getDragonMana() > 0 && bond >= TameRedDragonEntity.BOND_LEVEL_MANA ) {
				str = "Dragon Mana Bond";
				
				fonter.draw(matrixStackIn, str, x, y, capabilityColor);
				y += h + 2;
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return false;
	}

	@Override
	public void handleMessage(CompoundTag data) {
		
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
	public void overlay(PoseStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
	}

}
