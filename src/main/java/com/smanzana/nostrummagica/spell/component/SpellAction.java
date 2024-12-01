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
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolType;

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
			caster.setLastAttackedEntity(entity);
			entity.setRevengeTarget(caster);
			//entity.setHealth(Math.max(0f, entity.getHealth() - fin));
			entity.hurtResistantTime = 0;
			
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
		
		private static final ITextComponent LABEL_MOD_ICE_MASTER = new TranslationTextComponent("spelllogmod.nostrummagica.ice.master");
		
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
			
			if (entity.isEntityUndead()) {
				caster.setLastAttackedEntity(entity);
				entity.setRevengeTarget(caster);
				//entity.setHealth(Math.max(0f, entity.getHealth() - fin));
				
				log.damageStart(this.amount, EMagicElement.ICE); // trickery here: skill modifiers added globally so 'start' with real base
													// even though later we use the current 'base' modified amount
				final float fin = SpellDamage.DamageEntity(entity, EMagicElement.ICE, base, efficiency, caster, log);
				
				entity.hurtResistantTime = 0;
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
					if (wolf.isTamed() && wolf.getOwner() == caster) {
						wolf.addBond(1f);
					}
				}
				resultBuilder.heals += base * efficiency;
				log.healFinish(base * efficiency);
				
				if (attr != null && attr.hasSkill(NostrumSkills.Ice_Adept)) {
					if (NostrumMagica.rand.nextBoolean()) {
						entity.addPotionEffect(new EffectInstance(NostrumEffects.magicShield, (int)((20 * 15) * efficiency), 0));
					}
				}
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
		private static final ITextComponent LABEL_FOOD_NAME = new TranslationTextComponent("spelllog.nostrummagica.food.name");
		private static final ITextComponent LABEL_BREED_NAME = new TranslationTextComponent("spelllog.nostrummagica.breed.name");
		private static final ITextComponent LABEL_BREED_DESC = new TranslationTextComponent("spelllog.nostrummagica.breed.desc");
		
		private int amount;
		
		public HealFoodEffect(int amount) {
			this.amount = amount;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			
			if (entity instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) entity;
				player.getFoodStats().addStats((int) (amount * efficiency), 2);
				NostrumMagicaSounds.STATUS_BUFF2.play(entity);
				resultBuilder.applied |= true;
				log.generalEffectStart(LABEL_FOOD_NAME, new TranslationTextComponent("spelllog.nostrummagica.food.desc", "" + amount, "" + (int) (amount * efficiency)), false);
				log.generalEffectFinish(0f, 0f);
				return;
			} else if (entity instanceof AnimalEntity && caster != null && 
					caster instanceof PlayerEntity) {
				((AnimalEntity) entity)
					.setInLove((PlayerEntity) caster);
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
		private static final ITextComponent LABEL_MANA_NAME = new TranslationTextComponent("spelllog.nostrummagica.mana.name");
		
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
				log.generalEffectStart(LABEL_MANA_NAME, new TranslationTextComponent("spelllog.nostrummagica.mana.desc", "" + amount, "" + (int) (amount * efficiency)), false);
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
		protected final Effect effect;
		protected final int duration;
		protected final int amp;
		
		public StatusEffect(Effect effect, int duration, int amp) {
			this.effect = effect;
			this.duration = duration;
			this.amp = amp; 
		}
		
		@Override
		public boolean isHarmful() {
			return this.effect.getEffectType() == EffectType.HARMFUL;
		}
		
		protected @Nonnull EffectInstance makeEffect(LivingEntity caster, LivingEntity target, float efficiency, SpellActionResult resultBuilder) {
			return new EffectInstance(effect, (int) (duration * efficiency), amp);
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			entity.addPotionEffect(this.makeEffect(caster, entity, efficiency, resultBuilder));
			
			if (isHarmful()) {
				caster.setLastAttackedEntity(entity);
				entity.setRevengeTarget(caster);
				entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
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
		private static final ITextComponent LABEL_DISPEL_NAME = new TranslationTextComponent("spelllog.nostrummagica.dispel.name");
		
		private int number; // -1 to clear all
		
		public DispelEffect(int number) {
			this.number = number; 
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			log.generalEffectStart(LABEL_DISPEL_NAME, new TranslationTextComponent("spelllog.nostrummagica.dispel.desc", number), false);
			log.generalEffectFinish(0f, 0f);
			
			
			NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			
			if (number == -1 || entity.getActivePotionEffects().size() < number) {
				entity.clearActivePotions();
			} else {
				// Remove #number effects. We do this by getting another list of effects and shuffling, and then
				// just walking that list to remove from the real one
				List<EffectInstance> effectList = Lists.newArrayList(entity.getActivePotionEffects());
				Collections.shuffle(effectList);
				for (int i = 0; i < number; i++) {
					entity.removePotionEffect(effectList.get(i).getPotion());
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

		public AmplifiedStatusEffect(Effect effect, int duration, IAmpProvider amplifier) {
			super(effect, duration, 0);
			this.amplifierSupplier = amplifier;
		}
		
		@Override
		protected @Nonnull EffectInstance makeEffect(LivingEntity caster, LivingEntity target, float efficiency, SpellActionResult resultBuilder) {
			return new EffectInstance(effect, (int) (duration * efficiency), this.amplifierSupplier.getAmplifier(caster, target, efficiency));
		}
	}
	
	private static class OptionalStatusEffect extends StatusEffect {
		protected final IOptionalEffectFilter predicate;
		
		public OptionalStatusEffect(Effect effect, int duration, int amplifier, IOptionalEffectFilter predicate) {
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
		
		private static final ITextComponent LABEL_BLINK_NAME = new TranslationTextComponent("spelllog.nostrummagica.blink.name");
		private static final ITextComponent LABEL_BLINK_MOD_ENDERBELT = new TranslationTextComponent("spelllogmod.nostrummagica.enderbelt");
		private static final ITextComponent LABEL_BLINK_MOD_ENDERSET = new TranslationTextComponent("spelllogmod.nostrummagica.enderset");
		
		private float dist;
		
		public BlinkEffect(float dist) {
			this.dist = dist;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			log.pushModifierStack();
			
			boolean hasBelt = false;
			
			if (caster != null && caster instanceof PlayerEntity) {
				// Look for lightning belt
				IInventory baubles = NostrumMagica.instance.curios.getCurios((PlayerEntity) caster);
				if (baubles != null) {
					for (int i = 0; i < baubles.getSizeInventory(); i++) {
						ItemStack stack = baubles.getStackInSlot(i);
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
			Vector3d direction = entity.getLookVec().normalize();
			Vector3d source = entity.getPositionVec();
			source = source.add(0, entity.getEyeHeight(), 0);
			Vector3d dest = SpellTeleportation.Blink(entity, source, direction, dist, hasBelt && entity.isSneaking());			
			
			if (dest != null) {
				// We are about to put feet at dest, so try to shift down so that eye position matches
				if (entity.world.isAirBlock(new BlockPos(dest.subtract(0, 1.5, 0)))) {
					dest = dest.subtract(0, 1.5, 0);
				}
				
				NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(entity, .5 + Math.floor(dest.x), Math.floor(dest.y), .5 + Math.floor(dest.z), caster);
				if (!event.isCanceled()) {
					entity.setPositionAndUpdate(event.getTargetX(), event.getTargetY(), event.getTargetZ());
					entity.fallDistance = 0;
					NostrumMagicaSounds.STATUS_BUFF1.play(entity);
					NostrumMagica.fireTeleprotedOtherEvent(event.getEntity(), caster, event.getPrev(), event.getTarget());
					resultBuilder.applied |= true;
				}
			}
			
			log.generalEffectStart(LABEL_BLINK_NAME, new TranslationTextComponent("spelllog.nostrummagica.blink.desc",
					String.format("%.1f", this.dist), String.format("%.1f", dist), String.format("%.1f", source.distanceTo(entity.getPositionVec().add(0, entity.getEyeHeight(), 0)))), false);
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

		private static final ITextComponent LABEL_PUSH_NAME = new TranslationTextComponent("spelllog.nostrummagica.push.name");
		private static final ITextComponent LABEL_PULL_NAME = new TranslationTextComponent("spelllog.nostrummagica.pull.name");
		
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
			final ITextComponent desc = (amp < 0
					? new TranslationTextComponent("spelllog.nostrummagica.pull.desc", String.format("%.1f", this.range))
					: new TranslationTextComponent("spelllog.nostrummagica.push.desc", String.format("%.1f", this.range)));
			log.generalEffectStart(amp < 0 ? LABEL_PULL_NAME : LABEL_PUSH_NAME, desc, false);
			log.generalEffectFinish(0f, 0f);

			// We abs the amp here, but check it belwo for pull and negate vector
			float magnitude = .35f * (Math.abs(amp) + 1.0f) * (float) Math.min(2.0f, Math.max(0.0f, 1.0f + Math.log(efficiency)));
			Vector3d center = Vector3d.copyCentered(location.hitBlockPos); // idr why I wanted it centered in the block?
			NostrumMagicaSounds.DAMAGE_WIND.play(location.world, center.x, center.y, center.z);
			
			boolean any = false;
			for (Entity e : location.world.getEntitiesWithinAABBExcludingEntity(null, 
					new AxisAlignedBB(center.x - range, center.y - range, center.z - range, center.x + range, center.y + range, center.z + range)
					)) {
				double dist = e.getPositionVec().distanceTo(center); 
				if (dist <= range) {
					
					// If push, straight magnitude
					// If pull, cap magnitude so that it doesn't fly past player
					
					Vector3d force;
					Vector3d direction = e.getPositionVec().add(0, e.getEyeHeight(), 0).subtract(center).normalize();
					force = new Vector3d(
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
							force = new Vector3d(
									force.x * mod,
									force.y * mod,
									force.z * mod
									);
						}

						force = new Vector3d(
								force.x * -1.0,
								force.y * -1.0,
								force.z * -1.0);
					}
					
					e.addVelocity(force.x, force.y, force.z);
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
		
		private static final ITextComponent LABEL_TRANSMUTE_NAME = new TranslationTextComponent("spelllog.nostrummagica.transmute.name");
		
		private int level;
		
		public TransmuteEffect(int level) {
			this.level = level;
		}
		
		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			ItemStack inhand = entity.getHeldItemMainhand();
			boolean offhand = false;
			if (inhand.isEmpty() || inhand.getItem() instanceof SpellScroll) {
				inhand = entity.getHeldItemOffhand();
				offhand = true;
			}
			
			if (inhand.isEmpty())
				return;
			
			Item item = inhand.getItem();
			TransmuteResult<Item> result = Transmutation.GetTransmutationResult(item, level);
			
			if (!result.valid) {
				NostrumMagicaSounds.CAST_FAIL.play(entity);
				log.generalEffectStart(LABEL_TRANSMUTE_NAME, new TranslationTextComponent("spelllog.nostrummagica.transmute_fail.desc", item.getName()), false);
				log.generalEffectFinish(0f, 0f);
				return;
			}

			log.generalEffectStart(LABEL_TRANSMUTE_NAME, new TranslationTextComponent("spelllog.nostrummagica.transmute.desc", item.getName(), result.output.getName()), false);
			log.generalEffectFinish(0f, 0f);
			
			ItemStack stack = new ItemStack(result.output);
			NostrumMagicaSounds.CAST_CONTINUE.play(entity);
			
			// Award knowledge
			INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			attr.giveTransmuteKnowledge(result.source.getName(), level);
			if (caster instanceof ServerPlayerEntity) {
				NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) caster);
			}
			
			if (entity instanceof PlayerEntity) {
				PlayerEntity p = (PlayerEntity) entity;
				if (inhand.getCount() == 1) {
					if (offhand) {
						p.inventory.removeStackFromSlot(40);
					} else {
						p.inventory.removeStackFromSlot(p.inventory.currentItem);
					}
					((PlayerEntity) entity).inventory.addItemStackToInventory(stack);
				} else {
					inhand.split(1);
					((PlayerEntity) entity).inventory.addItemStackToInventory(stack);
				}
			} else {
				// MobEntity has held item in slot 0
				entity.setHeldItem(Hand.MAIN_HAND, stack);
			}

			resultBuilder.applied |= true;
		}
		
		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			Block block = location.world.getBlockState(location.selectedBlockPos).getBlock();
			TransmuteResult<Block> result = Transmutation.GetTransmutationResult(block, level);
			if (!result.valid) {
				NostrumMagicaSounds.CAST_FAIL.play(location.world, location.selectedBlockPos.getX() + .5, location.selectedBlockPos.getY(), location.selectedBlockPos.getZ() + .5);
				log.generalEffectStart(LABEL_TRANSMUTE_NAME, new TranslationTextComponent("spelllog.nostrummagica.transmute_fail.desc", block.getTranslatedName()), false);
				log.generalEffectFinish(0f, 0f);
				return;
			}
			
			log.generalEffectStart(LABEL_TRANSMUTE_NAME, new TranslationTextComponent("spelllog.nostrummagica.transmute.desc", block.getTranslatedName(), result.output.getTranslatedName()), false);
			log.generalEffectFinish(0f, 0f);
			
			NostrumMagicaSounds.CAST_CONTINUE.play(location.world, location.selectedBlockPos.getX() + .5, location.selectedBlockPos.getY(), location.selectedBlockPos.getZ() + .5);

			// Award knowledge
			INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			attr.giveTransmuteKnowledge(result.source.getName(), level);
			
			if (caster instanceof ServerPlayerEntity) {
				NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) caster);
			}
			
			location.world.setBlockState(location.selectedBlockPos, result.output.getDefaultState());
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
		
		private static final ITextComponent LABEL_BURN_NAME = new TranslationTextComponent("spelllog.nostrummagica.burn.name");

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
			
			caster.setLastAttackedEntity(entity);
			entity.setRevengeTarget(caster);
			entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
			entity.hurtResistantTime = 0;
			
			entity.setFire((int) Math.ceil((float) duration / 20.0f));
			
			@Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			if (attr != null && attr.hasSkill(NostrumSkills.Fire_Inflict)) {
				// Also bust shields
				entity.removePotionEffect(NostrumEffects.mysticWater);
				entity.removePotionEffect(NostrumEffects.physicalShield);
				entity.removePotionEffect(NostrumEffects.magicShield);
				entity.removePotionEffect(Effects.ABSORPTION);
			}
			
			resultBuilder.applied |= true;
			
			log.generalEffectStart(LABEL_BURN_NAME,
					new TranslationTextComponent("spelllog.nostrummagica.burn_ent.desc", String.format("%.1f", (float) this.duration / 20f), "" + (int) Math.ceil((float) duration / 20.0f)), true);
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
			
			if (location.world.isAirBlock(applyPos)) {
				location.world.setBlockState(applyPos, Blocks.FIRE.getDefaultState());
				NostrumMagicaSounds.DAMAGE_FIRE.play(location.world,
						applyPos.getX() + .5, applyPos.getY(), applyPos.getZ() + .5);
				resultBuilder.applied |= true;
				resultBuilder.affectedPos = new SpellLocation(location.world, applyPos);
				
				log.generalEffectStart(LABEL_BURN_NAME,
						new TranslationTextComponent("spelllog.nostrummagica.burn.desc"), true);
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
		
		private static final ITextComponent LABEL_LIGHTNING_NAME = new TranslationTextComponent("spelllog.nostrummagica.lightning.name");
		private static final ITextComponent LABEL_LIGHTNING_MOD_BELT = new TranslationTextComponent("spelllogmod.nostrummagica.lightningbelt");
		
		public LightningEffect() {
			
		}
		
		@Override
		public void applyEffect(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			entity.setRevengeTarget(caster);
			entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
			entity.hurtResistantTime = 0;
			apply(caster, new SpellLocation(entity), efficiency, resultBuilder, log);
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			int count = 1;
			
			log.pushModifierStack();
			
			if (caster != null && caster instanceof PlayerEntity) {
				// Look for lightning belt
				IInventory baubles = NostrumMagica.instance.curios.getCurios((PlayerEntity) caster);
				if (baubles != null) {
					for (int i = 0; i < baubles.getSizeInventory(); i++) {
						ItemStack stack = baubles.getStackInSlot(i);
						if (stack.isEmpty() || stack.getItem() != NostrumCurios.lightningBelt) {
							continue;
						}
						
						count = caster.getRNG().nextInt(3) + 3;
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
					new TranslationTextComponent("spelllog.nostrummagica.lightning.desc", "" + count, String.format("%.1f", damage)), true);
			log.generalEffectFinish(damage * count, 0f);
			
			final BlockPos applyPos = location.hitBlockPos;
			BlockPos.Mutable cursor = new BlockPos.Mutable().setPos(applyPos);
			Random rand = (caster == null ? new Random() : caster.getRNG());
			for (int i = 0; i < count; i++) {
				
				if (i == 0) {
					; // Don't adjust pos
				} else {
					// Apply random x/z offsets. Then step up to 4 to find surface
					cursor.setPos(
							applyPos.getX() + rand.nextInt(6) - 3,
							applyPos.getY() - 2,
							applyPos.getZ() + rand.nextInt(6) - 3);
					
					// Find surface
					int dist = 0;
					while (dist++ < 4 && !location.world.isAirBlock(cursor)) {
						cursor.setY(cursor.getY() + 1);
					}
				}
				
				TameLightning bolt = new TameLightning(NostrumEntityTypes.tameLightning, location.world, cursor.getX() + 0.5, cursor.getY(), cursor.getZ() + 0.5);
				bolt.setEntityToIgnore(caster);
				bolt.setDamage(damage);
				
				((ServerWorld) location.world).addEntity(bolt);
				
				
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
			World world = caster.getEntityWorld();
			BlockPos center = caster.getPosition();
			BlockPos pos;
			do {
				pos = center.add(1, 1, 0);
				if (world.isAirBlock(pos))
					break;
				
				pos = center.add(-1, 1, 0);
				if (world.isAirBlock(pos))
					break;
				
				pos = center.add(0, 1, -1);
				if (world.isAirBlock(pos))
					break;
				
				pos = center.add(0, 1, 1);
				if (world.isAirBlock(pos))
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
			if (caster instanceof PlayerEntity) {
				if (NostrumMagica.getMagicWrapper(caster) == null) {
					return;
				}
				
				NostrumMagica.getMagicWrapper(caster).clearFamiliars();
				caster.removeActivePotionEffect(NostrumEffects.familiar);
				for (int i = 0; i < power; i++) {
					MagicGolemEntity golem = spawnGolem(location.world);
					golem.setPosition(applyPos.getX() + .5, applyPos.getY(), applyPos.getZ() + .5);
					location.world.addEntity(golem);
					golem.setOwnerId(caster.getUniqueID());
					NostrumMagica.getMagicWrapper(caster).addFamiliar(golem);
				}
				int baseTime = (int) (20 * 60 * 2.5 * Math.pow(2, Math.max(0, power - 1)));
				int time = (int) (baseTime * efficiency);
				caster.addPotionEffect(new EffectInstance(NostrumEffects.familiar, time, 0) {
					@Override
					public boolean tick(LivingEntity entityIn, Runnable onComplete) {
						// heh snekky
						boolean ret = super.tick(entityIn, onComplete);
						if (ret) {
							// we're not being removed. Check familiars
							if (entityIn.world.isRemote) {
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
				
				final ITextComponent LABEL_SUMMON_NAME = new TranslationTextComponent("spelllog.nostrummagica.summon.name", this.element.getName());
				final ITextComponent LABEL_SUMMON_DESC = new TranslationTextComponent("spelllog.nostrummagica.summon.desc", this.element.getName(), "" + power, "" + ((float) (baseTime) / 20f), "" + ((float) time / 20f));
				log.generalEffectStart(LABEL_SUMMON_NAME, LABEL_SUMMON_DESC, false);
				log.generalEffectFinish(0f, 0f);
			} else {
				// Just summon some new golems
				final int time = (int) (20f * (15f * efficiency));
				for (int i = 0; i < power; i++) {
					MagicGolemEntity golem = spawnGolem(location.world);
					golem.setPosition(applyPos.getX() + .5, applyPos.getY(), applyPos.getZ() + .5);
					golem.setExpiresAfterTicks(time);
					location.world.addEntity(golem);
				}
			}
			
			NostrumMagicaSounds.CAST_CONTINUE.play(location.world,
					applyPos.getX() + .5, applyPos.getY(), applyPos.getZ() + .5);
			resultBuilder.applied |= true;
			resultBuilder.affectedPos = new SpellLocation(location.world, applyPos);
		}
		
		private MagicGolemEntity spawnGolem(World world) {
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
			
			Vector3d pos = caster.getPositionVec();
			float pitch = caster.rotationPitch;
			float yaw = caster.rotationYawHead;
			
			NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(caster, entity.getPosX(), entity.getPosY(), entity.getPosZ(), caster);
			if (!event.isCanceled()) {
				if (caster instanceof PlayerEntity) {
					caster.setPositionAndRotation(caster.getPosX(), caster.getPosY(), caster.getPosZ(), entity.rotationYawHead, entity.rotationPitch);
					caster.setPositionAndUpdate(
							event.getTargetX(), event.getTargetY(), event.getTargetZ());
				} else {
					caster.setPositionAndRotation(
							event.getTargetX(), event.getTargetY(), event.getTargetZ(),
							entity.rotationPitch, entity.rotationYawHead
							);
				}
				NostrumMagica.fireTeleprotedOtherEvent(event.getEntity(), caster, event.getPrev(), event.getTarget());
			}
			
			event = NostrumMagica.fireTeleportAttemptEvent(entity, pos.x, pos.y, pos.z, caster);
			if (!event.isCanceled()) {
				if (entity instanceof PlayerEntity) {
					entity.setPositionAndRotation(entity.getPosX(), entity.getPosY(), entity.getPosZ(), yaw, pitch);
					entity.setPositionAndUpdate(event.getTargetX(), event.getTargetY(), event.getTargetZ());
				} else {
					entity.setPositionAndRotation(event.getTargetX(), event.getTargetY(), event.getTargetZ(), yaw, pitch);
				}
				NostrumMagica.fireTeleprotedOtherEvent(event.getEntity(), caster, event.getPrev(), event.getTarget());
			}
			
			entity.fallDistance = 0;
			caster.fallDistance = 0;
			resultBuilder.applied |= true;			
		}
		
		protected BlockPos adjustPosition(World world, BlockPos pos) {
			// Do one graceful positioning attempt. If block above is not air but there's room to shift down
			// one, do so
			if (!world.isAirBlock(pos.up()) && world.isAirBlock(pos.down())) {
				return pos.down();
			}
			
			return pos;
		}
		
		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			BlockPos pos = adjustPosition(location.world, location.hitBlockPos);
			
			NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(caster, pos.getX() + .5, pos.getY(), pos.getZ() + .5, caster);
			if (!event.isCanceled()) {
				caster.setPositionAndUpdate(event.getTargetX(), event.getTargetY(), event.getTargetZ());
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

			Vector3d force = entity.getLookVec().add(0, 0.15, 0).normalize();
			float scale = 1f * (.5f * (level + 1)) * efficiency;
			
			if (entity.isElytraFlying()) {
				scale *= .5f;
			}
			
			force = new Vector3d(force.x * scale, force.y * scale, force.z * scale);
			
			entity.setMotion(entity.getMotion().add(force.x, force.y, force.z));
			entity.velocityChanged = true;
			
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
			if (caster != entity && entity instanceof MobEntity) {
				// Make sure they want to attack you if you do it
				entity.setRevengeTarget(caster);
				entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
				entity.hurtResistantTime = 0;
			}
			
			double radius = (16 + (32.0 * level)) * efficiency;
			
			if (caster != null && caster instanceof PlayerEntity) {
				// Look for ender belt
				IInventory baubles = NostrumMagica.instance.curios.getCurios((PlayerEntity) caster);
				if (baubles != null) {
					for (int i = 0; i < baubles.getSizeInventory(); i++) {
						ItemStack stack = baubles.getStackInSlot(i);
						if (stack.isEmpty() || stack.getItem() != NostrumCurios.enderBelt) {
							continue;
						}
						
						radius *= 2.0;
						break;
					}
				}
			}
			
			if (ElementalArmor.GetSetCount(entity, EMagicElement.ENDER, ElementalArmor.Type.MASTER) == 4) {
				// has full ender set
				radius *= 2.0;
			}
			
			NostrumMagicaSounds.DAMAGE_ENDER.play(entity);
			
			for (int i = 0; i < 20; i++) {
			
				// Find a random place to teleport
		        double x = entity.getPosX() + (NostrumMagica.rand.nextDouble() - 0.5D) * radius;
		        double y = entity.getPosY() + (double)(NostrumMagica.rand.nextInt((int) radius) - (int) radius / 2.0);
		        double z = entity.getPosZ() + (NostrumMagica.rand.nextDouble() - 0.5D) * radius;
	
			    // Try to teleport
		        NostrumTeleportEvent event = NostrumMagica.fireTeleportAttemptEvent(entity, x, y, z, caster);
				if (event.isCanceled()) {
					// Break on a single cancel
					break;
				} else {
					if (entity.attemptTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), false)) {
						NostrumMagica.fireTeleprotedOtherEvent(event.getEntity(), caster, event.getPrev(), event.getTarget());
						break;
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
				
				entity.setPosition(location.hitPosition.x, location.hitPosition.y + .1, location.hitPosition.z);
				
				location.world.addEntity(entity);
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
			if (!(caster instanceof PlayerEntity)) {
				int count = level + 1;
				double amt = 2 + level;
				caster.removeActivePotionEffect(NostrumEffects.magicBuff);
				NostrumMagica.magicEffectProxy.applyMagicBuff(entity, element, amt, count);
				entity.addPotionEffect(new EffectInstance(NostrumEffects.magicBuff, 60 * 20, 0));
				resultBuilder.applied |= true;
			}
			
			ItemStack inhand = entity.getHeldItemMainhand();
			boolean offhand = false;
			if (!isEnchantable(inhand)) {
				inhand = entity.getHeldItemOffhand();
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
				if (entity instanceof PlayerEntity) {
					PlayerEntity p = (PlayerEntity) entity;
					
					if (consumeInput) {
						if (inhand.getCount() == 1) {
							if (offhand) {
								p.inventory.removeStackFromSlot(40);
							} else {
								p.inventory.removeStackFromSlot(p.inventory.currentItem);
							}
						} else {
							inhand.split(1);
						}
					}
					if (!addedItem.isEmpty()) {
						((PlayerEntity) entity).inventory.addItemStackToInventory(addedItem);
					}
					
					
				} else {
					// MobEntity has held item in slot 0
					if (!addedItem.isEmpty()) {
						entity.setHeldItem(Hand.MAIN_HAND, addedItem);
					}
				}
				NostrumMagicaSounds.CAST_CONTINUE.play(entity);
			}
			
			// Apply enchant effect
			entity.addPotionEffect(new EffectInstance(ElementalEnchantEffect.GetForElement(this.element), (int) (20 * 15 * efficiency), level-1));
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
			if (entity instanceof AnimalEntity) {
				AnimalEntity animal = (AnimalEntity) entity;
				animal.addGrowth((int) (count * 500 * efficiency));
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
			
			if (location.world.getBlockState(pos.up()).getBlock() instanceof FarmlandBlock) {
				pos = pos.up();
			}
			
			if (location.world.getBlockState(pos).getBlock() instanceof FarmlandBlock) {
				pos = pos.up();
			}
			
			if (location.world.isAirBlock(pos)) {
				return;
			}
			
			ItemStack junk = new ItemStack(Items.BONE_MEAL, 10); // each apply call may reduce count by 1
			boolean worked = false;
			for (int i = 0; i < count; i++) {
				if (caster instanceof PlayerEntity) {
					if (!BoneMealItem.applyBonemeal(junk, location.world, pos, (PlayerEntity)caster)) {
						break;
					}
				} else {
					if (!BoneMealItem.applyBonemeal(junk, location.world, pos)) {
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
			for (ItemStack equip : entity.getArmorInventoryList()) {
				if (equip.isEmpty())
					continue;
				
				count++;
			}
			if (count != 0) {
				for (ItemStack equip : entity.getArmorInventoryList()) {
					if (equip.isEmpty() || !(equip.getItem() instanceof ArmorItem))
						continue;
					ItemStacks.damageEquippedArmor(equip, entity, ((ArmorItem) equip.getItem()).getEquipmentSlot(), amount/count);
				}
			}
			
			NostrumMagicaSounds.MELT_METAL.play(entity);
			caster.setLastAttackedEntity(entity);
			entity.setRevengeTarget(caster);
			entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
			entity.hurtResistantTime = 0;
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
			entity.addPotionEffect(new EffectInstance(NostrumEffects.mysticAir, duration, amp));
			resultBuilder.applied |= true;
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			BlockPos pos = location.hitBlockPos;
			if (!location.world.isAirBlock(pos) && !(location.world.getBlockState(pos).getBlock() instanceof MagicWallBlock)) {
				NostrumMagicaSounds.CAST_FAIL.play(location.world, pos);
			} else {
				NostrumMagicaSounds.DAMAGE_WIND.play(location.world, pos);
				location.world.setBlockState(pos, NostrumBlocks.magicWall.getState(level));
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
			
			entity.addPotionEffect(new EffectInstance(NostrumEffects.mysticWater, (int) (effectDuration * efficiency), (int) amp));
			NostrumMagicaSounds.STATUS_BUFF1.play(entity);
			resultBuilder.applied |= true;
			
			if (attr != null && attr.hasSkill(NostrumSkills.Ice_Adept)) {
				if (NostrumMagica.rand.nextBoolean()) {
					entity.addPotionEffect(new EffectInstance(NostrumEffects.magicShield, (int)((20 * 15) * efficiency), 0));
				}
			}
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			BlockPos pos = location.hitBlockPos;
			if (location.world.isAirBlock(pos) || location.world.getBlockState(pos).getBlock() instanceof MysticWaterBlock) {
				location.world.setBlockState(pos, NostrumBlocks.mysticWaterBlock.getStateWithPower(this.waterLevel));
				NostrumMagicaSounds.DAMAGE_ICE.play(location.world, pos);
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

	private static class CursedFire implements SpellEffect {
		
		private int level;
		
		public CursedFire(int level) {
			this.level = level;
		}

		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			@Nullable EffectInstance instance = entity.getActivePotionEffect(NostrumEffects.cursedFire);
			final int duration = 20 * 1200;
			if (instance == null || instance.getDuration() < (int) (duration * .8f)) {
				entity.addPotionEffect(new EffectInstance(
						NostrumEffects.cursedFire,
						duration,
						0
						));
			}
			caster.setLastAttackedEntity(entity);
			entity.setRevengeTarget(caster);
			entity.attackEntityFrom(DamageSource.causeMobDamage(caster), 0);
			resultBuilder.applied |= true;
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float efficiency, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			final BlockState state = NostrumBlocks.cursedFire.GetWithLevel(level);
			final BlockPos pos = location.hitBlockPos;
			if (location.world.getBlockState(pos).getMaterial().isReplaceable() && state.isValidPosition(location.world, pos) && location.world.setBlockState(pos, state)) {
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
			apply(caster, new SpellLocation(entity.world, entity.getPosition().add(0, -1, 0)), eff, resultBuilder, log);
		}
		
		protected boolean isTool(@Nullable PlayerEntity player, ItemStack stack) {
			if (stack.isEmpty()) {
				return false;
			}
			
			Set<ToolType> classes = stack.getItem().getToolTypes(stack);
			for (ToolType cla : classes) {
				// Required harvest level >= iron so throw-away levels like wood and stone don't count
				if (stack.getItem().getHarvestLevel(stack, cla, player, null) >= 2) {
					return true;
				}
			}
			
			return false;
		}

		@Override
		public void apply(LivingEntity caster, SpellLocation location, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			final BlockPos pos = location.selectedBlockPos;
			if (location.world.isAirBlock(pos))
				return;
			
			if (DimensionUtils.IsSorceryDim(location.world)) {
				return;
			}
			
			BlockState state = location.world.getBlockState(pos);
			if (state == null || state.getMaterial().isLiquid())
				return;
			
			boolean onlyStone = (level <= 1);
			if (onlyStone && caster instanceof PlayerEntity) {
				if (!Tags.Blocks.STONE.contains(state.getBlock())) {
					return;
				}
			}
			
			boolean usePickaxe = (level >= 3);
			float hardness = state.getBlockHardness(location.world, pos);
			
			if (hardness >= 100f || hardness < 0f)
				return;
			
			if (caster instanceof ServerPlayerEntity) {
				// This checks item harvest level >:(
//				if (!state.getBlock().canHarvestBlock(location.world, block, (PlayerEntity) caster)) {
//					return false;
//				}
				
				if (usePickaxe) {
					// Check if they have a pickaxe
					ItemStack inHand = caster.getHeldItemMainhand();
					if (!isTool((PlayerEntity) caster, inHand)) {
						inHand = caster.getHeldItemOffhand();
					}
					if (!isTool((PlayerEntity) caster, inHand)) {
						usePickaxe = false;
					}
				}
				
				if (usePickaxe) {
					((ServerPlayerEntity) caster).interactionManager.tryHarvestBlock(pos);
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
				entity.addPotionEffect(new EffectInstance(NostrumEffects.lootLuck, (int) (15 * eff) * 20, level-1));
				resultBuilder.applied = true;
			} else {
				apply(caster, new SpellLocation(entity.world, entity.getPosition().add(0, -1, 0)), eff, resultBuilder, log);
			}
		}
		
		protected boolean isTool(@Nullable PlayerEntity player, ItemStack stack) {
			if (stack.isEmpty()) {
				return false;
			}
			
			Set<ToolType> classes = stack.getItem().getToolTypes(stack);
			for (ToolType cla : classes) {
				// Required harvest level >= iron so throw-away levels like wood and stone don't count
				if (cla == ToolType.AXE
						&& stack.getItem().getHarvestLevel(stack, cla, player, null) >= 2) {
					return true;
				}
			}
			
			return false;
		}
		
		protected boolean harvestCropBlock(LivingEntity caster, World world, BlockPos block, @Nullable ItemStack tool) {
			return HarvestUtil.HarvestCrop(world, block);
		}
		
		protected boolean harvestCrop(LivingEntity caster, World world, BlockPos block, @Nullable ItemStack tool, Set<BlockPos> visitted) {
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
		
		protected boolean harvestCrop(LivingEntity caster, World world, BlockPos block, @Nullable ItemStack tool, boolean doNearby) {
			if (doNearby) {
				return harvestCrop(caster, world, block, tool, new HashSet<>());
			} else {
				return harvestCropBlock(caster, world, block, tool);
			}
		}
		
		protected boolean harvestTreeBlock(LivingEntity caster, World world, BlockPos pos, @Nullable ItemStack tool) {
			if (tool != null) {
				((ServerPlayerEntity) caster).interactionManager.tryHarvestBlock(pos);
			} else {
				world.destroyBlock(pos, true);
			}
			return true;
		}
		
		protected boolean harvestTree(LivingEntity caster, World world, BlockPos pos, @Nullable ItemStack tool, boolean wholeTree) {
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
			if (location.world.isAirBlock(pos))
				return;
			
			if (DimensionUtils.IsSorceryDim(location.world)) {
				return;
			}
			
			BlockState state = location.world.getBlockState(pos);
			if (state == null || state.getMaterial().isLiquid())
				return;
			
			float hardness = state.getBlockHardness(location.world, pos);
			
			if (hardness >= 100f || hardness < 0f)
				return; // unbreakable?
			
			ItemStack tool = null;
			if (level >= 3 && caster instanceof ServerPlayerEntity) {
				tool = caster.getHeldItemMainhand();
				if (!isTool((PlayerEntity) caster, tool)) {
					tool = caster.getHeldItemOffhand();
				}
				if (!isTool((PlayerEntity) caster, tool)) {
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
			} else if (HarvestUtil.canHarvestCrop(location.world.getBlockState(pos.up()))) {
				if (harvestCrop(caster, location.world, pos.up(), tool, spread)) {
					resultBuilder.applied = true;
					resultBuilder.affectedPos = new SpellLocation(location.world, pos.up());
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
		
		private static final ITextComponent LABEL_TARGET_RESET_NAME = new TranslationTextComponent("spelllog.nostrummagica.target_reset.name");
		private static final ITextComponent LABEL_TARGET_RESET_DESC = new TranslationTextComponent("spelllog.nostrummagica.target_reset.desc");
		
		private final IEffectPredicate predicate;
		
		public ResetTargetEffect(IEffectPredicate predicate) {
			this.predicate = predicate;
		}

		@Override
		public void apply(LivingEntity caster, LivingEntity entity, float eff, SpellActionResult resultBuilder, ISpellLogBuilder log) {
			if (this.predicate == null || this.predicate.test(caster, entity, eff)) {
				log.generalEffectStart(LABEL_TARGET_RESET_NAME, LABEL_TARGET_RESET_DESC, false);
				log.generalEffectFinish(0f, 0f);
				
				if (entity instanceof MobEntity) {
					((MobEntity) entity).setAttackTarget(null);
					((MobEntity) entity).setRevengeTarget(null);
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
		
		private static final ITextComponent LABEL_SWAP_STATUS_NAME = new TranslationTextComponent("spelllog.nostrummagica.swap_status.name");
		private static final ITextComponent LABEL_SWAP_STATUS_DESC = new TranslationTextComponent("spelllog.nostrummagica.swap_status.desc");
		
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
					List<EffectInstance> casterClone = caster.getActivePotionEffects().stream().map(orig -> new EffectInstance(orig.getPotion(), orig.getDuration(), orig.getAmplifier())).collect(Collectors.toList());
					List<EffectInstance> targetClone = entity.getActivePotionEffects().stream().map(orig -> new EffectInstance(orig.getPotion(), orig.getDuration(), orig.getAmplifier())).collect(Collectors.toList());
					
					caster.clearActivePotions();
					entity.clearActivePotions();
					
					casterClone.forEach(inst -> entity.addPotionEffect(inst));
					targetClone.forEach(inst -> caster.addPotionEffect(inst));
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
		
		private static final ITextComponent LABEL_DROP_EQUIPMENT_NAME = new TranslationTextComponent("spelllog.nostrummagica.swap_status.name");
		private static final ITextComponent LABEL_DROP_EQUIPMENT_DESC = new TranslationTextComponent("spelllog.nostrummagica.swap_status.desc");
		
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
				
				List<EquipmentSlotType> slots = Lists.newArrayList(EquipmentSlotType.values());
				Collections.shuffle(slots);
				
				int left = this.pieces;
				for (EquipmentSlotType slot : slots) {
					ItemStack held = entity.getItemStackFromSlot(slot);
					if (!held.isEmpty()) {
						entity.entityDropItem(held);
						entity.setItemStackToSlot(slot, ItemStack.EMPTY);
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
	private TextComponent name;
	private TextComponent desc;
	
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
		if (entity.world.isRemote)
			return SpellActionResult.FAIL;
		
		final LivingEntity ent = entity;
		SpellActionResult result = new SpellActionResult();
		
		SpellActionSummary summary = new SpellActionSummary(this, efficiency);
		NostrumMagica.playerListener.onMagicEffect(entity, source, summary);
		if (!summary.wasCancelled()) {
			for (SpellEffect e : effects) {
				final SpellEffect effect = e;
				
				if (!entity.getServer().isOnExecutionThread()) { // TODO I think?
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
		if (location.world.isRemote)
			return SpellActionResult.FAIL;
		SpellActionResult result = new SpellActionResult();
		for (SpellEffect e : effects) {
			final SpellEffect effect = e;
			
			if (!location.world.getServer().isOnExecutionThread()) {
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
	
	public SpellAction status(Effect effect, int duration, int amplitude) {
		effects.add(new StatusEffect(effect, duration, amplitude));
		return this;
	}
	
	public SpellAction status(Effect effect, int duration, IAmpProvider amplitude) {
		effects.add(new AmplifiedStatusEffect(effect, duration, amplitude));
		return this;
	}
	
	public SpellAction status(Effect effect, int duration, int amplitude, IOptionalEffectFilter filter) {
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
		this.name = new TranslationTextComponent("spelleffect." + key + ".name");
		this.desc = new TranslationTextComponent("spelleffect." + key + ".desc");
		return this;
	}
	
	public TextComponent getName() {
		return this.name;
	}
	
	public TextComponent getDescription() {
		return this.desc;
	}
}
