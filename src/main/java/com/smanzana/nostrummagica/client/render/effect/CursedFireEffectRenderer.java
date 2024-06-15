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
	public static final RenderMaterial TEX_FIRE_0 = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, NostrumMagica.Loc("block/cursed_fire_0"));
	@SuppressWarnings("deprecation")
	public static final RenderMaterial TEX_FIRE_1 = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, NostrumMagica.Loc("block/cursed_fire_1"));

	
	@Override
	public void renderEffectOnEntity(EffectInstance effect, MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		final Minecraft mc = Minecraft.getInstance();
		final ActiveRenderInfo activeInfo = mc.gameRenderer.getActiveRenderInfo();
		
		final float entYaw = MathHelper.interpolateAngle(partialTicks, entity.prevRenderYawOffset, entity.renderYawOffset);
		
		stack.push();
		
		// Stack is offset to up. Undo
		stack.translate(0, (double)+1.501F, 0);
		
		stack.rotate(Vector3f.YP.rotationDegrees(180f + activeInfo.getYaw() - entYaw));
		stack.translate(0, 0, -0.32f);
		
		// These positions set to player scale. So scale to entities dimensions
		final float playerWidth = .6f;
		final float playerHeight = 1.8f;
		stack.scale(entity.getWidth() / playerWidth, entity.getHeight() / playerHeight, entity.getWidth() / playerWidth);
		{
			stack.push();
			stack.translate(.5, (-1.501F) + .2, 0);
			renderFire(stack, typeBuffer, TEX_FIRE_0.getSprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
			stack.pop();
			stack.push();
			stack.translate(-.5, (-1.501F) - .35, 0);
			renderFire(stack, typeBuffer, TEX_FIRE_1.getSprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
			stack.pop();
			stack.push();
			stack.translate(-.4, (-1.501F) + 1.1, 0);
			renderFire(stack, typeBuffer, TEX_FIRE_0.getSprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
			stack.pop();
			stack.push();
			stack.translate(-.1, (-1.501F) + .45, 0);
			renderFire(stack, typeBuffer, TEX_FIRE_1.getSprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
			stack.pop();
			stack.push();
			stack.translate(.6, (-1.501F) + -.5, 0);
			renderFire(stack, typeBuffer, TEX_FIRE_1.getSprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
			stack.pop();
		}
		stack.pop();
	}
	
	public static final void renderFire(MatrixStack stack, IRenderTypeBuffer typeBuffer, TextureAtlasSprite sprite, int packedLight, float width, float red, float green, float blue, float alpha) {
		final Matrix4f transform = stack.getLast().getMatrix();
		final Matrix3f normal = stack.getLast().getNormal();
		final int packedOverlay = OverlayTexture.NO_OVERLAY;
		final float xMin = -width/2f;
		final float xMax = width/2f;
		final float yMin = -width/2f;
		final float yMax = width/2f;
		final float uMin = sprite.getMinU();
		final float uMax = sprite.getMaxU();
		final float vMin = sprite.getMinV();
		final float vMax = sprite.getMaxV();
		final IVertexBuilder buffer = typeBuffer.getBuffer(Atlases.getCutoutBlockType());
		buffer.pos(transform, xMax, yMin, 0).color(red, green, blue, alpha).tex(uMax, vMin).overlay(packedOverlay).lightmap(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.pos(transform, xMin, yMin, 0).color(red, green, blue, alpha).tex(uMin, vMin).overlay(packedOverlay).lightmap(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.pos(transform, xMin, yMax, 0).color(red, green, blue, alpha).tex(uMin, vMax).overlay(packedOverlay).lightmap(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.pos(transform, xMax, yMax, 0).color(red, green, blue, alpha).tex(uMax, vMax).overlay(packedOverlay).lightmap(packedLight).normal(normal, 0, 1, 0).endVertex();
	}
}
