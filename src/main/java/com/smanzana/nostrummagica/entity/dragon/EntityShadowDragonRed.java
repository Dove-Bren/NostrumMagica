package com.smanzana.nostrummagica.entity.dragon;

import com.smanzana.nostrummagica.entity.tasks.dragon.DragonAIFocusedTarget;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonAINearestAttackableTarget;
import com.smanzana.nostrummagica.entity.tasks.dragon.DragonMeleeAttackTask;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
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
	
	protected void registerData() {
		super.registerData();
		this.dataManager.register(HASTARGET, false);
	}
	
	private void setTargetTasks() {
		if (!targetInitted) {
			if (this.target != null) {
				this.targetSelector.addGoal(1, new DragonAIFocusedTarget<LivingEntity>(this, this.target, true));
			} else {
				this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setCallsForHelp(EntityShadowDragonRed.class));
				this.targetSelector.addGoal(2, new DragonAINearestAttackableTarget<PlayerEntity>(this, PlayerEntity.class, true));
			}
			targetInitted = true;
		}
	}
	
	@Override
	protected void registerGoals() {
		super.registerGoals();
		
		this.goalSelector.addGoal(1, new DragonMeleeAttackTask(this, 1.0D, true, 4F * .6F * 4F * .6F * 1.2));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomWalkingGoal(this, 1.0D, 30));
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
	protected void registerAttributes() {
		super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33D);
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
        this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8.0D);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        this.getAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(0.5D);
        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64D);
    }
	
	@Override
	public boolean canDespawn(double nearestPlayer) {
		return true;
	}
	
	@Override
	public void tick() {
		super.tick();
		setTargetTasks();
		
		if (this.target != null) {
			if (!this.target.isAlive()) {
				this.attackEntityFrom(DamageSource.OUT_OF_WORLD, (float) this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).getValue());
			}
		} else {
			// If target is null but we're a target-type, DIE
			if (this.dataManager.get(HASTARGET)) {
				this.attackEntityFrom(DamageSource.OUT_OF_WORLD, (float) this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).getValue());
			}
		}
	}
	
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);

		if (compound.contains(DRAGON_SERIAL_HASTARGET_TOK, NBT.TAG_ANY_NUMERIC)) {
        	this.dataManager.set(HASTARGET, compound.getBoolean(DRAGON_SERIAL_HASTARGET_TOK));
        }
	}
	
	public void writeAdditional(CompoundNBT compound) {
    	super.writeAdditional(compound);
    	compound.putBoolean(DRAGON_SERIAL_HASTARGET_TOK, this.dataManager.get(HASTARGET));
	}

}
