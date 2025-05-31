package com.smanzana.nostrummagica.spell;

import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellEffectEvent.SpellEffectBlockEvent;
import com.smanzana.nostrummagica.spell.SpellEffectEvent.SpellEffectEndEvent;
import com.smanzana.nostrummagica.spell.SpellEffectEvent.SpellEffectEntityEvent;
import com.smanzana.nostrummagica.spell.component.SpellAction.SpellActionResult;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape.SpellShapeInstance;
import com.smanzana.nostrummagica.spell.log.ESpellLogModifierType;
import com.smanzana.nostrummagica.spell.log.ISpellLogBuilder;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
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
	public Spell getSpell() {
		return this.spell;
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
	
	protected void finish(List<LivingEntity> targets, List<SpellLocation> locations) {
		final BiConsumer<LivingEntity, SpellActionResult> onEnt = (targ, result) -> EmitSpellEffectEntity(this.spell, this.caster, targ, result);
		final BiConsumer<SpellLocation, SpellActionResult> onBlock = (targ, result) -> EmitSpellEffectBlock(this.spell, this.caster, targ, result);
		
		log.pushModifierStack();
		if (this.efficiency != 1f) {
			log.addGlobalModifier(LABEL_MOD_EFF, this.efficiency-1f, ESpellLogModifierType.BONUS_SCALE);
		}
		
		final SpellEffects.ApplyResult result = SpellEffects.ApplySpellEffects(caster, parts, this.efficiency,
				targets, locations, log,
				onEnt, onBlock);
		
		log.popModifierStack();
		log.flush();
		EmitSpellEffectEnd(spell, caster, new SpellResult(result.anySuccess(), result.damageTotal(), result.healTotal(), result.totalAffectedEntities(), result.totalAffectedLocations()));
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
