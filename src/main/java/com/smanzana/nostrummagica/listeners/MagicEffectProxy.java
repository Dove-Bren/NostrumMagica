package com.smanzana.nostrummagica.listeners;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.potions.MagicBuffPotion;
import com.smanzana.nostrummagica.potions.MagicShieldPotion;
import com.smanzana.nostrummagica.potions.PhysicalShieldPotion;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.spells.components.SpellAction.MagicDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
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
		
		public NBTTagCompound toNBT() {
			NBTTagCompound tag = new NBTTagCompound();
			
			if (element != null) {
				tag.setString(NBT_ELEMENT, element.name().toLowerCase());
			}
			
			if (count != 0) {
				tag.setInteger(NBT_COUNT, count);
			}
			
			if (amt != 0.0) {
				tag.setDouble(NBT_AMT, amt);
			}
			
			return tag;
		}
		
		public static EffectData fromNBT(NBTTagCompound nbt) {
			EffectData data = new EffectData();
			
			if (nbt.hasKey(NBT_ELEMENT)) {
				String key = nbt.getString(NBT_ELEMENT);
				try {
					EMagicElement elem = EMagicElement.valueOf(key.toUpperCase());
					data.element(elem);
				} catch (Exception e) {
					;
				}
			}
			
			if (nbt.hasKey(NBT_COUNT)) {
				data.count(nbt.getInteger(NBT_COUNT));
			}
			
			if (nbt.hasKey(NBT_AMT)) {
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
		SHIELD_PHYSICAL,
		SHIELD_MAGIC,
		MAGIC_BUFF,
		ROOTED, // Just visual. Actual effects are in potion
		CONTINGENCY_DAMAGE, // Just visual. Actual effects are in trigger instance
		CONTINGENCY_HEALTH, // Just visual. Actual effects are in trigger instance
		CONTINGENCY_MANA, // Just visual. Actual effects are in trigger instance
		CONTINGENCY_FOOD, // Just visual. Actual effects are in trigger instance
		TARGETED, // Can key many things off. As of writing, used for battle music
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
		
		if (entity instanceof EntityPlayerMP) {
			NostrumMagica.proxy.updatePlayerEffect((EntityPlayerMP) entity, effect, value);
		}
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
	
	public void applyRootedEffect(EntityLivingBase entity) {
		apply(SpecialEffect.ROOTED, new EffectData().count(1), entity);
	}
	
	public void applyOnHitEffect(EntityLivingBase entity, double startTicks, int duration) {
		apply(SpecialEffect.CONTINGENCY_DAMAGE, new EffectData().amt(0).count(duration), entity);
	}
	
	public void applyOnHealthEffect(EntityLivingBase entity, double startTicks, int duration) {
		apply(SpecialEffect.CONTINGENCY_HEALTH, new EffectData().amt(0).count(duration), entity);
	}
	
	public void applyOnManaEffect(EntityLivingBase entity, double startTicks, int duration) {
		apply(SpecialEffect.CONTINGENCY_MANA, new EffectData().amt(0).count(duration), entity);
	}
	
	public void applyOnFoodEffect(EntityLivingBase entity, double startTicks, int duration) {
		apply(SpecialEffect.CONTINGENCY_FOOD, new EffectData().amt(0).count(duration), entity);
	}
	
	public void setTargetted(EntityLivingBase entity) {
		final int start = entity.ticksExisted;
		final int dimension = entity.dimension;
		apply(SpecialEffect.TARGETED, new EffectData().count(start).amt(dimension), entity);
		NostrumMagica.playerListener.registerTimer((type, ent, junk) -> {
			boolean remove = false;
			
			// Find what the current data is
			EffectData data = NostrumMagica.magicEffectProxy.getData(entity, SpecialEffect.TARGETED);
			if (data != null && (int) data.getAmt() == dimension && data.getCount() == start) {
				// Most recent is still us. Check if we should cancel
				if (entity.world != null && !entity.isDead) {
					if (entity.world.getEntities(EntityLiving.class, (e) -> {
						return e != null
								&& ((EntityLiving) e).getAttackTarget() != null
								&& ((EntityLiving) e).getAttackTarget().equals(entity)
								&& entity.getDistanceSq(e) < 400;
					}).isEmpty()) {
						NostrumMagica.magicEffectProxy.remove(SpecialEffect.TARGETED, entity);
						remove = true;
					} else {
						// Check again
					}
				}
			} else {
				remove = true;
			}
			
			return remove;
		}, 20 * 5, 1);
	}
	
	public void remove(SpecialEffect effect, EntityLivingBase entity) {
		UUID id = entity.getPersistentID();
		Map<SpecialEffect, EffectData> record = effects.get(id);
		if (record == null)
			return;
		
		record.remove(effect);
		// Send a little bit of an update to the entity (if it's a player) to update UI
		if (entity instanceof EntityPlayerMP) {
			NostrumMagica.proxy.updatePlayerEffect((EntityPlayerMP) entity, effect, null);
		}
	}
	
	public void removeAll(EntityLivingBase entity) {
		for (SpecialEffect eff : SpecialEffect.values()) {
			remove(eff, entity);
		}
//		effects.remove(entity.getPersistentID());
		
	}
	
	protected float applyMagicShields(EntityLivingBase hurt, DamageSource source, float inAmt) {
		if (source.isDamageAbsolute())
			return inAmt;
		
		UUID id = hurt.getPersistentID();
		if (!effects.containsKey(id))
			return inAmt;
		
		SpecialEffect effect = SpecialEffect.SHIELD_PHYSICAL;
		if (source instanceof SpellAction.MagicDamageSource
				&& ((SpellAction.MagicDamageSource) source).getElement() != EMagicElement.PHYSICAL)
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
			if (hurt instanceof EntityPlayerMP) {
				NostrumMagica.proxy.updatePlayerEffect((EntityPlayerMP) hurt, effect, left.amt <= 0 ? null : left);
			}
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
	
	protected void applyMagicDamageBuffs(EntityLivingBase target, DamageSource source, float amt) {
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
		if (source != null && sourceEnt instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase) sourceEnt;
			UUID id = living.getPersistentID();
			if (!effects.containsKey(id)) {
				return;
			}
			
			EffectData data = effects.get(id).get(SpecialEffect.MAGIC_BUFF);
			if (data != null) {
				target.attackEntityFrom(new SpellAction.MagicDamageSource(living, data.element), 
						SpellAction.calcDamage(living, target, (float) data.amt, data.element));
				target.setEntityInvulnerable(false);
				target.hurtResistantTime = 0;
				
				NostrumMagicaSounds.MELT_METAL.play(target);
				
				// Reduce count of charges and maybe remove
				data.count--;
				if (data.count <= 0) {
					remove(SpecialEffect.MAGIC_BUFF, living);
					living.removePotionEffect(MagicBuffPotion.instance());
				}
				
				if (living instanceof EntityPlayerMP) {
					NostrumMagica.proxy.updatePlayerEffect((EntityPlayerMP) living, SpecialEffect.MAGIC_BUFF, data.count == 0 ? null : data);
				}
			}
		}
	}
	
	protected void applyTargetted(EntityLivingBase ent) {
		if (getData(ent, SpecialEffect.TARGETED) == null) {
			setTargetted(ent);
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
		
		if (e.getSource().getTrueSource() != null && e.getSource().getTrueSource() instanceof EntityLivingBase) {
			applyTargetted(e.getEntityLiving());
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
		case ROOTED:
			potion = RootedPotion.instance();
			break;
		default:
			;
		}
		
		if (potion != null) {
			PotionEffect eff = entity.getActivePotionEffect(potion);
			if (eff != null && eff.getDuration() > 1) {
				entity.removePotionEffect(potion);
			}
		}
	}
	
	public EffectData getData(EntityLivingBase entity, SpecialEffect effectType) {
		UUID id = entity.getUniqueID();
		Map<SpecialEffect, EffectData> map = effects.get(id);
		EffectData data = null;
		
		if (map != null) {
			data = map.get(effectType);
		}
		
		return data;
	}
	
	public void setOverride(SpecialEffect effect, EffectData override) {
		if (NostrumMagica.proxy.isServer()) {
			NostrumMagica.logger.fatal("Got an effect override on the server!");
		} else {
			UUID id = NostrumMagica.proxy.getPlayer().getUniqueID();
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
				override.amt = NostrumMagica.proxy.getPlayer().ticksExisted;
			}
			
			map.put(effect, override);
			effects.put(id, map);
		}
	}
	
	public void clearAll() {
		effects.clear();
	}
}
