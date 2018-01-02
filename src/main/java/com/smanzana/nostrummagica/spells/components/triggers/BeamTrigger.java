package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Touch. Returns one target -- either an entity or a blockpos
 * Other is set to current self in either case
 * @author Skyler
 *
 */
public class BeamTrigger extends InstantTrigger {

	private static final String TRIGGER_KEY = "trigger_beam";
	private static BeamTrigger instance = null;
	
	public static BeamTrigger instance() {
		if (instance == null)
			instance = new BeamTrigger();
		
		return instance;
	}
	
	private BeamTrigger() {
		super(TRIGGER_KEY);
	}

	private static final float BEAM_RANGE = 15.0f;
	
	@Override
	protected TriggerData getTargetData(SpellState state, World world,
				Vec3d pos, float pitch, float yaw) {
		
		Collection<RayTraceResult> traces = RayTrace.allInPath(world, pos, pitch, yaw, BEAM_RANGE, true);
		
		if (traces == null || traces.isEmpty()) {
			return new TriggerData(null, null, null, null);
		}
		
		List<EntityLivingBase> others = Lists.newArrayList(state.getSelf());
		List<EntityLivingBase> targs = new LinkedList<>();
		List<BlockPos> blocks = new LinkedList<>();
		
		for (RayTraceResult trace : traces) {
			if (trace == null)
				continue;
			
			if (trace.typeOfHit == RayTraceResult.Type.MISS)
				continue;
			
			if (trace.typeOfHit == RayTraceResult.Type.ENTITY && trace.entityHit != null)
				targs.add((EntityLivingBase) trace.entityHit);
			else
				blocks.add(new BlockPos(trace.hitVec.xCoord, trace.hitVec.yCoord, trace.hitVec.zCoord));
				
		}
		
		return new TriggerData(targs, others, world, blocks);
	}
	
	@Override
	public int getManaCost() {
		return 35;
	}

	@Override
	public List<ItemStack> getReagents() {
		List<ItemStack> list = new ArrayList<>(2);
		
		list.add(ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1));
		list.add(ReagentItem.instance().getReagent(ReagentType.CRYSTABLOOM, 1));
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Beam";
	}
	
}
