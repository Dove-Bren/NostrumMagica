package com.smanzana.nostrummagica.spell;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.SpellCooldownMessage;
import com.smanzana.nostrummagica.network.message.SpellCooldownResetMessage;
import com.smanzana.nostrummagica.network.message.SpellGlobalCooldownMessage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * Like Vanilla's cooldown tracker, except for spells
 * @author Skyler
 *
 */
public class SpellCooldownTracker {

	private final Map<PlayerEntity, Cooldowns> cooldowns;
	
	public SpellCooldownTracker() {
		this.cooldowns = new HashMap<>();
	}
	
	public @Nonnull Cooldowns getCooldowns(PlayerEntity player) {
		return cooldowns.computeIfAbsent(player, (p) -> new Cooldowns());
	}
	
	public @Nullable SpellCooldown getSpellCooldown(PlayerEntity player, Spell spell) {
		Cooldowns cooldowns = getCooldowns(player);
		@Nullable SpellCooldown spellCooldown = cooldowns.getSpellCooldown(spell);
		@Nullable SpellCooldown globalCooldown = cooldowns.getGlobalCooldown();
		
		// Figure out which to use
		if (globalCooldown == null) {
			return spellCooldown;
		}
		if (spellCooldown == null) {
			return globalCooldown;
		}
		
		// If both present, pick whichever one is further
		return globalCooldown.endTicks > spellCooldown.endTicks ? globalCooldown : spellCooldown;
	}
	
	public boolean hasCooldown(PlayerEntity player, Spell spell) {
		@Nullable SpellCooldown cooldown = this.getSpellCooldown(player, spell);
		if (cooldown == null) { 
			return false;
		}
		
		return player.ticksExisted < cooldown.endTicks;
	}
	
	public void setSpellCooldown(PlayerEntity player, Spell spell, int ticks) {
		SpellCooldown cooldown = new SpellCooldown(player.ticksExisted, player.ticksExisted + ticks);
		cooldowns.computeIfAbsent(player, (p) -> new Cooldowns()).setSpellCooldown(spell, cooldown);
		notifyPlayer(player, spell, ticks);
	}
	
	public void removeSpellCooldown(PlayerEntity player, Spell spell) {
		setSpellCooldown(player, spell, 0);
	}
	
	public void overrideSpellCooldown(PlayerEntity player, Spell spell, int cooldownTicks) {
		SpellCooldown cooldown = new SpellCooldown(player.ticksExisted, player.ticksExisted + cooldownTicks);
		cooldowns.computeIfAbsent(player, (p) -> new Cooldowns()).setSpellCooldown(spell, cooldown);
	}
	
	public void setGlobalCooldown(PlayerEntity player, int ticks) {
		SpellCooldown cooldown = new SpellCooldown(player.ticksExisted, player.ticksExisted + ticks);
		cooldowns.computeIfAbsent(player, (p) -> new Cooldowns()).setGlobalCooldown(cooldown);
		notifyPlayer(player, ticks);
	}
	
	public void overrideGlobalCooldown(PlayerEntity player, int cooldownTicks) {
		SpellCooldown cooldown = new SpellCooldown(player.ticksExisted, player.ticksExisted + cooldownTicks);
		cooldowns.computeIfAbsent(player, (p) -> new Cooldowns()).setGlobalCooldown(cooldown);
	}
	
	protected void notifyPlayer(PlayerEntity player, Spell spell, int cooldown) {
		NetworkHandler.sendTo(new SpellCooldownMessage(spell, cooldown), (ServerPlayerEntity) player);
	}
	
	protected void notifyPlayer(PlayerEntity player, int cooldown) {
		NetworkHandler.sendTo(new SpellGlobalCooldownMessage(cooldown), (ServerPlayerEntity) player);
	}
	
	public void clearCooldowns(PlayerEntity player) {
		cooldowns.computeIfAbsent(player, (p) -> new Cooldowns()).clear();
		if (!player.world.isRemote()) {
			NetworkHandler.sendTo(new SpellCooldownResetMessage(), (ServerPlayerEntity) player);
		}
	}
	
	public static final class Cooldowns {
		private final Map<Integer, SpellCooldown> spellCooldowns;
		private SpellCooldown globalCooldown;
		
		public Cooldowns() {
			spellCooldowns = new HashMap<>();
			globalCooldown = new SpellCooldown(0, 0);
		}
		
		public void setSpellCooldown(Spell spell, SpellCooldown cooldown) {
			spellCooldowns.put(spell.getRegistryID(), cooldown);
		}
		
		public void setGlobalCooldown(SpellCooldown cooldown) {
			globalCooldown = cooldown;
		}
		
		public @Nullable SpellCooldown getSpellCooldown(Spell spell) {
			for (Entry<Integer, SpellCooldown> entry : spellCooldowns.entrySet()) {
				if (entry.getKey() == spell.getRegistryID()) {
					return entry.getValue();
				}
			}
			return null;
		}
		
		public SpellCooldown getGlobalCooldown() {
			return this.globalCooldown;
		}
		
		public void clear() {
			globalCooldown = new SpellCooldown(0, 0);
			spellCooldowns.clear();
		}
	}
	
	public static final class SpellCooldown {
		public final int startTicks;
		public final int endTicks;
		
		public SpellCooldown(int startTicks, int endTicks) {
			this.startTicks = startTicks;
			this.endTicks = endTicks;
		}
	}
}
