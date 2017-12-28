package com.smanzana.nostrummagica.listeners;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.smanzana.nostrummagica.potions.MagicShieldPotion;
import com.smanzana.nostrummagica.potions.PhysicalShieldPotion;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellAction;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Does all the listening and tweaking for special effects
 * like shields, etc that potions can't really handle alone
 * @author Skyler
 *
 */
public class MagicEffectProxy {
	
	public static enum SpecialEffect {
		SHIELD_PHYSICAL,
		SHIELD_MAGIC,
	}
	
	private Map<UUID, Map<SpecialEffect, Double>> effects;

	public MagicEffectProxy() {
		effects = new HashMap<>();
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void apply(SpecialEffect effect, double value, EntityLivingBase entity) {
		UUID id = entity.getPersistentID();
		
		if (!effects.containsKey(id)) {
			effects.put(id, new EnumMap<SpecialEffect, Double>(SpecialEffect.class));
		}
		
		effects.get(id).put(effect, value);
	}
	
	public void remove(SpecialEffect effect, EntityLivingBase entity) {
		UUID id = entity.getPersistentID();
		Map<SpecialEffect, Double> record = effects.get(id);
		if (record == null)
			return;
		
		record.remove(effect);
	}
	
	public void removeAll(EntityLivingBase entity) {
		effects.remove(entity.getPersistentID());
	}
	
	@SubscribeEvent
	public void onAttack(LivingHurtEvent event) {
		if (effects.isEmpty())
			return;
		
		if (event.getSource().isUnblockable())
			return;
		
		UUID id = event.getEntityLiving().getPersistentID();
		if (!effects.containsKey(id))
			return;
		
		SpecialEffect effect = SpecialEffect.SHIELD_PHYSICAL;
		if (event.getSource() instanceof SpellAction.MagicDamageSource
				&& ((SpellAction.MagicDamageSource) event.getSource()).getElement() != EMagicElement.PHYSICAL)
			effect = SpecialEffect.SHIELD_MAGIC;
		
		Map<SpecialEffect, Double> record = effects.get(id);
		Double left = record.get(effect);
		if (left != null) {
			left -= event.getAmount();
			if (left < 0) {
				event.setAmount((float) -left);
			} else {
				event.setCanceled(true);
			}
			
			if (left <= 0) {
				removeEffect(event.getEntityLiving(), effect);
				record.remove(effect);
				NostrumMagicaSounds.SHIELD_BREAK.play(event.getEntityLiving());
			} else {
				record.put(effect, left);
				NostrumMagicaSounds.SHIELD_ABSORB.play(event.getEntityLiving());
			}
		}
	}
	
	@SubscribeEvent
	public void onDeath(LivingDeathEvent e) {
		this.removeAll(e.getEntityLiving());
	}
	
	private void removeEffect(EntityLivingBase entity, SpecialEffect effect) {
		Potion potion = null;
		switch (effect) {
		case SHIELD_PHYSICAL:
			potion = PhysicalShieldPotion.instance();
			break;
		case SHIELD_MAGIC:
			potion = MagicShieldPotion.instance();
			break;
		default:
			;
		}
		
		if (potion != null) {
			entity.removePotionEffect(potion);
		}
	}
	
}
