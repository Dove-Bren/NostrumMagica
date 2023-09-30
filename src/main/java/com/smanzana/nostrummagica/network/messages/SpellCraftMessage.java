package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.tiles.SpellTableEntity;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.ByteBufUtils;
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
			
			SpellTableEntity entity = (SpellTableEntity) TE;
			
			Spell spell = entity.craft(sp, message.name, message.iconIndex);
			if (spell != null) {
				NostrumMagicaSounds.UI_RESEARCH.play(entity.getWorld(), 
						message.pos.getX(), message.pos.getY(), message.pos.getZ());
				}
			
				NetworkHandler.getSyncChannel().sendToAll(
						new SpellRequestReplyMessage(Lists.newArrayList(spell)));
		});
	}

	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	private final String name;
	private final BlockPos pos;
	private final int iconIndex;
	
	public SpellCraftMessage(String name, BlockPos pos, int iconIndex) {
		this.name = name;
		this.pos = pos;
		this.iconIndex = iconIndex;
	}

	public static SpellCraftMessage decode(PacketBuffer buf) {
		return new SpellCraftMessage(buf.readString(), buf.readBlockPos(), buf.readVarInt());
	}

	public static void encode(SpellCraftMessage msg, PacketBuffer buf) {
		buf.writeString(msg.name);
		buf.writeBlockPos(msg.pos);
		buf.writeVarInt(msg.iconIndex);
	}

}
