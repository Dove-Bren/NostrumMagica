package com.smanzana.nostrummagica.entity;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.tasks.EntityAIFollowEntityGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntitySpellAttackTask;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.NostrumSkillItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem.SkillItemType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntitySprite extends EntityMob implements ILoreTagged {

	private static final DataParameter<Boolean> SPRITE_ANGRY = EntityDataManager.<Boolean>createKey(EntitySprite.class, DataSerializers.BOOLEAN);
	
	private static Spell EARTH_ZAP = null;
	
	private static final double EFFECT_DISTANCE_SQ = 256.0;
	
	private static void initStaticSpells() {
		if (EARTH_ZAP == null) {
			EARTH_ZAP = new Spell("Sprite_EARTHZAP", true);
			EARTH_ZAP.addPart(new SpellPart(TouchTrigger.instance()));
			EARTH_ZAP.addPart(new SpellPart(SingleShape.instance(), EMagicElement.EARTH, 3, null));
		}
	}
	
	private int idleCooldown;
	private int effectCooldown = 0;
	
	public EntitySprite(World worldIn) {
        super(worldIn);
        this.setSize(1F, 1.75F);
        
        idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
        effectCooldown = 20 * 5;
    }
    
    protected void initEntityAI() {
    	EntitySprite.initStaticSpells();
    	
    	int priority = 1;
    	this.tasks.addTask(priority++, new EntitySpellAttackTask<EntitySprite>(this, 20, 4, true, (sprite) -> {
    		return sprite.isAngry() && sprite.getAttackTarget() != null
    				&& sprite.getAttackTarget().getDistanceSq(sprite) <= TouchTrigger.TOUCH_RANGE * TouchTrigger.TOUCH_RANGE;
    	}, EARTH_ZAP));
        this.tasks.addTask(priority++, new EntityAISwimming(this));
        this.tasks.addTask(priority++, new EntityAIFollowEntityGeneric<EntitySprite>(this, 1D, 2f, 4f, false, null) {
        	@Override
        	protected LivingEntity getTarget(EntitySprite entity) {
        		return entity.getAttackTarget();
        	}
        	
        	@Override
        	protected boolean canFollow(EntitySprite entity) {
        		return entity.isAngry();
        	}
        });
        this.tasks.addTask(priority++, new EntityAIFollowEntityGeneric<EntitySprite>(this, 1D, 6f, 10f, false, null) {
        	@Override
        	protected LivingEntity getTarget(EntitySprite entity) {
        		return entity.getAttackTarget();
        	}
        });
        this.tasks.addTask(priority++, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(priority++, new EntityAIWatchClosest(this, PlayerEntity.class, 24.0F));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<PlayerEntity>(this, PlayerEntity.class, true));
    }
    
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
    }

    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        this.playSound(SoundEvents.ENTITY_HUSK_STEP, 0.15F, 1.0F);
    }

    protected SoundEvent getHurtSound()
    {
        return NostrumMagicaSounds.CAST_FAIL.getEvent();
    }

    protected SoundEvent getDeathSound()
    {
    	return NostrumMagicaSounds.CAST_FAIL.getEvent();
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 1F;
    }

    public float getEyeHeight()
    {
        return this.getHeight() * 0.4F;
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
        return target != this;
    }

    public boolean canBeLeashedTo(PlayerEntity player)
    {
        return false;
    }
    
    private void applyEffect(LivingEntity entity) {
    	entity.removePotionEffect(Potion.getPotionFromResourceLocation("levitation"));
    	entity.removePotionEffect(RootedPotion.instance());
    	if (this.isAngry()) {
    		entity.addPotionEffect(new PotionEffect(RootedPotion.instance(), 20 * 10));
		} else {
			final int finalDur = 20*10;
			int dur = 20 * 1;
			if (entity == this.getAttackTarget()) {
				dur = finalDur;
			}
			
			entity.addPotionEffect(
				new PotionEffect(Potion.getPotionFromResourceLocation("levitation"), dur)
				);
			if (entity instanceof PlayerEntity) {
				this.addPotionEffect(
						new PotionEffect(Potion.getPotionFromResourceLocation("glowing"), finalDur)
					);
			}
		}
    }

	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (idleCooldown > 0) {
			idleCooldown--;
			if (idleCooldown == 0) {
				if (this.getAttackTarget() == null)
					NostrumMagicaSounds.DAMAGE_EARTH.play(this);
				idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10); 
			}
		}
		
		if (effectCooldown > 0 && !world.isRemote) {
			effectCooldown--;
			if (effectCooldown == 0) {
				
				// Lift up players in a large radius. Lift up other entities at a much smaller one
				
				// Non-players
				AxisAlignedBB bb = new AxisAlignedBB(posX - 6, posY - 40, posZ - 6,
						posX + 6, posY + 40, posZ + 6);
				List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this, bb);
				
				for (Entity entity : entities) {
					if (entity instanceof LivingEntity && !(entity instanceof EntitySprite)) {
						
						if (entity instanceof PlayerEntity) {
							continue;
						}
						
						applyEffect((LivingEntity) entity);
					}
				}
				
				// Players
				for (PlayerEntity player : world.playerEntities) {
					if (player.isCreative() || player.isSpectator()) {
						continue;
					}
					
					if (player.getDistanceSq(this) <= EFFECT_DISTANCE_SQ) {
						applyEffect(player);
					}
				}
				
				effectCooldown = 20 * 5;
			}
		}
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum__sprite";
	}

	@Override
	public String getLoreDisplayName() {
		return "Sprites";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Strange, crystaline pieces of magic sometimes swirl together.", "While they do attack when provoked, they seem to be creatures of curiosity instead of wrath...");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Strange, crystaline pieces of magic sometimes swirl together.", "While they do attack when provoked, they seem to be creatures of curiosity instead of wrath...");
	}
	
	
	@Override
	protected void entityInit() {
		super.entityInit();
		
		this.dataManager.register(SPRITE_ANGRY, false);
	}
	
	public void readEntityFromNBT(CompoundNBT compound) {
		super.readEntityFromNBT(compound);
		
		this.dataManager.set(SPRITE_ANGRY, compound.getBoolean("angry"));
	}
	
	public void writeEntityToNBT(CompoundNBT compound) {
    	super.writeEntityToNBT(compound);
    	
    	compound.putBoolean("angry", this.isAngry());
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

    /**
     * Gets how bright this entity is.
     */
    public float getBrightness(float partialTicks)
    {
        return 1.0F;
    }
    
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		// Sprite Core
		int chances = this.rand.nextInt(4);
		int count = 0;
		chances += lootingModifier;
		
		while (chances > 0) {
			int eff = Math.min(chances, 4);
			if (this.rand.nextInt(8) < eff) {
				count++;
			}
			chances -= 4;
		}
		
		for (int i = 0; i < count; i++) {
			this.entityDropItem(NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1), 0);
		}
		
		// Research scroll
		chances = 1 + lootingModifier;
		if (rand.nextInt(100) < chances) {
			this.entityDropItem(NostrumSkillItem.getItem(SkillItemType.RESEARCH_SCROLL_SMALL, 1), 0);
		}
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source.isProjectile()) {
			amount *= 0.25f;
		}
		
		boolean hurt = super.attackEntityFrom(source, amount);
		
		if (hurt && !this.isAngry()) {
			setAngry(true);
			effectCooldown = 1; // Make next tick grou nd everyone again
			
			if (!this.world.isRemote) {
				this.world.setEntityState(this, (byte) 6);
			}
		}
		
		return hurt;
	}
	
	private void playEffect(EnumParticleTypes enumparticletypes) {
		
		for (int i = 0; i < 15; ++i) {
			double d0 = this.rand.nextGaussian() * 0.02D;
			double d1 = this.rand.nextGaussian() * 0.02D;
			double d2 = this.rand.nextGaussian() * 0.02D;
			this.world.spawnParticle(enumparticletypes, this.posX + (double)(this.rand.nextFloat() * this.getWidth * 2.0F) - (double)this.getWidth, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.getHeight()), this.posZ + (double)(this.rand.nextFloat() * this.getWidth * 2.0F) - (double)this.getWidth, d0, d1, d2, new int[0]);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void handleStatusUpdate(byte id) {
		if (id == 6) {
			playEffect(EnumParticleTypes.VILLAGER_ANGRY);
		}
	}
	
	public boolean isAngry() {
		return this.dataManager.get(SPRITE_ANGRY);
	}
	
	protected void setAngry(boolean angry) {
		this.dataManager.set(SPRITE_ANGRY, angry);
	}
	
	@Override
	protected boolean isValidLightLevel() {
		BlockPos blockpos = new BlockPos(this.posX, this.getBoundingBox().minY, this.posZ);
		
		if (world.getBlockState(blockpos).getMaterial() == Material.GRASS) {
			return world.getLightFromNeighbors(blockpos) <= this.getRNG().nextInt(12);
		}
		
		return super.isValidLightLevel();
	}
}
