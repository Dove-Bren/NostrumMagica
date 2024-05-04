package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityCyclerSpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer;
import com.smanzana.nostrummagica.utils.ColorUtil;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class RenderMagicSaucer<T extends EntitySpellSaucer> extends EntityRenderer<T> {
	
	private static final ResourceLocation MODEL = new ResourceLocation(NostrumMagica.MODID, "entity/magic_saucer");
	
	private ModelBaked<T> mainModel;

	public RenderMagicSaucer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
		mainModel = new ModelBaked<>(RenderType::getEntityTranslucent, MODEL);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ResourceLocation getEntityTexture(EntitySpellSaucer entity) {
		return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
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
		
		final float yOffset = entityIn.getHeight() / 2;
		final float[] color = ColorUtil.ARGBToColor(entityIn.getElement().getColor());
		
		matrixStackIn.push();
		if (!(entityIn instanceof EntityCyclerSpellSaucer)) {
			//matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90.0F - entityLiving.rotationPitch));
			matrixStackIn.rotate(Vector3f.XP.rotationDegrees(entityIn.rotationPitch));
		}
		
		matrixStackIn.scale(.5f, .5f, .5f);
		matrixStackIn.translate(0, yOffset, 0);
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90f));
		
		mainModel.render(matrixStackIn, bufferIn.getBuffer(mainModel.getRenderType(getEntityTexture(entityIn))), packedLightIn, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], 1f);
		
		matrixStackIn.pop();
	}
}
