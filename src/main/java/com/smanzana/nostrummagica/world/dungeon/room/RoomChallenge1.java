package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.dungeon.room.DungeonRoomExit;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.SingleSpawnerBlock;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeons;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;

public class RoomChallenge1 extends StaticRoom {
	
	public static final ResourceLocation ID = NostrumMagica.Loc("room_challenge1");
	
	//25, 13, 24
	// 
	public RoomChallenge1() {
		super(ID, -19, -7, 0, 5, 5, 23,
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
				'X', NostrumBlocks.lightDungeonBlock,
				'0', Blocks.LAVA,
				' ', null,
				'L', new StaticBlockState(Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST)), // EAST
				'l', new StaticBlockState(Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH)), // NORTH 
				'I', Blocks.IRON_BARS,
				'N', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.NORTH)),
				'E', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.EAST)),
				'S', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.SOUTH)),
				'F', new StaticBlockState(NostrumBlocks.singleSpawner.getState(SingleSpawnerBlock.Type.GOLEM_FIRE)),
				'P', new StaticBlockState(NostrumBlocks.singleSpawner.getState(SingleSpawnerBlock.Type.GOLEM_LIGHTNING)));
	}

	@Override
	public int getNumExits() {
		return 0;
	}

	@Override
	public List<DungeonRoomExit> getExits(BlueprintLocation start) {
		return new ArrayList<>();
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
		return false;
	}

	@Override
	public BlueprintLocation getKeyLocation(BlueprintLocation start) {
		return null;
	}
	
	@Override
	public boolean supportsTreasure() {
		return true;
	}

	@Override
	public List<BlueprintLocation> getTreasureLocations(BlueprintLocation start) {
		return Lists.newArrayList(
				NostrumDungeon.asRotated(start,
						new BlockPos(-4, 1, 21),
						Direction.EAST));
	}

	@Override
	public List<String> getRoomTags() {
		return Lists.newArrayList(NostrumDungeons.TAG_DRAGON);
	}

	@Override
	public String getRoomName() {
		return "Lava Challenge";
	}
	
}
