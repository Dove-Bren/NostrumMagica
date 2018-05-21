package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.command.CommandTestConfig;
import com.smanzana.nostrummagica.world.dungeon.LootUtil;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * Room with all known blocks and bounds at compile-time.
 * @author Skyler
 *
 */
public abstract class StaticRoom implements IDungeonRoom {
	
	protected static class BlockState {
		public Block block;
		public int meta;
		private IBlockState actualState;
		
		public BlockState(Block block, int meta) {
			this.meta = meta;
			this.block = block;
		}
		
		public BlockState(Block block, IBlockState actual) {
			this.block = block;
			this.actualState = actual;
		}
		
		@SuppressWarnings("deprecation")
		public void set(World world, BlockPos pos, EnumFacing rotation) {
			IBlockState state;
			if (actualState != null) {
				state = actualState;
			} else {
				state = block.getStateFromMeta(meta);
			}
			
			if (block instanceof BlockHorizontal) {
				EnumFacing cur = state.getValue(BlockHorizontal.FACING);
				cur = rotate(cur, rotation);
				state = state.withProperty(BlockHorizontal.FACING, cur);
			} else if (block instanceof BlockTorch) {
				EnumFacing cur = state.getValue(BlockTorch.FACING);
				cur = rotate(cur, rotation);
				state = state.withProperty(BlockTorch.FACING, cur);
			} else if (block instanceof BlockLadder) {
				EnumFacing cur = state.getValue(BlockLadder.FACING);
				cur = rotate(cur, rotation);
				state = state.withProperty(BlockLadder.FACING, cur);
			} else if (block instanceof BlockStairs) {
				EnumFacing cur = state.getValue(BlockStairs.FACING);
				cur = rotate(cur, rotation);
				state = state.withProperty(BlockStairs.FACING, cur);
			}
			world.setBlockState(pos, state);
		}
		
		private static EnumFacing rotate(EnumFacing in, EnumFacing rotation) {
			int count;
			switch (rotation) {
			case NORTH:
			default:
				count = 0;
				break;
			case EAST:
				count = 1;
				break;
			case SOUTH:
				count = 2;
				break;
			case WEST:
				count = 3;
				break;
			}
			
			while (count-- > 0)
				in = in.rotateY();
			
			return in;
		}
	}

	private int locMinX;
	private int locMinY;
	private int locMinZ;
	private int locMaxX;
	private int locMaxY;
	private int locMaxZ;
	private BlockState blocks[][][];
	
	/**
	 * 
	 * @param minX
	 * @param minY
	 * @param minZ
	 * @param maxX
	 * @param maxY
	 * @param maxZ
	 * @param args First, strings that represent a row of blocks with same Y and Z.
	 * A lot of those. One for each row in each layer, and then repeat for each y
	 * layer. Then, series of characters followed by IBlockStates or Blocks.
	 * It's like registering a recipe in GameRegistry
	 */
	public StaticRoom(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
			Object ... args) {
		this.locMinX = minX;
		this.locMinY = minY;
		this.locMinZ = minZ;
		this.locMaxX = maxX;
		this.locMaxY = maxY;
		this.locMaxZ = maxZ;
		
		int s;
		if (locMinX > locMaxX) {
			s = locMinX;
			locMinX = locMaxX;
			locMaxX = s;
		}
		if (locMinY > locMaxY) {
			s = locMinY;
			locMinY = locMaxY;
			locMaxY = s;
		}
		if (locMinZ > locMaxZ) {
			s = locMinZ;
			locMinZ = locMaxZ;
			locMaxZ = s;
		}
		
		s = locMaxX - locMinX;
		blocks = new BlockState[s + 1][][];
		for (int i = 0; i <= s; i++) {
			int y = locMaxY - locMinY;
			blocks[i] = new BlockState[y + 1][];
			for (int j = 0; j <= y; j++) {
				int z = locMaxZ - locMinZ;
				blocks[i][j] = new BlockState[z + 1];
			}
		}
		
		parse(args);
	}
	
	@Override
	public boolean canSpawnAt(World world, DungeonExitPoint start) {
		int minX = start.getPos().getX() + locMinX;
		int minY = start.getPos().getY() + locMinY;
		int minZ = start.getPos().getZ() + locMinZ;
		int maxX = start.getPos().getX() + locMaxX;
		int maxY = start.getPos().getY() + locMaxY;
		int maxZ = start.getPos().getZ() + locMaxZ;
		for (int i = minX; i <= maxX; i++)
		for (int j = minY; j <= maxY; j++)
		for (int k = minZ; k <= maxZ; k++) {
			BlockPos pos = new BlockPos(i, j, k);
			IBlockState cur = world.getBlockState(pos);
		
			// Check if unbreakable...
			if (cur != null && cur.getBlockHardness(world, pos) == -1)
				return false;
		}
		
		return true;
	}
	
	@Override
	public void spawn(NostrumDungeon dungeon, World world, DungeonExitPoint start) {
		Set<Chunk> chunks = new HashSet<>();
		
		// Get inversions based on rotation
		int modX = 1;
		int modZ = 1;
		boolean swap = false;
		switch (start.getFacing()) {
		case EAST:
			swap = true;
			modX = -1;
			break;
		case SOUTH:
			modX = -1;
			modZ = -1;
			break;
		case NORTH: // -z
		default:
			break;
		case WEST: // -x
			swap = true;
			modZ = -1;
			break;
		}
		
		for (int i = locMinX; i <= locMaxX; i++)
		for (int j = locMinY; j <= locMaxY; j++)
		for (int k = locMinZ; k <= locMaxZ; k++) {
			int x = i, z = k;
			if (swap) {
				int t = x;
				x = z;
				z = t;
			}
			x *= modX;
			z *= modZ;

			BlockPos pos = new BlockPos(x + start.getPos().getX(),
					j + start.getPos().getY(),
					z + start.getPos().getZ());
			
			if (CommandTestConfig.level != 1) {
				if (!chunks.contains(world.getChunkFromBlockCoords(pos))) {
					// Side effect: generates chunks if they haven't been. >:)
					chunks.add(world.getChunkFromBlockCoords(pos));
				}
			}
			
			if (blocks[i-locMinX][j-locMinY][k-locMinZ] == null) {
				world.setBlockToAir(pos);
			} else {
				blocks[i-locMinX][j-locMinY][k-locMinZ].set(world, pos, start.getFacing());
			}
			
		}
		
		List<DungeonExitPoint> loots = this.getTreasureLocations(start);
		if (loots != null && !loots.isEmpty())
		for (NostrumDungeon.DungeonExitPoint lootSpot : this.getTreasureLocations(start)) {
			LootUtil.generateLoot(world, lootSpot.getPos(), lootSpot.getFacing());
		}
	}
	
	private void parse(Object[] args) {
		
		// First, skip past all the strings and map characters to BlockStates
		Map<Character, BlockState> states = new HashMap<>();
		Character last = null;
		for (Object o : args) {
			if (o instanceof String)
				continue;
			
			if (last == null) {
				// This should be a char
				if (!(o instanceof Character)) {
					NostrumMagica.logger.warn("Found unmatched character: " + last);
				}
				
				last = (Character) o;
			} else {
				// last has character. This should be Block or Blockstate
				if (o instanceof Block) {
					states.put(last, new BlockState((Block) o, 0));
				} else if (o instanceof IBlockState) {
					IBlockState s = (IBlockState) o;
					states.put(last, new BlockState(
							s.getBlock(),
							s.getBlock().getMetaFromState(s)
							));
				} else if (o == null) {
					states.put(last, null);
				} else if (o instanceof BlockState) {
					states.put(last, (BlockState) o);
				}else {
					NostrumMagica.logger.warn("Found non compatible definition for type [" + last + "]");
				}
				
				last = null;
			}
		}
		
		int x = locMinX;
		int y = locMinY;
		int z = locMinZ;
		
		for (Object o : args) {
			if (o instanceof String) {
				if (y > locMaxY) {
					NostrumMagica.logger.error("Got too many rows of strings for dungeon room. Ignoring");
					continue;
				}
				
				x = locMinX;
				// row of constant y, z. Parse it
				for (Character c : o.toString().toCharArray()) {
					if (x > locMaxX) {
						NostrumMagica.logger.warn("Found row that was too long in x dir");
						break;
					}
					if (states.containsKey(c)) {
						blocks[x-locMinX]
								[y-locMinY]
										[z-locMinZ] = states.get(c);
					} // else insert null
					x++;
					
					
				}
				
				if (x <= locMaxX) {
					NostrumMagica.logger.warn("Found row that was too short (x: " + x + ", maxx: " + locMaxX);
				}
				
				// increment z, bumping y as necessary
				z++;
				if (z > locMaxZ) {
					z = locMinZ;
					y++;
				}
				
			} else {
				break; // Passed strings
			}
		}
		
		if (y <= locMaxY) {
			NostrumMagica.logger.warn("Didn't find enough to fill static room (not tall enough)!");
		}
	}
}
