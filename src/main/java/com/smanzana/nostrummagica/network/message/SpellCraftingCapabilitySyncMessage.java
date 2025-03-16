package com.smanzana.nostrummagica.network.message;


import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.ISpellCrafting;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
	
	@CapabilityInject(ISpellCrafting.class)
	public static Capability<ISpellCrafting> CAPABILITY = null;
	
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
		ISpellCrafting stats = CAPABILITY.getDefaultInstance();
		final int entID = buf.readVarInt();
		CAPABILITY.getStorage().readNBT(CAPABILITY, stats, null, buf.readNbt());
		
		return new SpellCraftingCapabilitySyncMessage(
				entID,
				stats
				);
	}

	public static void encode(SpellCraftingCapabilitySyncMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.entID);
		buf.writeNbt((CompoundTag) CAPABILITY.getStorage().writeNBT(CAPABILITY, msg.stats, null));
	}

}
