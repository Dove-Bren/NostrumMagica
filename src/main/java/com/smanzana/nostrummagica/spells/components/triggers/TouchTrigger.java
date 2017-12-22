package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Touch. Returns one target -- either an entity or a blockpos
 * Other is set to current self in either case
 * @author Skyler
 *
 */
public class TouchTrigger extends InstantTrigger {

	private static final String TRIGGER_KEY = "trigger_touch";
	private static TouchTrigger instance = null;
	
	public static TouchTrigger instance() {
		if (instance == null)
			instance = new TouchTrigger();
		
		return instance;
	}
	
	private TouchTrigger() {
		super(TRIGGER_KEY);
	}

	private static final float TOUCH_RANGE = 1.5f;
	
	@Override
	protected TriggerData getTargetData(SpellState state, World world,
				Vec3 pos, float pitch, float yaw) {
		
		MovingObjectPosition trace = RayTrace.raytrace(world, pos, pitch, yaw, TOUCH_RANGE, true);
		
		if (trace == null)
			return null;
		
		List<EntityLiving> others = Lists.newArrayList(state.getSelf());
		if (trace.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
			// Cast is safe from 'onlyLiving' option in trace
			return new TriggerData(Lists.newArrayList((EntityLiving) trace.entityHit), others, null);
		} else if (trace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			Vec3 vec = trace.hitVec;
			return new TriggerData(null, others,
					Lists.newArrayList(new BlockPos(Math.round(vec.xCoord), Math.round(vec.yCoord), Math.round(vec.zCoord))));
		} else {
			return null;
		}
	}
	
}
