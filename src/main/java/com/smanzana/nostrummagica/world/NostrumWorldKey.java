package com.smanzana.nostrummagica.world;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.util.NetUtils;
import com.smanzana.nostrummagica.util.PortingUtil;

import net.minecraft.nbt.CompoundNBT;

public class NostrumWorldKey {
	
	private static final String NBT_ID = "key_id";
	;//private static final String NBT_COLOR = "color";
	
	private final UUID id;
	;//private final int color;
	
	public NostrumWorldKey(@Nonnull UUID id) {;//, int colorARGB) {
		this.id = id;
		;//this.color = colorARGB;
	}
	
	public NostrumWorldKey() {
		this(UUID.randomUUID());//, 0xFF000000 | NostrumMagica.rand.nextInt());
	}
	
	/**
	 * Takes another UUID and creates a new, unique key based on this key and the
	 * other ID passed in.
	 * This is intended to be deterministic such that two NostrumWorldKeys with the same underlying
	 * ID can be mutated with the same second id and produce equal new keys.
	 * @param id
	 * @return
	 */
	public NostrumWorldKey mutateWithID(UUID id) {
		return new NostrumWorldKey(NetUtils.CombineUUIDs(this.id, id));
	}
	
	public NostrumWorldKey mutateWithKey(NostrumWorldKey other) {
		return mutateWithID(other.id);
	}
	
	public CompoundNBT asNBT() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putUniqueId(NBT_ID, id);
		;//nbt.putInt(NBT_COLOR, color);
		return nbt;
	}
	
	public static NostrumWorldKey fromNBT(CompoundNBT nbt) {
		UUID id = PortingUtil.readNBTUUID(nbt, NBT_ID);
		;//int color = nbt.getInt(NBT_COLOR);
		return new NostrumWorldKey(id);//, color);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof NostrumWorldKey) {
			NostrumWorldKey other = (NostrumWorldKey) o;
			if (other.id.equals(this.id)) {;// && other.color == this.color) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();// * 37 + Integer.hashCode(color);
	}
	
	@Override
	public String toString() {
		return this.id.toString();// + " - " + this.color;
	}
}