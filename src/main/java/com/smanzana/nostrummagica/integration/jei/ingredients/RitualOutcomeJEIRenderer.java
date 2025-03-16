package com.smanzana.nostrummagica.integration.jei.ingredients;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.jei.RitualOutcomeWrapper;
import com.smanzana.nostrummagica.ritual.outcome.IItemRitualOutcome;
import com.smanzana.nostrummagica.ritual.outcome.IRitualOutcome;
import com.smanzana.nostrummagica.util.RenderFuncs;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class RitualOutcomeJEIRenderer implements IIngredientRenderer<RitualOutcomeWrapper> {
	
	public static final ResourceLocation RITUAL_TEXTURE = new ResourceLocation(NostrumMagica.MODID, "textures/gui/book_extras.png");
	public static final int RITUAL_TEXT_WIDTH = 256;
	public static final int RITUAL_TEXT_HEIGHT = 128;
	public static final int RITUAL_TEXT_TABLET_VOFFSET = 64;
	public static final int RITUAL_TEXT_TABLET_WIDTH = 18;


	private static RitualOutcomeJEIRenderer instance = null;
	public static RitualOutcomeJEIRenderer instance() {
		if (instance == null) {
			instance = new RitualOutcomeJEIRenderer();
		}
		return instance;
	}
	
	@Override
	public void render(PoseStack matrixStackIn, int xPosition, int yPosition, RitualOutcomeWrapper ingredient) {
		if (ingredient == null)
			return;
		Minecraft.getInstance().getTextureManager().bind(RITUAL_TEXTURE);
		ItemStack item = fetchItem(ingredient.getOutcome());
		RenderSystem.enableBlend();
		if (item != null) {
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, xPosition,
					yPosition,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_TABLET_VOFFSET,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_WIDTH, RITUAL_TEXT_HEIGHT);
			
			Minecraft.getInstance().getItemRenderer().renderGuiItem(item, xPosition + 1, yPosition + 1);
		} else {
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, xPosition,
					yPosition,
					0,
					RITUAL_TEXT_TABLET_VOFFSET,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_TABLET_WIDTH,
					RITUAL_TEXT_WIDTH, RITUAL_TEXT_HEIGHT);
		}
	}

	@Override
	public List<Component> getTooltip(RitualOutcomeWrapper ingredient, TooltipFlag flag) {
		if (ingredient == null)
			return new LinkedList<>();
		
		ItemStack item = fetchItem(ingredient.getOutcome());
		if (item == null)
			return ingredient.getOutcome().getDescription();
		else
			return Lists.newArrayList(item.getHoverName());
	}

	@Override
	public Font getFontRenderer(Minecraft minecraft, RitualOutcomeWrapper ingredient) {
		if (ingredient == null)
			return minecraft.font;
		
		Font render = null;
		ItemStack stack = fetchItem(ingredient.getOutcome());
		if (stack != null)
			render = stack.getItem().getFontRenderer(stack);
		
		if (render == null)
			render = minecraft.font;
		
		return render;
	}
	
	private static ItemStack fetchItem(IRitualOutcome outcome) {
		if (outcome != null && outcome instanceof IItemRitualOutcome) {
			return ((IItemRitualOutcome) outcome).getResult();
		}
		
		return null;
	}

}
