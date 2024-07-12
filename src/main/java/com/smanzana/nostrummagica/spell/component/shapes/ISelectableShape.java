package com.smanzana.nostrummagica.spell.component.shapes;

import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeSelector;

public interface ISelectableShape {

	public default boolean affectsBlocks(SpellShapeProperties properties) {
		return properties.getValue(SpellShapeSelector.PROPERTY).affectsBlocks();
	}
	
	public default boolean affectsEntities(SpellShapeProperties properties) {
		return properties.getValue(SpellShapeSelector.PROPERTY).affectsEntities();
	}
	
}
