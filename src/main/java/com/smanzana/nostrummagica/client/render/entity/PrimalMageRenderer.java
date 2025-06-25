package com.smanzana.nostrummagica.client.render.entity;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.CursedGlassModel;
import com.smanzana.nostrummagica.client.model.NostrumModelLayers;
import com.smanzana.nostrummagica.client.model.PrimalMageModel;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.client.render.layer.PrimalMageArmorLayer;
import com.smanzana.nostrummagica.entity.boss.primalmage.PrimalMageEntity;
import com.smanzana.nostrummagica.entity.boss.primalmage.PrimalMageEntity.BattlePose;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.ColorUtil;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class PrimalMageRenderer extends IllagerRenderer<PrimalMageEntity> {
	
	public static final ResourceLocation TEXT = NostrumMagica.Loc("textures/entity/primal_mage.png");
	
	protected CursedGlassModel shieldModel;

	public PrimalMageRenderer(EntityRendererProvider.Context context) {
		// This is mostly copied from the evoker renderer, but its constructor doesn't let us descend from it and modify the model
		super(context, new PrimalMageModel(context.bakeLayer(NostrumModelLayers.PrimalMage)), .5f);
		
		this.addLayer(new ItemInHandLayer<PrimalMageEntity, IllagerModel<PrimalMageEntity>>(this) {
			public void render(PoseStack p_114569_, MultiBufferSource p_114570_, int p_114571_, PrimalMageEntity p_114572_, float p_114573_, float p_114574_, float p_114575_, float p_114576_, float p_114577_, float p_114578_) {
				if (p_114572_.isCastingSpell()) {
					super.render(p_114569_, p_114570_, p_114571_, p_114572_, p_114573_, p_114574_, p_114575_, p_114576_, p_114577_, p_114578_);
				}
			}
		});
		this.addLayer(new PrimalMageArmorLayer(this, context.getModelSet()));
		this.shieldModel = new CursedGlassModel();
	}

	@Override
	public ResourceLocation getTextureLocation(PrimalMageEntity entity) {
		return TEXT;
	}
	
	protected void renderChargeBeam(PrimalMageEntity ent, BlockPos pos, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLight) {
		matrixStack.pushPose();
		
		final float offsetX = (float) ((pos.getX() + .5) - ent.getX());
		final float offsetY = (float) (((pos.getY() - 1) + .5) - ent.getY());
		final float offsetZ = (float) ((pos.getZ() + .5) - ent.getZ());
		
		matrixStack.translate(offsetX, offsetY - 1, offsetZ);
		
		EnderDragonRenderer.renderCrystalBeams(-offsetX, -offsetY, -offsetZ, partialTicks, ent.tickCount, matrixStack, bufferIn, packedLight);
		matrixStack.popPose();
	}
	
	protected void renderElementalShield(PrimalMageEntity ent, EMagicElement element, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		final float stateTicks = ent.getTicksInPose() + partialTicks;
		
		matrixStackIn.pushPose();
		
		if (stateTicks < 20f) {
			final float shieldPopinProg = stateTicks < 20 ? (stateTicks / 20f) : 1f;
			final float scale = .2f + (.8f * shieldPopinProg);
			matrixStackIn.scale(scale, scale, scale);
			matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-90f * (1 - shieldPopinProg)));
		}
		
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees((stateTicks * (360f * .0025f)) % 360f));
		
		shieldModel.renderToBuffer(matrixStackIn, bufferIn.getBuffer(ItemBlockRenderTypes.getRenderType(Blocks.AIR.defaultBlockState(), false)), packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		
		final float[] color = ColorUtil.ARGBToColor(element.getColor());
		final float glowPeriod = 20 * 2;
		final float glowProg = ((ent.tickCount + partialTicks) % glowPeriod) / glowPeriod;
		
		final float glow = .5f + (.25f * (float) Math.sin(glowProg * Math.PI * 2));
		
		shieldModel.renderDecal(matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], color[3] * glow);
		
		
		matrixStackIn.popPose();
	}
	
	protected void renderChain(PrimalMageEntity ent, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, float partialTicks, Vector3f chainOffset) {
		
		final int points = 20;
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
	
	@Override
	public void render(PrimalMageEntity ent, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLight) {
		final float ticksInPose = partialTicks + ent.getTicksInPose();
		
		// Maybe rotate if fallen
		matrixStack.pushPose();
		final BattlePose pose = ent.getBattlePose();
		if (pose == BattlePose.FALLEN) {
			// put on side
			final float prog = Math.min(1f, ticksInPose / 10f);
			final float yOffset = Mth.lerp(prog, 0, .35f);
			final float xOffset = Mth.lerp(prog, 0, ent.getBbHeight() / 2f);
			final float zRot = Mth.lerp(prog, 0, 90f);
			matrixStack.translate(xOffset, yOffset, 0);
			matrixStack.mulPose(Vector3f.ZP.rotationDegrees(zRot));
		} else if (pose == BattlePose.RECOVERING) {
			// stand back up
			final float prog = Math.min(1f, ticksInPose / 10f);
			final float yOffset = Mth.lerp(prog, .35f, 0);
			final float xOffset = Mth.lerp(prog, ent.getBbHeight() / 2f, 0);
			final float zRot = Mth.lerp(prog, 90f, 0);
			matrixStack.translate(xOffset, yOffset, 0);
			matrixStack.mulPose(Vector3f.ZP.rotationDegrees(zRot));
		}
		
		super.render(ent, entityYaw, partialTicks, matrixStack, bufferIn, packedLight);
		matrixStack.popPose();
		
		if (pose == BattlePose.INACTIVE || pose == BattlePose.ACTIVATING) {
			matrixStack.pushPose();
			matrixStack.translate(0, .75f, 0);
			
			this.renderChain(ent, matrixStack, bufferIn, packedLight, partialTicks, new Vector3f(3f, 7f, 3f));
			this.renderChain(ent, matrixStack, bufferIn, packedLight, partialTicks, new Vector3f(-3f, 7f, -3f));
			matrixStack.popPose();
		}
		
		Optional<EMagicElement> shieldElement = ent.getShieldElement();
		if (shieldElement.isPresent()) {
			this.renderElementalShield(ent, shieldElement.get(), partialTicks, matrixStack, bufferIn, packedLight);
		}
		
		for (BlockPos pos : ent.getChargeLocations()) {
			renderChargeBeam(ent, pos, partialTicks, matrixStack, bufferIn, packedLight);
		}
	}
}
