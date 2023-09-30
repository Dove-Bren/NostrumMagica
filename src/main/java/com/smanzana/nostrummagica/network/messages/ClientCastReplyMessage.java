package com.smanzana.nostrummagica.network.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server has processed spell cast request and sent back
 * the status (as well as final mana)
 * Includes xp and spent reagent totals
 * @author Skyler
 *
 */
public class ClientCastReplyMessage {

		public static void handle(ClientCastReplyMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		Minecraft.getInstance().runAsync(() -> {
			PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
			INostrumMagic att = NostrumMagica.getMagicWrapper(
					player);
			// Regardless of success, server has synced mana with us.
			int mana = message.mana;
			float xp = message.xp;
			boolean success = message.success;
			
			att.setMana(mana);
			
			if (success) {
				// On success, server sends XP that was added
				att.addXP(xp);
			} else {
				
			}
			
//				if (message.tag.contains(NBT_REAGENTS, NBT.TAG_COMPOUND)) {
//					CompoundNBT regs = message.tag.getCompound(NBT_REAGENTS);
//					if (!regs.keySet().isEmpty())
//					for (String key : regs.keySet()) {
//						int cost = regs.getInt(key);
//						if (cost == 0)
//							continue;
//						
//						try {
//							ReagentType type = ReagentType.valueOf(key);
//							NostrumMagica.removeReagents(player, type, cost);
//						} catch (Exception e) {
//							;
//						}
//					}
//					
//				}
		});
	}

	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	private final boolean success;
	private final int mana;
	private float xp;
	private Map<ReagentType, Integer> reagentCost;
	
	public ClientCastReplyMessage(boolean success, int mana, float xp,
			Map<ReagentType, Integer> reagentCostIn) {
		this.success = success;
		this.mana = mana;
		this.xp = xp;
		this.reagentCost = new HashMap<>();
		
		// remove empty elements
		for (ReagentType type : reagentCostIn.keySet()) {
			if (type == null)
				continue;
			
			Integer cost = reagentCostIn.get(type);
			if (cost == null || cost == 0)
				continue;
			
			reagentCost.put(type, cost);
		}
	}

	public static ClientCastReplyMessage decode(PacketBuffer buf) {
		boolean success = buf.readBoolean();
		int mana = buf.readInt();
		float xp = buf.readFloat();
		
		int reagentCount = buf.readInt();
		Map<ReagentType, Integer> reagentCost = new HashMap<>();
		
		for (int i = 0; i < reagentCount; i++) {
			ReagentType type = buf.readEnumValue(ReagentType.class);
			int count = buf.readVarInt();
			reagentCost.put(type, count);
		}
		
		return new ClientCastReplyMessage(success, mana, xp, reagentCost);
	}

	public static void encode(ClientCastReplyMessage msg, PacketBuffer buf) {
		buf.writeBoolean(msg.success);
		buf.writeInt(msg.mana);
		buf.writeFloat(msg.xp);
		
		buf.writeInt(msg.reagentCost.size());
		for (ReagentType type : msg.reagentCost.keySet()) {
			buf.writeEnumValue(type);
			buf.writeVarInt(msg.reagentCost.get(type));
		}
	}

}
