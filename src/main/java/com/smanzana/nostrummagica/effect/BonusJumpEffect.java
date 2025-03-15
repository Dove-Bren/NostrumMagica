package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class BonusJumpEffect extends Effect {

	public static final String ID = "bonus_jump";
	
	public BonusJumpEffect() {
		super(EffectType.BENEFICIAL, 0xFFD1CDD6);
		this.addAttributeModifier(NostrumAttributes.bonusJump, "d10d2bfa-1b89-47a0-ade5-5b4d1cc9c48d", 1, AttributeModifier.Operation.ADDITION);
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityFall(LivingFallEvent event) {
		if (event.isCanceled()) {
			return;
		}
		
		if (event.getDistance() > 0) {
			EffectInstance effect = event.getEntityLiving().getEffect(NostrumEffects.bonusJump);
			if (effect != null && effect.getDuration() > 0) {
				// Reduce by 1.5 blocks per level, plus an additional .25 blocks per .1 jump velocity over normal.
//				final float baseVelocity = 0.42F; // from LivingEntity
//				final float interval = .1f; // what jump boost gives, also in LivingEntity code
//				final float extra = Math.max(0, (event.getEntityLiving().jumpMovementFactor - baseVelocity) / interval) * .25f;
				// ... or rather, WANT to, but can't actually tell what the jump velocity will be until it's done. So just
				// go to 2 blocks per level for now
				final float extra = .5f;
				
				final float amt = (effect.getAmplifier()+1) * (1.5f + extra);
				event.setDistance(Math.max(0, event.getDistance() - amt));
			}
		}
		
	}
}
