package com.smanzana.nostrummagica.world.blueprints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.IUniqueBlueprintTileEntity;
import com.smanzana.nostrummagica.util.NetUtils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Contains all the data needed to spawn a room in the world
 * @author Skyler
 *
 */
public class RoomBlueprint extends Blueprint {
	
	public static class RoomSpawnContext extends Blueprint.SpawnContext {
		
		public final UUID globalID;
		public final UUID roomID;
		
		public RoomSpawnContext(IWorld world, BlockPos pos, Direction direction, @Nullable MutableBoundingBox bounds, UUID globalID, UUID roomID, @Nullable IBlueprintBlockPlacer placer) {
			super(world, pos, direction, bounds, placer);
			this.globalID = globalID;
			this.roomID = roomID;
		}
		
		public RoomSpawnContext(IWorld world, BlockPos pos, Direction direction, @Nullable MutableBoundingBox bounds, UUID globalID, UUID roomID) {
			this(world, pos, direction, bounds, globalID, roomID, null);
		}
		
		public RoomSpawnContext(IWorld world, BlockPos pos, Direction direction, @Nullable MutableBoundingBox bounds, UUID globalID) {
			this(world, pos, direction, bounds, globalID, UUID.randomUUID());
		}
	}
	
	protected static class RoomCaptureContext extends Blueprint.CaptureContext {
		public final List<BlueprintLocation> doorsRaw;
		public final List<BlueprintLocation> keysRaw;
		public BlueprintLocation largeDoorRaw;
		
		public RoomCaptureContext(BlockPos pos1, BlockPos pos2, BlueprintLocation origin) {
			super(pos1, pos2, origin);
			doorsRaw = new ArrayList<>();
			keysRaw = new ArrayList<>();
			largeDoorRaw = null;
		}
	}
	
	private static final String NBT_DOOR_LIST = "doors";
	private static final String NBT_KEYS = "large_keys";
	private static final String NBT_DUNGEON_DOOR = "dungeon_door";
	
	private final Set<BlueprintLocation> doors;
	private final List<BlueprintLocation> largeKeySpots;
	private @Nullable BlueprintLocation largeKeyDoor;
	
	public RoomBlueprint(BlockPos dimensions, BlueprintBlock[] blocks, Set<BlueprintLocation> exits, BlueprintLocation entry, List<BlueprintLocation> largeKeySpots,
			@Nullable BlueprintLocation largeKeyDoor) {
		super(dimensions, blocks, entry);
		this.doors = exits == null ? new HashSet<>() : exits;
		this.largeKeySpots = largeKeySpots == null ? new ArrayList<>() : largeKeySpots;
		this.largeKeyDoor = largeKeyDoor;
	}
	
	public static RoomBlueprint Capture(IWorld world, BlockPos pos1, BlockPos pos2, @Nullable BlueprintLocation origin) {
		RoomBlueprint blueprint = new RoomBlueprint(null, null, null, null, null, null);
		blueprint.capture(world, pos1, pos2, origin);
		return blueprint;
	}
	
	@Override
	protected RoomCaptureContext makeCaptureContext(IWorld world, BlockPos pos1, BlockPos pos2, @Nullable BlueprintLocation origin) {
		return new RoomCaptureContext(pos1, pos2, origin);
	}
	
	@Override
	protected BlueprintBlock captureBlock(CaptureContext contextIn, IWorld world, BlockPos pos) {
		RoomCaptureContext context = (RoomCaptureContext) contextIn;
		BlueprintBlock block = super.captureBlock(context, world, pos);
		
		if (block.isDoorIndicator()) {
			context.doorsRaw.add(new BlueprintLocation(pos.toImmutable().subtract(context.pos1), block.getFacing().getOpposite()));
			block = BlueprintBlock.Air(); // Make block an air one
		} else if (block.isEntry()) {
			if (this.entry != null) {
				NostrumMagica.logger.error("Found multiple entry points to room while creating blueprint!");
			}
			this.entry = new BlueprintLocation(pos.toImmutable().subtract(context.pos1), block.getFacing());
			block = BlueprintBlock.Air(); // Make block an air one
		} else if (block.isLargeKeySpot()) {
			context.keysRaw.add(new BlueprintLocation(pos.toImmutable().subtract(context.pos1), block.getFacing()));
			block = BlueprintBlock.Air(); // Make block into an air one. Could make it a chest...
		} else if (block.isLargeKeyDoor()) {
			if (context.largeDoorRaw != null) {
				NostrumMagica.logger.error("Found multiple large dungeon doors in room while creating blueprint!");
			}
			context.largeDoorRaw = new BlueprintLocation(pos.toImmutable().subtract(context.pos1), block.getFacing());
		}
		
		return block;
	}
	
	@Override
	protected void finishCapture(CaptureContext contextIn) {
		RoomCaptureContext context = (RoomCaptureContext) contextIn;
		// Adjust found doors/chests/etc. to be offsets from entry
		if (entry != null) {
			for (BlueprintLocation door : context.doorsRaw) {
				doors.add(new BlueprintLocation(
						door.getPos().subtract(entry.getPos()),
						door.getFacing()
						));
			}
			for (BlueprintLocation keySpot : context.keysRaw) {
				largeKeySpots.add(new BlueprintLocation(
						keySpot.getPos().subtract(entry.getPos()),
						keySpot.getFacing()
						));
			}
			if (context.largeDoorRaw != null) {
				this.largeKeyDoor = new BlueprintLocation(
						context.largeDoorRaw.getPos().subtract(entry.getPos()),
						context.largeDoorRaw.getFacing()
						);
			}
		}
	}
	
	@Override
	protected void capture(IWorld world, BlockPos pos1, BlockPos pos2, @Nullable BlueprintLocation origin) {
		super.capture(world, pos1, pos2, origin);
	}
	
	@Override
	protected void fixupTileEntity(TileEntity te, Direction direction, SpawnContext contextIn, boolean worldGen) {
		RoomSpawnContext context = (RoomSpawnContext) contextIn;
		super.fixupTileEntity(te, direction, context, worldGen);
		
		if (te instanceof IUniqueBlueprintTileEntity) {
			((IUniqueBlueprintTileEntity) te).onRoomBlueprintSpawn(context.globalID, context.roomID, worldGen);
		}
	}
	
	@Override
	@Deprecated
	public void spawn(IWorld world, BlockPos at) {
		super.spawn(world, at, Direction.NORTH);
	}
	
	@Override
	@Deprecated
	public void spawn(IWorld world, BlockPos at, Direction direction) {
		super.spawn(world, at, direction);
	}
	
	@Override
	@Deprecated
	public void spawn(IWorld world, BlockPos at, Direction direction, @Nullable MutableBoundingBox bounds, @Nullable IBlueprintBlockPlacer placer) {
		spawn(world, at, direction, bounds, placer, UUID.randomUUID());
	}
	
	public void spawn(IWorld world, BlockPos at, Direction direction, @Nullable MutableBoundingBox bounds, @Nullable IBlueprintBlockPlacer placer, UUID globalID) {
		RoomSpawnContext context = new RoomSpawnContext(world, at, direction, bounds, globalID, UUID.randomUUID(), placer);
		this.spawnWithContext(context);
	}
	
	public Collection<BlueprintLocation> getExits() {
		return this.doors;
	}
	
	public Collection<BlueprintLocation> getLargeKeySpots() {
		return this.largeKeySpots;
	}
	
	public @Nullable BlueprintLocation getLargeDoorLocation() {
		return this.largeKeyDoor;
	}
	
	@Override
	public RoomBlueprint join(Blueprint blueprint) {
		if (!(blueprint instanceof RoomBlueprint)) {
			throw new IllegalArgumentException("Cannot join room blueprints to non-room blueprints");
		}
		
		RoomBlueprint joinedRoom = (RoomBlueprint) super.join(blueprint);
		
		// This is where we'd absorb any exits, keys, etc. from the room being joined to this one.
		// For now, all of that stuff stays in the parent so nothing to pull in.
		
		return joinedRoom;
	}
	
	protected RoomBlueprint(LoadContext context, CompoundNBT nbt) {
		super(context, nbt);
		
		Set<BlueprintLocation> doors = null;
		List<BlueprintLocation> keys = null;
		BlueprintLocation largeDoor = null;
		
		ListNBT list = nbt.getList(NBT_DOOR_LIST, NBT.TAG_COMPOUND);
		doors = new HashSet<>();
		int listCount = list.size();
		for (int i = 0; i < listCount; i++) {
			CompoundNBT tag = (CompoundNBT) list.getCompound(i);
			BlueprintLocation door;
			door = BlueprintLocation.fromNBT(tag);
			doors.add(door);
		}
		
		if (nbt.contains(NBT_DUNGEON_DOOR)) {
			largeDoor = BlueprintLocation.fromNBT(nbt.getCompound(NBT_DUNGEON_DOOR));
		}
		
		if (nbt.contains(NBT_KEYS, NBT.TAG_LIST)) {
			keys = new ArrayList<>();
			NetUtils.FromNBT(keys, (ListNBT) nbt.get(NBT_KEYS), subtag -> BlueprintLocation.fromNBT((CompoundNBT) subtag));
		}
		
		this.doors = doors;
		this.largeKeyDoor = largeDoor;
		this.largeKeySpots = keys == null ? new ArrayList<>() : keys;
	}
	
	public static RoomBlueprint FromNBT(LoadContext context, CompoundNBT nbt) {
		return new RoomBlueprint(context, nbt);
	}
	
	// Version 4
	protected CompoundNBT toNBTInternal(int startIdx, int count) {
		CompoundNBT nbt = toNBTInternal(startIdx, count);
		
		// First blueprint (when splitting) has all the extra pieces
		if (startIdx == 0) {
			if (this.doors != null && !this.doors.isEmpty()) {
				ListNBT list = new ListNBT();
				for (BlueprintLocation door : doors) {
					list.add(door.toNBT());
				}
				nbt.put(NBT_DOOR_LIST, list);
			}
			
			nbt.put(NBT_KEYS, NetUtils.ToNBT(this.largeKeySpots, s -> s.toNBT()));
			
			if (this.largeKeyDoor != null) {
				nbt.put(NBT_DUNGEON_DOOR, this.largeKeyDoor.toNBT());
			}
		}
		
		return nbt;
	}
}
