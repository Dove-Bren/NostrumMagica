package com.smanzana.nostrummagica.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.ResourceLocation;

/**
 * A near clone of JsonReloadListener except with NBT
 * @author Skyler
 *
 */
public abstract class NBTReloadListener extends AutoReloadListener<Map<ResourceLocation, CompoundNBT>> {
	
		private final boolean compressed;
	
	public NBTReloadListener(String folder, String extension, boolean compressed) {
		super(folder, extension);
		this.compressed = compressed;
	}
	
	@Override
	protected Map<ResourceLocation, CompoundNBT> prepareResource(Map<ResourceLocation, CompoundNBT> builder, ResourceLocation location, InputStream input) throws IOException, IllegalStateException {
		if (builder == null) {
			builder = new HashMap<>();
		}
		
		final CompoundNBT existing;
		if (compressed) {
			existing = builder.put(location, CompressedStreamTools.readCompressed(input));
		} else {
			existing = builder.put(location, CompressedStreamTools.read(new DataInputStream(input)));
		}
		
		if (existing != null) {
			throw new IllegalStateException("Duplicate data file ignored with ID " + location);
		}
		
		return builder;
	}
}
