package com.smanzana.nostrummagica.entity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.tasks.EntityAIStayHomeTask;
import com.smanzana.nostrummagica.entity.tasks.EntitySpellAttackTask;
import com.smanzana.nostrummagica.integration.aetheria.blocks.WispBlock;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.serializers.MagicElementDataSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.ChainShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.NetherBiome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityWisp extends GolemEntity implements ILoreTagged {
	
	public static final String ID = "entity_wisp";
	
	protected static final double MAX_WISP_DISTANCE_SQ = 144;
	protected static final DataParameter<Optional<BlockPos>> HOME  = EntityDataManager.<Optional<BlockPos>>createKey(EntityWisp.class, DataSerializers.OPTIONAL_BLOCK_POS);
	protected static final DataParameter<EMagicElement> ELEMENT = EntityDataManager.<EMagicElement>createKey(EntityWisp.class, MagicElementDataSerializer.instance);
	
	public static final String LoreKey = "nostrum__wisp";
	
	private int idleCooldown;
	private Spell defaultSpell;
	private @Nullable Spell lastSpell;
	private int perilTicks;
	private @Nullable BlockPos perilLoc;
	
	public EntityWisp(EntityType<? extends EntityWisp> type, World worldIn) {
		super(type, worldIn);
		this.setNoGravity(true);
		this.moveController = new WispMoveHelper(this);
		
		idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
		perilTicks = 0;
	}
	
	public EntityWisp(EntityType<? extends EntityWisp> type, World worldIn, BlockPos homePos) {
		this(type, worldIn);
		this.setHomePosAndDistance(homePos, (int) MAX_WISP_DISTANCE_SQ);
		this.setHome(homePos);
	}
	
	protected void registerGoals() {
		int priority = 1;
		this.goalSelector.addGoal(priority++, new AIRandomFly(this));
		this.goalSelector.addGoal(priority++, new EntitySpellAttackTask<EntityWisp>(this, 20, 4, true, (wisp) -> {
			return wisp.getAttackTarget() != null;
		}, new Spell[0]){
			@Override
			public Spell pickSpell(Spell[] spells, EntityWisp wisp) {
				// Ignore empty array and use spell from the wisp
				return getSpellToUse();
			}
		});
		if (this.getHome() != null) {
			this.goalSelector.addGoal(priority++, new EntityAIStayHomeTask<EntityWisp>(this, 1D, (MAX_WISP_DISTANCE_SQ * .8)));
		}
		
		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setCallsForHelp(EntityWisp.class));
		if (world != null && this.rand.nextBoolean() && world.getBiome(this.getPosition()) instanceof NetherBiome) {
			this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, null));
		} else {
			this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<MonsterEntity>(this, MonsterEntity.class, 10, true, false, (mob) -> {
				// Wisps spawned with no home will be neutral
				if (this.getHome() == null) {
					return false;
				}
				
				return (mob instanceof ITameableEntity ? !((ITameableEntity) mob).isEntityTamed()
						: true);
			}));
		}
	}
	
	protected void registerAttributes()
	{
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.1D);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(5.0D);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(30.0);
		this.getAttribute(AttributeMagicResist.instance()).setBaseValue(50.0D);
	}

	protected void playStepSound(BlockPos pos, BlockState blockIn)
	{
		this.playSound(SoundEvents.BLOCK_GLASS_STEP, 0.15F, 1.0F);
	}

	protected SoundEvent getHurtSound(DamageSource source)
	{
		return NostrumMagicaSounds.CAST_FAIL.getEvent();
	}

	protected SoundEvent getDeathSound()
	{
		return NostrumMagicaSounds.SHIELD_BREAK.getEvent();
	}

	/**
	 * Returns the volume for the sounds this mob makes.
	 */
	protected float getSoundVolume()
	{
		return 1F;
	}

	protected float getStandingEyeHeight(Pose pose, EntitySize size)
	{
		return this.getHeight() * 0.5F;
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
				if (this.getAttackTarget() == null) {
					NostrumMagicaSounds.WISP_IDLE.play(this);
				}
				idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10); 
			}
		}
		
		if (!world.isRemote && this.getHome() == null && this.isAlive() && this.getHealth() > 0) {
			if (perilLoc == null || !perilLoc.equals(getPosition())) {
				MutableBlockPos cursor = new MutableBlockPos();
				cursor.setPos(getPosition());
				while (world.isAirBlock(cursor)) {
					cursor.move(Direction.DOWN);
				}
				
				final int diff = (int) (posY) - cursor.getY();
				if (diff > 30) {
					perilLoc = this.getPosition().toImmutable();
				}
			}
			
			if (perilLoc != null) {
				perilTicks++;
				if (perilTicks > 20 * 20) {
					// POP
					final EMagicElement elem;
					if (this.defaultSpell != null) {
						elem = this.defaultSpell.getPrimaryElement();
					} else {
						// else random
						elem = EMagicElement.values()[rand.nextInt(EMagicElement.values().length)];
					}
					
					this.attackEntityFrom(DamageSource.DROWN, 4.0F);
					if (!this.isAlive() || this.getHealth() <= 0) {
						this.entityDropItem(EssenceItem.getEssence(elem, 1), 0);
					}
				}
			}
		}
		
		if (world.isRemote) {
			EMagicElement element = this.getElement();
			if (element == null) element = EMagicElement.PHYSICAL;
			int color = element.getColor();
			NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
					1, posX, posY + getHeight()/2f, posZ, 0, 40, 0,
					new Vec3d(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05 - .025, rand.nextFloat() * .05 - .025), null
					).color(color));
		}
	}
	
	@Override
	public String getLoreKey() {
		return LoreKey;
	}

	@Override
	public String getLoreDisplayName() {
		return "Wisps";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Strange floating vortexes of magical energy");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Strange floating vortexes of magical energy.", "They are drawn to mani crystals, and can be passively spawned to protect you and your base.");
	}
	
	
	@Override
	protected void registerData() {
		super.registerData();
		
		this.dataManager.register(HOME, Optional.empty());
		this.dataManager.register(ELEMENT, EMagicElement.PHYSICAL);
	}
	
	protected void setHome(BlockPos home) {
		this.dataManager.set(HOME, Optional.of(home));
		this.setHomePosAndDistance(home, (int) MAX_WISP_DISTANCE_SQ);
	}
	
	public BlockPos getHome() {
		return this.dataManager.get(HOME).orElse(null);
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if (compound.contains("home", NBT.TAG_LONG)) {
			setHome(BlockPos.fromLong(compound.getLong("home")));
		}
	}
	
	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		
		BlockPos homePos = this.getHome();
		if (homePos != null) {
			compound.putLong("home", homePos.toLong());
		}
	}
	
	@Override
	public boolean writeUnlessRemoved(CompoundNBT compound) {
		return false; // don't persist
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
	
//	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
//		if (wasRecentlyHit && this.getHome() == null) {
//			int chance = 1 + (2 * lootingModifier);
//			if (this.rand.nextInt(100) < chance) {
//				this.entityDropItem(NostrumResourceItem.getItem(ResourceType.WISP_PEBBLE, 1), 0);
//			}
//			
//			// Research scroll
//			int chances = 1 + lootingModifier;
//			if (rand.nextInt(200) < chances) {
//				this.entityDropItem(NostrumSkillItem.getItem(SkillItemType.RESEARCH_SCROLL_SMALL, 1), 0);
//			}
//		}
//	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		this.playEffect(ParticleTypes.CRIT);
		return super.attackEntityFrom(source, amount);
	}
	
	private void playEffect(IParticleData particle) {
		
		for (int i = 0; i < 15; ++i) {
			double d0 = this.rand.nextGaussian() * 0.02D;
			double d1 = this.rand.nextGaussian() * 0.02D;
			double d2 = this.rand.nextGaussian() * 0.02D;
			this.world.addParticle(particle, this.posX + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), this.posY + 0.5D + (double)(this.rand.nextFloat() * this.getHeight()), this.posZ + (double)(this.rand.nextFloat() * this.getWidth() * 2.0F) - (double)this.getWidth(), d0, d1, d2);
		}
	}
	
	public EMagicElement getElement() {
		return this.dataManager.get(ELEMENT);
	}
	
	protected Spell getSpellToUse() {
		ItemStack scroll = ItemStack.EMPTY;
		BlockPos homePos = this.getHome();
		if (homePos != null) {
			scroll = WispBlock.instance().getScroll(world, homePos);
		}
		
		@Nullable Spell spell = null;
		
		if (!scroll.isEmpty()) {
			spell = SpellScroll.getSpell(scroll);
		}
		
		if (spell == null) {
			// Use a random spell
			init();
			if (this.defaultSpell == null) {
				this.defaultSpell = defaultSpells.get(rand.nextInt(defaultSpells.size()));
				this.dataManager.set(ELEMENT, defaultSpell.getPrimaryElement());
			}
			
			spell = this.defaultSpell;
		}
		
		if (spell != lastSpell) {
			lastSpell = spell;
			this.dataManager.set(ELEMENT, spell.getPrimaryElement());
		}
		
		return spell;
	}
	
	// Adapted from the wisp move helper
	static protected class WispMoveHelper extends MovementController {
		private final EntityWisp parentEntity;
		private int courseChangeCooldown;

		public WispMoveHelper(EntityWisp wisp) {
			super(wisp);
			this.parentEntity = wisp;
		}

		@Override
		public void tick() {
			if (this.action == MovementController.Action.MOVE_TO) {
				double d0 = this.posX - this.parentEntity.posX;
				double d1 = this.posY - this.parentEntity.posY;
				double d2 = this.posZ - this.parentEntity.posZ;
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;
				
				if (d3 < 6) {
					this.action = MovementController.Action.WAIT;
					return;
				}

				if (this.courseChangeCooldown-- <= 0) {
					this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
					d3 = (double)MathHelper.sqrt(d3);

					if (this.isNotColliding(this.posX, this.posY, this.posZ, d3)) {
						this.parentEntity.setMotion(this.parentEntity.getMotion().add(
								d0 / d3 * 0.005D,
								d1 / d3 * 0.005D,
								d2 / d3 * 0.005D
								));
					} else {
						this.action = MovementController.Action.WAIT;
					}
				}
			}
		}

		/**
		 * Checks if entity bounding box is not colliding with terrain
		 */
		private boolean isNotColliding(double x, double y, double z, double p_179926_7_) {
			double d0 = (x - this.parentEntity.posX) / p_179926_7_;
			double d1 = (y - this.parentEntity.posY) / p_179926_7_;
			double d2 = (z - this.parentEntity.posZ) / p_179926_7_;
			AxisAlignedBB axisalignedbb = this.parentEntity.getBoundingBox();

			for (int i = 1; (double)i < p_179926_7_; ++i) {
				axisalignedbb = axisalignedbb.offset(d0, d1, d2);

				if (!this.parentEntity.world.isCollisionBoxesEmpty(this.parentEntity, axisalignedbb)) {
					return false;
				}
			}

			return true;
		}
	}
	
	// Copied from EntityFlying class
	@Override
	public void travel(Vec3d how) {
		if (this.isInWater()) {
			this.moveRelative(0.02F, how);
			this.move(MoverType.SELF, this.getMotion());
			this.setMotion(this.getMotion().scale(0.8));
		} else if (this.isInLava()) {
			this.moveRelative(0.02F, how);
			this.move(MoverType.SELF, this.getMotion());
			this.setMotion(this.getMotion().scale(0.5));
		} else {
			float f = 0.91F;

			if (this.onGround) {
				//f = this.world.getBlockState(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.posZ))).getBlock().slipperiness * 0.91F;
				BlockPos underPos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
				BlockState underState = this.world.getBlockState(underPos);
				f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.91F;
			}

			float f1 = 0.16277136F / (f * f * f);
			this.moveRelative(this.onGround ? 0.1F * f1 : 0.02F, how);
			f = 0.91F;

			if (this.onGround) {
				//f = this.world.getBlockState(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.posZ))).getBlock().slipperiness * 0.91F;
				BlockPos underPos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
				BlockState underState = this.world.getBlockState(underPos);
				f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.91F;
			}

			this.move(MoverType.SELF, this.getMotion());
			this.setMotion(this.getMotion().scale(f));
		}

		this.prevLimbSwingAmount = this.limbSwingAmount;
		double d1 = this.posX - this.prevPosX;
		double d0 = this.posZ - this.prevPosZ;
		float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;

		if (f2 > 1.0F) {
			f2 = 1.0F;
		}

		this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
		this.limbSwing += this.limbSwingAmount;
	}

	/**
	 * returns true if this entity is by a ladder, false otherwise
	 */
	@Override
	public boolean isOnLadder() {
		return false;
	}
	
	@Override
	public boolean canSpawn(IWorld world, SpawnReason spawnReason) {
		if (!super.canSpawn(world, spawnReason)) {
			return false;
		}
		
		if (world.getDimension().getType() == DimensionType.OVERWORLD) {
			BlockPos blockpos = new BlockPos(this.posX, this.getBoundingBox().minY, this.posZ);
			
			if (world.getLight(blockpos) > .75f) {
				return false;
			}
	
			List<EntityWisp> wisps = world.getEntitiesWithinAABB(EntityWisp.class, this.getBoundingBox().grow(20, 20, 20), (w) -> {
				return w !=  EntityWisp.this;
			});
			return wisps.size() < 20;
		} else {
			// Other dimensions, just check nearby wisp count
			List<EntityWisp> wisps = world.getEntitiesWithinAABB(EntityWisp.class, this.getBoundingBox().grow(20, 20, 20), (w) -> {
				return w !=  EntityWisp.this;
			});
			
			return wisps.size() < 20;
		}
	}
	
	static class AIRandomFly extends Goal {
		private final EntityWisp parentEntity;

		public AIRandomFly(EntityWisp wisp) {
			this.parentEntity = wisp;
			this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		/**
		 * Returns whether the Goal should begin execution.
		 */
		public boolean shouldExecute() {
			MovementController MovementController = this.parentEntity.getMoveHelper();

			if (!MovementController.isUpdating()) {
				return true;
			} else {
				double d0 = MovementController.getX() - this.parentEntity.posX;
				double d1 = MovementController.getY() - this.parentEntity.posY;
				double d2 = MovementController.getZ() - this.parentEntity.posZ;
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;
				return d3 < 1.0D || d3 > 3600.0D;
			}
		}

		/**
		 * Returns whether an in-progress Goal should continue executing
		 */
		@Override
		public boolean shouldContinueExecuting() {
			return false;
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		public void startExecuting() {
			Random random = this.parentEntity.getRNG();
			double d0 = this.parentEntity.posX + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
			double d1 = this.parentEntity.posY + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
			double d2 = this.parentEntity.posZ + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
			this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 0.3D);
		}
	}
	
	private static List<Spell> defaultSpells;
	
	private static void putSpell(String name,
			SpellTrigger trigger,
			SpellShape shape,
			EMagicElement element,
			int power,
			EAlteration alteration) {
		Spell spell = new Spell(name, true);
		spell.addPart(new SpellPart(trigger));
		spell.addPart(new SpellPart(shape, element, power, alteration));
		
		defaultSpells.add(spell);
	}
	
	private static void init() {
		if (defaultSpells == null) {
			defaultSpells = new ArrayList<>();
			
			Spell spell;
			
			// Physical
			putSpell("Physic Blast",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					1,
					null);
//			putSpell("Shield",
//					SelfTrigger.instance(),
//					AoEShape.instance(),
//					EMagicElement.PHYSICAL,
//					1,
//					EAlteration.RESIST);
//			putSpell("Weaken",
//					AITargetTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.PHYSICAL,
//					1,
//					EAlteration.INFLICT);
//			putSpell("Weaken II",
//					AITargetTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.PHYSICAL,
//					2,
//					EAlteration.INFLICT);
//			putSpell("Crush",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.PHYSICAL,
//					1,
//					null);
//			putSpell("Bone Crusher",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.PHYSICAL,
//					2,
//					null);
			
			// Lightning
			putSpell("Lightning Ball I",
					ProjectileTrigger.instance(),
					ChainShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					null);
//			putSpell("Magic Shell",
//					SelfTrigger.instance(),
//					AoEShape.instance(),
//					EMagicElement.LIGHTNING,
//					1,
//					EAlteration.RESIST);
//			putSpell("Bolt",
//					BeamTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.LIGHTNING,
//					1,
//					EAlteration.CONJURE);
//			putSpell("Shock",
//					AITargetTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.LIGHTNING,
//					1,
//					EAlteration.INFLICT);
//			putSpell("Lightning Ball I",
//					ProjectileTrigger.instance(),
//					ChainShape.instance(),
//					EMagicElement.LIGHTNING,
//					1,
//					null);
//			putSpell("Lightning Ball II",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.LIGHTNING,
//					2,
//					null);
			
			// Fire
			putSpell("Burn",
					SeekingBulletTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.FIRE,
					1,
					EAlteration.CONJURE);
//			spell = new Spell("Fireball", true);
//			spell.addPart(new SpellPart(ProjectileTrigger.instance()));
//			spell.addPart(new SpellPart(AoEShape.instance(), EMagicElement.FIRE,
//					1, null, new SpellPartParam(3, false)));
//			defaultSpells.add(spell);
//			putSpell("Flare",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.FIRE,
//					3,
//					null);

			// Ice
			spell = new Spell("Frostbite", true);
			spell.addPart(new SpellPart(ProjectileTrigger.instance()));
			spell.addPart(new SpellPart(SingleShape.instance(), EMagicElement.ICE,
					1, EAlteration.INFLICT));
			spell.addPart(new SpellPart(SingleShape.instance(), EMagicElement.ICE,
					1, null));
			defaultSpells.add(spell);
//			putSpell("Magic Aegis",
//					SelfTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.ICE,
//					2,
//					EAlteration.SUPPORT);
//			putSpell("Ice Shard",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.ICE,
//					1,
//					null);
//			spell = new Spell("Group Frostbite", true);
//			spell.addPart(new SpellPart(ProjectileTrigger.instance()));
//			spell.addPart(new SpellPart(AoEShape.instance(), EMagicElement.ICE,
//					1, EAlteration.INFLICT, new SpellPartParam(5, false)));
//			defaultSpells.add(spell);
//			
//			putSpell("Hand Of Cold",
//					MagicCyclerTrigger.instance(),
//					AoEShape.instance(),
//					EMagicElement.ICE,
//					2,
//					EAlteration.RUIN);
			
			// Earth
			putSpell("Rock Fling",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					1,
					null);
//			putSpell("Earth Aegis",
//					SelfTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.EARTH,
//					2,
//					EAlteration.SUPPORT);
//			putSpell("Earth Aegis II",
//					SelfTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.EARTH,
//					3,
//					EAlteration.SUPPORT);
//			putSpell("Roots",
//					AITargetTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.EARTH,
//					3,
//					EAlteration.INFLICT);
//			putSpell("Rock Fling",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.EARTH,
//					1,
//					null);
//			putSpell("Earth Bash",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.EARTH,
//					2,
//					null);
			
			// Wind
			putSpell("Wind Slash",
					MagicCutterTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					1,
					null);
//			putSpell("Gust",
//					SelfTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.WIND,
//					2,
//					EAlteration.RESIST);
//			putSpell("Poison",
//					AITargetTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.WIND,
//					1,
//					EAlteration.INFLICT);
//			putSpell("Wind Slash",
//					MagicCutterTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.WIND,
//					1,
//					null);
//			putSpell("Wind Ball I",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.WIND,
//					2,
//					null);
//			putSpell("Wind Ball II",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.WIND,
//					3,
//					null);
			
			// Ender
			putSpell("Ender Beam",
					BeamTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ENDER,
					1,
					null);
//			spell = new Spell("Blinker", true);
//			spell.addPart(new SpellPart(SelfTrigger.instance()));
//			spell.addPart(new SpellPart(DamagedTrigger.instance()));
//			spell.addPart(new SpellPart(OtherTrigger.instance()));
//			spell.addPart(new SpellPart(SingleShape.instance(), EMagicElement.ENDER,
//					2, EAlteration.GROWTH));
//			defaultSpells.add(spell);
//			putSpell("Blindness",
//					ProjectileTrigger.instance(),
//					AoEShape.instance(),
//					EMagicElement.ENDER,
//					1,
//					EAlteration.INFLICT);
//			putSpell("Random Teleport",
//					ProjectileTrigger.instance(),
//					SingleShape.instance(),
//					EMagicElement.ENDER,
//					1,
//					EAlteration.CONJURE);
		}
	}
}
