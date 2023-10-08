package com.smanzana.nostrummagica.network.messages;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicPotency;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.items.ISpellArmor;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

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
		
		final ServerPlayerEntity sp = ctx.get().getSender();
		
		// What spell?
		Spell spell = NostrumMagica.instance.getSpellRegistry().lookup(
				message.id
				);
		
		if (spell == null) {
			NostrumMagica.logger.warn("Could not find matching spell from client cast request");
			return;
		}
		
		boolean isScroll = message.isScroll;
		int tomeID = message.tomeId;
		
		ctx.get().enqueueWork(() -> {
			
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
			
			// Add player's base magic potency
			summary.addEfficiency((float) sp.getAttribute(AttributeMagicPotency.instance()).getValue() / 100f);
			
			// Add the player's personal bonuses
			summary.addCostRate(att.getManaCostModifier());
			ItemStack tome = ItemStack.EMPTY;
			if (!isScroll) {
				// Find the tome this was cast from, if any
				tome = NostrumMagica.findTome(sp, tomeID);
				
				if (!tome.isEmpty() && tome.getItem() instanceof SpellTome
						&& SpellTome.getTomeID(tome) == tomeID) {
					// Casting from a tome.
					
					// Check if base mana cost exceeds what we can do
					int cap = SpellTome.getMaxMana(tome);
					if (cap < cost) {
						NetworkHandler.sendTo(new ClientCastReplyMessage(false, att.getMana(), 0, null),
								ctx.get().getSender());
						return;
					}
					
					SpellTome.applyEnhancements(tome, summary, sp);
					
				} else {
					NostrumMagica.logger.warn("Got cast from client with mismatched tome");
					NetworkHandler.sendTo(new ClientCastReplyMessage(false, att.getMana(), 0, null),
							ctx.get().getSender());
					return;
				}
			}
			
			if (isScroll) {
				// Scrolls cost no reagents
				summary.addReagentCost(-summary.getReagentCost());
			}
			
			// Cap enhancements at 80% LRC
			if (!isScroll)
			{
				float lrc = summary.getReagentCost();
				if (lrc < .2f)
					summary.addCostRate(.2f - lrc); // Add however much we need to get to 1
			}
			
			// Visit an equipped spell armor
			for (ItemStack equip : sp.getEquipmentAndArmor()) {
				if (equip.isEmpty())
					continue;
				if (equip.getItem() instanceof ISpellArmor) {
					ISpellArmor armor = (ISpellArmor) equip.getItem();
					armor.apply(sp, summary, equip);
				}
			}
			
			// Possibly use baubles
			IInventory curios = NostrumMagica.instance.curios.getCurios(sp);
			if (curios != null) {
				for (int i = 0; i < curios.getSizeInventory(); i++) {
					ItemStack equip = curios.getStackInSlot(i);
					if (equip.isEmpty()) {
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
				
				int mana = att.getMana();
				
				// Add dragon mana pool
				Collection<ITameDragon> dragons = NostrumMagica.getNearbyTamedDragons(sp, 32, true);
				if (dragons != null && !dragons.isEmpty()) {
					for (ITameDragon dragon : dragons) {
						if (dragon.sharesMana(sp)) {
							mana += dragon.getMana();
						}
					}
				}
				
				if (mana < cost) {
					NetworkHandler.sendTo(new ClientCastReplyMessage(false, att.getMana(), 0.0f, null),
							ctx.get().getSender());
					return;
				}
				
				// Check that the player can cast this
				if (!NostrumMagica.canCast(spell, att)) {
					NostrumMagica.logger.warn("Got cast message from client with too low of stats. They should relog...");
					NetworkHandler.sendTo(new ClientCastReplyMessage(false, att.getMana(), 0, null),
							ctx.get().getSender());
					return;
				}
				
				reagents = spell.getRequiredReagents();
				
				// Scan inventory for any applicable discounts
				
				applyReagentRate(reagents, reagentCost);
				for (Entry<ReagentType, Integer> row : reagents.entrySet()) {
					int count = NostrumMagica.getReagentCount(sp, row.getKey());
					if (count < row.getValue()) {
						sp.sendMessage(new TranslationTextComponent("info.spell.bad_reagent", row.getKey().prettyName()));
						NetworkHandler.sendTo(new ClientCastReplyMessage(false, att.getMana(), 0, null),
								ctx.get().getSender());
						return;
					}
				}
				// actually deduct
				for (Entry<ReagentType, Integer> row : reagents.entrySet()) {
					NostrumMagica.removeReagents(sp, row.getKey(), row.getValue());
				}
				
				
				// Find some way to pay the mana cost
				int avail = att.getMana();
				if (avail >= cost) {
					att.addMana(-cost);
					cost = 0;
				} else {
					att.addMana(-avail);
					cost -= avail;
				}
				
				if (cost > 0 && dragons != null) {
					for (ITameDragon dragon : dragons) {
						LivingEntity ent = (LivingEntity) dragon;
						NostrumMagica.instance.proxy.spawnEffect(sp.world, new SpellComponentWrapper(BeamTrigger.instance()),
								null, sp.getPositionVector().add(0, sp.getEyeHeight(), 0),
								null, ent.getPositionVector().add(0, ent.getEyeHeight(), 0),
								new SpellComponentWrapper(EMagicElement.ICE), false, 0);
						
						int dAvail = dragon.getMana();
						if (dAvail >= cost) {
							dragon.addMana(-cost);
							break;
						} else {
							dragon.addMana(-dAvail);
							cost -= dAvail;
						}
					}
				}
			}
			
			if (!isScroll) {
				// little hook here for extra effects
				SpellTome.doSpecialCastEffects(tome, sp);
			}
			
			spell.cast(sp, summary.getEfficiency());
			
			// No xp if magic isn't unlocked
			if (!att.isUnlocked()) {
				xp = 0;
			}
			
			att.addXP(xp);

			NetworkHandler.sendTo(new ClientCastReplyMessage(true, att.getMana(), xp, reagents),
					ctx.get().getSender());
		});
	}

	private static void applyReagentRate(Map<ReagentType, Integer> reagents, float reagentCost) {
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

	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	private final int id;
	private final int tomeId;
	private final boolean isScroll;
	
	public ClientCastMessage(Spell spell, boolean scroll, int tomeID) {
		this(spell.getRegistryID(), scroll, tomeID);
	}
	
	public ClientCastMessage(int id, boolean scroll, int tomeID) {
		this.id = id;
		this.isScroll = scroll;
		this.tomeId = tomeID;
	}

	public static ClientCastMessage decode(PacketBuffer buf) {
		return new ClientCastMessage(buf.readInt(), buf.readBoolean(), buf.readInt());
	}

		public static void encode(ClientCastMessage msg, PacketBuffer buf) {
		buf.writeInt(msg.id);
		buf.writeBoolean(msg.isScroll);
		buf.writeInt(msg.tomeId);
	}

}
