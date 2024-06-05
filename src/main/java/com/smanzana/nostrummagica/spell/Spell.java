package com.smanzana.nostrummagica.spell;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.effect.ElementalSpellBoostEffect;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellAction.SpellActionResult;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape.SpellShapeInstance;
import com.smanzana.nostrummagica.stat.PlayerStat;
import com.smanzana.nostrummagica.stat.PlayerStatTracker;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * A collection of spell shapes and effects.
 * Shapes all are resolved first, after which effects are applied.
 * If shapes affect multiple people, effects are applied to all.
 * @author Skyler
 *
 */
public class Spell {
	
	public static class SpellState {
		private final Spell spell;
		private final float efficiency;
		private final LivingEntity caster;

		private int index;
		private LivingEntity self;
		private SpellShapeInstance shapeInstance;
		
		public SpellState(Spell spell, LivingEntity caster, float efficiency) {
			index = -1;
			this.caster = this.self = caster;
			this.efficiency = efficiency;
			this.spell = spell;
		}
		
		/**
		 * Callback given to spawned shapes.
		 * Indicates the current shape has been done and the spell
		 * should move forward
		 */
		public void trigger(List<LivingEntity> targets, World world, List<BlockPos> locations) {
			this.trigger(targets, world, locations, false);
		}
		
		public void trigger(List<LivingEntity> targets, World world, List<BlockPos> locations, boolean forceSplit) {
			//for each target (if more than one), break into multiple spellstates
			// persist index++ and set self, then start doing shapes or do ending effect
			
			// If being forced to split, dupe state right now and continue on that
			if (forceSplit) {
				this.split().trigger(targets, world, locations, false);
			} else {
				index++;
				if (index >= spell.shapes.size()) {
					this.finish(targets, world, locations);
				} else {
					if (index > 0) {
						if (locations != null && !locations.isEmpty()) {
							Vector3d where = Vector3d.copyCentered(locations.get(0));
							NostrumMagicaSounds.CAST_CONTINUE.play(world, where.getX(), where.getY(), where.getZ());
						} else if (targets != null && !targets.isEmpty()) {
							NostrumMagicaSounds.CAST_CONTINUE.play(targets.get(0));
						}
					}
					
					SpellShapePart shape = spell.shapes.get(index);
					
					// If we have more than one target/pos we hit, split here so each
					// can proceed at their own pace
					if (targets != null && !targets.isEmpty()) {
						if (targets.size() == 1) {
							// don't need to split
							// Also base case after a split happens
							
							// Adjust self, other like if we split
							this.self = targets.get(0);
							spawnShape(shape, this.self, null, null);
						} else {
							index--; // Make splits have same shape as we're performing now
							for (int i = 0; i < targets.size(); i++) {
								LivingEntity targ = targets.get(i);
								SpellState sub = split();
								sub.trigger(Lists.newArrayList(targ), world, null);
							}
							index++;
						}
					} else if (locations != null && !locations.isEmpty()) {
						if (locations.size() == 1) {
							// Base case here, too. Instantiate trigger!!!!
							spawnShape(shape, null, world, locations.get(0));
						} else {
							index--; // Make splits have same trigger as we're performing now
							for (BlockPos targ : locations) {
								SpellState sub = split();
								sub.trigger(null, world, Lists.newArrayList(targ));
							}
							index++;
						}
					} else {
						// Last shape affected no entities or locations. Fizzle
						this.triggerFail();
					}
									
				}
			}
		}
		
		private void spawnShape(SpellShapePart shape, LivingEntity targ, World world, BlockPos targpos) {
			// instantiate trigger in world
			Vector3d pos;
			if (world == null)
				world = targ.world;
			if (targ == null)
				pos = new Vector3d(targpos.getX() + .5, targpos.getY(), targpos.getZ() + .5);
			else
				pos = targ.getPositionVec();
			
			this.shapeInstance = shape.getShape().createInstance(this, world, pos,
					(targ == null ? -90.0f : targ.rotationPitch),
					(targ == null ? 0.0f : targ.rotationYaw),
					shape.getProperties(), spell.getCharacteristics());
			this.shapeInstance.spawn(caster);
		}
		
		private SpellState split() {
			SpellState spawn = new SpellState(spell, caster, this.efficiency);
			spawn.index = this.index;
			
			return spawn;
		}

		public LivingEntity getSelf() {
			return self;
		}

		public LivingEntity getCaster() {
			return caster;
		}

		/**
		 * Called when triggers fail to be triggered and have failed.
		 */
		public void triggerFail() {
			//NostrumMagicaSounds.CAST_FAIL.play();
		}
		
		protected float getTargetEfficiencyBonus(LivingEntity caster, LivingEntity target, SpellEffectPart effect, SpellAction action, float base) {
			float bonus = 0f;
			
			if (effect.getElement() != EMagicElement.PHYSICAL) {
				final Effect boostEffect = ElementalSpellBoostEffect.GetForElement(effect.getElement().getOpposite());
				if (target.getActivePotionEffect(boostEffect) != null) {
					bonus += .25f * (1 + target.getActivePotionEffect(boostEffect).getAmplifier());
					target.removePotionEffect(boostEffect);
				}
			}
			
			return bonus;
		}
		
		protected float getCasterEfficiencyBonus(LivingEntity caster, SpellEffectPart effect, SpellAction action, float base) {
			float bonus = 0f;
			INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			
			if (attr != null)
			switch (effect.getElement()) {
			case EARTH:
				if (attr.hasSkill(NostrumSkills.Earth_Novice)) {
					bonus += .2f;
				}
				break;
			case ENDER:
				if (attr.hasSkill(NostrumSkills.Ender_Novice)) {
					bonus += .2f;
				}
				break;
			case FIRE:
				if (attr.hasSkill(NostrumSkills.Fire_Novice)) {
					bonus += .2f;
				}
				break;
			case ICE:
				if (attr.hasSkill(NostrumSkills.Ice_Novice)) {
					bonus += .2f;
				}
				break;
			case LIGHTNING:
				if (attr.hasSkill(NostrumSkills.Lightning_Novice)) {
					bonus += .2f;
				}
				break;
			case PHYSICAL:
				if (attr.hasSkill(NostrumSkills.Physical_Novice)) {
					bonus += .2f;
				}
				break;
			case WIND:
				if (attr.hasSkill(NostrumSkills.Wind_Novice)) {
					bonus += .2f;
				}
				break;
			}
			
			return bonus;
		}
		
		private void finish(List<LivingEntity> targets, World world, List<BlockPos> positions) {
			boolean first = true;
			INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			float damageTotal = 0f;
			final Map<LivingEntity, EMagicElement> totalAffectedEntities = new HashMap<>();
			for (SpellEffectPart part : spell.parts) {
				SpellAction action = solveAction(part.getAlteration(), part.getElement(), part.getElementCount());
				float efficiency = this.efficiency + (part.getPotency() - 1f);
				
				// Apply part-specific bonuses that don't matter on targets here
				efficiency += getCasterEfficiencyBonus(caster, part, action, efficiency);
				
				if (attr != null && attr.isUnlocked()) {
					attr.setKnowledge(part.getElement(), part.getAlteration());
				}
				
				// Track what entities/positions actually have an affect applied to them
				final List<LivingEntity> affectedEnts = new ArrayList<>();
				final List<BlockPos> affectedPos = new ArrayList<>();
				
				if (targets != null && !targets.isEmpty()) {
					for (LivingEntity targ : targets) {
						// Apply per-target bonuses
						float perEfficiency = efficiency + getTargetEfficiencyBonus(caster, targ, part, action, efficiency);
						
						SpellActionResult result = action.apply(caster, targ, perEfficiency); 
						if (result.applied) {
							affectedEnts.add(targ);
							totalAffectedEntities.put(targ, part.getElement());
							damageTotal += result.damage;
						}
					}
				} else if (positions != null && !positions.isEmpty()) {
					// use locations
					for (BlockPos pos : positions) {
						SpellActionResult result = action.apply(caster, world, pos, efficiency); 
						if (result.applied) {
							affectedPos.add(pos);
						}
					}
				} else {
					; // Drop it on the floor
				}
				
				// Evaluate showing vfx for each part
				if (first) {
					final boolean harmful = action.getProperties().isHarmful;
					
					if (!affectedEnts.isEmpty())
					for (LivingEntity affected : affectedEnts) {
						SpellComponentWrapper comp;
						if (part.getAlteration() == null)
							comp = new SpellComponentWrapper(part.getElement());
						else
							comp = new SpellComponentWrapper(part.getAlteration());
						
						NostrumMagica.instance.proxy.spawnEffect(world, comp,
								caster, null, affected, null,
								new SpellComponentWrapper(part.getElement()), harmful, 0);
					}
					
					if (!affectedPos.isEmpty())
					for (BlockPos affectPos : affectedPos) {
						SpellComponentWrapper comp;
						if (part.getAlteration() == null)
							comp = new SpellComponentWrapper(part.getElement());
						else
							comp = new SpellComponentWrapper(part.getAlteration());
						
						NostrumMagica.instance.proxy.spawnEffect(world, comp,
								caster, null, null, new Vector3d(affectPos.getX() + .5, affectPos.getY(), affectPos.getZ() + .5),
								new SpellComponentWrapper(part.getElement()), harmful, 0);
					}
				}
				
				first = false;
			}
			
			if (attr != null && attr.hasSkill(NostrumSkills.Spellcasting_ElemLinger)) {
				for (Entry<LivingEntity, EMagicElement> entry : totalAffectedEntities.entrySet()) {
					final Effect effect = ElementalSpellBoostEffect.GetForElement(entry.getValue());
					entry.getKey().addPotionEffect(new EffectInstance(effect, 20 * 5, 0));
				}
			}
			
			if (!caster.world.isRemote() && caster instanceof PlayerEntity) {
				final float damageTotalFinal = damageTotal;
				PlayerStatTracker.Update((PlayerEntity) caster, (stats) -> {
					if (damageTotalFinal > 0) {
						stats.takeMax(PlayerStat.MaxSpellDamageDealt, damageTotalFinal);
					}
					// Per element damage calculated by damage listener
				});
			}
		}
	}

	private final String name;
	private final int manaCost;
	private final int weight;
	private final List<SpellShapePart> shapes;
	private final List<SpellEffectPart> parts;

	private int registryID;
	private @Nonnull SpellCharacteristics characteristics;
	private int iconIndex; // Basically useless on server, selects which icon to show on the client
	
	private Spell(String name, int manaCost, int weight, boolean dummy) {
		this.shapes = new ArrayList<>();
		this.parts = new ArrayList<>();
		this.manaCost = manaCost;
		this.weight = weight;
		this.name = name;
		
		iconIndex = 0;
		determineCharacteristics();
	}
	
	/**
	 * Creates a new spell and registers it in the registry.
	 * @param name
	 */
	public Spell(String name, int manaCost, int weight) {
		this(name, false, manaCost, weight);
	}
	
	public Spell(String name, boolean trans, int manaCost, int weight) {
		this(name, manaCost, weight, false);
		
		if (trans)
			registryID = NostrumMagica.instance.getSpellRegistry().registerTransient(this);
		else
			registryID = NostrumMagica.instance.getSpellRegistry().register(this);
	}
	
	public static Spell CreateFake(String name, int id) {
		Spell s = new Spell(name, 0, 0);
		s.registryID = id;
		
		NostrumMagica.instance.getSpellRegistry().override(id, s);
		return s;
	}
	
	public static Spell CreateAISpell(String name) {
		return new Spell(name, true, 50, 10);
	}
	
	/**
	 * Takes a transient spell and makes it an official, non-transient spell
	 */
	public void promoteFromTrans() {
		NostrumMagica.instance.getSpellRegistry().removeTransientStatus(this);
	}
	
	protected void determineCharacteristics() {
		boolean harmful = false;
		EMagicElement element = EMagicElement.PHYSICAL;
		
		if (!parts.isEmpty()) {
			element = parts.get(0).getElement();
			
			for (SpellEffectPart part : parts) {
				SpellAction action = solveAction(part.getAlteration(), part.getElement(), part.getElementCount());
				if (action.getProperties().isHarmful) {
					harmful = true;
					break;
				}
			}
		}
		
		this.characteristics = new SpellCharacteristics(harmful, element);
	}
	
	public void setIcon(int index) {
		this.iconIndex = index;
	}
	
	public Spell addPart(SpellShapePart part) {
		this.shapes.add(part);
		return this;
	}
	
	public Spell addPart(SpellEffectPart part) {
		this.parts.add(part);
		determineCharacteristics();
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public int getRegistryID() {
		return registryID;
	}
	
	public int getIconIndex() {
		return this.iconIndex;
	}
	
	public void cast(LivingEntity caster, float efficiency) {
		SpellState state = new SpellState(this, caster, efficiency);
		Spell.onCast(caster, this);
		state.trigger(Lists.newArrayList(caster), null, null);
		
		NostrumMagicaSounds.CAST_LAUNCH.play(caster);
		if (caster instanceof PlayerEntity) {
			PlayerStatTracker.Update((PlayerEntity) caster, (stats) -> stats.incrStat(PlayerStat.SpellsCast).addStat(PlayerStat.TotalSpellWeight, weight));
		}
	}
	
	public String crc() {
		String s = "";
		for (SpellShapePart part : shapes) {
				s += part.getShape().getShapeKey();
		}
		for (SpellEffectPart part : parts) {
			s += part.getElement().name();
			if (part.getAlteration() != null) {
				s += part.getAlteration().name();
			}
		}
		
		return s;
	}
	
	public int getManaCost() {
		return manaCost;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public Map<ReagentType, Integer> getRequiredReagents() {
		Map<ReagentType, Integer> costs = new EnumMap<ReagentType, Integer>(ReagentType.class);
		
		for (ReagentType type : ReagentType.values())
			costs.put(type, 0);
		
		for (SpellShapePart part : shapes) {
			ReagentType type;
			for (ItemStack req : part.getShape().getReagents()) {
				type = ReagentItem.FindType(req);
				int count = costs.get(type);
				count += req.getCount();
				costs.put(type, count);
			}
		}
		
		for (SpellEffectPart part : parts) {
			ReagentType type;
			if (part.getAlteration() != null) {
				for (ItemStack req : part.getAlteration().getReagents()) {
					type = ReagentItem.FindType(req);
					int count = costs.get(type);
					count += req.getCount();
					costs.put(type, count);
				}
			}
		}
		
		return costs;
	}
	
	// seen is if they've seen it before or not
	public float getXP(boolean seen) {
		// Shapes add some
		// More elements mean more xp
		// Alterations give some
		// 300% first time you use it
		
		float total = 0f;
		
//		for (SpellShapePart part : shapes) {
//			total += 1f;
//		}
		total += shapes.size();
		
		for (SpellEffectPart part : parts) {
			total += 1f;
			if (part.getElementCount() > 1)
				total += (float) (Math.pow(2, part.getElementCount() - 1));
			if (part.getAlteration() != null)
				total += 5f;
		}
		
		if (!seen)
			total *= 3f;
		return total;
	}
	
	public static final SpellAction solveAction(EAlteration alteration,	EMagicElement element, int elementCount) {
		
		// Could do a registry with hooks here, if wanted it to be extensible
		
		if (alteration == null) {
			// Damage spell
			return new SpellAction().damage(element, (float) (elementCount + 1))
					.name("damage." + element.name().toLowerCase());
		}
		
		switch (alteration) {
		case RUIN:
			return solveRuin(element, elementCount);
		case CONJURE:
			return solveConjure(element, elementCount);
		case ENCHANT:
			return solveEnchant(element, elementCount);
		case GROWTH:
			return solveGrowth(element, elementCount);
		case INFLICT:
			return solveInflict(element, elementCount);
		case RESIST:
			return solveResist(element, elementCount);
		case SUMMON:
			return solveSummon(element, elementCount);
		case SUPPORT:
			return solveSupport(element, elementCount);
		case CORRUPT:
			return solveCorrupt(element, elementCount);
		}
		
		return null;
	}
	
	private static final SpellAction solveRuin(EMagicElement element, int elementCount) {
		switch (element) {
		case PHYSICAL:
			return new SpellAction().transmute(elementCount).name("transmute");
		case EARTH:
		case ENDER:
		case FIRE:
		case ICE:
		case LIGHTNING:
		case WIND:
			return new SpellAction().damage(element, 2f + (float) (2 * elementCount))
					.name("ruin." + element.name().toLowerCase());
		}
		
		return null;
	}
	
	private static final SpellAction solveInflict(EMagicElement element, int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction().status(Effects.WEAKNESS, duration, amp).name("weakness");
		case EARTH:
			return new SpellAction().status(NostrumEffects.rooted, duration, amp).name("rooted");
		case ENDER:
			return new SpellAction().status(Effects.BLINDNESS, duration, amp).name("blindness");
		case FIRE:
			//return new SpellAction().status(Effects.NAUSEA, duration / 2, amp).damage(EMagicElement.FIRE, 1 + (amp / 2)).name("overheat");
			return new SpellAction().damage(EMagicElement.FIRE, 1f + ((float) amp/2f)).burn(elementCount * 20 * 5).name("burn");
		case ICE:
			return new SpellAction().status(NostrumEffects.frostbite, duration, amp).name("frostbite");
		case LIGHTNING:
			return new SpellAction().status(Effects.SLOWNESS, (int) (duration * .7), amp + 1).name("slowness");
		case WIND:
			return new SpellAction().status(Effects.POISON, duration, amp).name("poison");
		}
		
		return null;
	}
	
	private static final SpellAction solveResist(EMagicElement element, int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction().status(Effects.RESISTANCE, duration, amp).name("resistance");
		case EARTH:
			return new SpellAction().status(Effects.STRENGTH, duration, amp).name("strength");
		case ENDER:
			return new SpellAction().status(Effects.INVISIBILITY, duration, amp).name("invisibility");
		case FIRE:
			return new SpellAction().status(Effects.FIRE_RESISTANCE, duration, amp).name("fireresist");
		case ICE:
			return new SpellAction().dispel(elementCount * (int) (Math.pow(3, elementCount - 1))).name("dispel");
		case LIGHTNING:
			return new SpellAction().status(NostrumEffects.magicResist, duration, amp).name("magicresist");
		case WIND:
			return new SpellAction().push(5f + (2 * amp), elementCount).name("push");
		}
		
		return null;
	}
	
	private static final SpellAction solveSupport(EMagicElement element, int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction().status(Effects.ABSORPTION, duration * 5, amp).name("lifeboost");
		case EARTH:
			return new SpellAction().status(NostrumEffects.physicalShield, duration, amp).name("shield.physical");
		case ENDER:
			return new SpellAction().blink(15.0f * elementCount).name("blink");
		case FIRE:
			return new SpellAction().status(NostrumEffects.magicBoost, duration, amp).name("magicboost");
		case ICE:
			return new SpellAction().status(NostrumEffects.magicShield, duration, amp).name("shield.magic");
		case LIGHTNING:
			return new SpellAction().pull(5 * elementCount, elementCount).name("pull");
		case WIND:
			return new SpellAction().status(Effects.SPEED, duration, amp).name("speed");
		}
		
		return null;
	}
	
	private static final SpellAction solveGrowth(EMagicElement element, int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction().status(Effects.SATURATION, 1, 4 * elementCount).name("food");
		case EARTH:
			return new SpellAction().status(Effects.REGENERATION, duration, amp).name("regen");
		case ENDER:
			return new SpellAction().swap().name("swap");
		case FIRE:
			return new SpellAction().burnArmor(elementCount).name("burnarmor");
		case ICE:
			return new SpellAction().heal(4f * elementCount).name("heal");
		case LIGHTNING:
			return new SpellAction().status(Effects.JUMP_BOOST, duration, amp).name("jumpboost");
		case WIND:
			return new SpellAction().propel(elementCount).name("propel");
		}
		
		return null;
	}
	
	private static final SpellAction solveEnchant(EMagicElement element, int elementCount) {
		return new SpellAction().enchant(element, elementCount).name("enchant." + element.name().toLowerCase());
	}
	
	private static final SpellAction solveConjure(EMagicElement element, int elementCount) {
		switch (element) {
		case PHYSICAL:
			return new SpellAction().blockBreak(elementCount).name("break");
		case EARTH:
			return new SpellAction().grow(elementCount).name("grow");
		case ENDER:
			return new SpellAction().phase(elementCount).name("phase");
		case FIRE:
			return new SpellAction().cursedFire(elementCount).name("cursed_fire");
		case ICE:
			return new SpellAction().cursedIce(elementCount).name("cursedice");
		case LIGHTNING:
			return new SpellAction().lightning().name("lightningbolt");
		case WIND:
			return new SpellAction().wall(elementCount).name("magicwall");
		}
		
		return null;
	}
	
	private static final SpellAction solveSummon(EMagicElement element, int elementCount) {
		return new SpellAction().summon(element, elementCount).name("summon." + element.name().toLowerCase());
	}
	
	private static final SpellAction solveCorrupt(EMagicElement element, int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction().status(NostrumEffects.rend, duration, amp).name("rend");
		case EARTH:
			return new SpellAction().harvest(elementCount).name("harvest");
		case ENDER:
			return new SpellAction().status(NostrumEffects.disruption, duration, amp).name("disruption");
		case FIRE:
			return new SpellAction().status(NostrumEffects.sublimation, duration, amp).name("sublimation");
		case ICE:
			return new SpellAction().status(NostrumEffects.healResist, duration, amp).name("healresist");
		case LIGHTNING:
			return new SpellAction().status(NostrumEffects.magicRend, duration, amp).name("magicrend");
		case WIND:
			return new SpellAction().status(NostrumEffects.fastFall, duration, amp).name("fastfall");
		}
		
		return null;
	}
	
	private static final String NBT_SPELL_NAME = "name";
	private static final String NBT_MANA_COST = "mana_cost";
	private static final String NBT_WEIGHT = "spell_weight";
	private static final String NBT_SHAPE_LIST = "shapes";
	private static final String NBT_EFFECT_LIST = "effects";
	private static final String NBT_ICON_INDEX = "ico_index";
	
	public CompoundNBT toNBT() {
		CompoundNBT compound = new CompoundNBT();
		compound.putString(NBT_SPELL_NAME, name);
		compound.putInt(NBT_ICON_INDEX, iconIndex);
		compound.putInt(NBT_MANA_COST, manaCost);
		compound.putInt(NBT_WEIGHT, weight);
		
		ListNBT list = new ListNBT();
		for (SpellShapePart part : shapes) {
			CompoundNBT tag = part.toNBT(null);
			list.add(tag);
		}
		compound.put(NBT_SHAPE_LIST, list);
		
		list = new ListNBT();
		for (SpellEffectPart part : parts) {
			CompoundNBT tag = part.toNBT(null);
			list.add(tag);
		}
		compound.put(NBT_EFFECT_LIST, list);
		
		return compound;
	}
	
	/**
	 * Deserializes a spell from NBT.
	 * Does not register it in the registry
	 * @param nbt
	 * @param id
	 * @return
	 */
	public static Spell fromNBT(CompoundNBT nbt, int id) {
		if (nbt == null)
			return null;
		
		String name = nbt.getString(NBT_SPELL_NAME); 
		int index = nbt.getInt(NBT_ICON_INDEX);
		int manaCost = nbt.getInt(NBT_MANA_COST);
		int weight = nbt.getInt(NBT_WEIGHT);
		
		{
			if (!nbt.contains(NBT_MANA_COST)) {
				NostrumMagica.logger.warn("Found spell with no recorded mana cost! Making absurd. " + name + "[" + id + "]");
				manaCost = 10000;
			}
		}
		
		Spell spell = new Spell(name, manaCost, weight, false);
		spell.registryID = id;
		spell.iconIndex = index;
		
		ListNBT list = nbt.getList(NBT_SHAPE_LIST, NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundNBT tag = list.getCompound(i);
			spell.addPart(SpellShapePart.FromNBT(tag));
		}
		
		list = nbt.getList(NBT_EFFECT_LIST, NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundNBT tag = list.getCompound(i);
			spell.addPart(SpellEffectPart.FromNBT(tag));
		}
		
		return spell;
	}
	
	public SpellCharacteristics getCharacteristics() {
		return this.characteristics;
	}

	public EMagicElement getPrimaryElement() {
		return this.getCharacteristics().element;
	}

	public boolean isEmpty() {
		return parts.isEmpty();
	}
	
	public static interface ICastListener {
		
		public void onCast(LivingEntity entity, Spell spell);
	}
	
	private static List<ICastListener> castListeners = new LinkedList<>();
	public static void registerCastListener(ICastListener listener) {
		castListeners.add(listener);
	}
	
	private static void onCast(LivingEntity entity, Spell spell) {
		if (castListeners.isEmpty())
			return;
		
		for (ICastListener l : castListeners) {
			l.onCast(entity, spell);
		}
	}
	
	public int getComponentCount() {
		return shapes.size() + parts.size();
	}
	
	public int getElementCount() {
		int count = 0;
		for (SpellEffectPart part : parts) {
			count += part.getElementCount();
		}
		return count;
	}
	
	public int getShapeCount() {
		return shapes.size();
	}
	
	public Map<EMagicElement, Integer> getElements() {
		Map<EMagicElement, Integer> list = new EnumMap<>(EMagicElement.class);
		for (SpellEffectPart part : parts) {
			EMagicElement element = part.getElement() == null ? EMagicElement.PHYSICAL : part.getElement();
			int count = 0;
			if (list.get(element) != null)
				count = list.get(element);
			count += part.getElementCount();
			list.put(element, count);
		}
		return list;
	}
	
	public Map<EAlteration, Integer> getAlterations() {
		Map<EAlteration, Integer> list = new EnumMap<>(EAlteration.class);
		if (!parts.isEmpty())
		for (SpellEffectPart part : parts) {
			if (part.getAlteration() != null) {
				int count = 0;
				if (list.get(part.getAlteration()) != null)
					count = list.get(part.getAlteration());
				count++;
				list.put(part.getAlteration(), count);
			}
		}
		return list;
	}
	
	public Map<SpellShape, Integer> getShapes() {
		Map<SpellShape, Integer> list = new HashMap<>();
		if (!parts.isEmpty())
		for (SpellShapePart part : shapes) {
			int count = 0;
			if (list.get(part.getShape()) != null)
				count = list.get(part.getShape());
			count++;
			list.put(part.getShape(), count);
		}
		return list;
	}

	public List<SpellEffectPart> getSpellEffectParts() {
		return this.parts;
	}
	
	public List<SpellShapePart> getSpellShapeParts() {
		return this.shapes;
	}
	
	/**
	 * Whether (the first part of this spell) wants tracing, to show indicators when an enemy is being looked at.
	 * For example, seeking bullet needs the player to be looking at an enemy to select who to go after.
	 * @return
	 */
	public boolean shouldTrace() {
		if (!getSpellShapeParts().isEmpty()) {
			SpellShapePart firstShape = getSpellShapeParts().get(0);
			return firstShape.getShape().shouldTrace(firstShape.getProperties());
		}
		
		return false;
	}
	
	public double getTraceRange() {
		if (!getSpellShapeParts().isEmpty()) {
			SpellShapePart firstShape = getSpellShapeParts().get(0);
			return firstShape.getShape().getTraceRange(firstShape.getProperties());
		}
		
		return 0;
	}
}
