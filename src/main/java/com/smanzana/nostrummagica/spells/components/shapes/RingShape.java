package com.smanzana.nostrummagica.spells.components.shapes;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.SpellCharacteristics;
import com.smanzana.nostrummagica.spells.SpellShapePartProperties;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Affect all entities in a ring around the caster.
 * @author Skyler
 *
 */
public class RingShape extends BurstShape {

	private static final String ID = "ring";
	private static final int INNER_RADIUS = 2;
	
	protected RingShape(String id) {
		super(id);
	}
	
	public RingShape() {
		this(ID);
	}
	
	@Override
	protected TriggerData getTargetData(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties param, SpellCharacteristics characteristics) {
		
		this.spawnShapeEffect(state.getCaster(), null, world, pos, param, characteristics);
		
		List<LivingEntity> ret = new ArrayList<>();
		
		double radiusEnts = Math.max(supportedFloats()[0], (double) param.level) + INNER_RADIUS + .5;
		
		for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, 
				new AxisAlignedBB(pos.getX() - radiusEnts,
							pos.getY() - radiusEnts,
							pos.getZ() - radiusEnts,
							pos.getX() + radiusEnts,
							pos.getY() + radiusEnts,
							pos.getZ() + radiusEnts))) {
			LivingEntity living = NostrumMagica.resolveLivingEntity(entity);
			if (living != null) {
				final Vector3d diff = entity.getPositionVec().subtract(pos);
				final double distFlat = Math.sqrt(Math.abs(Math.pow(diff.getX(), 2)) + Math.abs(Math.pow(diff.getZ(), 2)));
				if (distFlat <= radiusEnts
						&& distFlat >= INNER_RADIUS
						&& Math.abs(entity.getPosY() - pos.getY()) <= (INNER_RADIUS + .5) // Flatter than a sphere
					) {
					ret.add(living);
				}
			}
		}
		
		List<BlockPos> list = new ArrayList<>();
		
		final int radiusBlocks = Math.round(Math.abs(Math.max(2.0f, param.level + INNER_RADIUS)));
		
		final BlockPos center = new BlockPos(pos);
		if (radiusBlocks == 0) {
			list.add(center);
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
						list.add(center.add(i, k, j));
					}
				}
				
			}
		}
		
		return new TriggerData(ret, world, list);
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

	@Override
	public boolean supportsBoolean() {
		return false;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] {2f, 3f, 4f, 6f};
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY, 
				ItemStack.EMPTY,
				new ItemStack(ReagentItem.GetItem(ReagentType.MANI_DUST)),
				new ItemStack(Blocks.REDSTONE_BLOCK, 1),
				new ItemStack(NostrumItems.crystalSmall)
			);
		}
		return costs;
	}

	@Override
	public String supportedBooleanName() {
		return null;
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.ring.name", (Object[]) null);
	}
	
	@Override
	public int getWeight() {
		return 1;
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.BONE_MEAL);
	}

	@Override
	public int getManaCost() {
		return 30;
	}

	@Override
	public boolean shouldTrace(SpellShapePartProperties params) {
		return false;
	}
	
	@Override
	public boolean isTerminal(SpellShapePartProperties params) {
		return true;
	}

}
