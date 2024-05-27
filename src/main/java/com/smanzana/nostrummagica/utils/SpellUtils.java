package com.smanzana.nostrummagica.utils;

import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.SpellShapePartProperties;
import com.smanzana.nostrummagica.spells.components.SpellEffectPart;
import com.smanzana.nostrummagica.spells.components.SpellShapePart;
import com.smanzana.nostrummagica.spells.components.shapes.SpellShape;

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
					param = new SpellShapePartProperties();
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
