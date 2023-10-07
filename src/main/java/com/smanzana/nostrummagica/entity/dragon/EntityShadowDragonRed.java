package com.smanzana.nostrummagica.entity.dragon;

import com.smanzana.nostrummagica.entity.tasks.dragon.DragonAIFocusedTarget;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonAINearestAttackableTarget;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonMeleeAttackTask;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityShadowDragonRed extends EntityDragonRedBase {
	
	public static final String ID = "entity_shadow_dragon_red";

	private static final DataParameter<Boolean> HASTARGET =
			EntityDataManager.<Boolean>createKey(EntityDragonRed.class, DataSerializers.BOOLEAN);
	private static final String DRAGON_SERIAL_HASTARGET_TOK = "DragonShadowTarget";
	
	private LivingEntity target;
	private boolean targetInitted;
	
	public EntityShadowDragonRed(EntityType<? extends EntityShadowDragonRed> type, World worldIn) {
		super(type, worldIn);
		
        this.stepHeight = 2;
        this.targetInitted = false;
	}
	
	public EntityShadowDragonRed(EntityType<? extends EntityShadowDragonRed> type, World worldIn, LivingEntity target) {
		this(type, worldIn);
		this.target = target;
		this.dataManager.set(HASTARGET, true);
	}
	
	protected void registerData() { int unused; // TODO
		super.entityInit();
		this.dataManager.register(HASTARGET, false);
	}
	
	private void setTargetTasks() {
		if (!targetInitted) {
			if (this.target != null) {
				this.targetTasks.addTask(1, new DragonAIFocusedTarget<LivingEntity>(this, this.target, true));
			} else {
				this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[0]));
				this.targetTasks.addTask(2, new DragonAINearestAttackableTarget<PlayerEntity>(this, PlayerEntity.class, true));
			}
			targetInitted = true;
		}
	}
	
	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		
		this.tasks.addTask(1, new DragonMeleeAttackTask(this, 1.0D, true, 4F * .6F * 4F * .6F * 1.2));
		this.tasks.addTask(2, new EntityAIWander(this, 1.0D, 30));
	}
	
	@Override
	protected float getSoundVolume() {
		return 1F;
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
	protected void setFlyingAI() {
		//
	}

	@Override
	protected void setGroundedAI() {
		//
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8.0D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64D);
    }
	
	@Override
	protected boolean canDespawn() {
		return true;
	}
	
	@Override
	public void tick() {
		super.tick();
		setTargetTasks();
		
		if (this.target != null) {
			if (this.target.isDead) {
				this.attackEntityFrom(DamageSource.OUT_OF_WORLD, (float) this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue());
			}
		} else {
			// If target is null but we're a target-type, DIE
			if (this.dataManager.get(HASTARGET)) {
				this.attackEntityFrom(DamageSource.OUT_OF_WORLD, (float) this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue());
			}
		}
	}
	
	public void readAdditional(CompoundNBT compound) {
		super.readEntityFromNBT(compound);

		if (compound.contains(DRAGON_SERIAL_HASTARGET_TOK, NBT.TAG_ANY_NUMERIC)) {
        	this.dataManager.set(HASTARGET, compound.getBoolean(DRAGON_SERIAL_HASTARGET_TOK));
        }
	}
	
	public void writeAdditional(CompoundNBT compound) {
    	super.writeEntityToNBT(compound);
    	compound.putBoolean(DRAGON_SERIAL_HASTARGET_TOK, this.dataManager.get(HASTARGET));
	}

}
