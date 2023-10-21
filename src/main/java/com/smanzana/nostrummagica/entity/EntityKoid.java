package com.smanzana.nostrummagica.entity;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.tasks.KoidTask;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityKoid extends MonsterEntity implements ILoreSupplier {
	
	public static final String ID = "entity_koid";

	private static final DataParameter<Integer> KOID_VARIANT =
			EntityDataManager.<Integer>createKey(EntityKoid.class, DataSerializers.VARINT);
	
	private KoidTask kTask;
	private int idleCooldown;
	
	public EntityKoid(EntityType<? extends EntityKoid> type, World worldIn) {
		this(type, worldIn, EMagicElement.values()[NostrumMagica.rand.nextInt(
				EMagicElement.values().length)]);
	}
	
    protected EntityKoid(EntityType<? extends EntityKoid> type, World worldIn, EMagicElement element) {
        super(type, worldIn);
        
        this.setElement(element);
        idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
    }
    
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SwimGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
    }
    
    protected void registerAttributes()
    {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33D);
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
    }

    protected void playStepSound(BlockPos pos, BlockState blockIn)
    {
        this.playSound(SoundEvents.ENTITY_HUSK_STEP, 0.15F, 1.0F);
    }

    protected SoundEvent getHurtSound(DamageSource source)
    {
        return SoundEvents.ENTITY_HUSK_HURT;
    }

    protected SoundEvent getDeathSound()
    {
    	return SoundEvents.ENTITY_HUSK_DEATH;
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 0.7F;
    }

    protected float getStandingEyeHeight(Pose pose, EntitySize size)
    {
        return this.getHeight() * 0.8F;
    }

    public boolean attackEntityAsMob(Entity entityIn)
    {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue()));

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
    public boolean canMateWith(AnimalEntity otherAnimal)
    {
        return false;
    }

    public boolean shouldAttackEntity(LivingEntity target, LivingEntity owner)
    {
        return target != this;
    }

    public boolean canBeLeashedTo(PlayerEntity player)
    {
        return false;
    }

	@Override
	public void tick() {
		super.tick();
		
		if (idleCooldown > 0) {
			idleCooldown--;
			if (idleCooldown == 0) {
				if (this.getAttackTarget() == null)
					NostrumMagicaSounds.CAST_FAIL.play(this);
				idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10); 
			}
		}
	}
	
	@Override
	public ILoreTagged getLoreTag() {
		return KoidLore.instance;
	}
	
	public static final class KoidLore implements ILoreTagged {

		public static final KoidLore instance = new KoidLore();
		public static final KoidLore instance() {
			return instance;
		}
	
		public static String LoreKey = "nostrum__koid";
	
		@Override
		public String getLoreKey() {
			return LoreKey;
		}
	
		@Override
		public String getLoreDisplayName() {
			return "Koids";
		}
		
		@Override
		public Lore getBasicLore() {
			return new Lore().add("Koids are strange wisps of energy that have over time become attuned to the elements.");
					
		}
		
		@Override
		public Lore getDeepLore() {
			return new Lore().add("Koids are created when too much of one element of energy gathers in a location.", "Koids hate other koids, golems, and players.", "Koids have a chance of dropping elemental runes.");
		}

		@Override
		public InfoScreenTabs getTab() {
			return InfoScreenTabs.INFO_ENTITY;
		}
	}
	
	public EMagicElement getElement() {
		return EMagicElement.values()[
              this.dataManager.get(KOID_VARIANT).intValue()];
	}
	
	public void setElement(EMagicElement element) {
		this.dataManager.set(KOID_VARIANT, element.ordinal());
		setCombatTask();
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(KOID_VARIANT, EMagicElement.PHYSICAL.ordinal());
	}
	
	public void setCombatTask() {
		if (this.world != null && !this.world.isRemote) {
			if (kTask != null)
				this.goalSelector.removeGoal(kTask);
			
			kTask = new KoidTask(this);
	        this.goalSelector.addGoal(2, kTask);
		}
	}
	
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);

        if (compound.contains("KoidType", NBT.TAG_ANY_NUMERIC)) {
        	int i = compound.getByte("KoidType");
            this.setElement(EMagicElement.values()[i]);
        }

        this.setCombatTask();
	}
	
	public void writeAdditional(CompoundNBT compound) {
    	super.writeAdditional(compound);
        compound.putByte("KoidType", (byte)this.getElement().ordinal());
	}
	
	@Override
	public void fall(float distance, float damageMulti) {
		; // No fall damage
	}
	
	@Override
	protected void updateFallState(double y, boolean onGround, BlockState stae, BlockPos pos) {
		
	}
	
	@OnlyIn(Dist.CLIENT)
    public int getBrightnessForRender(float partialTicks)
    {
        return 15728880;
    }

//    @Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		int count = this.rand.nextInt(2);
//		count += lootingModifier;
//		
//		this.entityDropItem(EssenceItem.getEssence(
//				this.getElement(),
//				count), 0);
//		
//		// Research scroll
//		int chances = 1 + lootingModifier;
//		if (rand.nextInt(100) < chances) {
//			this.entityDropItem(NostrumSkillItem.getItem(SkillItemType.RESEARCH_SCROLL_SMALL, 1), 0);
//		}
//	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source.isProjectile()) {
			amount *= 0.25f;
		}
		
		return super.attackEntityFrom(source, amount);
	}
}
