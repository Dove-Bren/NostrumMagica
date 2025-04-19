package com.smanzana.nostrummagica.network.message;


import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.ISpellCrafting;
import com.smanzana.nostrummagica.capabilities.SpellCraftingCapability;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

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
		
		Minecraft.getInstance().submit(() -> {
			final Minecraft mc = Minecraft.getInstance();
			@Nullable Entity ent = mc.player.getCommandSenderWorld().getEntity(message.entID);
			if (ent != null) {
				NostrumMagica.instance.proxy.receiveSpellCraftingOverride(ent, message.stats);
			}
		});
	}
	
	private final int entID;
	private final ISpellCrafting stats;
	
	public SpellCraftingCapabilitySyncMessage(Entity ent, ISpellCrafting stats) {
		this(ent.getId(), stats);
	}
	
	public SpellCraftingCapabilitySyncMessage(int entID, ISpellCrafting stats) {
		this.entID = entID;
		this.stats = stats;
	}

	public static SpellCraftingCapabilitySyncMessage decode(FriendlyByteBuf buf) {
		ISpellCrafting stats = new SpellCraftingCapability();
		final int entID = buf.readVarInt();
		stats.deserializeNBT(buf.readNbt());
		
		return new SpellCraftingCapabilitySyncMessage(
				entID,
				stats
				);
	}

	public static void encode(SpellCraftingCapabilitySyncMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.entID);
		buf.writeNbt(msg.stats.serializeNBT());
	}

}
