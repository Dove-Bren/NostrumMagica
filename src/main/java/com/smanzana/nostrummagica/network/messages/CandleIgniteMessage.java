package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Sent to client to let them know a candle has been ignited, and a tile entity should be created.
 * @author Skyler
 *
 */
public class CandleIgniteMessage implements IMessage {

	public static class Handler implements IMessageHandler<CandleIgniteMessage, IMessage> {

		@Override
		public IMessage onMessage(CandleIgniteMessage message, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				final int dim = message.tag.getInteger(NBT_DIM);
				final BlockPos pos = BlockPos.fromLong(message.tag.getLong(NBT_POS));
				final ReagentType type = message.tag.hasKey(NBT_REAGENT)
						? ReagentType.values()[message.tag.getInteger(NBT_REAGENT) % ReagentType.values().length]
						: null;
				
				EntityPlayer player = NostrumMagica.proxy.getPlayer();
				if (player.dimension != dim) {
					return;
				}
				
				IBlockState state = player.world.getBlockState(pos);
				if (state == null || !(state.getBlock() instanceof Candle)) {
					return;
				}
				
				Candle.setReagent(player.world, pos, state, type);
			});
			
			return null;
		}
		
	}
	
	private static final String NBT_DIM = "dim";
	private static final String NBT_POS = "pos";
	private static final String NBT_REAGENT = "reagent_type";
	
	protected NBTTagCompound tag;
	
	public CandleIgniteMessage() {
		tag = new NBTTagCompound();
	}
	
	public CandleIgniteMessage(int dimension, BlockPos pos, ReagentType type) {
		tag = new NBTTagCompound();
		tag.setInteger(NBT_DIM, dimension);
		tag.setLong(NBT_POS, pos.toLong());
		if (type != null) {
			tag.setInteger(NBT_REAGENT, type.ordinal());
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		tag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, tag);
	}

}

