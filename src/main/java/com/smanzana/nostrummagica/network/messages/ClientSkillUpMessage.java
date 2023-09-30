package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has requested a skillpoint be spent
 * @author Skyler
 *
 */
public class ClientSkillUpMessage {
	
	public static enum Type {
		TECHNIQUE,
		FINESSE,
		CONTROL,
	}

	public static void handle(ClientSkillUpMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			PlayerEntity sp = ctx.get().getSender();
			INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
			
			if (att == null) {
				NostrumMagica.logger.warn("Could not look up player magic wrapper");
				return;
			}
			
			if (att.getSkillPoints() > 0) {
				switch (message.type) {
				case CONTROL:
					att.addControl();
					break;
				case FINESSE:
					att.addFinesse();
					break;
				case TECHNIQUE:
					att.addTech();
					break;
				default: // don't take point when something's wrong!
					return;
				}
				att.takeSkillPoint();
			}
			
			NetworkHandler.sendTo(new StatSyncMessage(att),
					ctx.get().getSender());
		});
	}

	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	private final Type type;
	
	public ClientSkillUpMessage(Type type) {
		this.type = type;
	}

	public static ClientSkillUpMessage decode(PacketBuffer buf) {
		return new ClientSkillUpMessage(buf.readEnumValue(Type.class));
	}

	public static void encode(ClientSkillUpMessage msg, PacketBuffer buf) {
		buf.writeEnumValue(msg.type);
	}

}
