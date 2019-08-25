package com.smanzana.nostrummagica.network.messages;

import java.util.Map;
import java.util.Map.Entry;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.ISpellArmor;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
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
			int tomeID = message.tag.getInteger(NBT_TOME_ID);
			
			ctx.getServerHandler().playerEntity.getServerWorld().addScheduledTask(() -> {
				
				INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
				
				if (att == null) {
					NostrumMagica.logger.warn("Could not look up player magic wrapper");
					return;
				}
				
				// Cast it!
				boolean seen = att.wasSpellDone(spell);
				float xp = spell.getXP(seen);
				int cost = spell.getManaCost();
				SpellCastSummary summary = new SpellCastSummary(cost, xp);
				
				// Add the player's personal bonuses
				summary.addCostRate(att.getManaCostModifier());
				ItemStack tome = null;
				if (!isScroll) {
					// Find the tome this was cast from, if any
					tome = NostrumMagica.findTome(sp, tomeID);
					
					if (tome != null && tome.getItem() instanceof SpellTome
							&& SpellTome.getTomeID(tome) == tomeID) {
						// Casting from a tome.
						
						// Check if base mana cost exceeds what we can do
						int cap = SpellTome.getMaxMana(tome);
						if (cap < cost) {
							NetworkHandler.getSyncChannel().sendTo(new ClientCastReplyMessage(false, att.getMana(), 0, null),
									ctx.getServerHandler().playerEntity);
							return;
						}
						
						SpellTome.applyEnhancements(tome, summary, sp);
						
					} else {
						NostrumMagica.logger.warn("Got cast from client with mismatched tome");
						NetworkHandler.getSyncChannel().sendTo(new ClientCastReplyMessage(false, att.getMana(), 0, null),
								ctx.getServerHandler().playerEntity);
						return;
					}
				}
				
				// Cap enhancements at 80% LRC
				{
					float lrc = summary.getReagentCost();
					if (lrc < .2f)
						summary.addCostRate(.2f - lrc); // Add however much we need to get to 1
				}
				
				// Visit an equipped spell armor
				for (ItemStack equip : sp.getEquipmentAndArmor()) {
					if (equip == null)
						continue;
					if (equip.getItem() instanceof ISpellArmor) {
						ISpellArmor armor = (ISpellArmor) equip.getItem();
						armor.apply(sp, summary, equip);
					}
				}
				
				// Possible use baubles
				IInventory baubles = NostrumMagica.baubles.getBaubles(sp);
				if (baubles != null) {
					for (int i = 0; i < baubles.getSizeInventory(); i++) {
						ItemStack equip = baubles.getStackInSlot(i);
						if (equip == null) {
							continue;
						}
						
						if (equip.getItem() instanceof ISpellArmor) {
							ISpellArmor armor = (ISpellArmor) equip.getItem();
							armor.apply(sp, summary, equip);
						}
					}
				}
				
				cost = summary.getFinalCost();
				xp = summary.getFinalXP();
				float reagentCost = summary.getReagentCost();
				
				cost = Math.max(cost, 0);
				reagentCost = Math.max(reagentCost, 0);
				
				Map<ReagentType, Integer> reagents = null;
				
				if (!sp.isCreative() && !isScroll) {
					// Take mana and reagents
					
					if (att.getMana() < cost) {
						NetworkHandler.getSyncChannel().sendTo(new ClientCastReplyMessage(false, att.getMana(), 0.0f, null),
								ctx.getServerHandler().playerEntity);
						return;
					}
					
					// Check that the player can cast this
					if (!NostrumMagica.canCast(spell, att)) {
						NostrumMagica.logger.warn("Got cast message from client with too low of stats. They should relog...");
						NetworkHandler.getSyncChannel().sendTo(new ClientCastReplyMessage(false, att.getMana(), 0, null),
								ctx.getServerHandler().playerEntity);
						return;
					}
					
					reagents = spell.getRequiredReagents();
					
					// Scan inventory for any applicable discounts
					
					applyReagentRate(reagents, reagentCost);
					for (Entry<ReagentType, Integer> row : reagents.entrySet()) {
						int count = NostrumMagica.getReagentCount(sp, row.getKey());
						if (count < row.getValue()) {
							sp.addChatComponentMessage(new TextComponentTranslation("info.spell.bad_reagent", row.getKey().prettyName()));
							NetworkHandler.getSyncChannel().sendTo(new ClientCastReplyMessage(false, att.getMana(), 0, null),
									ctx.getServerHandler().playerEntity);
							return;
						}
					}
					// actually deduct
					for (Entry<ReagentType, Integer> row : reagents.entrySet()) {
						NostrumMagica.removeReagents(sp, row.getKey(), row.getValue());
					}
					
					att.addMana(-cost);
				}
				
				if (!isScroll) {
					// little hook here for extra effects
					SpellTome.doSpecialCastEffects(tome, sp);
				}
				
				spell.cast(sp, summary.getEfficiency());
				att.addXP(xp);

				NetworkHandler.getSyncChannel().sendTo(new ClientCastReplyMessage(true, att.getMana(), xp, reagents),
						ctx.getServerHandler().playerEntity);
			});
			
			return null;
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
				int cost;
				if (reagentCost <= 0f) {
					cost = 0;
				} else {
					int def = reagents.get(type);
					cost = (def * whole);
					for (; def > 0; def--) {
						if (NostrumMagica.rand.nextFloat() < frac)
							cost++;
					}
				}
				
				reagents.put(type, cost);
			}
		}
		
	}

	private static final String NBT_ID = "id";
	private static final String NBT_SCROLL = "isscroll";
	private static final String NBT_TOME_ID = "tome_id";
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected NBTTagCompound tag;
	
	public ClientCastMessage() {
		tag = new NBTTagCompound();
	}
	
	public ClientCastMessage(Spell spell, boolean scroll, int tomeID) {
		this(spell.getRegistryID(), scroll, tomeID);
	}
	
	public ClientCastMessage(int id, boolean scroll, int tomeID) {
		tag = new NBTTagCompound();
		
		tag.setInteger(NBT_ID, id);
		tag.setInteger(NBT_TOME_ID, tomeID);
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
