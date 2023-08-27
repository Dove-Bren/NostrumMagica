package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.ModificationTableEntity;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has clicked to submit a modification on the modification table
 * @author Skyler
 *
 */
public class ModifyMessage implements IMessage {

	public static class Handler implements IMessageHandler<ModifyMessage, IMessage> {

		@Override
		public IMessage onMessage(ModifyMessage message, MessageContext ctx) {
			// Find the tile entity.
			// Call 'modify' method on it
			// boom
			
			int x = message.tag.getInt(NBT_POS_X);
			int y = message.tag.getInt(NBT_POS_Y);
			int z = message.tag.getInt(NBT_POS_Z);
			BlockPos pos = new BlockPos(x, y, z);
			boolean bool = message.tag.getBoolean(NBT_VAL_B);
			float flt = message.tag.getFloat(NBT_VAL_F);
			
			ctx.getServerHandler().player.getServerWorld().runAsync(() -> {
				PlayerEntity sp = ctx.getServerHandler().player;
				World world = sp.world;
				
				// Get the TE
				TileEntity TE = world.getTileEntity(pos);
				if (TE == null) {
					NostrumMagica.logger.warn("Got modify message that didn't line up with a modification table. This is a bug!");
					return;
				}
				
				ModificationTableEntity entity = (ModificationTableEntity) TE;
				
				entity.modify(bool, flt);
			});
			
			return null;
		}
	}

	private static final String NBT_POS_X = "x";
	private static final String NBT_POS_Y = "y";
	private static final String NBT_POS_Z = "z";
	private static final String NBT_VAL_B = "valB";
	private static final String NBT_VAL_F = "valF";
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected CompoundNBT tag;
	
	public ModifyMessage() {
		tag = new CompoundNBT();
	}
	
	public ModifyMessage(BlockPos pos, boolean bool, float flt) {
		tag = new CompoundNBT();
		
		tag.putInt(NBT_POS_X, pos.getX());
		tag.putInt(NBT_POS_Y, pos.getY());
		tag.putInt(NBT_POS_Z, pos.getZ());
		tag.putBoolean(NBT_VAL_B, bool);
		tag.putFloat(NBT_VAL_F, flt);
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
