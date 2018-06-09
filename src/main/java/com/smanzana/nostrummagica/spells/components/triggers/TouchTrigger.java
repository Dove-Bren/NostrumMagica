package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
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

	private static final float TOUCH_RANGE = 3.0f;
	
	@Override
	protected TriggerData getTargetData(SpellState state, World world,
				Vec3d pos, float pitch, float yaw) {
		pos = pos.addVector(0, state.getSelf().getEyeHeight(), 0);
		
		RayTraceResult trace = RayTrace.raytrace(world, pos, pitch, yaw, TOUCH_RANGE, true);
		
		if (trace == null) {
			return new TriggerData(null, null, null, null);
		}
		
		List<EntityLivingBase> others = Lists.newArrayList(state.getSelf());
		if (trace.typeOfHit == RayTraceResult.Type.ENTITY && trace.entityHit != null
				&& trace.entityHit.isEntityEqual(state.getSelf())) {
			// Cast is safe from 'onlyLiving' option in trace
			return new TriggerData(Lists.newArrayList((EntityLivingBase) trace.entityHit), others, world, null);
		} else if (trace.typeOfHit == RayTraceResult.Type.BLOCK) {
			Vec3d vec = trace.hitVec;
			return new TriggerData(null, others, world,
					Lists.newArrayList(new BlockPos(Math.floor(vec.xCoord), Math.floor(vec.yCoord), Math.floor(vec.zCoord))));
		} else {
			return new TriggerData(null, null, null, null);
		}
	}
	
	@Override
	public int getManaCost() {
		return 20;
	}

	@Override
	public List<ItemStack> getReagents() {
		List<ItemStack> list = new ArrayList<>(1);
		
		list.add(ReagentItem.instance().getReagent(ReagentType.GRAVE_DUST, 1));
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Touch";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.IRON_INGOT);
	}

	@Override
	public boolean supportsBoolean() {
		return false;
	}

	@Override
	public float[] supportedFloats() {
		return null;
	}

	@Override
	public ItemStack[] supportedFloatCosts() {
		return null;
	}

	@Override
	public String supportedBooleanName() {
		return null;
	}

	@Override
	public String supportedFloatName() {
		return null;
	}
	
}
