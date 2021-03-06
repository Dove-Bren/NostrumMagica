package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.ModificationTable.ModificationTableEntity;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
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
			
			int x = message.tag.getInteger(NBT_POS_X);
			int y = message.tag.getInteger(NBT_POS_Y);
			int z = message.tag.getInteger(NBT_POS_Z);
			BlockPos pos = new BlockPos(x, y, z);
			boolean bool = message.tag.getBoolean(NBT_VAL_B);
			float flt = message.tag.getFloat(NBT_VAL_F);
			
			ctx.getServerHandler().playerEntity.getServerWorld().addScheduledTask(() -> {
				EntityPlayer sp = ctx.getServerHandler().playerEntity;
				World world = sp.worldObj;
				
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
	
	protected NBTTagCompound tag;
	
	public ModifyMessage() {
		tag = new NBTTagCompound();
	}
	
	public ModifyMessage(BlockPos pos, boolean bool, float flt) {
		tag = new NBTTagCompound();
		
		tag.setInteger(NBT_POS_X, pos.getX());
		tag.setInteger(NBT_POS_Y, pos.getY());
		tag.setInteger(NBT_POS_Z, pos.getZ());
		tag.setBoolean(NBT_VAL_B, bool);
		tag.setFloat(NBT_VAL_F, flt);
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
