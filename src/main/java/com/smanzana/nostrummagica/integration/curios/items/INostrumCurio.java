package com.smanzana.nostrummagica.integration.curios.items;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.integration.curios.NostrumCurioCapability;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosCapability;

public interface INostrumCurio {
	
	public static ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new ICapabilityProvider() {
			@Override
			public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
				return CuriosCapability.ITEM.orEmpty(cap, LazyOptional.of(() -> new NostrumCurioCapability(stack)));
			}
		};
	}
	
	public void onWornTick(ItemStack stack, LivingEntity entity);
	public void onEquipped(ItemStack stack, LivingEntity entity);
	public void onUnequipped(ItemStack stack, LivingEntity entity);

	public boolean canEquip(ItemStack stack, LivingEntity entity);
	
	public Multimap<Attribute,AttributeModifier> getEquippedAttributeModifiers(ItemStack stack);

	@OnlyIn(Dist.CLIENT)
	public boolean hasRender(ItemStack stack, LivingEntity living);

	@OnlyIn(Dist.CLIENT)
	public void doRender(ItemStack stack, PoseStack matrixStackIn, int index, MultiBufferSource bufferIn, int packedLightIn, LivingEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch);
	
}
