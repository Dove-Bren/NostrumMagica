package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.tiles.NostrumObeliskEntity;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has selected which other target the obelisk should target
 * @author Skyler
 *
 */
public class ObeliskSelectMessage {

	public static void handle(ObeliskSelectMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Get from and to
		// Validate to is an obelisk and then try to update it
		ctx.get().setPacketHandled(true);
		ServerPlayerEntity player = ctx.get().getSender();
		ctx.get().enqueueWork(() -> {
			TileEntity te = player.world.getTileEntity(message.pos);
			if (te != null && te instanceof NostrumObeliskEntity) {
				((NostrumObeliskEntity) te).setTargetIndex(message.index);
			}
		});
	}

	private final BlockPos pos;
	private final int index;
	
	public ObeliskSelectMessage(BlockPos obeliskPos, int index) {
		this.pos = obeliskPos;
		this.index = index;
	}

	public static ObeliskSelectMessage decode(PacketBuffer buf) {
		return new ObeliskSelectMessage(buf.readBlockPos(), buf.readVarInt());
	}

	public static void encode(ObeliskSelectMessage msg, PacketBuffer buf) {
		buf.writeBlockPos(msg.pos);
		buf.writeVarInt(msg.index);
	}
}
