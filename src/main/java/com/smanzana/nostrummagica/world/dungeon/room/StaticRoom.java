package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.util.WorldUtil;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.dungeon.DungeonInstance;
import com.smanzana.autodungeons.world.dungeon.room.IDungeonRoom;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.command.CommandTestConfig;
import com.smanzana.nostrummagica.world.dungeon.NostrumOverworldDungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Room with all known blocks and bounds at compile-time.
 * @author Skyler
 *
 */
public abstract class StaticRoom implements IDungeonRoom {
	
	protected static class StaticBlockState {
		private BlockState wrappedState;
		
		public StaticBlockState(Block block) {
			this(block.defaultBlockState());
		}
		
		public StaticBlockState(BlockState state) {
			this.wrappedState = state;
		}
		
		public void set(LevelAccessor world, BlockPos pos, Direction rotation) {
			final Block block = wrappedState.getBlock();
			BlockState state = this.wrappedState;
			
			if (block instanceof HorizontalDirectionalBlock) {
				Direction cur = state.getValue(HorizontalDirectionalBlock.FACING);
				cur = rotate(cur, rotation);
				state = state.setValue(HorizontalDirectionalBlock.FACING, cur);
			} else if (block instanceof WallTorchBlock) {
				Direction cur = state.getValue(WallTorchBlock.FACING);
				cur = rotate(cur, rotation);
				state = state.setValue(WallTorchBlock.FACING, cur);
			} else if (block instanceof RedstoneWallTorchBlock) {
				Direction cur = state.getValue(RedstoneWallTorchBlock.FACING);
				cur = rotate(cur, rotation);
				state = state.setValue(RedstoneWallTorchBlock.FACING, cur);
			} else if (block instanceof LadderBlock) {
				Direction cur = state.getValue(LadderBlock.FACING);
				cur = rotate(cur, rotation);
				state = state.setValue(LadderBlock.FACING, cur);
			} else if (block instanceof StairBlock) {
				Direction cur = state.getValue(StairBlock.FACING);
				cur = rotate(cur, rotation);
				state = state.setValue(StairBlock.FACING, cur);
			}
			world.setBlock(pos, state, 2);
			if (world instanceof WorldGenRegion && WorldUtil.blockNeedsGenFixup(state)) {
				world.getChunk(pos).markPosForPostprocessing(pos);
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
				in = in.getClockWise();
			
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
	public boolean canSpawnAt(LevelAccessor world, BlueprintLocation start) {
		int relMinX = locMinX;
		int relMinY = locMinY;
		int relMinZ = locMinZ;
		int relMaxX = locMaxX;
		int relMaxY = locMaxY;
		int relMaxZ = locMaxZ;
		
		// Apply rotation
		for (int i = 0; i < start.getFacing().getOpposite().get2DDataValue(); i++) {
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
			if (cur != null && cur.getDestroySpeed(world, pos) == -1)
				return false;
		}
		
		return true;
	}
	
	@Override
	public void spawn(LevelAccessor world, BlueprintLocation start, @Nullable BoundingBox bounds, DungeonInstance dungeonInstance, UUID roomID) {
		Set<ChunkAccess> chunks = new HashSet<>();
		
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
			if (bounds != null && !bounds.isInside(pos)) {
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
			// Actual dugneon generation will set up loot, but I was originally lazy and didn't put chests in spots.
			// So if there isn't a chest there and it's air, replace with a chest.
			if (bounds != null && !bounds.isInside(lootSpot.getPos())) {
				continue;
			}
			
			if (world.isEmptyBlock(lootSpot.getPos())) {
				world.setBlock(lootSpot.getPos(), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, lootSpot.getFacing()), 2);
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
	
	protected void applyBlockOverrides(LevelAccessor world, BlockPos worldPos, BlockPos dataPos, StaticBlockState defaultState) {
		
	}
	
	@Override
	public ResourceLocation getRoomID() {
		return this.ID;
	}
	
	@Override
	public BoundingBox getBounds(BlueprintLocation entry) {
		// TODO stash and store these
		BlueprintLocation corner1 = NostrumOverworldDungeon.asRotated(entry, new BlockPos(locMinX, locMinY, locMinZ), Direction.NORTH);
		BlueprintLocation corner2 = NostrumOverworldDungeon.asRotated(entry, new BlockPos(locMaxX, locMaxY, locMaxZ), Direction.NORTH);
		
		return BoundingBox.fromCorners(corner1.getPos(), corner2.getPos());
	}
	
	public int getRoomWeight() {
		return 1;
	}
	
	public int getRoomCost() {
		return 1;
	}
	
	public abstract List<String> getRoomTags();
	
	public abstract String getRoomName();
	
	@Override
	public @Nullable Component getDisplayName() {
		return null; // we have names here... could use them... new TextComponent(getRoomName());
	}
}
