package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;
import com.smanzana.nostrummagica.tile.ISpellCraftingTileEntity;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

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
		final ServerPlayerEntity sp = ctx.get().getSender();
		
		ctx.get().enqueueWork(() -> {
			World world = sp.world;
			
			// Get the TE
			TileEntity TE = world.getTileEntity(message.pos);
			if (TE == null) {
				NostrumMagica.logger.warn("Got craft message that didn't line up with a crafting table. This is a bug!");
				return;
			}
			
			if (message.iconIndex < 0) {
				NostrumMagica.logger.warn("Got craft message with no icon idx!");
				return;
			}
			
			ISpellCraftingTileEntity entity = (ISpellCraftingTileEntity) TE;
			
			Spell spell = entity.craft(sp, entity.getSpellCraftingInventory(), message.name, message.iconIndex, message.craftPattern);
			if (spell != null) {
				NostrumMagicaSounds.UI_RESEARCH.play(TE.getWorld(), 
						message.pos.getX(), message.pos.getY(), message.pos.getZ());
			
				NetworkHandler.sendToAll(
						new SpellRequestReplyMessage(Lists.newArrayList(spell)));
			}
		});
	}

	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
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

	public static SpellCraftMessage decode(PacketBuffer buf) {
		return new SpellCraftMessage(buf.readString(32767), buf.readBlockPos(), buf.readVarInt(),
				(buf.readBoolean() ? SpellCraftPattern.Get(buf.readResourceLocation()) : null)
			);
	}

	public static void encode(SpellCraftMessage msg, PacketBuffer buf) {
		buf.writeString(msg.name);
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
