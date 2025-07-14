package com.smanzana.nostrummagica.entity.boss.shadowdragon;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import com.smanzana.autodungeons.util.JavaUtils;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellShapeSelector;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.util.SpellUtils;

public class ShadowDragonSpells {

	public static final ShadowDragonSpells Instance() {
		if (instance == null) {
			instance = new ShadowDragonSpells();
		}
		
		return instance;
	}
	
	private static ShadowDragonSpells instance = null;
	
	private final Map<EMagicElement, Spell> bombSpells;
	
	private ShadowDragonSpells() {
		this.bombSpells = new EnumMap<>(EMagicElement.class);
		
		for (EMagicElement element : EMagicElement.values()) {
			bombSpells.put(element, makeBombSpell(element));
		}
	}
	
	protected Spell makeBombSpell(EMagicElement element) {
		return SpellUtils.MakeSpell("Dragon's %s bomb".formatted(element.getBareName()),
				NostrumSpellShapes.Mortar, NostrumSpellShapes.Mortar.makeProps(true),
				NostrumSpellShapes.Burst, NostrumSpellShapes.Burst.makeProps(5f).setValue(SpellShapeSelector.PROPERTY, SpellShapeSelector.ENTITIES),
				element, 3, EAlteration.HARM
				);
	}

	public Spell randomBombSpell(Random rand) {
		return JavaUtils.GetRandom(bombSpells.values(), rand).get();
	}
	
}
