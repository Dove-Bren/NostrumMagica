package com.smanzana.nostrummagica.client.gui.petgui.reddragon;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;

public class RedDragonInfoSheet implements IPetGUISheet<TameRedDragonEntity> {

	private TameRedDragonEntity dragon;
	
	public RedDragonInfoSheet(TameRedDragonEntity dragon) {
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
		FontRenderer fonter = mc.font;
		int x = 0;
		int y = 5;
		int w;
		final int categoryColor = 0xFFFFFFFF;
		final int labelColor = 0xFF22FFFF;
		final int dataColor = 0xFFD0D0D0;
		final int goodDataColor = 0xFFA0FFA0;
		final int badDataColor = 0xFFFFA0A0;
		final int capabilityColor = 0xFFFFA0FF;
		final int h = fonter.lineHeight;
		final int smallMargin = 2;
		final int mediumMargin = 3;
		final int largeMargin = 7;
		//final int 
		String str;
		
		str = TextFormatting.BOLD + "Attributes" + TextFormatting.RESET;
		x = 5;
		fonter.draw(matrixStackIn, str, x, y, categoryColor);
		
		x = 10;
		y += h + mediumMargin;
		
		{
			str = "Health: ";
			w = fonter.width(str);
			
			fonter.draw(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			fonter.draw(matrixStackIn, "" + (int) dragon.getMaxHealth(), x, y, dataColor);
			y += h + smallMargin;
		}
		
		x = 10;
		if (dragon.getDragonMana() > 0) {
			str = "Mana: ";
			w = fonter.width(str);
			
			fonter.draw(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			fonter.draw(matrixStackIn, "" + dragon.getDragonMana(), x, y, dataColor);
			y += h + smallMargin;
		}
		
		
		y += largeMargin;
		
		str = TextFormatting.BOLD + "Movement" + TextFormatting.RESET;
		x = 5;
		fonter.draw(matrixStackIn, str, x, y, categoryColor);
		y += h + mediumMargin;
		
		x = 10;
		{
			str = "Jumps: ";
			w = fonter.width(str);
			
			fonter.draw(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			int jumps = 1 + dragon.getBonusJumps();
			
			fonter.draw(matrixStackIn, "" + jumps, x, y, jumps > 1 ? goodDataColor : badDataColor);
			y += h + smallMargin;
		}
		
		x = 10;
		if (Math.abs(dragon.getSpeedBonus()) > 0.01f) {
			str = "Speed: ";
			w = fonter.width(str);
			
			fonter.draw(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			fonter.draw(matrixStackIn, String.format("%+.2f%%", dragon.getSpeedBonus()), x, y, dragon.getSpeedBonus() > 0 ? goodDataColor : badDataColor);
			y += h + smallMargin;
		}
		
		x = 10;
		if (Math.abs(dragon.getJumpHeightBonus()) > 0.01f) {
			str = "Jump Height: ";
			w = fonter.width(str);
			
			fonter.draw(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			fonter.draw(matrixStackIn, String.format("%+.2f%%", dragon.getJumpHeightBonus()), x, y, dragon.getJumpHeightBonus() > 0 ? goodDataColor : badDataColor);
			y += h + smallMargin;
		}
		
		y += largeMargin;
		
		str = TextFormatting.BOLD + "Capabilities" + TextFormatting.RESET;
		x = 5;
		fonter.draw(matrixStackIn, str, x, y, categoryColor);
		y += h + mediumMargin;
		
		x = 10;
		{
			str = "Rideable";
			
			fonter.draw(matrixStackIn, str, x, y, capabilityColor);
			y += h + smallMargin;
		}
		
		x = 10;
		if (dragon.getCanFly()) {
			str = "Weak Flight";
			
			fonter.draw(matrixStackIn, str, x, y, capabilityColor);
			y += h + smallMargin;
		}
		
		x = 10;
		if (dragon.getCanUseMagic()) {
			str = "Magic";
			
			fonter.draw(matrixStackIn, str, x, y, capabilityColor);
			y += h + smallMargin;
		}
		
		if (dragon.getCanUseMagic() || dragon.getDragonMana() > 0) {
		
			y += largeMargin;
		
			str = TextFormatting.BOLD + "Magic" + TextFormatting.RESET;
			x = 5;
			fonter.draw(matrixStackIn, str, x, y, categoryColor);
			y += h + mediumMargin;
			
			x = 10;
			if (dragon.getCanUseMagic()) {
				str = "Memory: ";
				w = fonter.width(str);
				
				fonter.draw(matrixStackIn, str, x, y, labelColor);
				x += w;
				
				fonter.draw(matrixStackIn, "" + dragon.getMagicMemorySize(), x, y, goodDataColor);
				y += h + smallMargin;
			}
			
			x = 10;
			if (dragon.getDragonMana() > 0) {
				str = "Mana Regen: ";
				w = fonter.width(str);
				
				fonter.draw(matrixStackIn, str, x, y, labelColor);
				x += w;
				
				fonter.draw(matrixStackIn, String.format("%.2f Mana/Sec", dragon.getManaRegen()), x, y, goodDataColor);
				y += h + smallMargin;
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
	
	public boolean shouldShow(TameRedDragonEntity dragon, IPetContainer<TameRedDragonEntity> container) {
		return true;
	}

	@Override
	public void overlay(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
	}

}
