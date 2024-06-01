package com.smanzana.nostrummagica.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerStatTracker extends WorldSavedData {

	public static final String DATA_NAME = NostrumMagica.MODID + "_PlayerStats";
	
	private final Map<UUID, PlayerStats> playerTable;
	
	public PlayerStatTracker() {
		this(DATA_NAME);
	}
	
	public PlayerStatTracker(String name) {
		super(name);
		this.playerTable = new HashMap<>();
	}
	
	@Override
	public void read(CompoundNBT nbt) {
		synchronized(this) {
			this.playerTable.clear();
			
			for (String key : nbt.keySet()) {
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
	public CompoundNBT write(CompoundNBT nbt) {
		synchronized(this) {
			for (Entry<UUID, PlayerStats> entry : playerTable.entrySet()) {
					nbt.put(entry.getKey().toString(), entry.getValue().toNBT(null));
			}
		}
		
		return nbt;
	}
	
	public PlayerStats get(@Nonnull PlayerEntity player) {
		return get(player.getUniqueID());
	}
	
	public @Nonnull PlayerStats get(@Nonnull UUID playerID) {
		PlayerStats stats = playerTable.get(playerID);
		if (stats == null) {
			stats = new PlayerStats();
			playerTable.put(playerID, stats);
		}
		return stats;
	}
	
	public void update(@Nonnull PlayerEntity player, PlayerStats stats) {
		update(player.getUniqueID(), stats);
	}
	
	public void update(@Nonnull UUID playerID, PlayerStats stats) {
		if (!playerTable.containsKey(playerID) || stats != playerTable.get(playerID)) {
			playerTable.put(playerID, stats);
		}
		this.markDirty();
	}
	
	@OnlyIn(Dist.CLIENT)
	public void override(@Nonnull PlayerEntity player, PlayerStats stats) {
		override(player.getUniqueID(), stats);
	}
	
	@OnlyIn(Dist.CLIENT)
	public void override(@Nonnull UUID playerID, PlayerStats stats) {
		playerTable.put(playerID, stats);
	}
	
}
