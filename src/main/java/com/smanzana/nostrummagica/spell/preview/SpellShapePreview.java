package com.smanzana.nostrummagica.spell.preview;

import java.util.ArrayList;
import java.util.List;

public class SpellShapePreview {
	
	protected final List<SpellShapePreviewComponent> components;
	
	public SpellShapePreview() {
		this.components = new ArrayList<>();
	}
	
	public SpellShapePreview add(SpellShapePreviewComponent component) {
		this.components.add(component);
		return this;
	}
	
	public List<SpellShapePreviewComponent> getComponents() {
		return components;
	}
}
