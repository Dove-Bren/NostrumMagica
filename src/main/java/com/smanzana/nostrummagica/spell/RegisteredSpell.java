package com.smanzana.nostrummagica.spell;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;

import net.minecraft.nbt.CompoundTag;

/**
 * A spell that's registered, persisted, and can be referred to be a registry ID.
 * @author Skyler
 *
 */
public class RegisteredSpell extends Spell {
	
	private int registryID;
	
	protected RegisteredSpell(String name, SpellType type, int manaCost, int weight) {
		super(name, type, manaCost, weight);
	}
	
	protected RegisteredSpell(String name, SpellType type, int manaCost, int weight, int registryID) {
		this(name, type, manaCost, weight);
		this.registryID = registryID;
	}
	
	protected RegisteredSpell(CompoundTag nbt, int registryID) {
		super(nbt);
		this.registryID = registryID;
	}
	
//	public static final RegisteredSpell MakeExisting(String name, SpellType type, int manaCost, int weight, int registryID) {
//		return new RegisteredSpell(name, type, manaCost, weight, registryID);
//	}
	
	public static final RegisteredSpell MakeAndRegister(String name, SpellType type, int manaCost, int weight) {
		RegisteredSpell spell = new RegisteredSpell(name, type, manaCost, weight);
		spell.registryID = NostrumMagica.instance.getSpellRegistry().register(spell);
		return spell;
	}
	
	public static final RegisteredSpell WrapAndRegister(Spell spell) {
		RegisteredSpell registered = MakeAndRegister(spell.getName(), spell.getType(), spell.getManaCost(), spell.getWeight());
		for (SpellShapePart shape : spell.getSpellShapeParts()) {
			registered.addPart(shape);
		}
		for (SpellEffectPart part : spell.getSpellEffectParts()) {
			registered.addPart(part);
		}
		return registered;
	}

	public int getRegistryID() {
		return registryID;
	}
	
	@Override
	public CompoundTag toNBT() {
		return super.toNBT();
	}
	
	public static RegisteredSpell FromNBT(CompoundTag nbt, int id) {
		return new RegisteredSpell(nbt, id);
	}
}
