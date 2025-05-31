package com.smanzana.nostrummagica.client.gui.tooltip;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.inventory.tooltip.ImbuementTooltip;
import com.smanzana.nostrummagica.spell.SpellEffects;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class ImbuementTooltipComponent extends AbsoluteTooltipComponent {

	private static final ResourceLocation GUI_ICONS = new ResourceLocation(NostrumMagica.MODID, "textures/gui/icons.png");
	private static final int ICON_HEIGHT = 16;
	
	protected static final Component LABEL = new TranslatableComponent("tooltip.imbued").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
	
	private final ImbuementTooltip tooltip;
	private final List<SpellAction> cachedActions;
	private final SpellIcon cachedIcon;
	
	public ImbuementTooltipComponent(ImbuementTooltip tooltip) {
		super();
		this.tooltip = tooltip;
		this.cachedActions = new ArrayList<>(tooltip.imbuement.getParts().size());
		for (SpellEffectPart effect : tooltip.imbuement.getParts()) {
			cachedActions.add(SpellEffects.SolveAction(effect.getAlteration(), effect.getElement(), effect.getElementCount()));
		}
		this.cachedIcon = SpellIcon.get(tooltip.imbuement.getIconIndex());
	}
	
	@Override
	public int getHeight() {
		return 10 + 2 + Math.max(ICON_HEIGHT, 10 * tooltip.imbuement.getParts().size());
	}

	@Override
	public int getWidth(Font p_169952_) {
		return 130;
	}
	
	@Override
	public void renderText(Font font, int x, int y, Matrix4f matrixStackIn, MultiBufferSource.BufferSource bufferIn) {
		; // render text in image layer so we can put a background
		
//		font.drawInBatch(new TranslatableComponent("tooltip.imbued").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED), x, y, 0xFFFFFFFF, true, matrixStackIn, bufferIn, false, 0, 0xF000F0);
//		
//		final List<SpellEffectPart> parts = tooltip.imbuement.getParts();
//		final int yShift = parts.size() == 1 ? 5 : 0;
//		for (int i = 0; i < cachedActions.size(); i++) {
//			final SpellEffectPart part = parts.get(i);
//			final SpellAction action = cachedActions.get(i);
//			
//			font.drawInBatch(action.getName(), x+2+ICON_HEIGHT, y + yShift + (i + 1) * 10, 0xFFFFFFFF, true, matrixStackIn, bufferIn, false, 0, 0xF000F0);
//		}
	}
	
	@Override
	public void renderImage(Font font, int x, int y, PoseStack matrixStackIn, ItemRenderer itemRenderer, int z) {
		//RenderSystem.enableBlend();
		//RenderSystem.defaultBlendFunc();
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, 2, 400);
		GuiComponent.fill(matrixStackIn, x-2, y-2, x + this.tooltipWidth + 2, y + getHeight() + -2, 0xFF4080A0);
		GuiComponent.fill(matrixStackIn, x-1, y-1, x + this.tooltipWidth + 1, y + -1 + getHeight() + -2, 0x30FF0000);
		font.drawShadow(matrixStackIn, LABEL, x, y, 0xFFFFFFFF);
		
		final List<SpellEffectPart> parts = tooltip.imbuement.getParts();
		final int yShift = parts.size() == 1 ? 5 : 0;
		for (int i = 0; i < cachedActions.size(); i++) {
			final SpellEffectPart part = parts.get(i);
			final SpellAction action = cachedActions.get(i);
			final Component label;
			if (part.getPotency() == 1f) {
				label = action.getName();
			} else {
				label = action.getName().copy().append(new TextComponent(String.format("  (%2.0f%%)", part.getPotency() * 100)).withStyle(ChatFormatting.ITALIC));
			}
			
			font.drawShadow(matrixStackIn, label, x+2+ICON_HEIGHT, y + yShift + (i + 1) * 10, 0xFFFFFFFF);
		}
		
		final int subHeight = this.getHeight() - 12;
		matrixStackIn.pushPose();
		matrixStackIn.translate(x, y + 10 + (subHeight - ICON_HEIGHT) / 2, 0);
		
		RenderSystem.enableBlend();
		RenderSystem.setShaderTexture(0, GUI_ICONS);
		this.cachedIcon.render(Minecraft.getInstance(), matrixStackIn, 0, 0, ICON_HEIGHT, ICON_HEIGHT);
		//RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, mouseX - 7, mouseY + tooltipHeight, 160, 32, 32, 32, ICON_WIDTH, ICON_HEIGHT, 256, 256);
		matrixStackIn.popPose();
		matrixStackIn.popPose();
	}
}
