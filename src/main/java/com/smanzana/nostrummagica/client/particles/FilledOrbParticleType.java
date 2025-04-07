package com.smanzana.nostrummagica.client.particles;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class FilledOrbParticleType extends ParticleType<FilledOrbParticleType.Data> {

	public FilledOrbParticleType() {
		super(false, FilledOrbParticleType.Data.DESERIALIZER);
	}
	
	@Override
	public Codec<FilledOrbParticleType.Data> codec() {
		return Data.CODEC;
	}
	
	public static class Factory implements ParticleProvider<FilledOrbParticleType.Data> {
		private final SpriteSet sprite;
		
		public Factory(SpriteSet set) {
			this.sprite = set;
		}
		
		@Override
		public Particle createParticle(FilledOrbParticleType.Data data, ClientLevel world, double x, double y, double z, double mx, double my, double mz) {
			return new FilledOrbParticle(world, x, y, z, data.red, data.green, data.blue, data.alpha, data.lifetime, data.gravity, sprite);
		}
	}
	
	public static class Data implements ParticleOptions {
		public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.FLOAT.fieldOf("size").forGetter(d -> d.size),
					Codec.INT.fieldOf("lifetime").forGetter(d -> d.lifetime),
					Codec.FLOAT.fieldOf("red").forGetter(d -> d.red),
					Codec.FLOAT.fieldOf("green").forGetter(d -> d.green),
					Codec.FLOAT.fieldOf("blue").forGetter(d -> d.blue),
					Codec.FLOAT.fieldOf("alpha").forGetter(d -> d.alpha),
					Codec.FLOAT.fieldOf("gravity").forGetter(d -> d.gravity)
				).apply(instance, Data::new));
		
		public final float size;
		public final int lifetime;
		public final float red, green, blue, alpha;
		public final float gravity;
		
		public Data(float size, int lifetime, float red, float green, float blue, float alpha, float gravity) {
			this.size = size;
			this.lifetime = lifetime;
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.alpha = alpha;
			this.gravity = gravity;
		}
		
		@Override
		public ParticleType<Data> getType() {
			return NostrumParticles.FilledOrb;
		}

		@Override
		public void writeToNetwork(FriendlyByteBuf buf) {
			buf.writeFloat(size);
			buf.writeVarInt(lifetime);
			buf.writeFloat(red);
			buf.writeFloat(green);
			buf.writeFloat(blue);
			buf.writeFloat(alpha);
			buf.writeFloat(gravity);
		}

		@Override
		public String writeToString() {
			return String.format(Locale.ROOT, "%s %.2f %d %.2f %.2f %.2f %.2f %.2f",
					Registry.PARTICLE_TYPE.getKey(this.getType()),
					size, lifetime, red, green, blue, alpha, gravity);
		}
		
		public static final Deserializer<Data> DESERIALIZER = new Deserializer<>() {

			@Override
			public Data fromCommand(ParticleType<Data> type, StringReader reader)
					throws CommandSyntaxException {
				reader.expect(' ');
				float size = reader.readFloat();
				reader.expect(' ');
				int lifetime = reader.readInt();
				reader.expect(' ');
				float red = reader.readFloat();
				reader.expect(' ');
				float green = reader.readFloat();
				reader.expect(' ');
				float blue = reader.readFloat();
				reader.expect(' ');
				float alpha = reader.readFloat();
				reader.expect(' ');
				float gravity = reader.readFloat();
				
				return new Data(size, lifetime, red, green, blue, alpha, gravity);
			}

			@Override
			public Data fromNetwork(ParticleType<Data> type, FriendlyByteBuf buf) {
				return new Data(
						buf.readFloat(),
						buf.readVarInt(),
						buf.readFloat(),
						buf.readFloat(),
						buf.readFloat(),
						buf.readFloat(),
						buf.readFloat()
						);
			}
			
		};
	}
}
