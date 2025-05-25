package com.smanzana.nostrummagica.spell;

import java.util.List;

import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.log.ISpellLogBuilder;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class PreviewSpellState extends SpellState {
	
	private final SpellShapePreview previewBuilder;
	private final float partialTicks;
	
	public PreviewSpellState(Spell spell, LivingEntity caster, SpellShapePreview previewBuilder, float partialTicks) {
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
	protected PreviewSpellState split() {
		PreviewSpellState spawn = new PreviewSpellState(spell, caster, this.previewBuilder, partialTicks);
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