package com.smanzana.nostrummagica.entity;


import java.util.UUID;

import com.google.common.base.Optional;
import com.smanzana.nostrummagica.entity.IDragonSpawnData.IDragonSpawnFactory;
import com.smanzana.nostrummagica.items.DragonEggFragment;

import net.minecraft.block.BlockHay;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class EntityDragonEgg extends EntityLiving {
	
	protected static final DataParameter<Float> HEAT  = EntityDataManager.<Float>createKey(EntityDragonEgg.class, DataSerializers.FLOAT);
	protected static final DataParameter<Optional<UUID>> PLAYER  = EntityDataManager.<Optional<UUID>>createKey(EntityDragonEgg.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	
	private static final String NBT_AGE_TIMER = "age";
	private static final String NBT_DRAGON_TYPE = "spawn_type";
	private static final String NBT_DRAGON_DATA = "spawn_data";
	private static final String NBT_HEAT = "heat";
	private static final String NBT_PLAYER = "notify_player";
	
	public static final float HEAT_MAX = 50f;
	
	private IDragonSpawnData<? extends ITameDragon> spawnData;
	private int ageTimer;

	public EntityDragonEgg(World worldIn) {
		super(worldIn);
		this.setSize(.45f, .5f);
		ageTimer = 20 * 60 * 5; // Base hatching time. Can be overriden by saved NBT
	}
	
	public EntityDragonEgg(World worldIn, EntityPlayer player, IDragonSpawnData<? extends ITameDragon> spawnData) {
		this(worldIn);
		this.spawnData = spawnData;
		
		if (player != null && !worldIn.isRemote) {
			this.setPlayerUUID(player.getUniqueID());
		}
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		
		this.dataManager.register(HEAT, HEAT_MAX);
		this.dataManager.register(PLAYER, Optional.<UUID>absent());
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(2D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0D);
	}
	
	
	@Override
	public void knockBack(Entity entityIn, float strenght, double xRatio, double zRatio) {
		return; // Do not get knocked around
	}
	
	@Override
	public void fall(float distance, float damageMultiplier) {
		this.attackEntityFrom(DamageSource.fall, 9999f);
	}
	
	@Override
	public boolean canBePushed() {
		return false;
	}
	
	@Override
	public void applyEntityCollision(Entity entityIn) {
		return;
	}
	
	@Override
	protected void collideWithEntity(Entity entity) {
		if (entity instanceof ITameDragon || entity instanceof EntityPlayer) {
			
		} else {
			super.collideWithEntity(entity);
		}
	}
	
	@Override
	protected void collideWithNearbyEntities() {
		
	}
	
	@Override
	protected int decreaseAirSupply(int air) {
		return 0;
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		
		compound.setFloat(NBT_HEAT, this.getHeat());
		compound.setInteger(NBT_AGE_TIMER, ageTimer);
		
		UUID playerID = getPlayerID();
		if (playerID != null) {
			compound.setUniqueId(NBT_PLAYER, getPlayerID());
		}
		
		if (this.spawnData != null) {
			NBTTagCompound dataTag = new NBTTagCompound();
			this.spawnData.writeToNBT(dataTag);
			compound.setTag(NBT_DRAGON_DATA, dataTag);
			compound.setString(NBT_DRAGON_TYPE, spawnData.getKey());
		}
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		
		if (compound.hasKey(NBT_HEAT)) {
			this.setHeat(compound.getFloat(NBT_HEAT));
		}
		
		if (compound.hasKey(NBT_AGE_TIMER)) {
			this.ageTimer = compound.getInteger(NBT_AGE_TIMER);
		}
		
		if (compound.hasKey(NBT_PLAYER)) {
			this.setPlayerUUID(compound.getUniqueId(NBT_PLAYER));
		}
		
		if (compound.hasKey(NBT_DRAGON_TYPE)) {
			IDragonSpawnFactory factory = IDragonSpawnData.lookupFactory(compound.getString(NBT_DRAGON_TYPE));
			this.spawnData = factory.create(compound.getCompoundTag(NBT_DRAGON_DATA));
		}
	}
	
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		
		if (!this.isDead && !this.dead) {
			if (!worldObj.isRemote && this.ticksExisted > 20) {
				float heatLoss = .05f;
				
				if (worldObj.isRainingAt(getPosition())) {
					heatLoss = .1f;
				} else if (worldObj.getLight(this.getPosition()) > 8) {
					if (worldObj.getBlockState(getPosition().add(0, -1, 0)).getBlock() instanceof BlockHay) {
						heatLoss = 0f;
					}
				}
				
				this.setHeat(this.getHeat() - heatLoss);
				
				if (this.getHeat() <= 0f) {
					EntityPlayer player = this.getPlayer();
					if (player != null) {
						player.addChatComponentMessage(new TextComponentTranslation("info.egg.death.cold"));
					}
					
					this.attackEntityFrom(DamageSource.starve, 9999f);
				} else if (this.ageTimer-- <= 0) {
					// HATCH
					this.hatch();
				}
			}
		}
	}
	
	protected EntityPlayer getPlayer() {
		UUID id = getPlayerID();
		EntityPlayer player = null;
		
		if (id != null) {
			player = this.worldObj.getPlayerEntityByUUID(id);
		}
		
		return player;
	}
	
	protected UUID getPlayerID() {
		return dataManager.get(PLAYER).orNull();
	}
	
	private void setPlayerUUID(UUID id) {
		dataManager.set(PLAYER, Optional.fromNullable(id));
	}
	
	public float getHeat() {
		return this.dataManager.get(HEAT);
	}
	
	protected void setHeat(float heat) {
		this.dataManager.set(HEAT, Math.max(0, Math.min(HEAT_MAX, heat)));
	}
	
	public void heatUp() {
		this.setHeat(this.getHeat() + .10f);
	}
	
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		this.entityDropItem(new ItemStack(DragonEggFragment.instance(), 2), 0);
	}
	
	private void hatch() {
		
		if (this.spawnData != null) {
			this.worldObj.spawnEntityInWorld((EntityLivingBase) this.spawnData.spawnDragon(worldObj, posX, posY, posZ));
			
			EntityPlayer player = this.getPlayer();
			if (player != null) {
				player.addChatComponentMessage(new TextComponentTranslation("info.egg.hatch"));
			}
		}
		
		this.setDead();
	}
}
