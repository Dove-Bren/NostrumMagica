package com.smanzana.nostrummagica.network.messages;

import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.dragongui.TamedDragonGUI.DragonContainer;
import com.smanzana.nostrummagica.client.gui.dragongui.TamedDragonGUI.DragonGUI;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server has opened a dragon GUI and is requesting the client open it, too
 * @author Skyler
 *
 */
public class TamedDragonGUIOpenMessage implements IMessage {

	public static class Handler implements IMessageHandler<TamedDragonGUIOpenMessage, IMessage> {

		@Override
		public IMessage onMessage(TamedDragonGUIOpenMessage message, MessageContext ctx) {
			final UUID uuid = message.tag.getUniqueId(NBT_UUID);
			final int sheets = message.tag.getInteger(NBT_SHEETS);
			final int id = message.tag.getInteger(NBT_CONTAINER_ID);
			final int mcID = message.tag.getInteger(NBT_MC_CONTAINER_ID);
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				ITameDragon dragon = null;
				//for (Entity ent : Minecraft.getMinecraft().theWorld.getLoadedEntityList()) {
				for (Entity ent : NostrumMagica.proxy.getPlayer().worldObj.loadedEntityList) {
					if (ent == null || !(ent instanceof ITameDragon)) {
						continue;
					}
					
					if (ent.getUniqueID().equals(uuid)) {
						dragon = (ITameDragon) ent;
						break;
					}
				}
				
				if (dragon != null) {
					DragonContainer container = dragon.getGUIContainer(NostrumMagica.proxy.getPlayer());
					container.overrideID(id);
					container.windowId = mcID;
					
					if (sheets != container.getSheetCount()) {
						NostrumMagica.logger.error("Sheet count differs on client and server for " + dragon);
						return null;
					}
					
					DragonGUI gui = new DragonGUI(container);
					FMLCommonHandler.instance().showGuiScreen(gui);
				}
				return null;
			});
			
			
			return null;
		}
		
	}

	private static final String NBT_UUID = "uuid";
	private static final String NBT_SHEETS = "numsheets"; // for sanity checking
	private static final String NBT_CONTAINER_ID = "id";
	private static final String NBT_MC_CONTAINER_ID = "minecraftScreenID";
	
	protected NBTTagCompound tag;
	
	public TamedDragonGUIOpenMessage() {
		tag = new NBTTagCompound();
	}
	
	public TamedDragonGUIOpenMessage(ITameDragon dragon, int mcID, int id, int numSheets) {
		this();
		
		tag.setUniqueId(NBT_UUID, ((EntityLivingBase) dragon).getUniqueID());
		tag.setInteger(NBT_CONTAINER_ID, id);
		tag.setInteger(NBT_SHEETS, numSheets);
		tag.setInteger(NBT_MC_CONTAINER_ID, mcID);
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
