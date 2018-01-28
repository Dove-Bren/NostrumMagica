package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockTorch;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class RoomChallenge1 extends StaticRoom {
	//25, 13, 24
	// 
	public RoomChallenge1() {
		super(-19, -7, 0, 5, 5, 23,
				// Floor
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				// Layer 1
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00XXX000000000000000X",
				"X00000XXX000000000000000X",
				"X00000XXX000000000000000X",
				"X00000000000XXXXX000XX00X",
				"X00000000000XXXXX000XX00X",
				"X00000000000XXXXX0000000X",
				"X00000000000000000000000X",
				"X00000000000000000000XXXX",
				"X00000000000000000000XXXX",
				"X00000000000000000000XXXX",
				"X00000000000000000000XXXX",
				"X00000000000000000000XXXX",
				"X00000000000000000000XXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				// Layer 2
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00000000000000000000X",
				"XXXX00XXX000000000000000X",
				"X00000XXX000000000000000X",
				"X00000XXX000000000000000X",
				"X00000000000XXXXX000XX00X",
				"X00000000000XXXXX000XX00X",
				"X00000000000XXXXX0000000X",
				"X00000000000000000000000X",
				"X00000000000000000000XXXX",
				"X00000000000000000000XXXX",
				"X00000000000000000000XXXX",
				"X00000000000000000000XXXX",
				"X00000000000000000000XXXX",
				"X00000000000000000000XXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				// Layer 3
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"X                       X",
				"XL                      X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X              000      X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X     0                 X",
				"X    000                X",
				"X     0                 X",
				"X                       X",
				"X         XXXXXX        X",
				"X         XXXXXX        X",
				"X         XXXXXX      l X",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"X                       X",
				"XL                      X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X              000      X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X     0                 X",
				"X    000                X",
				"X     0                 X",
				"X                       X",
				"X         IIIIII        X",
				"X         I F  I        X",
				"X         I    I      l X",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"X                       X",
				"XL                      X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X              000      X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X     0                 X",
				"X    000                X",
				"X     0                 X",
				"X                       X",
				"X         I    I        X",
				"X                       X",
				"X         I    I      l X",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				// Layer            
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XLXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"X                       X",
				"X                       X",
				"X              000      X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X     0                 X",
				"X    000                X",
				"X     0                 X",
				"X                       X",
				"X         IIIIII        X",
				"X         I    I        X",
				"X         I    I      l X",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXX XXXXX",
				"X                       X",
				"XL                      X",
				"XIIIIIIIIIIIIIIIIIIIIIIIX",
				"X                       X",
				"X                       X",
				"X              000      X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X     0                 X",
				"X    000                X",
				"X     0                 X",
				"X                       X",
				"X             XXXXXXXXXXX",
				"X             XXXXXXXXXXX",
				"X             XXXXXXXXlXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXX XXXXX",
				"XE                S S   X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X              000      X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X     0                 X",
				"X    000                X",
				"X     0                 X",
				"X                       X",
				"X             IIIIIIIIIIX",
				"X             I         X",
				"X             I       l X",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X              000      X",
				"X                       X",
				"XXX                     X",
				"XXX                     X",
				"XXX                     X",
				"XXX                     X",
				"XXX                     X",
				"XXX                     X",
				"X                       X",
				"X                       X",
				"X     0                 X",
				"X    000                X",
				"X     0                 X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                    N NX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X              000      X",
				"X                       X",
				"XII                     X",
				"X I                     X",
				"XPI                     X",
				"X I                     X",
				"X I                     X",
				"XII                     X",
				"X                       X",
				"X                       X",
				"X     0                 X",
				"X    000                X",
				"X     0                 X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X              000      X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X     0                 X",
				"X    000                X",
				"X     0                 X",
				"X                       X",
				"X                       X",
				"X                       X",
				"X                       X",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				// Ceil
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXXXXXXXX",
				'X', DungeonBlock.instance(),
				'0', Blocks.LAVA,
				' ', null,
				'L', new BlockState(Blocks.LADDER, Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.EAST)), // EAST
				'l', new BlockState(Blocks.LADDER, Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.NORTH)), // NORTH 
				'I', Blocks.IRON_BARS,
				'N', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.NORTH)),
				'E', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.EAST)),
				'S', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.SOUTH)),
				'F', new BlockState(NostrumSingleSpawner.instance(), NostrumSingleSpawner.Type.GOLEM_FIRE.ordinal()),
				'P', new BlockState(NostrumSingleSpawner.instance(), NostrumSingleSpawner.Type.GOLEM_LIGHTNING.ordinal()));
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
		return 7;
	}

	@Override
	public boolean hasEnemies() {
		return true;
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
		return true;
	}

	@Override
	public DungeonExitPoint getKeyLocation(DungeonExitPoint start) {
		return NostrumDungeon.asRotated(start, 
				new BlockPos(-4, 1, 21),
				EnumFacing.EAST); 
	}

	@Override
	public List<DungeonExitPoint> getTreasureLocations(DungeonExitPoint start) {
		return Lists.newArrayList(
				NostrumDungeon.asRotated(start,
						new BlockPos(-4, 1, 21),
						EnumFacing.EAST));
	}
	
}
