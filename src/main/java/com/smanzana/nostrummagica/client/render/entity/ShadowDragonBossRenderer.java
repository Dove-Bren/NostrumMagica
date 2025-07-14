package com.smanzana.nostrummagica.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.NostrumModelLayers;
import com.smanzana.nostrummagica.client.model.ShadowDragonBossModel;
import com.smanzana.nostrummagica.client.render.layer.ShadowDragonArmorLayer;
import com.smanzana.nostrummagica.client.render.layer.ShadowDragonEyesLayer;
import com.smanzana.nostrummagica.entity.boss.shadowdragon.ShadowDragonEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

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
	}
}
