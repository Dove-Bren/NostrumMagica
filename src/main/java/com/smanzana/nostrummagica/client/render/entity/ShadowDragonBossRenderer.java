package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.NostrumModelLayers;
import com.smanzana.nostrummagica.client.model.ShadowDragonBossModel;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.client.render.layer.ShadowDragonArmorLayer;
import com.smanzana.nostrummagica.client.render.layer.ShadowDragonEyesLayer;
import com.smanzana.nostrummagica.entity.boss.shadowdragon.ShadowDragonEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ShadowDragonBossRenderer extends MobRenderer<ShadowDragonEntity, ShadowDragonBossModel> {

	private static final ResourceLocation RES_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/entity/shadow_dragon.png");
	private static final ResourceLocation RES_TEXT_ETHEREAL = new ResourceLocation(NostrumMagica.MODID, "textures/entity/shadow_dragon_ethereal.png");
	
	public ShadowDragonBossRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn,
				new ShadowDragonBossModel(renderManagerIn.bakeLayer(NostrumModelLayers.ShadowDragonBoss)),
				1f);
		this.addLayer(new ShadowDragonEyesLayer(this));
		this.addLayer(new ShadowDragonArmorLayer(this, new ShadowDragonBossModel(renderManagerIn.bakeLayer(NostrumModelLayers.ShadowDragonBossArmor))));
	}
	
	@Override
	public ResourceLocation getTextureLocation(ShadowDragonEntity entity) {
		return entity.isEthereal() ? RES_TEXT_ETHEREAL : RES_TEXT;
	}
	
	@Override
	protected void setupRotations(ShadowDragonEntity entityIn, PoseStack matrixStackIn, float partialTicks, float bobProg, float bodyRotYaw) {
		super.setupRotations(entityIn, matrixStackIn, partialTicks, bobProg, bodyRotYaw);
	}
	
	@Override
	public void render(ShadowDragonEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		final ShadowDragonEntity.BattlePose pose = entityIn.getBattlePose();
		if (pose == ShadowDragonEntity.BattlePose.INACTIVE) {
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, 1, 0);
			renderChain(entityIn, matrixStackIn, bufferIn, packedLightIn, partialTicks, new Vector3f(18, 11, 14));
			renderChain(entityIn, matrixStackIn, bufferIn, packedLightIn, partialTicks, new Vector3f(-18, 11, 14));
			renderChain(entityIn, matrixStackIn, bufferIn, packedLightIn, partialTicks, new Vector3f(-18, 11, -14));
			renderChain(entityIn, matrixStackIn, bufferIn, packedLightIn, partialTicks, new Vector3f(18, 11, -14));
			matrixStackIn.popPose();
		}
	}
	
	protected void renderChargeBeam(ShadowDragonEntity ent, BlockPos pos, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLight) {
		matrixStack.pushPose();
		
		final float offsetX = (float) ((pos.getX() + .5) - ent.getX());
		final float offsetY = (float) (((pos.getY() - 1) + .5) - ent.getY());
		final float offsetZ = (float) ((pos.getZ() + .5) - ent.getZ());
		
		matrixStack.translate(offsetX, offsetY - 1, offsetZ);
		
		EnderDragonRenderer.renderCrystalBeams(-offsetX, -offsetY, -offsetZ, partialTicks, ent.tickCount, matrixStack, bufferIn, packedLight);
		matrixStack.popPose();
	}
	
	protected void renderChain(ShadowDragonEntity ent, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, float partialTicks, Vector3f chainOffset) {
		final float length = Mth.sqrt(Mth.square(chainOffset.x()) + Mth.square(chainOffset.y()) + Mth.square(chainOffset.z())); 
		final int points = (int)(length / .5f);
		final float linkWidth = .25f;
		final VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.LOCKEDCHEST_CHAIN);
		final Matrix4f transform = matrixStackIn.last().pose();
		
		// alpha will be 0 on edges.
		// On low-i, it will quickly become 1 (2-4 segments)
		// it will slowly go back to 0 after 50%
		
		for (int i = 0; i < points; i++)
		{
			final float prog1 = ((float) i / (float)points);
			final float px1 = chainOffset.x() * prog1;
			final float py1 = chainOffset.y() * prog1;
			final float pz1 = chainOffset.z() * prog1;
			final float alpha1 = i < 4 ? 0f :
					i < 5 ? ((float)i / 5f) :
					i < (points / 2) ? 1f
					: 1f - ((i - (points / 2f)) / (points / 2f))
					;
			final float v1 = (i % 2 == 0 ? 0 : 1);
			final float offZ1 = linkWidth/2;
			final float offX1 = linkWidth/2;
			
			final float prog2 = ((float) (i+1) / (float)points);
			final float px2 = chainOffset.x() * prog2;
			final float py2 = chainOffset.y() * prog2;
			final float pz2 = chainOffset.z() * prog2;
			final float alpha2 = (i+1) < 4 ? 0f :
				(i+1) < 5 ? ((float)(i+1) / 5f) :
				(i+1) < (points / 2) ? 1f
				: 1f - (((i+1) - (points / 2f)) / (points / 2f))
				;
			final float v2 = ((i+1) % 2 == 0 ? 0 : 1);
			final float offZ2 = linkWidth/2;
			final float offX2 = linkWidth/2;
			
			buffer.vertex(transform, px1 - offX1, py1, pz1).color(1f, 1f, 1f, alpha1).uv(0, v1).uv2(packedLightIn).endVertex();
			buffer.vertex(transform, px1 + offX1, py1, pz1).color(1f, 1f, 1f, alpha1).uv(1, v1).uv2(packedLightIn).endVertex();
			buffer.vertex(transform, px2 + offX2, py2, pz2).color(1f, 1f, 1f, alpha2).uv(1, v2).uv2(packedLightIn).endVertex();
			buffer.vertex(transform, px2 - offX2, py2, pz2).color(1f, 1f, 1f, alpha2).uv(0, v2).uv2(packedLightIn).endVertex();
			
			// Cross quad
			buffer.vertex(transform, px1, py1, pz1 - offZ1).color(1f, 1f, 1f, alpha1).uv(0, v1 + .5f).uv2(packedLightIn).endVertex();
			buffer.vertex(transform, px1, py1, pz1 + offZ1).color(1f, 1f, 1f, alpha1).uv(1, v1 + .5f).uv2(packedLightIn).endVertex();
			buffer.vertex(transform, px2, py2, pz2 + offZ2).color(1f, 1f, 1f, alpha2).uv(1, v2 + .5f).uv2(packedLightIn).endVertex();
			buffer.vertex(transform, px2, py2, pz2 - offZ2).color(1f, 1f, 1f, alpha2).uv(0, v2 + .5f).uv2(packedLightIn).endVertex();
		}
	}
}
