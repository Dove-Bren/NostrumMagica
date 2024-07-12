package com.smanzana.nostrummagica.world.dungeon;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonInstance;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonRoomInstance;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public final class DungeonRecord {
	public final @Nonnull NostrumDungeonStructure structure;
	public final @Nonnull DungeonInstance instance;
	public final DungeonRoomInstance currentRoom;
//	public final List<DungeonRoomInstance> rooms;
	
	public DungeonRecord(NostrumDungeonStructure structure, DungeonInstance instance, DungeonRoomInstance currentRoom) {
		this.structure = structure;
		this.instance = instance;
		this.currentRoom = currentRoom;
//		this.rooms = new ArrayList<>(rooms);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.structure, this.instance, this.currentRoom);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof DungeonRecord) {
			DungeonRecord other = (DungeonRecord) o;
			return other.instance.equals(instance)
					&& structure.equals(other.structure)
					&& Objects.equals(this.currentRoom, other.currentRoom)
					//&& rooms.equals(other.rooms)
					;
		}
		return false;
	}

	private static final String NBT_STRUCTURE = "structure";
	private static final String NBT_INSTANCE = "instance";
	//private static final String NBT_ROOMS = "rooms";
	private static final String NBT_CURRENT_ROOM = "current_room";
	
	public CompoundNBT toNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.putString(NBT_STRUCTURE, structure.getStructureName().toString());
		tag.put(NBT_INSTANCE, instance.toNBT());
		//tag.put(NBT_ROOMS, NetUtils.ToNBT(rooms, (r) -> r.toNBT(null)));
		tag.put(NBT_CURRENT_ROOM, currentRoom.toNBT(null));
		return tag;
	}
	
	public static final DungeonRecord FromNBT(CompoundNBT nbt) {
		
		final @Nullable DungeonInstance instance = DungeonInstance.FromNBT(nbt.get(NBT_INSTANCE));
		@SuppressWarnings("deprecation")
		final NostrumDungeonStructure structure = (NostrumDungeonStructure) Registry.STRUCTURE_FEATURE.getOptionalValue(
				RegistryKey.getOrCreateKey(Registry.STRUCTURE_FEATURE_KEY, new ResourceLocation(nbt.getString(NBT_STRUCTURE))))
				.orElseThrow(() -> new RuntimeException("Failed to look up structure with key " + nbt.getString(NBT_STRUCTURE)));
		//final List<DungeonRoomInstance> rooms = NetUtils.FromNBT(new ArrayList<DungeonRoomInstance>(), (ListNBT) nbt.get(NBT_ROOMS), (tag) -> DungeonRoomInstance.fromNBT((CompoundNBT) tag));
		final DungeonRoomInstance room = DungeonRoomInstance.fromNBT(nbt.getCompound(NBT_CURRENT_ROOM));
		
		return new DungeonRecord(structure, instance, room);
	}
}