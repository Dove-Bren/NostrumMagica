package com.smanzana.nostrummagica.spells;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Collection of triggers and shapes.
 * @author Skyler
 *
 */
public class Spell {
	
	public static class SpellPartParam {
		 public float level;
		 public boolean flip;
		 
		 public SpellPartParam(float level, boolean flip) {
			 this.level = level;
			 this.flip = flip;
		 }
	}
	
	public class SpellState {
		private int index;
		private EntityLivingBase caster;
		private EntityLivingBase self;
		private EntityLivingBase other;
		private SpellTrigger.SpellTriggerInstance triggerInstance;
		
		public SpellState(EntityLivingBase caster) {
			index = -1;
			this.caster = this.self = this.other = caster;
		}
		
		/**
		 * Callback given to spawned triggers.
		 * Indicates the current trigger has been done and the spell
		 * should move forward
		 */
		public void trigger(List<EntityLivingBase> targets, List<EntityLivingBase> other, World world, List<BlockPos> locations) {
			
			//for each target/other pair (if more than one), break into multiple spellstates
			// persist index++ and set self/other, then start doing shapes or next trigger
			
			SpellPart next = null;
			
			List<EntityLivingBase> targs = targets;
			
			while ((next = (++index < parts.size() ? parts.get(index) : null)) != null && !next.isTrigger()) {
				// it's a shape. Do it idk
				SpellShape shape = next.getShape();
				SpellAction action = solveAction(caster, next.getAlteration(),
						next.getElement(), next.getElementCount());
				SpellPartParam param = next.getParam();
				
				if (param.flip) {
					// use other instead of self
					targs = other;
				} else {
					targs = targets;
				}
				
				if (targs != null && !targs.isEmpty()) {
					for (EntityLivingBase targ : targs) {
						shape.perform(action, param, targ, null, null);
					}
				} else {
					// use locations
					for (BlockPos pos : locations) {
						shape.perform(action, param, null, world, pos);
					}
				}
			}
			
			// next is either null or a trigger
			if (next == null) {
				// end of spell
			} else {
				// If we have more than one target/pos we hit, split here so each
				// can proceed at their own pace
				
				// targs is correct (targets or others) based on last spell shape (or
				// just targets if no previous shape)
				
				if (targs != null && !targs.isEmpty()) {
					if (targs.size() == 1) {
						// don't need to split
						// Also base case after a split happens
						spawnTrigger(next.getTrigger(), targs.get(0), null, null);
					} else {
						index--; // Make splits have same trigger as we're performing now
						for (EntityLivingBase targ : targs) {
							SpellState sub = split(targ, this.getSelf());
							sub.trigger(Lists.newArrayList(targ), Lists.newArrayList(targ),
									world, null);
						}
					}
				} else {
					if (locations.size() == 1) {
						// Base case here, too. Instantiate trigger!!!!
						spawnTrigger(next.getTrigger(), null, world, locations.get(0));
					} else {
						index--; // Make splits have same trigger as we're performing now
						for (BlockPos targ : locations) {
							SpellState sub = split(this.getSelf(), this.getSelf());
							sub.trigger(null, null,
									world, Lists.newArrayList(targ));
						}
					}
				}
								
			}
		}
		
		private void spawnTrigger(SpellTrigger trigger, EntityLivingBase targ, World world, BlockPos targpos) {
			// instantiate trigger in world
			Vec3d pos;
			if (world == null)
				world = targ.worldObj;
			if (targ == null)
				pos = new Vec3d(targpos.getX(), targpos.getY(), targpos.getZ());
			else
				pos = targ.getPositionVector();
			
			this.triggerInstance = trigger.instance(this, world, pos,
					(targ == null ? -90.0f : targ.rotationPitch),
					(targ == null ? 0.0f : targ.rotationYaw));
			this.triggerInstance.init(caster);
		}
		
		private SpellState split(EntityLivingBase self, EntityLivingBase other) {
			SpellState spawn = new SpellState(caster);
			spawn.index = this.index;
			spawn.self = self;
			spawn.other = other;
			
			return spawn;
		}

		public EntityLivingBase getSelf() {
			return self;
		}

		public EntityLivingBase getOther() {
			return other;
		}

		public EntityLivingBase getCaster() {
			return caster;
		}
	}
	
	public static class SpellPart {
		private SpellTrigger trigger;
		private SpellShape shape;
		private EAlteration alteration;
		private EMagicElement element;
		private int elementCount;
		private SpellPartParam param;
		
		public SpellPart(SpellTrigger trigger, SpellPartParam param) {
			this.trigger = trigger;
			this.param = param;
		}
		
		public SpellPart(SpellShape shape, EMagicElement element, int count, EAlteration alt,
				SpellPartParam param) {
			this.shape = shape;
			this.element = element;
			this.elementCount = count;
			this.alteration = alt;
			this.param = param;
		}
		
		public boolean isTrigger() {
			return trigger != null;
		}

		public SpellTrigger getTrigger() {
			return trigger;
		}

		public SpellShape getShape() {
			return shape;
		}

		public EAlteration getAlteration() {
			return alteration;
		}

		public EMagicElement getElement() {
			return element;
		}

		public int getElementCount() {
			return elementCount;
		}

		public SpellPartParam getParam() {
			return param;
		}
	}

	private String name;
	private List<SpellPart> parts;
	private int manaCost;
	
	// TODO some cool effects and stuff
	
	public Spell(String name) {
		this.name = name;
		this.parts = new LinkedList<>();
		manaCost = -1; // un-calculated value
	}
	
	public void addPart(SpellPart part) {
		this.parts.add(part);
		manaCost = -1;
	}
	
	public String getName() {
		return name;
	}
	
	public void cast(EntityLivingBase caster) {
		SpellState state = new SpellState(caster);
		state.trigger(Lists.newArrayList(caster), null, null, null);
	}
	
	public String crc() {
		String s = "";
		for (SpellPart part : parts) {
			if (part.isTrigger())
				s += part.getTrigger().getTriggerKey();
			else
				s += part.getShape().getShapeKey();
		}
		
		return s;
	}
	
	public int getManaCost() {
		if (manaCost != -1)
			return manaCost;
		
		// Triggers can report their  cost
		// Alterations are in enum
		// Shapes cost 15
		// First elem is free. Extra costs 30 ea
		manaCost = 0;
		
		for (SpellPart part : parts) {
			if (part.isTrigger())
				manaCost += part.getTrigger().getManaCost();
			else {
				manaCost += 15;
				if (part.getElementCount() > 1)
					manaCost += (30 * (part.getElementCount() - 1));
				if (part.getAlteration() != null)
					manaCost += part.getAlteration().getCost();
			}
		}
		
		return manaCost;
	}
	
	
	public static final SpellAction solveAction(EntityLivingBase caster, EAlteration alteration,
			EMagicElement element, int elementCount) {
		
		// Could do a registry with hooks here, if wanted it to be extensible
		
		if (alteration == null) {
			// Damage spell
			return new SpellAction(caster).damage(element, 5.0f * elementCount);
		}
		
		switch (alteration) {
		case ALTER:
			return solveAlter(caster, element, elementCount);
		case CONJURE:
			return solveConjure(caster, element, elementCount);
		case ENCHANT:
			return solveEnchant(caster, element, elementCount);
		case GROWTH:
			return solveGrowth(caster, element, elementCount);
		case INFLICT:
			return solveInflict(caster, element, elementCount);
		case RESIST:
			return solveResist(caster, element, elementCount);
		case SUMMON:
			return solveSummon(caster, element, elementCount);
		case SUPPORT:
			return solveSupport(caster, element, elementCount);
		default:
			return null;
		}
	}
	
	private static final SpellAction solveAlter(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).transmute(elementCount);
		case EARTH:
			break;
		case ENDER:
			break;
		case FIRE:
			break;
		case ICE:
			break;
		case LIGHTNING:
			break;
		case WIND:
			break;
		}
		
		return null;
	}
	
	private static final SpellAction solveInflict(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("weakness"), duration, amp);
		case EARTH:
			return new SpellAction(caster).status(RootedPotion.instance(), duration, amp);
		case ENDER:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("blindness"), duration, amp);
		case FIRE:
			return new SpellAction(caster).burn(duration);
		case ICE:
			break; // TODO
		case LIGHTNING:
			break;
		case WIND:
			break;
		}
		
		return null;
	}
	
	private static final SpellAction solveResist(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("resistance"), duration, amp);
		case EARTH:
			break;
		case ENDER:
			break;
		case FIRE:
			break;
		case ICE:
			break; // TODO
		case LIGHTNING:
			break;
		case WIND:
			break;
		}
		
		return null;
	}
	
	private static final SpellAction solveSupport(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("absorption"), duration * 5, amp);
		case EARTH:
			break;
		case ENDER:
			break;
		case FIRE:
			break;
		case ICE:
			break; // TODO
		case LIGHTNING:
			break;
		case WIND:
			break;
		}
		
		return null;
	}
	
	private static final SpellAction solveGrowth(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("saturation"), 1, 4 * elementCount);
		case EARTH:
			break;
		case ENDER:
			break;
		case FIRE:
			break;
		case ICE:
			break; // TODO
		case LIGHTNING:
			break;
		case WIND:
			break;
		}
		
		return null;
	}
	
	private static final SpellAction solveEnchant(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			break;
		case EARTH:
			break;
		case ENDER:
			break;
		case FIRE:
			break;
		case ICE:
			break; // TODO
		case LIGHTNING:
			break;
		case WIND:
			break;
		}
		
		return null;
	}
	
	private static final SpellAction solveConjure(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			break;
		case EARTH:
			break;
		case ENDER:
			break;
		case FIRE:
			break;
		case ICE:
			break; // TODO
		case LIGHTNING:
			break;
		case WIND:
			break;
		}
		
		return null;
	}
	
	private static final SpellAction solveSummon(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			break;
		case EARTH:
			break;
		case ENDER:
			break;
		case FIRE:
			break;
		case ICE:
			break; // TODO
		case LIGHTNING:
			break;
		case WIND:
			break;
		}
		
		return null;
	}
	
	
	
}
