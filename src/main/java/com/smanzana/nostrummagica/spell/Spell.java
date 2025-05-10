package com.smanzana.nostrummagica.spell;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.criteria.CastSpellCriteriaTrigger;
import com.smanzana.nostrummagica.effect.ElementalSpellBoostEffect;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.SpellEffectEvent.SpellEffectBlockEvent;
import com.smanzana.nostrummagica.spell.SpellEffectEvent.SpellEffectEndEvent;
import com.smanzana.nostrummagica.spell.SpellEffectEvent.SpellEffectEntityEvent;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellAction.SpellActionResult;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape.SpellShapeInstance;
import com.smanzana.nostrummagica.spell.log.ESpellLogModifierType;
import com.smanzana.nostrummagica.spell.log.ISpellLogBuilder;
import com.smanzana.nostrummagica.spell.log.SpellLogBuilder;
import com.smanzana.nostrummagica.spell.log.SpellLogEntry;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;
import com.smanzana.nostrummagica.stat.PlayerStat;
import com.smanzana.nostrummagica.stat.PlayerStatTracker;
import com.smanzana.nostrummagica.util.NonNullEnumMap;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

/**
 * A collection of spell shapes and effects.
 * Shapes all are resolved first, after which effects are applied.
 * If shapes affect multiple people, effects are applied to all.
 * @author Skyler
 *
 */
public class Spell {
	
	public static interface ISpellState {
		public LivingEntity getSelf();
		public LivingEntity getCaster();
		public @Nullable LivingEntity getTargetHint();
		
		public boolean isPreview();
		// May not be supported if isPreview() is true
		public void triggerFail(SpellLocation pos);
		public default void trigger(List<LivingEntity> targets, List<SpellLocation> locations) {
			this.trigger(targets, locations, 1f, false);
		}
		public void trigger(List<LivingEntity> targets, List<SpellLocation> locations, float stageEfficiency, boolean forceSplit);
	}
	
	protected static class SpellState implements ISpellState {
		
		private static final Component LABEL_MOD_EFF = new TranslatableComponent("spelllogmod.nostrummagica.efficiency");
		
		protected final Spell spell;
		protected final LivingEntity caster;
		protected float efficiency;
		protected final @Nullable LivingEntity targetHint;
		protected final ISpellLogBuilder log;
		
		private final long startTicks;

		protected int index;
		private LivingEntity self;
		private SpellShapeInstance shapeInstance;
		
		public SpellState(Spell spell, LivingEntity caster, float efficiency, @Nullable LivingEntity targetHint, ISpellLogBuilder log) {
			index = -1;
			this.caster = this.self = caster;
			this.efficiency = efficiency;
			this.spell = spell;
			this.targetHint = targetHint;
			this.log = log;
			
			this.startTicks = caster.level.getGameTime();
		}
		
		public int getIndex() {
			return index;
		}
		
		@Override
		public @Nullable LivingEntity getTargetHint() {
			return this.targetHint;
		}
		
		@Override
		public boolean isPreview() {
			return false;
		}
		
		@Override
		public void trigger(List<LivingEntity> targets, List<SpellLocation> locations, float stageEfficiency, boolean forceSplit) {
			//for each target (if more than one), break into multiple spellstates
			// persist index++ and set self, then start doing shapes or do ending effect
			
			// If being forced to split, dupe state right now and continue on that
			if (forceSplit) {
				this.split().trigger(targets, locations, stageEfficiency, false);
			} else {
				// Log the stage
				final SpellShape stageShape = index == -1 ? null : this.spell.shapes.get(index).getShape();
				this.log.stage(index + 1, stageShape, (int) (this.caster.level.getGameTime() - this.startTicks), targets, locations);
				
				this.efficiency *= stageEfficiency;
				index++;
				if (index >= spell.shapes.size()) {
					this.finish(targets, locations);
				} else {
					if (index > 0) {
						if (targets != null && !targets.isEmpty()) {
							playContinueEffect(targets.get(0));
						} else if (locations != null && !locations.isEmpty()) {
							playContinueEffect(locations.get(0).world, locations.get(0).hitPosition);
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
								sub.trigger(Lists.newArrayList(targ), null);
							}
							index++;
						}
					} else if (locations != null && !locations.isEmpty()) {
						if (locations.size() == 1) {
							// Base case here, too. Instantiate trigger!!!!
							spawnShape(shape, null, locations.get(0).world, locations.get(0));
						} else {
							index--; // Make splits have same trigger as we're performing now
							for (SpellLocation targ : locations) {
								SpellState sub = split();
								sub.trigger(null, Lists.newArrayList(targ));
							}
							index++;
						}
					} else {
						// Last shape affected no entities or locations. Fizzle
						this.triggerFail(new SpellLocation(caster));
					}
									
				}
			}
		}
		
		protected void playContinueEffect(Level world, Vec3 where) {
			NostrumMagicaSounds.CAST_CONTINUE.play(world, where.x(), where.y(), where.z());
		}
		
		protected void playContinueEffect(LivingEntity at) {
			NostrumMagicaSounds.CAST_CONTINUE.play(at);
		}
		
		protected void spawnShape(SpellShapePart shape, LivingEntity targ, Level world, SpellLocation location) {
			// instantiate trigger in world
			if (world == null)
				world = targ.level;
			
			if (location == null) {
				location = new SpellLocation(targ);
			}
			
			this.shapeInstance = shape.getShape().createInstance(this, targ, location,
					(targ == null ? -90.0f : targ.getXRot()),
					(targ == null ? 0.0f : targ.getYRot()),
					shape.getProperties(), spell.getCharacteristics());
			this.shapeInstance.spawn(caster);
		}
		
		protected SpellState split() {
			SpellState spawn = new SpellState(spell, caster, this.efficiency, this.targetHint, this.log);
			spawn.index = this.index;
			
			return spawn;
		}
		
		protected void doFailEffect(Level world, Vec3 pos) {
			NostrumMagicaSounds.CAST_FAIL.play(world, pos.x(), pos.y(), pos.z());
			((ServerLevel) world).sendParticles(ParticleTypes.SMOKE, pos.x(), pos.y(), pos.z(), 10, 0, 0, 0, .05);
		}
		
		protected float getTargetEfficiencyBonus(LivingEntity caster, LivingEntity target, SpellEffectPart effect, SpellAction action, float base, ISpellLogBuilder log) {
			float bonus = 0f;
			
			if (effect.getElement() != EMagicElement.PHYSICAL) {
				final MobEffect boostEffect = ElementalSpellBoostEffect.GetForElement(effect.getElement().getOpposite());
				if (target.getEffect(boostEffect) != null) {
					final float amt = .25f * (1 + target.getEffect(boostEffect).getAmplifier());
					bonus += amt;
					target.removeEffect(boostEffect);
					log.addGlobalModifier(NostrumSkills.Spellcasting_ElemLinger, amt, ESpellLogModifierType.BONUS_SCALE);
				}
			}
			
			return bonus;
		}
		
		protected float getCasterEfficiencyBonus(LivingEntity caster, SpellEffectPart effect, SpellAction action, float base, ISpellLogBuilder log) {
			float bonus = 0f;
			INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			
			if (attr != null)
			switch (effect.getElement()) {
			case EARTH:
				if (attr.hasSkill(NostrumSkills.Earth_Novice)) {
					bonus += .2f;
					log.addGlobalModifier(NostrumSkills.Earth_Novice, .2f, ESpellLogModifierType.BONUS_SCALE);
				}
				break;
			case ENDER:
				if (attr.hasSkill(NostrumSkills.Ender_Novice)) {
					bonus += .2f;
					log.addGlobalModifier(NostrumSkills.Ender_Novice, .2f, ESpellLogModifierType.BONUS_SCALE);
				}
				break;
			case FIRE:
				if (attr.hasSkill(NostrumSkills.Fire_Novice)) {
					bonus += .2f;
					log.addGlobalModifier(NostrumSkills.Fire_Novice, .2f, ESpellLogModifierType.BONUS_SCALE);
				}
				break;
			case ICE:
				if (attr.hasSkill(NostrumSkills.Ice_Novice)) {
					bonus += .2f;
					log.addGlobalModifier(NostrumSkills.Ice_Novice, .2f, ESpellLogModifierType.BONUS_SCALE);
				}
				break;
			case LIGHTNING:
				if (attr.hasSkill(NostrumSkills.Lightning_Novice)) {
					bonus += .2f;
					log.addGlobalModifier(NostrumSkills.Lightning_Novice, .2f, ESpellLogModifierType.BONUS_SCALE);
				}
				break;
			case PHYSICAL:
				if (attr.hasSkill(NostrumSkills.Physical_Novice)) {
					bonus += .2f;
					log.addGlobalModifier(NostrumSkills.Physical_Novice, .2f, ESpellLogModifierType.BONUS_SCALE);
				}
				break;
			case WIND:
				if (attr.hasSkill(NostrumSkills.Wind_Novice)) {
					bonus += .2f;
					log.addGlobalModifier(NostrumSkills.Wind_Novice, .2f, ESpellLogModifierType.BONUS_SCALE);
				}
				break;
			}
			
			return bonus;
		}
		
		protected void finish(List<LivingEntity> targets, List<SpellLocation> locations) {
			boolean first = true;
			boolean anySuccess = false;
			INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			float damageTotal = 0f;
			float healTotal = 0f;
			final Map<LivingEntity, Map<EMagicElement, Float>> totalAffectedEntities = new HashMap<>();
			final Set<SpellLocation> totalAffectedLocations = new HashSet<>();
			final Map<LivingEntity, EMagicElement> entityLastElement = new HashMap<>();
			
			log.pushModifierStack();
			if (this.efficiency != 1f) {
				log.addGlobalModifier(LABEL_MOD_EFF, this.efficiency-1f, ESpellLogModifierType.BONUS_SCALE);
			}
			
			for (SpellEffectPart part : spell.parts) {
				SpellAction action = solveAction(part.getAlteration(), part.getElement(), part.getElementCount());
				float efficiency = this.efficiency + (part.getPotency() - 1f);
				
				log.pushModifierStack();
				
				// Apply part-specific bonuses that don't matter on targets here
				final float partBonus = getCasterEfficiencyBonus(caster, part, action, efficiency, log);
				efficiency += partBonus;
				
				if (attr != null && attr.isUnlocked()) {
					attr.setKnowledge(part.getElement(), part.getAlteration());
				}
				
				// Track what entities/positions actually have an affect applied to them
				final List<LivingEntity> affectedEnts = new ArrayList<>();
				final List<SpellLocation> affectedPos = new ArrayList<>();
				
				if (targets != null && !targets.isEmpty()) {
					for (LivingEntity targ : targets) {
						if (targ == null) {
							continue;
						}
						
						log.effect(targ);
						log.pushModifierStack();
						
						// Apply per-target bonuses
						final float targBonus = getTargetEfficiencyBonus(caster, targ, part, action, efficiency, log);
						float perEfficiency = efficiency + targBonus;
						
						SpellActionResult result = action.apply(caster, targ, perEfficiency, log); 
						if (result.applied) {
							affectedEnts.add(targ);
							totalAffectedEntities.computeIfAbsent(targ, e -> new NonNullEnumMap<>(EMagicElement.class, 0f)).merge(part.getElement(), result.damage - result.heals, Float::sum);
							entityLastElement.put(targ, part.getElement());
							damageTotal += result.damage;
							healTotal += result.heals;
							anySuccess = true;
							EmitSpellEffectEntity(spell, this.caster, targ, result);
						}
						
						log.popModifierStack();
						
						log.endEffect();
					}
				} else if (locations != null && !locations.isEmpty()) {
					// use locations
					for (SpellLocation pos : locations) {
						log.effect(pos);
						SpellActionResult result = action.apply(caster, pos, efficiency, log); 
						if (result.applied) {
							if (result.affectedPos != null) {
								affectedPos.add(result.affectedPos);
							}
							anySuccess = true;
							totalAffectedLocations.add(result.affectedPos);
							EmitSpellEffectBlock(spell, caster, pos, result);
						}
						log.endEffect();
					}
				} else {
					; // Drop it on the floor
				}
				
				// Evaluate showing vfx for each part
				if (first) {
					if (!affectedEnts.isEmpty())
					for (LivingEntity affected : affectedEnts) {
						NostrumMagica.instance.proxy.spawnSpellEffectVfx(affected.level, part,
								caster, null, affected, null);
					}
					
					if (!affectedPos.isEmpty())
					for (SpellLocation affectPos : affectedPos) {
						NostrumMagica.instance.proxy.spawnSpellEffectVfx(affectPos.world, part,
								caster, null, null, new Vec3(affectPos.selectedBlockPos.getX() + .5, affectPos.selectedBlockPos.getY(), affectPos.selectedBlockPos.getZ() + .5)
								);
					}
				}
				
				first = false;
				
				log.popModifierStack();
			}
			
			log.popModifierStack();
			
			if (anySuccess) {
				if (attr != null && attr.hasSkill(NostrumSkills.Spellcasting_ElemLinger)) {
					for (Entry<LivingEntity, EMagicElement> entry : entityLastElement.entrySet()) {
						final MobEffect effect = ElementalSpellBoostEffect.GetForElement(entry.getValue());
						entry.getKey().addEffect(new MobEffectInstance(effect, 20 * 5, 0));
					}
				}
			} else {
				// Do an effect so it's clearer to caster that there was no effect at any tried location/entity.
				// Mirror "ents, then if not positions" from above.
				if (targets != null && !targets.isEmpty()) {
					for (LivingEntity targ : targets) {
						doFailEffect(targ.level, targ.position().add(0, .2 + targ.getBbHeight(), 0));
					}
				} else if (locations != null && !locations.isEmpty()) {
					for (SpellLocation pos : locations) {
						doFailEffect(pos.world, pos.hitPosition);
					}
				}
			}
			
			log.flush();
			EmitSpellEffectEnd(spell, caster, new SpellResult(anySuccess, damageTotal, healTotal, totalAffectedEntities, totalAffectedLocations));
		}
		
		@Override
		public LivingEntity getSelf() {
			return self;
		}

		@Override
		public LivingEntity getCaster() {
			return caster;
		}

		/**
		 * Called when triggers fail to be triggered and have failed.
		 */
		@Override
		public void triggerFail(SpellLocation pos) {
			log.flush();
			doFailEffect(pos.world, pos.hitPosition);
		}
	}
	
	protected static final class PreviewState extends SpellState {
		
		private final SpellShapePreview previewBuilder;
		private final float partialTicks;
		
		public PreviewState(Spell spell, LivingEntity caster, SpellShapePreview previewBuilder, float partialTicks) {
			super(spell, caster, 1f, null, ISpellLogBuilder.Dummy);
			this.previewBuilder = previewBuilder;
			this.partialTicks = partialTicks;
		}
		
		@Override
		public boolean isPreview() {
			return true;
		}
		
		@Override
		public void trigger(List<LivingEntity> targets, List<SpellLocation> locations, float stageEfficiency, boolean forceSplit) {
			//if (this.getIndex() != 1) {
			{
				if (targets != null && !targets.isEmpty()) {
					for (LivingEntity targ : targets) {
						this.previewBuilder.add(new SpellShapePreviewComponent.Ent(targ));
					}
				} else if (locations != null && !locations.isEmpty()) {
					for (SpellLocation pos : locations) {
						this.previewBuilder.add(new SpellShapePreviewComponent.Position(pos));
					}
				}
			}
			super.trigger(targets, locations, stageEfficiency, forceSplit);
		}

		@Override
		protected void spawnShape(SpellShapePart shape, LivingEntity targ, Level world, SpellLocation location) {
			// For every spawned shape, we should get their special preview parts.
			// Doing this may recurse into triggering this state, but that's alright.
			// Automatically add the target/targetPos to the preview if provided, though.
			
			if (world == null)
				world = targ.level;
			if (location == null) {
				location = new SpellLocation(targ, this.partialTicks);
			}
			
			shape.getShape().addToPreview(previewBuilder, this, targ, location,
					(targ == null ? -90.0f : targ.getXRot()),
					(targ == null ? 0.0f : targ.getYRot()),
					shape.getProperties(), this.spell.getCharacteristics());
		}
		
		@Override
		protected void finish(List<LivingEntity> targets, List<SpellLocation> locations) {
			// Final affected targets or positions for this state
		}

		@Override
		protected PreviewState split() {
			PreviewState spawn = new PreviewState(spell, caster, this.previewBuilder, partialTicks);
			spawn.index = this.index;
			
			return spawn;
		}

		@Override
		protected void doFailEffect(Level world, Vec3 pos) {
			;
		}

		@Override
		protected void playContinueEffect(Level world, Vec3 where) {
			;
		}

		@Override
		protected void playContinueEffect(LivingEntity at) {
			;
		}
	}
	
	public static class SpellResult {
		public final boolean anySuccess;
		public final float damageTotal;
		public final float healingTotal;
		public final Map<LivingEntity, Map<EMagicElement, Float>> affectedEntities;
		public final Set<SpellLocation> affectedLocations;
		
		
		public SpellResult(boolean anySuccess, float damageTotal, float healingTotal,
				Map<LivingEntity, Map<EMagicElement, Float>> affectedEntities, Set<SpellLocation> affectedLocations) {
			super();
			this.anySuccess = anySuccess;
			this.damageTotal = damageTotal;
			this.healingTotal = healingTotal;
			this.affectedLocations = affectedLocations;
			this.affectedEntities = affectedEntities;
		}
	}
	
	private static final void EmitSpellEffectEntity(Spell spell, LivingEntity caster, LivingEntity targ, SpellActionResult result) {
		MinecraftForge.EVENT_BUS.post(new SpellEffectEntityEvent(spell, caster, targ, result));
	}
	
	private static final void EmitSpellEffectBlock(Spell spell, LivingEntity caster, SpellLocation targ, SpellActionResult result) {
		MinecraftForge.EVENT_BUS.post(new SpellEffectBlockEvent(spell, caster, targ, result));
	}
	
	private static final void EmitSpellEffectEnd(Spell spell, LivingEntity caster, SpellResult result) {
		MinecraftForge.EVENT_BUS.post(new SpellEffectEndEvent(spell, caster, result));
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
		cast(caster, efficiency, null);
	}
	
	public void cast(LivingEntity caster, float efficiency, @Nullable LivingEntity targetHint) {
		if (!caster.getServer().isSameThread()) {
			throw new IllegalStateException("Can't cast spell on a thread other than the game thread");
		}
		
		final ISpellLogBuilder logger;
		if (caster instanceof Player && NostrumMagica.instance.proxy.hasIntegratedServer()) {
			SpellLogEntry log = new SpellLogEntry(this, caster);
			logger = new SpellLogBuilder(log);
		} else {
			logger = ISpellLogBuilder.Dummy;
		}
		SpellState state = new SpellState(this, caster, efficiency, targetHint, logger);
		state.trigger(Lists.newArrayList(caster), null);
		
		NostrumMagicaSounds.CAST_LAUNCH.play(caster);
		if (caster instanceof Player) {
			PlayerStatTracker.Update((Player) caster, (stats) -> stats.incrStat(PlayerStat.SpellsCast).addStat(PlayerStat.TotalSpellWeight, weight));
			
			if (caster instanceof ServerPlayer) {
				CastSpellCriteriaTrigger.Instance.trigger((ServerPlayer) caster);
			}
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
	
	public int getCastTicks() {
		final int weight = getWeight();
		int base = 20;
//		if (caster != null && NostrumMagica.getMagicWrapper(caster) != null) {
//			if (NostrumMagica.getMagicWrapper(caster).hasSkill(NostrumSkills.Spellcasting_CooldownReduc)) {
//				base = 10;
//			}
//		}
		return base + (20 * Math.max(0, weight-2)); // incantations superficially add 2 weight so reduce that a bit 
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
			return new SpellAction().status(MobEffects.WEAKNESS, duration, amp)
					.status(NostrumEffects.magicWeakness, duration, amp, (caster, target, eff) -> {
						// Only apply with physical inflict skill
						INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
						if (attr != null && attr.hasSkill(NostrumSkills.Physical_Inflict)) {
							return true;
						}
						return false;
					}).name("weakness");
		case EARTH:
			return new SpellAction().status(NostrumEffects.rooted, duration, (caster, target, eff) -> {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Earth_Inflict)) {
					return amp + 3;
				} else {
					return amp;
				}
			}).name("rooted");
		case ENDER:
			return new SpellAction().status(MobEffects.BLINDNESS, duration, amp).status(NostrumEffects.mobBlindness, duration, amp, (caster, target, eff) -> {
				// With the inflict skill (or if non-player), apply to mobs
				if (target instanceof Mob) {
					INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
					if (!(caster instanceof Player) || (attr != null && attr.hasSkill(NostrumSkills.Ender_Inflict))) {
						return true;
					}
				}
				return false;
			}).resetTarget((caster, target, eff) -> {
				// With the inflict skill, reset target to none
				if (target instanceof Mob) {
					INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
					if (!(caster instanceof Player) || (attr != null && attr.hasSkill(NostrumSkills.Ender_Inflict))) {
						return true;
					}
				}
				return false;
			}).name("blindness");
		case FIRE:
			// Note: damage is AFTER burn so that if burn takes away shields the damage comes through after
			return new SpellAction().burn(elementCount * 20 * 5).damage(EMagicElement.FIRE, 1f + ((float) amp/2f)).name("burn");
		case ICE:
			return new SpellAction().status(NostrumEffects.frostbite, duration, amp, (caster, target, eff) -> {
				if (caster == target) {
					return true;
				}
				
				// With the inflict skill, don't apply frostbite to friendlies
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Ice_Inflict)) {
					return !NostrumMagica.IsSameTeam(caster, target);
				}
				return true;
			}).status(NostrumEffects.manaRegen, duration, amp, (caster, target, eff) -> {
				if (caster == target) {
					return false;
				}
				
				// With the inflict skill, apply mana regen to friendlies
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Ice_Inflict)) {
					return NostrumMagica.IsSameTeam(caster, target);
				}
				return false;
			}).name("frostbite");
		case LIGHTNING:
			return new SpellAction().status(MobEffects.MOVEMENT_SLOWDOWN, (int) (duration * .7), amp)
					.status(NostrumEffects.immobilize, 30 + (10 * elementCount) , amp, (caster, target, eff) -> {
						// Only apply with lightning inflict skill
						INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
						if (attr != null && attr.hasSkill(NostrumSkills.Lightning_Inflict)) {
							return true;
						}
						return false;
					})
					.name("slowness");
		case WIND:
			return new SpellAction().status(MobEffects.POISON, duration, amp).name("poison");
		}
		
		return null;
	}
	
	private static final SpellAction solveResist(EMagicElement element, int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction().status(MobEffects.DAMAGE_RESISTANCE, duration, amp).name("resistance");
		case EARTH:
			return new SpellAction().status(MobEffects.DAMAGE_BOOST, duration, amp).name("strength");
		case ENDER:
			return new SpellAction().status(MobEffects.INVISIBILITY, duration, amp).name("invisibility");
		case FIRE:
			return new SpellAction().status(MobEffects.FIRE_RESISTANCE, duration, amp).name("fireresist");
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
			return new SpellAction().status(MobEffects.ABSORPTION, duration * 5, amp).name("lifeboost");
		case EARTH:
			return new SpellAction().status(NostrumEffects.physicalShield, duration, (caster, target, eff) -> {
				// With the support skill, give 2 extra levels of shield
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Earth_Support)) {
					return amp + 2;
				}
				return amp;
			}).name("shield.physical");
		case ENDER:
			return new SpellAction().blink(15.0f * elementCount).name("blink");
		case FIRE:
			return new SpellAction().status(NostrumEffects.magicBoost, duration, amp).name("magicboost");
		case ICE:
			return new SpellAction().status(NostrumEffects.magicShield, duration, amp)
			.status(NostrumEffects.manaRegen, duration, 0, (caster, target, eff) -> {
				// With the support skill, also give mana regen
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Ice_Support)) {
					return true;
				}
				return false;
			}).name("shield.magic");
		case LIGHTNING:
			return new SpellAction().pull(5 * elementCount, elementCount).name("pull");
		case WIND:
			return new SpellAction().status(MobEffects.MOVEMENT_SPEED, duration, amp)
					.status(MobEffects.DIG_SPEED, duration, amp, (caster, target, eff) -> {
						// With the support skill, also give haste
						INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
						if (attr != null && attr.hasSkill(NostrumSkills.Wind_Support)) {
							return true;
						}
						return false;
					}).name("speed");
		}
		
		return null;
	}
	
	private static final SpellAction solveGrowth(EMagicElement element, int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction().healFood(4 * elementCount).status(NostrumEffects.naturesBlessing, duration * 5, amp, (caster, target, eff) -> {
				// Only apply with physical growth skill
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Physical_Growth)) {
					return true;
				}
				return false;
			}).name("food");
		case EARTH:
			return new SpellAction().status(MobEffects.REGENERATION, duration, amp).name("regen");
		case ENDER:
			return new SpellAction().swap().swapStatus((caster, target, eff) -> {
				// Only apply with ender growth skill
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Ender_Growth)) {
					return true;
				}
				return false;
			}).name("swap");
		case FIRE:
			return new SpellAction().dropEquipment(elementCount, (caster, target, eff) -> {
				// Only apply with fire growth skill AND if a mob
				if (target instanceof Mob) {
					INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
					if (attr != null && attr.hasSkill(NostrumSkills.Fire_Growth)) {
						return true;
					}
				}
				return false;
			}).burnArmor(elementCount).name("burnarmor"); // burn armor after dropping to not damage things before dropping them
		case ICE:
			return new SpellAction().heal(4f * elementCount).name("heal");
		case LIGHTNING:
			return new SpellAction().status(MobEffects.JUMP, duration, amp)
					.status(NostrumEffects.bonusJump, duration, 0, (caster, target, eff) -> {
						// With the growth skill, also give jump boost
						INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
						if (attr != null && attr.hasSkill(NostrumSkills.Lightning_Growth)) {
							return true;
						}
						return false;
					}).name("jumpboost");
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
			return new SpellAction().mysticWater(elementCount-1, elementCount * 2, 20 * 60).name("mystic_water");
		case LIGHTNING:
			return new SpellAction().lightning().name("lightningbolt");
		case WIND:
			return new SpellAction().wall(elementCount).name("mystic_air");
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
			return new SpellAction().status(NostrumEffects.healResist, duration, (caster, target, eff) -> {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Ice_Corrupt)) {
					return amp + 3;
				} else {
					return amp;
				}
			}).name("healresist");
		case LIGHTNING:
			return new SpellAction().status(NostrumEffects.magicRend, duration, amp).name("magicrend");
		case WIND:
			return new SpellAction().status(NostrumEffects.fastFall, duration, amp, (caster, target, eff) -> {
				// With the corrupt skill, don't apply fastfall to friendlies
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Wind_Corrupt)) {
					return !NostrumMagica.IsSameTeam(caster, target);
				}
				return true;
			}).status(MobEffects.SLOW_FALLING, duration, amp, (caster, target, eff) -> {
				// With the corrupt skill, apply slowfall to friendlies
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Wind_Corrupt)) {
					return NostrumMagica.IsSameTeam(caster, target);
				}
				return false;
			}).name("fastfall");
		}
		
		return null;
	}
	
	private static final String NBT_SPELL_NAME = "name";
	private static final String NBT_MANA_COST = "mana_cost";
	private static final String NBT_WEIGHT = "spell_weight";
	private static final String NBT_SHAPE_LIST = "shapes";
	private static final String NBT_EFFECT_LIST = "effects";
	private static final String NBT_ICON_INDEX = "ico_index";
	
	public CompoundTag toNBT() {
		CompoundTag compound = new CompoundTag();
		compound.putString(NBT_SPELL_NAME, name);
		compound.putInt(NBT_ICON_INDEX, iconIndex);
		compound.putInt(NBT_MANA_COST, manaCost);
		compound.putInt(NBT_WEIGHT, weight);
		
		ListTag list = new ListTag();
		for (SpellShapePart part : shapes) {
			CompoundTag tag = part.toNBT(null);
			list.add(tag);
		}
		compound.put(NBT_SHAPE_LIST, list);
		
		list = new ListTag();
		for (SpellEffectPart part : parts) {
			CompoundTag tag = part.toNBT(null);
			list.add(tag);
		}
		compound.put(NBT_EFFECT_LIST, list);
		
		return compound;
	}
	
	/**
	 * Deserializes a spell from Tag.
	 * Does not register it in the registry
	 * @param nbt
	 * @param id
	 * @return
	 */
	public static Spell fromNBT(CompoundTag nbt, int id) {
		Spell spell = fromNBT(nbt); 
		spell.registryID = id;
		return spell;
	}
	
	public static Spell transientFromNBT(CompoundTag nbt) {
		Spell spell = fromNBT(nbt);
		spell.registryID = NostrumMagica.instance.getSpellRegistry().registerTransient(spell);
		return spell;
	}
	
	protected static Spell fromNBT(CompoundTag nbt) {
		if (nbt == null)
			return null;
		
		String name = nbt.getString(NBT_SPELL_NAME); 
		int index = nbt.getInt(NBT_ICON_INDEX);
		int manaCost = nbt.getInt(NBT_MANA_COST);
		int weight = nbt.getInt(NBT_WEIGHT);
		
		Spell spell = new Spell(name, manaCost, weight, false);
		spell.iconIndex = index;
		
		ListTag list = nbt.getList(NBT_SHAPE_LIST, Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag tag = list.getCompound(i);
			spell.addPart(SpellShapePart.FromNBT(tag));
		}
		
		list = nbt.getList(NBT_EFFECT_LIST, Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag tag = list.getCompound(i);
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
	public boolean shouldTrace(Player player) {
		if (!getSpellShapeParts().isEmpty()) {
			SpellShapePart firstShape = getSpellShapeParts().get(0);
			return firstShape.getShape().shouldTrace(player, firstShape.getProperties());
		}
		
		return false;
	}
	
	public double getTraceRange(Player player) {
		if (!getSpellShapeParts().isEmpty()) {
			SpellShapePart firstShape = getSpellShapeParts().get(0);
			return firstShape.getShape().getTraceRange(player, firstShape.getProperties());
		}
		
		return 0;
	}
	
	public boolean supportsPreview() {
		if (!getSpellShapeParts().isEmpty()) {
			for (SpellShapePart shapePart : this.getSpellShapeParts()) {
				if (!shapePart.getShape().supportsPreview(shapePart.getProperties())) {
					return false;
				}
			}
			return true;
		}
		
		return true; // Affects caster
	}
	
	public @Nullable SpellShapePreview getPreview(LivingEntity caster, float partialTicks) {
		if (!getSpellShapeParts().isEmpty()) {
			SpellShapePreview preview = new SpellShapePreview();
			PreviewState previewState = new PreviewState(this, caster, preview, partialTicks);
			previewState.trigger(Lists.newArrayList(caster), null);
			return preview;
		}
		
		// Affects caster
		return new SpellShapePreview().add(new SpellShapePreviewComponent.Ent(caster));
	}
}
