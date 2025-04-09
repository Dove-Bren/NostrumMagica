package com.smanzana.nostrummagica.client.particles;

import javax.annotation.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class NostrumParticleData implements ParticleOptions {
	
	@SuppressWarnings("deprecation")
	public static final Deserializer<NostrumParticleData> DESERIALIZER = new Deserializer<NostrumParticleData>() {

		@Override
		public NostrumParticleData fromCommand(ParticleType<NostrumParticleData> particleTypeIn, StringReader reader)
				throws CommandSyntaxException {
			return new NostrumParticleData(particleTypeIn, new SpawnParams(1, 0, 0, 0, .5, 20, 0, new Vec3(0, 1, 0), Vec3.ZERO));
		}

		@Override
		public NostrumParticleData fromNetwork(ParticleType<NostrumParticleData> particleTypeIn, FriendlyByteBuf buffer) {
			SpawnParams params = SpawnParams.FromNBT(buffer.readNbt());
			return new NostrumParticleData(particleTypeIn, params);
		}
		
	};
	
	
	// Note: This sucks. BUT apparently it's only for if you were to try and use the particle in a biome.
	// It sucks that we have to have the particle_type be in here since it's outside of these params too (which is how
	// the vanilla deserialization engine found this CODEC) but whatever.
	public static final Codec<NostrumParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("particle_type").forGetter((d) -> d.getType().getRegistryName()),
			SpawnParams.CODEC.fieldOf("params").forGetter(NostrumParticleData::getParams)
		).apply(instance, NostrumParticleData::Load));
	
	@SuppressWarnings("unchecked")
	private static final NostrumParticleData Load(ResourceLocation name, SpawnParams params) {
		final ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(name);
		return new NostrumParticleData((ParticleType<NostrumParticleData>) type, params);
	}
	
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
	public void writeToNetwork(FriendlyByteBuf buffer) {
		buffer.writeNbt(params.toNBT(null));
	}

	@SuppressWarnings("deprecation")
	@Override
	public String writeToString() {
		return Registry.PARTICLE_TYPE.getKey(this.getType()) + " " + "NOT SUPPORTED";
	}
	
	// NBT interface for ease-of-use. Not to be used with write/read vanilla particle system
	public CompoundTag toNBT(@Nullable CompoundTag nbt) {
		if (nbt == null) {
			nbt = new CompoundTag();
		}
		
		nbt.putString("particleType", this.getType().getRegistryName().toString());
		nbt.put("particleData", params.toNBT(null));
		
		return nbt;
	}
	
	// NBT interface for ease-of-use. Not to be used with write/read vanilla particle system
	@SuppressWarnings("unchecked")
	public static @Nullable NostrumParticleData fromNBT(CompoundTag nbt) {
		String typeName = nbt.getString("particleType");
		ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.tryParse(typeName));
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
