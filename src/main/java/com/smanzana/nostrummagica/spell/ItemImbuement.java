package com.smanzana.nostrummagica.spell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.log.ISpellLogBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * A stored spell imbuement meant to be attached to an item. Provides ease-of-use funcs for pulling from a stack
 * or for storing on a stack.
 */
public final class ItemImbuement {
	
	protected static final String NBT_IMBUEMENT = "nostrum_imbuement";
	protected static final String NBT_EFFECTS = "effects";
	protected static final String NBT_EFFICIENCY = "efficiency";
	protected static final String NBT_ICON = "icon";

	protected final List<SpellEffectPart> effects;
	protected float efficiency;
	protected int icon;
	
	protected ItemImbuement() {
		effects = new ArrayList<>();
		efficiency = 1f;
		icon = 0;
	}
	
	protected ItemImbuement(CompoundTag nbt) {
		this();
		
		this.fromNBT(nbt);
	}
	
	public static ItemImbuement Make(Collection<SpellEffectPart> effects, float castEfficiency, int icon) {
		ItemImbuement imbue = new ItemImbuement();
		imbue.effects.addAll(effects);
		imbue.efficiency = castEfficiency;
		imbue.icon = icon;
		return imbue;
	}
	
	public static @Nullable ItemImbuement FromItemStack(ItemStack stack) {
		if (stack.isEmpty() || !stack.hasTag()) {
			return null;
		}
		
		CompoundTag stackTag = stack.getTag();
		if (!stackTag.contains(NBT_IMBUEMENT)) {
			return null;
		}
		
		return new ItemImbuement(stackTag.getCompound(NBT_IMBUEMENT));
	}
	
	public static void AttachToStack(ItemStack stack, ItemImbuement imbue) {
		CompoundTag tag = imbue.toNBT();
		stack.getOrCreateTag().put(NBT_IMBUEMENT, tag);
	}
	
	public static void ClearStack(ItemStack stack) {
		if (stack.hasTag()) {
			stack.getTag().remove(NBT_IMBUEMENT);
		}
	}
	
	protected CompoundTag toNBT() {
		CompoundTag tag = new CompoundTag();
		
		if (!this.effects.isEmpty()) {
			ListTag list = new ListTag();
			for (SpellEffectPart effect : this.effects) {
				list.add(effect.toNBT(null));
			}
			
			tag.put(NBT_EFFECTS, list);
		}
		tag.putFloat(NBT_EFFICIENCY, efficiency);
		tag.putInt(NBT_ICON, this.icon);
		
		return tag;
	}
	
	protected void fromNBT(CompoundTag nbt) {
		this.effects.clear();
		if (nbt.contains(NBT_EFFECTS)) {
			ListTag list = nbt.getList(NBT_EFFECTS, Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				this.effects.add(SpellEffectPart.FromNBT(list.getCompound(i)));
			}
		}
		this.efficiency = nbt.getFloat(NBT_EFFICIENCY);
		this.icon = nbt.getInt(NBT_ICON);
	}
	
	public SpellEffects.ApplyResult triggerOn(LivingEntity caster, LivingEntity target) {
		return SpellEffects.ApplySpellEffects(caster, this.effects, this.efficiency, List.of(target), new ArrayList<>(), ISpellLogBuilder.Dummy, null, null);
		
	}
	
	public SpellEffects.ApplyResult triggerOn(LivingEntity caster, SpellLocation target) {
		return SpellEffects.ApplySpellEffects(caster, this.effects, this.efficiency, new ArrayList<>(), List.of(target), ISpellLogBuilder.Dummy, null, null);
	}

	public List<SpellEffectPart> getParts() {
		return effects;
	}
	
	public float getEfficiency() {
		return this.efficiency;
	}
	
	public int getIconIndex() {
		return this.icon;
	}
	
}
