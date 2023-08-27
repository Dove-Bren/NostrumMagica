package com.smanzana.nostrummagica.spells.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
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
	
	public static Collection<String> getAllNames() {
		return registry.keySet();
	}
	
	public static Collection<SpellShape> getAllShapes() {
		return registry.values();
	}
	
	private String key;
	
	public SpellShape(String key) {
		this.key = key;
	}
	
	public String getShapeKey() {
		return key;
	}
	
	public abstract String getDisplayName();
	
	/**
	 * 
	 * @param action
	 * @param target if there is one. In either case, fill in world and pos
	 * @param world
	 * @param pos
	 */
	public void perform(SpellAction action,
						SpellPartParam param,
						LivingEntity target,
						World world,
						BlockPos pos,
						float efficiency,
						List<LivingEntity> affectedEnts,
						List<BlockPos> affectedPos) {
		
		if (target != null && (world == null || pos == null)) {
			world = target.world;
			Vec3d vec = target.getPositionVector();
			pos = new BlockPos(vec.x, vec.y, vec.z);
		}
		
		List<LivingEntity> entTargets = getTargets(param, target, world, pos);
		if (entTargets != null && !entTargets.isEmpty())
		for (LivingEntity ent : entTargets) {
			if (ent != null) {
				if (action.apply(ent, efficiency)) {
					affectedEnts.add(ent);
				}
			}
		}
		
		List<BlockPos> blockTargets = getTargetLocations(param, target, world, pos);
		if (blockTargets != null && !blockTargets.isEmpty())
		for (BlockPos bp : blockTargets) {
			if (bp != null) {
				if (action.apply(world, bp, efficiency)) {
					affectedPos.add(bp);
				}
			}
		}
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
	protected abstract List<LivingEntity> getTargets(SpellPartParam param, LivingEntity target, World world, BlockPos pos);
	
	/**
	 * Returns all blockPOS that are affected when using this shape.
	 * This is called in addition to getTargets (which returns entities).
	 * Target may be null. Work positions should not be.
	 * @param param
	 * @param target
	 * @param world
	 * @param pos
	 * @return
	 */
	protected abstract List<BlockPos> getTargetLocations(SpellPartParam param, LivingEntity target, World world, BlockPos pos);
	
	/**
	 * Return a list of reagents required.
	 * Both type and count of the itemstacks will be respected.
	 * @return
	 */
	public abstract NonNullList<ItemStack> getReagents();
	
	/**
	 * Whether this shape supports a boolean switch in its SpellPartParam
	 * @return
	 */
	public abstract boolean supportsBoolean();
	
	/**
	 * Display name for the boolean option.
	 * @return
	 */
	public abstract String supportedBooleanName();
	
	/**
	 * If this shape supports float values in its SpellPartParams, which floats are
	 * accepted.
	 * @return
	 */
	public abstract float[] supportedFloats();
	
	/**
	 * Array of itemstack costs for the above floats.
	 * Should be the same size as the array returned by supportedFloats()
	 * The idea is you return more valuable materials the higher the float.
	 * @return
	 */
	public abstract NonNullList<ItemStack> supportedFloatCosts();
	
	/**
	 * Display name for the float option. Should be translated already
	 */
	public abstract String supportedFloatName();
	
	
}
