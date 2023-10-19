package com.smanzana.nostrummagica.network.messages;


import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is sending a new copy of the mana armor capability
 * @author Skyler
 *
 */
public class ManaArmorSyncMessage {

	public static void handle(ManaArmorSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
		//update local attributes
		ctx.get().setPacketHandled(true);
		NostrumMagica.logger.info("Recieved Mana Armor sync message from server");
		
		Minecraft.getInstance().runAsync(() -> {
			final Minecraft mc = Minecraft.getInstance();
			@Nullable Entity ent = mc.player.getEntityWorld().getEntityByID(message.entID);
			if (ent != null) {
				NostrumMagica.instance.proxy.receiveManaArmorOverride(ent, message.stats);
			}
		});
	}
	
	@CapabilityInject(IManaArmor.class)
	public static Capability<IManaArmor> CAPABILITY = null;
	
	private final int entID;
	private final IManaArmor stats;
	
	public ManaArmorSyncMessage(Entity ent, IManaArmor stats) {
		this(ent.getEntityId(), stats);
	}
	
	public ManaArmorSyncMessage(int entID, IManaArmor stats) {
		this.entID = entID;
		this.stats = stats;
	}

	public static ManaArmorSyncMessage decode(PacketBuffer buf) {
		IManaArmor stats = CAPABILITY.getDefaultInstance();
		final int entID = buf.readVarInt();
		CAPABILITY.getStorage().readNBT(CAPABILITY, stats, null, buf.readCompoundTag());
		
		return new ManaArmorSyncMessage(
				entID,
				stats
				);
	}

	public static void encode(ManaArmorSyncMessage msg, PacketBuffer buf) {
		buf.writeVarInt(msg.entID);
		buf.writeCompoundTag((CompoundNBT) CAPABILITY.getStorage().writeNBT(CAPABILITY, msg.stats, null));
	}

}
