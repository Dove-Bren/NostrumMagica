package com.smanzana.nostrummagica.spells.components.shapes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.SpellCharacteristics;
import com.smanzana.nostrummagica.spells.SpellShapePartProperties;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

/**
 * Instant ray returning all entities and blocks in it's path
 * @author Skyler
 *
 */
public class BeamShape extends InstantShape {

	public static final String ID = "beam";
	public static final float BEAM_RANGE = 15.0f;
	
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.from(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.MANI_DUST, 1),
			ReagentItem.CreateStack(ReagentType.GRAVE_DUST, 1)));
	
	public BeamShape() {
		this(ID);
	}
	
	protected BeamShape(String key) {
		super(key);
	}

	@Override
	protected TriggerData getTargetData(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		// Cast from eyes
		pos = pos.add(0, state.getCaster().getEyeHeight(), 0);
		
		Collection<RayTraceResult> traces = RayTrace.allInPath(world, state.getSelf(), pos, pitch, yaw, BEAM_RANGE, new RayTrace.OtherLiving(state.getCaster()));
		List<LivingEntity> targs = null;
		List<BlockPos> blocks = null;
		
		Vector3d end = null;
		
		if (traces != null && !traces.isEmpty()) {
			targs = new LinkedList<>();
			blocks = new LinkedList<>();
			
			for (RayTraceResult trace : traces) {
				if (trace == null)
					continue;
				
				if (trace.getType() == RayTraceResult.Type.MISS)
					continue;
				
				if (trace.getType() == RayTraceResult.Type.ENTITY
						&& RayTrace.livingFromRaytrace(trace) != null) {
					targs.add(RayTrace.livingFromRaytrace(trace));
				} else {
					blocks.add(new BlockPos(trace.getHitVec().x, trace.getHitVec().y, trace.getHitVec().z));
					end = trace.getHitVec();
				}
			}
		}
		
		if (end == null)
			end = pos.add(RayTrace.directionFromAngles(pitch, yaw).normalize().scale(BEAM_RANGE));
		
		NostrumMagica.instance.proxy.spawnEffect(world, new SpellComponentWrapper(this),
				null, pos, null, end, null, false, 0);
		
		return new TriggerData(targs, world, blocks);
	}

	@Override
	public String getDisplayName() {
		return "Beam";
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}
	
	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.BLAZE_ROD);
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
		return 2;
	}

	@Override
	public boolean shouldTrace(SpellShapePartProperties params) {
		return true;
	}
	
	@Override
	public double getTraceRange(SpellShapePartProperties params) {
		return BEAM_RANGE;
	}

	@Override
	public int getManaCost() {
		return 35;
	}

}
