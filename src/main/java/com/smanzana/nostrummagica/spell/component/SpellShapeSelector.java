package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.IPrettyEnum;

import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;

public enum SpellShapeSelector implements IPrettyEnum {

	ENTITIES(false, true),
	BOTH(true, true),
	BLOCKS(true, false);
	
	private final boolean affectsBlocks;
	private final boolean affectsEntities;
	private final BaseComponent displayName;
	
	private SpellShapeSelector(boolean blocks, boolean entities) {
		this.displayName = new TranslatableComponent("shape_selector." + this.name().toLowerCase() + ".name");
		this.affectsBlocks = blocks;
		this.affectsEntities = entities;
	}

	@Override
	public BaseComponent getDisplayName() {
		return displayName;
	}

	public boolean affectsBlocks() {
		return affectsBlocks;
	}

	public boolean affectsEntities() {
		return affectsEntities;
	}
	
	public static final EnumSpellShapeProperty<SpellShapeSelector> PROPERTY = new EnumSpellShapeProperty<SpellShapeSelector>("selector", SpellShapeSelector.class) {
		
		private final BaseComponent name = new TranslatableComponent("shapeprop." + this.getName() + ".name");
		private final BaseComponent desc = new TranslatableComponent("shapeprop." + this.getName() + ".desc");
		
		@Override
		public BaseComponent getDisplayName(SpellShape shape) {
			return name.plainCopy();
		}
		
		@Override
		public BaseComponent getDisplayDescription(SpellShape shape) {
			return desc.plainCopy();
		}
		
		@Override
		public SpellShapeSelector getDefault() {
			return SpellShapeSelector.BOTH;
		}
	};
	
}
