package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Touch. Returns one target -- either an entity or a blockpos
 * Other is set to current self in either case
 * @author Skyler
 *
 */
public class TouchTrigger extends InstantTrigger {

	private static final String TRIGGER_KEY = "touch";
	private static TouchTrigger instance = null;
	
	public static TouchTrigger instance() {
		if (instance == null)
			instance = new TouchTrigger();
		
		return instance;
	}
	
	private TouchTrigger() {
		super(TRIGGER_KEY);
	}

	public static final float TOUCH_RANGE = 3.0f;
	
	@Override
	protected TriggerData getTargetData(SpellState state, World world,
				Vector3d pos, float pitch, float yaw) {
		pos = pos.add(0, state.getSelf().getEyeHeight(), 0);
		
		RayTraceResult trace = RayTrace.raytrace(world, state.getSelf(), pos, pitch, yaw, TOUCH_RANGE, new RayTrace.OtherLiving(state.getCaster()));
		
		if (trace == null) {
			return new TriggerData(null, null, null, null);
		}
		
		List<LivingEntity> others = Lists.newArrayList(state.getSelf());
		if (trace.getType() == RayTraceResult.Type.ENTITY
				&& null != RayTrace.livingFromRaytrace(trace)
				&& !RayTrace.livingFromRaytrace(trace).isEntityEqual(state.getSelf())) {
			// Cast is safe from 'onlyLiving' option in trace
			return new TriggerData(Lists.newArrayList(RayTrace.livingFromRaytrace(trace)), others, world, null);
		} else if (trace.getType() == RayTraceResult.Type.BLOCK) {
			Vector3d vec = trace.getHitVec();
			return new TriggerData(null, others, world,
					Lists.newArrayList(new BlockPos(Math.floor(vec.x), Math.floor(vec.y), Math.floor(vec.z))));
		} else {
			return new TriggerData(null, null, null, null);
		}
	}
	
	@Override
	public int getManaCost() {
		return 20;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.from(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.GRAVE_DUST, 1));
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
	public NonNullList<ItemStack> supportedFloatCosts() {
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
	
	@Override
	public int getWeight() {
		return 0;
	}
	
}
