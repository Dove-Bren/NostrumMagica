package com.smanzana.nostrummagica.network.messages;

import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetContainer;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetGUIContainer;
import com.smanzana.nostrummagica.entity.IEntityPet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server has opened a pet GUI and is requesting the client open it, too
 * @author Skyler
 *
 */
public class PetGUIOpenMessage implements IMessage {

	public static class Handler implements IMessageHandler<PetGUIOpenMessage, IMessage> {

		@Override
		public IMessage onMessage(PetGUIOpenMessage message, MessageContext ctx) {
			final UUID uuid = message.tag.getUniqueId(NBT_UUID);
			final int sheets = message.tag.getInt(NBT_SHEETS);
			final int id = message.tag.getInt(NBT_CONTAINER_ID);
			final int mcID = message.tag.getInt(NBT_MC_CONTAINER_ID);
			
			Minecraft.getInstance().runAsync(() -> {
				IEntityPet pet = null;
				//for (Entity ent : Minecraft.getInstance().theWorld.getLoadedEntityList()) {
				for (Entity ent : NostrumMagica.proxy.getPlayer().world.loadedEntityList) {
					if (ent == null || !(ent instanceof IEntityPet)) {
						continue;
					}
					
					if (ent.getUniqueID().equals(uuid)) {
						pet = (IEntityPet) ent;
						break;
					}
				}
				
				if (pet != null) {
					PetContainer<?> container = pet.getGUIContainer(NostrumMagica.proxy.getPlayer());
					container.overrideID(id);
					container.windowId = mcID;
					
					if (sheets != container.getSheetCount()) {
						NostrumMagica.logger.error("Sheet count differs on client and server for " + pet);
						return null;
					}
					
					@SuppressWarnings({ "unchecked", "rawtypes" })
					PetGUIContainer<?> gui = new PetGUIContainer(container, pet.getGUIAdapter());
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
	
	protected CompoundNBT tag;
	
	public PetGUIOpenMessage() {
		tag = new CompoundNBT();
	}
	
	public PetGUIOpenMessage(IEntityPet pet, int mcID, int id, int numSheets) {
		this();
		
		tag.setUniqueId(NBT_UUID, ((LivingEntity) pet).getUniqueID());
		tag.putInt(NBT_CONTAINER_ID, id);
		tag.putInt(NBT_SHEETS, numSheets);
		tag.putInt(NBT_MC_CONTAINER_ID, mcID);
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
