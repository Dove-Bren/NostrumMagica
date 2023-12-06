package com.smanzana.nostrummagica.effects;

import com.smanzana.nostrummagica.attributes.AttributeMagicResist;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MagicRendEffect extends Effect {

	public static final String ID = "magic_rend";
	private static final String MOD_UUID = "23a1bd05-7864-473a-bf4e-52e419849473";
	
	public MagicRendEffect() {
		super(EffectType.HARMFUL, 0xFFE36338);
		this.addAttributesModifier(AttributeMagicResist.instance(), MOD_UUID, -20D, AttributeModifier.Operation.ADDITION);
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
