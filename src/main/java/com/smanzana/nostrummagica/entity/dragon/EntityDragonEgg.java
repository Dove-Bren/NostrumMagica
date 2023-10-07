package com.smanzana.nostrummagica.entity.dragon;


import java.util.UUID;

import java.util.Optional;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.dragon.IDragonSpawnData.IDragonSpawnFactory;
import com.smanzana.nostrummagica.items.DragonEggFragment;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.BlockHay;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class EntityDragonEgg extends MobEntity implements ILoreTagged {
	
	public static final String ID = "entity_dragon_egg";
	
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

	public EntityDragonEgg(EntityType<? extends EntityDragonEgg> type, World worldIn) {
		super(type, worldIn);
		ageTimer = 20 * 60 * 5; // Base hatching time. Can be overriden by saved NBT
	}
	
	public EntityDragonEgg(EntityType<? extends EntityDragonEgg> type, World worldIn, PlayerEntity player, IDragonSpawnData<? extends ITameDragon> spawnData) {
		this(type, worldIn);
		this.spawnData = spawnData;
		
		if (player != null && !worldIn.isRemote) {
			this.setPlayerUUID(player.getUniqueID());
		}
	}
	
	@Override
	protected void registerData() { int unused; // TODO
		super.entityInit();
		
		this.dataManager.register(HEAT, HEAT_MAX);
		this.dataManager.register(PLAYER, Optional.<UUID>absent());
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(2D);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0D);
	}
	
	
	@Override
	public void knockBack(Entity entityIn, float strenght, double xRatio, double zRatio) {
		return; // Do not get knocked around
	}
	
	@Override
	public void fall(float distance, float damageMultiplier) {
		this.attackEntityFrom(DamageSource.FALL, 9999f);
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
		if (entity instanceof ITameDragon || entity instanceof PlayerEntity) {
			
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
	public void writeAdditional(CompoundNBT compound) {
		super.writeEntityToNBT(compound);
		
		compound.putFloat(NBT_HEAT, this.getHeat());
		compound.putInt(NBT_AGE_TIMER, ageTimer);
		
		UUID playerID = getPlayerID();
		if (playerID != null) {
			compound.setUniqueId(NBT_PLAYER, getPlayerID());
		}
		
		if (this.spawnData != null) {
			CompoundNBT dataTag = new CompoundNBT();
			this.spawnData.writeToNBT(dataTag);
			compound.put(NBT_DRAGON_DATA, dataTag);
			compound.putString(NBT_DRAGON_TYPE, spawnData.getKey());
		}
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readEntityFromNBT(compound);
		
		if (compound.contains(NBT_HEAT)) {
			this.setHeat(compound.getFloat(NBT_HEAT));
		}
		
		if (compound.contains(NBT_AGE_TIMER)) {
			this.ageTimer = compound.getInt(NBT_AGE_TIMER);
		}
		
		if (compound.contains(NBT_PLAYER)) {
			this.setPlayerUUID(compound.getUniqueId(NBT_PLAYER));
		}
		
		if (compound.contains(NBT_DRAGON_TYPE)) {
			IDragonSpawnFactory factory = IDragonSpawnData.lookupFactory(compound.getString(NBT_DRAGON_TYPE));
			this.spawnData = factory.create(compound.getCompound(NBT_DRAGON_DATA));
		}
	}
	
	@Override
	public void livingTick() {
		super.livingTick();
		
		if (this.isAlive() && !this.dead) {
			if (!world.isRemote && this.ticksExisted > 20) {
				float heatLoss = .05f;
				
				if (world.isRainingAt(getPosition())) {
					heatLoss = .1f;
				} else if (world.getLight(this.getPosition()) > 8) {
					if (world.getBlockState(getPosition().add(0, -1, 0)).getBlock() instanceof BlockHay) {
						heatLoss = 0f;
					}
				}
				
				this.setHeat(this.getHeat() - heatLoss);
				
				if (this.getHeat() <= 0f) {
					PlayerEntity player = this.getPlayer();
					if (player != null) {
						player.sendMessage(new TranslationTextComponent("info.egg.death.cold"));
					}
					
					this.attackEntityFrom(DamageSource.STARVE, 9999f);
				} else if (this.ageTimer-- <= 0) {
					// HATCH
					this.hatch();
				}
			}
		}
	}
	
	protected PlayerEntity getPlayer() {
		UUID id = getPlayerID();
		PlayerEntity player = null;
		
		if (id != null) {
			player = this.world.getPlayerEntityByUUID(id);
		}
		
		return player;
	}
	
	protected UUID getPlayerID() {
		return dataManager.get(PLAYER).orElse(null);
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
			this.world.addEntity((LivingEntity) this.spawnData.spawnDragon(world, posX, posY, posZ));
			
			PlayerEntity player = this.getPlayer();
			if (player != null) {
				player.sendMessage(new TranslationTextComponent("info.egg.hatch"));
			}
		}
		
		this.remove();
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
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}
}
