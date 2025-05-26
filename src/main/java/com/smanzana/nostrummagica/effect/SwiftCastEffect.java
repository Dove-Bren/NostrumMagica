package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.spell.SpellCastEvent;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SwiftCastEffect extends MobEffect {

	public static final String ID_INSTANT = "swift_cast";
	public static final String ID_LASTING = "lasting_swift_cast";
	
	public static final String UUID_INSTANT = "dcd56c5e-3c10-4c66-834c-e9b752a8f84c";
	public static final String UUID_LASTING = "ae29edc7-3ead-4605-9489-517580d8dd10";
	
	public final boolean expireOnCast;
	
	public SwiftCastEffect(boolean expireOnCast) {
		super(MobEffectCategory.BENEFICIAL, 0xFFFFEE61);
		this.addAttributeModifier(NostrumAttributes.castSpeed, expireOnCast ? UUID_INSTANT : UUID_LASTING, 1000.D, AttributeModifier.Operation.ADDITION);
		this.expireOnCast = expireOnCast;
		
		if (expireOnCast) {
			MinecraftForge.EVENT_BUS.register(this);
		}
	}
	
	@Override
	public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
		return 1000.D;
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return super.isDurationEffectTick(duration, amp);
	}
	
	@SubscribeEvent
	public void onSpellCast(SpellCastEvent.Post event) {
		if (!event.isChecking && event.getCastResult().succeeded && event.getCaster() != null && !event.getCaster().level.isClientSide()) {
			final LivingEntity living = event.getCaster();
			MobEffectInstance instance = living.getEffect(this);
			if (instance != null && instance.getDuration() > 0) {
				// consume a charge
				int newAmp = instance.getAmplifier() - 1;
				living.removeEffect(instance.getEffect());
				if (newAmp >= 0) {
					living.addEffect(new MobEffectInstance(instance.getEffect(), instance.getDuration(), newAmp));
				}
			}
		}
	}
}
