package com.smanzana.nostrummagica.listener;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.MagicDamageSource;
import com.smanzana.nostrummagica.spell.SpellDamage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Does all the listening and tweaking for special effects
 * like shields, etc that potions can't really handle alone
 * @author Skyler
 *
 */
public class MagicEffectProxy {
	
	public static class EffectData {
		private static final String NBT_ELEMENT = "element";
		private static final String NBT_AMT = "amt";
		private static final String NBT_COUNT = "count";
		
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
		
		public CompoundNBT toNBT() {
			CompoundNBT tag = new CompoundNBT();
			
			if (element != null) {
				tag.putString(NBT_ELEMENT, element.name().toLowerCase());
			}
			
			if (count != 0) {
				tag.putInt(NBT_COUNT, count);
			}
			
			if (amt != 0.0) {
				tag.putDouble(NBT_AMT, amt);
			}
			
			return tag;
		}
		
		public static EffectData fromNBT(CompoundNBT nbt) {
			EffectData data = new EffectData();
			
			if (nbt.contains(NBT_ELEMENT)) {
				String key = nbt.getString(NBT_ELEMENT);
				try {
					EMagicElement elem = EMagicElement.valueOf(key.toUpperCase());
					data.element(elem);
				} catch (Exception e) {
					;
				}
			}
			
			if (nbt.contains(NBT_COUNT)) {
				data.count(nbt.getInt(NBT_COUNT));
			}
			
			if (nbt.contains(NBT_AMT)) {
				data.amt(nbt.getDouble(NBT_AMT));
			}
			
			return data;
		}

		public EMagicElement getElement() {
			return element;
		}

		public double getAmt() {
			return amt;
		}

		public int getCount() {
			return count;
		}
	}
	
	public static enum SpecialEffect {
		SHIELD_PHYSICAL(true),
		SHIELD_MAGIC(true),
		MAGIC_BUFF(true),
		ROOTED(true), // Just visual. Actual effects are in potion
		CONTINGENCY_DAMAGE(true), // Just visual. Actual effects are in trigger instance
		CONTINGENCY_HEALTH(true), // Just visual. Actual effects are in trigger instance
		CONTINGENCY_MANA(true), // Just visual. Actual effects are in trigger instance
		CONTINGENCY_FOOD(true), // Just visual. Actual effects are in trigger instance
		;
		
		public final boolean isPublic; // Whether other entities should know about this effect data for the entity
		
		private SpecialEffect() {
			this(false);
		}
		
		private SpecialEffect(boolean isPublic) {
			this.isPublic = isPublic;
		}
	}
	
	private Map<UUID, Map<SpecialEffect, EffectData>> effects;

	public MagicEffectProxy() {
		effects = new HashMap<>();
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private void apply(SpecialEffect effect, EffectData value, LivingEntity entity) {
		UUID id = entity.getUniqueID();
		
		if (!effects.containsKey(id)) {
			effects.put(id, new EnumMap<SpecialEffect, EffectData>(SpecialEffect.class));
		}
		
		effects.get(id).put(effect, value);
		
		notify(entity, effect, value);
	}
	
	public void applyPhysicalShield(LivingEntity entity, double value) {
		apply(SpecialEffect.SHIELD_PHYSICAL, new EffectData().amt(value), entity);
	}
	
	public void applyMagicalShield(LivingEntity entity, double value) {
		apply(SpecialEffect.SHIELD_MAGIC, new EffectData().amt(value), entity);
	}
	
	public void applyMagicBuff(LivingEntity entity, EMagicElement element, double value, int count) {
		apply(SpecialEffect.MAGIC_BUFF, new EffectData().element(element).amt(value).count(count), entity);
	}
	
	public void applyRootedEffect(LivingEntity entity) {
		apply(SpecialEffect.ROOTED, new EffectData().count(1), entity);
	}
	
	public void applyOnHitEffect(LivingEntity entity, double startTicks, int duration) {
		apply(SpecialEffect.CONTINGENCY_DAMAGE, new EffectData().amt(0).count(duration), entity);
	}
	
	public void applyOnHealthEffect(LivingEntity entity, double startTicks, int duration) {
		apply(SpecialEffect.CONTINGENCY_HEALTH, new EffectData().amt(0).count(duration), entity);
	}
	
	public void applyOnManaEffect(LivingEntity entity, double startTicks, int duration) {
		apply(SpecialEffect.CONTINGENCY_MANA, new EffectData().amt(0).count(duration), entity);
	}
	
	public void applyOnFoodEffect(LivingEntity entity, double startTicks, int duration) {
		apply(SpecialEffect.CONTINGENCY_FOOD, new EffectData().amt(0).count(duration), entity);
	}
	
	public void remove(SpecialEffect effect, LivingEntity entity) {
		UUID id = entity.getUniqueID();
		Map<SpecialEffect, EffectData> record = effects.get(id);
		if (record == null)
			return;
		
		record.remove(effect);
		
		// Send a little bit of an update to the entity (if it's a player) to update UI
		notify(entity, effect, null);
	}
	
	public void removeAll(LivingEntity entity) {
		for (SpecialEffect eff : SpecialEffect.values()) {
			remove(eff, entity);
		}
//		effects.remove(entity.getUniqueID());
		
	}
	
	protected float applyMagicShields(LivingEntity hurt, DamageSource source, float inAmt) {
		if (source.isDamageAbsolute())
			return inAmt;
		
		UUID id = hurt.getUniqueID();
		if (!effects.containsKey(id))
			return inAmt;
		
		SpecialEffect effect = SpecialEffect.SHIELD_PHYSICAL;
		if (source instanceof MagicDamageSource
				&& ((MagicDamageSource) source).getElement() != EMagicElement.PHYSICAL)
			effect = SpecialEffect.SHIELD_MAGIC;
		
		Map<SpecialEffect, EffectData> record = effects.get(id);
		EffectData left = record.get(effect);
		if (left != null) {
			left.amt -= inAmt;
			if (left.amt < 0) {
				inAmt = (float) -left.amt; // How much we couldn't shield
			} else {
				inAmt = 0f; // We shielded all so set to 0
			}
			
			if (left.amt <= 0) {
				removeEffect(hurt, effect);
				record.remove(effect);
				NostrumMagicaSounds.SHIELD_BREAK.play(hurt);
			} else {
				record.put(effect, left);
				NostrumMagicaSounds.SHIELD_ABSORB.play(hurt);
			}
			
			// Send a little bit of an update to the entity (if it's a player) to update UI
			notify(hurt, effect, left.amt <= 0 ? null : left);
		}
		return inAmt;
	}
	
	@SubscribeEvent
	public void onAttack(LivingHurtEvent event) {
		if (event.getEntity().world.isRemote) {
			return;
		}
		
		if (effects.isEmpty())
			return;
		
		float amt = applyMagicShields(event.getEntityLiving(), event.getSource(), event.getAmount());
		if (amt != event.getAmount()) {
			event.setAmount(amt);
			if (amt <= 0.0) {
				event.setCanceled(true);
			}
		}
	}
	
	protected void applyMagicDamageBuffs(LivingEntity target, DamageSource source, float amt) {
		if (effects.isEmpty()) {
			return;
		}
		
		if (source instanceof MagicDamageSource) {
			return; // Can't recurse!
		}
		
		if (!(source instanceof EntityDamageSource)) {
			return;
		}
		
		if (((EntityDamageSource) source).getIsThornsDamage()) {
			return;
		}
		
		if (amt <= 1f) {
			return;
		}
		
		Entity sourceEnt = source.getTrueSource();
		if (source != null && sourceEnt instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) sourceEnt;
			UUID id = living.getUniqueID();
			if (!effects.containsKey(id)) {
				return;
			}
			
			EffectData data = effects.get(id).get(SpecialEffect.MAGIC_BUFF);
			if (data != null) {
				SpellDamage.DamageEntity(target, data.element, (float) data.amt, living);
				target.setInvulnerable(false);
				target.hurtResistantTime = 0;
				
				NostrumMagicaSounds.MELT_METAL.play(target);
				
				// Reduce count of charges and maybe remove
				data.count--;
				if (data.count <= 0) {
					remove(SpecialEffect.MAGIC_BUFF, living);
					living.removePotionEffect(NostrumEffects.magicBuff);
				}
				
				notify(living, SpecialEffect.MAGIC_BUFF, data.count == 0 ? null : data);
			}
		}
	}
	
	@SubscribeEvent
	public void onAttackStart(LivingAttackEvent e) {
		if (e.getEntity().world.isRemote) {
			return;
		}
		
		if (e.isCanceled()) {
			return;
		}
		
		applyMagicDamageBuffs(e.getEntityLiving(), e.getSource(), e.getAmount());
	}
	
	@SubscribeEvent
	public void onDeath(LivingDeathEvent e) {
		this.removeAll(e.getEntityLiving());
	}
	
	private void removeEffect(LivingEntity entity, SpecialEffect effect) {
		Effect potion = null;
		switch (effect) {
		case SHIELD_PHYSICAL:
			potion = NostrumEffects.physicalShield;
			break;
		case SHIELD_MAGIC:
			potion = NostrumEffects.magicShield;
			break;
		case ROOTED:
			potion = NostrumEffects.rooted;
			break;
		default:
			;
		}
		
		if (potion != null) {
			EffectInstance eff = entity.getActivePotionEffect(potion);
			if (eff != null && eff.getDuration() > 1) {
				entity.removePotionEffect(potion);
			}
		}
	}
	
	public EffectData getData(LivingEntity entity, SpecialEffect effectType) {
		UUID id = entity.getUniqueID();
		Map<SpecialEffect, EffectData> map = effects.get(id);
		EffectData data = null;
		
		if (map != null) {
			data = map.get(effectType);
		}
		
		return data;
	}
	
	public void setOverride(UUID id, SpecialEffect effect, EffectData override) {
		if (NostrumMagica.instance.proxy.isServer()) {
			NostrumMagica.logger.fatal("Got an effect override on the server!");
		} else {
			Map<SpecialEffect, EffectData> map = effects.get(id);
			if (map == null) {
				map = new EnumMap<>(SpecialEffect.class);
			}
			
			// HACK OH WELL
			// contingencies use packet receive time as 'start time' for an approximation
			if (effect == SpecialEffect.CONTINGENCY_DAMAGE
					|| effect == SpecialEffect.CONTINGENCY_HEALTH
					|| effect == SpecialEffect.CONTINGENCY_MANA
					|| effect == SpecialEffect.CONTINGENCY_FOOD) {
				if (override != null) {
					override.amt = NostrumMagica.instance.proxy.getPlayer().ticksExisted;
				}
			}
			
			map.put(effect, override);
			effects.put(id, map);
		}
	}
	
	public void clearAll() {
		effects.clear();
	}
	
	protected void notify(LivingEntity base, SpecialEffect type, EffectData value) {
		if (base == null
				|| !type.isPublic) {
			return;
		}
		
		if (base.world.isRemote) {
			return;
		}
		
		for (ServerPlayerEntity player : ((ServerWorld) base.world).getPlayers()) {
			if (!(player instanceof ServerPlayerEntity)) {
				continue;
			}
			
			NostrumMagica.instance.proxy.updateEntityEffect((ServerPlayerEntity) player, base, type, value);
		}
	}
}
