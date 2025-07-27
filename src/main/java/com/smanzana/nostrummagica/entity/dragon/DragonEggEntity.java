package com.smanzana.nostrummagica.entity.dragon;


import java.util.Optional;
import java.util.UUID;

import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.dragon.IDragonSpawnData.IDragonSpawnFactory;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HayBlock;

public class DragonEggEntity extends Mob implements ILoreSupplier {
	
	public static final String ID = "entity_dragon_egg";
	
	protected static final EntityDataAccessor<Float> HEAT  = SynchedEntityData.<Float>defineId(DragonEggEntity.class, EntityDataSerializers.FLOAT);
	protected static final EntityDataAccessor<Optional<UUID>> PLAYER  = SynchedEntityData.<Optional<UUID>>defineId(DragonEggEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	
	private static final String NBT_AGE_TIMER = "age";
	private static final String NBT_DRAGON_TYPE = "spawn_type";
	private static final String NBT_DRAGON_DATA = "spawn_data";
	private static final String NBT_HEAT = "heat";
	private static final String NBT_PLAYER = "notify_player";
	
	public static final float HEAT_MAX = 50f;
	
	private IDragonSpawnData<? extends ITameDragon> spawnData;
	private int ageTimer;

	public DragonEggEntity(EntityType<? extends DragonEggEntity> type, Level worldIn) {
		super(type, worldIn);
		ageTimer = 20 * 60 * 5; // Base hatching time. Can be overriden by saved NBT
	}
	
	public DragonEggEntity(EntityType<? extends DragonEggEntity> type, Level worldIn, Player player, IDragonSpawnData<? extends ITameDragon> spawnData) {
		this(type, worldIn);
		this.spawnData = spawnData;
		
		if (player != null && !worldIn.isClientSide) {
			this.setPlayerUUID(player.getUUID());
		}
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		
		this.entityData.define(HEAT, HEAT_MAX);
		this.entityData.define(PLAYER, Optional.<UUID>empty());
	}

	public static final AttributeSupplier.Builder BuildAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 2D)
				.add(Attributes.ARMOR, 0D);
	}
	
	
	@Override
	public void knockback(double strength, double xRatio, double zRatio) {
		return; // Do not get knocked around
	}
	
	@Override
	public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
		return this.hurt(DamageSource.FALL, 9999f);
	}
	
	@Override
	public boolean isPushable() {
		return false;
	}
	
	@Override
	public void push(Entity entityIn) {
		return;
	}
	
	@Override
	protected void doPush(Entity entity) {
		if (entity instanceof ITameDragon || entity instanceof Player) {
			
		} else {
			super.doPush(entity);
		}
	}
	
	@Override
	protected void pushEntities() {
		
	}
	
	@Override
	protected int decreaseAirSupply(int air) {
		return 0;
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		
		compound.putFloat(NBT_HEAT, this.getHeat());
		compound.putInt(NBT_AGE_TIMER, ageTimer);
		
		UUID playerID = getPlayerID();
		if (playerID != null) {
			compound.putUUID(NBT_PLAYER, getPlayerID());
		}
		
		if (this.spawnData != null) {
			CompoundTag dataTag = new CompoundTag();
			this.spawnData.writeToNBT(dataTag);
			compound.put(NBT_DRAGON_DATA, dataTag);
			compound.putString(NBT_DRAGON_TYPE, spawnData.getKey());
		}
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		
		if (compound.contains(NBT_HEAT)) {
			this.setHeat(compound.getFloat(NBT_HEAT));
		}
		
		if (compound.contains(NBT_AGE_TIMER)) {
			this.ageTimer = compound.getInt(NBT_AGE_TIMER);
		}
		
		if (compound.hasUUID(NBT_PLAYER)) {
			this.setPlayerUUID(compound.getUUID(NBT_PLAYER));
		}
		
		if (compound.contains(NBT_DRAGON_TYPE)) {
			IDragonSpawnFactory factory = IDragonSpawnData.lookupFactory(compound.getString(NBT_DRAGON_TYPE));
			this.spawnData = factory.create(compound.getCompound(NBT_DRAGON_DATA));
		}
	}
	
	@Override
	public void aiStep() {
		super.aiStep();
		
		if (this.isAlive() && !this.dead) {
			if (!level.isClientSide && this.tickCount > 20) {
				float heatLoss = .05f;
				
				if (level.isRainingAt(blockPosition())) {
					heatLoss = .1f;
				} else if (level.getMaxLocalRawBrightness(this.blockPosition()) > 8) {
					if (level.getBlockState(blockPosition().offset(0, -1, 0)).getBlock() instanceof HayBlock) {
						heatLoss = 0f;
					}
				}
				
				this.setHeat(this.getHeat() - heatLoss);
				
				if (this.getHeat() <= 0f) {
					Player player = this.getPlayer();
					if (player != null) {
						player.sendMessage(new TranslatableComponent("info.egg.death.cold"), Util.NIL_UUID);
					}
					
					this.hurt(DamageSource.STARVE, 9999f);
				} else if (this.ageTimer-- <= 0) {
					// HATCH
					this.hatch();
				}
			}
		}
	}
	
	protected Player getPlayer() {
		UUID id = getPlayerID();
		Player player = null;
		
		if (id != null) {
			player = this.level.getPlayerByUUID(id);
		}
		
		return player;
	}
	
	protected UUID getPlayerID() {
		return entityData.get(PLAYER).orElse(null);
	}
	
	private void setPlayerUUID(UUID id) {
		entityData.set(PLAYER, Optional.ofNullable(id));
	}
	
	public float getHeat() {
		return this.entityData.get(HEAT);
	}
	
	protected void setHeat(float heat) {
		this.entityData.set(HEAT, Math.max(0, Math.min(HEAT_MAX, heat)));
	}
	
	public void heatUp() {
		this.setHeat(this.getHeat() + .10f);
	}
	
//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		this.entityDropItem(new ItemStack(NostrumItems.dragonEggFragment, 2), 0);
//	}
	
	private void hatch() {
		
		if (this.spawnData != null) {
			this.level.addFreshEntity((LivingEntity) this.spawnData.spawnDragon(level, getX(), getY(), getZ()));
			
			Player player = this.getPlayer();
			if (player != null) {
				player.sendMessage(new TranslatableComponent("info.egg.hatch"), Util.NIL_UUID);
			}
		}
		
		this.discard();
	}
	
	@Override
	public ILoreTagged getLoreTag() {
		return DragonEggLore.instance;
	}
	
	public static final class DragonEggLore implements IEntityLoreTagged<DragonEggEntity> {

		public static final DragonEggLore instance = new DragonEggLore();
		public static final DragonEggLore instance() {
			return instance;
		}
		
		@Override
		public String getLoreKey() {
			return "nostrum_dragon_egg_entity";
		}
	
		@Override
		public String getLoreDisplayName() {
			return "Caring For Dragon Eggs";
		}
	
		@Override
		public Lore getBasicLore() {
			// Never used
			return new Lore().add("");
		}
	
		@Override
		public Lore getDeepLore() {
			return new Lore().add("You've placed a dragon egg!", "Be careful, as dragon eggs must be kept warm in order to survive!", "To keep the egg warm, keep it near a strong source of light. Additionally, ensure the egg stays on-top of a bed of straw or hay.", "If the egg gets too cold (or if it falls and cracks, or is damaged), it will die, and the creature inside will perish.", "Be warned: Once hatched, the creature inside will be wild, and must be tamed!");
		}
		
		@Override
		public ELoreCategory getCategory() {
			return ELoreCategory.ENTITY;
		}

		@Override
		public EntityType<DragonEggEntity> getEntityType() {
			return NostrumEntityTypes.dragonEgg;
		}
	}
}
