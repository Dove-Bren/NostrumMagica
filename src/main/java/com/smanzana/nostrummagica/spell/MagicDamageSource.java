package com.smanzana.nostrummagica.spell;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class MagicDamageSource extends EntityDamageSource {
	
	private EMagicElement element;
	
	public MagicDamageSource(@Nullable Entity source, EMagicElement element) {
		super("nostrummagic", source);
		this.element = element;
		
		this.bypassArmor();
		this.bypassInvul();
	}
	
	@Override
	public ITextComponent getLocalizedDeathMessage(LivingEntity entityLivingBaseIn) {
		final String untranslated;
		final ITextComponent extraName;
		if (this.entity != null) {
			untranslated = "death.attack.magic.attacker." + element.name();
			extraName = this.entity.getDisplayName();
		} else {
			untranslated = "death.attack.magic." + element.name();
			extraName = StringTextComponent.EMPTY;
		}
        return new TranslationTextComponent(untranslated, entityLivingBaseIn.getDisplayName(), extraName);
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