package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.FloatSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Affect all entities in a ring around the caster.
 * @author Skyler
 *
 */
public class RingShape extends BurstShape {

	private static final String ID = "ring";
	private static final int INNER_RADIUS = 2;
	
	public static final SpellShapeProperty<Float> RADIUS = new FloatSpellShapeProperty("radius", 2f, 3f, 4f, 6f);
	
	protected RingShape(String id) {
		super(id);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		this.baseProperties.addProperty(RADIUS);
	}
	
	public RingShape() {
		this(ID);
	}
	
	protected float getRadius(SpellShapeProperties properties) {
		return properties.getValue(RADIUS);
	}
	
	@Override
	protected TriggerData getTargetData(ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties param, SpellCharacteristics characteristics) {
		
		if (!state.isPreview()) {
			this.spawnShapeEffect(state.getCaster(), null, location, param, characteristics);
		}
		
		List<LivingEntity> ret = new ArrayList<>();
		
		double radiusEnts = getRadius(param) + INNER_RADIUS + .5;
		final Vector3d centerPos = location.hitPosition;
		
		for (Entity entity : location.world.getEntitiesWithinAABBExcludingEntity(null, 
				new AxisAlignedBB(centerPos.getX() - radiusEnts,
						centerPos.getY() - radiusEnts,
						centerPos.getZ() - radiusEnts,
						centerPos.getX() + radiusEnts,
						centerPos.getY() + radiusEnts,
						centerPos.getZ() + radiusEnts))) {
			LivingEntity living = NostrumMagica.resolveLivingEntity(entity);
			if (living != null) {
				final Vector3d diff = entity.getPositionVec().subtract(centerPos);
				final double distFlat = Math.sqrt(Math.abs(Math.pow(diff.getX(), 2)) + Math.abs(Math.pow(diff.getZ(), 2)));
				if (distFlat <= radiusEnts
						&& distFlat >= INNER_RADIUS
						&& Math.abs(entity.getPosY() - centerPos.getY()) <= (INNER_RADIUS + .5) // Flatter than a sphere
					) {
					ret.add(living);
				}
			}
		}
		
		List<SpellLocation> list = new ArrayList<>();
		
		final int radiusBlocks = Math.round(Math.max(2.0f, getRadius(param) + INNER_RADIUS));
		
		final BlockPos center = location.hitBlockPos;
		if (radiusBlocks == 0) {
			list.add(new SpellLocation(location.world, center));
		} else {
			for (int i = -radiusBlocks; i <= radiusBlocks; i++) {
				// x loop. I is offset of x
				int innerRadius = radiusBlocks - Math.abs(i);
				for (int j = -innerRadius; j <= innerRadius; j++) {
					// Make sure it's outside the inner radius
					final int safetyRadius = (characteristics.harmful ? INNER_RADIUS + 1 : INNER_RADIUS);
					if (Math.abs(i) + Math.abs(j) < safetyRadius) {
						continue;
					}
					
					int yRadius = 1;
					for (int k = -yRadius; k <= yRadius; k++) {
						list.add(new SpellLocation(location.world, center.add(i, k, j)));
					}
				}
				
			}
		}
		
		return new TriggerData(ret, list);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		NonNullList<ItemStack> list = NonNullList.from(ItemStack.EMPTY,
			ReagentItem.CreateStack(ReagentType.BLACK_PEARL, 1),
			ReagentItem.CreateStack(ReagentType.GINSENG, 1)
		);
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Ring";
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public <T> NonNullList<ItemStack> supportedFloatCosts(SpellShapeProperty<T> property) {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY, 
				ItemStack.EMPTY,
				new ItemStack(ReagentItem.GetItem(ReagentType.MANI_DUST)),
				new ItemStack(Blocks.REDSTONE_BLOCK, 1),
				new ItemStack(NostrumItems.crystalSmall)
			);
		}
		return property == RADIUS ? costs : super.supportedFloatCosts(property);
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 1;
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.BONE_MEAL);
	}

	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 30;
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapeProperties params) {
		return false;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(true, true, true);
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	protected void addRangeRings(SpellShapePreview builder, ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		float radiusEnts = getRadius(properties) + INNER_RADIUS;
		builder.add(new SpellShapePreviewComponent.Disk(location.hitPosition.add(0, .5, 0), (float) radiusEnts));
		builder.add(new SpellShapePreviewComponent.Disk(location.hitPosition.add(0, .5, 0), (float) INNER_RADIUS));
	}

}
