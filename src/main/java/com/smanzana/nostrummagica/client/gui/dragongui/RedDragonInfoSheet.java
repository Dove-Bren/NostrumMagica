package com.smanzana.nostrummagica.client.gui.dragongui;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.nostrummagica.client.gui.dragongui.TamedDragonGUI.DragonContainer;
import com.smanzana.nostrummagica.entity.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.ITameDragon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.NBTTagCompound;

public class RedDragonInfoSheet implements IDragonGUISheet {

	private EntityTameDragonRed dragon;
	
	public RedDragonInfoSheet(EntityTameDragonRed dragon) {
		this.dragon = dragon;
	}
	
	@Override
	public void showSheet(ITameDragon dragon, DragonContainer container) {
		
	}

	@Override
	public void hideSheet() {
		
	}

	@Override
	public void draw(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		FontRenderer fonter = mc.fontRendererObj;
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
		String str;
		
		str = ChatFormatting.BOLD + "Attributes" + ChatFormatting.RESET;
		x = 5;
		fonter.drawString(str, x, y, categoryColor);
		
		x = 10;
		y += h + 5;
		
		{
			str = "Health: ";
			w = fonter.getStringWidth(str);
			
			fonter.drawString(str, x, y, labelColor);
			x += w;
			
			fonter.drawString("" + (int) dragon.getMaxHealth(), x, y, dataColor);
			y += h + 2;
		}
		
		x = 10;
		if (dragon.getDragonMana() > 0) {
			str = "Mana: ";
			w = fonter.getStringWidth(str);
			
			fonter.drawString(str, x, y, labelColor);
			x += w;
			
			fonter.drawString("" + dragon.getDragonMana(), x, y, dataColor);
			y += h + 2;
		}
		
		
		y += 10;
		
		str = ChatFormatting.BOLD + "Movement" + ChatFormatting.RESET;
		x = 5;
		fonter.drawString(str, x, y, categoryColor);
		y += h + 5;
		
		x = 10;
		{
			str = "Jumps: ";
			w = fonter.getStringWidth(str);
			
			fonter.drawString(str, x, y, labelColor);
			x += w;
			
			int jumps = 1 + dragon.getBonusJumps();
			
			fonter.drawString("" + jumps, x, y, jumps > 1 ? goodDataColor : badDataColor);
			y += h + 2;
		}
		
		x = 10;
		if (Math.abs(dragon.getSpeedBonus()) > 0.01f) {
			str = "Speed: ";
			w = fonter.getStringWidth(str);
			
			fonter.drawString(str, x, y, labelColor);
			x += w;
			
			fonter.drawString(String.format("%+.2f%%", dragon.getSpeedBonus()), x, y, dragon.getSpeedBonus() > 0 ? goodDataColor : badDataColor);
			y += h + 2;
		}
		
		x = 10;
		if (Math.abs(dragon.getJumpHeightBonus()) > 0.01f) {
			str = "Jump Height: ";
			w = fonter.getStringWidth(str);
			
			fonter.drawString(str, x, y, labelColor);
			x += w;
			
			fonter.drawString(String.format("%+.2f%%", dragon.getJumpHeightBonus()), x, y, dragon.getJumpHeightBonus() > 0 ? goodDataColor : badDataColor);
			y += h + 2;
		}
		
		y += 10;
		
		str = ChatFormatting.BOLD + "Capabilities" + ChatFormatting.RESET;
		x = 5;
		fonter.drawString(str, x, y, categoryColor);
		y += h + 5;
		
		x = 10;
		{
			str = "Rideable";
			
			fonter.drawString(str, x, y, capabilityColor);
			y += h + 2;
		}
		
		x = 10;
		if (dragon.getCanFly()) {
			str = "Weak Flight";
			
			fonter.drawString(str, x, y, capabilityColor);
			y += h + 2;
		}
		
		x = 10;
		if (dragon.getCanUseMagic()) {
			str = "Magic";
			
			fonter.drawString(str, x, y, capabilityColor);
			y += h + 2;
		}
		
		y += 10;
		
		if (dragon.getCanUseMagic()) {
			str = ChatFormatting.BOLD + "Magic" + ChatFormatting.RESET;
			x = 5;
			fonter.drawString(str, x, y, categoryColor);
			y += h + 5;
			
			x = 10;
			{
				str = "Memory: ";
				w = fonter.getStringWidth(str);
				
				fonter.drawString(str, x, y, labelColor);
				x += w;
				
				fonter.drawString("" + dragon.getMagicMemorySize(), x, y, goodDataColor);
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
		return "Stats";
	}

}
