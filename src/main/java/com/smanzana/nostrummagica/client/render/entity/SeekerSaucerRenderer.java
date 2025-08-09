package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.SeekerSectionModel;
import com.smanzana.nostrummagica.entity.SeekerSpellSaucerEntity;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.Color;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class SeekerSaucerRenderer<T extends SeekerSpellSaucerEntity> extends EntityRenderer<T> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(NostrumMagica.MODID, "textures/entity/seeker_saucer.png");
	
	private SeekerSectionModel sectionModel;
	
	public SeekerSaucerRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
		sectionModel = new SeekerSectionModel();
	}
	
	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return TEXTURE;
	}
	
	@Override
	public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		int unused; // remove this!
		if (entityIn.tickCount > 1000) {
		final EMagicElement element = entityIn.getElement();
		final Color baseColor = new Color(element.getColor());
		final float ticks = entityIn.tickCount + partialTicks;
		final Vec3 srcPos = entityIn.getPosition(partialTicks);
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(0f, 0f, 0f);
		
		for (SeekerSpellSaucerEntity.Section section : entityIn.getRenderSections()) {
			final Vec3 sectionOffset = section.pos().subtract(srcPos);
			final float sectionAge = Math.min(1f, (ticks - section.startTicks()) / entityIn.getRenderSectionLifetime());
			final Color sectionColor = baseColor.scaleAlpha(this.getFadeAlpha(entityIn, section, sectionAge));
			BlockPos lightPos = new BlockPos(section.pos());
			final int sectionLight = LightTexture.pack(this.getBlockLightLevel(entityIn, lightPos), this.getSkyLightLevel(entityIn, lightPos));
			//return LightTexture.pack(this.getBlockLightLevel(p_114506_, blockpos), this.getSkyLightLevel(p_114506_, blockpos));
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(sectionOffset.x, sectionOffset.y, sectionOffset.z);
			matrixStackIn.mulPose(getRotation(entityIn, section));
			//matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(90f));
			renderSection(entityIn, matrixStackIn, bufferIn, sectionColor, sectionLight);
			matrixStackIn.popPose();
		}
		
		matrixStackIn.popPose();
		}
	}
	
	protected float getFadeAlpha(T entity, SeekerSpellSaucerEntity.Section section, float sectionAgeProg) {
		return 1-sectionAgeProg; // start full and fade out
	}
	
	protected Quaternion getRotation(T entity, SeekerSpellSaucerEntity.Section section) {
		return section.rotation();
	}
	
	protected void renderSection(T entity, PoseStack matrixStackIn, MultiBufferSource bufferIn, Color color, int packedLightIn) {
		final VertexConsumer buffer = bufferIn.getBuffer(sectionModel.renderType(getTextureLocation(entity)));
		sectionModel.renderToBuffer(matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, color.red, color.green, color.blue, color.alpha);
	}
	
}
