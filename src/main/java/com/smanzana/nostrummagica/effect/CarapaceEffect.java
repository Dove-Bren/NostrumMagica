package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class CarapaceEffect extends MobEffect {

	public static final String ID = "carapace";
	
	public CarapaceEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFFA59756); 
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No tick effects
	}
	
	static private boolean recurseMarker = false;
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onEntityAttack(LivingHurtEvent event) {
		if (!recurseMarker) {
			recurseMarker = true;
			if (event.getAmount() > 0f && !event.isCanceled() &&  event.getSource() instanceof EntityDamageSource entSource && !entSource.isThorns() && entSource.getEntity() instanceof LivingEntity livingSource) {
				LivingEntity ent = event.getEntityLiving();
				MobEffectInstance effect = ent.getEffect(NostrumEffects.carapace);
				
				if (effect != null && effect.getDuration() > 0) {
					final float reduc = Math.min(.5f * (effect.getAmplifier() + 1), event.getAmount());
					event.setAmount(Math.max(0, event.getAmount() - reduc));
					NostrumMagicaSounds.HOOKSHOT_TICK.play(ent);
					
					livingSource.hurt(DamageSource.thorns(ent), reduc);
				}
			}
			recurseMarker = false;
		}
	}
}
