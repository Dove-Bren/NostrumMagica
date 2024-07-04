package com.smanzana.nostrummagica.world.dungeon;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonInstance;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public final class DungeonRecord {
	public final @Nonnull NostrumDungeonStructure structure;
	public final @Nonnull DungeonInstance instance;
	
	public DungeonRecord(NostrumDungeonStructure structure, DungeonInstance instance) {
		this.structure = structure;
		this.instance = instance;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.structure, this.instance);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof DungeonRecord) {
			DungeonRecord other = (DungeonRecord) o;
			return other.instance.equals(instance)
					&& structure.equals(other.structure);
		}
		return false;
	}

	private static final String NBT_STRUCTURE = "structure";
	private static final String NBT_INSTANCE = "instance";
	
	public CompoundNBT toNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.putString(NBT_STRUCTURE, structure.getStructureName().toString());
		tag.put(NBT_INSTANCE, instance.toNBT());
		return tag;
	}
	
	public static final DungeonRecord FromNBT(CompoundNBT nbt) {
		
		final @Nullable DungeonInstance instance = DungeonInstance.FromNBT(nbt.get(NBT_INSTANCE));
		@SuppressWarnings("deprecation")
		final NostrumDungeonStructure structure = (NostrumDungeonStructure) Registry.STRUCTURE_FEATURE.getOptionalValue(
				RegistryKey.getOrCreateKey(Registry.STRUCTURE_FEATURE_KEY, new ResourceLocation(nbt.getString(NBT_STRUCTURE))))
				.orElseThrow(() -> new RuntimeException("Failed to look up structure with key " + nbt.getString(NBT_STRUCTURE)));
		
		return new DungeonRecord(structure, instance);
	}
}