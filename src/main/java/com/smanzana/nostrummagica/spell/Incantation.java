package com.smanzana.nostrummagica.spell;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellCrafting;

import net.minecraft.nbt.CompoundTag;

/**
 * A weaker version of a spell. Ultimately is still expressed and cast as a spell, but limits what can be in itself.
 */
public class Incantation {

	private final SpellShapePart shapePart;
	private final SpellEffectPart effectPart;
	
	private @Nullable Spell resultSpell;
	
	protected Incantation(SpellShapePart shapePart, SpellEffectPart effectPart) {
		this.shapePart = shapePart;
		this.effectPart = effectPart;
	}

	public Incantation(SpellShape shape, EMagicElement element, @Nullable EAlteration alteration) {
		this(new SpellShapePart(shape), new SpellEffectPart(element, 1, alteration, .5f));
	}
	
	public SpellShape getShape() {
		return shapePart.getShape();
	}

	public EMagicElement getElement() {
		return effectPart.getElement();
	}

	public @Nullable EAlteration getAlteration() {
		return effectPart.getAlteration();
	}
	
	public int getManaCost() {
		final SpellCraftContext context = SpellCraftContext.DUMMY;
		return SpellCrafting.CalculateManaCost(context, shapePart) + SpellCrafting.CalculateManaCost(context, effectPart);
	}
	
	public int getWeight() {
		final SpellCraftContext context = SpellCraftContext.DUMMY;
		return 2 + SpellCrafting.CalculateWeight(context, shapePart) + SpellCrafting.CalculateWeight(context, effectPart);
	}
	
	protected Spell createSpell() {
		final int mana = getManaCost();
		final int weight = getWeight();
		
		return new Spell("incantation", true, mana, weight).addPart(shapePart).addPart(effectPart);
	}
	
	public Spell makeSpell() {
		if (resultSpell == null) {
			resultSpell = createSpell();
		}
		
		return resultSpell;
	}
	
	public CompoundTag toNBT() {
		CompoundTag tag = new CompoundTag();
		
		tag.put("shape", this.shapePart.toNBT(null));
		tag.put("effect", this.effectPart.toNBT(null));
		
		return tag;
	}
	
	public static Incantation FromNBT(CompoundTag tag) {
		SpellShapePart shape = SpellShapePart.FromNBT(tag.getCompound("shape"));
		SpellEffectPart effect = SpellEffectPart.FromNBT(tag.getCompound("effect"));
		
		return new Incantation(shape, effect);
	}
	
}
