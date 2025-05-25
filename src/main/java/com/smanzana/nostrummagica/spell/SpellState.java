package com.smanzana.nostrummagica.spell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ISpellTargetBlock;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.effect.ElementalSpellBoostEffect;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
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
import com.smanzana.nostrummagica.util.NonNullEnumMap;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

public class SpellState implements ISpellState {
	private static final Component LABEL_MOD_EFF = new TranslatableComponent("spelllogmod.nostrummagica.efficiency");
	
	protected final Spell spell;
	protected final LivingEntity caster;
	protected float efficiency;
	protected final @Nullable LivingEntity targetHint;
	protected final ISpellLogBuilder log;
	protected final List<SpellShapePart> shapes;
	protected final List<SpellEffectPart> parts;
	
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
		this.shapes = spell.getSpellShapeParts();
		this.parts = spell.getSpellEffectParts();
		
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
			final SpellShape stageShape = index == -1 ? null : this.shapes.get(index).getShape();
			this.log.stage(index + 1, stageShape, (int) (this.caster.level.getGameTime() - this.startTicks), targets, locations);
			
			this.efficiency *= stageEfficiency;
			index++;
			if (index >= shapes.size()) {
				this.finish(targets, locations);
			} else {
				if (index > 0) {
					if (targets != null && !targets.isEmpty()) {
						playContinueEffect(targets.get(0));
					} else if (locations != null && !locations.isEmpty()) {
						playContinueEffect(locations.get(0).world, locations.get(0).hitPosition);
					}
				}
				
				SpellShapePart shape = shapes.get(index);
				
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
		
		for (SpellEffectPart part : parts) {
			SpellAction action = SpellEffects.solveAction(part.getAlteration(), part.getElement(), part.getElementCount());
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
					// Possibly interact with spell aware blocks
					BlockState state = pos.world.getBlockState(pos.selectedBlockPos);
					if (state.getBlock() instanceof ISpellTargetBlock target) {
						if (target.processSpellEffect(pos.world, state, pos.selectedBlockPos, caster, pos, spell, action)) {
							// Block consumed this effect
							anySuccess = true;
							totalAffectedLocations.add(pos);
							continue;
						}
					}
					
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
	
	private static final void EmitSpellEffectEntity(Spell spell, LivingEntity caster, LivingEntity targ, SpellActionResult result) {
		MinecraftForge.EVENT_BUS.post(new SpellEffectEntityEvent(spell, caster, targ, result));
	}
	
	private static final void EmitSpellEffectBlock(Spell spell, LivingEntity caster, SpellLocation targ, SpellActionResult result) {
		MinecraftForge.EVENT_BUS.post(new SpellEffectBlockEvent(spell, caster, targ, result));
	}
	
	private static final void EmitSpellEffectEnd(Spell spell, LivingEntity caster, SpellResult result) {
		MinecraftForge.EVENT_BUS.post(new SpellEffectEndEvent(spell, caster, result));
	}
}
