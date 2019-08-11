package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.entity.tasks.DragonAIAggroTable;
import com.smanzana.nostrummagica.entity.tasks.DragonAINearestAttackableTarget;
import com.smanzana.nostrummagica.entity.tasks.DragonFlyEvasionTask;
import com.smanzana.nostrummagica.entity.tasks.DragonFlyRandomTask;
import com.smanzana.nostrummagica.entity.tasks.DragonLandTask;
import com.smanzana.nostrummagica.entity.tasks.DragonMeleeAttackTask;
import com.smanzana.nostrummagica.entity.tasks.DragonSpellAttackTask;
import com.smanzana.nostrummagica.entity.tasks.DragonSummonShadowAttack;
import com.smanzana.nostrummagica.entity.tasks.DragonTakeoffLandTask;
import com.smanzana.nostrummagica.items.DragonEggFragment;
import com.smanzana.nostrummagica.items.NostrumSkillItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem.SkillItemType;
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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIWander;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityDragonRed extends EntityDragonRedBase {

	private static enum DragonPhase {
		GROUNDED_PHASE,
		FLYING_PHASE,
		RAMPAGE_PHASE,
	}
	
	private static final DataParameter<Integer> DRAGON_PHASE =
			EntityDataManager.<Integer>createKey(EntityDragonRed.class, DataSerializers.VARINT);
	
	private static final String DRAGON_SERIAL_PHASE_TOK = "DragonPhase";

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
	
	// AI. First array is indexed by the phase. Second is just a collection of tasks.
	private EntityAIBase[][] flyingAI;
	private EntityAIBase[][] groundedAI;
	
	private EntityAIBase[] lastAI;
	
	private DragonSummonShadowAttack<EntityDragonRed> shadowAttack;
	private DragonFlyEvasionTask evasionTask;
	private DragonAIAggroTable<EntityDragonRed, EntityLivingBase> aggroTable;
	
	public EntityDragonRed(World worldIn) {
		super(worldIn);
        this.setSize(6F, 4.6F);
        this.stepHeight = 2;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
        this.experienceValue = 1000;
        this.noClip = false;
	}
	
	private DragonPhase getPhase() {
		return DragonPhase.values()[this.dataManager.get(DRAGON_PHASE).intValue()];
	}
	
	private void setPhase(DragonPhase phase) {
		this.dataManager.set(DRAGON_PHASE, phase.ordinal());
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
		super.notifyDataManagerChange(key);
		if (key == DRAGON_PHASE) {
			onPhaseChange();
		}
	}
	
	private void initBaseAI() {
		
		initSpells();
        shadowAttack = new DragonSummonShadowAttack<EntityDragonRed>(this, 5 * 60, 10);
        evasionTask = new DragonFlyEvasionTask(this, 1.0D);
        aggroTable = new DragonAIAggroTable<>(this, true);
		
		// Order on these are priority numbers!
		flyingAI = new EntityAIBase[][] {
			// PHASE_GROUNDED
			new EntityAIBase[] {},
			new EntityAIBase[] {
				shadowAttack,
        		new DragonLandTask(this),
        		new DragonMeleeAttackTask(this, 1.0D, true),
        		evasionTask,
        		new DragonSpellAttackTask<EntityDragonRed>(this, (5 * 5), 10, true, DSPELL_Fireball),
        		//new DragonFlyStrafeTask<EntityDragonRed>(this, 20),
        		new DragonTakeoffLandTask(this),
        		new DragonFlyRandomTask(this),
			},
			new EntityAIBase[] {
				shadowAttack,
				new DragonLandTask(this),
        		new DragonMeleeAttackTask(this, 1.0D, true),
        		new DragonTakeoffLandTask(this),
        		evasionTask,
				new DragonSpellAttackTask<EntityDragonRed>(this, (5 * 5), 12, true, DSPELL_Fireball2),
				new DragonSpellAttackTask<EntityDragonRed>(this, (5 * 12), 10, false, DSPELL_Speed, DSPELL_Shield),
				new DragonSpellAttackTask<EntityDragonRed>(this, (5 * 10), 20, false, DSPELL_Weaken),
				new DragonSpellAttackTask<EntityDragonRed>(this, (5 * 45), 20, true, DSPELL_Curse),
        		//new DragonFlyStrafeTask<EntityDragonRed>(this, 20),
        		new DragonFlyRandomTask(this),
			}
        };
        groundedAI = new EntityAIBase[][] {
        	new EntityAIBase[] {
    			shadowAttack,
        		new DragonMeleeAttackTask(this, 1.0D, true),
        		new EntityAIWander(this, 1.0D, 30)
        	},
        	new EntityAIBase[] {
    			shadowAttack,
        		new DragonSpellAttackTask<EntityDragonRed>(this, (5 * 5), 20, true, DSPELL_Fireball),
        		new DragonTakeoffLandTask(this),
    			new DragonMeleeAttackTask(this, 1.0D, true),
        		new EntityAIWander(this, 1.0D, 30)
        	},
        	new EntityAIBase[] {
    			shadowAttack,
        		new DragonTakeoffLandTask(this),
    			new DragonMeleeAttackTask(this, 1.0D, true),
    			new DragonSpellAttackTask<EntityDragonRed>(this, (5 * 5), 12, true, DSPELL_Fireball2),
				new DragonSpellAttackTask<EntityDragonRed>(this, (5 * 12), 10, false, DSPELL_Speed, DSPELL_Shield),
				new DragonSpellAttackTask<EntityDragonRed>(this, (5 * 10), 20, false, DSPELL_Weaken),
				new DragonSpellAttackTask<EntityDragonRed>(this, (5 * 45), 20, true, DSPELL_Curse),
        		new EntityAIWander(this, 1.0D, 30)
        	}
        		
        };
		
//      this.tasks.addTask(1, new EntityAISwimming(this));
//		this.tasks.addTask(4, new EntityAIWander(this, 1.0D, 30));
        this.targetTasks.addTask(1, aggroTable);
        this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false, new Class[0]));
		this.targetTasks.addTask(3, new DragonAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
		this.targetTasks.addTask(4, new DragonAINearestAttackableTarget<EntityZombie>(this, EntityZombie.class, true));
		this.targetTasks.addTask(5, new DragonAINearestAttackableTarget<EntitySheep>(this, EntitySheep.class, true));
		this.targetTasks.addTask(6, new DragonAINearestAttackableTarget<EntityCow>(this, EntityCow.class, true));
		this.targetTasks.addTask(7, new DragonAINearestAttackableTarget<EntityPig>(this, EntityPig.class, true));
		this.targetTasks.addTask(8, new DragonAINearestAttackableTarget<EntityVillager>(this, EntityVillager.class, true));
		this.targetTasks.addTask(9, new DragonAINearestAttackableTarget<EntityHorse>(this, EntityHorse.class, true));
		this.targetTasks.addTask(10, new DragonAINearestAttackableTarget<EntityGiantZombie>(this, EntityGiantZombie.class, true));
		this.targetTasks.addTask(11, new DragonAINearestAttackableTarget<EntityPolarBear>(this, EntityPolarBear.class, true));
	}
	
	@Override
	protected void setFlyingAI() {
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
	
	@Override
	protected void setGroundedAI() {
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
		super.initEntityAI();
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
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1000.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(15.0D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64D);
    }
	
	@Override
	protected void entityInit() {
		super.entityInit();
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
		
		if (!this.worldObj.isRemote) {
			this.initEntityAI();
		}
	}
	
	public void writeEntityToNBT(NBTTagCompound compound) {
    	super.writeEntityToNBT(compound);
    	compound.setByte(DRAGON_SERIAL_PHASE_TOK, (byte)this.getPhase().ordinal());
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
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
		
		int count = this.getRNG().nextInt(2 + lootingModifier);
		if (count != 0) {
			this.entityDropItem(NostrumSkillItem.getItem(SkillItemType.WING, count), 0);
		}
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (!this.worldObj.isRemote && source.getSourceOfDamage() != null) {
			Entity ent = source.getSourceOfDamage();
			if (ent instanceof EntityLivingBase && ent != this) {
				this.shadowAttack.addToPool((EntityLivingBase) ent);
				this.aggroTable.addDamage((EntityLivingBase) ent, amount);
			}
			
			this.evasionTask.reset();
		}
		
		return super.attackEntityFrom(source, amount);
	}
	
	@Override
	public void bite(EntityLivingBase target) {
		super.bite(target);
		
		if (!this.worldObj.isRemote) {
			this.evasionTask.reset();
		}
	}

}
