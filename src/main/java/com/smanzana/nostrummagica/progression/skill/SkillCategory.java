package com.smanzana.nostrummagica.progression.skill;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SkillCategory {

	protected final ResourceLocation id;
	protected final int color;
	
	private final ITextComponent name;
	
	public SkillCategory(ResourceLocation id, int color) {
		this.id = id;
		this.color = color;
		
		name = new TranslationTextComponent("skill.category." + id.getNamespace() + "." + id.getPath());
	}
	
	public ResourceLocation getID() {
		return this.id;
	}
	
	public ITextComponent getName() {
		return name;
	}
	
	public int getColor() {
		return color;
	}
	
}
