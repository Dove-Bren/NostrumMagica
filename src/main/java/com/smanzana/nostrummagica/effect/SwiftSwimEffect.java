package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class SwiftSwimEffect extends MobEffect {

	public static final String ID = "swift_swim";
	private static final String MOD_UUID = "f104046d-2538-429b-bd19-79a228cc9492";
	
	public SwiftSwimEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFFB8F3C6);
		this.addAttributeModifier(ForgeMod.SWIM_SPEED.get(), MOD_UUID, .4D, AttributeModifier.Operation.MULTIPLY_BASE);
	}
}
