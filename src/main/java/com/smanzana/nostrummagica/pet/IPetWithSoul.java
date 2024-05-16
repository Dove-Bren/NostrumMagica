package com.smanzana.nostrummagica.pet;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public interface IPetWithSoul {

	public @Nonnull UUID getPetSoulID();
	
	public @Nonnull UUID getWorldID();
	
	public CompoundNBT serializeNBT();

	
	/**
	 * Convenience wrapper for recreating a generic entity out of a saved snapshot nbt.
	 * Individuals items etc. that may do the spawning should wrap this and, say, set the new
	 * worldID on the entity.
	 * This does NOT add the entity to the world.
	 * @param world
	 * @param pos
	 * @param snapshot
	 * @return
	 */
	public static @Nullable Entity CreatePetFromSnapshot(World world, Vector3d pos, CompoundNBT snapshot) {
		// return AnvilChunkLoader.readWorldEntityPos(snapshot, world, pos.x, pos.y, pos.z, worldSpawn);
		
		// Could use "EntityType.func_220335_a" which is more like readWorldEntityPos in that it handles passengers,
		// But we don't want to support passengers.
		// Want to use "EntityType.loadEntity" but it's private, so do our own exception handling
		Entity ent;
		try {
			ent = EntityType.loadEntityUnchecked(snapshot, world).orElse(null);
			if (ent != null) {
				ent.setPosition(pos.x, pos.y, pos.z);
			}
		} catch (Exception e) {
			NostrumMagica.logger.error("Failed to spawn pet from snapshot: " + e.getMessage());
			e.printStackTrace();
			ent = null;
		}
		
		return ent;
	}
	
	public static final class SoulBoundLore implements ILoreTagged {
		
		private static SoulBoundLore instance = null;
		public static SoulBoundLore instance() {
			if (instance == null) {
				instance = new SoulBoundLore();
			}
			return instance;
		}

		@Override
		public String getLoreKey() {
			return "lore_generic_soulbound";
		}

		@Override
		public String getLoreDisplayName() {
			return "Soulbound Pets";
		}

		@Override
		public Lore getBasicLore() {
			return new Lore().add("You've soulbonded with one of your pets!", "This means that your pet trusted you enough to grant you access to its soul. For a powerful mage like you, this means you can bring the pet back from the dead should it die!", "Soulbonding usually produces a soul item linked to the pet. Keep this item safe, as it's the only way to get the pet back if it dies!");
		}

		@Override
		public Lore getDeepLore() {
			return new Lore().add("You've soulbonded with one of your pets!", "This means that your pet trusted you enough to grant you access to its soul. For a powerful mage like you, this means you can bring the pet back from the dead should it die!", "Soulbonding usually produces a soul item linked to the pet. Keep this item safe, as it's the only way to get the pet back if it dies!");
		}

		@Override
		public InfoScreenTabs getTab() {
			// Don't actually display! We're going to show our own page!
			return InfoScreenTabs.INFO_GUIDES;
		}
		
	}
}
