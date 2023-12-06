package com.smanzana.nostrummagica.effects;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RendEffect extends Effect {

	public static final String ID = "rend";
	private static final String MOD_UUID = "251a6920-7345-4ec6-a11e-972c4035adc1";
	
	public RendEffect() {
		super(EffectType.HARMFUL, 0xFFC7B5BE);
		this.addAttributesModifier(SharedMonsterAttributes.ARMOR, MOD_UUID, -2D, AttributeModifier.Operation.ADDITION);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, int x, int y, float z) {
		;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, int x, int y, float z, float alpha) {
		;
	}
}
