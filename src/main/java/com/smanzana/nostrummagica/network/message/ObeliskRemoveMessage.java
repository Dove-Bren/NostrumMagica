package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.tile.ObeliskTileEntity;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

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
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(() -> {
			TileEntity te = player.world.getTileEntity(message.pos);
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

	public static ObeliskRemoveMessage decode(PacketBuffer buf) {
		return new ObeliskRemoveMessage(buf.readBlockPos(), buf.readVarInt());
	}

	public static void encode(ObeliskRemoveMessage msg, PacketBuffer buf) {
		buf.writeBlockPos(msg.pos);
		buf.writeVarInt(msg.index);
	}
}
