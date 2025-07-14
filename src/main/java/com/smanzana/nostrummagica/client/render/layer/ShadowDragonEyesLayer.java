package com.smanzana.nostrummagica.client.render.layer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.ShadowDragonBossModel;
import com.smanzana.nostrummagica.entity.boss.shadowdragon.ShadowDragonEntity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class ShadowDragonEyesLayer extends EyesLayer<ShadowDragonEntity, ShadowDragonBossModel> {
	
	private static final RenderType PHANTOM_EYES = RenderType.eyes(new ResourceLocation(NostrumMagica.MODID, "textures/entity/shadow_dragon_eyes.png"));

	public ShadowDragonEyesLayer(RenderLayerParent<ShadowDragonEntity, ShadowDragonBossModel> parent) {
		super(parent);
	}

	@Override
	public RenderType renderType() {
		return PHANTOM_EYES;
	}

}
