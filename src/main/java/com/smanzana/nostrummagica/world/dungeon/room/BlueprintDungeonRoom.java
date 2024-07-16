package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.DungeonKeyChestBlock;
import com.smanzana.nostrummagica.tile.IUniqueBlueprintTileEntity;
import com.smanzana.nostrummagica.world.blueprints.Blueprint;
import com.smanzana.nostrummagica.world.blueprints.BlueprintBlock;
import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.world.blueprints.BlueprintSpawnContext;
import com.smanzana.nostrummagica.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.world.blueprints.IBlueprintBlockPlacer;
import com.smanzana.nostrummagica.world.dungeon.LootUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;

/**
 * Room where the room structure is a blueprint.
 * @author Skyler
 *
 */
public class BlueprintDungeonRoom implements IDungeonRoom, IDungeonLobbyRoom {
	
	protected static class BlueprintDungeonRoomPlacer implements IBlueprintBlockPlacer {

		private final UUID dungeonID;
		private final UUID roomID;
		
		public BlueprintDungeonRoomPlacer(UUID dungeonID, UUID roomID) {
			this.dungeonID = dungeonID;
			this.roomID = roomID;
		}

		@Override
		public boolean spawnBlock(BlueprintSpawnContext context, BlockPos pos, Direction direction, BlueprintBlock block) {
			return false; // do regular BP spawning
		}
		
		@Override
		public void finalizeBlock(BlueprintSpawnContext context, BlockPos pos, BlockState placedState, @Nullable TileEntity te, Direction direction, BlueprintBlock block) {
			if (te != null) {
				if (te instanceof IUniqueBlueprintTileEntity) {
					((IUniqueBlueprintTileEntity) te).onRoomBlueprintSpawn(dungeonID, roomID, context.isWorldGen);
				}
			}
		}
	}
	
	private final ResourceLocation id;
	private final IBlueprint blueprint;
	private final Set<BlueprintLocation> doors;
	private final List<BlueprintLocation> largeKeySpots;
	private @Nullable BlueprintLocation largeKeyDoor;
	private final List<BlueprintLocation> chestsRelative;
	
	public BlueprintDungeonRoom(ResourceLocation id, Blueprint blueprint) {
		this.id = id;
		this.blueprint = blueprint;
		this.doors = new HashSet<>();
		this.largeKeySpots = new ArrayList<>();
		this.largeKeyDoor = null;
		this.chestsRelative = new ArrayList<>();
		
		blueprint.scanBlocks(this::parseRoom);
	}
	
	// TODO: move these out of here
	public static boolean IsDoorIndicator(BlockState state) {
		return state != null && state.getBlock() == Blocks.REPEATER;
	}
	
	public static boolean IsEntry(BlockState state) {
		return state != null && state.getBlock() == Blocks.COMPARATOR;
	}
	
	public static boolean IsLargeKeySpot(BlockState state) {
		return state != null
				&& state.getBlock() == NostrumBlocks.largeDungeonKeyChest
				&& !state.get(DungeonKeyChestBlock.Large.SLAVE);
	}
	
	public static boolean IsLargeKeyDoor(BlockState state) {
		return state != null
				&& state.getBlock() == NostrumBlocks.largeDungeonDoor
				&& NostrumBlocks.largeDungeonDoor.isMaster(state);
	}
	
	private static boolean debugConnections = false;

	protected BlueprintBlock parseRoom(BlockPos offset, BlueprintBlock block) {
		BlockState state = block.getSpawnState(blueprint.getEntry().getFacing()); 
		if (state != null && state.getBlock() == Blocks.CHEST) {
			chestsRelative.add(new BlueprintLocation(offset, state.get(ChestBlock.FACING)));
		}
		
		if (IsDoorIndicator(block.getState())) {
			doors.add(new BlueprintLocation(offset, block.getFacing().getOpposite()));
			if (!debugConnections) {
				block = BlueprintBlock.Air; // Make block an air one
			}
		} else if (IsEntry(block.getState())) {
			if (!debugConnections && offset.equals(BlockPos.ZERO)) {
				// Clear out any comparator that's there from capturing still
				block = BlueprintBlock.Air;
			}
		} else if (IsLargeKeySpot(block.getState())) {
			largeKeySpots.add(new BlueprintLocation(offset, block.getFacing()));
			if (!debugConnections) {
				block = BlueprintBlock.Air; // Make block into an air one. Could make it a chest...
			}
		} else if (IsLargeKeyDoor(block.getState())) {
			if (this.largeKeyDoor != null) {
				NostrumMagica.logger.error("Found multiple large dungeon doors in room while parsing blueprint!");
			}
			this.largeKeyDoor = new BlueprintLocation(offset, block.getFacing());
		}
		
		return block;
	}
	
	protected IBlueprint getBlueprint() {
		return blueprint;
	}
	
	@Override
	public boolean canSpawnAt(IWorld world, BlueprintLocation start) {
		BlockPos dims = getBlueprint().getAdjustedDimensions(start.getFacing());
		BlockPos offset = getBlueprint().getAdjustedOffset(start.getFacing());
		
		int minX = start.getPos().getX() - offset.getX();
		int minY = start.getPos().getY() - offset.getY();
		int minZ = start.getPos().getZ() - offset.getZ();
		int maxX = minX + dims.getX();
		int maxY = minY + dims.getY();
		int maxZ = minZ + dims.getZ();
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
		final BlueprintDungeonRoomPlacer placer = new BlueprintDungeonRoomPlacer(UUID.randomUUID(), dungeonID);
		getBlueprint().spawn(world, start.getPos(), start.getFacing(), bounds, placer);

		// See note about dungeon vs blueprint facing in @getExits
		List<BlueprintLocation> loots = this.getTreasureLocations(start);
		if (loots != null && !loots.isEmpty())
		for (BlueprintLocation lootSpot : loots) {
			if (bounds != null && !bounds.isVecInside(lootSpot.getPos())) {
				continue; // Will come back for you later <3
			}
			
			// Dungeon generation may replace some chests with other things.
			// Make sure it's still a chest.
			// TODO improve this, especially since rooms are part of dungeongen not blueprints
			if (world.getBlockState(lootSpot.getPos()).getBlock() instanceof ChestBlock) {
				LootUtil.generateLoot(world, lootSpot.getPos(), lootSpot.getFacing());
			}
		}
	}

	@Override
	public int getNumExits() {
		return doors.size();
	}

	@Override
	public List<BlueprintLocation> getExits(BlueprintLocation start) {
		Collection<BlueprintLocation> exits = doors;
		
		// Dungeon notion of direction is backwards to blueprints:
		// Dungeon wants facing to be you looking back through the door
		// Blueprint wants your facing as you go in the door. That's there the 'opposite' comes from.
		
		// Blueprint exits are rotated to the entry entry direction (and have their own rotation too).
		//final Direction modDir = IBlueprint.GetModDir(blueprint.getEntry().getFacing(), start.getFacing());
		// Door offset and final rotation is what's in exits rotated modDir times
		
		List<BlueprintLocation> ret;
		if (exits != null) {
			ret = new ArrayList<>(exits.size());
			for (BlueprintLocation door : exits) {
				
//				final BlueprintLocation relative = NostrumDungeon.asRotated(start, door.getPos(), door.getFacing()); 
//				ret.add(relative);
				
				ret.add(BlueprintToRoom(door, getBlueprint().getEntry(), start));
				
				
				
				
//				Direction doorDir = door.getFacing();
//				int times = (modDir.getHorizontalIndex() + 2) % 4;
//				while (times-- > 0) {
//					doorDir = doorDir.rotateY();
//				}
//				final DungeonExitPoint fromEntry = new DungeonExitPoint(
//						IBlueprint.ApplyRotation(door.getPos(), modDir),
//						doorDir
//						);
//				final DungeonExitPoint relative = new DungeonExitPoint(start.getPos().add(fromEntry.getPos()), fromEntry.getFacing()); 
//				ret.add(relative);
			}
		} else {
			ret = new LinkedList<>();
		}
		return ret;
	}

	@Override
	public int getDifficulty() {
		return 1;
	}

	@Override
	public boolean supportsDoor() {
		return largeKeyDoor != null;
	}
	
	@Override
	public BlueprintLocation getDoorLocation(BlueprintLocation start) {
		return BlueprintToRoom(largeKeyDoor, blueprint.getEntry(), start);
		//return NostrumDungeon.asRotated(start, largeKeyDoor.getPos(), largeKeyDoor.getFacing());
	}

	@Override
	public boolean supportsKey() {
		return !largeKeySpots.isEmpty();
	}

	@Override
	public BlueprintLocation getKeyLocation(BlueprintLocation start) {
		BlueprintLocation orig = largeKeySpots.get(0);
		return BlueprintToRoom(orig, blueprint.getEntry(), start);
		//return NostrumDungeon.asRotated(start, orig.getPos(), orig.getFacing());
	}
	
	@Override
	public boolean supportsTreasure() {
		return !chestsRelative.isEmpty();
	}

	@Override
	public List<BlueprintLocation> getTreasureLocations(BlueprintLocation start) {
//		// See note about dungeon vs blueprint facing in @getExits
//		
//		// Blueprint exits are rotated to the entry entry direction (and have their own rotation too).
//		final Direction modDir = RoomBlueprint.getModDir(blueprint.getEntry().getFacing().getOpposite(), start.getFacing());
//		// Door offset and final rotation is what's in exits rotated modDir times
//		
//		List<DungeonExitPoint> ret;
//		if (chestsRelative != null) {
//			ret = new ArrayList<>(chestsRelative.size());
//			for (DungeonExitPoint chest : chestsRelative) {
//				Direction chestDir = chest.getFacing();
//				int times = (modDir.getHorizontalIndex() + 2) % 4;
//				while (times-- > 0) {
//					chestDir = chestDir.rotateY();
//				}
//				final DungeonExitPoint fromEntry = new DungeonExitPoint(
//						RoomBlueprint.applyRotation(chest.getPos(), modDir),
//						chestDir
//						);
//				final DungeonExitPoint relative = new DungeonExitPoint(start.getPos().add(fromEntry.getPos()), fromEntry.getFacing()); 
//				ret.add(relative);
//			}
//		} else {
//			ret = new LinkedList<>();
//		}
//		return ret;
		
		
		
		{
			List<BlueprintLocation> ret = new ArrayList<>();
			for (BlueprintLocation orig : chestsRelative) {
				//final BlueprintLocation relative = NostrumDungeon.asRotated(start, orig.getPos(), orig.getFacing().getOpposite()); 
				//ret.add(relative);
				ret.add(BlueprintToRoom(orig, blueprint.getEntry(), start));
			}
			return ret;
		}
	}
	
	@Override
	public MutableBoundingBox getBounds(BlueprintLocation entry) {
		final IBlueprint blueprint = getBlueprint();
		BlockPos dims = blueprint.getAdjustedDimensions(entry.getFacing());
		BlockPos offset = blueprint.getAdjustedOffset(entry.getFacing());
		
		int minX = entry.getPos().getX() - offset.getX();
		int minY = entry.getPos().getY() - offset.getY();
		int minZ = entry.getPos().getZ() - offset.getZ();
		int maxX = minX + (dims.getX()-(int) Math.signum(dims.getX()));
		int maxY = minY + (dims.getY()-(int) Math.signum(dims.getY()));
		int maxZ = minZ + (dims.getZ()-(int) Math.signum(dims.getZ()));
		
		// Have to figure out real min/max ourselves
		return new MutableBoundingBox(
				Math.min(minX, maxX),
				Math.min(minY, maxY),
				Math.min(minZ, maxZ),
				Math.max(maxX, minX),
				Math.max(maxY, minY),
				Math.max(maxZ, minZ));
	}

	@Override
	public boolean hasEnemies() {
		return true;
	}

	@Override
	public boolean hasTraps() {
		return true;
	}
	
	@Override
	public ResourceLocation getRoomID() {
		return id;
	}
	
	protected static final BlueprintLocation BlueprintToRoom(BlueprintLocation blueprintPoint, BlueprintLocation blueprintEntry, BlueprintLocation start) {
		BlueprintLocation orig = blueprintPoint;
		
		// Dungeon notion of direction is backwards to blueprints:
		// Dungeon wants facing to be you looking back through the door
		// Blueprint wants your facing as you go in the door. That's there the 'opposite' comes from.
		
		// Blueprint exits are rotated to the entry entry direction (and have their own rotation too).
		final Direction modDir = IBlueprint.GetModDir(blueprintEntry.getFacing(), start.getFacing());
		// Door offset and final rotation is what's in exits rotated modDir times
				
		Direction doorDir = orig.getFacing();
		int times = (modDir.getHorizontalIndex() + 2) % 4;
		while (times-- > 0) {
			doorDir = doorDir.rotateY();
		}
		final BlueprintLocation fromEntry = new BlueprintLocation(
				IBlueprint.ApplyRotation(orig.getPos(), modDir),
				doorDir
				);
		return new BlueprintLocation(start.getPos().add(fromEntry.getPos()), fromEntry.getFacing()); 
	}

	// This is a bit hacky, but all loaded rooms can be 'lobby' rooms in that the entry should be where the stairs go.
	@Override
	public Vector3i getStairOffset() {
		return Vector3i.NULL_VECTOR;
	}
}
