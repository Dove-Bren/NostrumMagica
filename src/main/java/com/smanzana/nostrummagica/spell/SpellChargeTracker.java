package com.smanzana.nostrummagica.spell;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.SpellChargeClientUpdateMessage;
import com.smanzana.nostrummagica.network.message.SpellChargeServerUpdateMessage;
import com.smanzana.nostrummagica.util.Entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Simply collects and routes information about entities charging up spells. This is traditional 'cast time.'
 * Opting to just route it all through one place instead of trying to make it an attribute or use MagicEffectProxy.
 * My gut told me to make it a tracked data parameter on entities, but the docs mention modifying that for entities you don't control
 * is a bad idea, so it wasn't clear how to put it in entities.
 * 
 * Spell charging is CLIENT owned. This component does exist on the server, but mainly serves to give visual clues about when things
 * are casting. For players, how long they cast and when they are done charging and emit the spell is client determined for a smoother
 * experience, with no real anti-cheat present.
 * 
 * Note that for dedicated servers, the tracker is present on each client and the server. Clients take updates about other entities
 * but remain authoritative (and issue updates to the server for) their own player entity. The server broadcasts out changes to other players.
 * For INTEGRATED servers, though, we only keep one tracker and both server and client poke into it.
 * 
 */
public class SpellChargeTracker {
	
	public static enum ChargeType {
		INCANTATION,
		TOME_CAST,
		SCROLL,
		;
	}
	
	public static record SpellCharge(Spell spell, int duration, ChargeType type) {
		
		public CompoundTag toNBT() {
			CompoundTag tag = new CompoundTag();
			
			tag.put("spell", spell.toNBT());
			tag.putInt("duration", duration);
			tag.putString("type", type.name());
			
			return tag;
		}
		
		public static final SpellCharge FromNBT(CompoundTag nbt) {
			Spell incant = Spell.FromNBT(nbt.getCompound("spell"));
			final int duration = nbt.getInt("duration");
			final ChargeType type = ChargeType.valueOf("type");
			
			return new SpellCharge(incant, duration, type);
		}
		
	}

	/**
	 *  Whether an entity (by its UUID) is charging.
	 */
	protected final Map<UUID, SpellCharge> chargeMap;
	
	public SpellChargeTracker() {
		chargeMap = new HashMap<>();
	}
	
	public synchronized boolean isCharging(Entity e) {
		return getCharge(e) != null;
	}
	
	public synchronized @Nullable SpellCharge getCharge(Entity e) {
		return e == null ? null : chargeMap.get(e.getUUID());
	}
	
	protected synchronized boolean setCharging(UUID id, @Nullable SpellCharge charge) {
		final boolean changed;
		if (charge != null) {
			changed = (Objects.equals(charge, chargeMap.put(id, charge)));
		} else {
			changed = chargeMap.remove(id) != null;
		}
		
		return changed;
	}
	
	/**
	 * Set a specific entity as charging.
	 * Expected to be called on the client for players, and on the server for non-player entities.
	 * @param e
	 * @param charge
	 */
	public void setCharging(Entity e, @Nullable SpellCharge charge) {
		if (setCharging(e.getUUID(), charge)) {
			broadcast(e);
		}
	}
	
	/**
	 * Override information with data received FROM the server BY the client.
	 * For player entities, this is received by all clients that aren't the player.
	 * For non-player entities, this is received by all clients.
	 * @param id
	 * @param charge
	 */
	public void overrideClientCharge(UUID id, @Nullable SpellCharge charge) {
		// id should NOT identify our entity
		// assert(id != proxy.getPlayer().getUUID);
		
		setCharging(id, charge);
	}
	
	public void overrideServerCharge(UUID id, @Nullable SpellCharge charge) {
		// For integrated servers, we don't want to touch the running client's data
		final @Nullable Player integratedPlayer = NostrumMagica.Proxy.getPlayer();
		if (integratedPlayer != null && integratedPlayer.getUUID().equals(id)) {
			return;
		}
		
		// On server, look for entity
		final @Nullable Entity ent = Entities.FindEntityAnyLevel(id);
		if (ent != null) {
			setCharging(ent, charge); // broadcasts back out
		}
	}
	
	protected void broadcast(Entity e) {
		// On a client, send to server if we updated our own matching player.
		// On a server, send to everyone except the entity
		if (e.getLevel().isClientSide()) {
			notifyServer(e);
		} else {
			notifyClients(e);
		}
	}
	
	private void notifyServer(Entity e) {
		// If e is our player, send to server
		if (e == NostrumMagica.Proxy.getPlayer()) {
			NetworkHandler.sendToServer(new SpellChargeClientUpdateMessage(getCharge(e)));
		}
	}
	
	private void notifyClients(Entity notMe) {
		// Send to all clients, EXCEPT notMe if they are a client
		NetworkHandler.sendToAllTrackingExcept(new SpellChargeServerUpdateMessage(notMe.getUUID(), getCharge(notMe)), notMe);
	}
	
}
