package com.smanzana.nostrummagica.spells.components.shapes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

public class ChainShape extends SpellShape {

	private static final String SHAPE_KEY = "shape_chain";
	private static ChainShape instance = null;
	
	public static ChainShape instance() {
		if (instance == null)
			instance = new ChainShape();
		
		return instance;
	}
	
	private ChainShape() {
		super(SHAPE_KEY);
	}

	@Override
	protected List<EntityLivingBase> getTargets(SpellPartParam param, EntityLivingBase target, World world, BlockPos pos) {
		List<EntityLivingBase> ret = new LinkedList<>();
		
		double radius = 6.0;
		if (world == null)
			world = target.worldObj;
		
		int arc = Math.max(2, (int) param.level);
		
		Set<Entity> seen = new HashSet<>();
		
		while (target != null && arc > 0) {
			ret.add(target);
			seen.add(target);
			pos = target.getPosition();
			target = null;

			arc--;
			if (arc > 0)
			for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, 
					new AxisAlignedBB(pos.getX() - radius,
								pos.getY() - radius,
								pos.getZ() - radius,
								pos.getX() + radius,
								pos.getY() + radius,
								pos.getZ() + radius))) {
				if (entity instanceof EntityLivingBase)
					if (Math.abs(entity.getPositionVector().distanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ()))) <= radius
							&& !seen.contains(entity)) {
						target = (EntityLivingBase) entity;
						break;
					}
			}
		}
		
		return ret;
	}

	@Override
	protected List<BlockPos> getTargetLocations(SpellPartParam param, EntityLivingBase target, World world,
			BlockPos pos) {
		List<BlockPos> list = new LinkedList<>();
		
		list.add(pos);
		
		return list;
	}

	@Override
	public List<ItemStack> getReagents() {
		List<ItemStack> list = new ArrayList<>(2);
		
		list.add(ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1));
		list.add(ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1));
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Chain";
	}

}
