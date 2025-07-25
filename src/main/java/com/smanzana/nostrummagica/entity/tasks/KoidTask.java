package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.smanzana.nostrummagica.entity.KoidEntity;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class KoidTask extends Goal {
	
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
			EMagicElement element,
			int power,
			EAlteration alteration) {
		Spell spell = Spell.CreateAISpell(name);
		spell.addPart(new SpellEffectPart(element, power, alteration));
		
		if (map.get(element) == null)
			map.put(element, new LinkedList<>());
		map.get(element).add(spell);
	}
	
	private static void putSpell(Map<EMagicElement, List<Spell>> map,
			String name,
			SpellShape shape,
			EMagicElement element,
			int power,
			EAlteration alteration) {
		Spell spell = Spell.CreateAISpell(name);
		spell.addPart(new SpellShapePart(shape));
		spell.addPart(new SpellEffectPart(element, power, alteration));
		
		if (map.get(element) == null)
			map.put(element, new LinkedList<>());
		map.get(element).add(spell);
	}
	
	private static void putSpell(Map<EMagicElement, List<Spell>> map,
			String name,
			SpellShape shape,
			SpellShape shape2,
			EMagicElement element,
			int power,
			EAlteration alteration) {
		Spell spell = Spell.CreateAISpell(name);
		spell.addPart(new SpellShapePart(shape));
		spell.addPart(new SpellShapePart(shape2));
		spell.addPart(new SpellEffectPart(element, power, alteration));
		
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
					EMagicElement.PHYSICAL,
					1,
					EAlteration.SUMMON);
			putSpell(buffSpells, "Shield",
					EMagicElement.PHYSICAL,
					1,
					EAlteration.RESIST);
			putSpell(rangedSpells, "Weaken",
					NostrumSpellShapes.AI,
					EMagicElement.PHYSICAL,
					1,
					EAlteration.INFLICT);
			putSpell(rangedSpells, "Weaken II",
					NostrumSpellShapes.AI,
					EMagicElement.PHYSICAL,
					2,
					EAlteration.INFLICT);
			putSpell(rangedSpells, "Crush",
					NostrumSpellShapes.AI,
					EMagicElement.PHYSICAL,
					1,
					EAlteration.HARM);
			putSpell(meleeSpells, "Bone Crusher",
					NostrumSpellShapes.AI,
					EMagicElement.PHYSICAL,
					2,
					EAlteration.HARM);
			
			// Lightning
			putSpell(buffSpells, "Magic Shell",
					EMagicElement.LIGHTNING,
					1,
					EAlteration.RESIST);
			putSpell(rangedSpells, "Bolt",
					NostrumSpellShapes.AI,
					EMagicElement.LIGHTNING,
					1,
					null);
			putSpell(rangedSpells, "Shock",
					NostrumSpellShapes.AI,
					EMagicElement.LIGHTNING,
					1,
					EAlteration.INFLICT);
			putSpell(rangedSpells, "Lightning Ball I",
					NostrumSpellShapes.Projectile,
					NostrumSpellShapes.Chain,
					EMagicElement.LIGHTNING,
					1,
					EAlteration.HARM);
			putSpell(rangedSpells, "Lightning Ball II",
					NostrumSpellShapes.Projectile,
					EMagicElement.LIGHTNING,
					2,
					EAlteration.HARM);
			putSpell(meleeSpells, "Shocking Touch",
					NostrumSpellShapes.AI,
					EMagicElement.LIGHTNING,
					2,
					EAlteration.HARM);
			
			// Fire
			putSpell(rangedSpells, "Burn",
					NostrumSpellShapes.AI,
					EMagicElement.FIRE,
					1,
					null);
			spell = Spell.CreateAISpell("Fireball");
			spell.addPart(new SpellShapePart(NostrumSpellShapes.Projectile));
			spell.addPart(new SpellShapePart(NostrumSpellShapes.Burst, NostrumSpellShapes.Burst.makeProps(3f)));
			spell.addPart(new SpellEffectPart(EMagicElement.FIRE,
					1, null));
			rangedSpells.get(EMagicElement.FIRE).add(spell);
			putSpell(rangedSpells, "Flare",
					NostrumSpellShapes.Projectile,
					EMagicElement.FIRE,
					3,
					EAlteration.HARM);

			// Ice
			putSpell(buffSpells, "Magic Aegis",
					EMagicElement.ICE,
					2,
					EAlteration.SUPPORT);
			putSpell(rangedSpells, "Ice Shard",
					NostrumSpellShapes.Projectile,
					EMagicElement.ICE,
					1,
					EAlteration.HARM);
			spell = Spell.CreateAISpell("Group Frostbite");
			spell.addPart(new SpellShapePart(NostrumSpellShapes.Projectile));
			spell.addPart(new SpellShapePart(NostrumSpellShapes.Burst, NostrumSpellShapes.Burst.makeProps(5f)));
			spell.addPart(new SpellEffectPart(EMagicElement.ICE,
					1, EAlteration.INFLICT));
			rangedSpells.get(EMagicElement.ICE).add(spell);
			
			putSpell(meleeSpells, "Hand Of Cold",
					NostrumSpellShapes.AI,
					NostrumSpellShapes.Burst,
					EMagicElement.ICE,
					2,
					EAlteration.HARM);
			
			// Earth
			putSpell(buffSpells, "Earth Aegis",
					EMagicElement.EARTH,
					2,
					EAlteration.SUPPORT);
			putSpell(buffSpells, "Earth Aegis II",
					EMagicElement.EARTH,
					3,
					EAlteration.SUPPORT);
			putSpell(rangedSpells, "Roots",
					NostrumSpellShapes.AI,
					EMagicElement.EARTH,
					3,
					EAlteration.INFLICT);
			putSpell(rangedSpells, "Rock Fling",
					NostrumSpellShapes.Projectile,
					EMagicElement.EARTH,
					1,
					EAlteration.HARM);
			putSpell(meleeSpells, "Earth Bash",
					NostrumSpellShapes.AI,
					EMagicElement.EARTH,
					2,
					EAlteration.HARM);
			
			// Wind
			putSpell(buffSpells, "Gust",
					EMagicElement.WIND,
					2,
					EAlteration.RESIST);
			putSpell(buffSpells, "Haste",
					EMagicElement.WIND,
					1,
					EAlteration.SUPPORT);
			putSpell(rangedSpells, "Poison",
					NostrumSpellShapes.AI,
					EMagicElement.WIND,
					1,
					EAlteration.INFLICT);
			putSpell(rangedSpells, "Wind Slash",
					NostrumSpellShapes.AI,
					EMagicElement.WIND,
					1,
					EAlteration.HARM);
			putSpell(rangedSpells, "Wind Ball I",
					NostrumSpellShapes.Projectile,
					EMagicElement.WIND,
					2,
					EAlteration.HARM);
			putSpell(rangedSpells, "Wind Ball II",
					NostrumSpellShapes.Projectile,
					EMagicElement.WIND,
					3,
					EAlteration.HARM);
			
			// Ender
			putSpell(buffSpells, "Invisibility",
					EMagicElement.ENDER,
					2,
					EAlteration.RESIST);
			spell = Spell.CreateAISpell("Blinker");
			spell.addPart(new SpellShapePart(NostrumSpellShapes.OnDamage));
			spell.addPart(new SpellEffectPart(EMagicElement.ENDER,
					2, EAlteration.GROWTH));
			buffSpells.get(EMagicElement.ENDER).add(spell);
			putSpell(rangedSpells, "Blindness",
					NostrumSpellShapes.AI,
					EMagicElement.ENDER,
					1,
					EAlteration.INFLICT);
			putSpell(rangedSpells, "Random Teleport",
					NostrumSpellShapes.AI,
					EMagicElement.ENDER,
					1,
					null);
			putSpell(rangedSpells, "Random Teleport Self",
					EMagicElement.ENDER,
					2,
					null);
		}
	}
	
	private static final Random rand = new Random();
	private static final double RANGE_SQR = 225.0;
	
	private KoidEntity koid;
	
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
	
	public KoidTask(KoidEntity koid) {
		init();
		this.koid = koid;
		
		setupCombat();
	
		meleeCooldown = 0;
		rangeCooldown = 0;
		auxCooldown = 0;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	@Override
	public boolean canUse() {
		if (!koid.isAlive())
			return false;
		
		if (koid.getTarget() == null)
			return false;
		
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return running;
	}
	
	@Override
	public void tick() {
		running = run();
	}
	
	@Override
	public void start() {
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
		LivingEntity target = koid.getTarget();

		boolean done = false;
		boolean again = true;
		
		// First, we try to move.
		
		// Else don't execute task
		if (target != null && target.isAlive()) {
			if (!pathTo(target)) {
				return false;
			}
		}
		
		// Does not check done so we can do even when target is dead
		if (hasAux && auxCooldown <= 0 && koid.tickCount > 100) {
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

		if (target != null && target.isAlive()) {
			double distTarget = target.position().distanceTo(koid.position());
			
			double meleeRange = (double)(koid.getBbWidth() * 2.0F * koid.getBbWidth() * 2.0F + koid.getBbWidth());
			if (distTarget < meleeRange) {
				inMelee = true;
			}
			if (distTarget < RANGE_SQR) {
				inRange = true;
			}
			
			if (!done && !inMelee && hasRange && inRange && rangeCooldown <= 0) {
				// Can we do a ranged attack?
				if (koid.hasLineOfSight(target)) {
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
	
	private boolean pathTo(LivingEntity target) {
		if (target == null || !target.isAlive())
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
        	
        	if (updateCooldown > 0 && !koid.getNavigation().isDone())
    			return true;
        	
        	//if (Math.abs(koid.getPosY() - target.getPosY()) > 1) {
        	//	if (koid.getNavigator().)
        	//}
			
        	koid.getNavigation().stop();
			success = koid.getNavigation().moveTo(target, 1.0);
			if (success) {
				updateCooldown = 5;
			}
		} else if (hasRange) {
			
			success = true;
			double dist = koid.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());

            if (dist <= RANGE_SQR - 64.0 && koid.hasLineOfSight(target))
            {
            	koid.getNavigation().stop();
            	++this.strafeTime;
            }
            else
            {
                this.strafeTime = -1;
            	if (updateCooldown > 0)
            		updateCooldown--;
            	
            	if (updateCooldown > 0 && !koid.getNavigation().isDone())
        			return true;
            	
            	koid.getNavigation().stop();
            	koid.getNavigation().moveTo(target, 1.0);
                this.updateCooldown = 5;
            }

            if (this.strafeTime >= 20)
            {
                if ((double) koid.getRandom().nextFloat() < 0.3D)
                {
                    this.strafeClockwise = !this.strafeClockwise;
                }

                if ((double) koid.getRandom().nextFloat() < 0.3D)
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
                
                if (koid.isOnGround()) {
                	Vec3 forward = getForward(koid);
                	Vec3 right = getForward(koid).xRot(90.0f);
                	double mag = strafeBack ? -.1 : 0;
                	double x, y, z;
                	x = forward.x * mag;
                	y = forward.y * mag;
                	z = forward.z * mag;
                	mag = strafeClockwise ? -.3 : .3;
                	x += right.x * mag;
                	y += right.y * mag;
                	z += right.z * mag;
                	y += .8;
                	koid.push(x, y, z);
                	koid.setOnGround(false);
                }
                koid.lookAt(target, 30.0F, 30.0F);
                
            }
            else
            {
            	koid.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
			
		}
		return success;
		
	}
	
	private void setupCombat() {
		int count = rand.nextInt(3) + 2;
		int attempts = 10;
		
		while (count > 0 && attempts > 0) {
			attempts--;
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
	
	private static Vec2f getPitchYaw(LivingEntity entity){
		Vec2f vec2f = new Vec2f(entity.getXRot(), entity.getYRot());
		return vec2f;
	}

	private static Vec3 getForward(LivingEntity entity) {
		return fromPitchYawVector(getPitchYaw(entity));
	}
	
	public static Vec3 fromPitchYawVector(Vec2f vector) {
		return fromPitchYaw(vector.pitch, vector.yaw);
	}
	
	public static Vec3 fromPitchYaw(float p_189986_0_, float p_189986_1_)
    {
        float f = Mth.cos(-p_189986_1_ * 0.017453292F - (float)Math.PI);
        float f1 = Mth.sin(-p_189986_1_ * 0.017453292F - (float)Math.PI);
        float f2 = -Mth.cos(-p_189986_0_ * 0.017453292F);
        float f3 = Mth.sin(-p_189986_0_ * 0.017453292F);
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
    }
	
}
