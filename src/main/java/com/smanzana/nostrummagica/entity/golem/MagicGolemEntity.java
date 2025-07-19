package com.smanzana.nostrummagica.entity.golem;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.IElementalEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.tasks.GolemTask;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MagicGolemEntity extends TamableAnimal implements ILoreSupplier, IElementalEntity {

	Entity e;
	private static final EntityDataAccessor<Float> DATA_HEALTH_ID = SynchedEntityData.<Float>defineId(MagicGolemEntity.class, EntityDataSerializers.FLOAT);
	protected static final int ROSE_DROP_DENOM = 12500;

	protected boolean isMelee;
	protected boolean isRange;
	protected boolean hasBuff;
	
	private GolemTask gTask;
	private int idleCooldown;
	protected EMagicElement element;
	
	private int expireTicks;
	
    protected MagicGolemEntity(EntityType<? extends MagicGolemEntity> type, Level worldIn, EMagicElement element, boolean melee, boolean range, boolean buff) {
        super(type, worldIn);
        this.setTame(true);
        
        this.isMelee = melee;
        this.isRange = range;
        this.hasBuff = buff;
        
        if (worldIn != null && !worldIn.isClientSide)
        	gTask.initStance(isMelee, isRange, hasBuff);
        
        idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
        this.element = element;
    }
    
    public void setExpiresAfterTicks(int ticks) {
    	this.expireTicks = this.tickCount + ticks;
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

    protected void registerGoals()
    {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        //this.goalSelector.addGoal(3, new EntityAIAttackMelee(this, 1.0D, true));
        gTask = new GolemTask(this);
        this.goalSelector.addGoal(2, gTask);
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers(MagicGolemEntity.class));
        this.targetSelector.addGoal(1, new GolemAIFindEntityNearestPlayer(this));
    }
    
    protected static final AttributeSupplier.Builder BuildBaseAttributes(EMagicElement element) {
    	var attributes = Animal.createMobAttributes()
    			.add(Attributes.ATTACK_DAMAGE, 2.0)
    			.add(NostrumAttributes.GetReduceAttribute(element), 1.0);
    	
    	if (element == EMagicElement.PHYSICAL) {
    		attributes.add(NostrumAttributes.GetReduceAttribute(EMagicElement.FIRE), -3.0);
    		attributes.add(NostrumAttributes.GetReduceAttribute(EMagicElement.ICE), -3.0);
    		attributes.add(NostrumAttributes.GetReduceAttribute(EMagicElement.WIND), -3.0);
    		attributes.add(NostrumAttributes.GetReduceAttribute(EMagicElement.EARTH), -3.0);
    		attributes.add(NostrumAttributes.GetReduceAttribute(EMagicElement.LIGHTNING), -3.0);
    		attributes.add(NostrumAttributes.GetReduceAttribute(EMagicElement.ENDER), -3.0);
    	} else {
    		attributes.add(NostrumAttributes.GetReduceAttribute(element.getOpposite()), -3.0);
    	}
    	return attributes;
    }

    protected void customServerAiStep() {
        this.entityData.set(DATA_HEALTH_ID, Float.valueOf(this.getHealth()));
    }

    @Override
    protected void defineSynchedData() {
    	super.defineSynchedData();
        this.entityData.define(DATA_HEALTH_ID, Float.valueOf(this.getHealth()));
    }

    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(SoundEvents.WOLF_STEP, 0.15F, 1.0F);
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return NostrumMagicaSounds.GOLEM_HURT.getEvent();
    }

    protected SoundEvent getDeathSound() {
    	return NostrumMagicaSounds.GOLEM_HURT.getEvent();
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume() {
        return 0.4F;
    }

    protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
        return this.getBbHeight() * 0.8F;
    }

    public boolean doHurtTarget(Entity entityIn) {
        boolean flag = entityIn.hurt(DamageSource.mobAttack(this), (float)((int)this.getAttribute(Attributes.ATTACK_DAMAGE).getValue()));

        if (flag) {
            this.doEnchantDamageEffects(this, entityIn);
        }

        return flag;
    }

    public InteractionResult /*processInteract*/ mobInteract(Player player, InteractionHand hand, @Nonnull ItemStack stack) {
        return InteractionResult.PASS;
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    public boolean isFood(@Nonnull ItemStack stack) {
        return false;
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    public boolean canMate(Animal otherAnimal) {
        return false;
    }

    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        return target != owner;
    }

    public boolean canBeLeashed(Player player) {
        return false;
    }

	@Override
	public AgeableMob /*createChild*/ getBreedOffspring(ServerLevel world, AgeableMob ageable) {
		return null;
	}
	
	public abstract String getTextureKey();
	
	@Override
	public void tick() {
		super.tick();
		
		if (idleCooldown > 0) {
			idleCooldown--;
			if (idleCooldown == 0) {
				if (this.getTarget() == null)
					NostrumMagicaSounds.GOLEM_IDLE.play(this);
				idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10); 
			}
		}
		
		if (!level.isClientSide && expireTicks != 0 && expireTicks > this.tickCount) {
			this.discard();
		}
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source.isProjectile()) {
			amount *= 0.3f;
		}
		
		return super.hurt(source, amount);
	}
	
	@Override
	public boolean saveAsPassenger(CompoundTag compound) {
		// If given an expire time, don't save out with the world/persist
		if (this.expireTicks != 0) {
			return false;
		}
		
		return super.saveAsPassenger(compound);
    }
	
	@Override
	public ILoreTagged getLoreTag() {
		return GolemLore.instance;
	}
	
	public static final class GolemLore implements IEntityLoreTagged<MagicGolemEntity> {

		public static final GolemLore instance = new GolemLore();
		public static final GolemLore instance() {
			return instance;
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

		@Override
		public EntityType<MagicFireGolemEntity> getEntityType() {
			return NostrumEntityTypes.golemFire;
		}
	}
	
	@Override
	public EMagicElement getElement() {
		return this.element;
	}
	
	private static class GolemAIFindEntityNearestPlayer extends NearestAttackableTargetGoal<Player> {

		protected Mob rood; // parent doesn't expose
		
		public GolemAIFindEntityNearestPlayer(PathfinderMob entityLivingIn) {
			super(entityLivingIn, Player.class, true);
			this.rood = entityLivingIn;
		}
		
		@Override
		public boolean canUse() {
			if (rood instanceof TamableAnimal) {
				if (((TamableAnimal) rood).getOwner() != null)
					return false;
			}
			
			return super.canUse();
		}
		
	}
	
}
