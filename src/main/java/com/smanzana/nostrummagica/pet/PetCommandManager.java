package com.smanzana.nostrummagica.pet;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.PetCommandSettingsSyncMessage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Pet command and settings manager.
 * Does two jobs:
 * 1) Stores various settings about how intelligent pets should act through saves
 * 2) Handles requests to direct pet actions
 * @author Skyler
 *
 */
public class PetCommandManager extends WorldSavedData {
	
	private static final class PetCommandSettings {
		
		private static final String NBT_ENTRY_PLACEMENT = "placement";
		private static final String NBT_ENTRY_TARGET = "target";
		
		public static PetCommandSettings Empty = new PetCommandSettings();
		
		public PetPlacementMode placementMode;
		public PetTargetMode targetMode;
		
		public PetCommandSettings() {
			placementMode = PetPlacementMode.FREE;
			targetMode = PetTargetMode.FREE;
		}
		
		public CompoundNBT writeToNBT(@Nullable CompoundNBT nbt) {
			if (nbt == null) {
				nbt = new CompoundNBT();
			}
			
			nbt.putString(NBT_ENTRY_PLACEMENT, placementMode.name());
			nbt.putString(NBT_ENTRY_TARGET, targetMode.name());
			
			return nbt;
		}
		
		public static PetCommandSettings FromNBT(CompoundNBT nbt) {
			PetCommandSettings settings = new PetCommandSettings();
			
			try {
				settings.placementMode = PetPlacementMode.valueOf(nbt.getString(NBT_ENTRY_PLACEMENT).toUpperCase());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				settings.targetMode = PetTargetMode.valueOf(nbt.getString(NBT_ENTRY_TARGET).toUpperCase());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return settings;
		}
	}

	public static final String DATA_NAME =  NostrumMagica.MODID + "_PetCommandData";
	
	private static final String NBT_SETTINGS = "playerSettings";
	
	private Map<UUID, PetCommandSettings> playerSettings;
	
	public PetCommandManager() {
		this(DATA_NAME);
	}
	
	public PetCommandManager(String name) {
		super(name);
		this.playerSettings = new HashMap<>();
	}
	
	@Override
	public void readFromNBT(CompoundNBT nbt) {
		synchronized(playerSettings) {
			playerSettings.clear();
			
			CompoundNBT subtag = nbt.getCompound(NBT_SETTINGS);
			for (String key : subtag.keySet()) {
				UUID uuid = null;
				try {
					uuid = UUID.fromString(key);
				} catch (Exception e) {
					e.printStackTrace();
					uuid = null;
				}
				
				if (uuid == null) {
					continue;
				}
				
				playerSettings.put(uuid, PetCommandSettings.FromNBT(subtag.getCompound(key)));
			}
		}
	}
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT compound) {
		synchronized(playerSettings) {
			CompoundNBT subtag = new CompoundNBT();
			for (Entry<UUID, PetCommandSettings> entry : playerSettings.entrySet()) {
				subtag.put(entry.getKey().toString(), entry.get().writeToNBT(null));
			}
			compound.put(NBT_SETTINGS, subtag);
		}
		
		return compound;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void overrideClientSettings(CompoundNBT settingsNBT) {
		PetCommandSettings settings = PetCommandSettings.FromNBT(settingsNBT);
		final UUID ID = NostrumMagica.instance.proxy.getPlayer().getUniqueID();
		synchronized(playerSettings) {
			playerSettings.put(ID, settings);
		}
	}
	
	protected CompoundNBT generateClientSettings(UUID clientID) {
		CompoundNBT nbt = new CompoundNBT();
		
		PetCommandSettings settings = getSettings(clientID);
		nbt = settings.writeToNBT(nbt);
		
		return nbt;
	}
	
	@SubscribeEvent
	public void onConnect(PlayerLoggedInEvent event) {
		if (event.player.world.isRemote) {
			return;
		}
		
		NetworkHandler.sendTo(
				new PetCommandSettingsSyncMessage(generateClientSettings(event.player.getUniqueID())),
				(ServerPlayerEntity) event.player);
		
		NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) event.player);
	}
	
	protected @Nonnull PetCommandSettings getSettings(@Nonnull UUID uuid) {
		final PetCommandSettings settings;
		synchronized(playerSettings) {
			settings = playerSettings.get(uuid);
		}
		
		return settings == null ? PetCommandSettings.Empty : settings;
	}
	
	public PetPlacementMode getPlacementMode(LivingEntity entity) {
		return getPlacementMode(entity.getUniqueID());
	}
	
	public PetPlacementMode getPlacementMode(UUID uuid) {
		final PetCommandSettings settings = getSettings(uuid);
		return settings.placementMode;
	}
	
	public PetTargetMode getTargetMode(LivingEntity entity) {
		return getTargetMode(entity.getUniqueID());
	}
	
	public PetTargetMode getTargetMode(UUID uuid) {
		final PetCommandSettings settings = getSettings(uuid);
		return settings.targetMode;
	}
	
	public void setPlacementMode(LivingEntity entity, PetPlacementMode mode) {
		setPlacementMode(entity.getUniqueID(), mode);
	}
	
	public void setPlacementMode(UUID uuid, PetPlacementMode mode) {
		synchronized(playerSettings) {
			PetCommandSettings settings = playerSettings.get(uuid);
			if (settings == null) {
				settings = new PetCommandSettings();
				playerSettings.put(uuid, settings);
			}
			
			settings.placementMode = mode;
		}
		
		this.markDirty();
	}
	
	public void setTargetMode(LivingEntity entity, PetTargetMode mode) {
		setTargetMode(entity.getUniqueID(), mode);
	}
	
	public void setTargetMode(UUID uuid, PetTargetMode mode) {
		synchronized(playerSettings) {
			PetCommandSettings settings = playerSettings.get(uuid);
			if (settings == null) {
				settings = new PetCommandSettings();
				playerSettings.put(uuid, settings);
			}
			
			settings.targetMode = mode;
		}
		
		this.markDirty();
	}
	
	public void commandToAttack(LivingEntity owner, IEntityPet pet, LivingEntity target) {
		if (!owner.equals(pet.getOwner())) {
			return;
		}
		
		pet.onAttackCommand(target);
	}
	
	public void commandToAttack(LivingEntity owner, MobEntity pet, LivingEntity target) {
		if (pet instanceof IEntityPet) {
			commandToAttack(owner, (IEntityPet) pet, target);
			return;
		}
		
		if (pet instanceof IEntityOwnable) {
			if (!owner.equals(((IEntityOwnable) pet).getOwner())) {
				return;
			}
		}
		
		pet.setAttackTarget(target);
	}
	
	protected void forAllOwned(LivingEntity owner, Function<Entity, Integer> petAction) {
		for (LivingEntity e : NostrumMagica.getTamedEntities(owner)) {
			if (owner.getDistance(e) > 100) {
				continue;
			}
			
			petAction.apply(e);
		}
	}
	
	public void commandAllToAttack(LivingEntity owner, LivingEntity target) {
		forAllOwned(owner, (e) -> {
			if (e instanceof IEntityPet) {
				((IEntityPet) e).onAttackCommand(target);
			} else if (e instanceof MobEntity) {
				((MobEntity) e).setAttackTarget(target);
			}
			return 0;
		});
	}
	
	public void commandAllStopAttacking(LivingEntity owner) {
		forAllOwned(owner, (e) -> {
			if (e instanceof IEntityPet) {
				((IEntityPet) e).onStopCommand();
			} else if (e instanceof MobEntity) {
				((MobEntity) e).setAttackTarget(null);
			}
			return 0;
		});
	}
}
