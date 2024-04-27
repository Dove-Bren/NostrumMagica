package com.smanzana.nostrummagica.utils;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

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
	
}
