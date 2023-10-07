package com.smanzana.nostrummagica.entity.golem;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.tasks.GolemTask;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class EntityGolem extends TameableEntity implements ILoreTagged {

	Entity e;
	private static final DataParameter<Float> DATA_HEALTH_ID = EntityDataManager.<Float>createKey(EntityGolem.class, DataSerializers.FLOAT);
	protected static final int ROSE_DROP_DENOM = 12500;

	protected boolean isMelee;
	protected boolean isRange;
	protected boolean hasBuff;
	
	private GolemTask gTask;
	private int idleCooldown;
	protected EMagicElement element;
	
	private int expireTicks;
	
    protected EntityGolem(EntityType<? extends EntityGolem> type, World worldIn, EMagicElement element, boolean melee, boolean range, boolean buff) {
        super(type, worldIn);
        this.setTamed(true);
        
        this.isMelee = melee;
        this.isRange = range;
        this.hasBuff = buff;
        
        if (worldIn != null && !worldIn.isRemote)
        	gTask.initStance(isMelee, isRange, hasBuff);
        
        idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
        this.element = element;
    }
    
    public void setExpiresAfterTicks(int ticks) {
    	this.expireTicks = this.ticksExisted + ticks;
    }
    
    /**
     * Execute melee task, if you have it.
     * This is called when we're close enough (and cooldown has went down)
     * to do a melee attack.
     */
    public abstract void doMeleeTask(LivingEntity target);
    
    /**
     * Executed when not in melee range but within medium
     * range (spell range) AND cooldown has expired
     */
    public abstract void doRangeTask(LivingEntity target);
    
    /**
     * Executed randomly (with cooldown) while at range
     */
    public abstract void doBuffTask(LivingEntity target);
    public abstract boolean shouldDoBuff(LivingEntity target);

    protected void initEntityAI()
    {
        this.tasks.addTask(1, new EntityAISwimming(this));
        //this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0D, true));
        gTask = new GolemTask(this);
        this.tasks.addTask(2, gTask);
        this.tasks.addTask(3, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(4, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, PlayerEntity.class, 8.0F));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true, new Class[0]));
        this.targetTasks.addTask(1, new GolemAIFindEntityNearestPlayer(this));
    }
    
    public abstract void initGolemAttributes();

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
        return this.getHeight() * 0.8F;
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

    public boolean processInteract(PlayerEntity player, Hand hand, @Nonnull ItemStack stack)
    {
        return false;
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    public boolean isBreedingItem(@Nonnull ItemStack stack)
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

    public boolean shouldAttackEntity(LivingEntity target, LivingEntity owner)
    {
        return target != owner;
    }

    public boolean canBeLeashedTo(PlayerEntity player)
    {
        return false;
    }

	@Override
	public EntityAgeable createChild(EntityAgeable ageable) {
		return null;
	}
	
	public abstract String getTextureKey();
	
	@Override
	public void tick() {
		super.tick();
		
		if (idleCooldown > 0) {
			idleCooldown--;
			if (idleCooldown == 0) {
				if (this.getAttackTarget() == null)
					NostrumMagicaSounds.GOLEM_IDLE.play(this);
				idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10); 
			}
		}
		
		if (!world.isRemote && expireTicks != 0 && expireTicks > this.ticksExisted) {
			this.remove();
		}
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source.isProjectile()) {
			amount *= 0.3f;
		}
		
		return super.attackEntityFrom(source, amount);
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

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}
	
	public EMagicElement getElement() {
		return this.element;
	}
	
	private static class GolemAIFindEntityNearestPlayer extends EntityAINearestAttackableTarget<PlayerEntity> {

		protected MobEntity rood; // parent doesn't expose
		
		public GolemAIFindEntityNearestPlayer(CreatureEntity entityLivingIn) {
			super(entityLivingIn, PlayerEntity.class, true);
			this.rood = entityLivingIn;
		}
		
		@Override
		public boolean shouldExecute() {
			if (rood instanceof EntityTameable) {
				if (((EntityTameable) rood).getOwner() != null)
					return false;
			}
			
			return super.shouldExecute();
		}
		
	}
	
}
