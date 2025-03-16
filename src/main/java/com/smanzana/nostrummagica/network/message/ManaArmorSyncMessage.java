package com.smanzana.nostrummagica.network.message;


import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
		
		Minecraft.getInstance().submit(() -> {
			final Minecraft mc = Minecraft.getInstance();
			@Nullable Entity ent = mc.player.getCommandSenderWorld().getEntity(message.entID);
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
		this(ent.getId(), stats);
	}
	
	public ManaArmorSyncMessage(int entID, IManaArmor stats) {
		this.entID = entID;
		this.stats = stats;
	}

	public static ManaArmorSyncMessage decode(FriendlyByteBuf buf) {
		IManaArmor stats = CAPABILITY.getDefaultInstance();
		final int entID = buf.readVarInt();
		CAPABILITY.getStorage().readNBT(CAPABILITY, stats, null, buf.readNbt());
		
		return new ManaArmorSyncMessage(
				entID,
				stats
				);
	}

	public static void encode(ManaArmorSyncMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.entID);
		buf.writeNbt((CompoundTag) CAPABILITY.getStorage().writeNBT(CAPABILITY, msg.stats, null));
	}

}
