package com.smanzana.nostrummagica.client.render.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.effect.NostrumEffects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class CursedFireLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
	
	@SuppressWarnings("deprecation")
	private final RenderMaterial TEX_FIRE_0 = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, NostrumMagica.Loc("block/cursed_fire_0"));
	@SuppressWarnings("deprecation")
	private final RenderMaterial TEX_FIRE_1 = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, NostrumMagica.Loc("block/cursed_fire_1"));

	
	//protected final PlayerRenderer renderPlayer;
	
	public CursedFireLayer(LivingRenderer<T, M> rendererIn) {
		super(rendererIn);
	}
	
	@Override
	public void render(MatrixStack stack, IRenderTypeBuffer typeBuffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (shouldRender(entity)) {
			final Minecraft mc = Minecraft.getInstance();
			final ActiveRenderInfo activeInfo = mc.gameRenderer.getActiveRenderInfo();
			final IVertexBuilder buffer = typeBuffer.getBuffer(Atlases.getCutoutBlockType());
			
			stack.push();
			final float fireWidth = entity.getWidth() * 1.4f; // copied from EntityRendererManager#renderFire
			stack.scale(fireWidth, fireWidth, fireWidth);
			//stack.rotate(Vector3f.YP.rotationDegrees(activeInfo.getYaw()));
			stack.translate(0, 0, -0.32f); // probably needs adjusting
			{
				stack.push();
				stack.translate(.5, .5, 0);
				renderFire(stack, buffer, TEX_FIRE_0.getSprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
				stack.pop();
				stack.push();
				stack.translate(-.3, .05, 0);
				renderFire(stack, buffer, TEX_FIRE_1.getSprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
				stack.pop();
				stack.push();
				stack.translate(-.4, 1.2, 0);
				renderFire(stack, buffer, TEX_FIRE_0.getSprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
				stack.pop();
				stack.push();
				stack.translate(-.1, .65, 0);
				renderFire(stack, buffer, TEX_FIRE_1.getSprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
				stack.pop();
				stack.push();
				stack.translate(.6, -.2, 0);
				renderFire(stack, buffer, TEX_FIRE_1.getSprite(), packedLight, .4f, 1f, 1f, 1f, 1f);
				stack.pop();
			}
			stack.pop();
		}
	}
	
	public boolean shouldRender(T entity) {
		int unused; // change to cursed status
		return !entity.isSpectator() && entity.getActivePotionEffect(NostrumEffects.disruption) != null;
	}
	
	protected void renderFire(MatrixStack stack, IVertexBuilder buffer, TextureAtlasSprite sprite, int packedLight, float width, float red, float green, float blue, float alpha) {
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
		buffer.pos(transform, xMax, yMin, 0).color(red, green, blue, alpha).tex(uMax, vMin).overlay(packedOverlay).lightmap(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.pos(transform, xMin, yMin, 0).color(red, green, blue, alpha).tex(uMin, vMin).overlay(packedOverlay).lightmap(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.pos(transform, xMin, yMax, 0).color(red, green, blue, alpha).tex(uMin, vMax).overlay(packedOverlay).lightmap(packedLight).normal(normal, 0, 1, 0).endVertex();
		buffer.pos(transform, xMax, yMax, 0).color(red, green, blue, alpha).tex(uMax, vMax).overlay(packedOverlay).lightmap(packedLight).normal(normal, 0, 1, 0).endVertex();
	}
}
