package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.SingleSpawnerBlock;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceLocation;

public class RoomRedDragonArena extends StaticRoom {
	
	public static final ResourceLocation ID = NostrumMagica.Loc("room_arena");
	
	public RoomRedDragonArena() {
		super(ID, -20, -10, 0, 20, 10, 40);
		final int minX = -20;
		final int minY = -10;
		final int minZ = 0;
		final int maxX = 20;
		final int maxY = 10;
		final int maxZ = 40;
		
		final StaticBlockState wall = new StaticRoom.StaticBlockState(NostrumBlocks.lightDungeonBlock.defaultBlockState());
		final StaticBlockState lava = new StaticRoom.StaticBlockState(Blocks.LAVA.defaultBlockState());
		final StaticBlockState spawner = new StaticRoom.StaticBlockState(NostrumBlocks.singleSpawner.getState(SingleSpawnerBlock.Type.DRAGON_RED));
		final StaticBlockState obsidian = new StaticRoom.StaticBlockState(Blocks.OBSIDIAN.defaultBlockState());
		
		
		int s = maxX - minX;
		StaticBlockState[][][] blocks = new StaticBlockState[s + 1][][];
		for (int i = 0; i <= s; i++) {
			int y = maxY - minY;
			blocks[i] = new StaticBlockState[y + 1][];
			for (int j = 0; j <= y; j++) {
				int z = maxZ - minZ;
				blocks[i][j] = new StaticBlockState[z + 1];
			}
		}
		
		for (int y = minY; y <= maxY; y++) {

			for (int x = minX; x <= maxX; x++)
			for (int z = minZ; z <= maxZ; z++) {
				StaticBlockState state;
				// Create solid top and bottom
				if (y == minY || y == maxY) {
					state = wall;
				} else if (x == minX || x == maxX || z == minZ || z == maxZ) {
					// Outline
					if (x == 0 && z == 0 && (y == 0 || y == 1)) {
						state = obsidian;
					} else {
						state = wall; 
					}
				} else if (y == 0 && x == (minX + ((maxX-minX)/2)) && z == (minZ + ((maxZ-minZ) / 2))) {
					state = spawner;
				} else if (y < 0) {
					// Lava level. Make a 3/4 ring of lava around, with Z rows every 10 or so
					if (x < minX+3 || x > maxX -3 || z > maxZ - 3) {
						state = lava;
					} else if ((x-minX) % ((maxX - minX) / 5) == 0
							&& !(z > maxZ - 5 || z < minZ + 3)) {
						state = lava;
					} else {
						state = wall;
					}
				} else {
					state = null;
				}
				
				blocks[x-minX][y-minY][z-minZ] = state;
			}
		}
		
		this.setBlocks(blocks);
		
		
		
//				'X', NostrumBlocks.lightDungeonBlock,
//				'W', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH, Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.WEST)),
//				'E', new StaticBlockState(Blocks.REDSTONE_WALL_TORCH, Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.EAST)),
//				'C', new StaticBlockState(Blocks.RED_CARPET),
//				' ', null);
	}

	@Override
	public int getNumExits() {
		return 0;
	}

	@Override
	public List<BlueprintLocation> getExits(BlueprintLocation start) {
		return new LinkedList<>();
	}

	@Override
	public int getDifficulty() {
		return 10;
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
		return false;
	}

	@Override
	public List<BlueprintLocation> getTreasureLocations(BlueprintLocation start) {
		return new LinkedList<>();
	}

	@Override
	public List<String> getRoomTags() {
		return new ArrayList<>();
	}

	@Override
	public String getRoomName() {
		return "Red Dragon Arena";
	}
}
