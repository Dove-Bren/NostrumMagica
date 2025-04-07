package com.smanzana.nostrummagica.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class NetUtils {

	public static ResourceKey<Level> unpackDimension(FriendlyByteBuf buffer) {
		ResourceLocation loc = buffer.readResourceLocation();
		return DimensionUtils.GetDimKey(loc);
	}
	
	public static FriendlyByteBuf packDimension(FriendlyByteBuf buffer, ResourceKey<Level> dimension) {
		buffer.writeResourceLocation(dimension.location());
		return buffer;
	}
	
	public static void packVec(FriendlyByteBuf buf, @Nullable Vec3 vec) {
		if (vec == null) {
			buf.writeBoolean(false);
		} else {
			buf.writeBoolean(true);
			buf.writeDouble(vec.x);
			buf.writeDouble(vec.y);
			buf.writeDouble(vec.z);
		}
	}
	
	public static @Nullable Vec3 unpackVec(FriendlyByteBuf buf) {
		final Vec3 vec;
		if (buf.readBoolean()) {
			vec = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		} else {
			vec = null;
		}
		
		return vec;
	}
	
	public static Codec<Vec3> CODEC_VECTOR3D = Codec.DOUBLE.listOf().comapFlatMap(NetUtils::Vector3dUnpack, NetUtils::Vector3dPack);
	
	protected static final DataResult<Vec3> Vector3dUnpack(List<Double> values) {
		if (values == null || values.size() != 3) {
			final int count = (values == null ? 0 : values.size());
			return DataResult.error("Require 3 doubles for vector, but only saw " + count);
		}
		
		return DataResult.success(new Vec3(values.get(0), values.get(1), values.get(2)));
	}
	
	protected static final List<Double> Vector3dPack(Vec3 vector) {
		return ImmutableList.of(
				vector.x(),
				vector.y(),
				vector.z()
				);
	}
	
	public static <E extends Enum<E>, T> CompoundTag ToNBT(Map<E, T> map, Function<T, Tag> writer) {
		return ToNBT(map, (E key) -> key.name().toLowerCase(), writer);
	}
	
	public static <E extends Enum<E>, T> Map<E, T> FromNBT(Map<E, T> mapToFill, Class<E> enumClass, CompoundTag tag, Function<Tag, T> reader) {
		return FromNBT(mapToFill, tag, (key) -> Enum.valueOf(enumClass, key.toUpperCase()), reader);
	}
	
	public static <K, V> CompoundTag ToNBT(Map<K, V> map, Function<K, String> keyWriter, Function<V, Tag> valueWriter) {
		CompoundTag tag = new CompoundTag();
		for (Entry<K, V> entry : map.entrySet()) {
			if (entry.getValue() != null) {
				tag.put(keyWriter.apply(entry.getKey()), valueWriter.apply(entry.getValue()));
			}
		}
		return tag;
	}
	
	public static <K, V> Map<K, V> FromNBT(Map<K, V> mapToFill, CompoundTag tag, Function<String, K> keyReader, Function<Tag, V> valueReader) {
		for (String key : tag.getAllKeys()) {
			try {
				K mapKey = keyReader.apply(key);
				mapToFill.put(mapKey, valueReader.apply(tag.get(key)));
			} catch (Exception e) {
				
			}
		}
		return mapToFill;
	}
	
	public static <T> ListTag ToNBT(List<T> list, Function<T, Tag> writer) {
		ListTag tagList = new ListTag();
		for (T elem : list) {
			tagList.add(writer.apply(elem));
		}
		return tagList;
	}
	
	public static <T> List<T> FromNBT(List<T> listToFill, ListTag tagList, Function<Tag, T> reader) {
		for (int i = 0; i < tagList.size(); i++) {
			Tag tag = tagList.get(i);
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
