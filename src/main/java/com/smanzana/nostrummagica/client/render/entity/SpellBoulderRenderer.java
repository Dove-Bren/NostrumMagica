package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.entity.SpellBoulderEntity;
import com.smanzana.nostrummagica.util.Color;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class SpellBoulderRenderer extends EntityRenderer<SpellBoulderEntity> {
	
	//private final CursedGlassModel model;
	
	public SpellBoulderRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
		//this.model = new CursedGlassModel();
	}

	@SuppressWarnings("deprecation")
	@Override
	public ResourceLocation getTextureLocation(SpellBoulderEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
	
	@Override
	public void render(SpellBoulderEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		final float width = entityIn.getBbWidth();
		final float height = entityIn.getBbHeight();
		BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.SEA_LANTERN.defaultBlockState());
		VertexConsumer buffer = bufferIn.getBuffer(RenderType.translucentMovingBlock());
		final Vec3 move = entityIn.position().subtract(entityIn.xo, entityIn.yo, entityIn.zo);
		final float yaw = (float)(Mth.atan2(move.x, move.z) * (double)(180F / (float)Math.PI));
		final float age = entityIn.tickCount + partialTicks;
		
		final float growTime = 20f;
		final float growScale = (age < growTime) ? (age / growTime) : 1f;
		
		final Color color = new Color(.4f, .6f, .4f, .4f);
		
		RenderSystem.depthMask(false);
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, height/2, 0);
		matrixStackIn.scale(width * growScale, height * growScale, width * growScale);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(yaw));
		matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(age * 15f));
		matrixStackIn.translate(-.5, -.5, -.5);
		RenderFuncs.RenderModel(matrixStackIn, buffer, model, packedLightIn, OverlayTexture.NO_OVERLAY, color.red, color.green, color.blue, color.alpha);
		matrixStackIn.popPose();
	}
	
}
