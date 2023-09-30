package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Sent to client to let them know a candle has been ignited, and a tile entity should be created.
 * @author Skyler
 *
 */
public class CandleIgniteMessage {

	public static void handle(CandleIgniteMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		Minecraft.getInstance().runAsync(() -> {
			
			PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
			if (player.dimension.getId() != message.dimensionID) {
				return;
			}
			
			BlockState state = player.world.getBlockState(message.pos);
			if (state == null || !(state.getBlock() instanceof Candle)) {
				return;
			}
			
			Candle.setReagent(player.world, message.pos, state, message.type);
		});
	}
	
	private final int dimensionID;
	private final BlockPos pos;
	private final ReagentType type;
	
	public CandleIgniteMessage(int dimension, BlockPos pos, ReagentType type) {
		this.dimensionID = dimension;
		this.pos = pos;
		this.type = type;
	}

	public static CandleIgniteMessage decode(PacketBuffer buf) {
		return new CandleIgniteMessage(buf.readInt(), buf.readBlockPos(), buf.readEnumValue(ReagentType.class));
	}

	public static void encode(CandleIgniteMessage msg, PacketBuffer buf) {
		buf.writeInt(msg.dimensionID);
		buf.writeBlockPos(msg.pos);
		buf.writeEnumValue(msg.type);
	}

}

