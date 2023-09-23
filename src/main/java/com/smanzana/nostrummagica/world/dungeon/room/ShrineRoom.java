package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.ShrineBlock;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.init.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShrineRoom extends StaticRoom implements ISpellComponentRoom {

	private static final int blockXOffset = 0;
	private static final int blockZOffset = 8;
	private SpellComponentWrapper component;
	
	public ShrineRoom() {
		// end up providing the type of shrine!
		super(-5, -1, 0, 5, 5, 10,
				// Floor
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				// Layer 1
				"XXXXXCXXXXX",
				"X 	  C    X",
				"X    C    X",
				"X    C    X",
				"X    C    X",
				"X   CCC   X",
				"X   DDD   X",
				"XQQQQQQQQQX",
				"XQQQQQQQQQX",
				"XQQQQQQQQQX",
				"XXXXXXXXXXX",
				// Layer 2
				"XXXXX XXXXX",
				"X 	S   S  X",
				"X         X",
				"X         X",
				"X         X",
				"XE       WX",
				"X         X",
				"X         X",
				"X    0    X",
				"X         X",
				"XXXXXXXXXXX",
				// Layer 3
				"XXXXXXXXXXX",
				"X 	       X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X  N   N  X",
				"XXXXXXXXXXX",
				// Layer 4
				"XXXXXXXXXXX",
				"X 	       X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"XXXXXXXXXXX",
				// Layer 5
				"XXXXXXXXXXX",
				"X 	       X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"XXXXXXXXXXX",
				// Ceil
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				'X', DungeonBlock.instance(),
				'Q', Blocks.QUARTZ_BLOCK,
				'W', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.WEST)),
				'E', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.EAST)),
				'S', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.SOUTH)),
				'N', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().with(BlockTorch.FACING, Direction.NORTH)),
				' ', null,
				'C', new BlockState(Blocks.CARPET, 14),
				'D', new BlockState(Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(BlockStairs.FACING, Direction.SOUTH).with(BlockStairs.HALF, BlockStairs.EnumHalf.BOTTOM).with(BlockStairs.SHAPE, BlockStairs.EnumShape.STRAIGHT)));
	}

	@Override
	public int getNumExits() {
		return 0;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		return new LinkedList<>();
	}

	@Override
	public int getDifficulty() {
		return 0;
	}

	@Override
	public boolean hasEnemies() {
		return false;
	}

	@Override
	public boolean hasTraps() {
		return false;
	}

	@Override
	public boolean supportsDoor() {
		return false;
	}

	@Override
	public boolean supportsKey() {
		return false;
	}

	@Override
	public DungeonExitPoint getKeyLocation(DungeonExitPoint start) {
		return null;
	}

	@Override
	public List<DungeonExitPoint> getTreasureLocations(DungeonExitPoint start) {
		return null;
	}

	@Override
	public void setComponent(SpellComponentWrapper component) {
		this.component = component;
	}
	
	@Override
	public void spawn(NostrumDungeon dungeon, World world, DungeonExitPoint start) {
		super.spawn(dungeon, world, start);
		
		BlockPos pos = NostrumDungeon.asRotated(start, new BlockPos(blockXOffset, 1, blockZOffset), Direction.NORTH).getPos();
		ShrineBlock.instance().setInWorld(world, pos, component);
	}
}
