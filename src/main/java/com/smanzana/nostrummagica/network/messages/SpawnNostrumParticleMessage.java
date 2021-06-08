package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server is signalling that some particles should be spawned
 * @author Skyler
 *
 */
public class SpawnNostrumParticleMessage implements IMessage {

	public static class Handler implements IMessageHandler<SpawnNostrumParticleMessage, IMessage> {

		@Override
		public IMessage onMessage(SpawnNostrumParticleMessage message, MessageContext ctx) {
			
			final int particleID = message.tag.getInteger(NBT_PARTICLE_ID);
			final SpawnParams params = SpawnParams.FromNBT(message.tag.getCompoundTag(NBT_PARAMS));
			final NostrumParticles type = NostrumParticles.FromID(particleID);
			
			if (type == null) {
				NostrumMagica.logger.warn("Got particle spawn message with unknown ID");
			} else {
				Minecraft.getMinecraft().addScheduledTask(() -> {
					type.spawn(NostrumMagica.proxy.getPlayer().getEntityWorld(), params);
				});
			}

			return null;
		}
		
	}

	private static final String NBT_PARTICLE_ID = "particle_id";
	private static final String NBT_PARAMS = "params";
	
	protected NBTTagCompound tag;
	
	public SpawnNostrumParticleMessage() {
		tag = new NBTTagCompound();
	}
	
	public SpawnNostrumParticleMessage(NostrumParticles type, SpawnParams params) {
		tag = new NBTTagCompound();
		
		tag.setInteger(NBT_PARTICLE_ID, type.getID());
		tag.setTag(NBT_PARAMS, params.toNBT(null));
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		tag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, tag);
	}

}
