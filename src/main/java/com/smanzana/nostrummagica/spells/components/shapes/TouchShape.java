package com.smanzana.nostrummagica.spells.components.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.SpellCharacteristics;
import com.smanzana.nostrummagica.spells.SpellShapePartProperties;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

/**
 * Touch. Returns one target -- either an entity or a blockpos
 * @author Skyler
 *
 */
public class TouchShape extends InstantShape {

	public static final String ID = "touch";
	public static final float TOUCH_RANGE = 3.0f;
	
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.from(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.GRAVE_DUST, 1)));
	
	public TouchShape() {
		this(ID);
	}
	
	protected TouchShape(String key) {
		super(key);
	}

	@Override
	protected TriggerData getTargetData(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		pos = pos.add(0, state.getSelf().getEyeHeight(), 0);
		
		RayTraceResult trace = RayTrace.raytrace(world, state.getSelf(), pos, pitch, yaw, TOUCH_RANGE, new RayTrace.OtherLiving(state.getCaster()));
		
		if (trace == null) {
			return new TriggerData(null, null, null);
		}
		
		if (trace.getType() == RayTraceResult.Type.ENTITY
				&& null != RayTrace.livingFromRaytrace(trace)
				&& !RayTrace.livingFromRaytrace(trace).isEntityEqual(state.getSelf())) {
			// Cast is safe from 'onlyLiving' option in trace
			return new TriggerData(Lists.newArrayList(RayTrace.livingFromRaytrace(trace)), world, null);
		} else if (trace.getType() == RayTraceResult.Type.BLOCK) {
			Vector3d vec = trace.getHitVec();
			return new TriggerData(null, world,
					Lists.newArrayList(new BlockPos(Math.floor(vec.x), Math.floor(vec.y), Math.floor(vec.z))));
		} else {
			return new TriggerData(null, null, null);
		}
	}

	@Override
	public String getDisplayName() {
		return "Touch";
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public boolean supportsBoolean() {
		return false;
	}

	@Override
	public String supportedBooleanName() {
		return null;
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
	public String supportedFloatName() {
		return null;
	}

	@Override
	public int getWeight() {
		return 0;
	}

	@Override
	public boolean shouldTrace(SpellShapePartProperties params) {
		return true;
	}
	
	@Override
	public double getTraceRange(SpellShapePartProperties params) {
		return TOUCH_RANGE;
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.IRON_INGOT);
	}

	@Override
	public int getManaCost() {
		return 10;
	}

}