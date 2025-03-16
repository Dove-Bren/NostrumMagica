package com.smanzana.nostrummagica.stat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerStatTracker extends SavedData {

	public static final String DATA_NAME = NostrumMagica.MODID + "_PlayerStats";
	
	private final Map<UUID, PlayerStats> playerTable;
	
	public PlayerStatTracker() {
		super();
		this.playerTable = new HashMap<>();
	}
	
	public static PlayerStatTracker Load(CompoundTag nbt) {
		PlayerStatTracker tracker = new PlayerStatTracker();
		tracker.load(nbt);
		return tracker;
	}
	
	public void load(CompoundTag nbt) {
		synchronized(this) {
			this.playerTable.clear();
			
			for (String key : nbt.getAllKeys()) {
				UUID id;
				try {
					id = UUID.fromString(key);
				} catch (IllegalArgumentException e) {
					NostrumMagica.logger.error("Failed to parse player id for playerstats: " + key);
					continue;
				}
				
				playerTable.put(id, PlayerStats.FromNBT(nbt.getCompound(key)));
			}
			
			NostrumMagica.logger.info("Loaded " + playerTable.size() + " player stats");
		}
	}

	@Override
	public CompoundTag save(CompoundTag nbt) {
		synchronized(this) {
			for (Entry<UUID, PlayerStats> entry : playerTable.entrySet()) {
					nbt.put(entry.getKey().toString(), entry.getValue().toNBT(null));
			}
		}
		
		return nbt;
	}
	
	public static final void Update(@Nonnull Player player, Consumer<PlayerStats> updater) {
		if (player.level.isClientSide()) {
			return;
		}
		
		PlayerStatTracker tracker = NostrumMagica.instance.getPlayerStats();
		PlayerStats stats = tracker.get(player);
		updater.accept(stats);
		tracker.update(player, stats);
	}
	
	public @Nonnull PlayerStats get(@Nonnull Player player) {
		return playerTable.computeIfAbsent(player.getUUID(), (u) -> new PlayerStats());
	}
	
	public void update(@Nonnull Player player, PlayerStats stats) {
		if (player.level.isClientSide()) {
			return;
		}
		if (!playerTable.containsKey(player.getUUID()) || stats != playerTable.get(player.getUUID())) {
			playerTable.put(player.getUUID(), stats);
		}
		
		this.setDirty();
	    NostrumMagica.instance.proxy.sendPlayerStatSync(player);
	}
	
	@OnlyIn(Dist.CLIENT)
	public void override(@Nonnull Player player, PlayerStats stats) {
		playerTable.put(player.getUUID(), stats);
	}
	
}
