package com.smanzana.nostrummagica.client.gui.petgui.arcanewolf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.WolfTypeCapability;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public class ArcaneWolfInfoSheet implements IPetGUISheet<ArcaneWolfEntity> {

	private ArcaneWolfEntity wolf;
	private List<CapabilityTooltip> widgets;
	
	public ArcaneWolfInfoSheet(ArcaneWolfEntity wolf) {
		this.wolf = wolf;
		widgets = new ArrayList<>();
	}
	
	@Override
	public void showSheet(ArcaneWolfEntity wolf, Player player, IPetContainer<ArcaneWolfEntity> container, int width, int height, int offsetX, int offsetY) {
		
	}

	@Override
	public void hideSheet(ArcaneWolfEntity wolf, Player player, IPetContainer<ArcaneWolfEntity> container) {
		
	}

	@Override
	public void draw(PoseStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		Font fonter = mc.font;
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
		
		str = ChatFormatting.BOLD + "Attributes" + ChatFormatting.RESET;
		x = 5;
		fonter.draw(matrixStackIn, str, x, y, categoryColor);
		
		x = 10;
		y += h + mediumMargin;
		
		{
			str = "Health: ";
			w = fonter.width(str);
			
			fonter.draw(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			fonter.draw(matrixStackIn, "" + (int) wolf.getMaxHealth(), x, y, dataColor);
			y += h + smallMargin;
		}
		
		x = 10;
		{
			str = "Mana: ";
			w = fonter.width(str);
			
			fonter.draw(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			fonter.draw(matrixStackIn, "" + wolf.getMaxMana(), x, y, dataColor);
			y += h + smallMargin;
		}
		
		x = 10;
		{
			str = "Mana Regen: ";
			w = fonter.width(str);
			
			fonter.draw(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			fonter.draw(matrixStackIn, String.format("%.2f Mana/Sec", wolf.getManaRegen()), x, y, dataColor);
			y += h + smallMargin;
		}
		
		x = 10;
		{
			str = "Type: ";
			w = fonter.width(str);
			
			fonter.draw(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			fonter.draw(matrixStackIn, I18n.get("info.wolf_type." + wolf.getElementalType().getNameKey() + ".name"), x, y, dataColor);
			y += h + smallMargin;
		}
		
		
		y += largeMargin;
		
		str = ChatFormatting.BOLD + "Training" + ChatFormatting.RESET;
		x = 5;
		fonter.draw(matrixStackIn, str, x, y, categoryColor);
		y += h + mediumMargin;
		
		final EMagicElement training = wolf.getTrainingElement();
		x = 10;
		{
			str = "Currently Training: ";
			w = fonter.width(str);
			
			fonter.draw(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			
			fonter.draw(matrixStackIn, training == null ? "Nothing" : training.getName(), x, y, dataColor);
			y += h + smallMargin;
		}
		
		if (training != null) {
			x = 10;
			{
				str = "Mastery: ";
				w = fonter.width(str);
				
				fonter.draw(matrixStackIn, str, x, y, labelColor);
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
				fonter.draw(matrixStackIn, mastery, x, y, dataColor);
				y += h + smallMargin;
			}
			
			x = 10;
			{
				str = "Progress: ";
				w = fonter.width(str);
				
				fonter.draw(matrixStackIn, str, x, y, labelColor);
				x += w;
				
				fonter.draw(matrixStackIn, wolf.getTrainingXP() + " / " + wolf.getMaxTrainingXP(), x, y, dataColor);
				y += h + smallMargin;
			}
		}
		
		
		y += largeMargin;
		
		str = ChatFormatting.BOLD + "Movement" + ChatFormatting.RESET;
		x = 5;
		fonter.draw(matrixStackIn, str, x, y, categoryColor);
		y += h + mediumMargin;
		
		x = 10;
		{
			str = "Jumps: ";
			w = fonter.width(str);
			
			fonter.draw(matrixStackIn, str, x, y, labelColor);
			x += w;
			
			int jumps = 1 + wolf.getBonusJumps();
			
			fonter.draw(matrixStackIn, "" + jumps, x, y, jumps > 1 ? goodDataColor : badDataColor);
			y += h + smallMargin;
		}
		
		y += largeMargin;
		
		str = ChatFormatting.BOLD + "Capabilities" + ChatFormatting.RESET;
		x = 5;
		fonter.draw(matrixStackIn, str, x, y, categoryColor);
		y += h + mediumMargin;
		
		// Hackily create widgets first time we're rendered so that this logic only exists in one place
		final @Nullable List<CapabilityTooltip> widgetCollection = (this.widgets.isEmpty() ? this.widgets : null); 
		x = 10;
		{
			str = "Rideable";
			fonter.draw(matrixStackIn, str, x, y, capabilityColor);
			
			if (widgetCollection != null) {
				Vector4f dims = new Vector4f(fonter.width(str), fonter.lineHeight, 0, 0);
				dims.transform(matrixStackIn.last().pose());
				
				final int widgetWidth = (int) dims.x();
				final int widgetHeight = (int) dims.y();
				final String key = "Can be ridden upon, and is even strong enough to jump!";
				widgetCollection.add(new CapabilityTooltip(new TextComponent(key), x, y, widgetWidth, widgetHeight));
			}
			
			y += h + smallMargin;
		}
		
		for (WolfTypeCapability cap : WolfTypeCapability.values()) {
			if (wolf.hasWolfCapability(cap)) {
				x = 10;
				{
					str = I18n.get("info.tamed_arcane_wolf.capability." + cap.getKey());
					fonter.draw(matrixStackIn, str, x, y, capabilityColor);
					
					if (widgetCollection != null) {
						Vector4f dims = new Vector4f(fonter.width(str), fonter.lineHeight, 0, 0);
						dims.transform(matrixStackIn.last().pose());
						
						final int widgetWidth = (int) dims.x();
						final int widgetHeight = (int) dims.y();
						final String key = "info.tamed_arcane_wolf.capability." + cap.getKey() + ".desc";
						widgetCollection.add(new CapabilityTooltip(new TranslatableComponent(key), x, y, widgetWidth, widgetHeight));
					}
					
					y += h + smallMargin;
				}
			}
		}
		
		for (AbstractWidget widget : this.widgets) {
			widget.render(matrixStackIn, mouseX, mouseY, partialTicks);
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
		return "Stats";
	}
	
	public boolean shouldShow(ArcaneWolfEntity wolf, IPetContainer<ArcaneWolfEntity> container) {
		return true;
	}

	@Override
	public void overlay(PoseStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		for (CapabilityTooltip widget : widgets) {
			widget.drawOverlay(mc, matrixStackIn, width, height, mouseX, mouseY);
		}
	}
	
	private static class CapabilityTooltip extends AbstractWidget {
		
		private final Component tooltip;
		
		public CapabilityTooltip(Component tooltip, int x, int y, int width, int height) {
			super(x, y, width, height, TextComponent.EMPTY);
			this.tooltip = tooltip;
		}
		
		@Override
		public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialsTicks) {
			;
		}
		
		public void drawOverlay(Minecraft mc, PoseStack matrixStackIn, int sheetWidth, int sheetHeight, int mouseX, int mouseY) {
			if (this.isHovered()) {
				mc.screen.renderTooltip(matrixStackIn, Arrays.asList(this.tooltip), Optional.empty(), mouseX, mouseY, mc.font);
			}
		}

		@Override
		public void updateNarration(NarrationElementOutput p_169152_) {
			// TODO Auto-generated method stub
			
		}
	}

}
