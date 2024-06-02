package com.smanzana.nostrummagica.client.render.entity;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.client.model.ModelDragonRed;
import com.smanzana.nostrummagica.client.model.ModelDragonRed.EDragonArmorPart;
import com.smanzana.nostrummagica.client.model.ModelDragonRed.EDragonOverlayMaterial;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRedBase;
import com.smanzana.nostrummagica.item.armor.DragonArmor;
import com.smanzana.nostrummagica.item.armor.DragonArmor.DragonEquipmentSlot;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderDragonRed<T extends EntityDragonRedBase> extends MobRenderer<T, ModelDragonRed<T>> {

	protected final ModelDragonRed<T> dragonModel;
	
	public RenderDragonRed(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		this(renderManagerIn, new ModelDragonRed<>(), shadowSizeIn);
	}
	
	protected RenderDragonRed(EntityRendererManager renderManagerIn, ModelDragonRed<T> modelBase, float shadowSizeIn) {
		super(renderManagerIn, modelBase, shadowSizeIn);
		this.dragonModel = modelBase;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ResourceLocation getEntityTexture(T entity) {
		return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	}
	
	@Override
	protected void preRenderCallback(T entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime) {
		super.preRenderCallback(entitylivingbaseIn, matrixStackIn, partialTickTime);
	}
	
	@Override
	public void render(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
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
