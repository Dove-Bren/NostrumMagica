package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has made a change to their tome's spell page loadouts
 * @author Skyler
 *
 */
public class SpellTomeSlotModifyMessage {

	public static void handle(SpellTomeSlotModifyMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		final ServerPlayerEntity sp = ctx.get().getSender();
		
		ctx.get().enqueueWork(() -> {
			// Find tome
			ItemStack tome = NostrumMagica.findTome(sp, message.tomeID);
			if (tome.isEmpty()) {
				NostrumMagica.logger.warn("Client sent tome update for tome that couldn't be found! " + message.tomeID);
				return;
			}
			
			final int maxPages = SpellTome.getPageCount(tome);
			final int maxSlots = SpellTome.getSlots(tome);
			
			if (message.pageIdx >= maxPages) {
				NostrumMagica.logger.warn("Client wants to modify spell page " + message.pageIdx + " but tome only goes to " + (maxPages-1));
				return;
			}
			
			if (message.slotIdx >= maxSlots) {
				NostrumMagica.logger.warn("Client wants to modify spell page slot " + message.slotIdx + " but tome only has  " + (maxSlots-1));
				return;
			}
			
			// Find spell
			@Nullable Spell spell;
			if (message.spellID == -1) {
				spell = null;
			} else {
				spell = NostrumMagica.instance.getSpellRegistry().lookup(message.spellID);
				if (spell == null) {
					NostrumMagica.logger.warn("Failed to find spell a client was assigning into their tome: " + message.spellID);
					return;
				}
			}
			
			SpellTome.setSpellInSlot(tome, message.pageIdx, message.slotIdx, spell);
		});
	}

	private final int tomeID;
	private final int pageIdx;
	private final int slotIdx;
	private final int spellID;
	
	public SpellTomeSlotModifyMessage(int tomeID, int pageIdx, int slotIdx, int spellID) {
		this.tomeID = tomeID;
		this.pageIdx = pageIdx;
		this.slotIdx = slotIdx;
		this.spellID = spellID;
	}

	public static SpellTomeSlotModifyMessage decode(PacketBuffer buf) {
		return new SpellTomeSlotModifyMessage(
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt()
				);
	}

	public static void encode(SpellTomeSlotModifyMessage msg, PacketBuffer buf) {
		buf.writeVarInt(msg.tomeID);
		buf.writeVarInt(msg.pageIdx);
		buf.writeVarInt(msg.slotIdx);
		buf.writeVarInt(msg.spellID);
	}

}
