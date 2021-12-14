package com.smanzana.nostrummagica.client.render.entity;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.entity.ModelDragonRed.EDragonArmorPart;
import com.smanzana.nostrummagica.client.render.entity.ModelDragonRed.EDragonOverlayMaterial;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRedBase;
import com.smanzana.nostrummagica.items.DragonArmor;
import com.smanzana.nostrummagica.items.DragonArmor.DragonEquipmentSlot;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderDragonRed extends RenderLiving<EntityDragonRedBase> {

	protected final ModelDragonRed dragonModel;
	
	public RenderDragonRed(RenderManager renderManagerIn, float shadowSizeIn) {
		this(renderManagerIn, new ModelDragonRed(), shadowSizeIn);
	}
	
	protected RenderDragonRed(RenderManager renderManagerIn, ModelDragonRed modelBase, float shadowSizeIn) {
		super(renderManagerIn, modelBase, shadowSizeIn);
		this.dragonModel = modelBase;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityDragonRedBase entity) {
		// TODO fixme?
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/koid.png"
				);
	}
	
	@Override
	public void doRender(EntityDragonRedBase dragon, double x, double y, double z, float entityYaw, float partialTicks) {
		// Set up armor visiblity
		@Nonnull final ItemStack chestArmor = dragon.getDragonEquipment(DragonEquipmentSlot.BODY);
		@Nonnull final ItemStack headArmor = dragon.getDragonEquipment(DragonEquipmentSlot.HELM);
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
		
		super.doRender(dragon, x, y, z, entityYaw, partialTicks);
	}

}
