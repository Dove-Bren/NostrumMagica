package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;

/**
 * Touch. Returns one target -- either an entity or a blockpos
 * @author Skyler
 *
 */
public class TouchShape extends InstantShape {

	public static final String ID = "touch";
	public static final float AI_TOUCH_RANGE = 3.0f;
	
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.from(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.GRAVE_DUST, 1)));
	
	public TouchShape() {
		this(ID);
	}
	
	protected TouchShape(String key) {
		super(key);
	}
	
	protected float getTouchRange(ISpellState state, SpellShapePartProperties params) {
		if (state.getSelf() instanceof PlayerEntity) {
			return getTouchRange((PlayerEntity) state.getSelf(), params);
		} else {
			return AI_TOUCH_RANGE;
		}
	}
	
	protected float getTouchRange(PlayerEntity player, SpellShapePartProperties params) {
		// This copied from PlayerController#getBlockReachDistance
		return (float) player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue() + (player.isCreative() ? 0 : -.5f);
	}

	@Override
	protected TriggerData getTargetData(ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		final float range = getTouchRange(state, params);
		
		RayTraceResult trace = RayTrace.raytrace(world, state.getSelf(), location.shooterPosition, pitch, yaw, range, new RayTrace.OtherLiving(state.getCaster()));
		
		if (trace == null || trace.getType() == RayTraceResult.Type.MISS) {
			final boolean ignoreAirHits = params.flip;
			if (ignoreAirHits) {
				return new TriggerData(null, null, null);
			} else {
				// Project where we reached and return there
				return new TriggerData(null, world, Lists.newArrayList(new SpellLocation(RayTrace.directionFromAngles(pitch, yaw).scale(range).add(location.shooterPosition))));
			}
		}
		
		if (trace.getType() == RayTraceResult.Type.ENTITY
				&& null != RayTrace.livingFromRaytrace(trace)
				&& !RayTrace.livingFromRaytrace(trace).isEntityEqual(state.getSelf())) {
			// Cast is safe from 'onlyLiving' option in trace
			return new TriggerData(Lists.newArrayList(RayTrace.livingFromRaytrace(trace)), world, null);
		} else if (trace.getType() == RayTraceResult.Type.BLOCK) {
			return new TriggerData(null, world,
					Lists.newArrayList(new SpellLocation(trace)));
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
	public boolean shouldTrace(PlayerEntity player, SpellShapePartProperties params) {
		return true;
	}
	
	@Override
	public double getTraceRange(PlayerEntity player, SpellShapePartProperties params) {
		return getTouchRange(player, params);
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
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		return super.addToPreview(builder, state, world, location, pitch, yaw, properties, characteristics);
	}

}
