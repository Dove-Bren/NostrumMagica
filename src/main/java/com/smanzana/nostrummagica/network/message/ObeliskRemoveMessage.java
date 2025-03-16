package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.tile.ObeliskTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Client has selected they want to remove a target from the obelisk
 * @author Skyler
 *
 */
public class ObeliskRemoveMessage {

	public static void handle(ObeliskRemoveMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Get from and to
		// Validate to is an obelisk and then try to update it
		ctx.get().setPacketHandled(true);
		ServerPlayer player = ctx.get().getSender();
		ctx.get().enqueueWork(() -> {
			BlockEntity te = player.level.getBlockEntity(message.pos);
			if (te != null && te instanceof ObeliskTileEntity) {
				((ObeliskTileEntity) te).removeTargetIndex(message.index);
			}
		});
	}

	private final BlockPos pos;
	private final int index;
	
	public ObeliskRemoveMessage(BlockPos obeliskPos, int index) {
		this.pos = obeliskPos;
		this.index = index;
	}

	public static ObeliskRemoveMessage decode(FriendlyByteBuf buf) {
		return new ObeliskRemoveMessage(buf.readBlockPos(), buf.readVarInt());
	}

	public static void encode(ObeliskRemoveMessage msg, FriendlyByteBuf buf) {
		buf.writeBlockPos(msg.pos);
		buf.writeVarInt(msg.index);
	}
}
