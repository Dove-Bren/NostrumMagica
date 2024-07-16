package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.command.CommandTestConfig;
import com.smanzana.nostrummagica.util.WorldUtil;
import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.world.dungeon.LootUtil;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.WorldGenRegion;

/**
 * Room with all known blocks and bounds at compile-time.
 * @author Skyler
 *
 */
public abstract class StaticRoom implements IDungeonRoom {
	
	protected static class StaticBlockState {
		private BlockState wrappedState;
		
		public StaticBlockState(Block block) {
			this(block.getDefaultState());
		}
		
		public StaticBlockState(BlockState state) {
			this.wrappedState = state;
		}
		
		public void set(IWorld world, BlockPos pos, Direction rotation) {
			final Block block = wrappedState.getBlock();
			BlockState state = this.wrappedState;
			
			if (block instanceof HorizontalBlock) {
				Direction cur = state.get(HorizontalBlock.HORIZONTAL_FACING);
				cur = rotate(cur, rotation);
				state = state.with(HorizontalBlock.HORIZONTAL_FACING, cur);
			} else if (block instanceof WallTorchBlock) {
				Direction cur = state.get(WallTorchBlock.HORIZONTAL_FACING);
				cur = rotate(cur, rotation);
				state = state.with(WallTorchBlock.HORIZONTAL_FACING, cur);
			} else if (block instanceof RedstoneWallTorchBlock) {
				Direction cur = state.get(RedstoneWallTorchBlock.FACING);
				cur = rotate(cur, rotation);
				state = state.with(RedstoneWallTorchBlock.FACING, cur);
			} else if (block instanceof LadderBlock) {
				Direction cur = state.get(LadderBlock.FACING);
				cur = rotate(cur, rotation);
				state = state.with(LadderBlock.FACING, cur);
			} else if (block instanceof StairsBlock) {
				Direction cur = state.get(StairsBlock.FACING);
				cur = rotate(cur, rotation);
				state = state.with(StairsBlock.FACING, cur);
			}
			world.setBlockState(pos, state, 2);
			if (world instanceof WorldGenRegion && WorldUtil.blockNeedsGenFixup(state)) {
				world.getChunk(pos).markBlockForPostprocessing(pos);
			}
		}
		
		private static Direction rotate(Direction in, Direction rotation) {
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
	private StaticBlockState blocks[][][];
	private final ResourceLocation ID;
	
	public StaticRoom(ResourceLocation ID, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
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
		
		this.ID = ID;
	}
	
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
	public StaticRoom(ResourceLocation ID, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
			Object ... args) {
		this(ID, minX, minY, minZ, maxX, maxY, maxZ);
		
		int s = locMaxX - locMinX;
		blocks = new StaticBlockState[s + 1][][];
		for (int i = 0; i <= s; i++) {
			int y = locMaxY - locMinY;
			blocks[i] = new StaticBlockState[y + 1][];
			for (int j = 0; j <= y; j++) {
				int z = locMaxZ - locMinZ;
				blocks[i][j] = new StaticBlockState[z + 1];
			}
		}
		
		parse(args);
	}
	
	public void setBlocks(StaticBlockState[][][] blocks) {
		if (this.blocks == null && blocks != null) {
			this.blocks = blocks;
		}
	}
	
	@Override
	public boolean canSpawnAt(IWorld world, BlueprintLocation start) {
		int relMinX = locMinX;
		int relMinY = locMinY;
		int relMinZ = locMinZ;
		int relMaxX = locMaxX;
		int relMaxY = locMaxY;
		int relMaxZ = locMaxZ;
		
		// Apply rotation
		for (int i = 0; i < start.getFacing().getOpposite().getHorizontalIndex(); i++) {
			// Actual coord change is (x,y)->(-y,x)
			int tmp = relMinX;
			relMinX = -relMinZ;
			relMinZ = relMinX;
			
			tmp = relMaxX;
			relMaxX = -relMaxZ;
			relMaxZ = relMaxX;
			
			// Mins/Maxes shifted to other two corners, though. Swap x's
			tmp = relMinX;
			relMinX = relMaxX;
			relMaxX = tmp;
		}
		
		int minX = start.getPos().getX() + relMinX;
		int minY = start.getPos().getY() + relMinY;
		int minZ = start.getPos().getZ() + relMinZ;
		int maxX = start.getPos().getX() + relMaxX;
		int maxY = start.getPos().getY() + relMaxY;
		int maxZ = start.getPos().getZ() + relMaxZ;
		
		for (int i = minX; i <= maxX; i++)
		for (int j = minY; j <= maxY; j++)
		for (int k = minZ; k <= maxZ; k++) {
			BlockPos pos = new BlockPos(i, j, k);
			BlockState cur = world.getBlockState(pos);
		
			// Check if unbreakable...
			if (cur != null && cur.getBlockHardness(world, pos) == -1)
				return false;
		}
		
		return true;
	}
	
	@Override
	public void spawn(IWorld world, BlueprintLocation start, @Nullable MutableBoundingBox bounds, UUID dungeonID) {
		Set<IChunk> chunks = new HashSet<>();
		
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
			
			// Bounds check!
			if (bounds != null && !bounds.isVecInside(pos)) {
				continue;
			}
			
			if (CommandTestConfig.level != 1) {
				if (!chunks.contains(world.getChunk(pos))) {
					// Side effect: generates chunks if they haven't been. >:)
					chunks.add(world.getChunk(pos));
				}
			}
			
			StaticBlockState state = blocks[i-locMinX][j-locMinY][k-locMinZ];
			
			if (state == null) {
				world.removeBlock(pos, false);
			} else {
				state.set(world, pos, start.getFacing());
			}
			
			applyBlockOverrides(world, pos, new BlockPos(i, j, k), state);
			
		}
		
		List<BlueprintLocation> loots = this.getTreasureLocations(start);
		if (loots != null && !loots.isEmpty())
		for (BlueprintLocation lootSpot : this.getTreasureLocations(start)) {
			// Dungeon generation may replace some chests with other things.
			// Make sure it's still a chest.
			// TODO improve this, especially since rooms are part of dungeongen not blueprints
			if (world.isAirBlock(lootSpot.getPos()) || world.getBlockState(lootSpot.getPos()).getBlock() instanceof ChestBlock) {
				LootUtil.generateLoot(world, lootSpot.getPos(), lootSpot.getFacing());
			}
		}
	}
	
	private void parse(Object[] args) {
		
		// First, skip past all the strings and map characters to BlockStates
		Map<Character, StaticBlockState> states = new HashMap<>();
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
					states.put(last, new StaticBlockState((Block) o));
				} else if (o instanceof BlockState) {
					BlockState s = (BlockState) o;
					states.put(last, new StaticBlockState(s));
				} else if (o == null) {
					states.put(last, null);
				} else if (o instanceof StaticBlockState) {
					states.put(last, (StaticBlockState) o);
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
	
	protected void applyBlockOverrides(IWorld world, BlockPos worldPos, BlockPos dataPos, StaticBlockState defaultState) {
		
	}
	
	@Override
	public ResourceLocation getRoomID() {
		return this.ID;
	}
	
	@Override
	public MutableBoundingBox getBounds(BlueprintLocation entry) {
		// TODO stash and store these
		BlueprintLocation corner1 = NostrumDungeon.asRotated(entry, new BlockPos(locMinX, locMinY, locMinZ), Direction.NORTH);
		BlueprintLocation corner2 = NostrumDungeon.asRotated(entry, new BlockPos(locMaxX, locMaxY, locMaxZ), Direction.NORTH);
		
		return new MutableBoundingBox(corner1.getPos(), corner2.getPos());
	}
	
	public int getRoomWeight() {
		return 1;
	}
	
	public int getRoomCost() {
		return 1;
	}
	
	public abstract List<String> getRoomTags();
	
	public abstract String getRoomName();
}
