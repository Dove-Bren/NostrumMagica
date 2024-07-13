package com.smanzana.nostrummagica.world.dungeon.room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprintRegistry;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprintRegistry.RoomBlueprintRecord;
import com.smanzana.nostrummagica.world.dungeon.LootUtil;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;

/**
 * Room where all blocks are loaded from a file at startup.
 * @author Skyler
 *
 */
public class BlueprintDungeonRoom implements IDungeonRoom {
	
	private final ResourceLocation roomID;
	private final List<BlueprintLocation> chestsRelative;
	private RoomBlueprintRecord _cachedRoom; // Only reason for this still is because dungeons have instances hardcodedin them.
											 // But if they took resource locations and/or wrappers, this isn't needed and only
											 // once instance could exist (in DungeonRoomRegistry) and anything that wanted it
											 // could get it by doing a lookup.
											 // Wrapper could be typed for when start rooms vs non-start rooms are needed, etc.
	
	public BlueprintDungeonRoom(ResourceLocation roomID) {
		this.roomID = roomID;
		
		if (roomID == null) {
			throw new RuntimeException("Blueprint null when creating LoadedRoom. Wrong room name looked up, or too early?");
		}
		
		chestsRelative = new ArrayList<>();
	}
	
	protected RoomBlueprintRecord getRoomRecord() {
		RoomBlueprintRecord current = RoomBlueprintRegistry.instance().getRoomRecord(roomID);
		if (current != this._cachedRoom) {
			this._cachedRoom = current;
			parseRoom(current.blueprint);
		}
		return current;
	}
	
	protected RoomBlueprint getBlueprint() {
		return getRoomRecord().blueprint;
	}
	
	protected void parseRoom(RoomBlueprint blueprint) {
		chestsRelative.clear();
		
		// Find and save chest locations
		blueprint.scanBlocks((offset, block) -> {
			BlockState state = block.getSpawnState(blueprint.getEntry().getFacing()); 
			if (state != null && state.getBlock() == Blocks.CHEST) {
				chestsRelative.add(new BlueprintLocation(offset, state.get(ChestBlock.FACING)));
			}
		});
	}
	
	// Need to have some sort of 'exit point' placeholder block so that I can encode doorways into the blueprint
	
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
		// See note about dungeon vs blueprint facing in @getExits
		getBlueprint().spawn(world, start.getPos(), start.getFacing(), bounds, dungeonID, null);
		
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
		Collection<BlueprintLocation> exits = getBlueprint().getExits();
		return exits == null ? 0 : exits.size();
	}

	@Override
	public List<BlueprintLocation> getExits(BlueprintLocation start) {
		Collection<BlueprintLocation> exits = getBlueprint().getExits();
		
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
		return getBlueprint().getLargeDoorLocation() != null;
	}
	
	@Override
	public BlueprintLocation getDoorLocation(BlueprintLocation start) {
		final RoomBlueprint blueprint = getBlueprint();
		BlueprintLocation orig = blueprint.getLargeDoorLocation();
		return BlueprintToRoom(orig, blueprint.getEntry(), start);
	}

	@Override
	public boolean supportsKey() {
		return !getBlueprint().getLargeKeySpots().isEmpty();
	}

	@Override
	public BlueprintLocation getKeyLocation(BlueprintLocation start) {
		final RoomBlueprint blueprint = getBlueprint();
		BlueprintLocation orig = blueprint.getLargeKeySpots().iterator().next();
		return BlueprintToRoom(orig, blueprint.getEntry(), start);
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
				final BlueprintLocation relative = NostrumDungeon.asRotated(start, orig.getPos(), orig.getFacing().getOpposite()); 
				ret.add(relative);
			}
			return ret;
		}
	}
	
	@Override
	public MutableBoundingBox getBounds(BlueprintLocation entry) {
		final RoomBlueprint blueprint = getBlueprint();
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
		return this.getRoomRecord().id;
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
}
