package com.smanzana.nostrummagica.items;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICapeProvider {

	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderCape(LivingEntity entity, ItemStack stack);
	
	@OnlyIn(Dist.CLIENT)
	public ModelResourceLocation[] getCapeModels(LivingEntity entity, ItemStack stack);
	
	@OnlyIn(Dist.CLIENT)
	public @Nullable ResourceLocation[] getCapeTextures(LivingEntity entity, ItemStack stack);
	
	@OnlyIn(Dist.CLIENT)
	public int getColor(LivingEntity entity, ItemStack stack, int model);
	
	@OnlyIn(Dist.CLIENT)
	public void preRender(Entity entity, int model, ItemStack stack, float entityYaw, float partialTicks);
	
	@OnlyIn(Dist.CLIENT)
	public boolean shouldPreventOtherRenders(LivingEntity entity, ItemStack stack);
	
}
