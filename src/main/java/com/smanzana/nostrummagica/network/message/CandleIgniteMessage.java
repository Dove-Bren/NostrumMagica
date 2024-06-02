package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.Candle;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.NetUtils;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
			if (!DimensionUtils.InDimension(player, message.dimension)) {
				return;
			}
			
			BlockState state = player.world.getBlockState(message.pos);
			if (state == null || !(state.getBlock() instanceof Candle)) {
				return;
			}
			
			Candle.setReagent(player.world, message.pos, state, message.type);
		});
	}
	
	private final RegistryKey<World> dimension;
	private final BlockPos pos;
	private final @Nullable ReagentType type;
	
	public CandleIgniteMessage(RegistryKey<World> dimension, BlockPos pos, @Nullable ReagentType type) {
		this.dimension = dimension;
		this.pos = pos;
		this.type = type;
	}

	public static CandleIgniteMessage decode(PacketBuffer buf) {
		return new CandleIgniteMessage(NetUtils.unpackDimension(buf), buf.readBlockPos(), buf.readBoolean() ? buf.readEnumValue(ReagentType.class) : null);
	}

	public static void encode(CandleIgniteMessage msg, PacketBuffer buf) {
		NetUtils.packDimension(buf, msg.dimension);
		buf.writeBlockPos(msg.pos);
		buf.writeBoolean(msg.type != null);
		if (msg.type != null) buf.writeEnumValue(msg.type);
	}

}

