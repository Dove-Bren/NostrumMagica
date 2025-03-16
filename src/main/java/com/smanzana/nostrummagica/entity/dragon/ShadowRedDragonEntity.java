package com.smanzana.nostrummagica.entity.dragon;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonFocusedTargetGoal;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonMeleeAttackGoal;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonNearestAttackableTargetGoal;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants.NBT;

public class ShadowRedDragonEntity extends RedDragonBaseEntity implements ILoreSupplier {
	
	public static final String ID = "entity_shadow_dragon_red";

	private static final EntityDataAccessor<Boolean> HASTARGET =
			SynchedEntityData.<Boolean>defineId(RedDragonEntity.class, EntityDataSerializers.BOOLEAN);
	private static final String DRAGON_SERIAL_HASTARGET_TOK = "DragonShadowTarget";
	
	private LivingEntity target;
	private boolean targetInitted;
	
	public ShadowRedDragonEntity(EntityType<? extends ShadowRedDragonEntity> type, Level worldIn) {
		super(type, worldIn);
		
        this.maxUpStep = 2;
        this.targetInitted = false;
	}
	
	public ShadowRedDragonEntity(EntityType<? extends ShadowRedDragonEntity> type, Level worldIn, LivingEntity target) {
		this(type, worldIn);
		this.target = target;
		this.entityData.set(HASTARGET, true);
	}
	
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(HASTARGET, false);
	}
	
	private void setTargetTasks() {
		if (!targetInitted) {
			if (this.target != null) {
				this.targetSelector.addGoal(1, new DragonFocusedTargetGoal<LivingEntity>(this, this.target, true));
			} else {
				this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(ShadowRedDragonEntity.class));
				this.targetSelector.addGoal(2, new DragonNearestAttackableTargetGoal<Player>(this, Player.class, true));
			}
			targetInitted = true;
		}
	}
	
	@Override
	protected void registerGoals() {
		super.registerGoals();
		
		this.goalSelector.addGoal(1, new DragonMeleeAttackGoal(this, 1.0D, true, 4F * .6F * 4F * .6F * 1.2));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D, 30));
	}
	
	@Override
	protected float getSoundVolume() {
		return 1F;
	}
	
	@Override
	public ILoreTagged getLoreTag() {
		return ShadowRedDragonLore.instance();
	}
	
	public static final class ShadowRedDragonLore implements IEntityLoreTagged<ShadowRedDragonEntity> {
		
		private static ShadowRedDragonLore instance = null;
		public static ShadowRedDragonLore instance() {
			if (instance == null) {
				instance = new ShadowRedDragonLore();
			}
			return instance;
		}

		@Override
		public String getLoreKey() {
			return "nostrum__dragon_shadow_red";
		}

		@Override
		public String getLoreDisplayName() {
			return "Shadow Red Dragons";
		}
		
		@Override
		public Lore getBasicLore() {
			return new Lore().add("A shadowy figure taking the shape of a dragon...");
		}
		
		@Override
		public Lore getDeepLore() {
			return new Lore().add("A shadowy figure taking the shape of a dragon...");
		}

		@Override
		public InfoScreenTabs getTab() {
			return InfoScreenTabs.INFO_ENTITY;
		}

		@Override
		public EntityType<ShadowRedDragonEntity> getEntityType() {
			return NostrumEntityTypes.shadowDragonRed;
		}
	}
	
	@Override
	protected void setFlyingAI() {
		//
	}

	@Override
	protected void setGroundedAI() {
		//
	}

	public static final AttributeSupplier.Builder BuildAttributes() {
		return RedDragonBaseEntity.BuildBaseRedDragonAttributes()
	        .add(Attributes.MOVEMENT_SPEED, 0.33D)
	        .add(Attributes.MAX_HEALTH, 50.0D)
	        .add(Attributes.ATTACK_DAMAGE, 5.0D)
	        .add(Attributes.ARMOR, 8.0D)
	        .add(Attributes.ATTACK_SPEED, 0.5D)
	        .add(Attributes.FOLLOW_RANGE, 64D);
    }
	
	@Override
	public boolean removeWhenFarAway(double nearestPlayer) {
		return true;
	}
	
	@Override
	public void tick() {
		super.tick();
		setTargetTasks();
		
		if (this.target != null) {
			if (!this.target.isAlive()) {
				this.hurt(DamageSource.OUT_OF_WORLD, (float) this.getAttribute(Attributes.MAX_HEALTH).getValue());
			}
		} else {
			// If target is null but we're a target-type, DIE
			if (this.entityData.get(HASTARGET)) {
				this.hurt(DamageSource.OUT_OF_WORLD, (float) this.getAttribute(Attributes.MAX_HEALTH).getValue());
			}
		}
	}
	
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);

		if (compound.contains(DRAGON_SERIAL_HASTARGET_TOK, NBT.TAG_ANY_NUMERIC)) {
        	this.entityData.set(HASTARGET, compound.getBoolean(DRAGON_SERIAL_HASTARGET_TOK));
        }
	}
	
	public void addAdditionalSaveData(CompoundTag compound) {
    	super.addAdditionalSaveData(compound);
    	compound.putBoolean(DRAGON_SERIAL_HASTARGET_TOK, this.entityData.get(HASTARGET));
	}

}
