package com.smanzana.nostrummagica.spells;

import java.util.List;

import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockPos;

/**
 * Collection of triggers and shapes.
 * @author Skyler
 *
 */
public class Spell {
	
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
		public void trigger(List<EntityLiving> targets, List<EntityLiving> other, List<BlockPos> location) {
			
			//for each target/other pair (if more than one), break into multiple spellstates
			// persist index++ and set self/other, then start doing shapes or next trigger
			
			SpellPart next;
			while ((next = parts.get(++index)) != null && !next.isTrigger()) {
				// it's a shape. Do it idk
				
			}
			
			// next is either null or a trigger
			if (next == null) {
				// end of spell
			} else {
				spawn trigger
			}
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
		
		public SpellPart(SpellTrigger trigger) {
			this.trigger = trigger;
		}
		
		public SpellPart(SpellShape shape, EMagicElement element, int count, EAlteration alt) {
			this.shape = shape;
			this.element = element;
			this.elementCount = count;
			this.alteration = alt;
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
	}

	private String name;
	private List<SpellPart> parts;
	
	
	public String crc() {
		
	}
	
	
	
}
