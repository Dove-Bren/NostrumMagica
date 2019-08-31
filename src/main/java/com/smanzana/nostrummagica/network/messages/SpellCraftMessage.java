package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.SpellTable.SpellTableEntity;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
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
 * Client has clicked to create a spell in the Spell Table
 * @author Skyler
 *
 */
public class SpellCraftMessage implements IMessage {

	public static class Handler implements IMessageHandler<SpellCraftMessage, IMessage> {

		@Override
		public IMessage onMessage(SpellCraftMessage message, MessageContext ctx) {
			// Find the tile entity.
			// Call 'craft' method on it
			// boom
			
			int x = message.tag.getInteger(NBT_POS_X);
			int y = message.tag.getInteger(NBT_POS_Y);
			int z = message.tag.getInteger(NBT_POS_Z);
			BlockPos pos = new BlockPos(x, y, z);
			String name = message.tag.getString(NBT_NAME);
			
			final EntityPlayerMP sp = ctx.getServerHandler().playerEntity;
			
			sp.getServerWorld().addScheduledTask(() -> {
			World world = sp.worldObj;
			
			// Get the TE
			TileEntity TE = world.getTileEntity(pos);
			if (TE == null) {
				NostrumMagica.logger.warn("Got craft message that didn't line up with a crafting table. This is a bug!");
				return;
			}
			
			SpellTableEntity entity = (SpellTableEntity) TE;
			
			entity.craft(name);
			NostrumMagicaSounds.AMBIENT_WOOSH.play(entity.getWorld(), 
					x, y, z);
			});
			
			return null;
		}
	}

	private static final String NBT_POS_X = "x";
	private static final String NBT_POS_Y = "y";
	private static final String NBT_POS_Z = "z";
	private static final String NBT_NAME = "name";
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected NBTTagCompound tag;
	
	public SpellCraftMessage() {
		tag = new NBTTagCompound();
	}
	
	public SpellCraftMessage(String name, BlockPos pos) {
		tag = new NBTTagCompound();
		
		tag.setString(NBT_NAME, name);
		tag.setInteger(NBT_POS_X, pos.getX());
		tag.setInteger(NBT_POS_Y, pos.getY());
		tag.setInteger(NBT_POS_Z, pos.getZ());
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
