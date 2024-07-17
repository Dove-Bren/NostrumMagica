package com.smanzana.nostrummagica.spell.log;

import java.util.List;

public class SpellLogEffectSummary {

	private final float totalDamage;
	private final float totalHeal;
	
	private final List<SpellLogEffectLine> elements;

	protected SpellLogEffectSummary(List<SpellLogEffectLine> elements, float totalDamage, float totalHeal) {
		this.totalDamage = totalDamage;
		this.totalHeal = totalHeal;
		this.elements = elements;
	}
	
	public SpellLogEffectSummary(List<SpellLogEffectLine> elements) {
		this(elements,
				(float) elements.stream().mapToDouble(e -> e.getTotalDamage()).sum(),
				(float) elements.stream().mapToDouble(e -> e.getTotalHeal()).sum());
	}

	public float getTotalDamage() {
		return totalDamage;
	}

	public float getTotalHeal() {
		return totalHeal;
	}

	public List<SpellLogEffectLine> getElements() {
		return elements;
	}
}
