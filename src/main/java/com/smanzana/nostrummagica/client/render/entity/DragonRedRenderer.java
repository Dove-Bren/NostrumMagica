package com.smanzana.nostrummagica.client.render.entity;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.client.model.DragonRedModel;
import com.smanzana.nostrummagica.client.model.DragonRedModel.EDragonArmorPart;
import com.smanzana.nostrummagica.client.model.DragonRedModel.EDragonOverlayMaterial;
import com.smanzana.nostrummagica.entity.dragon.RedDragonBaseEntity;
import com.smanzana.nostrummagica.item.armor.DragonArmor;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonEquipmentSlot;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DragonRedRenderer<T extends RedDragonBaseEntity> extends MobRenderer<T, DragonRedModel<T>> {

	protected final DragonRedModel<T> dragonModel;
	
	public DragonRedRenderer(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		this(renderManagerIn, new DragonRedModel<>(), shadowSizeIn);
	}
	
	protected DragonRedRenderer(EntityRendererProvider.Context renderManagerIn, DragonRedModel<T> modelBase, float shadowSizeIn) {
		super(renderManagerIn, modelBase, shadowSizeIn);
		this.dragonModel = modelBase;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
	
	@Override
	protected void scale(T entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
		super.scale(entitylivingbaseIn, matrixStackIn, partialTickTime);
	}
	
	@Override
	public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		// Set up armor visiblity
		@Nonnull final ItemStack chestArmor = entityIn.getDragonEquipment(DragonEquipmentSlot.BODY);
		@Nonnull final ItemStack headArmor = entityIn.getDragonEquipment(DragonEquipmentSlot.HELM);
		final EDragonOverlayMaterial chestMaterial;
		final EDragonOverlayMaterial headMaterial;
		if (chestArmor.isEmpty() || !(chestArmor.getItem() instanceof DragonArmor)) {
			chestMaterial = EDragonOverlayMaterial.NONE;
		} else {
			DragonArmor armor = (DragonArmor) chestArmor.getItem();
			switch (armor.getMaterial()) {
			case DIAMOND:
				chestMaterial = EDragonOverlayMaterial.DIAMOND;
				break;
			case GOLD:
				chestMaterial = EDragonOverlayMaterial.GOLD;
				break;
			case IRON:
				chestMaterial = EDragonOverlayMaterial.SCALES;
				break;
			default:
				chestMaterial = EDragonOverlayMaterial.NONE;
				break;
			}
		}
		this.dragonModel.setOverlayMaterial(EDragonArmorPart.BODY, chestMaterial);
		
		if (headArmor.isEmpty() || !(headArmor.getItem() instanceof DragonArmor)) {
			headMaterial = EDragonOverlayMaterial.NONE;
		} else {
			DragonArmor armor = (DragonArmor) headArmor.getItem();
			switch (armor.getMaterial()) {
			case DIAMOND:
				headMaterial = EDragonOverlayMaterial.DIAMOND;
				break;
			case GOLD:
				headMaterial = EDragonOverlayMaterial.GOLD;
				break;
			case IRON:
				headMaterial = EDragonOverlayMaterial.SCALES;
				break;
			default:
				headMaterial = EDragonOverlayMaterial.NONE;
				break;
			}
		}
		this.dragonModel.setOverlayMaterial(EDragonArmorPart.HEAD, headMaterial);
		
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

}
