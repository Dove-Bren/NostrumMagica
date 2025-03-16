package com.smanzana.nostrummagica.client.effects;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierColor;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierFollow;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierGrow;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierMove;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierShrink;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class ClientPredefinedEffect {

	public static enum PredefinedEffect {
		SOUL_DAGGER_STAB,
		HELL_BURN,
		ELDRICH_BLAST,
	}
	
	public static void Spawn(Vec3 offset, PredefinedEffect type, int duration, @Nullable Entity ent) {
		ClientEffect effect = null;
		switch (type) {
		case SOUL_DAGGER_STAB:
			effect = new ClientEffectMirrored(offset,
					new ClientEffectFormFlat(ClientEffectIcon.ARROW_SLASH, (-8f/24f), (8f/24f), (-12f/24f)),
					duration, 5);
			
			final float scale;
			if (ent != null && ent instanceof LivingEntity) {
				effect.modify(new ClientEffectModifierFollow((LivingEntity) ent));
				scale = ent.getBbHeight() / 1.8f;
			} else {
				scale = 1f;
			}
			
			
			effect
			.modify(new ClientEffectModifierColor(0xFF80EEFF, 0xFF404060))
			.modify(new ClientEffectModifierGrow(scale, 1f, scale, 1f, .1f))
			//.modify(new ClientEffectModifierTranslate(0, 0, 0))
			.modify(new ClientEffectModifierMove(new Vec3(2, 2, 0), new Vec3(0, 0, 0), 0f, .1f))
			.modify(new ClientEffectModifierGrow(2f, 0f, 2f, 1f, .05f))
			.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .75f))
			;
			break;
		case HELL_BURN:
			effect = new ClientEffectHellBurn(ent, duration);
			break;
		case ELDRICH_BLAST:
			effect = new ClientEffectEldrichBlast(ent, duration);
			break;
		}
		
		if (effect != null) {
			final ClientEffect effectToAdd = effect;
			Minecraft.getInstance().submit(() -> {
				ClientEffectRenderer.instance().addEffect(effectToAdd);
			});
		}
	}
	
	public static void SpawnRitualEffect(BlockPos pos, EMagicElement element, ItemStack center, @Nullable List<ItemStack> extras, List<ItemStack> reagents, ItemStack output) {
		Minecraft.getInstance().submit(() -> {
			ClientEffectRenderer.instance().addEffect(ClientEffectRitual.Create(
					new Vec3(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5),
					element, center, extras, reagents, output
					));
		});
	}
}
