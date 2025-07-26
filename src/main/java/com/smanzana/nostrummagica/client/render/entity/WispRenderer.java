package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.BakedModel;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.entity.WispEntity;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class WispRenderer extends EntityRenderer<WispEntity> {
	
	private static final ResourceLocation MODEL = new ResourceLocation(NostrumMagica.MODID, "entity/orb");

	protected BakedModel<WispEntity> orbModel;
	
	public WispRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
		orbModel = new BakedModel<>(NostrumRenderTypes::GetBlendedEntity, MODEL);
	}
	
	protected int getColor(WispEntity wisp) {
		final EMagicElement element = (wisp == null ? null : wisp.getElement());
		return (element == null ? EMagicElement.NEUTRAL : element).getColor();
	}
	
	protected void renderModel(WispEntity entityIn, PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn,
			int packedOverlayIn, float red, float green, float blue, float alpha, float partialTicks) {
		// Render orb twice: one at full transparency, and again larger and more translucent (for a glow)
		final float ticks = 3 * 20;
		final float frac = (((float) entityIn.tickCount + partialTicks) % ticks) / ticks;
		final float adjustedScale = (float) (Math.sin(frac * Math.PI * 2) * .05) + .5f;
		final float yOffset = entityIn.getBbHeight() / 2;
		
		matrixStackIn.pushPose();
		//GlStateManager.rotatef(-90f, 1f, 0f, 0f);
		matrixStackIn.translate(0, yOffset, 0);
		
		// For inner orb, make it 40% the base size but perfectly opaque
		matrixStackIn.pushPose();
		matrixStackIn.scale(.3f, .3f, .3f);
		orbModel.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha * .8f);
		matrixStackIn.popPose();
		
		// For outer orb, make (0x30/0xFF)% as bright and scale up
		matrixStackIn.pushPose();
		matrixStackIn.scale(adjustedScale, adjustedScale, adjustedScale);
		orbModel.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha * .1875f);
		matrixStackIn.popPose();
		
		matrixStackIn.popPose();
	}
	
	@Override
	public void render(WispEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		final float[] color = ColorUtil.ARGBToColor(getColor(entityIn));
		VertexConsumer buffer;
		final int packedOverlayIn = OverlayTexture.NO_OVERLAY;
		
		//renderModel(entityIn, matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], color[3], partialTicks);
		final float ticks = 3 * 20;
		final float frac = (((float) entityIn.tickCount + partialTicks) % ticks) / ticks;
		final float adjustedScale = (float) (Math.sin(frac * Math.PI * 2) * .05) + .5f;
		final float yOffset = entityIn.getBbHeight() / 2;
		
		matrixStackIn.pushPose();
		//GlStateManager.rotatef(-90f, 1f, 0f, 0f);
		matrixStackIn.translate(0, yOffset, 0);
		
		// For inner orb, make it 40% the base size but perfectly opaque
		buffer = bufferIn.getBuffer(RenderType.entityCutout(getTextureLocation(entityIn)));
		matrixStackIn.pushPose();
		matrixStackIn.scale(.3f, .3f, .3f);
		orbModel.renderToBuffer(matrixStackIn, buffer, packedLightIn, packedOverlayIn, color[0], color[1], color[2], color[3]);
		matrixStackIn.popPose();
		
		// For outer orb, make (0x30/0xFF)% as bright and scale up
		buffer = bufferIn.getBuffer(this.orbModel.renderType(getTextureLocation(entityIn)));
		matrixStackIn.pushPose();
		matrixStackIn.scale(adjustedScale, adjustedScale, adjustedScale);
		orbModel.renderToBuffer(matrixStackIn, buffer, packedLightIn, packedOverlayIn, color[0], color[1], color[2], color[3] * .1875f);
		matrixStackIn.popPose();
		
		matrixStackIn.popPose();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public ResourceLocation getTextureLocation(WispEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
	
}
