package com.smanzana.nostrummagica.client.render.layer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.NostrumModelLayers;
import com.smanzana.nostrummagica.client.model.PrimalMageModel;
import com.smanzana.nostrummagica.entity.boss.primalmage.PrimalMageEntity;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class PrimalMageArmorLayer extends EnergySwirlLayer<PrimalMageEntity, IllagerModel<PrimalMageEntity>> { // because of class heirarchy, can't say primal mage model here

	public static final ResourceLocation TEXTURE_ARMOR = new ResourceLocation(NostrumMagica.MODID, "textures/entity/primal_mage_armor.png");
	private final PrimalMageModel model;
	
	public PrimalMageArmorLayer(RenderLayerParent<PrimalMageEntity, IllagerModel<PrimalMageEntity>> parent, EntityModelSet models) {
		super(parent);
		this.model = new PrimalMageModel(models.bakeLayer(NostrumModelLayers.PrimalMageArmor));
	}
	
	@Override
	protected float xOffset(float p_117702_) {
		return Mth.cos(p_117702_ * 0.02F) * 3.0F;
	}

	protected ResourceLocation getTextureLocation() {
		return TEXTURE_ARMOR;
	}

	protected IllagerModel<PrimalMageEntity> model() {
		return this.model;
	}
}
