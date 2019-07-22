package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.init.Blocks;

public class RoomArena extends StaticRoom {
	
	public RoomArena() {
		super(-20, -10, 0, 20, 10, 40);
		final int minX = -20;
		final int minY = -10;
		final int minZ = 0;
		final int maxX = 20;
		final int maxY = 10;
		final int maxZ = 40;
		
		final BlockState wall = new StaticRoom.BlockState(DungeonBlock.instance(), DungeonBlock.instance().getDefaultState());
		final BlockState lava = new StaticRoom.BlockState(Blocks.LAVA, Blocks.LAVA.getDefaultState());
		final BlockState spawner = new StaticRoom.BlockState(NostrumSingleSpawner.instance(), NostrumSingleSpawner.Type.DRAGON_RED.ordinal());
		
		
		int s = maxX - minX;
		BlockState[][][] blocks = new BlockState[s + 1][][];
		for (int i = 0; i <= s; i++) {
			int y = maxY - minY;
			blocks[i] = new BlockState[y + 1][];
			for (int j = 0; j <= y; j++) {
				int z = maxZ - minZ;
				blocks[i][j] = new BlockState[z + 1];
			}
		}
		
		for (int y = minY; y <= maxY; y++) {

			for (int x = minX; x <= maxX; x++)
			for (int z = minZ; z <= maxZ; z++) {
				BlockState state;
				// Create solid top and bottom
				if (y == minY || y == maxY) {
					state = wall;
				} else if (x == minX || x == maxX || z == minZ || z == maxZ) {
					// Outline
					if (x == 0 && z == 0 && (y == 0 || y == 1)) {
						state = null;
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
							&& !(z < maxZ - 5 && z > minZ + 3)) {
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
		
		
		
//				'X', DungeonBlock.instance(),
//				'W', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.WEST)),
//				'E', new BlockState(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.EAST)),
//				'C', new BlockState(Blocks.CARPET, 14),
//				' ', null);
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
	public DungeonExitPoint getKeyLocation(DungeonExitPoint start) {
		return null;
	}

	@Override
	public List<DungeonExitPoint> getTreasureLocations(DungeonExitPoint start) {
		return new LinkedList<>();
	}
}
