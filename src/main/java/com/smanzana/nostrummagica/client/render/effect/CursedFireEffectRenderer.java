package com.smanzana.nostrummagica.client.render.effect;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.IEffectRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class CursedFireEffectRenderer implements IEffectRenderer {
	
	@SuppressWarnings("deprecation")
	public static final RenderMaterial TEX_FIRE_0 = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, NostrumMagica.Loc("block/cursed_fire_0"));
	@SuppressWarnings("deprecation")
	public static final RenderMaterial TEX_FIRE_1 = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, NostrumMagica.Loc("block/cursed_fire_1"));

	
	@Override
	public void renderEffectOnEntity(EffectInstance effect, MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		final Minecraft mc = Minecraft.getInstance();
		final ActiveRenderInfo activeInfo = mc.gameRenderer.getMainCamera();
		
		final float entYaw = MathHelper.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
		
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
	
	public static final void renderFire(MatrixStack stack, IRenderTypeBuffer typeBuffer, TextureAtlasSprite sprite, int packedLight, float width, float red, float green, float blue, float alpha) {
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
		final IVertexBuilder buffer = typeBuffer.getBuffer(Atlases.cutoutBlockSheet());
		buffer.vertex(transform, xMax, yMin, 0).color(red, green, blue, alpha).uv(uMax, vMin).overlayCoords(packedOverlay).uv2(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.vertex(transform, xMin, yMin, 0).color(red, green, blue, alpha).uv(uMin, vMin).overlayCoords(packedOverlay).uv2(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.vertex(transform, xMin, yMax, 0).color(red, green, blue, alpha).uv(uMin, vMax).overlayCoords(packedOverlay).uv2(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.vertex(transform, xMax, yMax, 0).color(red, green, blue, alpha).uv(uMax, vMax).overlayCoords(packedOverlay).uv2(packedLight).normal(normal, 0, 1, 0).endVertex();
	}
}
