package com.smanzana.nostrummagica.client.effects;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierColor;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierFollow;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierGrow;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierMove;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierShrink;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class ClientPredefinedEffect {

	public static enum PredefinedEffect {
		SOUL_DAGGER_STAB,
		HELL_BURN,
		ELDRICH_BLAST,
	}
	
	public static void Spawn(Vector3d offset, PredefinedEffect type, int duration, @Nullable Entity ent) {
		ClientEffect effect = null;
		switch (type) {
		case SOUL_DAGGER_STAB:
			effect = new ClientEffectMirrored(offset,
					new ClientEffectFormFlat(ClientEffectIcon.ARROW_SLASH, (-8f/24f), (8f/24f), (-12f/24f)),
					duration, 5);
			
			final float scale;
			if (ent != null && ent instanceof LivingEntity) {
				effect.modify(new ClientEffectModifierFollow((LivingEntity) ent));
				scale = ent.getHeight() / 1.8f;
			} else {
				scale = 1f;
			}
			
			
			effect
			.modify(new ClientEffectModifierColor(0xFF80EEFF, 0xFF404060))
			.modify(new ClientEffectModifierGrow(scale, 1f, scale, 1f, .1f))
			//.modify(new ClientEffectModifierTranslate(0, 0, 0))
			.modify(new ClientEffectModifierMove(new Vector3d(2, 2, 0), new Vector3d(0, 0, 0), 0f, .1f))
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
			Minecraft.getInstance().runAsync(() -> {
				ClientEffectRenderer.instance().addEffect(effectToAdd);
			});
		}
	}
	
	public static void SpawnRitualEffect(BlockPos pos, EMagicElement element, ItemStack center, @Nullable NonNullList<ItemStack> extras, ReagentType[] reagents, ItemStack output) {
		Minecraft.getInstance().runAsync(() -> {
			ClientEffectRenderer.instance().addEffect(ClientEffectRitual.Create(
					new Vector3d(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5),
					element, center, extras, reagents, output
					));
		});
	}
}
