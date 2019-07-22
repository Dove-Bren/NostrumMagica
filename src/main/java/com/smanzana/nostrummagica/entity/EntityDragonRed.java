package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.entity.tasks.DragonAINearestAttackableTarget;
import com.smanzana.nostrummagica.entity.tasks.DragonFlyRandomTask;
import com.smanzana.nostrummagica.entity.tasks.DragonLandTask;
import com.smanzana.nostrummagica.entity.tasks.DragonMeleeAttackTask;
import com.smanzana.nostrummagica.entity.tasks.DragonSpellAttackTask;
import com.smanzana.nostrummagica.entity.tasks.DragonTakeoffLandTask;
import com.smanzana.nostrummagica.items.DragonEggFragment;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DamagedTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.OtherTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityDragonRed extends EntityDragon {

	private static enum FlyState {
		LANDED,
		TAKING_OFF,
		FLYING,
		LANDING,
	}
	
	private static enum DragonPhase {
		GROUNDED_PHASE,
		FLYING_PHASE,
		RAMPAGE_PHASE,
	}
	
	private static final DataParameter<Integer> DRAGON_FLYING =
			EntityDataManager.<Integer>createKey(EntityDragonRed.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> DRAGON_SLASH =
			EntityDataManager.<Boolean>createKey(EntityDragonRed.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> DRAGON_BITE =
			EntityDataManager.<Boolean>createKey(EntityDragonRed.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> DRAGON_PHASE =
			EntityDataManager.<Integer>createKey(EntityDragonRed.class, DataSerializers.VARINT);
	
	private static final String DRAGON_SERIAL_FLYING_TOK = "DragonFlying";
	private static final String DRAGON_SERIAL_PHASE_TOK = "DragonPhase";

	// How long landing or taking off takes, in milliseconds
	public static long ANIM_UNFURL_DUR = 1000;
	
	public static long ANIM_SLASH_DUR = 500;
	
	public static long ANIM_BITE_DUR = 250;
	
	private static Spell DSPELL_Fireball;
	private static Spell DSPELL_Fireball2;
	private static Spell DSPELL_Speed;
	private static Spell DSPELL_Shield;
	private static Spell DSPELL_Weaken;
	private static Spell DSPELL_Curse;
	
	private static Spell makeSpell(
			String name,
			Object ... objects) {
		Spell spell = new Spell(name, true);
		
		for (int i = 0; i < objects.length; i++) {
			Object o = objects[i];
			if (o instanceof SpellTrigger) {
				SpellTrigger trigger = (SpellTrigger) o;
				spell.addPart(new SpellPart(trigger));
			} else if (o instanceof SpellShape) {
				SpellShape shape = (SpellShape) o;
				EMagicElement element = (EMagicElement) objects[++i];
				Integer level = (Integer) objects[++i];
				EAlteration alt = (EAlteration) objects[++i];
				SpellPartParam param = (SpellPartParam) objects[++i];
				
				if (param == null) {
					param = new SpellPartParam(0, false);
				}
				
				spell.addPart(new SpellPart(shape, element, level, alt, param));
			}
		}
		
		return spell;
	}
	
	private static void initSpells() {
		
		if (DSPELL_Fireball == null) {
			DSPELL_Fireball = makeSpell("Fireball",
					ProjectileTrigger.instance(),
					AoEShape.instance(),
					EMagicElement.FIRE,
					2,
					null,
					new SpellPartParam(3, false));
			DSPELL_Fireball2 = makeSpell("Fireball2",
					ProjectileTrigger.instance(),
					AoEShape.instance(),
					EMagicElement.FIRE,
					3,
					null,
					new SpellPartParam(3, false));
			DSPELL_Speed = makeSpell("Speed",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					1,
					EAlteration.SUPPORT,
					null);
			DSPELL_Shield = makeSpell("Shield",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					2,
					EAlteration.SUPPORT,
					null);
			DSPELL_Weaken = makeSpell("Weaken",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					2,
					EAlteration.INFLICT,
					null);
			DSPELL_Curse = makeSpell("Curse",
					SelfTrigger.instance(),
					DamagedTrigger.instance(),
					OtherTrigger.instance(),
					DamagedTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					2,
					null,
					null,
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					1,
					EAlteration.INFLICT,
					null);
		}
	}
	
	private final BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.RED, BossInfo.Overlay.NOTCHED_10)).setDarkenSky(true);
	
	// Time we entered our current flying state.
	// Used for animations.
	// Set on every state change, meaning the time indicated
	// depends on our state.
	private long flyStateTime;
	
	// Time in MS when we last slashed.
	private long slashTime;
	
	private long biteTime;
	
	// AI. First array is indexed by the phase. Second is just a collection of tasks.
	private EntityAIBase[][] flyingAI;
	private EntityAIBase[][] groundedAI;
	
	private EntityAIBase[] lastAI;
	
	public EntityDragonRed(World worldIn) {
		super(worldIn);
        this.setSize(6F, 4.6F);
        this.stepHeight = 2;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
        this.experienceValue = 1000;
        this.noClip = false;
        
        this.setFlyState(FlyState.LANDED);
	}
	
	private DragonPhase getPhase() {
		return DragonPhase.values()[this.dataManager.get(DRAGON_PHASE).intValue()];
	}
	
	private void setPhase(DragonPhase phase) {
		this.dataManager.set(DRAGON_PHASE, phase.ordinal());
	}
	
	private FlyState getFlyState() {
		return FlyState.values()[this.dataManager.get(DRAGON_FLYING).intValue()];
	}
	
	private void setFlyState(FlyState state) {
		this.dataManager.set(DRAGON_FLYING, state.ordinal());
		//onFlightStateChange();
	}
	
	private void onFlightStateChange() {
		flyStateTime = System.currentTimeMillis();
		
		FlyState state = getFlyState();
		if (state == FlyState.FLYING) {
			entityStartFlying();
		} else if (state == FlyState.LANDED) {
			entityStopFlying();
		}
	}
	
	private void onPhaseChange() {
		DragonPhase phase = this.getPhase();
		if (phase == DragonPhase.FLYING_PHASE) {
			if (!this.isTryingToLand() && !this.isFlying()) {
				// Conveniently will set up AI
				this.startFlying();
			}
			NostrumMagicaSounds.DRAGON_DEATH.play(this);
		} else if (phase == DragonPhase.RAMPAGE_PHASE) {
			NostrumMagicaSounds.DRAGON_DEATH.play(this);
			// No convenience. Set up AI depending on if we're flying or not
			if (!this.worldObj.isRemote) {
				if (this.isFlying()) {
					this.setFlyingAI();
				} else {
					this.setGroundedAI();
				}
			}
		}
	}
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (key == DRAGON_FLYING) {
			onFlightStateChange();
		} else if (key == DRAGON_SLASH) {
			if (this.dataManager.get(DRAGON_SLASH)) {
				this.slashTime = System.currentTimeMillis();
			}
		} else if (key == DRAGON_BITE) {
			if (this.dataManager.get(DRAGON_BITE)) {
				this.biteTime = System.currentTimeMillis();
			}
		} else if (key == DRAGON_PHASE) {
			onPhaseChange();
		}
	}
	
	public boolean isFlying() {
		FlyState state = getFlyState();
		return state == FlyState.FLYING
				|| (state == FlyState.LANDING && !this.onGround);
	}
	
	// Bad name, but are we currently landing or taking off?
	public boolean isFlightTransitioning() {
		FlyState state = getFlyState();
		return state == FlyState.LANDING
				|| state == FlyState.TAKING_OFF;
	}
	
	// For use in conjunction with isFlightTransitioning.
	public boolean isLanding() {
		FlyState state = getFlyState();
		return state == FlyState.LANDING;
	}
	
	@Override
	public boolean isTryingToLand() {
		return isLanding();
	}
	
	public long getFlyStateTime() {
		return flyStateTime;
	}
	
	public long getLastSlashTime() {
		return this.slashTime;
	}
	
	public long getLastBiteTime() {
		return this.biteTime;
	}
	
	private void initBaseAI() {
		
		initSpells();
		
		// Order on these are priority numbers!
		flyingAI = new EntityAIBase[][] {
			// PHASE_GROUNDED
			new EntityAIBase[] {},
			new EntityAIBase[] {
        		new DragonLandTask(this),
        		new DragonMeleeAttackTask(this, 1.0D, true),
        		new DragonSpellAttackTask<EntityDragonRed>(this, (20 * 3), 20, true, DSPELL_Fireball),
        		//new DragonFlyStrafeTask<EntityDragonRed>(this, 20),
        		new DragonTakeoffLandTask(this),
        		new DragonFlyRandomTask(this),
			},
			new EntityAIBase[] {
				new DragonLandTask(this),
        		new DragonMeleeAttackTask(this, 1.0D, true),
        		new DragonTakeoffLandTask(this),
				new DragonSpellAttackTask<EntityDragonRed>(this, (20 * 3), 12, true, DSPELL_Fireball2),
				new DragonSpellAttackTask<EntityDragonRed>(this, (20 * 5), 10, false, DSPELL_Speed, DSPELL_Shield),
				new DragonSpellAttackTask<EntityDragonRed>(this, (20 * 5), 20, false, DSPELL_Weaken),
				new DragonSpellAttackTask<EntityDragonRed>(this, (20 * 30), 20, true, DSPELL_Curse),
        		//new DragonFlyStrafeTask<EntityDragonRed>(this, 20),
        		new DragonFlyRandomTask(this),
			}
        };
        groundedAI = new EntityAIBase[][] {
        	new EntityAIBase[] {
        		new DragonMeleeAttackTask(this, 1.0D, true),
        		new EntityAIWander(this, 1.0D, 30)
        	},
        	new EntityAIBase[] {
        		new DragonSpellAttackTask<EntityDragonRed>(this, (20 * 3), 20, true, DSPELL_Fireball),
        		new DragonTakeoffLandTask(this),
    			new DragonMeleeAttackTask(this, 1.0D, true),
        		new EntityAIWander(this, 1.0D, 30)
        	},
        	new EntityAIBase[] {
    			new DragonSpellAttackTask<EntityDragonRed>(this, (20 * 3), 12, true, DSPELL_Fireball2),
				new DragonSpellAttackTask<EntityDragonRed>(this, (20 * 5), 10, false, DSPELL_Speed, DSPELL_Shield),
				new DragonSpellAttackTask<EntityDragonRed>(this, (20 * 5), 20, false, DSPELL_Weaken),
				new DragonSpellAttackTask<EntityDragonRed>(this, (20 * 30), 20, true, DSPELL_Curse),
        		new DragonTakeoffLandTask(this),
    			new DragonMeleeAttackTask(this, 1.0D, true),
        		new EntityAIWander(this, 1.0D, 30)
        	}
        		
        };
		
//      this.tasks.addTask(1, new EntityAISwimming(this));
//		this.tasks.addTask(4, new EntityAIWander(this, 1.0D, 30));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
		this.targetTasks.addTask(2, new DragonAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
		this.targetTasks.addTask(3, new DragonAINearestAttackableTarget<EntityZombie>(this, EntityZombie.class, true));
		this.targetTasks.addTask(4, new DragonAINearestAttackableTarget<EntitySheep>(this, EntitySheep.class, true));
		this.targetTasks.addTask(5, new DragonAINearestAttackableTarget<EntityCow>(this, EntityCow.class, true));
		this.targetTasks.addTask(6, new DragonAINearestAttackableTarget<EntityPig>(this, EntityPig.class, true));
		this.targetTasks.addTask(7, new DragonAINearestAttackableTarget<EntityVillager>(this, EntityVillager.class, true));
		this.targetTasks.addTask(8, new DragonAINearestAttackableTarget<EntityHorse>(this, EntityHorse.class, true));
		this.targetTasks.addTask(9, new DragonAINearestAttackableTarget<EntityGiantZombie>(this, EntityGiantZombie.class, true));
		this.targetTasks.addTask(10, new DragonAINearestAttackableTarget<EntityPolarBear>(this, EntityPolarBear.class, true));
	}
	
	private void setFlyingAI() {
		DragonPhase phase = this.getPhase();
		// Remove grounded
		if (lastAI != null) {
			for (EntityAIBase ai : lastAI) {
				this.tasks.removeTask(ai);
			}
		}
		
		lastAI = this.flyingAI[phase.ordinal()];
		
		if (lastAI != null && lastAI.length > 0) {
			for (int i = 0; i < lastAI.length; i++) {
				this.tasks.addTask(i, lastAI[i]);
			}
		}
	}
	
	private void setGroundedAI() {
		DragonPhase phase = this.getPhase();
		
		// Remove flying
		if (lastAI != null) {
			for (EntityAIBase ai : lastAI) {
				this.tasks.removeTask(ai);
			}
		}
		
		lastAI = this.groundedAI[phase.ordinal()];
		
		if (lastAI != null && lastAI.length > 0) {
			for (int i = 0; i < lastAI.length; i++) {
				this.tasks.addTask(i, lastAI[i]);
			}
		}
	}
	
	@Override
	protected void initEntityAI() {
		this.initBaseAI();
		
		if (isFlying()) {
			this.setFlyingAI();
		} else {
			this.setGroundedAI();
		}
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(2000.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64D);
    }
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(DRAGON_FLYING, FlyState.LANDED.ordinal());
		this.dataManager.register(DRAGON_SLASH, false);
		this.dataManager.register(DRAGON_BITE, false);
		this.dataManager.register(DRAGON_PHASE, DragonPhase.GROUNDED_PHASE.ordinal());
	}
	
	@Override
	protected boolean canDespawn() {
		return false;
	}
	
	public boolean canAttackClass(Class <? extends EntityLivingBase > cls) {
		return true;
	}
	
	public boolean isNonBoss() {
		return false;
	}
	
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);

		if (compound.hasKey(DRAGON_SERIAL_PHASE_TOK, NBT.TAG_ANY_NUMERIC)) {
        	int i = compound.getByte(DRAGON_SERIAL_PHASE_TOK);
            this.setPhase(DragonPhase.values()[i]);
        }
		
        if (compound.hasKey(DRAGON_SERIAL_FLYING_TOK, NBT.TAG_ANY_NUMERIC)) {
        	int i = compound.getByte(DRAGON_SERIAL_FLYING_TOK);
        	FlyState state = FlyState.values()[i];
        	if (state == FlyState.LANDING) {
        		// Actaully flying. Cancel landing
        		state = FlyState.FLYING;
        	} else if (state == FlyState.TAKING_OFF) {
        		// Still on the ground
        		state = FlyState.LANDED;
        	}
            this.setFlyState(state);
        }
	}
	
	public void writeEntityToNBT(NBTTagCompound compound) {
    	super.writeEntityToNBT(compound);
    	compound.setByte(DRAGON_SERIAL_PHASE_TOK, (byte)this.getPhase().ordinal());
        compound.setByte(DRAGON_SERIAL_FLYING_TOK, (byte)this.getFlyState().ordinal());
	}
	
	public void startFlying() {
		if (getFlyState() == FlyState.LANDED) {
			setFlyState(FlyState.TAKING_OFF);
		}
	}
	
	public void startLanding() {
		if (getFlyState() == FlyState.FLYING) {
			setFlyState(FlyState.LANDING);
		}
	}
	
	// Actually start flying. Called internally when animations are done.
	private void entityStartFlying() {
		if (!this.worldObj.isRemote) {
			this.moveHelper = new EntityDragon.DragonFlyMoveHelper(this);
			this.navigator = new EntityDragon.PathNavigateDragonFlier(this, worldObj);
			this.setFlyingAI();
		}
		
		this.setNoGravity(true);
		this.addVelocity(Math.cos(this.rotationYaw) * .2, 0.5, Math.sin(this.rotationYaw) * .2);
	}
	
	private void entityStopFlying() {
		if (!this.worldObj.isRemote) {
			this.moveHelper = new EntityMoveHelper(this);
			this.navigator = this.getNewNavigator(worldObj);
			this.setGroundedAI();
		}
		this.setNoGravity(false);
	}
	
	public void slash(EntityLivingBase target) {
		this.dataManager.set(DRAGON_SLASH, Boolean.TRUE);
		
		this.attackEntityAsMob(target);
	}
	
	public void bite(EntityLivingBase target) {
		this.dataManager.set(DRAGON_BITE, Boolean.TRUE);
		
		NostrumMagicaSounds.DRAGON_BITE.play(this);
		
		this.biteDamageInternal(target);
	}
	
	private void biteDamageInternal(EntityLivingBase target) {
		float f = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
		
		// Dragons do 20 damage while on the ground, and 16 when flying
		if (!this.isFlying()) {
			f *= 2.0;
		} else {
			f *= 1.6;
		}
		
		int i = 0;

		i = 2;

		boolean flag = target.attackEntityFrom(DamageSource.causeMobDamage(this), f);

		if (flag)
		{
			if (i > 0)
			{
				target.knockBack(this, (float)i * 0.5F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
				this.motionX *= 0.6D;
				this.motionZ *= 0.6D;
			}

			if (target instanceof EntityPlayer)
			{
				EntityPlayer entityplayer = (EntityPlayer)target;
				ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : null;

				if (itemstack1 != null && itemstack1.getItem() == Items.SHIELD)
				{
					float f1 = 0.5F;

					if (this.rand.nextFloat() < f1)
					{
						entityplayer.getCooldownTracker().setCooldown(Items.SHIELD, 100);
						this.worldObj.setEntityState(entityplayer, (byte)30);
					}
				}
			}
		}
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		//System.out.println("(" + this.posX + ", " + this.posZ + ")");
		
		long now = System.currentTimeMillis();
		if (isFlightTransitioning()) {
			// Still unfurling wings and stuff. Wait to transition!
			boolean landing = (FlyState.LANDING == getFlyState());
			
			if (landing && !this.onGround) {
				; // Let movement AI keep going till we find ground
			} else {
				if (now - flyStateTime >= ANIM_UNFURL_DUR) {
					if (landing) {
						setFlyState(FlyState.LANDED);
					} else {
						setFlyState(FlyState.FLYING);
					}
				}
			}
		}
		
		if (this.dataManager.get(DRAGON_SLASH)) {
			if (now - slashTime >= ANIM_SLASH_DUR) {
				this.dataManager.set(DRAGON_SLASH, Boolean.FALSE);
			}
		}
		
		if (this.dataManager.get(DRAGON_BITE)) {
			if (now - biteTime >= ANIM_BITE_DUR) {
				this.dataManager.set(DRAGON_BITE, Boolean.FALSE);
			}
		}
		
		DragonPhase phase = this.getPhase();
		if (phase == DragonPhase.GROUNDED_PHASE) {
			float ratio = this.getHealth() / this.getMaxHealth();
			if (ratio <= 0.70f) {
				this.setPhase(DragonPhase.FLYING_PHASE);
			}
		} else if (phase == DragonPhase.FLYING_PHASE) {
			float ratio = this.getHealth() / this.getMaxHealth();
			if (ratio <= 0.40f) {
				this.setPhase(DragonPhase.RAMPAGE_PHASE);
			}
		}
	}
	
	@Override
	protected void updateAITasks() {
		super.updateAITasks();
		
		this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
	}
	
	public void addTrackingPlayer(EntityPlayerMP player) {
		super.addTrackingPlayer(player);
		this.bossInfo.addPlayer(player);
	}

	public void removeTrackingPlayer(EntityPlayerMP player) {
		super.removeTrackingPlayer(player);
		this.bossInfo.removePlayer(player);
	}

	@Override
	public String getLoreKey() {
		return "nostrum__dragon_red";
	}

	@Override
	public String getLoreDisplayName() {
		return "Red Dragons";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Red Dragons are greedy creatures. They often live in abandoned castles, and have a strong fondness to anything that's shiny.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Red Dragons are greedy creatures. They often live in abandoned castles, and have a strong fondness to anything that's shiny.", "According to some reports, Red Dragons are the only ones which are hatched from eggs.", "Nothing is known about what such eggs would look like.");
	}
	
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		this.entityDropItem(new ItemStack(DragonEggFragment.instance()), 0);
	}

}
