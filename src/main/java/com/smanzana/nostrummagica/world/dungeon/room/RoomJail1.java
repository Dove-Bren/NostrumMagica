package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.dungeon.room.DungeonExitData;
import com.smanzana.autodungeons.world.dungeon.room.DungeonRoomExit;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.SingleSpawnerBlock;
import com.smanzana.nostrummagica.world.dungeon.NostrumOverworldDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeons;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;

public class RoomJail1 extends StaticRoom {
	
	public static final ResourceLocation ID = NostrumMagica.Loc("room_jail1");
	
	//19, 10, 25
	// 
	public RoomJail1() {
		super(ID, -4, -1, 0, 14, 9, 24,
				// Floor
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				// Layer 1
				"XXXXCXXXXXXXXXXXXXX",
				"X   C   XBB   X L X",
				"X   C   I     X   X",
				"X   C   I         X",
				"X   C   X     X   X",
				"X   C   I     X   X",
				"X   C   I     X   X",
				"X   C   X     X   X",
				"X   C   XXXXXXX   X",
				"X   C   I     X   X",
				"X   C   I         X",
				"X   C   X     X   X",
				"X   C   XR    X   X",
				"X   C   XXXXXXX   X",
				"X   C   X         X",
				"X   C   I  G  X   X",
				"X   C   I     X   X",
				"X   C   XXXXXXXX XX",
				"X   C             X",
				"X   C     H       X",
				"X   C             X",
				"X   CCCCCCCCCCCCCCC",
				"X   C             X",
				"X   C             X",
				"XXXXCXXXXXXXXXXXXXX",
				// Layer 2
				"XXXX XXXXXXXXXXXXXX",
				"X  S S  XB    X L X",
				"X       I     X   X",
				"X       I         X",
				"X      WX     XE  X",
				"X       I     X   X",
				"X       I     X   X",
				"X       X     X   X",
				"X       XXXXXXX   X",
				"X       I     XE  X",
				"X       I         X",
				"X       X     X   X",
				"X      WX     X   X",
				"X      WXXXXXXXE  X",
				"X       X         X",
				"X       I     X   X",
				"X       I     X   X",
				"X       XXXXXXXX XX",
				"X                 X",
				"X                 X",
				"X                WX",
				"X                  ",
				"X                WX",
				"X  N N            X",
				"XXXX XXXXXXXXXXXXXX",
				// Layer 3
				"XXXXXXXXXXXXXXXXXXX",
				"X       X     X L X",
				"X       X     X   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       XXXXXXX   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       XXXXXXX   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       X     X   X",
				"X       XXXXXXXXXXX",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"XXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXX",
				"X       XXXXXXXXLXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X       XXXXXXXXXXX",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"XXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXX",
				"X       I     X L X",
				"X       IH    X   X",
				"X       I  X  X   X",
				"X       I  X  X   X",
				"X       XXXX  I   X",
				"X       I  X  I   X",
				"X       I  XXXX   X",
				"X       I     X   X",
				"X       I   G X   X",
				"X       XXXX  X   X",
				"X       I TX  I   X",
				"X       I  X      X",
				"X       I  X  I   X",
				"X       I  XXXXI IX",
				"X       I         X",
				"X       IH     G  X",
				"X       IIIIIIIIIIX",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"XXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXX",
				"X       I     XS SX",
				"X             X   X",
				"X          X  X   X",
				"X       IN X  X   X",
				"X       XXXX  I   X",
				"X       IS X  I   X",
				"X          XXXX   X",
				"X             XE  X",
				"X       I     XE  X",
				"X       XXXX  X   X",
				"X          X  I   X",
				"X          X      X",
				"X          X  I   X",
				"X          XXXXI IX",
				"X           SS    X",
				"X       I         X",
				"X       II        X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"XXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXX",
				"X       I     X   X",
				"X             X   X",
				"X          X  X   X",
				"X       I  X  X   X",
				"X       XXXX  I   X",
				"X       I  X  I   X",
				"X          XXXX   X",
				"X             X   X",
				"X       I     X   X",
				"X       XXXX  X   X",
				"X          X  I   X",
				"X          X      X",
				"X          X  I   X",
				"X          XXXXI IX",
				"X                 X",
				"X       I         X",
				"X       II        X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"XXXXXXXXXXXXXXXXXXX",
				// Layer
				"XXXXXXXXXXXXXXXXXXX",
				"X       I     X   X",
				"X       I     X   X",
				"X       I  X  X   X",
				"X       I  X  X   X",
				"X       XXXX  I   X",
				"X       I  X  I   X",
				"X       I  XXXX   X",
				"X       I     X   X",
				"X       I     X   X",
				"X       XXXX  X   X",
				"X          X  I   X",
				"X          X  I   X",
				"X          X  I   X",
				"X          XXXXIIIX",
				"X                 X",
				"X       I         X",
				"X       II        X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"X                 X",
				"XXXXXXXXXXXXXXXXXXX",
				// 
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				// Ceil
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				"XXXXXXXXXXXXXXXXXXX",
				'X', NostrumBlocks.lightDungeonBlock,
				' ', null,
				'L', new StaticBlockState(Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH)),
				'B', Blocks.BOOKSHELF,
				'I', Blocks.IRON_BARS,
				'E', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.EAST)),
				'S', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.SOUTH)),
				'W', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.WEST)),
				'N', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.NORTH)),
				'C', new StaticBlockState(Blocks.RED_CARPET),
				'G', new StaticBlockState(NostrumBlocks.singleSpawner.getState(SingleSpawnerBlock.Type.GOLEM_EARTH)),
				'H', new StaticBlockState(NostrumBlocks.singleSpawner.getState(SingleSpawnerBlock.Type.GOLEM_ICE)),
				'R', new StaticBlockState(Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST)),
				'T', new StaticBlockState(Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH))
				);
	}

	@Override
	public int getNumExits() {
		return 2;
	}

	@Override
	public List<DungeonRoomExit> getExits(BlueprintLocation start) {
		List<DungeonRoomExit> list = new ArrayList<>();
		
		list.add(new DungeonRoomExit(NostrumOverworldDungeon.asRotated(start,
						new BlockPos(0, 0, 24),
						Direction.NORTH), DungeonExitData.EMPTY));
		
		list.add(new DungeonRoomExit(NostrumOverworldDungeon.asRotated(start,
				new BlockPos(14, 0, 21),
				Direction.WEST), DungeonExitData.EMPTY));
		
		return list;
	}

	@Override
	public int getDifficulty() {
		return 8;
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
				NostrumOverworldDungeon.asRotated(start,
						new BlockPos(5, 0, 12),
						Direction.EAST),
				NostrumOverworldDungeon.asRotated(start,
						new BlockPos(6, 4, 11),
						Direction.SOUTH));
	}

	@Override
	public List<String> getRoomTags() {
		return Lists.newArrayList(NostrumDungeons.TAG_DRAGON, NostrumDungeons.TAG_PLANTBOSS, NostrumDungeons.TAG_PORTAL);
	}

	@Override
	public String getRoomName() {
		return "Golem Jail";
	}
	
}
