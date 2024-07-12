package com.smanzana.nostrummagica.spell.component;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.IPrettyEnum;

import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public enum SpellShapeSelector implements IPrettyEnum {

	ENTITIES(false, true),
	BOTH(true, true),
	BLOCKS(true, false);
	
	private final boolean affectsBlocks;
	private final boolean affectsEntities;
	private final TextComponent displayName;
	
	private SpellShapeSelector(boolean blocks, boolean entities) {
		this.displayName = new TranslationTextComponent("shape_selector." + this.name().toLowerCase() + ".name");
		this.affectsBlocks = blocks;
		this.affectsEntities = entities;
	}

	@Override
	public TextComponent getDisplayName() {
		return displayName;
	}

	public boolean affectsBlocks() {
		return affectsBlocks;
	}

	public boolean affectsEntities() {
		return affectsEntities;
	}
	
	public static final EnumSpellShapeProperty<SpellShapeSelector> PROPERTY = new EnumSpellShapeProperty<SpellShapeSelector>("selector", SpellShapeSelector.class) {
		
		private final TextComponent name = new TranslationTextComponent("shapeprop." + this.getName() + ".name");
		private final TextComponent desc = new TranslationTextComponent("shapeprop." + this.getName() + ".desc");
		
		@Override
		public TextComponent getDisplayName(SpellShape shape) {
			return name;
		}
		
		@Override
		public TextComponent getDisplayDescription(SpellShape shape) {
			return desc;
		}
		
		@Override
		public SpellShapeSelector getDefault() {
			return SpellShapeSelector.BOTH;
		}
	};
	
}
