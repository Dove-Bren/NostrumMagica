package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ModelBaked;
import com.smanzana.nostrummagica.entity.CyclerSpellSaucerEntity;
import com.smanzana.nostrummagica.entity.SpellSaucerEntity;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class RenderMagicSaucer<T extends SpellSaucerEntity> extends EntityRenderer<T> {
	
	private static final ResourceLocation MODEL = new ResourceLocation(NostrumMagica.MODID, "entity/magic_saucer");
	
	private ModelBaked<T> mainModel;

	public RenderMagicSaucer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
		mainModel = new ModelBaked<>(RenderType::entityTranslucent, MODEL);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ResourceLocation getTextureLocation(SpellSaucerEntity entity) {
		return AtlasTexture.LOCATION_BLOCKS;
	}
	
	@Override
	public void render(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		
		// Cyclers used to render where they 'should' be instead of where they were, but I'm not super sure how to do that with this rendering stack.
//		if (entityIn instanceof EntityCyclerSpellSaucer) {
//        	EntityCyclerSpellSaucer cycler = (EntityCyclerSpellSaucer) entityIn;
//        	
//        	// Instead of rendering real position, render where we should basically be
//        	//Vector vec = cycler.getTargetOffsetLoc(partialTicks); // TODO should this just be the target pos? Otherwise it cycles the player even if it's actually on another player
//        	Vector vec = cycler.getTargetLoc(partialTicks);
//        	vec.subtract(NostrumMagica.instance.proxy.getPlayer().getEyePosition(partialTicks));
//        	
//        	GlStateManager.translated(vec.x, vec.y, vec.z);
//        }
		
		final float yOffset = entityIn.getBbHeight() / 2;
		final float[] color = ColorUtil.ARGBToColor(entityIn.getElement().getColor());
		
		matrixStackIn.pushPose();
		if (!(entityIn instanceof CyclerSpellSaucerEntity)) {
			//matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90.0F - entityLiving.rotationPitch));
			matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(entityIn.xRot));
		}
		
		matrixStackIn.scale(.5f, .5f, .5f);
		matrixStackIn.translate(0, yOffset, 0);
		matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(-90f));
		
		mainModel.renderToBuffer(matrixStackIn, bufferIn.getBuffer(mainModel.renderType(getTextureLocation(entityIn))), packedLightIn, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], 1f);
		
		matrixStackIn.popPose();
	}
}
