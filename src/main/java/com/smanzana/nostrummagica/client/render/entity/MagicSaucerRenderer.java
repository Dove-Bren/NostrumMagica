package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.BakedModel;
import com.smanzana.nostrummagica.entity.CyclerSpellSaucerEntity;
import com.smanzana.nostrummagica.entity.SpellSaucerEntity;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class MagicSaucerRenderer<T extends SpellSaucerEntity> extends EntityRenderer<T> {
	
	private static final ResourceLocation MODEL = new ResourceLocation(NostrumMagica.MODID, "entity/magic_saucer");
	
	private BakedModel<T> mainModel;

	public MagicSaucerRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
		mainModel = new BakedModel<>(RenderType::entityTranslucent, MODEL);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ResourceLocation getTextureLocation(SpellSaucerEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
	
	@Override
	public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		
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
			matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(entityIn.getXRot()));
		}
		
		matrixStackIn.scale(.5f, .5f, .5f);
		matrixStackIn.translate(0, yOffset, 0);
		matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(-90f));
		
		mainModel.renderToBuffer(matrixStackIn, bufferIn.getBuffer(mainModel.renderType(getTextureLocation(entityIn))), packedLightIn, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], 1f);
		
		matrixStackIn.popPose();
	}
}
