package com.smanzana.nostrummagica.integration.curios;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurio;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
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
	public void curioTick(SlotContext slot) {
		getItem().onWornTick(stack, slot);
	}

	@Override
	public void onEquip(SlotContext slot, ItemStack prevStack) {
		getItem().onEquipped(stack, slot);
	}

	@Override
	public void onUnequip(SlotContext slot, ItemStack prevStack) {
		getItem().onUnequipped(stack, slot);
	}

	@Override
	public boolean canEquip(SlotContext slot) {
		return getItem().canEquip(stack, slot);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier) {
		return getItem().getEquippedAttributeModifiers(stack);
	}

	@Override
	public boolean canSync(SlotContext slot) {
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
	public ItemStack getStack() {
		return stack;
	}
}
