package com.smanzana.nostrummagica.items;

import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.pet.IPetWithSoul;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
	
	public PetSoulItem() {
		super();
		this.setMaxStackSize(1);
	}
	
	public @Nullable UUID getPetSoulID(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null) {
			return tag.getUniqueId(NBT_PETID);
		} else {
			return null;
		}
	}
	
	public void setPetSoulID(ItemStack stack, UUID rawSoulID) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		nbt.setUniqueId(NBT_PETID, rawSoulID);
		stack.setTagCompound(nbt);
	}
	
	public @Nullable String getPetName(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null) {
			return tag.getString(NBT_PETNAME);
		} else {
			return null;
		}
	}
	
	public void setPetName(ItemStack stack, String name) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		nbt.setString(NBT_PETNAME, name);
		stack.setTagCompound(nbt);
	}
	
	public void setPet(ItemStack stack, IPetWithSoul pet) {
		this.setPetSoulID(stack, pet.getPetSoulID());
		if (pet instanceof EntityLivingBase) {
			this.setPetName(stack, ((EntityLivingBase) pet).getName());
		}
	}
	
	protected abstract void setWorldID(EntityLivingBase pet, UUID worldID);
	
	protected abstract void beforePetRespawn(EntityLivingBase pet, World world, Vec3d pos, ItemStack stack);
	
	@Override
	public int getEntityLifespan(ItemStack itemStack, World world) {
		// Default is 6000 == 5 mins.
		return 18000;
	}
	
	public static @Nullable EntityLivingBase SpawnPet(ItemStack stack, World world, Vec3d pos) {
		if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof PetSoulItem)) {
			return null;
		}
		
		final PetSoulItem soulItem = (PetSoulItem) stack.getItem();
		@Nullable UUID soulID = soulItem.getPetSoulID(stack);
		if (soulID == null) {
			return null;
		}
		
		NBTTagCompound snapshot = NostrumMagica.getPetSoulRegistry().getPetSnapshot(soulID);
		if (snapshot == null) {
			return null;
		}
		
		UUID worldID = NostrumMagica.getPetSoulRegistry().rotateWorldID(soulID);
		
		Entity rawEnt = IPetWithSoul.SpawnPetFromSnapshot(world, pos, snapshot, false);
		if (rawEnt == null) {
			return null;
		} else if (!(rawEnt instanceof EntityLivingBase)) {
			rawEnt.isDead = true;
			//world.removeEntity(rawEnt);
			return null;
		}
		
		EntityLivingBase ent = (EntityLivingBase) rawEnt;
		
		// Fix entity so it can respawn
		ent.isDead = false;
		ent.setHealth(1f);
		
		// Finish setting it up
		soulItem.setWorldID(ent, worldID);
		soulItem.beforePetRespawn(ent, world, pos, stack);
		
		if (!world.spawnEntity(ent)) {
			NostrumMagica.logger.error("Failed to spawn pet at least minute when adding it to the world!");
			ent = null;
		}
		
		return ent;
	}
}
