package com.smanzana.nostrummagica.entity;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.tasks.KoidTask;
import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.item.set.NostrumEquipmentSets;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class KoidEntity extends MonsterEntity implements ILoreSupplier, IElementalEntity {
	
	public static final String ID = "entity_koid";

	private static final DataParameter<Integer> KOID_VARIANT =
			EntityDataManager.<Integer>defineId(KoidEntity.class, DataSerializers.INT);
	
	private KoidTask kTask;
	private int idleCooldown;
	
	public KoidEntity(EntityType<? extends KoidEntity> type, World worldIn) {
		this(type, worldIn, EMagicElement.values()[NostrumMagica.rand.nextInt(
				EMagicElement.values().length)]);
	}
	
    protected KoidEntity(EntityType<? extends KoidEntity> type, World worldIn, EMagicElement element) {
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
    
    public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
        return MonsterEntity.createMonsterAttributes()
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

    protected float getStandingEyeHeight(Pose pose, EntitySize size)
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

    public ActionResultType /*processInteract*/ mobInteract(PlayerEntity player, Hand hand, @Nonnull ItemStack stack) {
        return ActionResultType.PASS;
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

    @Override
    public boolean canAttack(LivingEntity target) {
        return super.canAttack(target)
        		&& NostrumMagica.itemSetListener.getActiveSetCount(target, NostrumEquipmentSets.koidSet) < 3;
    }

    public boolean canBeLeashed(PlayerEntity player)
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
		public InfoScreenTabs getTab() {
			return InfoScreenTabs.INFO_ENTITY;
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
		this.entityData.define(KOID_VARIANT, EMagicElement.PHYSICAL.ordinal());
	}
	
	public void setCombatTask() {
		if (this.level != null && !this.level.isClientSide) {
			if (kTask != null)
				this.goalSelector.removeGoal(kTask);
			
			kTask = new KoidTask(this);
	        this.goalSelector.addGoal(2, kTask);
		}
	}
	
	public void readAdditionalSaveData(CompoundNBT compound) {
		super.readAdditionalSaveData(compound);

        if (compound.contains("KoidType", NBT.TAG_ANY_NUMERIC)) {
        	int i = compound.getByte("KoidType");
            this.setElement(EMagicElement.values()[i]);
        }

        this.setCombatTask();
	}
	
	public void addAdditionalSaveData(CompoundNBT compound) {
    	super.addAdditionalSaveData(compound);
        compound.putByte("KoidType", (byte)this.getElement().ordinal());
	}
	
	@Override
	public boolean causeFallDamage(float distance, float damageMulti) {
		return false; // No fall damage
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
