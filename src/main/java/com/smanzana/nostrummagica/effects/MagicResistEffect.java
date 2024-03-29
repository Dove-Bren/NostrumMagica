package com.smanzana.nostrummagica.effects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MagicResistEffect extends Effect {

	public static final String ID = "potions-magres";
	
	public MagicResistEffect() {
		super(EffectType.BENEFICIAL, 0xFFA5359A);
	}
	
	public boolean isReady(int duration, int amp) {
		return duration > 0; // Every tick
	}
	
	// This is an attribute now!
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, int x, int y, float z) {
		PotionIcon.MAGICRESIST.draw(gui.getMinecraft(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, int x, int y, float z, float alpha) {
		PotionIcon.MAGICRESIST.draw(Minecraft.getInstance(), x + 3, y + 3);
	}
}
