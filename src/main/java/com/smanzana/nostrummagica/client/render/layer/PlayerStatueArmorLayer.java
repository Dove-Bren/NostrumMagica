package com.smanzana.nostrummagica.client.render.layer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.model.NostrumModelLayers;
import com.smanzana.nostrummagica.client.model.PlayerStatueModel;
import com.smanzana.nostrummagica.entity.boss.playerstatue.PlayerStatueEntity;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class PlayerStatueArmorLayer extends EnergySwirlLayer<PlayerStatueEntity, PlayerStatueModel> {

	public static final ResourceLocation TEXTURE_ARMOR = new ResourceLocation(NostrumMagica.MODID, "textures/entity/player_statue_armor.png");
	private final PlayerStatueModel model;
	
	public PlayerStatueArmorLayer(RenderLayerParent<PlayerStatueEntity, PlayerStatueModel> parent, EntityModelSet models) {
		super(parent);
		this.model = new PlayerStatueModel(models.bakeLayer(NostrumModelLayers.PlayerStatueArmor));
	}
	
	@Override
	protected float xOffset(float p_117702_) {
		return Mth.cos(p_117702_ * 0.02F) * 3.0F;
	}

	protected ResourceLocation getTextureLocation() {
		return TEXTURE_ARMOR;
	}

	protected PlayerStatueModel model() {
		return this.model;
	}
}
