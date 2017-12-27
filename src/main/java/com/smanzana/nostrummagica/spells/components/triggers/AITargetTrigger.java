package com.smanzana.nostrummagica.spells.components.triggers;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.spells.Spell.SpellState;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Instantly affects the entity being targetted (attacktarget) by the caster.
 * This will not work for a player. It's made of AI spells
 * @author Skyler
 *
 */
public class AITargetTrigger extends InstantTrigger {

	private static final String TRIGGER_KEY = "trigger_ai";
	private static AITargetTrigger instance = null;
	
	public static AITargetTrigger instance() {
		if (instance == null)
			instance = new AITargetTrigger();
		
		return instance;
	}
	
	private AITargetTrigger() {
		super(TRIGGER_KEY);
	}
	
	@Override
	protected TriggerData getTargetData(SpellState state, World world, Vec3d pos, float pitch, float yaw) {
		EntityLivingBase target = state.getCaster(); // defult to caster. That's what you get for using a trigger for AI!
		if (state.getCaster() instanceof EntityLiving) {
			target = ((EntityLiving) state.getCaster()).getAttackTarget();
		}
		return new TriggerData(Lists.newArrayList(target), null, world, null);
	}
	
	@Override
	public int getManaCost() {
		return 0;
	}
	
}
