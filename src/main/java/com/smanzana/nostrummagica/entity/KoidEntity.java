package com.smanzana.nostrummagica.entity;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.tasks.KoidTask;
import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.item.set.NostrumEquipmentSets;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class KoidEntity extends Monster implements ILoreSupplier, IElementalEntity {
	
	public static final String ID = "entity_koid";

	private static final EntityDataAccessor<Integer> KOID_VARIANT =
			SynchedEntityData.<Integer>defineId(KoidEntity.class, EntityDataSerializers.INT);
	
	private KoidTask kTask;
	private int idleCooldown;
	
	public KoidEntity(EntityType<? extends KoidEntity> type, Level worldIn) {
		this(type, worldIn, EMagicElement.values()[NostrumMagica.rand.nextInt(
				EMagicElement.values().length)]);
	}
	
    protected KoidEntity(EntityType<? extends KoidEntity> type, Level worldIn, EMagicElement element) {
        super(type, worldIn);
        
        this.setElement(element);
        idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
    }
    
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>(this, Player.class, true));
    }
    
    public static final AttributeSupplier.Builder BuildAttributes() {
        return Monster.createMonsterAttributes()
	        .add(Attributes.MOVEMENT_SPEED, 0.33D)
	        .add(Attributes.MAX_HEALTH, 10.0D)
	        .add(Attributes.ATTACK_DAMAGE, 2.0D)
	        .add(Attributes.ARMOR, 2.0D);
    }

    protected void playStepSound(BlockPos pos, BlockState blockIn)
    {
        this.playSound(SoundEvents.HUSK_STEP, 0.15F, 1.0F);
    }

    protected SoundEvent getHurtSound(DamageSource source)
    {
        return SoundEvents.HUSK_HURT;
    }

    protected SoundEvent getDeathSound()
    {
    	return SoundEvents.HUSK_DEATH;
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 0.7F;
    }

    protected float getStandingEyeHeight(Pose pose, EntityDimensions size)
    {
        return this.getBbHeight() * 0.8F;
    }

    public boolean doHurtTarget(Entity entityIn)
    {
        boolean flag = entityIn.hurt(DamageSource.mobAttack(this), (float)((int)this.getAttribute(Attributes.ATTACK_DAMAGE).getValue()));

        if (flag)
        {
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
    public boolean isBreedingItem(@Nonnull ItemStack stack)
    {
        return false;
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    public boolean canMateWith(Animal otherAnimal)
    {
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return super.canAttack(target)
        		&& NostrumMagica.itemSetListener.getActiveSetCount(target, NostrumEquipmentSets.koidSet) < 3;
    }

    public boolean canBeLeashed(Player player)
    {
        return false;
    }

	@Override
	public void tick() {
		super.tick();
		
		if (idleCooldown > 0) {
			idleCooldown--;
			if (idleCooldown == 0) {
				if (this.getTarget() == null)
					NostrumMagicaSounds.CAST_FAIL.play(this);
				idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10); 
			}
		}
	}
	
	@Override
	public ILoreTagged getLoreTag() {
		return KoidLore.instance;
	}
	
	public static final class KoidLore implements IEntityLoreTagged<KoidEntity> {

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
		public ELoreCategory getCategory() {
			return ELoreCategory.ENTITY;
		}

		@Override
		public EntityType<KoidEntity> getEntityType() {
			return NostrumEntityTypes.koid;
		}
	}
	
	@Override
	public EMagicElement getElement() {
		return EMagicElement.values()[
              this.entityData.get(KOID_VARIANT).intValue()];
	}
	
	public void setElement(EMagicElement element) {
		this.entityData.set(KOID_VARIANT, element.ordinal());
		setCombatTask();
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(KOID_VARIANT, EMagicElement.NEUTRAL.ordinal());
	}
	
	public void setCombatTask() {
		if (this.level != null && !this.level.isClientSide) {
			if (kTask != null)
				this.goalSelector.removeGoal(kTask);
			
			kTask = new KoidTask(this);
	        this.goalSelector.addGoal(2, kTask);
		}
	}
	
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);

        if (compound.contains("KoidType", Tag.TAG_ANY_NUMERIC)) {
        	int i = compound.getByte("KoidType");
            this.setElement(EMagicElement.values()[i]);
        }

        this.setCombatTask();
	}
	
	public void addAdditionalSaveData(CompoundTag compound) {
    	super.addAdditionalSaveData(compound);
        compound.putByte("KoidType", (byte)this.getElement().ordinal());
	}
	
	@Override
	protected void checkFallDamage(double y, boolean onGround, BlockState stae, BlockPos pos) {
		
	}
	
	@OnlyIn(Dist.CLIENT)
    public int getBrightnessForRender(float partialTicks)
    {
        return 15728880;
    }
	
	@Override
	protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
		super.dropCustomDeathLoot(source, looting, recentlyHitIn);
		
		// Drop essence item and maybe gem, since that's harder to express in a loot table
		if (recentlyHitIn) {
			int count = this.random.nextInt(2);
			count += looting;
			
			this.spawnAtLocation(EssenceItem.getEssence(this.getElement(), count), 0);
			
			if (this.random.nextFloat() < (.01f + .02f * looting)) {
				this.spawnAtLocation(InfusedGemItem.getGem(this.getElement(), 1));
			}
		}
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
	public boolean hurt(DamageSource source, float amount) {
		if (source.isProjectile()) {
			amount *= 0.25f;
		}
		
		return super.hurt(source, amount);
	}
}
