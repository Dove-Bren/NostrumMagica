package com.smanzana.nostrummagica.client.render.item;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.ISpellCraftPatternRenderer;
import com.smanzana.nostrummagica.item.SpellPatternTome;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpellPatternTomeRenderer extends BlockEntityWithoutLevelRenderer {
	
	public static final SpellPatternTomeRenderer INSTANCE = new SpellPatternTomeRenderer();

	public static final ResourceLocation BASE_MODEL = NostrumMagica.Loc("item/" + SpellPatternTome.ID + "_base");
	
	protected BakedModel getBaseModel(ItemStack stack, ItemTransforms.TransformType transform) {
		return Minecraft.getInstance().getModelManager().getModel(BASE_MODEL);
	}
	
	protected void renderBase(ItemStack stack, ItemTransforms.TransformType transform, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLight, int combinedOverlay) {
		final BakedModel model = this.getBaseModel(stack, transform);
		final RenderType baseRenderType = ItemBlockRenderTypes.getRenderType(stack, true);
		
		matrixStack.pushPose();
		//matrixStack.scale(1.0F, -1.0F, -1.0F);
		VertexConsumer buffer = ItemRenderer.getFoilBufferDirect(bufferIn, baseRenderType, false, stack.hasFoil());
		RenderFuncs.RenderModel(matrixStack, buffer, model, combinedLight, combinedOverlay, 1f, 1f, 1f, 1f);
		matrixStack.popPose();
	}
	
	protected void renderIcon(SpellCraftPattern pattern, ItemStack stack, ItemTransforms.TransformType transform, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLight, int combinedOverlay) {
		@Nullable ISpellCraftPatternRenderer renderer = ISpellCraftPatternRenderer.GetRenderer(pattern);
		if (renderer != null) {
			matrixStack.pushPose();
			matrixStack.scale(1.0F, -1.0F, -1.0F);
			matrixStack.scale(1f/16f, 1f/16f, 1f/16f);
			matrixStack.translate(3, -12, -8.75);
			renderer.drawPatternIcon(matrixStack, pattern, bufferIn, 8, 8, 1f, 1f, 1f, 1f);
			matrixStack.popPose();
		}
	}
	
	@Override
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType transform, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLight, int combinedOverlay) {
		this.renderBase(stack, transform, matrixStack, bufferIn, combinedLight, combinedOverlay);
		
		@Nullable SpellCraftPattern pattern = ((SpellPatternTome) stack.getItem()).getPattern(stack);
		if (pattern != null) {
			this.renderIcon(pattern, stack, transform, matrixStack, bufferIn, combinedLight, combinedOverlay);
		}
	}
}
