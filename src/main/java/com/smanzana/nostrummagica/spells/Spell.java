package com.smanzana.nostrummagica.spells;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.EntityLiving;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Collection of triggers and shapes.
 * @author Skyler
 *
 */
public class Spell {
	
	public class SpellPartParam {
		 public float level;
		 public boolean flip;
		 
		 public SpellPartParam(float level, boolean flip) {
			 this.level = level;
			 this.flip = flip;
		 }
	}
	
	public class SpellState {
		private int index;
		private EntityLiving caster;
		private EntityLiving self;
		private EntityLiving other;
		private SpellTrigger.SpellTriggerInstance triggerInstance;
		
		public SpellState(EntityLiving caster) {
			index = -1;
			this.caster = this.self = this.other = caster;
		}
		
		/**
		 * Callback given to spawned triggers.
		 * Indicates the current trigger has been done and the spell
		 * should move forward
		 */
		public void trigger(List<EntityLiving> targets, List<EntityLiving> other, World world, List<BlockPos> locations) {
			
			//for each target/other pair (if more than one), break into multiple spellstates
			// persist index++ and set self/other, then start doing shapes or next trigger
			
			SpellPart next;
			
			List<EntityLiving> targs = targets;
			
			while ((next = parts.get(++index)) != null && !next.isTrigger()) {
				// it's a shape. Do it idk
				SpellShape shape = next.getShape();
				SpellAction action;
				SpellPartParam param = next.getParam();
				
				
				if (param.flip) {
					// use other instead of self
					targs = other;
				} else {
					targs = targets;
				}
				
				if (targs != null && !targs.isEmpty()) {
					for (EntityLiving targ : targs) {
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
						for (EntityLiving targ : targs) {
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
		
		private void spawnTrigger(SpellTrigger trigger, EntityLiving targ, World world, BlockPos targpos) {
			// instantiate trigger in world
			Vec3 pos;
			if (world == null)
				world = targ.worldObj;
			if (targ == null)
				pos = new Vec3(targpos.getX(), targpos.getY(), targpos.getZ());
			else
				pos = targ.getPositionVector();
			
			this.triggerInstance = trigger.instance(this, world, pos,
					(targ == null ? -90.0f : targ.rotationPitch),
					(targ == null ? 0.0f : targ.rotationYaw));
			this.triggerInstance.init(caster);
		}
		
		private SpellState split(EntityLiving self, EntityLiving other) {
			SpellState spawn = new SpellState(caster);
			spawn.index = this.index;
			spawn.self = self;
			spawn.other = other;
			
			return spawn;
		}

		public EntityLiving getSelf() {
			return self;
		}

		public EntityLiving getOther() {
			return other;
		}

		public EntityLiving getCaster() {
			return caster;
		}
	}
	
	private static class SpellPart {
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
	
	public Spell(String name) {
		this.name = name;
		this.parts = new LinkedList<>();
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
	
	
	public static final SpellAction solveAction(EntityLiving caster, EAlteration alteration,
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
	
	private static final SpellAction solveAlter(EntityLiving caster, EMagicElement element,
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
	
	private static final SpellAction solveInflict(EntityLiving caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Potion.weakness, duration, amp);
		case EARTH:
			return new SpellAction(caster).status(RootedPotion.instance(), duration, amp);
		case ENDER:
			return new SpellAction(caster).status(Potion.blindness, duration, amp);
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
	
	private static final SpellAction solveResist(EntityLiving caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Potion.resistance, duration, amp);
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
	
	private static final SpellAction solveSupport(EntityLiving caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Potion.absorption, duration * 5, amp);
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
	
	private static final SpellAction solveGrowth(EntityLiving caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Potion.saturation, 1, 4 * elementCount);
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
	
	private static final SpellAction solveEnchant(EntityLiving caster, EMagicElement element,
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
	
	private static final SpellAction solveConjure(EntityLiving caster, EMagicElement element,
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
	
	private static final SpellAction solveSummon(EntityLiving caster, EMagicElement element,
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
