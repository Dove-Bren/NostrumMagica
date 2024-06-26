package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelBaked;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.entity.WispEntity;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class RenderWisp extends EntityRenderer<WispEntity> {
	
	private static final ResourceLocation MODEL = new ResourceLocation(NostrumMagica.MODID, "entity/orb");

	protected ModelBaked<WispEntity> orbModel;
	
	public RenderWisp(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
		orbModel = new ModelBaked<>(NostrumRenderTypes::GetBlendedEntity, MODEL);
	}
	
	protected int getColor(WispEntity wisp) {
		final EMagicElement element = (wisp == null ? null : wisp.getElement());
		return (element == null ? EMagicElement.PHYSICAL : element).getColor();
	}
	
	protected void renderModel(WispEntity entityIn, MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn,
			int packedOverlayIn, float red, float green, float blue, float alpha, float partialTicks) {
		// Render orb twice: one at full transparency, and again larger and more translucent (for a glow)
		final float ticks = 3 * 20;
		final float frac = (((float) entityIn.ticksExisted + partialTicks) % ticks) / ticks;
		final float adjustedScale = (float) (Math.sin(frac * Math.PI * 2) * .05) + .5f;
		final float yOffset = entityIn.getHeight() / 2;
		
		matrixStackIn.push();
		//GlStateManager.rotatef(-90f, 1f, 0f, 0f);
		matrixStackIn.translate(0, yOffset, 0);
		
		// For inner orb, make it 40% the base size but perfectly opaque
		matrixStackIn.push();
		matrixStackIn.scale(.3f, .3f, .3f);
		orbModel.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha * .8f);
		matrixStackIn.pop();
		
		// For outer orb, make (0x30/0xFF)% as bright and scale up
		matrixStackIn.push();
		matrixStackIn.scale(adjustedScale, adjustedScale, adjustedScale);
		orbModel.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha * .1875f);
		matrixStackIn.pop();
		
		matrixStackIn.pop();
	}
	
	@Override
	public void render(WispEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		final float[] color = ColorUtil.ARGBToColor(getColor(entityIn));
		IVertexBuilder buffer;
		final int packedOverlayIn = OverlayTexture.NO_OVERLAY;
		
		//renderModel(entityIn, matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], color[3], partialTicks);
		final float ticks = 3 * 20;
		final float frac = (((float) entityIn.ticksExisted + partialTicks) % ticks) / ticks;
		final float adjustedScale = (float) (Math.sin(frac * Math.PI * 2) * .05) + .5f;
		final float yOffset = entityIn.getHeight() / 2;
		
		matrixStackIn.push();
		//GlStateManager.rotatef(-90f, 1f, 0f, 0f);
		matrixStackIn.translate(0, yOffset, 0);
		
		// For inner orb, make it 40% the base size but perfectly opaque
		buffer = bufferIn.getBuffer(RenderType.getEntityCutout(getEntityTexture(entityIn)));
		matrixStackIn.push();
		matrixStackIn.scale(.3f, .3f, .3f);
		orbModel.render(matrixStackIn, buffer, packedLightIn, packedOverlayIn, color[0], color[1], color[2], color[3]);
		matrixStackIn.pop();
		
		// For outer orb, make (0x30/0xFF)% as bright and scale up
		buffer = bufferIn.getBuffer(this.orbModel.getRenderType(getEntityTexture(entityIn)));
		matrixStackIn.push();
		matrixStackIn.scale(adjustedScale, adjustedScale, adjustedScale);
		orbModel.render(matrixStackIn, buffer, packedLightIn, packedOverlayIn, color[0], color[1], color[2], color[3] * .1875f);
		matrixStackIn.pop();
		
		matrixStackIn.pop();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public ResourceLocation getEntityTexture(WispEntity entity) {
		return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	}
	
}
