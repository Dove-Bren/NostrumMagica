package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.ModificationTableTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client has clicked to submit a modification on the modification table
 * @author Skyler
 *
 */
public class ModifyMessage {

	public static void handle(ModifyMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Find the tile entity.
		// Call 'modify' method on it
		// boom
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			Player sp = ctx.get().getSender();
			Level world = sp.level;
			
			// Get the TE
			BlockEntity TE = world.getBlockEntity(message.pos);
			if (TE == null) {
				NostrumMagica.logger.warn("Got modify message that didn't line up with a modification table. This is a bug!");
				return;
			}
			
			ModificationTableTileEntity entity = (ModificationTableTileEntity) TE;
			
			entity.modify(message.bool, message.flt);
		});
	}

	private final BlockPos pos;
	private final boolean bool;
	private final float flt;
	
	public ModifyMessage(BlockPos pos, boolean bool, float flt) {
		this.pos = pos;
		this.bool = bool;
		this.flt = flt;
	}

	public static ModifyMessage decode(FriendlyByteBuf buf) {
		return new ModifyMessage(buf.readBlockPos(), buf.readBoolean(), buf.readFloat());
	}

	public static void encode(ModifyMessage msg, FriendlyByteBuf buf) {
		buf.writeBlockPos(msg.pos);
		buf.writeBoolean(msg.bool);
		buf.writeFloat(msg.flt);
	}

}
