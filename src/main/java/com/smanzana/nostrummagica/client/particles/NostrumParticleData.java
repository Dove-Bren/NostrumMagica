package com.smanzana.nostrummagica.client.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

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

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeCompoundTag(params.toNBT(null));
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getParameters() {
		return Registry.PARTICLE_TYPE.getKey(this.getType()) + " " + "NOT SUPPORTED";
	}

}
