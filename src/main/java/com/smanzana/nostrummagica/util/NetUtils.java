package com.smanzana.nostrummagica.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class NetUtils {

	public static RegistryKey<World> unpackDimension(PacketBuffer buffer) {
		ResourceLocation loc = buffer.readResourceLocation();
		return DimensionUtils.GetDimKey(loc);
	}
	
	public static PacketBuffer packDimension(PacketBuffer buffer, RegistryKey<World> dimension) {
		buffer.writeResourceLocation(dimension.getLocation());
		return buffer;
	}
	
	public static Codec<Vector3d> CODEC_VECTOR3D = Codec.DOUBLE.listOf().comapFlatMap(NetUtils::Vector3dUnpack, NetUtils::Vector3dPack);
	
	protected static final DataResult<Vector3d> Vector3dUnpack(List<Double> values) {
		if (values == null || values.size() != 3) {
			final int count = (values == null ? 0 : values.size());
			return DataResult.error("Require 3 doubles for vector, but only saw " + count);
		}
		
		return DataResult.success(new Vector3d(values.get(0), values.get(1), values.get(2)));
	}
	
	protected static final List<Double> Vector3dPack(Vector3d vector) {
		return ImmutableList.of(
				vector.getX(),
				vector.getY(),
				vector.getZ()
				);
	}
	
	public static <E extends Enum<E>, T> CompoundNBT ToNBT(Map<E, T> map, Function<T, INBT> writer) {
		return ToNBT(map, (E key) -> key.name().toLowerCase(), writer);
	}
	
	public static <E extends Enum<E>, T> Map<E, T> FromNBT(Map<E, T> mapToFill, Class<E> enumClass, CompoundNBT tag, Function<INBT, T> reader) {
		return FromNBT(mapToFill, tag, (key) -> Enum.valueOf(enumClass, key.toUpperCase()), reader);
	}
	
	public static <K, V> CompoundNBT ToNBT(Map<K, V> map, Function<K, String> keyWriter, Function<V, INBT> valueWriter) {
		CompoundNBT tag = new CompoundNBT();
		for (Entry<K, V> entry : map.entrySet()) {
			if (entry.getValue() != null) {
				tag.put(keyWriter.apply(entry.getKey()), valueWriter.apply(entry.getValue()));
			}
		}
		return tag;
	}
	
	public static <K, V> Map<K, V> FromNBT(Map<K, V> mapToFill, CompoundNBT tag, Function<String, K> keyReader, Function<INBT, V> valueReader) {
		for (String key : tag.keySet()) {
			try {
				K mapKey = keyReader.apply(key);
				mapToFill.put(mapKey, valueReader.apply(tag.get(key)));
			} catch (Exception e) {
				
			}
		}
		return mapToFill;
	}
	
	public static <T> ListNBT ToNBT(List<T> list, Function<T, INBT> writer) {
		ListNBT tagList = new ListNBT();
		for (T elem : list) {
			tagList.add(writer.apply(elem));
		}
		return tagList;
	}
	
	public static <T> List<T> FromNBT(List<T> listToFill, ListNBT tagList, Function<INBT, T> reader) {
		for (int i = 0; i < tagList.size(); i++) {
			INBT tag = tagList.get(i);
			listToFill.add(reader.apply(tag));
		}
		return listToFill;
	}
	
	public static final UUID CombineUUIDs(UUID left, UUID right) {
		final long most = left.getMostSignificantBits() ^ right.getMostSignificantBits();
		final long least = left.getLeastSignificantBits() ^ right.getLeastSignificantBits();
		return new UUID(least, most);
	}
	
	/**
	 * Weaker version of {@link UUID#randomUUID()} that uses the provided random number provider
	 * instead of a default cryptographically-secure one.
	 * @param rand
	 * @return
	 */
	public static final UUID RandomUUID(Random rand) {
		return new UUID(
				rand.nextLong(),
				rand.nextLong()
				);
	}
	
}
