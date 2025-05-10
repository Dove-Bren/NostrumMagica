package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client has changed part of their current incantation selection
 * @author Skyler
 *
 */
public class IncantationSelectionMessage {
	
	public static void handle(IncantationSelectionMessage message, Supplier<NetworkEvent.Context> ctx) {
		final ServerPlayer sp = ctx.get().getSender();
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
			
			if (att == null) {
				NostrumMagica.logger.warn("Could not look up player magic wrapper");
				return;
			}
			
			if (message.element != null) {
				att.setIncantationElement(message.element);
			} else if (message.shape != null) {
				att.setIncantationShape(message.shape);
			} else {
				att.setIncantationAlteration(message.alteration);
			}
			
			// Don't resync stats and let client handle modification
			// NetworkHandler.sendTo(new StatSyncMessage(att), sp);
		});
	}

	private final @Nullable EMagicElement element;
	private final @Nullable EAlteration alteration;
	private final @Nullable SpellShape shape;
	
	private IncantationSelectionMessage(@Nullable EMagicElement element, @Nullable EAlteration alteration, @Nullable SpellShape shape) {
		this.element = element;
		this.alteration = alteration;
		this.shape = shape;
	}

	public IncantationSelectionMessage(EMagicElement element) {
		this(element, null, null);
	}

	public IncantationSelectionMessage(EAlteration alteration) {
		this(null, alteration, null);
	}

	public IncantationSelectionMessage(SpellShape shape) {
		this(null, null, shape);
	}

	public static IncantationSelectionMessage decode(FriendlyByteBuf buf) {
		EMagicElement elem = buf.readBoolean() ? buf.readEnum(EMagicElement.class) : null;
		EAlteration alteration = buf.readBoolean() ? buf.readEnum(EAlteration.class) : null;
		SpellShape shape = buf.readBoolean() ? SpellShape.get(buf.readUtf()) : null;
		
		return new IncantationSelectionMessage(elem, alteration, shape);
	}

	public static void encode(IncantationSelectionMessage msg, FriendlyByteBuf buf) {
		if (msg.element != null) {
			buf.writeBoolean(true);
			buf.writeEnum(msg.element);
		} else {
			buf.writeBoolean(false);
		}

		if (msg.alteration != null) {
			buf.writeBoolean(true);
			buf.writeEnum(msg.alteration);
		} else {
			buf.writeBoolean(false);
		}

		if (msg.shape != null) {
			buf.writeBoolean(true);
			buf.writeUtf(msg.shape.getShapeKey());
		} else {
			buf.writeBoolean(false);
		}
	}

}
