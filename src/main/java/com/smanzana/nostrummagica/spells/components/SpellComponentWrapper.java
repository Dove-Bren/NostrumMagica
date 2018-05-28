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
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof SpellComponentWrapper) {
			SpellComponentWrapper other = (SpellComponentWrapper) o;
			if (other.element != element)
				return false;
			if (other.alteration != alteration)
				return false;
			if (other.shape == null && shape != null)
				return false;
			if (other.shape != null && shape == null)
				return false;
			if (other.shape != null && shape != null &&
					!other.shape.getShapeKey().equals(shape.getShapeKey()))
				return false;
			if (other.trigger == null && trigger != null)
				return false;
			if (other.trigger != null && trigger == null)
				return false;
			if (other.trigger != null && trigger != null &&
					!other.trigger.getTriggerKey().equals(trigger.getTriggerKey()))
				return false;
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		int code;
		if (element != null) {
			code = element.hashCode();
		} else if (alteration != null) {
			code = alteration.hashCode() + 17;
		} else if (shape != null) {
			code = shape.getShapeKey().hashCode() * 7 + 37;
		} else if (trigger != null) {
			code = trigger.getTriggerKey().hashCode() * 19 + 197;
		} else {
			code = -1;
		}
		
		return code;
			
	}
	
	/**
	 * Returns a string that's unique to each value of a SpellComponentWrapper.
	 * Designed for serialization. A wrapper can be reconstructed from the string.
	 * @return
	 */
	public String getKeyString() {
		return this.toString();
	}
	
	public static SpellComponentWrapper fromKeyString(String keyString) {
		if (keyString == null || keyString.trim().isEmpty() ||
				keyString.indexOf(':') == -1)
			return new SpellComponentWrapper(EMagicElement.PHYSICAL);
		
		int pos = keyString.indexOf(':');
		String type = keyString.substring(0, pos);
		String key = keyString.substring(pos + 1);
		
		switch (type.toLowerCase()) {
		case "element":
			try {
				EMagicElement elem = EMagicElement.valueOf(key.toUpperCase());
				return new SpellComponentWrapper(elem);
			} catch (Exception e) {
				return new SpellComponentWrapper(EMagicElement.PHYSICAL);
			}
		case "alteration":
			try {
				EAlteration altr = EAlteration.valueOf(key.toUpperCase());
				return new SpellComponentWrapper(altr);
			} catch (Exception e) {
				return new SpellComponentWrapper(EAlteration.INFLICT);
			}
		case "shape":
			SpellShape shape = SpellShape.get(key);
			if (shape == null)
				return new SpellComponentWrapper(EMagicElement.PHYSICAL);
			else
				return new SpellComponentWrapper(shape);
		case "trigger":
			SpellTrigger trigger = SpellTrigger.get(key);
			if (trigger == null)
				return new SpellComponentWrapper(EMagicElement.PHYSICAL);
			else
				return new SpellComponentWrapper(trigger);
		default:
			return new SpellComponentWrapper(EMagicElement.PHYSICAL);
		}
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (element != null) {
			buf.append("element:");
			buf.append(element.name());
		} else if (alteration != null) {
			buf.append("alteration:");
			buf.append(alteration.name());
		} else if (shape != null) {
			buf.append("shape:");
			buf.append(shape.getShapeKey());
		} else if (trigger != null) {
			buf.append("trigger:");
			buf.append(trigger.getTriggerKey());
		} else {
			buf.append("element:").append("PHYSICAL");
		}
		
		return buf.toString();
	}
	
}
