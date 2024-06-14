package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
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
	protected TriggerData getTargetData(ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		// Cast from eyes
		final Vector3d start = location.shooterPosition;
		final Vector3d dir = RayTrace.directionFromAngles(pitch, yaw);
		final Vector3d end = start.add(dir.normalize().scale(BEAM_RANGE));
		Collection<RayTraceResult> traces = RayTrace.allInPath(world, state.getSelf(), start, end, new RayTrace.OtherLiving(state.getCaster()));
		List<LivingEntity> targs = null;
		List<SpellLocation> blocks = null;
		
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
					blocks.add(new SpellLocation(trace));
				}
			}
		}
		
		if (!state.isPreview()) {
			NostrumMagica.instance.proxy.spawnEffect(world, new SpellComponentWrapper(this),
					null, start, null, end, null, false, 0);
		}
		
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
	public int getWeight(SpellShapePartProperties properties) {
		return 2;
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapePartProperties params) {
		return true;
	}
	
	@Override
	public double getTraceRange(PlayerEntity player, SpellShapePartProperties params) {
		return BEAM_RANGE;
	}

	@Override
	public int getManaCost(SpellShapePartProperties properties) {
		return 35;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapePartProperties params) {
		return new SpellShapeAttributes(true, true, true);
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		final Vector3d from = location.shooterPosition;
		final Vector3d dir = RayTrace.directionFromAngles(pitch, yaw);
		final Vector3d maxDist = from.add(dir.normalize().scale(BEAM_RANGE));
		builder.add(new SpellShapePreviewComponent.AoELine(from.add(0, -.25, 0).add(Vector3d.fromPitchYaw(pitch, yaw+90).scale(.1f)), maxDist, 3f));
		return super.addToPreview(builder, state, world, location, pitch, yaw, properties, characteristics);
	}

}
