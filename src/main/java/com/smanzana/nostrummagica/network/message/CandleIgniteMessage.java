package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.CandleBlock;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.NetUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

/**
 * Sent to client to let them know a candle has been ignited, and a tile entity should be created.
 * @author Skyler
 *
 */
public class CandleIgniteMessage {

	public static void handle(CandleIgniteMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		Minecraft.getInstance().submit(() -> {
			
			Player player = NostrumMagica.Proxy.getPlayer();
			if (!DimensionUtils.InDimension(player, message.dimension)) {
				return;
			}
			
			BlockState state = player.level.getBlockState(message.pos);
			if (state == null || !(state.getBlock() instanceof CandleBlock)) {
				return;
			}
			
			CandleBlock.setReagent(player.level, message.pos, state, message.type);
		});
	}
	
	private final ResourceKey<Level> dimension;
	private final BlockPos pos;
	private final @Nullable ReagentType type;
	
	public CandleIgniteMessage(ResourceKey<Level> dimension, BlockPos pos, @Nullable ReagentType type) {
		this.dimension = dimension;
		this.pos = pos;
		this.type = type;
	}

	public static CandleIgniteMessage decode(FriendlyByteBuf buf) {
		return new CandleIgniteMessage(NetUtils.unpackDimension(buf), buf.readBlockPos(), buf.readBoolean() ? buf.readEnum(ReagentType.class) : null);
	}

	public static void encode(CandleIgniteMessage msg, FriendlyByteBuf buf) {
		NetUtils.packDimension(buf, msg.dimension);
		buf.writeBlockPos(msg.pos);
		buf.writeBoolean(msg.type != null);
		if (msg.type != null) buf.writeEnum(msg.type);
	}

}

