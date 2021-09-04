package com.smanzana.nostrummagica.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.tasks.EntityAIStayHomeTask;
import com.smanzana.nostrummagica.entity.tasks.EntitySpellAttackTask;
import com.smanzana.nostrummagica.integration.aetheria.blocks.WispBlock;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.NostrumSkillItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem.SkillItemType;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
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

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeHell;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityWisp extends EntityGolem implements ILoreTagged {
	
	protected static final double MAX_WISP_DISTANCE_SQ = 144;
	protected static final DataParameter<Optional<BlockPos>> HOME  = EntityDataManager.<Optional<BlockPos>>createKey(EntityWisp.class, DataSerializers.OPTIONAL_BLOCK_POS);
	protected static final DataParameter<EMagicElement> ELEMENT = EntityDataManager.<EMagicElement>createKey(EntityWisp.class, EMagicElement.Serializer);
	
	public static final String LoreKey = "nostrum__wisp";
	
	private int idleCooldown;
	private Spell defaultSpell;
	private @Nullable Spell lastSpell;
	private int perilTicks;
	private @Nullable BlockPos perilLoc;
	
	public EntityWisp(World worldIn) {
		super(worldIn);
		this.setSize(.75F, .75F);
		this.setNoGravity(true);
		this.moveHelper = new WispMoveHelper(this);
		
		idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10);
		perilTicks = 0;
	}
	
	public EntityWisp(World worldIn, BlockPos homePos) {
		this(worldIn);
		this.setHomePosAndDistance(homePos, (int) MAX_WISP_DISTANCE_SQ);
		this.setHome(homePos);
	}
	
	protected void initEntityAI() {
		int priority = 1;
		this.tasks.addTask(priority++, new AIRandomFly(this));
		this.tasks.addTask(priority++, new EntitySpellAttackTask<EntityWisp>(this, 20, 4, true, (wisp) -> {
			return wisp.getAttackTarget() != null;
		}, new Spell[0]){
			@Override
			public Spell pickSpell(Spell[] spells, EntityWisp wisp) {
				// Ignore empty array and use spell from the wisp
				return getSpellToUse();
			}
		});
		if (this.getHome() != null) {
			this.tasks.addTask(priority++, new EntityAIStayHomeTask<EntityWisp>(this, 1D, (MAX_WISP_DISTANCE_SQ * .8)));
		}
		
		priority = 1;
		this.targetTasks.addTask(priority++, new EntityAIHurtByTarget(this, true, new Class[] {EntityWisp.class}));
		if (worldObj != null && this.rand.nextBoolean() && worldObj.getBiome(this.getPosition()) instanceof BiomeHell) {
			this.targetTasks.addTask(priority++, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 10, true, false, null));
		} else {
			this.targetTasks.addTask(priority++, new EntityAINearestAttackableTarget<EntityMob>(this, EntityMob.class, 10, true, false, (mob) -> {
				return (mob instanceof IEntityTameable ? !((IEntityTameable) mob).isTamed()
						: true);
			}));
		}
	}
	
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.1D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(5.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(30.0);
		this.getEntityAttribute(AttributeMagicResist.instance()).setBaseValue(50.0D);
	}

	protected void playStepSound(BlockPos pos, Block blockIn)
	{
		this.playSound(SoundEvents.BLOCK_GLASS_STEP, 0.15F, 1.0F);
	}

	protected SoundEvent getHurtSound()
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

	public float getEyeHeight()
	{
		return this.height * 0.5F;
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

	public boolean canBeLeashedTo(EntityPlayer player)
	{
		return false;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (idleCooldown > 0) {
			idleCooldown--;
			if (idleCooldown == 0) {
				if (this.getAttackTarget() == null) {
					NostrumMagicaSounds.WISP_IDLE.play(this);
				}
				idleCooldown = NostrumMagica.rand.nextInt(20 * 30) + (20 * 10); 
			}
		}
		
		if (!worldObj.isRemote && this.getHome() == null && !this.isDead && this.getHealth() > 0) {
			if (perilLoc == null || !perilLoc.equals(getPosition())) {
				MutableBlockPos cursor = new MutableBlockPos();
				cursor.setPos(getPosition());
				while (worldObj.isAirBlock(cursor)) {
					cursor.move(EnumFacing.DOWN);
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
					
					this.attackEntityFrom(DamageSource.drown, 4.0F);
					if (this.isDead || this.getHealth() <= 0) {
						this.entityDropItem(EssenceItem.getEssence(elem, 1), 0);
					}
				}
			}
		}
		
		if (worldObj.isRemote) {
			EMagicElement element = this.getElement();
			if (element == null) element = EMagicElement.PHYSICAL;
			int color = element.getColor();
			NostrumParticles.GLOW_ORB.spawn(worldObj, new SpawnParams(
					1, posX, posY + height/2f, posZ, 0, 40, 0,
					new Vec3d(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05 - .025, rand.nextFloat() * .05 - .025), false
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
	protected void entityInit() {
		super.entityInit();
		
		this.dataManager.register(HOME, Optional.absent());
		this.dataManager.register(ELEMENT, EMagicElement.PHYSICAL);
	}
	
	protected void setHome(BlockPos home) {
		this.dataManager.set(HOME, Optional.of(home));
		this.setHomePosAndDistance(home, (int) MAX_WISP_DISTANCE_SQ);
	}
	
	public BlockPos getHome() {
		return this.dataManager.get(HOME).orNull();
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.hasKey("home", NBT.TAG_LONG)) {
			setHome(BlockPos.fromLong(compound.getLong("home")));
		}
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		
		BlockPos homePos = this.getHome();
		if (homePos != null) {
			compound.setLong("home", homePos.toLong());
		}
	}
	
	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {
		return false;
	}
	
	@Override
	public void fall(float distance, float damageMulti) {
		; // No fall damage
	}
	
	@Override
	protected void updateFallState(double y, boolean onGround, IBlockState stae, BlockPos pos) {
		
	}
	
	@SideOnly(Side.CLIENT)
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
		if (wasRecentlyHit && this.getHome() == null) {
			int chance = 1 + (2 * lootingModifier);
			if (this.rand.nextInt(100) < chance) {
				this.entityDropItem(NostrumResourceItem.getItem(ResourceType.WISP_PEBBLE, 1), 0);
			}
			
			// Research scroll
			int chances = 1 + lootingModifier;
			if (rand.nextInt(200) < chances) {
				this.entityDropItem(NostrumSkillItem.getItem(SkillItemType.RESEARCH_SCROLL_SMALL, 1), 0);
			}
		}
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		this.playEffect(EnumParticleTypes.CRIT);
		return super.attackEntityFrom(source, amount);
	}
	
	private void playEffect(EnumParticleTypes enumparticletypes) {
		
		for (int i = 0; i < 15; ++i) {
			double d0 = this.rand.nextGaussian() * 0.02D;
			double d1 = this.rand.nextGaussian() * 0.02D;
			double d2 = this.rand.nextGaussian() * 0.02D;
			this.worldObj.spawnParticle(enumparticletypes, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2, new int[0]);
		}
	}
	
	public EMagicElement getElement() {
		return this.dataManager.get(ELEMENT);
	}
	
	protected Spell getSpellToUse() {
		ItemStack scroll = null;
		BlockPos homePos = this.getHome();
		if (homePos != null) {
			scroll = WispBlock.instance().getScroll(worldObj, homePos);
		}
		
		@Nullable Spell spell = null;
		
		if (scroll != null) {
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
	static protected class WispMoveHelper extends EntityMoveHelper {
		private final EntityWisp parentEntity;
		private int courseChangeCooldown;

		public WispMoveHelper(EntityWisp wisp) {
			super(wisp);
			this.parentEntity = wisp;
		}

		public void onUpdateMoveHelper() {
			if (this.action == EntityMoveHelper.Action.MOVE_TO) {
				double d0 = this.posX - this.parentEntity.posX;
				double d1 = this.posY - this.parentEntity.posY;
				double d2 = this.posZ - this.parentEntity.posZ;
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;
				
				if (d3 < 6) {
					this.action = EntityMoveHelper.Action.WAIT;
					return;
				}

				if (this.courseChangeCooldown-- <= 0) {
					this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
					d3 = (double)MathHelper.sqrt_double(d3);

					if (this.isNotColliding(this.posX, this.posY, this.posZ, d3)) {
						this.parentEntity.motionX += d0 / d3 * 0.005D;
						this.parentEntity.motionY += d1 / d3 * 0.005D;
						this.parentEntity.motionZ += d2 / d3 * 0.005D;
					} else {
						this.action = EntityMoveHelper.Action.WAIT;
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
			AxisAlignedBB axisalignedbb = this.parentEntity.getEntityBoundingBox();

			for (int i = 1; (double)i < p_179926_7_; ++i) {
				axisalignedbb = axisalignedbb.offset(d0, d1, d2);

				if (!this.parentEntity.worldObj.getCollisionBoxes(this.parentEntity, axisalignedbb).isEmpty()) {
					return false;
				}
			}

			return true;
		}
	}
	
	// Copied from EntityFlying class
	@Override
	public void moveEntityWithHeading(float strafe, float forward) {
		if (this.isInWater()) {
			this.moveRelative(strafe, forward, 0.02F);
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.800000011920929D;
			this.motionY *= 0.800000011920929D;
			this.motionZ *= 0.800000011920929D;
		} else if (this.isInLava()) {
			this.moveRelative(strafe, forward, 0.02F);
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.5D;
			this.motionY *= 0.5D;
			this.motionZ *= 0.5D;
		} else {
			float f = 0.91F;

			if (this.onGround) {
				f = this.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(this.posZ))).getBlock().slipperiness * 0.91F;
			}

			float f1 = 0.16277136F / (f * f * f);
			this.moveRelative(strafe, forward, this.onGround ? 0.1F * f1 : 0.02F);
			f = 0.91F;

			if (this.onGround) {
				f = this.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(this.posZ))).getBlock().slipperiness * 0.91F;
			}

			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			this.motionX *= (double)f;
			this.motionY *= (double)f;
			this.motionZ *= (double)f;
		}

		this.prevLimbSwingAmount = this.limbSwingAmount;
		double d1 = this.posX - this.prevPosX;
		double d0 = this.posZ - this.prevPosZ;
		float f2 = MathHelper.sqrt_double(d1 * d1 + d0 * d0) * 4.0F;

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
	public boolean getCanSpawnHere() {
		if (!super.getCanSpawnHere()) {
			return false;
		}
		
		BlockPos blockpos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);

		if (this.worldObj.getLightFor(EnumSkyBlock.SKY, blockpos) > this.rand.nextInt(32)) {
			return false;
		} else {
			int i = this.worldObj.getLightFromNeighbors(blockpos);

			if (this.worldObj.isThundering()) {
				int j = this.worldObj.getSkylightSubtracted();
				this.worldObj.setSkylightSubtracted(10);
				i = this.worldObj.getLightFromNeighbors(blockpos);
				this.worldObj.setSkylightSubtracted(j);
			}

			return i <= this.rand.nextInt(12);
		}
	}
	
	static class AIRandomFly extends EntityAIBase {
		private final EntityWisp parentEntity;

		public AIRandomFly(EntityWisp wisp) {
			this.parentEntity = wisp;
			this.setMutexBits(1);
		}

		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		public boolean shouldExecute() {
			EntityMoveHelper entitymovehelper = this.parentEntity.getMoveHelper();

			if (!entitymovehelper.isUpdating()) {
				return true;
			} else {
				double d0 = entitymovehelper.getX() - this.parentEntity.posX;
				double d1 = entitymovehelper.getY() - this.parentEntity.posY;
				double d2 = entitymovehelper.getZ() - this.parentEntity.posZ;
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;
				return d3 < 1.0D || d3 > 3600.0D;
			}
		}

		/**
		 * Returns whether an in-progress EntityAIBase should continue executing
		 */
		public boolean continueExecuting() {
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
