package com.smanzana.nostrummagica.spells.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Area that is hit in a spell
 * @author Skyler
 *
 */
public abstract class SpellShape {
	
	private static Map<String, SpellShape> registry = new HashMap<>();
	
	public static void register(SpellShape shape) {
		registry.put(shape.getShapeKey(), shape);
	}
	
	public static SpellShape get(String name) {
		return registry.get(name);
	}
	
	private String key;
	
	public SpellShape(String key) {
		this.key = key;
	}
	
	public String getShapeKey() {
		return key;
	}
	
	/**
	 * 
	 * @param action
	 * @param target if there is one. In either case, fill in world and pos
	 * @param world
	 * @param pos
	 */
	public void perform(SpellAction action, SpellPartParam param, EntityLivingBase target, World world, BlockPos pos) {
		
		if (target != null && (world == null || pos == null)) {
			world = target.worldObj;
			Vec3d vec = target.getPositionVector();
			pos = new BlockPos(vec.xCoord, vec.yCoord, vec.zCoord);
		}
		
		for (EntityLivingBase ent : getTargets(param, target, world, pos))
			action.apply(ent);
	}
	
	/**
	 * Return a list of all targets that are affected by this shape when used at the
	 * given position (/ on the given target, if that's how it works for your shape).
	 * target may be null. World and pos should not be in either case.
	 * @param target
	 * @param world
	 * @param pos
	 * @return
	 */
	protected abstract List<EntityLivingBase> getTargets(SpellPartParam param, EntityLivingBase target, World world, BlockPos pos);
	
	
}
