package com.smanzana.nostrummagica.entity;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.entity.tasks.EntityAIFollowEntityGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntitySpellAttackTask;
import com.smanzana.nostrummagica.loretag.ILoreSupplier;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spells.components.shapes.TouchShape;
import com.smanzana.nostrummagica.utils.SpellUtils;

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

public class EntitySprite extends CreatureEntity implements ILoreSupplier, IElementalEntity {
	
	public static final String ID = "entity_sprite";

	private static final DataParameter<Boolean> SPRITE_ANGRY = EntityDataManager.<Boolean>createKey(EntitySprite.class, DataSerializers.BOOLEAN);
	
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
	
	public EntitySprite(EntityType<? extends EntitySprite> type, World worldIn) {
        super(type, worldIn);
        
        idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
        effectCooldown = 20 * 5;
        this.experienceValue = 10;
    }

    @Override
    protected void registerGoals() {
    	EntitySprite.initStaticSpells();
    	
    	int priority = 1;
    	this.goalSelector.addGoal(priority++, new EntitySpellAttackTask<EntitySprite>(this, 20, 4, true, (sprite) -> {
    		return sprite.isAngry() && sprite.getAttackTarget() != null
    				&& sprite.getAttackTarget().getDistanceSq(sprite) <= TouchShape.TOUCH_RANGE * TouchShape.TOUCH_RANGE;
    	}, EARTH_ZAP));
        this.goalSelector.addGoal(priority++, new SwimGoal(this));
        this.goalSelector.addGoal(priority++, new EntityAIFollowEntityGeneric<EntitySprite>(this, 1D, 2f, 4f, false, null) {
        	@Override
        	protected LivingEntity getTarget(EntitySprite entity) {
        		return entity.getAttackTarget();
        	}
        	
        	@Override
        	protected boolean canFollow(EntitySprite entity) {
        		return entity.isAngry();
        	}
        });
        this.goalSelector.addGoal(priority++, new EntityAIFollowEntityGeneric<EntitySprite>(this, 1D, 6f, 10f, false, null) {
        	@Override
        	protected LivingEntity getTarget(EntitySprite entity) {
        		return entity.getAttackTarget();
        	}
        });
        this.goalSelector.addGoal(priority++, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(priority++, new LookAtGoal(this, PlayerEntity.class, 24.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
    }

    public static final AttributeModifierMap.MutableAttribute BuildAttributes() {
    	return CreatureEntity.func_233666_p_()
    		.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.33D)
    		.createMutableAttribute(Attributes.MAX_HEALTH, 40.0D)
    		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 2.0D)
    		.createMutableAttribute(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn)
    {
        this.playSound(SoundEvents.ENTITY_HUSK_STEP, 0.15F, 1.0F);
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
        return this.getHeight() * 0.4F;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn)
    {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getAttribute(Attributes.ATTACK_DAMAGE).getValue()));

        if (flag)
        {
            this.applyEnchantments(this, entityIn);
        }

        return flag;
    }

    public ActionResultType /*processInteract*/ func_230254_b_(PlayerEntity player, Hand hand, @Nonnull ItemStack stack) {
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

    public boolean canBeLeashedTo(PlayerEntity player)
    {
        return false;
    }
    
    private void applyEffect(LivingEntity entity) {
    	entity.removePotionEffect(Effects.LEVITATION);
    	entity.removePotionEffect(NostrumEffects.rooted);
    	if (this.isAngry()) {
    		entity.addPotionEffect(new EffectInstance(NostrumEffects.rooted, 20 * 10));
		} else {
			final int finalDur = 20*10;
			int dur = 20 * 1;
			if (entity == this.getAttackTarget()) {
				dur = finalDur;
			}
			
			entity.addPotionEffect(
				new EffectInstance(Effects.LEVITATION, dur)
				);
			if (entity instanceof PlayerEntity) {
				this.addPotionEffect(
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
				AxisAlignedBB bb = new AxisAlignedBB(getPosX() - 6, getPosY() - 40, getPosZ() - 6,
						getPosX() + 6, getPosY() + 40, getPosZ() + 6);
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
				for (PlayerEntity player : world.getPlayers()) {
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
	public ILoreTagged getLoreTag() {
		return SpriteLoreTag.instance;
	}
	
	public static final class SpriteLoreTag implements ILoreTagged {
		
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
	}
	
	
	@Override
	protected void registerData() {
		super.registerData();
		
		this.dataManager.register(SPRITE_ANGRY, false);
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		
		this.dataManager.set(SPRITE_ANGRY, compound.getBoolean("angry"));
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
    	super.writeAdditional(compound);
    	
    	compound.putBoolean("angry", this.isAngry());
	}
	
	@Override
	public boolean onLivingFall(float distance, float damageMulti) {
		return false; // No fall damage
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
	
	private void playEffect(IParticleData particle) {
		
		for (int i = 0; i < 15; ++i) {
			double d0 = this.rand.nextGaussian() * 0.02D;
			double d1 = this.rand.nextGaussian() * 0.02D;
			double d2 = this.rand.nextGaussian() * 0.02D;
			this.world.addParticle(particle, this.getPosX() + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), this.getPosY() + 0.5D + (double)(this.rand.nextFloat() * this.getHeight()), this.getPosZ() + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), d0, d1, d2);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void handleStatusUpdate(byte id) {
		if (id == 6) {
			playEffect(ParticleTypes.ANGRY_VILLAGER);
		}
	}
	
	public boolean isAngry() {
		return this.dataManager.get(SPRITE_ANGRY);
	}
	
	protected void setAngry(boolean angry) {
		this.dataManager.set(SPRITE_ANGRY, angry);
	}
	
	@Override
	public float getBlockPathWeight(BlockPos pos, IWorldReader worldIn) {
		// Want to use dimension key but not available with IWorldReadyer
		RegistryKey<Biome> biomeKey = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, worldIn.getBiome(pos).getRegistryName());
		
//		if (DimensionUtils.IsNether(worldIn.)) {
//			return 0; // Nether is very bright
//		}
		if (BiomeDictionary.hasType(biomeKey, BiomeDictionary.Type.NETHER)) {
			return 0;
		}
		
		// most monsters do 0.5 - worldIn.getBrightness(pos);
		final float tolerance;
		if (worldIn.getBlockState(pos).getMaterial() == Material.ORGANIC) { // ORGANIC is what grass blocks use
			// Higher light tolerance
			tolerance = 0.75f;
		} else {
			tolerance = .5f;
		}
		
		return tolerance - worldIn.getLight(pos);
	}

	@Override
	public EMagicElement getElement() {
		return EMagicElement.EARTH;
	}
}
