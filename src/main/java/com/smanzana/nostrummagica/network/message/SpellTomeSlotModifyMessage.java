package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.spell.Spell;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client has made a change to their tome's spell page loadouts
 * @author Skyler
 *
 */
public class SpellTomeSlotModifyMessage {

	public static void handle(SpellTomeSlotModifyMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		final ServerPlayer sp = ctx.get().getSender();
		
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

	public static SpellTomeSlotModifyMessage decode(FriendlyByteBuf buf) {
		return new SpellTomeSlotModifyMessage(
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt()
				);
	}

	public static void encode(SpellTomeSlotModifyMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.tomeID);
		buf.writeVarInt(msg.pageIdx);
		buf.writeVarInt(msg.slotIdx);
		buf.writeVarInt(msg.spellID);
	}

}
