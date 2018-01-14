package com.smanzana.nostrummagica.world.dungeon;

import java.util.List;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IDungeonRoom {

	/**
	 * Check whether the room can be spawned at the indicated position with the
	 * given direction. This should check all affected blocks and make sure that
	 * no unbreakable blocks, etc are overlapped
	 * @param world
	 * @param start
	 * @param direction
	 * @return
	 */
	public boolean canSpawnAt(World world, BlockPos start, EnumFacing direction);
	
	/**
	 * Return a list of exists that would be generated if the room was
	 * placed at the given position and facing
	 * @return
	 */
	public List<BlockPos> getExits(BlockPos start, EnumFacing direction);
	
	/**
	 * Returns the difficulty of the given room, which is used when figuring outa
	 * which room to spawn
	 * @return difficulty on scale from 1 to 10
	 */
	public int getDifficulty();
	
	public boolean hasPuzzle();
	
	public boolean hasEnemies();
	
	public boolean hasTraps();
	
	public void spawn(NostrumDungeon dungeon, World world, BlockPos start, EnumFacing direction);
	
}
