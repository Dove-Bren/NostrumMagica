package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.progression.skill.Skill;
import com.smanzana.nostrummagica.spell.EMagicElement;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client has requested a skill be purchased
 * @author Skyler
 *
 */
public class ClientPurchaseSkillMessage {
	
	public static void handle(ClientPurchaseSkillMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		final ServerPlayer sp = ctx.get().getSender();
		
		ctx.get().enqueueWork(() -> {
			INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
			
			if (att == null) {
				NostrumMagica.logger.warn("Could not look up player magic wrapper");
				return;
			}
			
			if (!att.hasSkill(message.skill) && !message.skill.isHidden(sp) && message.skill.meetsRequirements(sp)) {
				EMagicElement skillPointType = message.skill.getCategory().getSkillpointType();
				if (skillPointType == null) {
					if (att.getSkillPoints() <= 0) {
						return;
					}
				} else {
					if (att.getElementalSkillPoints(skillPointType) <= 0) {
						return;
					}
				}
				
				if (skillPointType == null) {
					att.takeSkillPoint();
				} else {
					att.takeElementalSkillPoint(skillPointType);
				}
				message.skill.addToPlayer(sp);
			}
			
			NetworkHandler.sendTo(new StatSyncMessage(att), sp);
		});
	}

	private final Skill skill;
	
	public ClientPurchaseSkillMessage(Skill skill) {
		this.skill = skill;
	}

	public static ClientPurchaseSkillMessage decode(FriendlyByteBuf buf) {
		final String skillKeyRaw = buf.readUtf(32767);
		Skill skill = Skill.lookup(new ResourceLocation(skillKeyRaw));
		if (skill == null) {
			throw new DecoderException("Failed to find nostrum skill for " + skillKeyRaw);
		}
		
		return new ClientPurchaseSkillMessage(skill);
	}

	public static void encode(ClientPurchaseSkillMessage msg, FriendlyByteBuf buf) {
		buf.writeUtf(msg.skill.getKey().toString());
	}

}
