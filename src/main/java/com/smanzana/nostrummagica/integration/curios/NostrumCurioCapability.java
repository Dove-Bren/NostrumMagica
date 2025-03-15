package com.smanzana.nostrummagica.integration.curios;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurio;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class NostrumCurioCapability implements ICurio {

	private final ItemStack stack;

	public NostrumCurioCapability(ItemStack stack) {
		this.stack = stack;
	}

	private NostrumCurio getItem() {
		return (NostrumCurio) stack.getItem();
	}

	@Override
	public void curioTick(String identifier, int index, LivingEntity entity) {
		getItem().onWornTick(stack, entity);
	}

	@Override
	public void onEquip(String identifier, int index, LivingEntity entity) {
		getItem().onEquipped(stack, entity);
	}

	@Override
	public void onUnequip(String identifier, int index, LivingEntity entity) {
		getItem().onUnequipped(stack, entity);
	}

	@Override
	public boolean canEquip(String identifier, LivingEntity entity) {
		return getItem().canEquip(stack, entity);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier) {
		return getItem().getEquippedAttributeModifiers(stack);
	}

	@Override
	public boolean canSync(String identifier, int index, LivingEntity entity) {
		return true;
	}

	@Override
	public void playRightClickEquipSound(LivingEntity entity) {
		NostrumMagicaSounds.BAUBLE_EQUIP.play(entity.level, entity.getX(), entity.getY(), entity.getZ());
		//entity.world.playSound(null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), NostrumMagicaSounds.BAUBLE_EQUIP, entity.getSoundCategory(), 0.1F, 1.3F);
	}

	@Override
	public boolean canRightClickEquip() {
		return true;
	}

	@Override
	public boolean canRender(String identifier, int index, LivingEntity entity) {
		return getItem().hasRender(stack, entity);
	}

	@Override
	public void render(String identifier, int index, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int light,
			LivingEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		getItem().doRender(stack, matrixStackIn, index, bufferIn, light, entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
	}
	
}
