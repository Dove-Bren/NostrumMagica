package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.progression.skill.Skill;

import io.netty.handler.codec.DecoderException;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has requested a skill be purchased
 * @author Skyler
 *
 */
public class ClientPurchaseSkillMessage {
	
	public static void handle(ClientPurchaseSkillMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		final ServerPlayerEntity sp = ctx.get().getSender();
		
		ctx.get().enqueueWork(() -> {
			INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
			
			if (att == null) {
				NostrumMagica.logger.warn("Could not look up player magic wrapper");
				return;
			}
			
			if (!att.hasSkill(message.skill) && !message.skill.isHidden(sp) && message.skill.meetsRequirements(sp) && att.getSkillPoints() > 0) {
				att.takeSkillPoint();
				message.skill.addToPlayer(sp);
			}
			
			NetworkHandler.sendTo(new StatSyncMessage(att), sp);
		});
	}

	private final Skill skill;
	
	public ClientPurchaseSkillMessage(Skill skill) {
		this.skill = skill;
	}

	public static ClientPurchaseSkillMessage decode(PacketBuffer buf) {
		final String skillKeyRaw = buf.readString(32767);
		Skill skill = Skill.lookup(new ResourceLocation(skillKeyRaw));
		if (skill == null) {
			throw new DecoderException("Failed to find nostrum skill for " + skillKeyRaw);
		}
		
		return new ClientPurchaseSkillMessage(skill);
	}

	public static void encode(ClientPurchaseSkillMessage msg, PacketBuffer buf) {
		buf.writeString(msg.skill.getKey().toString());
	}

}
