package com.smanzana.nostrummagica.client.gui.petgui.arcanewolf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.WolfTypeCapability;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class ArcaneWolfInfoSheet implements IPetGUISheet<EntityArcaneWolf> {

	private EntityArcaneWolf wolf;
	private List<CapabilityTooltip> widgets;
	
	public ArcaneWolfInfoSheet(EntityArcaneWolf wolf) {
		this.wolf = wolf;
		widgets = new ArrayList<>();
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
		
		// Hackily create widgets first time we're rendered so that this logic only exists in one place
		final @Nullable List<CapabilityTooltip> widgetCollection = (this.widgets.isEmpty() ? this.widgets : null); 
		x = 10;
		{
			str = "Rideable";
			fonter.drawString(matrixStackIn, str, x, y, capabilityColor);
			
			if (widgetCollection != null) {
				Vector4f dims = new Vector4f(fonter.getStringWidth(str), fonter.FONT_HEIGHT, 0, 0);
				dims.transform(matrixStackIn.getLast().getMatrix());
				
				final int widgetWidth = (int) dims.getX();
				final int widgetHeight = (int) dims.getY();
				final String key = "Can be ridden upon, and is even strong enough to jump!";
				widgetCollection.add(new CapabilityTooltip(new StringTextComponent(key), x, y, widgetWidth, widgetHeight));
			}
			
			y += h + smallMargin;
		}
		
		for (WolfTypeCapability cap : WolfTypeCapability.values()) {
			if (wolf.hasWolfCapability(cap)) {
				x = 10;
				{
					str = I18n.format("info.tamed_arcane_wolf.capability." + cap.getKey());
					fonter.drawString(matrixStackIn, str, x, y, capabilityColor);
					
					if (widgetCollection != null) {
						Vector4f dims = new Vector4f(fonter.getStringWidth(str), fonter.FONT_HEIGHT, 0, 0);
						dims.transform(matrixStackIn.getLast().getMatrix());
						
						final int widgetWidth = (int) dims.getX();
						final int widgetHeight = (int) dims.getY();
						final String key = "info.tamed_arcane_wolf.capability." + cap.getKey() + ".desc";
						widgetCollection.add(new CapabilityTooltip(new TranslationTextComponent(key), x, y, widgetWidth, widgetHeight));
					}
					
					y += h + smallMargin;
				}
			}
		}
		
		for (Widget widget : this.widgets) {
			widget.render(matrixStackIn, mouseX, mouseY, partialTicks);
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
	
	public boolean shouldShow(EntityArcaneWolf wolf, IPetContainer<EntityArcaneWolf> container) {
		return true;
	}

	@Override
	public void overlay(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		for (CapabilityTooltip widget : widgets) {
			widget.drawOverlay(mc, matrixStackIn, width, height, mouseX, mouseY);
		}
	}
	
	private static class CapabilityTooltip extends Widget {
		
		private final ITextComponent tooltip;
		
		public CapabilityTooltip(ITextComponent tooltip, int x, int y, int width, int height) {
			super(x, y, width, height, StringTextComponent.EMPTY);
			this.tooltip = tooltip;
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialsTicks) {
			;
		}
		
		public void drawOverlay(Minecraft mc, MatrixStack matrixStackIn, int sheetWidth, int sheetHeight, int mouseX, int mouseY) {
			if (this.isHovered()) {
				GuiUtils.drawHoveringText(matrixStackIn, Arrays.asList(this.tooltip), mouseX, mouseY, sheetWidth, sheetHeight, -1, mc.fontRenderer);
			}
		}
	}

}
