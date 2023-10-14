package com.smanzana.nostrummagica.network.messages;

import java.util.UUID;
import java.util.function.Supplier;

import com.smanzana.nostrummagica.entity.IEntityPet;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server has opened a pet GUI and is requesting the client open it, too
 * @author Skyler
 *
 */
public class PetGUIOpenMessage {

	public static void handle(PetGUIOpenMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		
		int unused; // TODO deprecated and unused now?
		
//		Minecraft.getInstance().runAsync(() -> {
//			IEntityPet pet = null;
//			Entity foundEnt = Entities.FindEntity(NostrumMagica.instance.proxy.getPlayer().world, message.petID);
//			
//			if (foundEnt == null || !(foundEnt instanceof IEntityPet)) {
//				return;
//			}
//			
//			pet = (IEntityPet) foundEnt;
//			
//			if (pet != null) {
//				PetContainer<?> container = pet.getGUIContainer(NostrumMagica.instance.proxy.getPlayer());
//				container.overrideID(message.id);
//				container.windowId = message.mcID;
//				
//				if (message.numSheets != container.getSheetCount()) {
//					NostrumMagica.logger.error("Sheet count differs on client and server for " + pet);
//					return;
//				}
//				
//				@SuppressWarnings({ "unchecked", "rawtypes" })
//				PetGUIContainer<?> gui = new PetGUIContainer(container, pet.getGUIAdapter());
//				FMLCommonHandler.instance().showGuiScreen(gui);
//			}
//		});
	}

	private final UUID petID;
	private final int mcID;
	private final int id;
	private final int numSheets;
	
	public PetGUIOpenMessage(IEntityPet pet, int mcID, int id, int numSheets) {
		this(((LivingEntity) pet).getUniqueID(), mcID, id, numSheets);
	}
	
	public PetGUIOpenMessage(UUID petID, int mcID, int id, int numSheets) {
		this.petID = petID;
		this.mcID = mcID;
		this.id = id;
		this.numSheets = numSheets;
	}

	public static PetGUIOpenMessage decode(PacketBuffer buf) {
		return new PetGUIOpenMessage(
				buf.readUniqueId(),
				buf.readVarInt(),
				buf.readVarInt(),
				buf.readVarInt()
				);
	}

	public static void encode(PetGUIOpenMessage msg, PacketBuffer buf) {
		buf.writeUniqueId(msg.petID);
		buf.writeVarInt(msg.mcID);
		buf.writeVarInt(msg.id);
		buf.writeVarInt(msg.numSheets);
	}

}
