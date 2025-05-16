package com.smanzana.nostrummagica.item;

import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.pet.IPetWithSoul;
import com.smanzana.nostrummagica.util.Entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Item that stores a pet's soul, allowing for resurrection once dead.
 * These items store the pet SoulID which should never change.
 * @author Skyler
 *
 */
public abstract class PetSoulItem extends Item implements ILoreTagged {

	//public static final String ID = "dragon_soul";
	private static final String NBT_PETID = "pet_soulID";
	private static final String NBT_PETNAME = "pet_name";
	
	public PetSoulItem(Item.Properties props) {
		super(props.stacksTo(1));
	}
	
	public @Nullable UUID getPetSoulID(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.hasUUID(NBT_PETID)) {
			return tag.getUUID(NBT_PETID);
		} else {
			return null;
		}
	}
	
	public void setPetSoulID(ItemStack stack, UUID rawSoulID) {
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
		}
		nbt.putUUID(NBT_PETID, rawSoulID);
		stack.setTag(nbt);
	}
	
	public @Nullable String getPetName(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag != null) {
			return tag.getString(NBT_PETNAME);
		} else {
			return null;
		}
	}
	
	public void setPetName(ItemStack stack, String name) {
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
		}
		nbt.putString(NBT_PETNAME, name);
		stack.setTag(nbt);
	}
	
	public void setPet(ItemStack stack, IPetWithSoul pet) {
		this.setPetSoulID(stack, pet.getPetSoulID());
		if (pet instanceof LivingEntity) {
			this.setPetName(stack, ((LivingEntity) pet).getName().getString());
		}
	}
	
	/**
	 * Check if the passed item, spawner, world, etc. are allowed to spawn the soulbound entity.
	 * This check should include sending messaging about why it failed, if appropriate. No messages will be sent
	 * if this function is called and returns false.
	 * @param world
	 * @param spawner
	 * @param pos
	 * @param stack
	 * @return
	 */
	public abstract boolean canSpawnEntity(Level world, @Nullable LivingEntity spawner, Vec3 pos, ItemStack stack);
	
	protected abstract void setWorldID(LivingEntity pet, UUID worldID);
	
	protected abstract void beforePetRespawn(LivingEntity pet, Level world, Vec3 pos, ItemStack stack);
	
	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		// Default is 6000 == 5 mins.
		return 18000;
	}
	
	public static @Nullable LivingEntity SpawnPet(ItemStack stack, Level world, Vec3 pos) {
		if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof PetSoulItem)) {
			return null;
		}
		
		final PetSoulItem soulItem = (PetSoulItem) stack.getItem();
		@Nullable UUID soulID = soulItem.getPetSoulID(stack);
		if (soulID == null) {
			return null;
		}
		
		CompoundTag snapshot = NostrumMagica.instance.getPetSoulRegistry().getPetSnapshot(soulID);
		if (snapshot == null) {
			return null;
		}
		
		UUID worldID = NostrumMagica.instance.getPetSoulRegistry().rotateWorldID(soulID);
		
		Entity rawEnt = IPetWithSoul.CreatePetFromSnapshot(world, pos, snapshot);
		if (rawEnt == null) {
			return null;
		} else if (!(rawEnt instanceof LivingEntity)) {
			rawEnt.discard();
			return null;
		}
		
		LivingEntity ent = (LivingEntity) rawEnt;
		
		// Check for other copies of the entity in the world, and remove them if so
		Entity staleEnt = Entities.FindEntity(world, ent.getUUID());
		if (staleEnt != null) {
			staleEnt.discard();
		}
		
		// Fix entity so it can respawn
		
		ent.revive();
		ent.setHealth(1f);
		
		// Finish setting it up
		soulItem.setWorldID(ent, worldID);
		soulItem.beforePetRespawn(ent, world, pos, stack);
		
		if (!world.addFreshEntity(ent)) {
			NostrumMagica.logger.error("Failed to spawn pet at least minute when adding it to the world!");
			ent = null;
		}
		
		return ent;
	}
}
