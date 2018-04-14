package com.smanzana.nostrummagica.spells.components.shapes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellShape;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
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
	protected List<EntityLivingBase> getTargets(SpellPartParam param, EntityLivingBase target, World world, BlockPos pos) {
		List<EntityLivingBase> ret = new LinkedList<>();
		
		double radius = Math.max(3.0, (double) param.level);
		
		for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, 
				new AxisAlignedBB(pos.getX() - radius,
							pos.getY() - radius,
							pos.getZ() - radius,
							pos.getX() + radius,
							pos.getY() + radius,
							pos.getZ() + radius))) {
			if (entity instanceof EntityLivingBase)
				if (Math.abs(entity.getPositionVector().distanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ()))) <= radius)
					ret.add((EntityLivingBase) entity);
		}
		
		return ret;
	}

	@Override
	protected List<BlockPos> getTargetLocations(SpellPartParam param, EntityLivingBase target, World world,
			BlockPos pos) {
		List<BlockPos> list = new LinkedList<>();
		
		int radius = Math.round(Math.abs(Math.max(2.0f, param.level)));
		
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
	public List<ItemStack> getReagents() {
		List<ItemStack> list = new ArrayList<>(2);
		
		list.add(ReagentItem.instance().getReagent(ReagentType.BLACK_PEARL, 1));
		list.add(ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1));
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Area of Effect";
	}

}
