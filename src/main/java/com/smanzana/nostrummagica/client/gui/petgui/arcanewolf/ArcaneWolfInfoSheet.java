package com.smanzana.nostrummagica.client.gui.petgui.arcanewolf;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.client.gui.petgui.IPetGUISheet;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetContainer;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.WolfTypeCapability;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;

public class ArcaneWolfInfoSheet implements IPetGUISheet<EntityArcaneWolf> {

	private EntityArcaneWolf wolf;
	
	public ArcaneWolfInfoSheet(EntityArcaneWolf wolf) {
		this.wolf = wolf;
	}
	
	@Override
	public void showSheet(EntityArcaneWolf wolf, PlayerEntity player, PetContainer<EntityArcaneWolf> container, int width, int height, int offsetX, int offsetY) {
		
	}

	@Override
	public void hideSheet(EntityArcaneWolf wolf, PlayerEntity player, PetContainer<EntityArcaneWolf> container) {
		
	}

	@Override
	public void draw(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		FontRenderer fonter = mc.fontRenderer;
		int x = 0;
		int y = 5;
		int w;
		final int categoryColor = 0xFFFFFFFF;
		final int labelColor = 0xFF22FFFF;
		final int dataColor = 0xFFD0D0D0;
		final int goodDataColor = 0xFFA0FFA0;
		final int badDataColor = 0xFFFFA0A0;
		final int capabilityColor = 0xFFFFA0FF;
		final int h = fonter.FONT_HEIGHT;
		final int smallMargin = 2;
		final int mediumMargin = 3;
		final int largeMargin = 7;
		//final int 
		String str;
		
		str = TextFormatting.BOLD + "Attributes" + TextFormatting.RESET;
		x = 5;
		fonter.drawString(matrixStackIn, str, x, y, categoryColor);
		
		x = 10;
		y += h + mediumMargin;
		
		{
			str = "Health: ";
			w = fonter.getStringWidth(str);
			
			fonter.drawString(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			fonter.drawString(matrixStackIn, "" + (int) wolf.getMaxHealth(), x, y, dataColor);
			y += h + smallMargin;
		}
		
		x = 10;
		{
			str = "Mana: ";
			w = fonter.getStringWidth(str);
			
			fonter.drawString(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			fonter.drawString(matrixStackIn, "" + wolf.getMaxMana(), x, y, dataColor);
			y += h + smallMargin;
		}
		
		x = 10;
		{
			str = "Mana Regen: ";
			w = fonter.getStringWidth(str);
			
			fonter.drawString(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			fonter.drawString(matrixStackIn, String.format("%.2f Mana/Sec", wolf.getManaRegen()), x, y, dataColor);
			y += h + smallMargin;
		}
		
		x = 10;
		{
			str = "Type: ";
			w = fonter.getStringWidth(str);
			
			fonter.drawString(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			fonter.drawString(matrixStackIn, I18n.format("info.wolf_type." + wolf.getElementalType().getNameKey() + ".name"), x, y, dataColor);
			y += h + smallMargin;
		}
		
		
		y += largeMargin;
		
		str = TextFormatting.BOLD + "Training" + TextFormatting.RESET;
		x = 5;
		fonter.drawString(matrixStackIn, str, x, y, categoryColor);
		y += h + mediumMargin;
		
		final EMagicElement training = wolf.getTrainingElement();
		x = 10;
		{
			str = "Currently Training: ";
			w = fonter.getStringWidth(str);
			
			fonter.drawString(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			
			fonter.drawString(matrixStackIn, training == null ? "Nothing" : training.getName(), x, y, dataColor);
			y += h + smallMargin;
		}
		
		if (training != null) {
			x = 10;
			{
				str = "Mastery: ";
				w = fonter.getStringWidth(str);
				
				fonter.drawString(matrixStackIn, str, x, y, labelColor);
				x += w;
				
				final String mastery;
				switch (wolf.getTrainingLevel()) {
				case 1:
				default:
					mastery = "Novice";
					break;
				case 2:
					mastery = "Adept";
					break;
				case 3:
					mastery = "Master";
				}
				fonter.drawString(matrixStackIn, mastery, x, y, dataColor);
				y += h + smallMargin;
			}
			
			x = 10;
			{
				str = "Progress: ";
				w = fonter.getStringWidth(str);
				
				fonter.drawString(matrixStackIn, str, x, y, labelColor);
				x += w;
				
				fonter.drawString(matrixStackIn, wolf.getTrainingXP() + " / " + wolf.getMaxTrainingXP(), x, y, dataColor);
				y += h + smallMargin;
			}
		}
		
		
		y += largeMargin;
		
		str = TextFormatting.BOLD + "Movement" + TextFormatting.RESET;
		x = 5;
		fonter.drawString(matrixStackIn, str, x, y, categoryColor);
		y += h + mediumMargin;
		
		x = 10;
		{
			str = "Jumps: ";
			w = fonter.getStringWidth(str);
			
			fonter.drawString(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			int jumps = 1 + wolf.getBonusJumps();
			
			fonter.drawString(matrixStackIn, "" + jumps, x, y, jumps > 1 ? goodDataColor : badDataColor);
			y += h + smallMargin;
		}
		
		y += largeMargin;
		
		str = TextFormatting.BOLD + "Capabilities" + TextFormatting.RESET;
		x = 5;
		fonter.drawString(matrixStackIn, str, x, y, categoryColor);
		y += h + mediumMargin;
		
		x = 10;
		{
			str = "Rideable";
			
			fonter.drawString(matrixStackIn, str, x, y, capabilityColor);
			y += h + smallMargin;
		}
		
		for (WolfTypeCapability cap : WolfTypeCapability.values()) {
			if (wolf.hasWolfCapability(cap)) {
				x = 10;
				{
					str = I18n.format("info.tamed_arcane_wolf.capability." + cap.getKey());
					
					fonter.drawString(matrixStackIn, str, x, y, capabilityColor);
					y += h + smallMargin;
				}
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
		return "Stats";
	}
	
	public boolean shouldShow(EntityArcaneWolf wolf, PetContainer<EntityArcaneWolf> container) {
		return true;
	}

	@Override
	public void overlay(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
	}

}
