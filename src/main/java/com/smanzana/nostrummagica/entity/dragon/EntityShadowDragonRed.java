package com.smanzana.nostrummagica.entity.dragon;

import com.smanzana.nostrummagica.entity.tasks.DragonAIFocusedTarget;
import com.smanzana.nostrummagica.entity.tasks.DragonAINearestAttackableTarget;
import com.smanzana.nostrummagica.entity.tasks.DragonMeleeAttackTask;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityShadowDragonRed extends EntityDragonRedBase {

	private static final DataParameter<Boolean> HASTARGET =
			EntityDataManager.<Boolean>createKey(EntityDragonRed.class, DataSerializers.BOOLEAN);
	private static final String DRAGON_SERIAL_HASTARGET_TOK = "DragonShadowTarget";
	
	private EntityLivingBase target;
	private boolean targetInitted;
	
	public EntityShadowDragonRed(World worldIn) {
		super(worldIn);
		
		this.setSize(6F * .6F, 4.6F * .6F);
        this.stepHeight = 2;
        this.isImmuneToFire = true;
        this.targetInitted = false;
	}
	
	public EntityShadowDragonRed(World worldIn, EntityLivingBase target) {
		this(worldIn);
		this.target = target;
		this.dataManager.set(HASTARGET, true);
	}
	
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(HASTARGET, false);
	}
	
	private void setTargetTasks() {
		if (!targetInitted) {
			if (this.target != null) {
				this.targetTasks.addTask(1, new DragonAIFocusedTarget<EntityLivingBase>(this, this.target, true));
			} else {
				this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[0]));
				this.targetTasks.addTask(2, new DragonAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
			}
			targetInitted = true;
		}
	}
	
	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		
		this.tasks.addTask(1, new DragonMeleeAttackTask(this, 1.0D, true));
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
	public void onUpdate() {
		super.onUpdate();
		setTargetTasks();
		
		if (this.target != null) {
			if (this.target.isDead) {
				this.attackEntityFrom(DamageSource.outOfWorld, (float) this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue());
			}
		} else {
			// If target is null but we're a target-type, DIE
			if (this.dataManager.get(HASTARGET)) {
				this.attackEntityFrom(DamageSource.outOfWorld, (float) this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue());
			}
		}
	}
	
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);

		if (compound.hasKey(DRAGON_SERIAL_HASTARGET_TOK, NBT.TAG_ANY_NUMERIC)) {
        	this.dataManager.set(HASTARGET, compound.getBoolean(DRAGON_SERIAL_HASTARGET_TOK));
        }
	}
	
	public void writeEntityToNBT(NBTTagCompound compound) {
    	super.writeEntityToNBT(compound);
    	compound.setBoolean(DRAGON_SERIAL_HASTARGET_TOK, this.dataManager.get(HASTARGET));
	}

}
