package com.smanzana.nostrummagica.client.particles;

import javax.annotation.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;

public class NostrumParticleData implements IParticleData {
	
	public static final IDeserializer<NostrumParticleData> DESERIALIZER = new IDeserializer<NostrumParticleData>() {

		@Override
		public NostrumParticleData deserialize(ParticleType<NostrumParticleData> particleTypeIn, StringReader reader)
				throws CommandSyntaxException {
			return new NostrumParticleData(particleTypeIn, new SpawnParams(1, 0, 0, 0, .5, 20, 0, new Vec3d(0, 1, 0), Vec3d.ZERO));
		}

		@Override
		public NostrumParticleData read(ParticleType<NostrumParticleData> particleTypeIn, PacketBuffer buffer) {
			SpawnParams params = SpawnParams.FromNBT(buffer.readCompoundTag());
			return new NostrumParticleData(particleTypeIn, params);
		}
		
	};
	
	private final ParticleType<NostrumParticleData> type;
	private final SpawnParams params;
	
	public NostrumParticleData(ParticleType<NostrumParticleData> type, SpawnParams params) {
		this.type = type;
		this.params = params;
	}

	@Override
	public ParticleType<NostrumParticleData> getType() {
		return this.type;
	}
	
	public SpawnParams getParams() {
		return params;
	}

	// note: written NBT is params only, NOT particle type.
	// This compliments serializer's read method.
	// It does NOT work with toNBT() and fromNBT().
	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeCompoundTag(params.toNBT(null));
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getParameters() {
		return Registry.PARTICLE_TYPE.getKey(this.getType()) + " " + "NOT SUPPORTED";
	}
	
	// NBT interface for ease-of-use. Not to be used with write/read vanilla particle system
	public CompoundNBT toNBT(@Nullable CompoundNBT nbt) {
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		nbt.putString("particleType", this.getType().getRegistryName().toString());
		nbt.put("particleData", params.toNBT(null));
		
		return nbt;
	}
	
	// NBT interface for ease-of-use. Not to be used with write/read vanilla particle system
	@SuppressWarnings("unchecked")
	public static @Nullable NostrumParticleData fromNBT(CompoundNBT nbt) {
		String typeName = nbt.getString("particleType");
		ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.tryCreate(typeName));
		ParticleType<NostrumParticleData> particleType;
		try {
			particleType = (ParticleType<NostrumParticleData>) type;
		} catch (Exception e) {
			NostrumMagica.logger.error("Failed to deserialize particle in fromNBT: " + typeName);
			return null;
		}
		
		SpawnParams params = SpawnParams.FromNBT(nbt.getCompound("particleData"));
		return new NostrumParticleData(particleType, params);
	}

}
