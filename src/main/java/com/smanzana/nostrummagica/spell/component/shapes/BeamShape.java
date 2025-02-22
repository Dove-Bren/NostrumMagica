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
import com.smanzana.nostrummagica.spell.component.BooleanSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
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
	
	public static final SpellShapeProperty<Boolean> HIT_AIR = new BooleanSpellShapeProperty("hit_air");
	
	public BeamShape() {
		this(ID);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		this.baseProperties.addProperty(HIT_AIR, false);
	}
	
	protected BeamShape(String key) {
		super(key);
	}
	
	protected boolean hitsAir(SpellShapeProperties properties) {
		return properties.getValue(HIT_AIR);
	}
	
	private static NonNullList<ItemStack> costs = null;
	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.DRAGON_BREATH)
				);
		}
		
		if (property == HIT_AIR) {
			return costs;
		} else {
			return super.getPropertyItemRequirements(property);
		}
	}

	@Override
	protected TriggerData getTargetData(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		final boolean hitsAir = hitsAir(params);
		// Cast from eyes
		final Vector3d start = location.shooterPosition;
		final Vector3d dir = RayTrace.directionFromAngles(pitch, yaw);
		final Vector3d end = start.add(dir.normalize().scale(BEAM_RANGE));
		Collection<RayTraceResult> traces = RayTrace.allInPath(location.world, state.getSelf(), start, end, new RayTrace.OtherLiving(state.getCaster()), hitsAir);
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
				
				if (trace.getType() == RayTraceResult.Type.ENTITY) {
					if (RayTrace.livingFromRaytrace(trace) != null) {
						targs.add(RayTrace.livingFromRaytrace(trace));
					}
				} else {
					blocks.add(new SpellLocation(location.world, trace));
				}
			}
		}
		
		if (!state.isPreview()) {
			NostrumMagica.instance.proxy.spawnSpellShapeVfx(location.world, this, params,
					null, start, null, end, characteristics);
		}
		
		return new TriggerData(targs, blocks);
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
	public int getWeight(SpellShapeProperties properties) {
		return 2 + (hitsAir(properties) ? 1 : 0);
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public double getTraceRange(PlayerEntity player, SpellShapeProperties params) {
		return BEAM_RANGE;
	}

	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 45  + (hitsAir(properties) ? 15 : 0);
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(true, true, true);
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		final Vector3d from = location.shooterPosition;
		final Vector3d dir = RayTrace.directionFromAngles(pitch, yaw);
		final Vector3d maxDist = from.add(dir.normalize().scale(BEAM_RANGE));
		builder.add(new SpellShapePreviewComponent.AoELine(from.add(0, -.25, 0).add(Vector3d.fromPitchYaw(pitch, yaw+90).scale(.1f)), maxDist, 3f));
		return super.addToPreview(builder, state, entity, location, pitch, yaw, properties, characteristics);
	}

}
