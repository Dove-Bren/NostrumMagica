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
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		
		public NBTTagCompound writeToNBT(@Nullable NBTTagCompound nbt) {
			if (nbt == null) {
				nbt = new NBTTagCompound();
			}
			
			nbt.setString(NBT_ENTRY_PLACEMENT, placementMode.name());
			nbt.setString(NBT_ENTRY_TARGET, targetMode.name());
			
			return nbt;
		}
		
		public static PetCommandSettings FromNBT(NBTTagCompound nbt) {
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
	public void readFromNBT(NBTTagCompound nbt) {
		synchronized(playerSettings) {
			playerSettings.clear();
			
			NBTTagCompound subtag = nbt.getCompoundTag(NBT_SETTINGS);
			for (String key : subtag.getKeySet()) {
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
				
				playerSettings.put(uuid, PetCommandSettings.FromNBT(subtag.getCompoundTag(key)));
			}
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		synchronized(playerSettings) {
			NBTTagCompound subtag = new NBTTagCompound();
			for (Entry<UUID, PetCommandSettings> entry : playerSettings.entrySet()) {
				subtag.setTag(entry.getKey().toString(), entry.getValue().writeToNBT(null));
			}
			compound.setTag(NBT_SETTINGS, subtag);
		}
		
		return compound;
	}
	
	@SideOnly(Side.CLIENT)
	public void overrideClientSettings(NBTTagCompound settingsNBT) {
		PetCommandSettings settings = PetCommandSettings.FromNBT(settingsNBT);
		final UUID ID = NostrumMagica.proxy.getPlayer().getUniqueID();
		synchronized(playerSettings) {
			playerSettings.put(ID, settings);
		}
	}
	
	protected NBTTagCompound generateClientSettings(UUID clientID) {
		NBTTagCompound nbt = new NBTTagCompound();
		
		PetCommandSettings settings = getSettings(clientID);
		nbt = settings.writeToNBT(nbt);
		
		return nbt;
	}
	
	@SubscribeEvent
	public void onConnect(PlayerLoggedInEvent event) {
		if (event.player.world.isRemote) {
			return;
		}
		
		NetworkHandler.getSyncChannel().sendTo(
				new PetCommandSettingsSyncMessage(generateClientSettings(event.player.getUniqueID())),
				(EntityPlayerMP) event.player);
		
		NostrumMagica.proxy.syncPlayer((EntityPlayerMP) event.player);
	}
	
	protected @Nonnull PetCommandSettings getSettings(@Nonnull UUID uuid) {
		final PetCommandSettings settings;
		synchronized(playerSettings) {
			settings = playerSettings.get(uuid);
		}
		
		return settings == null ? PetCommandSettings.Empty : settings;
	}
	
	public PetPlacementMode getPlacementMode(EntityLivingBase entity) {
		return getPlacementMode(entity.getUniqueID());
	}
	
	public PetPlacementMode getPlacementMode(UUID uuid) {
		final PetCommandSettings settings = getSettings(uuid);
		return settings.placementMode;
	}
	
	public PetTargetMode getTargetMode(EntityLivingBase entity) {
		return getTargetMode(entity.getUniqueID());
	}
	
	public PetTargetMode getTargetMode(UUID uuid) {
		final PetCommandSettings settings = getSettings(uuid);
		return settings.targetMode;
	}
	
	public void setPlacementMode(EntityLivingBase entity, PetPlacementMode mode) {
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
	
	public void setTargetMode(EntityLivingBase entity, PetTargetMode mode) {
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
	
	public void commandToAttack(EntityLivingBase owner, IEntityPet pet, EntityLivingBase target) {
		if (!owner.equals(pet.getOwner())) {
			return;
		}
		
		pet.onAttackCommand(target);
	}
	
	public void commandToAttack(EntityLivingBase owner, EntityLiving pet, EntityLivingBase target) {
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
	
	protected void forAllOwned(EntityLivingBase owner, Function<Entity, Integer> petAction) {
		for (EntityLivingBase e : NostrumMagica.getTamedEntities(owner)) {
			if (owner.getDistance(e) > 100) {
				continue;
			}
			
			petAction.apply(e);
		}
	}
	
	public void commandAllToAttack(EntityLivingBase owner, EntityLivingBase target) {
		forAllOwned(owner, (e) -> {
			if (e instanceof IEntityPet) {
				((IEntityPet) e).onAttackCommand(target);
			} else if (e instanceof EntityLiving) {
				((EntityLiving) e).setAttackTarget(target);
			}
			return 0;
		});
	}
	
	public void commandAllStopAttacking(EntityLivingBase owner) {
		forAllOwned(owner, (e) -> {
			if (e instanceof IEntityPet) {
				((IEntityPet) e).onStopCommand();
			} else if (e instanceof EntityLiving) {
				((EntityLiving) e).setAttackTarget(null);
			}
			return 0;
		});
	}
}
