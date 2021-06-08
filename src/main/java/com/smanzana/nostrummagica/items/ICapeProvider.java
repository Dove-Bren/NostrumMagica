package com.smanzana.nostrummagica.items;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ICapeProvider {

	@SideOnly(Side.CLIENT)
	public boolean shouldRenderCape(EntityLivingBase entity, ItemStack stack);
	
	@SideOnly(Side.CLIENT)
	public ResourceLocation getCapeModel(EntityLivingBase entity, ItemStack stack);
	
	@SideOnly(Side.CLIENT)
	public @Nullable ResourceLocation getCapeTexture(EntityLivingBase entity, ItemStack stack);
	
	@SideOnly(Side.CLIENT)
	public void preRender(Entity entity, int model, VertexBuffer buffer, double x, double y, double z,
					float entityYaw, float partialTicks);
	
	@SideOnly(Side.CLIENT)
	public boolean shouldPreventOtherRenders(EntityLivingBase entity, ItemStack stack);
	
}
