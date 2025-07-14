package com.smanzana.nostrummagica.client.render.layer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ShadowDragonBossModel;
import com.smanzana.nostrummagica.entity.boss.shadowdragon.ShadowDragonEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ShadowDragonArmorLayer extends EnergySwirlLayer<ShadowDragonEntity, ShadowDragonBossModel> {

	public static final ResourceLocation TEXTURE_ARMOR = new ResourceLocation(NostrumMagica.MODID, "textures/entity/player_statue_armor.png");
	protected final ShadowDragonBossModel model;
	
	public ShadowDragonArmorLayer(RenderLayerParent<ShadowDragonEntity, ShadowDragonBossModel> parent, ShadowDragonBossModel model) {
		super(parent);
		this.model = model;
	}
	
	@Override
	protected float xOffset(float p_117702_) {
		return Mth.cos(p_117702_ * 0.02F) * 3.0F;
	}

	protected ResourceLocation getTextureLocation() {
		return TEXTURE_ARMOR;
	}

	@Override
	protected EntityModel<ShadowDragonEntity> model() {
		return model;
	}
}
