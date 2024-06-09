package com.smanzana.nostrummagica.progression.skill;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SkillCategory {

	protected final ResourceLocation id;
	protected final int color;
	protected final Supplier<ItemStack> iconSupplier;
	protected final @Nullable EMagicElement skillpointType;
	
	private final ITextComponent name;
	private ItemStack iconCache;
	
	public SkillCategory(ResourceLocation id, int color, Supplier<ItemStack> icon, @Nullable EMagicElement skillpointType) {
		this.id = id;
		this.color = color;
		this.iconSupplier = icon;
		this.skillpointType = skillpointType;
		
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
	
	public ItemStack getIcon() {
		if (iconCache == null) {
			iconCache = this.iconSupplier.get();
		}
		return iconCache;
	}

	public @Nullable EMagicElement getSkillpointType() {
		return skillpointType;
	}
	
}
