package com.smanzana.nostrummagica.entity;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.lore.ILoreTagged;
import com.smanzana.nostrummagica.lore.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class EntityGolem extends EntityTameable implements ILoreTagged {

	private static final DataParameter<Float> DATA_HEALTH_ID = EntityDataManager.<Float>createKey(EntityGolem.class, DataSerializers.FLOAT);

	protected boolean isMelee;
	protected boolean isRange;
	protected boolean hasBuff;
	
	private GolemTask gTask;
	private int idleCooldown;
	
    protected EntityGolem(World worldIn, boolean melee, boolean range, boolean buff)
    {
        super(worldIn);
        this.setSize(0.8F, 1.6F);
        this.setTamed(true);
        
        this.isMelee = melee;
        this.isRange = range;
        this.hasBuff = buff;
        
        if (!worldIn.isRemote)
        	gTask.initStance(isMelee, isRange, hasBuff);
        
        idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
    }
    
    /**
     * Execute melee task, if you have it.
     * This is called when we're close enough (and cooldown has went down)
     * to do a melee attack.
     */
    public abstract void doMeleeTask(EntityLivingBase target);
    
    /**
     * Executed when not in melee range but within medium
     * range (spell range) AND cooldown has expired
     */
    public abstract void doRangeTask(EntityLivingBase target);
    
    /**
     * Executed randomly (with cooldown) while at range
     */
    public abstract void doBuffTask(EntityLivingBase target);
    public abstract boolean shouldDoBuff(EntityLivingBase target);

    protected void initEntityAI()
    {
        this.tasks.addTask(1, new EntityAISwimming(this));
        //this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0D, true));
        gTask = new GolemTask(this);
        this.tasks.addTask(2, gTask);
        this.tasks.addTask(3, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(4, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true, new Class[0]));
    }
    
    public abstract void initGolemAttributes();

    // TODO TODO TODO take this into the subclasses
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        
        this.initGolemAttributes();
    }

    protected void updateAITasks()
    {
        this.dataManager.set(DATA_HEALTH_ID, Float.valueOf(this.getHealth()));
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(DATA_HEALTH_ID, Float.valueOf(this.getHealth()));
    }

    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        this.playSound(SoundEvents.ENTITY_WOLF_STEP, 0.15F, 1.0F);
    }

    protected SoundEvent getHurtSound()
    {
        return NostrumMagicaSounds.GOLEM_HURT.getEvent();
    }

    protected SoundEvent getDeathSound()
    {
    	return NostrumMagicaSounds.GOLEM_HURT.getEvent();
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 0.4F;
    }

    public float getEyeHeight()
    {
        return this.height * 0.8F;
    }

    public boolean attackEntityAsMob(Entity entityIn)
    {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));

        if (flag)
        {
            this.applyEnchantments(this, entityIn);
        }

        return flag;
    }

    public boolean processInteract(EntityPlayer player, EnumHand hand, @Nullable ItemStack stack)
    {
        return false;
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    public boolean isBreedingItem(@Nullable ItemStack stack)
    {
        return false;
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    public boolean canMateWith(EntityAnimal otherAnimal)
    {
        return false;
    }

    public boolean shouldAttackEntity(EntityLivingBase target, EntityLivingBase owner)
    {
        return target != owner;
    }

    public boolean canBeLeashedTo(EntityPlayer player)
    {
        return false;
    }

	@Override
	public EntityAgeable createChild(EntityAgeable ageable) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound)
    {
		return false;
    }

	public abstract String getTextureKey();
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (idleCooldown > 0) {
			idleCooldown--;
			if (idleCooldown == 0) {
				if (this.getAttackTarget() == null)
					NostrumMagicaSounds.GOLEM_IDLE.play(this);
				idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10); 
			}
		}
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum__golem";
	}

	@Override
	public String getLoreDisplayName() {
		return "Golems";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("By infusing stones with an element, a spark of life is born.", "These golems seem to be bound to their casters by an invisible bond.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("By infusing stones with an element, a spark of life is born.", "Golems take after the element they are infused with. Golems can have melee attacks, ranged spells, or even buffs they might share with their caster.");
	}
	
}
