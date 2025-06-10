package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.NostrumModelLayers;
import com.smanzana.nostrummagica.client.model.PlayerStatueModel;
import com.smanzana.nostrummagica.client.render.layer.PlayerStatueArmorLayer;
import com.smanzana.nostrummagica.entity.boss.playerstatue.PlayerStatueEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PlayerStatueRenderer extends MobRenderer<PlayerStatueEntity, PlayerStatueModel> {

	private static final ResourceLocation RES_TEXT = new ResourceLocation(NostrumMagica.MODID, "textures/entity/player_statue.png");
	
	public PlayerStatueRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn, new PlayerStatueModel(renderManagerIn.bakeLayer(NostrumModelLayers.PlayerStatue)), 1f);
		this.addLayer(new PlayerStatueArmorLayer(this, renderManagerIn.getModelSet()));
	}
	
	protected void renderWeapon(PlayerStatueEntity entity, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn,
			int packedOverlayIn, float red, float green, float blue, float alpha, float rotation) {
		
		matrixStackIn.pushPose();
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-entity.yBodyRot));
		matrixStackIn.translate(0, 1, 0);
		matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(rotation));
		matrixStackIn.translate(.45, 0, -.45);
		Minecraft.getInstance().getItemRenderer()
			.renderStatic(new ItemStack(Items.TRIDENT), TransformType.THIRD_PERSON_LEFT_HAND, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, 0);
		matrixStackIn.popPose();
	}
	
	@Override
	protected void setupRotations(PlayerStatueEntity entityIn, PoseStack matrixStackIn, float partialTicks, float bobProg, float bodyRotYaw) {
		super.setupRotations(entityIn, matrixStackIn, partialTicks, bobProg, bodyRotYaw);
	}
	
	@Override
	public void render(PlayerStatueEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		final PlayerStatueEntity.BattlePose pose = entityIn.getBattlePose();
		final float ticksInPose = partialTicks + entityIn.getTicksInPose();
		
		float yOffset = 0;
		float zRot = 0;
		float weaponRotZ = 0;
		float red = 1f;
		float green = 1f;
		float blue = 1f;
		
		switch (pose) {
		case UPRIGHT:
			{
				// base standing
				yOffset = 0;
				zRot = 0;
				weaponRotZ = 180f;
			}
			break;
		case RECOVERING:
		{
			// stand back up
			final float prog = Math.min(1f, ticksInPose / 10f);
			yOffset = Mth.lerp(prog, .35f, 0);
			zRot = Mth.lerp(prog, 90f, 0);
			weaponRotZ = 180f;
		}
		break;
		case TOPPLED:
			{
				// put on side
				final float prog = Math.min(1f, ticksInPose / 10f);
				yOffset = Mth.lerp(prog, 0, .35f);
				zRot = Mth.lerp(prog, 0, 90f);
				weaponRotZ = 180f;
			}
			break;
		case ACTIVATING:
			{
				// Saturate, and twist weapon
				final float prog = Math.min(1f, ticksInPose / 60f);
				yOffset = 0;
				zRot = 0;
				weaponRotZ = Mth.lerp(prog, 0, 180f);
				
				red = Mth.lerp(prog, .8f, 1f);
				green = Mth.lerp(prog, .8f, 1f);
				blue = Mth.lerp(prog, .2f, 1f);
			}
			break;
		case INACTIVE:
			yOffset = 0;
			zRot = 0;
			weaponRotZ = 0;
			red = .8f;
			green = .8f;
			blue = .2f;
			break;
		}
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, yOffset, 0);
		matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(zRot));
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		

		//renderWeakspot(entityIn, matrixStackIn, null, packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		renderWeapon(entityIn, matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY, red, green, blue, 1f, weaponRotZ);
		//renderArmor(entityIn, matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		
		matrixStackIn.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(PlayerStatueEntity entity) {
		return RES_TEXT;
	}
	
}
