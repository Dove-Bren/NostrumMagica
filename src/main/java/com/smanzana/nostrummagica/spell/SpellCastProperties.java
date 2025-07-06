package com.smanzana.nostrummagica.spell;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

public record SpellCastProperties(
		float efficiency,
		int bonusCharges,
		@Nullable LivingEntity targetHint
		) {
	
	public static final SpellCastProperties BASE = new SpellCastProperties(1f, 0, null);

	public final SpellCastProperties withEfficiency(float newEfficiency) {
		return new SpellCastProperties(newEfficiency, bonusCharges, targetHint);
	}
	
	public final NetworkWrapper wrap() {
		return new NetworkWrapper(efficiency, bonusCharges, targetHint == null ? null : targetHint.getId());
	}

	public static final SpellCastProperties makeSimple(float efficiency) {
		return new SpellCastProperties(efficiency, 0, null);
	}
	
	public static final SpellCastProperties makeWithTarget(float efficiency, @Nullable LivingEntity targetHint) {
		return new SpellCastProperties(efficiency, 0, targetHint);
	}
	
	public static record NetworkWrapper(float efficiency,
			int bonusCharges,
			@Nullable Integer targetHintID) {
		
		private static final String NBT_EFFICIENCY = "efficiency";
		private static final String NBT_CHARGE_BONUS = "bonus_charges";
		private static final String NBT_TARGET_HINT = "target";
		
		public NetworkWrapper(CompoundTag nbt) {
			this(nbt.getFloat(NBT_EFFICIENCY), nbt.getInt(NBT_CHARGE_BONUS), nbt.contains(NBT_TARGET_HINT) ? nbt.getInt(NBT_TARGET_HINT) : null);
		}
		
		public CompoundTag toNBT() {
			CompoundTag tag = new CompoundTag();
			tag.putFloat(NBT_EFFICIENCY, efficiency);
			tag.putInt(NBT_CHARGE_BONUS, bonusCharges);
			return tag;
		}
		
		public final SpellCastProperties unwrap(Function<Integer, LivingEntity> entityLookup) {
			return new SpellCastProperties(efficiency, bonusCharges, targetHintID == null ? null : entityLookup.apply(targetHintID));
		}
	}
}
