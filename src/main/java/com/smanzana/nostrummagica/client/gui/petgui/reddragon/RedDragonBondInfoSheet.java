package com.smanzana.nostrummagica.client.gui.petgui.reddragon;

import com.smanzana.nostrummagica.client.gui.petgui.IPetGUISheet;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetContainer;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class RedDragonBondInfoSheet implements IPetGUISheet<EntityTameDragonRed> {

	private EntityTameDragonRed dragon;
	
	public RedDragonBondInfoSheet(EntityTameDragonRed dragon) {
		this.dragon = dragon;
	}
	
	@Override
	public void showSheet(EntityTameDragonRed dragon, EntityPlayer player, PetContainer<EntityTameDragonRed> container, int width, int height, int offsetX, int offsetY) {
		
	}

	@Override
	public void hideSheet(EntityTameDragonRed dragon, EntityPlayer player, PetContainer<EntityTameDragonRed> container) {
		
	}

	@Override
	public void draw(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
		FontRenderer fonter = mc.fontRenderer;
		int x = 0;
		int y = 5;
		final int infoColor = 0xFFFFFFFF;
		final int capabilityColor = 0xFFFFA0FF;
		final int h = fonter.FONT_HEIGHT;
		String str;
		String untrans;
		
		float bond = this.dragon.getBond();
		
		if (bond < EntityTameDragonRed.BOND_LEVEL_FOLLOW) {
			untrans = "info.tamed_dragon.red_bond.wild";
		} else if (bond < EntityTameDragonRed.BOND_LEVEL_PLAYERS) {
			untrans = "info.tamed_dragon.red_bond.follow";
		} else if (bond < EntityTameDragonRed.BOND_LEVEL_CHEST) {
			untrans = "info.tamed_dragon.red_bond.players";
		} else if (bond < EntityTameDragonRed.BOND_LEVEL_ALLOW_RIDE) {
			untrans = "info.tamed_dragon.red_bond.chest";
		} else if ((this.dragon.getCanUseMagic() && bond < EntityTameDragonRed.BOND_LEVEL_MAGIC) || (!this.dragon.getCanUseMagic() && bond < 0.9999f)) {
			untrans = "info.tamed_dragon.red_bond.ride";
		} else if (this.dragon.getCanUseMagic() && bond < 0.9999f) {
			untrans = "info.tamed_dragon.red_bond.magic";
		} else {
			untrans = "info.tamed_dragon.red_bond.full";
		}
		
		str = I18n.format(untrans, new Object[0]);
		x = 5;
		
		fonter.drawSplitString(str, x, y, width - (x * 2), infoColor);
		
		x = 10;
		y += (h * 4) + 10;
		
		if (bond >= EntityTameDragonRed.BOND_LEVEL_FOLLOW)
		{
			if (bond >= EntityTameDragonRed.BOND_LEVEL_FOLLOW) {
				str = "Follows";
				
				fonter.drawString(str, x, y, capabilityColor);
				y += h + 2;
			}
			if (bond >= EntityTameDragonRed.BOND_LEVEL_PLAYERS) {
				str = "Respects Other Players";
				
				fonter.drawString(str, x, y, capabilityColor);
				y += h + 2;
			}
			if (bond >= EntityTameDragonRed.BOND_LEVEL_CHEST) {
				str = "Can Hold Items";
				
				fonter.drawString(str, x, y, capabilityColor);
				y += h + 2;
			}
			if (bond >= EntityTameDragonRed.BOND_LEVEL_ALLOW_RIDE) {
				str = "Rideable";
				
				fonter.drawString(str, x, y, capabilityColor);
				y += h + 2;
			}
			if (dragon.getCanUseMagic() && bond >= EntityTameDragonRed.BOND_LEVEL_MAGIC) {
				str = "Spell Tactics";
				
				fonter.drawString(str, x, y, capabilityColor);
				y += h + 2;
			}
			if (dragon.getDragonMana() > 0 && bond >= EntityTameDragonRed.BOND_LEVEL_MANA ) {
				str = "Dragon Mana Bond";
				
				fonter.drawString(str, x, y, capabilityColor);
				y += h + 2;
			}
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
	}

	@Override
	public void handleMessage(NBTTagCompound data) {
		
	}

	@Override
	public String getButtonText() {
		return "Bonding";
	}

	@Override
	public boolean shouldShow(EntityTameDragonRed dragon, PetContainer<EntityTameDragonRed> container) {
		return true; // always show
	}

	@Override
	public void overlay(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
	}

}
