package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.equipment.SpellScroll;
import com.smanzana.nostrummagica.item.equipment.SpellTome;
import com.smanzana.nostrummagica.spell.RegisteredSpell;
import com.smanzana.nostrummagica.spell.SpellCasting;
import com.smanzana.nostrummagica.spell.SpellCasting.SpellCastResult;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client has cast a spell
 * @author Skyler
 *
 */
public class ClientCastMessage {

	public static void handle(ClientCastMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Figure out what spell they have
		// cast it if they can
		ctx.get().setPacketHandled(true);
		
		final ServerPlayer sp = ctx.get().getSender();
		
		// What spell?
		RegisteredSpell spell = NostrumMagica.instance.getSpellRegistry().lookup(
				message.id
				);
		
		if (spell == null) {
			NostrumMagica.logger.warn("Could not find matching spell from client cast request");
			return;
		}
		
		final boolean isScroll = message.isScroll;
		final int toolID = message.toolId;
		final int entHintID = message.entityHintId;
		
		
		ctx.get().enqueueWork(() -> {
			boolean success = true;
			
			// Find matching hint entity, if one was indicated
			final @Nullable LivingEntity hintEntity;
			if (entHintID != -1) {
				@Nullable Entity raw = sp.getLevel().getEntity(entHintID);
				hintEntity = raw != null &&  raw instanceof LivingEntity living ? living : null;
			} else {
				hintEntity = null;
			}
			
			if (!isScroll) {
				// Find the tome this was cast from, if any
				ItemStack tome = NostrumMagica.findTome(sp, toolID);
				
				if (!tome.isEmpty() && tome.getItem() instanceof SpellTome
						&& SpellTome.getTomeID(tome) == toolID) {
					// Casting from a tome.
					success = SpellCasting.AttemptToolCast(spell, sp, tome, hintEntity).succeeded;
				} else {
					NostrumMagica.logger.warn("Got cast from client with mismatched tome");
					success = false;
				}
			} else {
				// toolID is main hand (0) or offhand (1)
				final InteractionHand hand = (toolID == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
				ItemStack scroll = sp.getItemInHand(hand);
				if (scroll.isEmpty()
						|| !(scroll.getItem() instanceof SpellScroll scrollItem)) {
					NostrumMagica.logger.warn("Got cast from client with mismatched scroll location");
					success = false;
				} else {
					RegisteredSpell scrollSpell = scrollItem.getSpell(scroll);
					if (scrollSpell == null || scrollSpell.getRegistryID() != message.id) {
						NostrumMagica.logger.warn("Got cast from client with mismatched scroll spell ID");
						success = false;
					} else {
						final SpellCastResult result = SpellCasting.AttemptScrollCast(spell, sp, hintEntity);
						success = result.succeeded;
						if (success) {
							// Set cooldown directly even though event handler will have already set it.
							// Using a scroll has more cooldown than noticing other spells being cast.
							sp.getCooldowns().addCooldown(scroll.getItem(), SpellCasting.CalculateSpellCooldown(spell, sp, result.summary) * 2);

							if (!sp.isCreative()) {
								ItemStacks.damageItem(scroll, sp, hand, scrollItem.getCastDurabilityCost(sp, spell));
							}
						}
						
					}
				}
				
				success = SpellCasting.AttemptScrollCast(spell, sp, hintEntity).succeeded;
			}

			// Whether it failed or not, sync attributes to client.
			// if it failed because they're out of mana on the server, or don't have the right attribs, etc.
			// then we'd want to sync them. If it succeeded, their mana and xp etc. have been adjusted!
			NostrumMagica.Proxy.syncPlayer(sp);
			
			if (!success) {
				NostrumMagica.logger.debug("Player attempted to cast " + spell.getName() + " but failed server side checks");
			}
		});
	}

	private final int id;
	private final int toolId;
	private final boolean isScroll;
	private final int entityHintId;
	
	public ClientCastMessage(RegisteredSpell spell, boolean scroll, int toolID, @Nullable Entity entityHint) {
		this(spell.getRegistryID(), scroll, toolID, entityHint == null ? -1 : entityHint.getId());
	}
	
	public ClientCastMessage(int id, boolean scroll, int tomeID) {
		this(id, scroll, tomeID, -1);
	}
	
	public ClientCastMessage(int id, boolean scroll, int tomeID, int entityHintId) {
		this.id = id;
		this.isScroll = scroll;
		this.toolId = tomeID;
		this.entityHintId = entityHintId;
	}

	public static ClientCastMessage decode(FriendlyByteBuf buf) {
		return new ClientCastMessage(buf.readInt(), buf.readBoolean(), buf.readInt(), buf.readInt());
	}

		public static void encode(ClientCastMessage msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.id);
		buf.writeBoolean(msg.isScroll);
		buf.writeInt(msg.toolId);
		buf.writeInt(msg.entityHintId);
	}

}
