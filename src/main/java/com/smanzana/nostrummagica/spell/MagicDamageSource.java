package com.smanzana.nostrummagica.spell;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class MagicDamageSource extends EntityDamageSource {
	
	private EMagicElement element;
	
	public MagicDamageSource(@Nullable Entity source, EMagicElement element) {
		super("nostrummagic", source);
		this.element = element;
		
		this.bypassArmor();
		this.bypassInvul();
	}
	
	@Override
	public Component getLocalizedDeathMessage(LivingEntity entityLivingBaseIn) {
		final String untranslated;
		final Component extraName;
		if (this.entity != null) {
			untranslated = "death.attack.magic.attacker." + element.name();
			extraName = this.entity.getDisplayName();
		} else {
			untranslated = "death.attack.magic." + element.name();
			extraName = TextComponent.EMPTY;
		}
        return new TranslatableComponent(untranslated, entityLivingBaseIn.getDisplayName(), extraName);
    }

	public EMagicElement getElement() {
		return element;
	}
	
	@Nullable
	public Entity getEntity() {
		return super.getEntity();
	}
	
	@Nullable
	public Entity getDirectEntity() {
		return super.getDirectEntity();
	}
}