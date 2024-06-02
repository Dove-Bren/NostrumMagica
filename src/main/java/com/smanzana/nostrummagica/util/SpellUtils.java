package com.smanzana.nostrummagica.util;

import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

public class SpellUtils {

	public static final Spell MakeSpell(
			String name,
			Object ... objects) {
		Spell spell = Spell.CreateAISpell(name);
		
		for (int i = 0; i < objects.length; i++) {
			Object o = objects[i];
			if (o instanceof SpellShape) {
				SpellShape shape = (SpellShape) o;
				SpellShapePartProperties param;
				if (i < objects.length - 1 && objects[i+1] instanceof SpellShapePartProperties) {
					param = (SpellShapePartProperties) objects[++i];
				} else {
					param = shape.getDefaultProperties();
				}
				
				spell.addPart(new SpellShapePart(shape, param));
			} else if (o instanceof EMagicElement) {
				EMagicElement element = (EMagicElement) objects[++i];
				Integer level = (Integer) objects[++i];
				EAlteration alt = (EAlteration) objects[++i];
				
				spell.addPart(new SpellEffectPart(element, level, alt));
			}
		}
		
		return spell;
	}
	
}
