package com.smanzana.nostrummagica.network.messages;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.items.SpellTome.EnhancementWrapper;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has cast a spell
 * @author Skyler
 *
 */
public class ClientCastMessage implements IMessage {

	public static class Handler implements IMessageHandler<ClientCastMessage, ClientCastReplyMessage> {

		@Override
		public ClientCastReplyMessage onMessage(ClientCastMessage message, MessageContext ctx) {
			// Figure out what spell they have
			// cast it if they can
			
			EntityPlayer sp = ctx.getServerHandler().playerEntity;
			
			// What spell?
			Spell spell = NostrumMagica.spellRegistry.lookup(
					message.tag.getInteger(NBT_ID)
					);
			
			if (spell == null) {
				NostrumMagica.logger.warn("Could not find matching spell from client cast request");
				return null;
			}
			
			boolean isScroll = message.tag.getBoolean(NBT_SCROLL);
			
			INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
			
			if (att == null) {
				NostrumMagica.logger.warn("Could not look up player magic wrapper");
				return null;
			}
			
			// Cast it!
			boolean seen = att.wasSpellDone(spell);
			float xp = spell.getXP(seen);
			int cost = spell.getManaCost();
			SpellCastSummary summary = new SpellCastSummary(cost, xp);
			
			if (!isScroll) {
				// Find the tome this was cast from, if any
				ItemStack tome = sp.getHeldItemMainhand();
				if (tome == null || !(tome.getItem() instanceof SpellTome))
					tome = sp.getHeldItemOffhand();
				
				if (tome != null && tome.getItem() instanceof SpellTome) {
					// Casting from a tome.
					List<EnhancementWrapper> enhancements = SpellTome.getEnhancements(tome);
					if (enhancements != null && !enhancements.isEmpty())
					for (EnhancementWrapper enhance : enhancements) {
						enhance.getEnhancement().onCast(
								enhance.getLevel(), summary, sp, att);
					}
				}
			}
			
			cost = summary.getFinalCost();
			xp = summary.getFinalXP();
			
			if (!sp.isCreative() && !isScroll) {
				// Take mana and reagents
				
				if (att.getMana() < cost)
					return new ClientCastReplyMessage(false, att.getMana(), 0.0f);
				
				Map<ReagentType, Integer> reagents = spell.getRequiredReagents();
				applyReagentRate(reagents, summary.getReagentCost());
				for (Entry<ReagentType, Integer> row : reagents.entrySet()) {
					int count = NostrumMagica.getReagentCount(sp, row.getKey());
					if (count < row.getValue()) {
						return new ClientCastReplyMessage(false, att.getMana(), 0);
					}
				}
				// actually deduct
				for (Entry<ReagentType, Integer> row : reagents.entrySet()) {
					NostrumMagica.removeReagents(sp, row.getKey(), row.getValue());
				}
				
				att.addMana(-cost);
			}
			
			spell.cast(sp, summary.getEfficiency());
			att.addXP(xp);

			return new ClientCastReplyMessage(true, att.getMana(), xp);
		}

		private void applyReagentRate(Map<ReagentType, Integer> reagents, float reagentCost) {
			// Take the total reagent cost rate and scale up/down the number of reagents needed
			int whole = (int) reagentCost;
			float frac = reagentCost - (int) reagentCost;
			for (ReagentType type : reagents.keySet()) {
				// rate 1 just means whatever cost is there * 1
				// rate .5 means 50% chance to avoid each one. So if 10
				// are required, each rolls with 50% chance of still being needed.
				// rate 1.5 means 100% of the requirements + a roll for each with
				// 50% chance of adding another.
				int def = reagents.get(type);
				int cost = (def * whole);
				for (; def > 0; def--) {
					if (NostrumMagica.rand.nextFloat() < frac)
						cost++;
				}
				
				reagents.put(type, cost);
			}
		}
		
	}

	private static final String NBT_ID = "id";
	private static final String NBT_SCROLL = "isscroll";
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected NBTTagCompound tag;
	
	public ClientCastMessage() {
		tag = new NBTTagCompound();
	}
	
	public ClientCastMessage(Spell spell, boolean scroll) {
		this(spell.getRegistryID(), scroll);
	}
	
	public ClientCastMessage(int id, boolean scroll) {
		tag = new NBTTagCompound();
		
		tag.setInteger(NBT_ID, id);
		tag.setBoolean(NBT_SCROLL, scroll);
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
