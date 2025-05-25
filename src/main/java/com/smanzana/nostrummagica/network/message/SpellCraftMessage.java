package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.RegisteredSpell;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;
import com.smanzana.nostrummagica.tile.ISpellCraftingTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client has clicked to create a spell in the Spell Table
 * @author Skyler
 *
 */
public class SpellCraftMessage {

	public static void handle(SpellCraftMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Find the tile entity.
		// Call 'craft' method on it
		// boom
		ctx.get().setPacketHandled(true);
		final ServerPlayer sp = ctx.get().getSender();
		
		ctx.get().enqueueWork(() -> {
			Level world = sp.level;
			
			// Get the TE
			BlockEntity TE = world.getBlockEntity(message.pos);
			if (TE == null) {
				NostrumMagica.logger.warn("Got craft message that didn't line up with a crafting table. This is a bug!");
				return;
			}
			
			if (message.iconIndex < 0) {
				NostrumMagica.logger.warn("Got craft message with no icon idx!");
				return;
			}
			
			ISpellCraftingTileEntity entity = (ISpellCraftingTileEntity) TE;
			
			RegisteredSpell spell = entity.craft(sp, entity.getSpellCraftingInventory(), message.name, message.iconIndex, message.craftPattern);
			if (spell != null) {
				NostrumMagicaSounds.UI_RESEARCH.play(TE.getLevel(), 
						message.pos.getX(), message.pos.getY(), message.pos.getZ());
			
				NetworkHandler.sendToAll(
						new SpellRequestReplyMessage(Lists.newArrayList(spell)));
			}
		});
	}

	private final String name;
	private final BlockPos pos;
	private final int iconIndex;
	private final @Nullable SpellCraftPattern craftPattern;
	
	public SpellCraftMessage(String name, BlockPos pos, int iconIndex) {
		this(name, pos, iconIndex, null);
	}
	
	public SpellCraftMessage(String name, BlockPos pos, int iconIndex, @Nullable SpellCraftPattern craftPattern) {
		this.name = name;
		this.pos = pos;
		this.iconIndex = iconIndex;
		this.craftPattern = craftPattern;
	}

	public static SpellCraftMessage decode(FriendlyByteBuf buf) {
		return new SpellCraftMessage(buf.readUtf(32767), buf.readBlockPos(), buf.readVarInt(),
				(buf.readBoolean() ? SpellCraftPattern.Get(buf.readResourceLocation()) : null)
			);
	}

	public static void encode(SpellCraftMessage msg, FriendlyByteBuf buf) {
		buf.writeUtf(msg.name);
		buf.writeBlockPos(msg.pos);
		buf.writeVarInt(msg.iconIndex);
		if (msg.craftPattern == null) {
			buf.writeBoolean(false);
		} else {
			buf.writeBoolean(true);
			buf.writeResourceLocation(msg.craftPattern.getRegistryName());
		}
	}

}
