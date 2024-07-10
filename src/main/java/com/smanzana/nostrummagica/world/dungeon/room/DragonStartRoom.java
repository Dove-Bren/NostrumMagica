package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonRoomInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.IWorldHeightReader;

import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;

public class DragonStartRoom extends StaticRoom implements IDungeonStartRoom {
	
	private final RoomExtendedDragonStaircase stairs;
	private final RoomEntryDragon entry;
	
	public DragonStartRoom() {
		// End up passing in height to surface?
		super(NostrumMagica.Loc("dragon_start_room"), -5, -1, -5, 5, 5, 5,
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
				"XXXXXXXXXXX",
				"XBBBBBBB BX",
				"XBBBBBBBBBX",
				"X  CCCCC  X",
				"X  CCCCC  X",
				"X  CDXXC  X",
				"X  CXXXC  X",
				"X  CCCCC  X",
				"CCCCCCCCCCC",
				"X         X",
				"XXXXXXXXXXX",
				// Layer 2
				"XXXXXXXXXXX",
				"XBBBBBBBBBX",
				"X BBBBBBB X",
				"X         X",
				"X         X",
				"X    XX   X",
				"X    RX   X",
				"X         X",
				"           ",
				"X         X",
				"XXXXXXXXXXX",
				// Layer 3
				"XXXXXXXXXXX",
				"X 	BBBBB  X",
				"X   BBB   X",
				"X         X",
				"X     X   X",
				"X    XU   X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"XXXXXXXXXXX",
				// Layer 4
				"XXXXXXXXXXX",
				"X 	BBBBB  X",
				"X         X",
				"X         X",
				"X   XL    X",
				"X    X    X",
				"X         X",
				"X         X",
				"X         X",
				"X         X",
				"XXXXXXXXXXX",
				// Layer 5
				"XXXXXXXXXXX",
				"X 	       X",
				"X         X",
				"X  G   G  X",
				"X         X",
				"X   DX    X",
				"X   X     X",
				"X  G   G  X",
				"X         X",
				"X         X",
				"XXXXXXXXXXX",
				// Ceil
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXX   XXXX",
				"XXXX X XXXX",
				"XXXX RXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				"XXXXXXXXXXX",
				'X', NostrumBlocks.lightDungeonBlock,
				' ', null,
				'G', Blocks.GLOWSTONE,
				'B', Blocks.BOOKSHELF,
				'C', new StaticBlockState(Blocks.RED_CARPET),
				'U', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				'R', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				'D', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)),
				'L', new StaticBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST).with(StairsBlock.HALF, Half.BOTTOM).with(StairsBlock.SHAPE, StairsShape.STRAIGHT)));
		
		this.stairs = new RoomExtendedDragonStaircase(false);
		this.entry = new RoomEntryDragon(false);
	}
	
	@Override
	public int getNumExits() {
		return 2;
	}

	@Override
	public List<DungeonExitPoint> getExits(DungeonExitPoint start) {
		List<DungeonExitPoint> list = new LinkedList<>();
		
		BlockPos pos;
		
		pos = new BlockPos(-5, 0, 3);
		list.add(NostrumDungeon.asRotated(start, pos, Direction.EAST));
		
		pos = new BlockPos(5, 0, 3);
		list.add(NostrumDungeon.asRotated(start, pos, Direction.WEST));
		
		return list;
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
	public boolean supportsTreasure() {
		return true;
	}

	@Override
	public List<DungeonExitPoint> getTreasureLocations(DungeonExitPoint start) {
		List<DungeonExitPoint> list = new LinkedList<>();
		
		BlockPos pos = new BlockPos(3, 0, -4);
		list.add(NostrumDungeon.asRotated(start, pos, Direction.SOUTH));
		
		return list;
	}
	
	@Override
	public void spawn(IWorld world, DungeonExitPoint start, @Nullable MutableBoundingBox bounds, UUID dungeonID) {
		super.spawn(world, start, bounds, dungeonID);
		
		DungeonExitPoint adj = new DungeonExitPoint(start.getPos().add(0, 6, 0), start.getFacing());
		stairs.spawn(world, adj, bounds, dungeonID);
	}

	@Override
	public List<DungeonRoomInstance> generateExtraPieces(IWorldHeightReader world, DungeonExitPoint start, Random rand, DungeonInstance instance) {
		// Stairs and entry room
		DungeonExitPoint adj = new DungeonExitPoint(start.getPos().add(0, 6, 0), start.getFacing());
		return Lists.newArrayList(
				new DungeonRoomInstance(adj, stairs, false, false, instance, UUID.randomUUID()),
				new DungeonRoomInstance(stairs.getEntryStart(world, adj), entry, false, false, instance, UUID.randomUUID())
				);
	}
}
