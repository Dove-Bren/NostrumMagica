package com.smanzana.nostrummagica.spells.components;

import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;

// Finally got sick of writing this over and over so here's a wrapper
public class SpellComponentWrapper {

	private EMagicElement element;
	private EAlteration alteration;
	private SpellShape shape;
	private SpellTrigger trigger;
	
	public SpellComponentWrapper(EMagicElement element) {
		this.element = element;
	}
	
	public SpellComponentWrapper(EAlteration alteration) {
		this.alteration = alteration;
	}
	
	public SpellComponentWrapper(SpellShape shape) {
		this.shape = shape;
	}
	
	public SpellComponentWrapper(SpellTrigger trigger) {
		this.trigger = trigger;
	}
	
	public boolean isElement() {
		return element != null;
	}
	
	public boolean isAlteration() {
		return alteration != null;
	}
	
	public boolean isShape() {
		return shape != null;
	}
	
	public boolean isTrigger() {
		return trigger != null;
	}

	public EMagicElement getElement() {
		return element;
	}

	public EAlteration getAlteration() {
		return alteration;
	}

	public SpellShape getShape() {
		return shape;
	}

	public SpellTrigger getTrigger() {
		return trigger;
	}
	
}
