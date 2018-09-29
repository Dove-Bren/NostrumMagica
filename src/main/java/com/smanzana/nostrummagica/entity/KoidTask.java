package com.smanzana.nostrummagica.entity;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.ChainShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DamagedTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class KoidTask extends EntityAIBase {
	
	public static final class Vec2f {
		public float pitch;
		public float yaw;
		
		public Vec2f(float pitch, float yaw) {
			this.pitch = pitch;
			this.yaw = yaw;
		}
	}

	private static Map<EMagicElement, List<Spell>> rangedSpells;
	private static Map<EMagicElement, List<Spell>> meleeSpells;
	private static Map<EMagicElement, List<Spell>> buffSpells;
	
	private static void putSpell(Map<EMagicElement, List<Spell>> map,
			String name,
			SpellTrigger trigger,
			SpellShape shape,
			EMagicElement element,
			int power,
			EAlteration alteration) {
		Spell spell = new Spell(name, true);
		spell.addPart(new SpellPart(trigger));
		spell.addPart(new SpellPart(shape, element, power, alteration));
		
		if (map.get(element) == null)
			map.put(element, new LinkedList<>());
		map.get(element).add(spell);
	}
	
	private static void init() {
		if (rangedSpells == null) {
			rangedSpells = new EnumMap<>(EMagicElement.class);
			meleeSpells = new EnumMap<>(EMagicElement.class);
			buffSpells = new EnumMap<>(EMagicElement.class);
			
			Spell spell;
			
			// Physical
			putSpell(buffSpells, "Summon Golem",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					1,
					EAlteration.SUMMON);
			putSpell(buffSpells, "Shield",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					1,
					EAlteration.RESIST);
			putSpell(rangedSpells, "Weaken",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					1,
					EAlteration.INFLICT);
			putSpell(rangedSpells, "Weaken II",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					2,
					EAlteration.INFLICT);
			putSpell(rangedSpells, "Crush",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					1,
					null);
			putSpell(meleeSpells, "Bone Crusher",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.PHYSICAL,
					2,
					null);
			
			// Lightning
			putSpell(buffSpells, "Magic Shell",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					EAlteration.RESIST);
			putSpell(rangedSpells, "Bolt",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					EAlteration.CONJURE);
			putSpell(rangedSpells, "Shock",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					EAlteration.INFLICT);
			putSpell(rangedSpells, "Lightning Ball I",
					ProjectileTrigger.instance(),
					ChainShape.instance(),
					EMagicElement.LIGHTNING,
					1,
					null);
			putSpell(rangedSpells, "Lightning Ball II",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.LIGHTNING,
					2,
					null);
			putSpell(meleeSpells, "Shocking Touch",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.LIGHTNING,
					2,
					null);
			
			// Fire
			putSpell(rangedSpells, "Burn",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.FIRE,
					1,
					null);
			spell = new Spell("Fireball", true);
			spell.addPart(new SpellPart(ProjectileTrigger.instance()));
			spell.addPart(new SpellPart(AoEShape.instance(), EMagicElement.FIRE,
					1, null, new SpellPartParam(3, false)));
			rangedSpells.get(EMagicElement.FIRE).add(spell);
			putSpell(rangedSpells, "Flare",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.FIRE,
					3,
					null);

			// Ice
			putSpell(buffSpells, "Magic Aegis",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ICE,
					2,
					EAlteration.SUPPORT);
			putSpell(rangedSpells, "Ice Shard",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ICE,
					1,
					null);
			spell = new Spell("Group Frostbite", true);
			spell.addPart(new SpellPart(ProjectileTrigger.instance()));
			spell.addPart(new SpellPart(AoEShape.instance(), EMagicElement.ICE,
					1, EAlteration.INFLICT, new SpellPartParam(5, false)));
			rangedSpells.get(EMagicElement.ICE).add(spell);
			
			putSpell(meleeSpells, "Hand Of Cold",
					AITargetTrigger.instance(),
					AoEShape.instance(),
					EMagicElement.ICE,
					2,
					null);
			
			// Earth
			putSpell(buffSpells, "Earth Aegis",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					2,
					EAlteration.SUPPORT);
			putSpell(buffSpells, "Earth Aegis II",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					3,
					EAlteration.SUPPORT);
			putSpell(rangedSpells, "Roots",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					3,
					EAlteration.INFLICT);
			putSpell(rangedSpells, "Rock Fling",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					1,
					null);
			putSpell(meleeSpells, "Earth Bash",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.EARTH,
					2,
					null);
			
			// Wind
			putSpell(buffSpells, "Gust",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					2,
					EAlteration.RESIST);
			putSpell(buffSpells, "Haste",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					1,
					EAlteration.SUPPORT);
			putSpell(rangedSpells, "Poison",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					1,
					EAlteration.INFLICT);
			putSpell(rangedSpells, "Wind Slash",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					1,
					null);
			putSpell(rangedSpells, "Wind Ball I",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					2,
					null);
			putSpell(rangedSpells, "Wind Ball II",
					ProjectileTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.WIND,
					3,
					null);
			
			// Ender
			putSpell(buffSpells, "Invisibility",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ENDER,
					2,
					EAlteration.RESIST);
			spell = new Spell("Blinker", true);
			spell.addPart(new SpellPart(SelfTrigger.instance()));
			spell.addPart(new SpellPart(DamagedTrigger.instance()));
			spell.addPart(new SpellPart(SingleShape.instance(), EMagicElement.ENDER,
					2, EAlteration.GROWTH));
			buffSpells.get(EMagicElement.ENDER).add(spell);
			putSpell(rangedSpells, "Blindness",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ENDER,
					1,
					EAlteration.INFLICT);
			putSpell(rangedSpells, "Random Teleport",
					AITargetTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ENDER,
					1,
					EAlteration.CONJURE);
			putSpell(rangedSpells, "Random Teleport Self",
					SelfTrigger.instance(),
					SingleShape.instance(),
					EMagicElement.ENDER,
					2,
					EAlteration.CONJURE);
		}
	}
	
	private static final Random rand = new Random();
	private static final double RANGE_SQR = 225.0;
	
	private EntityKoid koid;
	
	private boolean hasMelee;
	private boolean hasRange;
	private boolean hasAux;
	private List<Spell> meleeSkills;
	private List<Spell> buffSkills;
	private List<Spell> rangedSkills;
	
	private int meleeCooldown;
	private int rangeCooldown;
	private int auxCooldown;

	private boolean running;
	private int updateCooldown;
	private int strafeTime;
	private boolean strafeClockwise;
	private boolean strafeBack;
	
	public KoidTask(EntityKoid koid) {
		init();
		this.koid = koid;
		
		setupCombat();
	
		meleeCooldown = 0;
		rangeCooldown = 0;
		auxCooldown = 0;
		this.setMutexBits(3);
	}
	
	@Override
	public boolean shouldExecute() {
		if (koid.isDead)
			return false;
		
		if (koid.getAttackTarget() == null)
			return false;
		
		return true;
	}

	@Override
	public boolean continueExecuting() {
		return running;
	}
	
	@Override
	public void updateTask() {
		running = run();
	}
	
	@Override
	public void startExecuting() {
		meleeCooldown = 0;
		rangeCooldown = 0;
		strafeTime = 0;
		running = run();
	}
	
	private boolean run() {
		/**
		 * Do we not have a target?
		 *   - Do none of the below
		 *   
		 * Are we melee?
		 *   - Move towards melee range of target
		 *   - If within melee range & cooldown is expired, attack
		 *     - If within, don't continue
		 * Are we range?
		 *   - If not melee, move in range
		 *   - if in range and cooldown is expired, do range attack
		 */
		if (meleeCooldown > 0)
			meleeCooldown--;
		if (rangeCooldown > 0)
			rangeCooldown--;
		if (auxCooldown > 0)
			auxCooldown--;
		
		if (!hasMelee && !hasRange && !hasAux) {
			return false;
		}
		
		boolean inMelee = false;
		boolean inRange = false;
		EntityLivingBase target = koid.getAttackTarget();

		boolean done = false;
		boolean again = true;
		
		// First, we try to move.
		
		// Else don't execute task
		if (target != null && !target.isDead) {
			if (!pathTo(target)) {
				return false;
			}
		}
		
		// Does not check done so we can do even when target is dead
		if (hasAux && auxCooldown <= 0 && koid.ticksExisted > 100) {
			// Can do aux skill if not in melee
			if (!inMelee) {
				// Figure out who to do it to.
				// Usually do ourselves, but have a chance to aid master first
				
				Spell buff = this.getBuff();
				buff.cast(koid, 1.0f);
				auxCooldown = 20 * 5 * (1 + KoidTask.rand.nextInt(3));
				done = true;
			}
		}

		if (target != null && !target.isDead) {
			double distTarget = target.getPositionVector().distanceTo(koid.getPositionVector());
			
			double meleeRange = (double)(koid.width * 2.0F * koid.width * 2.0F + koid.width);
			if (distTarget < meleeRange) {
				inMelee = true;
			}
			if (distTarget < RANGE_SQR) {
				inRange = true;
			}
			
			if (!done && !inMelee && hasRange && inRange && rangeCooldown <= 0) {
				// Can we do a ranged attack?
				if (koid.canEntityBeSeen(target)) {
					Spell spell = this.getRanged();
					spell.cast(koid, 1.0f);
					rangeCooldown = 20 * 3 * (1 + KoidTask.rand.nextInt(3));
					done = true;
				}
			}
			
			if (!done && hasMelee && inMelee && meleeCooldown <= 0) {
				Spell spell = this.getMelee();
				spell.cast(koid, 1.0f);
				meleeCooldown = 20 * 1;
				done = true;
			}
		}
		
		return again;
	}
	
	private boolean pathTo(EntityLivingBase target) {
		if (target == null || target.isDead)
			return false;
		
		// If we're melee and !inMelee, move
		// Else if we're range and inMelee, move
		// Else if we're range and !inRange, move
		// Else if we're !melee && !range, move to owner
		// Else don't execute task
		boolean success = false;
		if (hasMelee) {
			if (updateCooldown > 0)
        		updateCooldown--;
        	
        	if (updateCooldown > 0 && !koid.getNavigator().noPath())
    			return true;
        	
        	//if (Math.abs(koid.posY - target.posY) > 1) {
        	//	if (koid.getNavigator().)
        	//}
			
        	koid.getNavigator().clearPathEntity();
			success = koid.getNavigator().tryMoveToEntityLiving(target, 1.0);
			if (success) {
				updateCooldown = 5;
			}
		} else if (hasRange) {
			
			success = true;
			double dist = koid.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);

            if (dist <= RANGE_SQR - 64.0 && koid.canEntityBeSeen(target))
            {
            	koid.getNavigator().clearPathEntity();
            	++this.strafeTime;
            }
            else
            {
                this.strafeTime = -1;
            	if (updateCooldown > 0)
            		updateCooldown--;
            	
            	if (updateCooldown > 0 && !koid.getNavigator().noPath())
        			return true;
            	
            	koid.getNavigator().clearPathEntity();
            	koid.getNavigator().tryMoveToEntityLiving(target, 1.0);
                this.updateCooldown = 5;
            }

            if (this.strafeTime >= 20)
            {
                if ((double) koid.getRNG().nextFloat() < 0.3D)
                {
                    this.strafeClockwise = !this.strafeClockwise;
                }

                if ((double) koid.getRNG().nextFloat() < 0.3D)
                {
                    this.strafeBack = !this.strafeBack;
                }

                this.strafeTime = 0;
            }

            if (this.strafeTime > -1)
            {
                if (dist > (double)(RANGE_SQR * 0.75F))
                {
                    this.strafeBack = false;
                }
                else if (dist < (double)(RANGE_SQR * 0.25F))
                {
                    this.strafeBack = true;
                }
                
                if (koid.onGround) {
                	Vec3d forward = getForward(koid);
                	Vec3d right = getForward(koid).rotatePitch(90.0f);
                	double mag = strafeBack ? -.1 : 0;
                	double x, y, z;
                	x = forward.xCoord * mag;
                	y = forward.yCoord * mag;
                	z = forward.zCoord * mag;
                	mag = strafeClockwise ? -.3 : .3;
                	x += right.xCoord * mag;
                	y += right.yCoord * mag;
                	z += right.zCoord * mag;
                	y += .8;
                	koid.addVelocity(x, y, z);
                	koid.onGround = false;
                }
                koid.faceEntity(target, 30.0F, 30.0F);
                
            }
            else
            {
            	koid.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            }
			
		}
		return success;
		
	}
	
	private void setupCombat() {
		int count = rand.nextInt(3) + 2;
		
		while (count > 0) {
			int type = rand.nextInt(3);
			if (type-- == 0) {
				// Melee, if it's there
				List<Spell> list = meleeSpells.get(koid.getElement());
				if (list != null) {
					Spell spell = list.get(rand.nextInt(list.size()));
					if (!this.hasMelee) {
						this.hasMelee = true;
						this.meleeSkills = new LinkedList<>();
					}
					this.meleeSkills.add(spell);
					count--;
				}
				
				continue;
			} else if (type-- == 0) {
				// Ranged, if it's there
				List<Spell> list = rangedSpells.get(koid.getElement());
				if (list != null) {
					Spell spell = list.get(rand.nextInt(list.size()));
					if (!this.hasRange) {
						this.hasRange = true;
						this.rangedSkills = new LinkedList<>();
					}
					this.rangedSkills.add(spell);
					count--;
				}
				
				continue;
			} else {
				// Buff, if it's there
				List<Spell> list = buffSpells.get(koid.getElement());
				if (list != null) {
					Spell spell = list.get(rand.nextInt(list.size()));
					if (!this.hasAux) {
						this.hasAux = true;
						this.buffSkills = new LinkedList<>();
					}
					this.buffSkills.add(spell);
					count--;
				}
				
				continue;
			}
			
		}
	}
	
	private Spell getMelee() {
		if (!hasMelee)
			return null;
		
		return meleeSkills.get(rand.nextInt(meleeSkills.size()));
	}
	
	private Spell getRanged() {
		if (!hasRange)
			return null;
		
		return rangedSkills.get(rand.nextInt(rangedSkills.size()));
	}
	
	private Spell getBuff() {
		if (!hasAux)
			return null;
		
		return buffSkills.get(rand.nextInt(buffSkills.size()));
	}
	
	private static Vec2f getPitchYaw(EntityLivingBase entity){
		Vec2f vec2f = new Vec2f(entity.rotationPitch, entity.rotationYaw);
		return vec2f;
	}

	private static Vec3d getForward(EntityLivingBase entity) {
		return fromPitchYawVector(getPitchYaw(entity));
	}
	
	public static Vec3d fromPitchYawVector(Vec2f vector) {
		return fromPitchYaw(vector.pitch, vector.yaw);
	}
	
	public static Vec3d fromPitchYaw(float p_189986_0_, float p_189986_1_)
    {
        float f = MathHelper.cos(-p_189986_1_ * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-p_189986_1_ * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-p_189986_0_ * 0.017453292F);
        float f3 = MathHelper.sin(-p_189986_0_ * 0.017453292F);
        return new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }
	
}
