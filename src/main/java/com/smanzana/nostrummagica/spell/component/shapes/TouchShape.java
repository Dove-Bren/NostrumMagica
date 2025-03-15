package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.BooleanSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeSelector;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;

/**
 * Touch. Returns one target -- either an entity or a blockpos
 * @author Skyler
 *
 */
public class TouchShape extends InstantShape implements ISelectableShape {

	public static final String ID = "touch";
	public static final float AI_TOUCH_RANGE = 3.0f;

	public static final SpellShapeProperty<Boolean> IGNORE_AIR = new BooleanSpellShapeProperty("ignore_air");
	
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.GRAVE_DUST, 1)));
	
	public TouchShape() {
		this(ID);
	}
	
	protected TouchShape(String key) {
		super(key);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		this.baseProperties.addProperty(IGNORE_AIR).addProperty(SpellShapeSelector.PROPERTY);
	}
	
	protected boolean getIgnoreAirHits(SpellShapeProperties properties) {
		return properties.getValue(IGNORE_AIR);
	}
	
	protected float getTouchRange(ISpellState state, SpellShapeProperties params) {
		if (state.getSelf() instanceof PlayerEntity) {
			return getTouchRange((PlayerEntity) state.getSelf(), params);
		} else {
			return AI_TOUCH_RANGE;
		}
	}
	
	protected float getTouchRange(PlayerEntity player, SpellShapeProperties params) {
		// This copied from PlayerController#getBlockReachDistance
		return (float) player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue() + (player.isCreative() ? 0 : -.5f);
	}

	@Override
	protected TriggerData getTargetData(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		final float range = getTouchRange(state, params);
		
		RayTraceResult trace = RayTrace.raytrace(location.world, state.getSelf(), location.shooterPosition, pitch, yaw, range, 
				this.affectsEntities(params) ? new RayTrace.OtherLiving(state.getCaster()) : (e) -> false);
		
		if (trace == null || trace.getType() == RayTraceResult.Type.MISS) {
			final boolean ignoreAirHits = getIgnoreAirHits(params);
			if (ignoreAirHits || !this.affectsBlocks(params)) {
				return new TriggerData(null, null);
			} else {
				// Project where we reached and return there
				return new TriggerData(null, Lists.newArrayList(new SpellLocation(location.world, RayTrace.directionFromAngles(pitch, yaw).scale(range).add(location.shooterPosition))));
			}
		}
		
		if (trace.getType() == RayTraceResult.Type.ENTITY
				&& this.affectsEntities(params)
				&& null != RayTrace.livingFromRaytrace(trace)
				&& !RayTrace.livingFromRaytrace(trace).is(state.getSelf())) {
			// Cast is safe from 'onlyLiving' option in trace
			return new TriggerData(Lists.newArrayList(RayTrace.livingFromRaytrace(trace)), null);
		} else if (trace.getType() == RayTraceResult.Type.BLOCK
				&& this.affectsBlocks(params)) {
			return new TriggerData(null, Lists.newArrayList(new SpellLocation(location.world, trace)));
		} else {
			return new TriggerData(null, null);
		}
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 0;
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public double getTraceRange(PlayerEntity player, SpellShapeProperties params) {
		return getTouchRange(player, params);
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.IRON_INGOT);
	}

	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 10;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		return super.addToPreview(builder, state, entity, location, pitch, yaw, properties, characteristics);
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(false, this.affectsEntities(params), this.affectsBlocks(params));
	}

}
