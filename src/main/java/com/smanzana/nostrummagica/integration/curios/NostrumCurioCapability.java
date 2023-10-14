package com.smanzana.nostrummagica.integration.curios;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurio;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.capability.ICurio;

public class NostrumCurioCapability implements ICurio {

	private final ItemStack stack;

	public NostrumCurioCapability(ItemStack stack) {
		this.stack = stack;
	}

	private NostrumCurio getItem() {
		return (NostrumCurio) stack.getItem();
	}

	@Override
	public void onCurioTick(String identifier, LivingEntity entity) {
		getItem().onWornTick(stack, entity);
	}

	@Override
	public void onEquipped(String identifier, LivingEntity entity) {
		getItem().onEquipped(stack, entity);
	}

	@Override
	public void onUnequipped(String identifier, LivingEntity entity) {
		getItem().onUnequipped(stack, entity);
	}

	@Override
	public boolean canEquip(String identifier, LivingEntity entity) {
		return getItem().canEquip(stack, entity);
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(String identifier) {
		return getItem().getEquippedAttributeModifiers(stack);
	}

	@Override
	public boolean shouldSyncToTracking(String identifier, LivingEntity entity) {
		return true;
	}

	@Override
	public void playEquipSound(LivingEntity entity) {
		NostrumMagicaSounds.BAUBLE_EQUIP.play(entity.world, entity.posX, entity.posY, entity.posZ);
		//entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, NostrumMagicaSounds.BAUBLE_EQUIP, entity.getSoundCategory(), 0.1F, 1.3F);
	}

	@Override
	public boolean canRightClickEquip() {
		return true;
	}

	@Override
	public boolean hasRender(String identifier, LivingEntity entity) {
		return getItem().hasRender(stack, entity);
	}

	@Override
	public void doRender(String identifier, LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		getItem().doRender(stack, entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
	}
	
}
