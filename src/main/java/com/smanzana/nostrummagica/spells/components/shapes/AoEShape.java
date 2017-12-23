package com.smanzana.nostrummagica.spells.components.shapes;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellShape;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
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
	protected List<EntityLiving> getTargets(SpellPartParam param, EntityLiving target, World world, BlockPos pos) {
		List<EntityLiving> ret = new LinkedList<>();
		
		double radius = (double) param.level;
		
		for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, 
				AxisAlignedBB.fromBounds(pos.getX() - radius,
							pos.getY() - radius,
							pos.getZ() - radius,
							pos.getX() + radius,
							pos.getY() + radius,
							pos.getZ() + radius))) {
			if (entity instanceof EntityLiving)
				if (Math.abs(entity.getPositionVector().distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ()))) <= radius)
					ret.add((EntityLiving) entity);
		}
		
		return ret;
	}

}
