package com.smanzana.nostrummagica.client.gui.petgui.arcanewolf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.IWolfAbility;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

public class ArcaneWolfAbilitySheet implements IPetGUISheet<ArcaneWolfEntity> {

	protected ArcaneWolfEntity wolf;
	private List<AbilityWidget> widgets;
	
	public ArcaneWolfAbilitySheet(ArcaneWolfEntity wolf) {
		this.wolf = wolf;
		this.widgets = new ArrayList<>();
	}
	
	@Override
	public void showSheet(ArcaneWolfEntity wolf, Player player, IPetContainer<ArcaneWolfEntity> container, int width, int height, int offsetX, int offsetY) {
		widgets.clear();
		List<IWolfAbility> abilities = wolf.getAbilityList();
		for (int i = 0; i < abilities.size(); i++) {
			widgets.add(new AbilityWidget(abilities.get(i),
					10, 20 + i * 15, width - 20, 15));
		}
	}

	@Override
	public void hideSheet(ArcaneWolfEntity wolf, Player player, IPetContainer<ArcaneWolfEntity> container) {
		widgets.clear();
	}

	@Override
	public void draw(PoseStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		mc.font.draw(matrixStackIn, ChatFormatting.BOLD + "Abilities" + ChatFormatting.RESET, 5, 5, 0xFFFFFFFF);
		
		for (AbilityWidget widget : widgets) {
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
		return "Abilities";
	}
	
	public boolean shouldShow(ArcaneWolfEntity wolf, IPetContainer<ArcaneWolfEntity> container) {
		return !wolf.getAbilityList().isEmpty();
	}

	@Override
	public void overlay(PoseStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		for (AbilityWidget widget : widgets) {
			widget.drawOverlay(mc, matrixStackIn, width, height, mouseX, mouseY);
		}
	}
	
	private static class AbilityWidget extends AbstractWidget {
		
		private final IWolfAbility ability;
		
		public AbilityWidget(IWolfAbility ability, int x, int y, int width, int height) {
			super(x, y, width, height, TextComponent.EMPTY);
			this.ability = ability;
		}
		
		@Override
		public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			Minecraft mc = Minecraft.getInstance();
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(x, y, 0);
			mc.font.drawShadow(matrixStackIn, ability.getName(), 0, 0, 0xFFFFFFFF);
			
			mc.font.draw(matrixStackIn, "MP: " + ability.getCost(), 100, 0, 0xFFDDDDFF);
			
			final String targetString;
			if (ability.getTargetGroup() == null) {
				targetString = "Other";
			} else {
				targetString = ability.getTargetGroup().name().substring(0, 1)
						+ ability.getTargetGroup().name().substring(1).toLowerCase();
			}
			mc.font.draw(matrixStackIn, "Target: " + targetString, 150, 0, 0xFFDDDDFF);
			matrixStackIn.popPose();
		}
		
		public void drawOverlay(Minecraft mc, PoseStack matrixStackIn, int sheetWidth, int sheetHeight, int mouseX, int mouseY) {
			if (this.isHovered()) {
				mc.screen.renderTooltip(matrixStackIn, Arrays.asList(ability.getDescription()), Optional.empty(), mouseX, mouseY, mc.font);
			}
		}

		@Override
		public void updateNarration(NarrationElementOutput p_169152_) {
			// TODO Auto-generated method stub
			
		}
	}

}
