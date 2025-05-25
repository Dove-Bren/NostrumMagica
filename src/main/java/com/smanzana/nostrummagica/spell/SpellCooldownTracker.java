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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Like Vanilla's cooldown tracker, except for spells
 * @author Skyler
 *
 */
public class SpellCooldownTracker {

	private final Map<Player, Cooldowns> cooldowns;
	
	public SpellCooldownTracker() {
		this.cooldowns = new HashMap<>();
	}
	
	public @Nonnull Cooldowns getCooldowns(Player player) {
		return cooldowns.computeIfAbsent(player, (p) -> new Cooldowns());
	}
	
	public @Nullable SpellCooldown getSpellCooldown(Player player, RegisteredSpell spell) {
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
	
	public boolean hasCooldown(Player player, RegisteredSpell spell) {
		@Nullable SpellCooldown cooldown = this.getSpellCooldown(player, spell);
		if (cooldown == null) { 
			return false;
		}
		
		return player.tickCount < cooldown.endTicks;
	}
	
	public void setSpellCooldown(Player player, RegisteredSpell spell, int ticks) {
		SpellCooldown cooldown = new SpellCooldown(player.tickCount, player.tickCount + ticks);
		cooldowns.computeIfAbsent(player, (p) -> new Cooldowns()).setSpellCooldown(spell, cooldown);
		notifyPlayer(player, spell, ticks);
	}
	
	public void removeSpellCooldown(Player player, RegisteredSpell spell) {
		setSpellCooldown(player, spell, 0);
	}
	
	public void overrideSpellCooldown(Player player, RegisteredSpell spell, int cooldownTicks) {
		SpellCooldown cooldown = new SpellCooldown(player.tickCount, player.tickCount + cooldownTicks);
		cooldowns.computeIfAbsent(player, (p) -> new Cooldowns()).setSpellCooldown(spell, cooldown);
	}
	
	public void setGlobalCooldown(Player player, int ticks) {
		SpellCooldown cooldown = new SpellCooldown(player.tickCount, player.tickCount + ticks);
		cooldowns.computeIfAbsent(player, (p) -> new Cooldowns()).setGlobalCooldown(cooldown);
		notifyPlayer(player, ticks);
	}
	
	public void overrideGlobalCooldown(Player player, int cooldownTicks) {
		SpellCooldown cooldown = new SpellCooldown(player.tickCount, player.tickCount + cooldownTicks);
		cooldowns.computeIfAbsent(player, (p) -> new Cooldowns()).setGlobalCooldown(cooldown);
	}
	
	protected void notifyPlayer(Player player, RegisteredSpell spell, int cooldown) {
		NetworkHandler.sendTo(new SpellCooldownMessage(spell, cooldown), (ServerPlayer) player);
	}
	
	protected void notifyPlayer(Player player, int cooldown) {
		NetworkHandler.sendTo(new SpellGlobalCooldownMessage(cooldown), (ServerPlayer) player);
	}
	
	public void clearCooldowns(Player player) {
		cooldowns.computeIfAbsent(player, (p) -> new Cooldowns()).clear();
		if (!player.level.isClientSide()) {
			NetworkHandler.sendTo(new SpellCooldownResetMessage(), (ServerPlayer) player);
		}
	}
	
	public static final class Cooldowns {
		private final Map<Integer, SpellCooldown> spellCooldowns;
		private SpellCooldown globalCooldown;
		
		public Cooldowns() {
			spellCooldowns = new HashMap<>();
			globalCooldown = new SpellCooldown(0, 0);
		}
		
		public void setSpellCooldown(RegisteredSpell spell, SpellCooldown cooldown) {
			spellCooldowns.put(spell.getRegistryID(), cooldown);
		}
		
		public void setGlobalCooldown(SpellCooldown cooldown) {
			globalCooldown = cooldown;
		}
		
		public @Nullable SpellCooldown getSpellCooldown(RegisteredSpell spell) {
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
