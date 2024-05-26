package com.smanzana.nostrummagica.spells;

import com.smanzana.nostrummagica.spells.components.LegacySpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

public class LegacySpellPart {
	private final SpellTrigger trigger;
	private final LegacySpellShape shape;
	private final EAlteration alteration;
	private final EMagicElement element;
	private final int elementCount;
	private final SpellPartProperties param;
	
	public LegacySpellPart(SpellTrigger trigger, SpellPartProperties param) {
		this.trigger = trigger;
		this.param = param;
		
		this.shape = null;
		this.alteration = null;
		this.element = null;
		this.elementCount = 0;
	}
	
	public LegacySpellPart(SpellTrigger trigger) {
		this(trigger, new SpellPartProperties(0, false));
	}
	
	public LegacySpellPart(LegacySpellShape shape, EMagicElement element, int count, EAlteration alt, SpellPartProperties param) {
		this.shape = shape;
		this.element = element;
		this.elementCount = count;
		this.alteration = alt;
		this.param = param;
		
		this.trigger = null;
	}
	
	public LegacySpellPart(LegacySpellShape shape, EMagicElement element, int count, EAlteration alteration) {
		this(shape, element, count, alteration, new SpellPartProperties(0, false));
	}
	
	public boolean isTrigger() {
		return trigger != null;
	}

	public SpellTrigger getTrigger() {
		return trigger;
	}

	public LegacySpellShape getShape() {
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