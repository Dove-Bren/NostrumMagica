package com.smanzana.nostrummagica.client.render.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.IEffectRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.util.Mth;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

public class CursedFireEffectRenderer implements IEffectRenderer {
	
	@SuppressWarnings("deprecation")
	public static final Material TEX_FIRE_0 = new Material(TextureAtlas.LOCATION_BLOCKS, NostrumMagica.Loc("block/cursed_fire_0"));
	@SuppressWarnings("deprecation")
	public static final Material TEX_FIRE_1 = new Material(TextureAtlas.LOCATION_BLOCKS, NostrumMagica.Loc("block/cursed_fire_1"));

	
	@Override
	public void renderEffectOnEntity(MobEffectInstance effect, PoseStack stack, MultiBufferSource typeBuffer, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		final Minecraft mc = Minecraft.getInstance();
		final Camera activeInfo = mc.gameRenderer.getMainCamera();
		
		final float entYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
		
		stack.pushPose();
		
		// Stack is offset to up. Undo
		stack.translate(0, (double)+1.501F, 0);
		
		stack.mulPose(Vector3f.YP.rotationDegrees(180f + activeInfo.getYRot() - entYaw));
		stack.translate(0, 0, -0.32f);
		
		// These positions set to player scale. So scale to entities dimensions
		final float playerWidth = .6f;
		final float playerHeight = 1.8f;
		stack.scale(entity.getBbWidth() / playerWidth, entity.getBbHeight() / playerHeight, entity.getBbWidth() / playerWidth);
		{
			stack.pushPose();
			stack.translate(.5, (-1.501F) + .2, 0);
			renderFire(stack, typeBuffer, TEX_FIRE_0.sprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
			stack.popPose();
			stack.pushPose();
			stack.translate(-.5, (-1.501F) - .35, 0);
			renderFire(stack, typeBuffer, TEX_FIRE_1.sprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
			stack.popPose();
			stack.pushPose();
			stack.translate(-.4, (-1.501F) + 1.1, 0);
			renderFire(stack, typeBuffer, TEX_FIRE_0.sprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
			stack.popPose();
			stack.pushPose();
			stack.translate(-.1, (-1.501F) + .45, 0);
			renderFire(stack, typeBuffer, TEX_FIRE_1.sprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
			stack.popPose();
			stack.pushPose();
			stack.translate(.6, (-1.501F) + -.5, 0);
			renderFire(stack, typeBuffer, TEX_FIRE_1.sprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
			stack.popPose();
		}
		stack.popPose();
	}
	
	public static final void renderFire(PoseStack stack, MultiBufferSource typeBuffer, TextureAtlasSprite sprite, int packedLight, float width, float red, float green, float blue, float alpha) {
		final Matrix4f transform = stack.last().pose();
		final Matrix3f normal = stack.last().normal();
		final int packedOverlay = OverlayTexture.NO_OVERLAY;
		final float xMin = -width/2f;
		final float xMax = width/2f;
		final float yMin = -width/2f;
		final float yMax = width/2f;
		final float uMin = sprite.getU0();
		final float uMax = sprite.getU1();
		final float vMin = sprite.getV0();
		final float vMax = sprite.getV1();
		final VertexConsumer buffer = typeBuffer.getBuffer(Sheets.cutoutBlockSheet());
		buffer.vertex(transform, xMax, yMin, 0).color(red, green, blue, alpha).uv(uMax, vMin).overlayCoords(packedOverlay).uv2(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.vertex(transform, xMin, yMin, 0).color(red, green, blue, alpha).uv(uMin, vMin).overlayCoords(packedOverlay).uv2(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.vertex(transform, xMin, yMax, 0).color(red, green, blue, alpha).uv(uMin, vMax).overlayCoords(packedOverlay).uv2(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.vertex(transform, xMax, yMax, 0).color(red, green, blue, alpha).uv(uMax, vMax).overlayCoords(packedOverlay).uv2(packedLight).normal(normal, 0, 1, 0).endVertex();
	}
}
