package com.smanzana.nostrummagica.effect;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class SteelSkinEffect extends MobEffect {

	public static final String ID = "steel_skin";
	
	public SteelSkinEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFF394653); 
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // No tick effects
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onEntityAttack(LivingHurtEvent event) {
		if (event.getAmount() > 0f && !event.isCanceled()) {
			LivingEntity ent = event.getEntityLiving();
			MobEffectInstance effect = ent.getEffect(NostrumEffects.steelSkin);
			
			if (effect != null && effect.getDuration() > 0) {
				final int reduc = 2 * (effect.getAmplifier() + 1);
				event.setAmount(Math.max(0, event.getAmount() - reduc));
				NostrumMagicaSounds.HOOKSHOT_TICK.play(ent);
			}
		}
	}
}
