package com.smanzana.nostrummagica.item.armor;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICapeProvider {

	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderCape(LivingEntity entity, ItemStack stack);
	
	@OnlyIn(Dist.CLIENT)
	public ResourceLocation[] getCapeModels(LivingEntity entity, ItemStack stack);
	
	@OnlyIn(Dist.CLIENT)
	public @Nullable RenderType[] getCapeRenderTypes(LivingEntity entity, ItemStack stack);
	
	@OnlyIn(Dist.CLIENT)
	public int getColor(LivingEntity entity, ItemStack stack, int model);
	
	@OnlyIn(Dist.CLIENT)
	public void preRender(Entity entity, int model, ItemStack stack, PoseStack matrixStack, float entityYaw, float partialTicks);
	
	@OnlyIn(Dist.CLIENT)
	public boolean shouldPreventOtherRenders(LivingEntity entity, ItemStack stack);
	
}
