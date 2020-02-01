package com.smanzana.nostrummagica.listeners;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.smanzana.nostrummagica.potions.MagicBuffPotion;
import com.smanzana.nostrummagica.potions.MagicShieldPotion;
import com.smanzana.nostrummagica.potions.PhysicalShieldPotion;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.spells.components.SpellAction.MagicDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
	
	private static class EffectData {
		private EMagicElement element;
		private double amt;
		private int count;
		
		public EffectData element(EMagicElement elem) {
			this.element = elem;
			return this;
		}
		
		public EffectData amt(double amt) {
			this.amt = amt;
			return this;
		}
		
		public EffectData count(int count) {
			this.count = count;
			return this;
		}
	}
	
	public static enum SpecialEffect {
		SHIELD_PHYSICAL,
		SHIELD_MAGIC,
		MAGIC_BUFF,
	}
	
	private Map<UUID, Map<SpecialEffect, EffectData>> effects;

	public MagicEffectProxy() {
		effects = new HashMap<>();
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private void apply(SpecialEffect effect, EffectData value, EntityLivingBase entity) {
		UUID id = entity.getPersistentID();
		
		if (!effects.containsKey(id)) {
			effects.put(id, new EnumMap<SpecialEffect, EffectData>(SpecialEffect.class));
		}
		
		effects.get(id).put(effect, value);
	}
	
	public void applyPhysicalShield(EntityLivingBase entity, double value) {
		apply(SpecialEffect.SHIELD_PHYSICAL, new EffectData().amt(value), entity);
	}
	
	public void applyMagicalShield(EntityLivingBase entity, double value) {
		apply(SpecialEffect.SHIELD_MAGIC, new EffectData().amt(value), entity);
	}
	
	public void applyMagicBuff(EntityLivingBase entity, EMagicElement element, double value, int count) {
		apply(SpecialEffect.MAGIC_BUFF, new EffectData().element(element).amt(value).count(count), entity);
	}
	
	public void remove(SpecialEffect effect, EntityLivingBase entity) {
		UUID id = entity.getPersistentID();
		Map<SpecialEffect, EffectData> record = effects.get(id);
		if (record == null)
			return;
		
		record.remove(effect);
	}
	
	public void removeAll(EntityLivingBase entity) {
		effects.remove(entity.getPersistentID());
	}
	
	@SubscribeEvent
	public void onAttack(LivingHurtEvent event) {
		if (event.getEntity().worldObj.isRemote) {
			return;
		}
		
		if (effects.isEmpty())
			return;
		
		if (event.getSource().isDamageAbsolute())
			return;
		
		UUID id = event.getEntityLiving().getPersistentID();
		if (!effects.containsKey(id))
			return;
		
		SpecialEffect effect = SpecialEffect.SHIELD_PHYSICAL;
		if (event.getSource() instanceof SpellAction.MagicDamageSource
				&& ((SpellAction.MagicDamageSource) event.getSource()).getElement() != EMagicElement.PHYSICAL)
			effect = SpecialEffect.SHIELD_MAGIC;
		
		Map<SpecialEffect, EffectData> record = effects.get(id);
		EffectData left = record.get(effect);
		if (left != null) {
			left.amt -= event.getAmount();
			if (left.amt < 0) {
				event.setAmount((float) -left.amt);
			} else {
				event.setCanceled(true);
			}
			
			if (left.amt <= 0) {
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
	public void onAttackStart(LivingAttackEvent e) {
		if (effects.isEmpty()) {
			return;
		}
		
		if (e.getEntity().worldObj.isRemote) {
			return;
		}
		
		if (e.getSource() instanceof MagicDamageSource) {
			return; // Can't recurse!
		}
		
		if (!(e.getSource() instanceof EntityDamageSource)) {
			return;
		}
		
		if (((EntityDamageSource) e.getSource()).getIsThornsDamage()) {
			return;
		}
		
		if (e.getAmount() <= 1f) {
			return;
		}
		
		Entity source = e.getSource().getSourceOfDamage();
		if (source != null && source instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase) source;
			UUID id = living.getPersistentID();
			if (!effects.containsKey(id)) {
				return;
			}
			
			EffectData data = effects.get(id).get(SpecialEffect.MAGIC_BUFF);
			if (data != null) {
				e.getEntityLiving().attackEntityFrom(new SpellAction.MagicDamageSource(living, data.element), 
						SpellAction.calcDamage(living, e.getEntityLiving(), (float) data.amt, data.element));
				e.getEntityLiving().setEntityInvulnerable(false);
				e.getEntityLiving().hurtResistantTime = 0;
				
				NostrumMagicaSounds.MELT_METAL.play(e.getEntity());
				
				// Reduce count of charges and maybe remove
				data.count--;
				if (data.count <= 0) {
					remove(SpecialEffect.MAGIC_BUFF, living);
					living.removePotionEffect(MagicBuffPotion.instance());
				}
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
