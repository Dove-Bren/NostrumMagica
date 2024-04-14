package com.smanzana.nostrummagica.utils;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
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
	
}
