package com.smanzana.nostrummagica.spells.components;

import java.util.List;

import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Area that is hit in a spell
 * @author Skyler
 *
 */
public abstract class SpellShape {
	
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
	public void perform(SpellAction action, SpellPartParam param, EntityLiving target, World world, BlockPos pos) {
		
		if (target != null && (world == null || pos == null)) {
			world = target.worldObj;
			Vec3 vec = target.getPositionVector();
			pos = new BlockPos(vec.xCoord, vec.yCoord, vec.zCoord);
		}
		
		for (EntityLiving ent : getTargets(param, target, world, pos))
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
	protected abstract List<EntityLiving> getTargets(SpellPartParam param, EntityLiving target, World world, BlockPos pos);
	
	
}
