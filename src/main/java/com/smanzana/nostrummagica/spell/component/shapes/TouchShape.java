package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.client.resources.I18n;
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
	protected TriggerData getTargetData(ISpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		pos = pos.add(0, state.getSelf().getEyeHeight(), 0);
		
		RayTraceResult trace = RayTrace.raytrace(world, state.getSelf(), pos, pitch, yaw, TOUCH_RANGE, new RayTrace.OtherLiving(state.getCaster()));
		
		if (trace == null || trace.getType() == RayTraceResult.Type.MISS) {
			final boolean ignoreAirHits = params.flip;
			if (ignoreAirHits) {
				return new TriggerData(null, null, null);
			} else {
				// Project where we reached and return there
				return new TriggerData(null, world, Lists.newArrayList(new BlockPos(RayTrace.directionFromAngles(pitch, yaw).scale(TOUCH_RANGE).add(pos))));
			}
		}
		
		if (trace.getType() == RayTraceResult.Type.ENTITY
				&& null != RayTrace.livingFromRaytrace(trace)
				&& !RayTrace.livingFromRaytrace(trace).isEntityEqual(state.getSelf())) {
			// Cast is safe from 'onlyLiving' option in trace
			return new TriggerData(Lists.newArrayList(RayTrace.livingFromRaytrace(trace)), world, null);
		} else if (trace.getType() == RayTraceResult.Type.BLOCK) {
			BlockPos hitPos = RayTrace.blockPosFromResult(trace);
			return new TriggerData(null, world,
					Lists.newArrayList(hitPos));
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
		return true;
	}

	@Override
	public String supportedBooleanName() {
		return  I18n.format("modification.touch.bool.name");
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
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		return super.addToPreview(builder, state, world, pos, pitch, yaw, properties, characteristics);
	}

}
