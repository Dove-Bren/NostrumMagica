package com.smanzana.nostrummagica.network.messages;


import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.ISpellCrafting;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is sending a new copy of the spellcrafting capability
 * @author Skyler
 *
 */
public class SpellCraftingCapabilitySyncMessage {
	
	public static void handle(SpellCraftingCapabilitySyncMessage message, Supplier<NetworkEvent.Context> ctx) {
		//update local attributes
		ctx.get().setPacketHandled(true);
		NostrumMagica.logger.info("Recieved Spell Crafting sync message from server");
		
		Minecraft.getInstance().runAsync(() -> {
			final Minecraft mc = Minecraft.getInstance();
			@Nullable Entity ent = mc.player.getEntityWorld().getEntityByID(message.entID);
			if (ent != null) {
				NostrumMagica.instance.proxy.receiveSpellCraftingOverride(ent, message.stats);
			}
		});
	}
	
	@CapabilityInject(ISpellCrafting.class)
	public static Capability<ISpellCrafting> CAPABILITY = null;
	
	private final int entID;
	private final ISpellCrafting stats;
	
	public SpellCraftingCapabilitySyncMessage(Entity ent, ISpellCrafting stats) {
		this(ent.getEntityId(), stats);
	}
	
	public SpellCraftingCapabilitySyncMessage(int entID, ISpellCrafting stats) {
		this.entID = entID;
		this.stats = stats;
	}

	public static SpellCraftingCapabilitySyncMessage decode(PacketBuffer buf) {
		ISpellCrafting stats = CAPABILITY.getDefaultInstance();
		final int entID = buf.readVarInt();
		CAPABILITY.getStorage().readNBT(CAPABILITY, stats, null, buf.readCompoundTag());
		
		return new SpellCraftingCapabilitySyncMessage(
				entID,
				stats
				);
	}

	public static void encode(SpellCraftingCapabilitySyncMessage msg, PacketBuffer buf) {
		buf.writeVarInt(msg.entID);
		buf.writeCompoundTag((CompoundNBT) CAPABILITY.getStorage().writeNBT(CAPABILITY, msg.stats, null));
	}

}
