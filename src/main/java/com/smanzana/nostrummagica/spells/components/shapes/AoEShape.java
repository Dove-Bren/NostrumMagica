package com.smanzana.nostrummagica.spells.components.shapes;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellShape;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AoEShape extends SpellShape {

	private static final String SHAPE_KEY = "shape_aoe";
	private static AoEShape instance = null;
	
	public static AoEShape instance() {
		if (instance == null)
			instance = new AoEShape();
		
		return instance;
	}
	
	private AoEShape() {
		super(SHAPE_KEY);
	}

	@Override
	protected List<LivingEntity> getTargets(SpellPartParam param, LivingEntity target, World world, BlockPos pos) {
		List<LivingEntity> ret = new LinkedList<>();
		
		double radius = Math.max(supportedFloats()[0], (double) param.level) + .5;
		final boolean ignoreAllies = param.flip;
		
		for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, 
				new AxisAlignedBB(pos.getX() - radius,
							pos.getY() - radius,
							pos.getZ() - radius,
							pos.getX() + radius,
							pos.getY() + radius,
							pos.getZ() + radius))) {
			LivingEntity living = NostrumMagica.resolveLivingEntity(entity);
			if (living != null && (!ignoreAllies || (target != null && !NostrumMagica.IsSameTeam(target, living))))
				if (Math.abs(entity.getPositionVector().distanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ()))) <= radius)
					ret.add(living);
		}
		
		return ret;
	}

	@Override
	protected List<BlockPos> getTargetLocations(SpellPartParam param, LivingEntity target, World world,
			BlockPos pos) {
		List<BlockPos> list = new LinkedList<>();
		
		final int radius = Math.round(Math.abs(Math.max(2.0f, param.level)));
		
		if (radius == 0) {
			list.add(pos);
		} else {
			for (int i = -radius; i <= radius; i++) {
				// x loop. I is offset of x
				int innerRadius = radius - Math.abs(i);
				for (int j = -innerRadius; j <= innerRadius; j++) {
					int yRadius = innerRadius - Math.abs(j);
					// 0 means just that cell. Otherwise, +- n
					if (yRadius == 0) {
						list.add(pos.add(i, j, 0));
					} else {
						for (int k = -yRadius; k <= yRadius; k++) {
							list.add(pos.add(i, j, k));
						}
					}
				}
				
			}
		}
		
		return list;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		NonNullList<ItemStack> list = NonNullList.from(ItemStack.EMPTY,
			ReagentItem.instance().getReagent(ReagentType.BLACK_PEARL, 1),
			ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1)
		);
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Area of Effect";
	}

	@Override
	public boolean supportsBoolean() {
		return true;
	}

	@Override
	public float[] supportedFloats() {
		return new float[] {2f, 3f, 5f, 10f};
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY, 
				ItemStack.EMPTY,
				new ItemStack(Blocks.REDSTONE_BLOCK, 1, 0),
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1),
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1)
			);
		}
		return costs;
	}

	@Override
	public String supportedBooleanName() {
		return I18n.format("modification.aoe.bool.name", (Object[]) null);
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.aoe.name", (Object[]) null);
	}
	
}
