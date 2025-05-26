package com.smanzana.nostrummagica.spell.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.NostrumMagica.NostrumTeleportEvent;
import com.smanzana.nostrummagica.block.CandleBlock;
import com.smanzana.nostrummagica.block.MagicWallBlock;
import com.smanzana.nostrummagica.block.MysticWaterBlock;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.effect.ElementalEnchantEffect;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.IEnchantableEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.TameLightning;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.entity.golem.MagicEarthGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicEnderGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicFireGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicIceGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicLightningGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicPhysicalGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicWindGolemEntity;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.item.IEnchantableItem;
import com.smanzana.nostrummagica.item.SpellScroll;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellActionSummary;
import com.smanzana.nostrummagica.spell.SpellDamage;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.SpellTeleportation;
import com.smanzana.nostrummagica.spell.component.Transmutation.TransmuteResult;
import com.smanzana.nostrummagica.spell.log.ESpellLogModifierType;
import com.smanzana.nostrummagica.spell.log.ISpellLogBuilder;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.HarvestUtil;
import com.smanzana.nostrummagica.util.HarvestUtil.ITreeWalker;
import com.smanzana.nostrummagica.util.ItemStacks;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.util.TargetLocation;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.ToolActions;

public class SpellAction {
	
	public static class SpellActionProperties {
		public final boolean isHarmful;
		public final boolean affectsEntity;
		public final boolean affectsBlock;
		
		protected SpellActionProperties(SpellAction action) {
			boolean isHarmful = false;
			boolean affectsEntity = false;
			boolean affectsBlock = false;
			
			for (SpellEffect effect : action.effects) {
				if (!isHarmful && effect.isHarmful()) {
					isHarmful = true;
				}
				
				if (!affectsEntity && effect.affectsEntities()) {
					affectsEntity = true;
				}
				
				if (!affectsBlock && effect.affectsBlocks()) {
					affectsBlock = true;
				}
			}
			
			
			this.isHarmful = isHarmful;
			this.affectsEntity = affectsEntity;
			this.affectsBlock = affectsBlock;
		}
	}
	
	public static class SpellActionResult {
		private static final SpellActionResult FAIL = new SpellActionResult();
		
		public boolean applied;
		public float damage;
		public float heals;
		public SpellLocation affectedPos;
		
		private SpellActionResult() {
			this.applied = false;
			this.damage = 0f;
			this.heals = 0f;
			affectedPos = null;
		}
	}
	
	public static interface IEffectPredicate {
		public boolean test(LivingEntity caster, LivingEntity target, float efficiency);
	}
	
	private static interface SpellEffect {
		public void apply(LivingEntity caster, LivingEntity entity, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log);
		public void apply(LivingEntity caster, SpellLocation location, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log);
		
		public default boolean isHarmful() {
			return false;
		}
		
		public boolean affectsEntities();
		public boolean affectsBlocks();
	}
	
	private static abstract class NegativeSpellEffect implements SpellEffect {
		
		@Override
		public boolean isHarmful() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
		
		protected abstract void applyEffect(LivingEntity caster, LivingEntity entity, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log);
		
		public final void apply(LivingEntity caster, LivingEntity entity, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			if (entity != null && isHarmful() && caster != entity) {
				if (PetFuncs.GetOwner(entity) != null && caster.equals(PetFuncs.GetOwner(entity))) {
					return; // we own the target entity
				}
				
				if (PetFuncs.GetOwner(caster) != null && entity.equals(PetFuncs.GetOwner(caster))) {
					return; // they own us
				}
			}
			
			applyEffect(caster, entity, eff, resultBuilder, log);
		}
	}
	
	private static class DamageEffect extends NegativeSpellEffect {
		private float amount;
		private EMagicElement element;
		
		public DamageEffect(EMagicElement element, float amount) {
			this.amount = amount;
			this.element = element;
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			caster.setLastHurtMob(entity);
			entity.setLastHurtByMob(caster);
			//entity.setHealth(Math.max(0f, entity.getHealth() - fin));
			entity.invulnerableTime = 0;
			
			final float baseDmg = amount;
			log.damageStart(this.amount, this.element);
			
			final float fin = SpellDamage.DamageEntity(entity, element, baseDmg, efficiency, caster, log);
			
			NostrumMagicaSounds sound;
			switch (element) {
			case EARTH:
				sound = NostrumMagicaSounds.DAMAGE_EARTH;
				break;
			case ENDER:
				sound = NostrumMagicaSounds.DAMAGE_ENDER;
				break;
			case FIRE:
				sound = NostrumMagicaSounds.DAMAGE_FIRE;
				break;
			case ICE:
				sound = NostrumMagicaSounds.DAMAGE_ICE;
				break;
			case LIGHTNING:
				sound = NostrumMagicaSounds.DAMAGE_LIGHTNING;
				break;
			case PHYSICAL:
			default:
				sound = NostrumMagicaSounds.DAMAGE_PHYSICAL;
				break;
			case WIND:
				sound = NostrumMagicaSounds.DAMAGE_WIND;
				break;
			
			}
			
			sound.play(entity);
			resultBuilder.applied |= true;
			resultBuilder.damage += fin;
			log.damageFinish(fin);
		}
		
		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			return; // Do nothing
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
	}
	
	private static class HealEffect implements SpellEffect {
		
		private static final Component LABEL_MOD_ICE_MASTER = new TranslatableComponent("spelllogmod.nostrummagica.ice.master");
		
		private float amount;
		
		public HealEffect(float amount) {
			this.amount = amount;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			float base = this.amount;
			
			log.pushModifierStack();
			final INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			if (attr != null && attr.hasSkill(NostrumSkills.Ice_Master)) {
				base *= 2;
				log.addGlobalModifier(LABEL_MOD_ICE_MASTER, +1f, ESpellLogModifierType.BONUS_SCALE);
			}
			
			if (entity.isInvertedHealAndHarm()) {
				caster.setLastHurtMob(entity);
				entity.setLastHurtByMob(caster);
				//entity.setHealth(Math.max(0f, entity.getHealth() - fin));
				
				log.damageStart(this.amount, EMagicElement.ICE); // trickery here: skill modifiers added globally so 'start' with real base
													// even though later we use the current 'base' modified amount
				final float fin = SpellDamage.DamageEntity(entity, EMagicElement.ICE, base, efficiency, caster, log);
				
				entity.invulnerableTime = 0;
				resultBuilder.damage += fin;
				log.damageFinish(fin);
			} else {
				
				log.healStart(this.amount, EMagicElement.ICE);
				entity.heal(base * efficiency);
				if (entity instanceof TameRedDragonEntity) {
					TameRedDragonEntity dragon = (TameRedDragonEntity) entity;
					if (dragon.isTamed() && dragon.getOwner() == caster) {
						dragon.addBond(1f);
					}
				} else if (entity instanceof ArcaneWolfEntity) {
					ArcaneWolfEntity wolf = (ArcaneWolfEntity) entity;
					if (wolf.isTame() && wolf.getOwner() == caster) {
						wolf.addBond(1f);
					}
				}
				resultBuilder.heals += base * efficiency;
				log.healFinish(base * efficiency);
				
				if (attr != null && attr.hasSkill(NostrumSkills.Ice_Adept)) {
					if (NostrumMagica.rand.nextBoolean()) {
						entity.addEffect(new MobEffectInstance(NostrumEffects.magicShield, (int)((20 * 15) * efficiency), 0));
					}
				}
				
				NostrumParticles.RISING_GLOW.spawn(entity.level, new SpawnParams(3, entity.getX(), entity.getY(), entity.getZ(), 0, 20, 0,
						new TargetLocation(entity, false)).setTargetBehavior(TargetBehavior.ATTACH).color(RenderFuncs.ARGBFade(EMagicElement.ICE.getColor(), .6f)));
			}
			
			NostrumMagicaSounds.STATUS_BUFF2.play(entity);
			resultBuilder.applied |= true;
			log.popModifierStack();
		}
		
		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			return; // Do nothing
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class HealFoodEffect implements SpellEffect {
		private static final Component LABEL_FOOD_NAME = new TranslatableComponent("spelllog.nostrummagica.food.name");
		private static final Component LABEL_BREED_NAME = new TranslatableComponent("spelllog.nostrummagica.breed.name");
		private static final Component LABEL_BREED_DESC = new TranslatableComponent("spelllog.nostrummagica.breed.desc");
		
		private int amount;
		
		public HealFoodEffect(int amount) {
			this.amount = amount;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			
			if (entity instanceof Player) {
				Player player = (Player) entity;
				player.getFoodData().eat((int) (amount * efficiency), 2);
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
				resultBuilder.applied |= true;
				log.generalEffectStart(LABEL_FOOD_NAME, new TranslatableComponent("spelllog.nostrummagica.food.desc", "" + amount, "" + (int) (amount * efficiency)), false);
				log.generalEffectFinish(0f, 0f);
				return;
			} else if (entity instanceof Animal && caster != null && 
					caster instanceof Player) {
				((Animal) entity)
					.setInLove((Player) caster);
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
				resultBuilder.applied |= true;
				log.generalEffectStart(LABEL_BREED_NAME, LABEL_BREED_DESC, false);
				log.generalEffectFinish(0f, 0f);
				return;
			} else {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
				return;
			}
			
		}
		
		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			return; // Do nothing
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class HealManaEffect implements SpellEffect {
		private static final Component LABEL_MANA_NAME = new TranslatableComponent("spelllog.nostrummagica.mana.name");
		
		private int amount;
		
		public HealManaEffect(int amount) {
			this.amount = amount;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			INostrumMagic magic = NostrumMagica.getMagicWrapper(entity);
			if (magic == null) {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
				return;
			} else {
				magic.addMana((int) (amount * efficiency));
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
				resultBuilder.applied |= true;
				log.generalEffectStart(LABEL_MANA_NAME, new TranslatableComponent("spelllog.nostrummagica.mana.desc", "" + amount, "" + (int) (amount * efficiency)), false);
				log.generalEffectFinish(0f, 0f);
				return;
			}
				
		}
		
		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			return; // Do nothing
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class StatusEffect extends NegativeSpellEffect {
		protected final MobEffect effect;
		protected final int duration;
		protected final int amp;
		
		public StatusEffect(MobEffect effect, int duration, int amp) {
			this.effect = effect;
			this.duration = duration;
			this.amp = amp; 
		}
		
		@Override
		public boolean isHarmful() {
			return this.effect.getCategory() == MobEffectCategory.HARMFUL;
		}
		
		protected @Nonnull MobEffectInstance makeEffect(LivingEntity caster, LivingEntity target, float efficiency, SpellActionResult resultBuilder) {
			return new MobEffectInstance(effect, (int) (duration * efficiency), amp);
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			entity.addEffect(this.makeEffect(caster, entity, efficiency, resultBuilder));
			
			if (isHarmful()) {
				caster.setLastHurtMob(entity);
				entity.setLastHurtByMob(caster);
				entity.hurt(DamageSource.mobAttack(caster), 0);
				NostrumMagicaSounds.STATUS_DEBUFF2.play(entity);
			} else {
				NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			}
			
			log.statusStart(this.effect, this.duration);
			log.statusFinish((int) (duration * efficiency));
			
			resultBuilder.applied |= true;
			return;
		}
		
		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			return; // Do nothing
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class DispelEffect implements SpellEffect {
		private static final Component LABEL_DISPEL_NAME = new TranslatableComponent("spelllog.nostrummagica.dispel.name");
		
		private int number; // -1 to clear all
		
		public DispelEffect(int number) {
			this.number = number; 
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			log.generalEffectStart(LABEL_DISPEL_NAME, new TranslatableComponent("spelllog.nostrummagica.dispel.desc", number), false);
			log.generalEffectFinish(0f, 0f);
			
			
			NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			
			if (number == -1 || entity.getActiveEffects().size() < number) {
				entity.removeAllEffects();
			} else {
				// Remove #number effects. We do this by getting another list of effects and shuffling, and then
				// just walking that list to remove from the real one
				List<MobEffectInstance> effectList = Lists.newArrayList(entity.getActiveEffects());
				Collections.shuffle(effectList);
				for (int i = 0; i < number; i++) {
					entity.removeEffect(effectList.get(i).getEffect());
				}
			}
			resultBuilder.applied |= true;
			return;
		}
		
		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			return; // Do nothing
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	public static interface IAmpProvider {
		public int getAmplifier(LivingEntity caster, LivingEntity target, float efficiency);
	}
	
	public static interface IOptionalEffectFilter extends IEffectPredicate {
		
	}
	
	private static class AmplifiedStatusEffect extends StatusEffect {
		
		protected final IAmpProvider amplifierSupplier;

		public AmplifiedStatusEffect(MobEffect effect, int duration, IAmpProvider amplifier) {
			super(effect, duration, 0);
			this.amplifierSupplier = amplifier;
		}
		
		@Override
		protected @Nonnull MobEffectInstance makeEffect(LivingEntity caster, LivingEntity target, float efficiency, SpellActionResult resultBuilder) {
			return new MobEffectInstance(effect, (int) (duration * efficiency), this.amplifierSupplier.getAmplifier(caster, target, efficiency));
		}
	}
	
	private static class OptionalStatusEffect extends StatusEffect {
		protected final IOptionalEffectFilter predicate;
		
		public OptionalStatusEffect(MobEffect effect, int duration, int amplifier, IOptionalEffectFilter predicate) {
			super(effect, duration, amplifier);
			this.predicate = predicate;
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			if (this.predicate.test(caster, entity, efficiency)) {
				super.applyEffect(caster, entity, efficiency, resultBuilder, log);
			}
		}
	}
	
	private static class BlinkEffect implements SpellEffect {
		
		private static final Component LABEL_BLINK_NAME = new TranslatableComponent("spelllog.nostrummagica.blink.name");
		private static final Component LABEL_BLINK_MOD_ENDERBELT = new TranslatableComponent("spelllogmod.nostrummagica.enderbelt");
		private static final Component LABEL_BLINK_MOD_ENDERSET = new TranslatableComponent("spelllogmod.nostrummagica.enderset");
		
		private float dist;
		
		public BlinkEffect(float dist) {
			this.dist = dist;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			log.pushModifierStack();
			
			boolean hasBelt = false;
			
			if (caster != null && caster instanceof Player) {
				// Look for lightning belt
				Container baubles = NostrumMagica.CuriosProxy.getCurios((Player) caster);
				if (baubles != null) {
					for (int i = 0; i < baubles.getContainerSize(); i++) {
						ItemStack stack = baubles.getItem(i);
						if (stack.isEmpty() || stack.getItem() != NostrumCurios.enderBelt) {
							continue;
						}
						
						hasBelt = true;
						break;
					}
				}
			}
			
			if (hasBelt) {
				efficiency += 1f;
				log.addGlobalModifier(LABEL_BLINK_MOD_ENDERBELT, +1f, ESpellLogModifierType.BONUS_SCALE);
			}
			
			if (ElementalArmor.GetSetCount(entity, EMagicElement.ENDER, ElementalArmor.Type.MASTER) == 4) {
				// has full ender set
				efficiency += 1f;
				log.addGlobalModifier(LABEL_BLINK_MOD_ENDERSET, +1f, ESpellLogModifierType.BONUS_SCALE);
			}
			
			// Apply efficiency bonus
			float dist = this.dist * efficiency;
			Vec3 direction = entity.getLookAngle().normalize();
			Vec3 source = entity.position();
			source = source.add(0, entity.getEyeHeight(), 0);
			Vec3 dest = SpellTeleportation.Blink(entity, source, direction, dist, hasBelt && entity.isShiftKeyDown());			
			
			if (dest != null) {
				// We are about to put feet at dest, so try to shift down so that eye position matches
				if (entity.level.isEmptyBlock(new BlockPos(dest.subtract(0, 1.5, 0)))) {
					dest = dest.subtract(0, 1.5, 0);
				}
				
				NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(entity, .5 + Math.floor(dest.x), Math.floor(dest.y), .5 + Math.floor(dest.z), caster);
				if (!event.isCanceled()) {
					entity.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
					entity.fallDistance = 0;
					NostrumMagicaSounds.STATUS_BUFF1.play(entity);
					NostrumMagica.fireTeleprotedOtherEvent(event.getEntity(), caster, event.getPrev(), event.getTarget());
					resultBuilder.applied |= true;
				}
			}
			
			log.generalEffectStart(LABEL_BLINK_NAME, new TranslatableComponent("spelllog.nostrummagica.blink.desc",
					String.format("%.1f", this.dist), String.format("%.1f", dist), String.format("%.1f", source.distanceTo(entity.position().add(0, entity.getEyeHeight(), 0)))), false);
			log.generalEffectFinish(0f, 0f);
			log.popModifierStack();
		}
		
		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			return; // Do nothing
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class PushEffect extends NegativeSpellEffect {

		private static final Component LABEL_PUSH_NAME = new TranslatableComponent("spelllog.nostrummagica.push.name");
		private static final Component LABEL_PULL_NAME = new TranslatableComponent("spelllog.nostrummagica.pull.name");
		
		private float range;
		private int amp; // - is pull
		
		public PushEffect(float range, int amp) {
			this.range = range;
			this.amp = amp; 
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			apply(caster, new SpellLocation(entity), efficiency, resultBuilder, log);
		}
		
		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			final Component desc = (amp < 0
					? new TranslatableComponent("spelllog.nostrummagica.pull.desc", String.format("%.1f", this.range))
					: new TranslatableComponent("spelllog.nostrummagica.push.desc", String.format("%.1f", this.range)));
			log.generalEffectStart(amp < 0 ? LABEL_PULL_NAME : LABEL_PUSH_NAME, desc, false);
			log.generalEffectFinish(0f, 0f);

			// We abs the amp here, but check it belwo for pull and negate vector
			float magnitude = .35f * (Math.abs(amp) + 1.0f) * (float) Math.min(2.0f, Math.max(0.0f, 1.0f + Math.log(efficiency)));
			Vec3 center = Vec3.atCenterOf(location.hitBlockPos); // idr why I wanted it centered in the block?
			NostrumMagicaSounds.DAMAGE_WIND.play(location.world, center.x, center.y, center.z);
			
			boolean any = false;
			for (Entity e : location.world.getEntities(null, 
					new AABB(center.x - range, center.y - range, center.z - range, center.x + range, center.y + range, center.z + range)
					)) {
				double dist = e.position().distanceTo(center); 
				if (dist <= range) {
					
					// If push, straight magnitude
					// If pull, cap magnitude so that it doesn't fly past player
					
					Vec3 force;
					Vec3 direction = e.position().add(0, e.getEyeHeight(), 0).subtract(center).normalize();
					force = new Vec3(
							direction.x * magnitude,
							direction.y * magnitude,
							direction.z * magnitude
							);
					if (amp < 0) {
						// pull
						// Cap force's magnitude at .2 dist
						double mod = force.length();
						if (mod > dist * .2) {
							mod = (dist * .4) / mod;
							force = new Vec3(
									force.x * mod,
									force.y * mod,
									force.z * mod
									);
						}

						force = new Vec3(
								force.x * -1.0,
								force.y * -1.0,
								force.z * -1.0);
					}
					
					e.push(force.x, force.y, force.z);
					any = true;
				}
			}
			
			resultBuilder.applied |= any;
			resultBuilder.affectedPos = new SpellLocation(location.world, location.hitBlockPos);
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return false;
		}
	}
	
	private static class TransmuteEffect implements SpellEffect {
		
		private static final Component LABEL_TRANSMUTE_NAME = new TranslatableComponent("spelllog.nostrummagica.transmute.name");
		
		private int level;
		
		public TransmuteEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			ItemStack inhand = entity.getMainHandItem();
			boolean offhand = false;
			if (inhand.isEmpty() || inhand.getItem() instanceof SpellScroll) {
				inhand = entity.getOffhandItem();
				offhand = true;
			}
			
			if (inhand.isEmpty())
				return;
			
			Item item = inhand.getItem();
			TransmuteResult<Item> result = Transmutation.GetTransmutationResult(item, level);
			
			if (!result.valid) {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
				log.generalEffectStart(LABEL_TRANSMUTE_NAME, new TranslatableComponent("spelllog.nostrummagica.transmute_fail.desc", item.getDescription()), false);
				log.generalEffectFinish(0f, 0f);
				return;
			}

			log.generalEffectStart(LABEL_TRANSMUTE_NAME, new TranslatableComponent("spelllog.nostrummagica.transmute.desc", item.getDescription(), result.output.getDescription()), false);
			log.generalEffectFinish(0f, 0f);
			
			ItemStack stack = new ItemStack(result.output);
			NostrumMagicaSounds.CAST_CONTINUE.play(entity);
			
			// Award knowledge
			INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			attr.giveTransmuteKnowledge(result.source.getName(), level);
			if (caster instanceof ServerPlayer) {
				NostrumMagica.Proxy.syncPlayer((ServerPlayer) caster);
			}
			
			if (entity instanceof Player) {
				Player p = (Player) entity;
				if (inhand.getCount() == 1) {
					if (offhand) {
						p.getInventory().removeItemNoUpdate(40);
					} else {
						p.getInventory().removeItemNoUpdate(p.getInventory().selected);
					}
					((Player) entity).getInventory().add(stack);
				} else {
					inhand.split(1);
					((Player) entity).getInventory().add(stack);
				}
			} else {
				// MobEntity has held item in slot 0
				entity.setItemInHand(InteractionHand.MAIN_HAND, stack);
			}

			resultBuilder.applied |= true;
		}
		
		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			Block block = location.world.getBlockState(location.selectedBlockPos).getBlock();
			TransmuteResult<Block> result = Transmutation.GetTransmutationResult(block, level);
			if (!result.valid) {
				NostrumMagicaSounds.CAST_FAIL.play(location.world, location.selectedBlockPos.getX() + .5, location.selectedBlockPos.getY(), location.selectedBlockPos.getZ() + .5);
				log.generalEffectStart(LABEL_TRANSMUTE_NAME, new TranslatableComponent("spelllog.nostrummagica.transmute_fail.desc", new TranslatableComponent(block.getDescriptionId())), false);
				log.generalEffectFinish(0f, 0f);
				return;
			}
			
			log.generalEffectStart(LABEL_TRANSMUTE_NAME, new TranslatableComponent("spelllog.nostrummagica.transmute.desc", new TranslatableComponent(block.getDescriptionId()), new TranslatableComponent(result.output.getDescriptionId())), false);
			log.generalEffectFinish(0f, 0f);
			
			NostrumMagicaSounds.CAST_CONTINUE.play(location.world, location.selectedBlockPos.getX() + .5, location.selectedBlockPos.getY(), location.selectedBlockPos.getZ() + .5);

			// Award knowledge
			INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			attr.giveTransmuteKnowledge(result.source.getName(), level);
			
			if (caster instanceof ServerPlayer) {
				NostrumMagica.Proxy.syncPlayer((ServerPlayer) caster);
			}
			
			location.world.setBlockAndUpdate(location.selectedBlockPos, result.output.defaultBlockState());
			resultBuilder.applied |= true;
			resultBuilder.affectedPos = new SpellLocation(location.world, location.selectedBlockPos);
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class BurnEffect extends NegativeSpellEffect {
		
		private static final Component LABEL_BURN_NAME = new TranslatableComponent("spelllog.nostrummagica.burn.name");

		private int duration;
		
		public BurnEffect(int duration) {
			this.duration = duration;
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			int duration = (int) (this.duration * efficiency);
			if (duration == 0)
				return; // Nope
			
			NostrumMagicaSounds.DAMAGE_FIRE.play(entity);
			
			caster.setLastHurtMob(entity);
			entity.setLastHurtByMob(caster);
			entity.hurt(DamageSource.mobAttack(caster), 0);
			entity.invulnerableTime = 0;
			
			entity.setSecondsOnFire((int) Math.ceil((float) duration / 20.0f));
			
			@Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			if (attr != null && attr.hasSkill(NostrumSkills.Fire_Inflict)) {
				// Also bust shields
				entity.removeEffect(NostrumEffects.mysticWater);
				entity.removeEffect(NostrumEffects.physicalShield);
				entity.removeEffect(NostrumEffects.magicShield);
				entity.removeEffect(MobEffects.ABSORPTION);
			}
			
			resultBuilder.applied |= true;
			
			log.generalEffectStart(LABEL_BURN_NAME,
					new TranslatableComponent("spelllog.nostrummagica.burn_ent.desc", String.format("%.1f", (float) this.duration / 20f), "" + (int) Math.ceil((float) duration / 20.0f)), true);
			log.generalEffectFinish(0f, 0f);
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			BlockPos applyPos = location.hitBlockPos;
			BlockState state = location.world.getBlockState(applyPos);
			if (state != null && state.getBlock() instanceof CandleBlock) {
				CandleBlock.light(location.world, applyPos, state);
				NostrumMagicaSounds.DAMAGE_FIRE.play(location.world,
						applyPos.getX() + .5, applyPos.getY(), applyPos.getZ() + .5);
				resultBuilder.applied |= true;
			}
			
			if (DimensionUtils.IsSorceryDim(location.world)) {
				return;
			}
			
			if (location.world.isEmptyBlock(applyPos)) {
				location.world.setBlockAndUpdate(applyPos, Blocks.FIRE.defaultBlockState());
				NostrumMagicaSounds.DAMAGE_FIRE.play(location.world,
						applyPos.getX() + .5, applyPos.getY(), applyPos.getZ() + .5);
				resultBuilder.applied |= true;
				resultBuilder.affectedPos = new SpellLocation(location.world, applyPos);
				
				log.generalEffectStart(LABEL_BURN_NAME,
						new TranslatableComponent("spelllog.nostrummagica.burn.desc"), true);
				log.generalEffectFinish(0f, 0f);
			}
			return;
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class LightningEffect extends NegativeSpellEffect {
		
		private static final Component LABEL_LIGHTNING_NAME = new TranslatableComponent("spelllog.nostrummagica.lightning.name");
		private static final Component LABEL_LIGHTNING_MOD_BELT = new TranslatableComponent("spelllogmod.nostrummagica.lightningbelt");
		
		public LightningEffect() {
			
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			entity.setLastHurtByMob(caster);
			entity.hurt(DamageSource.mobAttack(caster), 0);
			entity.invulnerableTime = 0;
			apply(caster, new SpellLocation(entity), efficiency, resultBuilder, log);
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			int count = 1;
			
			log.pushModifierStack();
			
			if (caster != null && caster instanceof Player) {
				// Look for lightning belt
				Container baubles = NostrumMagica.CuriosProxy.getCurios((Player) caster);
				if (baubles != null) {
					for (int i = 0; i < baubles.getContainerSize(); i++) {
						ItemStack stack = baubles.getItem(i);
						if (stack.isEmpty() || stack.getItem() != NostrumCurios.lightningBelt) {
							continue;
						}
						
						count = caster.getRandom().nextInt(3) + 3;
						log.addGlobalModifier(LABEL_LIGHTNING_MOD_BELT, count - 1, ESpellLogModifierType.FINAL_FLAT); // dishonest; not a damage bonus
						break;
					}
				}
			}
			
			float damage = 5; // Default for lightning
			INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			if (attr != null && attr.hasSkill(NostrumSkills.Lightning_Adept)) {
				damage += 1;
			}
			
			damage = (damage * efficiency);
			
			log.generalEffectStart(LABEL_LIGHTNING_NAME,
					new TranslatableComponent("spelllog.nostrummagica.lightning.desc", "" + count, String.format("%.1f", damage)), true);
			log.generalEffectFinish(damage * count, 0f);
			
			final BlockPos applyPos = location.hitBlockPos;
			BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos().set(applyPos);
			Random rand = (caster == null ? new Random() : caster.getRandom());
			for (int i = 0; i < count; i++) {
				
				if (i == 0) {
					; // Don't adjust pos
				} else {
					// Apply random x/z offsets. Then step up to 4 to find surface
					cursor.set(
							applyPos.getX() + rand.nextInt(6) - 3,
							applyPos.getY() - 2,
							applyPos.getZ() + rand.nextInt(6) - 3);
					
					// Find surface
					int dist = 0;
					while (dist++ < 4 && !location.world.isEmptyBlock(cursor)) {
						cursor.setY(cursor.getY() + 1);
					}
				}
				
				TameLightning bolt = new TameLightning(NostrumEntityTypes.tameLightning, location.world, cursor.getX() + 0.5, cursor.getY(), cursor.getZ() + 0.5);
				bolt.setEntityToIgnore(caster);
				bolt.setDamage(damage);
				
				((ServerLevel) location.world).addFreshEntity(bolt);
				
				
			}

			resultBuilder.applied |= true;
			resultBuilder.affectedPos = new SpellLocation(location.world, applyPos);
			
			log.popModifierStack();
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return false;
		}
	}
	
	private static class SummonEffect implements SpellEffect {
		private EMagicElement element;
		private int power;
		
		public SummonEffect(EMagicElement element, int power) {
			this.element = element;
			this.power = power;
			
			if (this.element == null)
				this.element = EMagicElement.PHYSICAL;
		}

		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			// Pick a place to spawn it and then defer to location one
			Level world = caster.getCommandSenderWorld();
			BlockPos center = caster.blockPosition();
			BlockPos pos;
			do {
				pos = center.offset(1, 1, 0);
				if (world.isEmptyBlock(pos))
					break;
				
				pos = center.offset(-1, 1, 0);
				if (world.isEmptyBlock(pos))
					break;
				
				pos = center.offset(0, 1, -1);
				if (world.isEmptyBlock(pos))
					break;
				
				pos = center.offset(0, 1, 1);
				if (world.isEmptyBlock(pos))
					break;
				
				pos = center;
			} while (false);
			
			apply(caster, new SpellLocation(world, pos), efficiency, resultBuilder, log);
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			final BlockPos applyPos = location.hitBlockPos;
			
			// For non-player entities, just spawn some new golems.
			// Else spawn player-bound golems
			if (caster instanceof Player) {
				if (NostrumMagica.getMagicWrapper(caster) == null) {
					return;
				}
				
				NostrumMagica.getMagicWrapper(caster).clearFamiliars();
				caster.removeEffectNoUpdate(NostrumEffects.familiar);
				for (int i = 0; i < power; i++) {
					MagicGolemEntity golem = spawnGolem(location.world);
					golem.setPos(applyPos.getX() + .5, applyPos.getY(), applyPos.getZ() + .5);
					location.world.addFreshEntity(golem);
					golem.setOwnerUUID(caster.getUUID());
					NostrumMagica.getMagicWrapper(caster).addFamiliar(golem);
				}
				int baseTime = (int) (20 * 60 * 2.5 * Math.pow(2, Math.max(0, power - 1)));
				int time = (int) (baseTime * efficiency);
				caster.addEffect(new MobEffectInstance(NostrumEffects.familiar, time, 0) {
					@Override
					public boolean tick(LivingEntity entityIn, Runnable onComplete) {
						// heh snekky
						boolean ret = super.tick(entityIn, onComplete);
						if (ret) {
							// we're not being removed. Check familiars
							if (entityIn.level.isClientSide) {
								return true;
							}
							
							INostrumMagic attr = NostrumMagica.getMagicWrapper(entityIn);
							if (attr != null) {
								boolean active = false;
								if (!attr.getFamiliars().isEmpty()) {
									for (LivingEntity fam : attr.getFamiliars()) {
										if (fam.isAlive()) {
											active = true;
											break;
										}
									}
								}
								if (!active) {
									ret = false;
								}
							}
						}
						
						return ret;
					}
				});
				
				final Component LABEL_SUMMON_NAME = new TranslatableComponent("spelllog.nostrummagica.summon.name", this.element.getDisplayName());
				final Component LABEL_SUMMON_DESC = new TranslatableComponent("spelllog.nostrummagica.summon.desc", this.element.getDisplayName(), "" + power, "" + ((float) (baseTime) / 20f), "" + ((float) time / 20f));
				log.generalEffectStart(LABEL_SUMMON_NAME, LABEL_SUMMON_DESC, false);
				log.generalEffectFinish(0f, 0f);
			} else {
				// Just summon some new golems
				final int time = (int) (20f * (15f * efficiency));
				for (int i = 0; i < power; i++) {
					MagicGolemEntity golem = spawnGolem(location.world);
					golem.setPos(applyPos.getX() + .5, applyPos.getY(), applyPos.getZ() + .5);
					golem.setExpiresAfterTicks(time);
					location.world.addFreshEntity(golem);
				}
			}
			
			NostrumMagicaSounds.CAST_CONTINUE.play(location.world,
					applyPos.getX() + .5, applyPos.getY(), applyPos.getZ() + .5);
			resultBuilder.applied |= true;
			resultBuilder.affectedPos = new SpellLocation(location.world, applyPos);
		}
		
		private MagicGolemEntity spawnGolem(Level world) {
			MagicGolemEntity golem;
			
			switch (element) {
			case EARTH:
				golem = new MagicEarthGolemEntity(NostrumEntityTypes.golemEarth, world);
				break;
			case ENDER:
				golem = new MagicEnderGolemEntity(NostrumEntityTypes.golemEnder, world);
				break;
			case FIRE:
				golem = new MagicFireGolemEntity(NostrumEntityTypes.golemFire, world);
				break;
			case ICE:
				golem = new MagicIceGolemEntity(NostrumEntityTypes.golemIce, world);
				break;
			case LIGHTNING:
				golem = new MagicLightningGolemEntity(NostrumEntityTypes.golemLightning, world);
				break;
			case WIND:
				golem = new MagicWindGolemEntity(NostrumEntityTypes.golemWind, world);
				break;
			default:
			case PHYSICAL:
				golem = new MagicPhysicalGolemEntity(NostrumEntityTypes.golemPhysical, world);
				break;
			}
			
			return golem;
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return false;
		}
	}
	
	private static class SwapEffect extends NegativeSpellEffect {
		
		public SwapEffect() {
			
		}
		
		@Override
		public boolean isHarmful() {
			return false;
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			if (caster == null || entity == null)
				return;
			
			Vec3 pos = caster.position();
			float pitch = caster.getXRot();
			float yaw = caster.yHeadRot;
			
			NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(caster, entity.getX(), entity.getY(), entity.getZ(), caster);
			if (!event.isCanceled()) {
				if (caster instanceof Player) {
					caster.absMoveTo(caster.getX(), caster.getY(), caster.getZ(), entity.yHeadRot, entity.getXRot());
					caster.teleportTo(
							event.getTargetX(), event.getTargetY(), event.getTargetZ());
				} else {
					caster.absMoveTo(
							event.getTargetX(), event.getTargetY(), event.getTargetZ(),
							entity.getXRot(), entity.yHeadRot
							);
				}
				NostrumMagica.fireTeleprotedOtherEvent(event.getEntity(), caster, event.getPrev(), event.getTarget());
			}
			
			event = NostrumMagica.fireTeleportAttemptEvent(entity, pos.x, pos.y, pos.z, caster);
			if (!event.isCanceled()) {
				if (entity instanceof Player) {
					entity.absMoveTo(entity.getX(), entity.getY(), entity.getZ(), yaw, pitch);
					entity.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
				} else {
					entity.absMoveTo(event.getTargetX(), event.getTargetY(), event.getTargetZ(), yaw, pitch);
				}
				NostrumMagica.fireTeleprotedOtherEvent(event.getEntity(), caster, event.getPrev(), event.getTarget());
			}
			
			entity.fallDistance = 0;
			caster.fallDistance = 0;
			resultBuilder.applied |= true;			
		}
		
		protected BlockPos adjustPosition(Level world, BlockPos pos) {
			// Do one graceful positioning attempt. If block above is not air but there's room to shift down
			// one, do so
			if (!world.isEmptyBlock(pos.above()) && world.isEmptyBlock(pos.below())) {
				return pos.below();
			}
			
			return pos;
		}
		
		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			BlockPos pos = adjustPosition(location.world, location.hitBlockPos);
			
			NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(caster, pos.getX() + .5, pos.getY(), pos.getZ() + .5, caster);
			if (!event.isCanceled()) {
				caster.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
				caster.fallDistance = 0;
				resultBuilder.applied |= true;
				resultBuilder.affectedPos = new SpellLocation(location.world, pos);
				NostrumMagica.fireTeleprotedOtherEvent(event.getEntity(), caster, event.getPrev(), event.getTarget());
			}
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class PropelEffect implements SpellEffect {
		
		int level;
		
		public PropelEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {

			Vec3 force = entity.getLookAngle().add(0, 0.15, 0).normalize();
			float scale = 1f * (.5f * (level + 1)) * efficiency;
			
			if (entity.isFallFlying()) {
				scale *= .5f;
			}
			
			force = new Vec3(force.x * scale, force.y * scale, force.z * scale);
			
			entity.setDeltaMovement(entity.getDeltaMovement().add(force.x, force.y, force.z));
			entity.hurtMarked = true;
			
			NostrumMagicaSounds.DAMAGE_WIND.play(entity);
			resultBuilder.applied |= true;
		}
		
		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			return; // Doesn't mean anything
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class PhaseEffect extends NegativeSpellEffect {
		
		private int level;
		
		public PhaseEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			if (caster != entity && entity instanceof Mob) {
				// Make sure they want to attack you if you do it
				entity.setLastHurtByMob(caster);
				entity.hurt(DamageSource.mobAttack(caster), 0);
				entity.invulnerableTime = 0;
			}
			
			double radius = (16 * Math.min(1, level)) * efficiency;
			boolean hasBelt = false;
			
			if (caster != null && caster instanceof Player) {
				// Look for ender belt
				Container baubles = NostrumMagica.CuriosProxy.getCurios((Player) caster);
				if (baubles != null) {
					for (int i = 0; i < baubles.getContainerSize(); i++) {
						ItemStack stack = baubles.getItem(i);
						if (stack.isEmpty() || stack.getItem() != NostrumCurios.enderBelt) {
							continue;
						}
						
						hasBelt = true;
						break;
					}
				}
			}
			
			if (hasBelt) {
				radius *= 2.0;
			}
			
			if (ElementalArmor.GetSetCount(entity, EMagicElement.ENDER, ElementalArmor.Type.MASTER) == 4) {
				// has full ender set
				radius *= 2.0;
			}
			
			NostrumMagicaSounds.DAMAGE_ENDER.play(entity);
			
			for (int i = 0; i < 20; i++) {
				// Pick a random direction to blink in
				float dirYaw = NostrumMagica.rand.nextFloat() * 360f;
				float dirPitch = NostrumMagica.rand.nextFloat() * 180f;
				Vec3 direction = Vec3.directionFromRotation(dirPitch, dirYaw);
				
				// Blink in that direction
				Vec3 dest = SpellTeleportation.Blink(entity, entity.position().add(0, entity.getEyeHeight(), 0), direction, radius, hasBelt);
				
				// Check if far enough
				if (dest != null && dest.distanceTo(entity.position()) > 3) {
					// Try to teleport
			        NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(entity, dest.x, dest.y, dest.z, caster);
					if (event.isCanceled()) {
						// Break on a single cancel
						break;
					} else {
						if (entity.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), false)) {
							NostrumMagica.fireTeleprotedOtherEvent(event.getEntity(), caster, event.getPrev(), event.getTarget());
							break;
						}
					}
				}
			}
			resultBuilder.applied |= true;
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			// Summon an entity
			int count = 1;
			for (int i = 1; i < level; i++)
				if (NostrumMagica.rand.nextBoolean())
					count++;
			
			for (int i = 0; i < count; i++) {
				Entity entity;
				if (NostrumMagica.rand.nextFloat() <= .1f) {
					entity = new ItemEntity(location.world,
							location.hitPosition.x, location.hitPosition.y + .1, location.hitPosition.z,
							new ItemStack(Items.ENDER_PEARL));
				} else if (NostrumMagica.rand.nextFloat() <= .3) {
					entity = EntityType.ENDERMAN.create(location.world);
				} else {
					entity = EntityType.ENDERMITE.create(location.world);
				}
				
				entity.setPos(location.hitPosition.x, location.hitPosition.y + .1, location.hitPosition.z);
				
				location.world.addFreshEntity(entity);
			}
			
			NostrumMagicaSounds.DAMAGE_ENDER.play(location.world, location.hitPosition.x, location.hitPosition.y, location.hitPosition.z);
			resultBuilder.applied |= true;
			resultBuilder.affectedPos = new SpellLocation(location.world, location.hitBlockPos);
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
		
	}
	
	private static class EnchantEffect implements SpellEffect {
		
		private int level;
		private EMagicElement element;
		
		public EnchantEffect(EMagicElement element, int level) {
			this.level = level;
			this.element = element;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			// If entity is special enchantable entity, try to use that
			if (entity instanceof IEnchantableEntity && ((IEnchantableEntity) entity).canEnchant(entity, element, level)) {
				resultBuilder.applied |= ((IEnchantableEntity) entity).attemptEnchant(entity, element, level);
			}
			
			// Different effect if non-player casts: just give magic buff
			if (!(caster instanceof Player)) {
				int count = level + 1;
				double amt = 2 + level;
				caster.removeEffectNoUpdate(NostrumEffects.magicBuff);
				NostrumMagica.magicEffectProxy.applyMagicBuff(entity, element, amt, count);
				entity.addEffect(new MobEffectInstance(NostrumEffects.magicBuff, 60 * 20, 0));
				resultBuilder.applied |= true;
			}
			
			ItemStack inhand = entity.getMainHandItem();
			boolean offhand = false;
			if (!isEnchantable(inhand)) {
				inhand = entity.getOffhandItem();
				offhand = true;
			}
			
			ItemStack addedItem = ItemStack.EMPTY;
			boolean didEmpower = false;
			boolean consumeInput = false;
			
			// Main hand attempt
			if (!inhand.isEmpty()) {
				Item item = inhand.getItem();
				if (item instanceof IEnchantableItem) {
					IEnchantableItem.Result result = ((IEnchantableItem) item).attemptEnchant(inhand, entity, element, level);
					didEmpower = result.success;
					addedItem = result.resultItem;
					consumeInput = result.consumeInput;
				}
			}
			
			if (addedItem.isEmpty() && !didEmpower) {
				//NostrumMagicaSounds.CAST_FAIL.play(entity);
			} else {
				if (entity instanceof Player) {
					Player p = (Player) entity;
					
					if (consumeInput) {
						if (inhand.getCount() == 1) {
							if (offhand) {
								p.getInventory().removeItemNoUpdate(40);
							} else {
								p.getInventory().removeItemNoUpdate(p.getInventory().selected);
							}
						} else {
							inhand.split(1);
						}
					}
					if (!addedItem.isEmpty()) {
						((Player) entity).getInventory().add(addedItem);
					}
					
					
				} else {
					// MobEntity has held item in slot 0
					if (!addedItem.isEmpty()) {
						entity.setItemInHand(InteractionHand.MAIN_HAND, addedItem);
					}
				}
				NostrumMagicaSounds.CAST_CONTINUE.play(entity);
			}
			
			// Apply enchant effect
			entity.addEffect(new MobEffectInstance(ElementalEnchantEffect.GetForElement(this.element), (int) (20 * 15 * efficiency), level-1));
			resultBuilder.applied |= true;
			
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			return;
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class GrowEffect implements SpellEffect {
		
		private int count;
		
		public GrowEffect(int count) {
			this.count = count;
		}

		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			if (entity instanceof Animal) {
				Animal animal = (Animal) entity;
				animal.ageUp((int) (count * 500 * efficiency));
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
				resultBuilder.applied |= true;
			}
		}

		@SuppressWarnings("deprecation")
		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			// Since farmland is smaller than a block, standing on it means the block below you (at feet trigger) is
			// the block below the farmland. So try and step up if we're in that specific case.
			BlockPos pos = location.selectedBlockPos;
			
			if (location.world.getBlockState(pos.above()).getBlock() instanceof FarmBlock) {
				pos = pos.above();
			}
			
			if (location.world.getBlockState(pos).getBlock() instanceof FarmBlock) {
				pos = pos.above();
			}
			
			if (location.world.isEmptyBlock(pos)) {
				return;
			}
			
			ItemStack junk = new ItemStack(Items.BONE_MEAL, 10); // each apply call may reduce count by 1
			boolean worked = false;
			for (int i = 0; i < count; i++) {
				if (caster instanceof Player) {
					if (!BoneMealItem.applyBonemeal(junk, location.world, pos, (Player)caster)) {
						break;
					}
				} else {
					if (!BoneMealItem.growCrop(junk, location.world, pos)) {
						break;
					}
				}
				worked = true;
			}
			
			if (worked) {
				NostrumMagicaSounds.STATUS_BUFF2.play(location.world, pos); // TODO this and fire both movep osition up which is bad
			}
			
			resultBuilder.applied |= worked;
			if (worked) {
				resultBuilder.affectedPos = new SpellLocation(location.world, pos);
			}
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}
	
	private static class BurnArmorEffect extends NegativeSpellEffect {
		
		private int level;
		
		public BurnArmorEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			int amount = (int) (20 * level * efficiency);
			if (level > 2)
				amount *= 2;
			
			int count = 0;
			for (ItemStack equip : entity.getArmorSlots()) {
				if (equip.isEmpty())
					continue;
				
				count++;
			}
			if (count != 0) {
				for (ItemStack equip : entity.getArmorSlots()) {
					if (equip.isEmpty() || !(equip.getItem() instanceof ArmorItem))
						continue;
					ItemStacks.damageEquippedArmor(equip, entity, ((ArmorItem) equip.getItem()).getSlot(), amount/count);
				}
			}
			
			NostrumMagicaSounds.MELT_METAL.play(entity);
			caster.setLastHurtMob(entity);
			entity.setLastHurtByMob(caster);
			entity.hurt(DamageSource.mobAttack(caster), 0);
			entity.invulnerableTime = 0;
			resultBuilder.applied |= true;
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			return;
		}
		
		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
		
	}
	
	private static class WallEffect implements SpellEffect {
		
		private int level;
		
		public WallEffect(int level) {
			this.level = level;
		}

		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			// Apply mystic air.
			// Doing in here instead of a status effect so that wall doesn't get created if used on an entity
			final int duration = (int) (20 * 60 * efficiency);
			final int amp = (int) (level * efficiency) - 1; // amp 0 is 1
			entity.addEffect(new MobEffectInstance(NostrumEffects.mysticAir, duration, amp));
			resultBuilder.applied |= true;
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			BlockPos pos = location.hitBlockPos;
			if (!location.world.isEmptyBlock(pos) && !(location.world.getBlockState(pos).getBlock() instanceof MagicWallBlock)) {
				NostrumMagicaSounds.CAST_FAIL.play(location.world, pos);
			} else {
				NostrumMagicaSounds.DAMAGE_WIND.play(location.world, pos);
				location.world.setBlockAndUpdate(pos, NostrumBlocks.magicWall.getState(level));
				resultBuilder.applied |= true;
				resultBuilder.affectedPos = new SpellLocation(location.world, pos);
			}
				
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
		
	}

	private static class MysticWater implements SpellEffect {
		
		private final int waterLevel;
		private final int healAmt;
		private final int effectDuration;
		
		public MysticWater(int level, int effectHealAmt, int effectDuration) {
			this.waterLevel = level;
			this.healAmt = effectHealAmt;
			this.effectDuration = effectDuration;
		}

		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			// Apply status here instead of using a StatusEffect so that we can scale the amplitude, too
			float amp = healAmt * efficiency;
			final @Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			if (attr != null && attr.hasSkill(NostrumSkills.Ice_Master)) {
				amp *= 2;
			}
			
			entity.addEffect(new MobEffectInstance(NostrumEffects.mysticWater, (int) (effectDuration * efficiency), (int) amp));
			NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			resultBuilder.applied |= true;
			
			if (attr != null && attr.hasSkill(NostrumSkills.Ice_Adept)) {
				if (NostrumMagica.rand.nextBoolean()) {
					entity.addEffect(new MobEffectInstance(NostrumEffects.magicShield, (int)((20 * 15) * efficiency), 0));
				}
			}
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			BlockPos pos = location.hitBlockPos;
			if (location.world.isEmptyBlock(pos) || location.world.getBlockState(pos).getBlock() instanceof MysticWaterBlock) {
				boolean allowed = true;
				
				// In sorcery dimension, make sure no lava is nearby
				if (DimensionUtils.IsSorceryDim(location.world)) {
					for (Direction direction : LiquidBlock.POSSIBLE_FLOW_DIRECTIONS) {
						BlockPos blockpos = pos.relative(direction);
			            if (location.world.getFluidState(blockpos).is(FluidTags.LAVA)) {
			            	allowed = false;
			            	break;
			            }
					}
				}
				
				if (allowed) {
					location.world.setBlockAndUpdate(pos, NostrumBlocks.mysticWaterBlock.getStateWithPower(this.waterLevel));
					NostrumMagicaSounds.DAMAGE_ICE.play(location.world, pos);
					resultBuilder.applied |= true;
					resultBuilder.affectedPos = new SpellLocation(location.world, pos);
				}
			}
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return true;
		}
	}

	private static class CursedFire implements SpellEffect {
		
		private int level;
		
		public CursedFire(int level) {
			this.level = level;
		}

		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			@Nullable MobEffectInstance instance = entity.getEffect(NostrumEffects.cursedFire);
			final int duration = 20 * 1200;
			if (instance == null || instance.getDuration() < (int) (duration * .8f)) {
				entity.addEffect(new MobEffectInstance(
						NostrumEffects.cursedFire,
						duration,
						0
						));
			}
			caster.setLastHurtMob(entity);
			entity.setLastHurtByMob(caster);
			entity.hurt(DamageSource.mobAttack(caster), 0);
			resultBuilder.applied |= true;
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			final BlockState state = NostrumBlocks.cursedFire.GetWithLevel(level);
			final BlockPos pos = location.hitBlockPos;
			if (location.world.getBlockState(pos).getMaterial().isReplaceable() && state.canSurvive(location.world, pos) && location.world.setBlockAndUpdate(pos, state)) {
				NostrumMagicaSounds.DAMAGE_FIRE.play(location.world, pos);
				resultBuilder.applied |= true;
				resultBuilder.affectedPos = new SpellLocation(location.world, pos);
			}
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return false;
		}
	}
	
	private static class BreakEffect implements SpellEffect {

		private int level;
		
		public BreakEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			apply(caster, new SpellLocation(entity.level, entity.blockPosition().offset(0, -1, 0)), eff, resultBuilder, log);
		}
		
		@SuppressWarnings("deprecation")
		protected boolean isTool(@Nullable Player player, ItemStack stack) {
			if (stack.isEmpty()) {
				return false;
			}
			
			if (stack.getItem() instanceof TieredItem) {
				Tier tier = ((TieredItem) stack.getItem()).getTier();
				if (TierSortingRegistry.isTierSorted(tier)) {
					// See if iron is lower than this
					return TierSortingRegistry.getTiersLowerThan(tier).contains(Tiers.IRON);
				} else {
					return tier.getLevel() >= 2;
				}
			}
			
			return false;
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			final BlockPos pos = location.selectedBlockPos;
			if (location.world.isEmptyBlock(pos))
				return;
			
			if (DimensionUtils.IsSorceryDim(location.world)) {
				return;
			}
			
			BlockState state = location.world.getBlockState(pos);
			if (state == null || state.getMaterial().isLiquid())
				return;
			
			boolean onlyStone = (level <= 1);
			if (onlyStone && caster instanceof Player) {
				if (!state.is(Tags.Blocks.STONE)) {
					return;
				}
			}
			
			boolean usePickaxe = (level >= 3);
			float hardness = state.getDestroySpeed(location.world, pos);
			
			if (hardness >= 100f || hardness < 0f)
				return;
			
			if (caster instanceof ServerPlayer) {
				// This checks item harvest level >:(
//				if (!state.getBlock().canHarvestBlock(location.world, block, (PlayerEntity) caster)) {
//					return false;
//				}
				
				if (usePickaxe) {
					// Check if they have a pickaxe
					ItemStack inHand = caster.getMainHandItem();
					if (!isTool((Player) caster, inHand)) {
						inHand = caster.getOffhandItem();
					}
					if (!isTool((Player) caster, inHand)) {
						usePickaxe = false;
					}
				}
				
				if (usePickaxe) {
					((ServerPlayer) caster).gameMode.destroyBlock(pos);
				} else {
					location.world.destroyBlock(pos, true);
				}
			} else {
				location.world.destroyBlock(pos, true);
			}
			
			resultBuilder.applied |= true;
			resultBuilder.affectedPos = new SpellLocation(location.world, pos);
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return false;
		}
		
	}
	
	private static class HarvestEffect implements SpellEffect {

		// Level 1 harvests like normal.
		// Level 2+ harvests all connected.
		// Level 3 uses tool in the caster's hand
		private int level;
		
		public HarvestEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			if (attr != null && attr.hasSkill(NostrumSkills.Earth_Corrupt)) {
				entity.addEffect(new MobEffectInstance(NostrumEffects.lootLuck, (int) (15 * eff) * 20, level-1));
				resultBuilder.applied = true;
			} else {
				apply(caster, new SpellLocation(entity.level, entity.blockPosition().offset(0, -1, 0)), eff, resultBuilder, log);
			}
		}
		
		@SuppressWarnings("deprecation")
		protected boolean isTool(@Nullable Player player, ItemStack stack) {
			if (stack.isEmpty()) {
				return false;
			}
			
			if (stack.getItem().canPerformAction(stack, ToolActions.AXE_DIG)) {
				if (stack.getItem() instanceof TieredItem) {
					Tier tier = ((TieredItem) stack.getItem()).getTier();
					if (TierSortingRegistry.isTierSorted(tier)) {
						// See if iron is lower than this
						return TierSortingRegistry.getTiersLowerThan(tier).contains(Tiers.IRON);
					} else {
						return tier.getLevel() >= 2;
					}
				}
			}
			
			return false;
		}
		
		protected boolean harvestCropBlock(LivingEntity caster, Level world, BlockPos block, @Nullable ItemStack tool) {
			return HarvestUtil.HarvestCrop(world, block);
		}
		
		protected boolean harvestCrop(LivingEntity caster, Level world, BlockPos block, @Nullable ItemStack tool, Set<BlockPos> visitted) {
			if (visitted.contains(block)) {
				return false;
			}
			
			visitted.add(block);
			if (harvestCropBlock(caster, world, block, tool)) {
				// Try nearby crops now
				harvestCrop(caster, world, block.north(), tool, visitted);
				harvestCrop(caster, world, block.east(), tool, visitted);
				harvestCrop(caster, world, block.south(), tool, visitted);
				harvestCrop(caster, world, block.west(), tool, visitted);
				return true;
			} else {
				// Not crop or not fully grown
				return false;
			}
		}
		
		protected boolean harvestCrop(LivingEntity caster, Level world, BlockPos block, @Nullable ItemStack tool, boolean doNearby) {
			if (doNearby) {
				return harvestCrop(caster, world, block, tool, new HashSet<>());
			} else {
				return harvestCropBlock(caster, world, block, tool);
			}
		}
		
		protected boolean harvestTreeBlock(LivingEntity caster, Level world, BlockPos pos, @Nullable ItemStack tool) {
			if (tool != null) {
				((ServerPlayer) caster).gameMode.destroyBlock(pos);
			} else {
				world.destroyBlock(pos, true);
			}
			return true;
		}
		
		protected boolean harvestTree(LivingEntity caster, Level world, BlockPos pos, @Nullable ItemStack tool, boolean wholeTree) {
			final class LastNode {
				@Nullable BlockPos finalPos = null;
				int maxDepth = -1;
			};
			
			final LastNode node = new LastNode();
			final ITreeWalker walker;
			if (wholeTree) {
				walker = (walkWorld, walkPos, depth, isLeaves) -> {
					// harvest all non-leaves as we visit them
					if (!isLeaves) {
						harvestTreeBlock(caster, walkWorld, walkPos, tool);
					}
					return true;
					// Note: not setting finalPos;
				};
			} else {
				walker = (walkWorld, walkPos, depth, isLeaves) -> {
					// Take furthest away, based on depth.
					// Note it's depth first, so this may not be right?
					if (!isLeaves && depth > node.maxDepth) {
						node.maxDepth = depth;
						node.finalPos = walkPos;
					}
					return true;
				};
			}
			
			boolean walked = HarvestUtil.WalkTree(world, pos, walker);
			
			if (node.finalPos != null) {
				harvestTreeBlock(caster, world, node.finalPos, tool);
			}
			
			return walked;
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			final BlockPos pos = location.selectedBlockPos;
			if (location.world.isEmptyBlock(pos))
				return;
			
			if (DimensionUtils.IsSorceryDim(location.world)) {
				return;
			}
			
			BlockState state = location.world.getBlockState(pos);
			if (state == null || state.getMaterial().isLiquid())
				return;
			
			float hardness = state.getDestroySpeed(location.world, pos);
			
			if (hardness >= 100f || hardness < 0f)
				return; // unbreakable?
			
			ItemStack tool = null;
			if (level >= 3 && caster instanceof ServerPlayer) {
				tool = caster.getMainHandItem();
				if (!isTool((Player) caster, tool)) {
					tool = caster.getOffhandItem();
				}
				if (!isTool((Player) caster, tool)) {
					tool = null;
				}
			}
			
			final boolean spread = level >= 2;
			
			if (HarvestUtil.canHarvestCrop(state)) {
				if (harvestCrop(caster, location.world, pos, tool, spread)) {
					resultBuilder.applied = true;
					resultBuilder.affectedPos = new SpellLocation(location.world, pos);
				}
			} else if (HarvestUtil.canHarvestTree(state)) {
				if (harvestTree(caster, location.world, pos, tool, spread)) {
					resultBuilder.applied = true;
					resultBuilder.affectedPos = new SpellLocation(location.world, pos);
				}
			} else if (HarvestUtil.canHarvestCrop(location.world.getBlockState(pos.above()))) {
				if (harvestCrop(caster, location.world, pos.above(), tool, spread)) {
					resultBuilder.applied = true;
					resultBuilder.affectedPos = new SpellLocation(location.world, pos.above());
				}
			} else {
				;
			}
		}
		
		@Override
		public boolean affectsBlocks() {
			return true;
		}
		
		@Override
		public boolean affectsEntities() {
			return false;
		}
		
	}
	
	private static class ResetTargetEffect implements SpellEffect {
		
		private static final Component LABEL_TARGET_RESET_NAME = new TranslatableComponent("spelllog.nostrummagica.target_reset.name");
		private static final Component LABEL_TARGET_RESET_DESC = new TranslatableComponent("spelllog.nostrummagica.target_reset.desc");
		
		private final IEffectPredicate predicate;
		
		public ResetTargetEffect(IEffectPredicate predicate) {
			this.predicate = predicate;
		}

		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			if (this.predicate == null || this.predicate.test(caster, entity, eff)) {
				log.generalEffectStart(LABEL_TARGET_RESET_NAME, LABEL_TARGET_RESET_DESC, false);
				log.generalEffectFinish(0f, 0f);
				
				if (entity instanceof Mob) {
					((Mob) entity).setTarget(null);
					((Mob) entity).setLastHurtByMob(null);
				}
			}
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float eff, SpellActionResult resultBuilder,
				ISpellLogBuilder log) {
			; // do nothing
		}

		@Override
		public boolean affectsEntities() {
			return true;
		}

		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean isHarmful() {
			return true;
		}
	}
	
	private static class SwapStatusEffect implements SpellEffect {
		
		private static final Component LABEL_SWAP_STATUS_NAME = new TranslatableComponent("spelllog.nostrummagica.swap_status.name");
		private static final Component LABEL_SWAP_STATUS_DESC = new TranslatableComponent("spelllog.nostrummagica.swap_status.desc");
		
		private final IEffectPredicate predicate;
		
		public SwapStatusEffect(IEffectPredicate predicate) {
			this.predicate = predicate;
		}

		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			if (this.predicate == null || this.predicate.test(caster, entity, eff)) {
				log.generalEffectStart(LABEL_SWAP_STATUS_NAME, LABEL_SWAP_STATUS_DESC, false);
				log.generalEffectFinish(0f, 0f);
				
				if (caster != entity) {
					List<MobEffectInstance> casterClone = caster.getActiveEffects().stream().map(orig -> new MobEffectInstance(orig.getEffect(), orig.getDuration(), orig.getAmplifier())).collect(Collectors.toList());
					List<MobEffectInstance> targetClone = entity.getActiveEffects().stream().map(orig -> new MobEffectInstance(orig.getEffect(), orig.getDuration(), orig.getAmplifier())).collect(Collectors.toList());
					
					caster.removeAllEffects();
					entity.removeAllEffects();
					
					casterClone.forEach(inst -> entity.addEffect(inst));
					targetClone.forEach(inst -> caster.addEffect(inst));
				}
			}
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			; // Do nothing
		}

		@Override
		public boolean affectsEntities() {
			return true;
		}

		@Override
		public boolean affectsBlocks() {
			return false;
		}
	}
	
	private static class DropEquipmentEffect implements SpellEffect {
		
		private static final Component LABEL_DROP_EQUIPMENT_NAME = new TranslatableComponent("spelllog.nostrummagica.swap_status.name");
		private static final Component LABEL_DROP_EQUIPMENT_DESC = new TranslatableComponent("spelllog.nostrummagica.swap_status.desc");
		
		private final IEffectPredicate predicate;
		private final int pieces;
		
		public DropEquipmentEffect(IEffectPredicate predicate, int pieces) {
			this.predicate = predicate;
			this.pieces = pieces;
		}

		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			if (this.predicate == null || this.predicate.test(caster, entity, eff)) {
				log.generalEffectStart(LABEL_DROP_EQUIPMENT_NAME, LABEL_DROP_EQUIPMENT_DESC, true);
				log.generalEffectFinish(0f, 0f);
				
				List<EquipmentSlot> slots = Lists.newArrayList(EquipmentSlot.values());
				Collections.shuffle(slots);
				
				int left = this.pieces;
				for (EquipmentSlot slot : slots) {
					ItemStack held = entity.getItemBySlot(slot);
					if (!held.isEmpty()) {
						entity.spawnAtLocation(held);
						entity.setItemSlot(slot, ItemStack.EMPTY);
						left--;
						
						if (left <= 0) {
							break;
						}
					}
				}
			}
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			return;
		}

		@Override
		public boolean affectsEntities() {
			return true;
		}

		@Override
		public boolean affectsBlocks() {
			return false;
		}
		
		@Override
		public boolean isHarmful() {
			return true;
		}
		
	}
	
	private List<SpellEffect> effects;
	private BaseComponent name;
	private BaseComponent desc;
	
	public SpellAction() {
		effects = new ArrayList<>(2);
		this.name("unknown");
	}
	
	/**
	 * Applies the contained effect(s) on the provided entity at the given efficiency.
	 * Returns whether the entity was affected. 
	 * @param entity
	 * @param efficiency
	 * @return
	 */
	public SpellActionResult apply(LivingEntity source, LivingEntity entity, float efficiency, ISpellLogBuilder log) {
		if (entity.level.isClientSide)
			return SpellActionResult.FAIL;
		
		final LivingEntity ent = entity;
		SpellActionResult result = new SpellActionResult();
		
		SpellActionSummary summary = new SpellActionSummary(this, efficiency);
		NostrumMagica.playerListener.onMagicEffect(entity, source, summary);
		if (!summary.wasCancelled()) {
			for (SpellEffect e : effects) {
				final SpellEffect effect = e;
				
				if (!entity.getServer().isSameThread()) { // TODO I think?
					throw new RuntimeException("Wrong thread for spell effects!");
				}
				effect.apply(source, ent, summary.getEfficiency(), result, log);
			}
		}
		
		return result;
	}
	
	/**
	 * Apply the contained effect(s) at the provided location.
	 * Returns whether the location was affected
	 * @param location
	 * @param efficiency
	 * @return
	 */
	public SpellActionResult apply(LivingEntity source, SpellLocation location, float efficiency, ISpellLogBuilder log) {
		if (location.world.isClientSide)
			return SpellActionResult.FAIL;
		SpellActionResult result = new SpellActionResult();
		for (SpellEffect e : effects) {
			final SpellEffect effect = e;
			
			if (!location.world.getServer().isSameThread()) {
				throw new RuntimeException("Wrong thread for spell effects!");
			}
			
			//world.getMinecraftServer().runAsync(() -> {
				effect.apply(source, location, efficiency, result, log);
			//});
		}
		
		return result;
	}
	
	public SpellActionProperties getProperties() {
		return new SpellActionProperties(this);
	}
	
	public static final boolean isEnchantable(ItemStack stack) {
		Item item = stack.getItem();
		return !stack.isEmpty() && item instanceof IEnchantableItem && ((IEnchantableItem) item).canEnchant(stack);
	}
	
	public SpellAction damage(EMagicElement element, float amount) {
		effects.add(new DamageEffect(element, amount));
		return this;
	}
	
	public SpellAction heal(float amount) {
		effects.add(new HealEffect(amount));
		return this;
	}
	
	public SpellAction status(MobEffect effect, int duration, int amplitude) {
		effects.add(new StatusEffect(effect, duration, amplitude));
		return this;
	}
	
	public SpellAction status(MobEffect effect, int duration, IAmpProvider amplitude) {
		effects.add(new AmplifiedStatusEffect(effect, duration, amplitude));
		return this;
	}
	
	public SpellAction status(MobEffect effect, int duration, int amplitude, IOptionalEffectFilter filter) {
		effects.add(new OptionalStatusEffect(effect, duration, amplitude, filter));
		return this;
	}
	
	public SpellAction dispel(int number) {
		effects.add(new DispelEffect(number));
		return this;
	}
	
	public SpellAction blink(float maxDistance) {
		effects.add(new BlinkEffect(maxDistance));
		return this;
	}
	
	public SpellAction push(float radius, int level) {
		effects.add(new PushEffect(radius, level));
		return this;
	}
	
	public SpellAction pull(float radius, int level) {
		effects.add(new PushEffect(radius, -level));
		return this;
	}
	
	public SpellAction transmute(int level) {
		effects.add(new TransmuteEffect(level));
		return this;
	}
	
	public SpellAction burn(int durationTicks) {
		effects.add(new BurnEffect(durationTicks));
		return this;
	}
	
	public SpellAction summon(EMagicElement element, int power) {
		effects.add(new SummonEffect(element, power));
		return this;
	}
	
	public SpellAction lightning() {
		effects.add(new LightningEffect());
		return this;
	}
	
	public SpellAction burnArmor(int power) {
		effects.add(new BurnArmorEffect(power));
		return this;
	}
	
	public SpellAction healFood(int level) {
		effects.add(new HealFoodEffect(level));
		return this;
	}
	
	public SpellAction healMana(int level) {
		effects.add(new HealManaEffect(20 * level));
		return this;
	}
	
	public SpellAction swap() {
		effects.add(new SwapEffect());
		return this;
	}
	
	public SpellAction swapStatus(@Nullable IEffectPredicate predicate) {
		effects.add(new SwapStatusEffect(predicate));
		return this;
	}
	
	public SpellAction propel(int level) {
		effects.add(new PropelEffect(level));
		return this;
	}
	
	public SpellAction phase(int level) {
		effects.add(new PhaseEffect(level));
		return this;
	}
	
	public SpellAction enchant(EMagicElement element, int level) {
		effects.add(new EnchantEffect(element, level));
		return this;
	}
	
	public SpellAction grow(int level) {
		effects.add(new GrowEffect((int) Math.pow(2, level - 1)));
		return this;
	}
	
	public SpellAction wall(int level) {
//		Do different things in the block based on level.
//		Spawn three different types of it.
//		1) just regular block
//		2) Only players can go through it
//		3) Only the caster can go through it
		effects.add(new WallEffect(level));
		return this;
	}
	
//	public SpellAction geoblock(int level) {
//		effects.add(new GeoBlock(level));
//		return this;
//	}
	
	public SpellAction mysticWater(int level, int effectHealAmt, int effectDuration) {
		effects.add(new MysticWater(level, effectHealAmt, effectDuration));
		return this;
	}
	
	public SpellAction cursedFire(int level) {
		effects.add(new CursedFire(level));
		return this;
	}
	
//	public SpellAction infuse(EMagicElement element, int level) {
//		effects.add(new InfuseEffect(element, level));
//		return this;
//	}
	
	public SpellAction blockBreak(int level) {
		effects.add(new BreakEffect(level));
		return this;
	}
	
	public SpellAction harvest(int level) {
		effects.add(new HarvestEffect(level));
		return this;
	}
	
	public SpellAction resetTarget(@Nullable IEffectPredicate predicate) {
		effects.add(new ResetTargetEffect(predicate));
		return this;
	}
	
	public SpellAction dropEquipment(int level, @Nullable IEffectPredicate predicate) {
		final int pieces = (int) Math.pow(2, level-1); // 1, 2, 4
		effects.add(new DropEquipmentEffect(predicate, pieces));
		return this;
	}
	
	public SpellAction name(String key) {
		this.name = new TranslatableComponent("spelleffect." + key + ".name");
		this.desc = new TranslatableComponent("spelleffect." + key + ".desc");
		return this;
	}
	
	public BaseComponent getName() {
		return this.name;
	}
	
	public BaseComponent getDescription() {
		return this.desc;
	}
}
