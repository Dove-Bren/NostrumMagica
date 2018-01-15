package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.HashMap;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Room with all known blocks and bounds at compile-time.
 * @author Skyler
 *
 */
public abstract class StaticRoom implements IDungeonRoom {
	
	private static class BlockState {
		public int meta;
		public Block block;
		
		public BlockState(int meta, Block block) {
			this.meta = meta;
			this.block = block;
		}
		
		@SuppressWarnings("deprecation")
		public void set(World world, BlockPos pos) {
			world.setBlockState(pos, block.getStateFromMeta(meta));
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
		for (int i = locMinX; i <= locMaxX; i++)
		for (int j = locMinY; j <= locMaxY; j++)
		for (int k = locMinZ; k <= locMaxZ; k++) {
			BlockPos pos = new BlockPos(i + start.getPos().getX(),
					j + start.getPos().getY(),
					k + start.getPos().getZ());
			if (blocks[i-locMinX][j-locMinY][k-locMinZ] == null) {
				world.setBlockToAir(pos);
			} else {
				blocks[i-locMinX][j-locMinY][k-locMinZ].set(world, pos);
			}
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
					states.put(last, new BlockState(0, (Block) o));
				} else if (o instanceof IBlockState) {
					IBlockState s = (IBlockState) o;
					states.put(last, new BlockState(
							s.getBlock().getMetaFromState(s),
							s.getBlock()
							));
				} else if (o == null) {
					states.put(last, null);
				} else {
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
			NostrumMagica.logger.warn("Didn't find enough to fill static room!");
		}
	}
}
