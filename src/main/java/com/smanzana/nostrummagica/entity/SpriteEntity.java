package com.smanzana.nostrummagica.entity;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.tasks.FollowEntityGenericGoal;
import com.smanzana.nostrummagica.entity.tasks.SpellAttackGoal;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.component.shapes.TouchShape;
import com.smanzana.nostrummagica.util.SpellUtils;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureEntity;
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
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.BiomeDictionary;

public class SpriteEntity extends CreatureEntity implements ILoreSupplier, IElementalEntity {
	
	public static final String ID = "entity_sprite";

	private static final DataParameter<Boolean> SPRITE_ANGRY = EntityDataManager.<Boolean>defineId(SpriteEntity.class, DataSerializers.BOOLEAN);
	
	private static Spell EARTH_ZAP = null;
	
	private static final double EFFECT_DISTANCE_SQ = 256.0;
	
	private static void initStaticSpells() {
		if (EARTH_ZAP == null) {
			EARTH_ZAP = SpellUtils.MakeSpell("Sprite_EARTHZAP",
					NostrumSpellShapes.Touch,
					EMagicElement.EARTH, 3, null);
		}
	}
	
	private int idleCooldown;
	private int effectCooldown = 0;
	
	public SpriteEntity(EntityType<? extends SpriteEntity> type, World worldIn) {
        super(type, worldIn);
        
        idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
        effectCooldown = 20 * 5;
        this.xpReward = 10;
    }

    @Override
    protected void registerGoals() {
    	SpriteEntity.initStaticSpells();
    	
    	int priority = 1;
    	this.goalSelector.addGoal(priority++, new SpellAttackGoal<SpriteEntity>(this, 20, 4, true, (sprite) -> {
    		return sprite.isAngry() && sprite.getTarget() != null
    				&& sprite.getTarget().distanceToSqr(sprite) <= TouchShape.AI_TOUCH_RANGE * TouchShape.AI_TOUCH_RANGE;
    	}, EARTH_ZAP));
        this.goalSelector.addGoal(priority++, new SwimGoal(this));
        this.goalSelector.addGoal(priority++, new FollowEntityGenericGoal<SpriteEntity>(this, 1D, 2f, 4f, false, null) {
        	@Override
        	protected LivingEntity getTarget(SpriteEntity entity) {
        		return entity.getTarget();
        	}
        	
        	@Override
        	protected boolean canFollow(SpriteEntity entity) {
        		return entity.isAngry();
        	}
        });
        this.goalSelector.addGoal(priority++, new FollowEntityGenericGoal<SpriteEntity>(this, 1D, 6f, 10f, false, null) {
        	@Override
        	protected LivingEntity getTarget(SpriteEntity entity) {
        		return entity.getTarget();
        	}
        });
        this.goalSelector.addGoal(priority++, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(priority++, new LookAtGoal(this, PlayerEntity.class, 24.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
    }

    public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
    	return CreatureEntity.createMobAttributes()
    		.add(Attributes.MOVEMENT_SPEED, 0.33D)
    		.add(Attributes.MAX_HEALTH, 40.0D)
    		.add(Attributes.ATTACK_DAMAGE, 2.0D)
    		.add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn)
    {
        this.playSound(SoundEvents.HUSK_STEP, 0.15F, 1.0F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return NostrumMagicaSounds.CAST_FAIL.getEvent();
    }

    @Override
    protected SoundEvent getDeathSound()
    {
    	return NostrumMagicaSounds.CAST_FAIL.getEvent();
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    @Override
    protected float getSoundVolume()
    {
        return 1F;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntitySize size)
    {
        return this.getBbHeight() * 0.4F;
    }

    @Override
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

    public boolean shouldAttackEntity(LivingEntity target, LivingEntity owner)
    {
        return target != this;
    }

    public boolean canBeLeashed(PlayerEntity player)
    {
        return false;
    }
    
    private void applyEffect(LivingEntity entity) {
    	entity.removeEffect(Effects.LEVITATION);
    	entity.removeEffect(NostrumEffects.rooted);
    	if (this.isAngry()) {
    		entity.addEffect(new EffectInstance(NostrumEffects.rooted, 20 * 10));
		} else {
			final int finalDur = 20*10;
			int dur = 20 * 1;
			if (entity == this.getTarget()) {
				dur = finalDur;
			}
			
			entity.addEffect(
				new EffectInstance(Effects.LEVITATION, dur)
				);
			if (entity instanceof PlayerEntity) {
				this.addEffect(
						new EffectInstance(Effects.GLOWING, finalDur)
					);
			}
		}
    }

	@Override
	public void tick() {
		super.tick();
		
		if (idleCooldown > 0) {
			idleCooldown--;
			if (idleCooldown == 0) {
				if (this.getTarget() == null)
					NostrumMagicaSounds.DAMAGE_EARTH.play(this);
				idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10); 
			}
		}
		
		if (effectCooldown > 0 && !level.isClientSide) {
			effectCooldown--;
			if (effectCooldown == 0) {
				
				// Lift up players in a large radius. Lift up other entities at a much smaller one
				
				// Non-players
				AxisAlignedBB bb = new AxisAlignedBB(getX() - 6, getY() - 40, getZ() - 6,
						getX() + 6, getY() + 40, getZ() + 6);
				List<Entity> entities = level.getEntities(this, bb);
				
				for (Entity entity : entities) {
					if (entity instanceof LivingEntity && !(entity instanceof SpriteEntity)) {
						
						if (entity instanceof PlayerEntity) {
							continue;
						}
						
						applyEffect((LivingEntity) entity);
					}
				}
				
				// Players
				for (PlayerEntity player : level.players()) {
					if (player.isCreative() || player.isSpectator()) {
						continue;
					}
					
					if (player.distanceToSqr(this) <= EFFECT_DISTANCE_SQ) {
						applyEffect(player);
					}
				}
				
				effectCooldown = 20 * 5;
			}
		}
	}
	
	@Override
	public ILoreTagged getLoreTag() {
		return SpriteLoreTag.instance;
	}
	
	public static final class SpriteLoreTag implements IEntityLoreTagged<SpriteEntity> {
		
		private static final SpriteLoreTag instance = new SpriteLoreTag();
		public static final SpriteLoreTag instance() {
			return instance;
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
		public InfoScreenTabs getTab() {
			return InfoScreenTabs.INFO_ENTITY;
		}

		@Override
		public EntityType<SpriteEntity> getEntityType() {
			return NostrumEntityTypes.sprite;
		}
	}
	
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		
		this.entityData.define(SPRITE_ANGRY, false);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundNBT compound) {
		super.readAdditionalSaveData(compound);
		
		this.entityData.set(SPRITE_ANGRY, compound.getBoolean("angry"));
	}

	@Override
	public void addAdditionalSaveData(CompoundNBT compound) {
    	super.addAdditionalSaveData(compound);
    	
    	compound.putBoolean("angry", this.isAngry());
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

    /**
     * Gets how bright this entity is.
     */
	@Override
    public float getBrightness()
    {
        return 1.0F;
    }

//	@Override
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		// Sprite Core
//		int chances = this.rand.nextInt(4);
//		int count = 0;
//		chances += lootingModifier;
//		
//		while (chances > 0) {
//			int eff = Math.min(chances, 4);
//			if (this.rand.nextInt(8) < eff) {
//				count++;
//			}
//			chances -= 4;
//		}
//		
//		for (int i = 0; i < count; i++) {
//			this.entityDropItem(NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1), 0);
//		}
//		
//		// Research scroll
//		chances = 1 + lootingModifier;
//		if (rand.nextInt(100) < chances) {
//			this.entityDropItem(NostrumSkillItem.getItem(SkillItemType.RESEARCH_SCROLL_SMALL, 1), 0);
//		}
//	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source.isProjectile()) {
			amount *= 0.25f;
		}
		
		boolean hurt = super.hurt(source, amount);
		
		if (hurt && !this.isAngry()) {
			setAngry(true);
			effectCooldown = 1; // Make next tick grou nd everyone again
			
			if (!this.level.isClientSide) {
				this.level.broadcastEntityEvent(this, (byte) 6);
			}
		}
		
		return hurt;
	}
	
	private void playEffect(IParticleData particle) {
		
		for (int i = 0; i < 15; ++i) {
			double d0 = this.random.nextGaussian() * 0.02D;
			double d1 = this.random.nextGaussian() * 0.02D;
			double d2 = this.random.nextGaussian() * 0.02D;
			this.level.addParticle(particle, this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), this.getY() + 0.5D + (double)(this.random.nextFloat() * this.getBbHeight()), this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(), d0, d1, d2);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void handleEntityEvent(byte id) {
		if (id == 6) {
			playEffect(ParticleTypes.ANGRY_VILLAGER);
		}
	}
	
	public boolean isAngry() {
		return this.entityData.get(SPRITE_ANGRY);
	}
	
	protected void setAngry(boolean angry) {
		this.entityData.set(SPRITE_ANGRY, angry);
	}
	
	@Override
	public float getWalkTargetValue(BlockPos pos, IWorldReader worldIn) {
		// Want to use dimension key but not available with IWorldReadyer
		RegistryKey<Biome> biomeKey = RegistryKey.create(Registry.BIOME_REGISTRY, worldIn.getBiome(pos).getRegistryName());
		
//		if (DimensionUtils.IsNether(worldIn.)) {
//			return 0; // Nether is very bright
//		}
		if (BiomeDictionary.hasType(biomeKey, BiomeDictionary.Type.NETHER)) {
			return 0;
		}
		
		// most monsters do 0.5 - worldIn.getBrightness(pos);
		final float tolerance;
		if (worldIn.getBlockState(pos).getMaterial() == Material.GRASS) { // ORGANIC is what grass blocks use
			// Higher light tolerance
			tolerance = 0.75f;
		} else {
			tolerance = .5f;
		}
		
		return tolerance - worldIn.getMaxLocalRawBrightness(pos);
	}

	@Override
	public EMagicElement getElement() {
		return EMagicElement.EARTH;
	}
}
