package com.smanzana.nostrummagica.integration.curios.items;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.integration.curios.NostrumCurioCapability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;

public interface INostrumCurio {
	
	public static ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new ICapabilityProvider() {
			@Override
			public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
				return CuriosCapability.ITEM.orEmpty(cap, LazyOptional.of(() -> new NostrumCurioCapability(stack)));
			}
		};
	}
	
	public void onWornTick(ItemStack stack, SlotContext slot);
	public void onEquipped(ItemStack stack, SlotContext entity);
	public void onUnequipped(ItemStack stack, SlotContext entity);

	public boolean canEquip(ItemStack stack, SlotContext entity);
	
	public Multimap<Attribute,AttributeModifier> getEquippedAttributeModifiers(ItemStack stack);
}
