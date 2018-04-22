package com.smanzana.nostrummagica.client.effects;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ClientEffectForm {

	public void draw(Minecraft mc, float partialTicks, int color);
	
}
