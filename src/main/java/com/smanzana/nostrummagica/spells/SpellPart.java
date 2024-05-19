package com.smanzana.nostrummagica.spells;

import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

public class SpellPart {
	private final SpellTrigger trigger;
	private final SpellShape shape;
	private final EAlteration alteration;
	private final EMagicElement element;
	private final int elementCount;
	private final SpellPartProperties param;
	
	public SpellPart(SpellTrigger trigger, SpellPartProperties param) {
		this.trigger = trigger;
		this.param = param;
		
		this.shape = null;
		this.alteration = null;
		this.element = null;
		this.elementCount = 0;
	}
	
	public SpellPart(SpellTrigger trigger) {
		this(trigger, new SpellPartProperties(0, false));
	}
	
	public SpellPart(SpellShape shape, EMagicElement element, int count, EAlteration alt, SpellPartProperties param) {
		this.shape = shape;
		this.element = element;
		this.elementCount = count;
		this.alteration = alt;
		this.param = param;
		
		this.trigger = null;
	}
	
	public SpellPart(SpellShape shape, EMagicElement element, int count, EAlteration alteration) {
		this(shape, element, count, alteration, new SpellPartProperties(0, false));
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

	public SpellPartProperties getParam() {
		return param;
	}
}