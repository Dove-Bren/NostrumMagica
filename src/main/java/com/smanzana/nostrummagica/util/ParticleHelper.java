package com.smanzana.nostrummagica.util;

import javax.annotation.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticleData;

import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleOptions;

public final class ParticleHelper {
	
	private static final String NBT_WRAPPER_TYPE = "type";
	private static final String NBT_WRAPPER_TYPE_NOSTRUM = "nostrum";
	private static final String NBT_WRAPPER_TYPE_VANILLA = "vanilla";
	private static final String NBT_WRAPPER_DATA = "data";

	public static final CompoundTag WriteToNBT(ParticleOptions data) {
		CompoundTag nbt = new CompoundTag();
		
		if (data instanceof NostrumParticleData) {
			// ease of use NBT helpers
			nbt.putString(NBT_WRAPPER_TYPE, NBT_WRAPPER_TYPE_NOSTRUM);
			nbt.put(NBT_WRAPPER_DATA, ((NostrumParticleData) data).toNBT(null));
		} else {
			// Vanilla uses the cmd/string interface...
			nbt.putString(NBT_WRAPPER_TYPE, NBT_WRAPPER_TYPE_VANILLA);
			nbt.putString(NBT_WRAPPER_DATA, data.writeToString());
		}
		
		return nbt;
	}
	
	public static final @Nullable ParticleOptions ReadFromNBT(CompoundTag nbt) {
		ParticleOptions data = null;
		final String type = nbt.getString(NBT_WRAPPER_TYPE);
		
		if (type.equalsIgnoreCase(NBT_WRAPPER_TYPE_NOSTRUM)) {
			data = NostrumParticleData.fromNBT(nbt.getCompound(NBT_WRAPPER_DATA));
		} else if (type.equalsIgnoreCase(NBT_WRAPPER_TYPE_VANILLA)) {
			try {
				// Copied from AreaEffectCloudEntity
				data = ParticleArgument.readParticle(new StringReader(nbt.getString(NBT_WRAPPER_DATA)));
			} catch (CommandSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			NostrumMagica.logger.warn("Failed to deserialize particle wrapper with type " + type);
		}
		
		return data;
	}
	
}
