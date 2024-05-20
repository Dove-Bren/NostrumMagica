package com.smanzana.nostrummagica.client.gui.petgui.arcanewolf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.IWolfAbility;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class ArcaneWolfAbilitySheet implements IPetGUISheet<EntityArcaneWolf> {

	protected EntityArcaneWolf wolf;
	private List<AbilityWidget> widgets;
	
	public ArcaneWolfAbilitySheet(EntityArcaneWolf wolf) {
		this.wolf = wolf;
		this.widgets = new ArrayList<>();
	}
	
	@Override
	public void showSheet(EntityArcaneWolf wolf, PlayerEntity player, IPetContainer<EntityArcaneWolf> container, int width, int height, int offsetX, int offsetY) {
		widgets.clear();
		List<IWolfAbility> abilities = wolf.getAbilityList();
		for (int i = 0; i < abilities.size(); i++) {
			widgets.add(new AbilityWidget(abilities.get(i),
					10, 20 + i * 15, width - 20, 15));
		}
	}

	@Override
	public void hideSheet(EntityArcaneWolf wolf, PlayerEntity player, IPetContainer<EntityArcaneWolf> container) {
		widgets.clear();
	}

	@Override
	public void draw(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		mc.fontRenderer.drawString(matrixStackIn, TextFormatting.BOLD + "Abilities" + TextFormatting.RESET, 5, 5, 0xFFFFFFFF);
		
		for (AbilityWidget widget : widgets) {
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
		return "Abilities";
	}
	
	public boolean shouldShow(EntityArcaneWolf wolf, IPetContainer<EntityArcaneWolf> container) {
		return !wolf.getAbilityList().isEmpty();
	}

	@Override
	public void overlay(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		for (AbilityWidget widget : widgets) {
			widget.drawOverlay(mc, matrixStackIn, width, height, mouseX, mouseY);
		}
	}
	
	private static class AbilityWidget extends Widget {
		
		private final IWolfAbility ability;
		
		public AbilityWidget(IWolfAbility ability, int x, int y, int width, int height) {
			super(x, y, width, height, StringTextComponent.EMPTY);
			this.ability = ability;
		}
		
		@Override
		public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			Minecraft mc = Minecraft.getInstance();
			
			matrixStackIn.push();
			matrixStackIn.translate(x, y, 0);
			mc.fontRenderer.func_243246_a(matrixStackIn, ability.getName(), 0, 0, 0xFFFFFFFF);
			
			mc.fontRenderer.drawString(matrixStackIn, "MP: " + ability.getCost(), 100, 0, 0xFFDDDDFF);
			
			final String targetString;
			if (ability.getTargetGroup() == null) {
				targetString = "Other";
			} else {
				targetString = ability.getTargetGroup().name().substring(0, 1)
						+ ability.getTargetGroup().name().substring(1).toLowerCase();
			}
			mc.fontRenderer.drawString(matrixStackIn, "Target: " + targetString, 150, 0, 0xFFDDDDFF);
			matrixStackIn.pop();
		}
		
		public void drawOverlay(Minecraft mc, MatrixStack matrixStackIn, int sheetWidth, int sheetHeight, int mouseX, int mouseY) {
			if (this.isHovered()) {
				GuiUtils.drawHoveringText(matrixStackIn, Arrays.asList(ability.getDescription()), mouseX, mouseY, sheetWidth, sheetHeight, -1, mc.fontRenderer);
			}
		}
	}

}
