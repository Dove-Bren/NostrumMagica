package com.smanzana.nostrummagica.integration.caelus;

import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import top.theillusivec4.caelus.api.CaelusAPI;

public class NostrumElytraWrapper {
	
	public static final AttributeModifier MakeHasElytraModifier() {
		return MakeHasElytraModifier(UUID.randomUUID());
	}

	public static final AttributeModifier MakeHasElytraModifier(@Nonnull UUID id) {
		return new AttributeModifier(id, "NostrumHasElytraOverride", 1.0, AttributeModifier.Operation.ADDITION);
	}
	
	public static void AddElytraModifier(LivingEntity entity, AttributeModifier modifier) {
		if (entity.getAttribute(CaelusAPI.ELYTRA_FLIGHT) == null) {
			throw new RuntimeException("Caelus is required for NostrumMagica, but no Caelus attribute found.");
		}
		entity.getAttribute(CaelusAPI.ELYTRA_FLIGHT).applyModifier(modifier);
	}
	
	public static void RemoveElytraModifier(LivingEntity entity, AttributeModifier modifier) {
		if (entity.getAttribute(CaelusAPI.ELYTRA_FLIGHT) == null) {
			throw new RuntimeException("Caelus is required for NostrumMagica, but no Caelus attribute found.");
		}
		entity.getAttribute(CaelusAPI.ELYTRA_FLIGHT).removeModifier(modifier);
	}
}
