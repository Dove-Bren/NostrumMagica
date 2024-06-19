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
		
		this.setDamageBypassesArmor();
		this.setDamageAllowedInCreativeMode();
	}
	
	@Override
	public ITextComponent getDeathMessage(LivingEntity entityLivingBaseIn) {
		final String untranslated;
		final ITextComponent extraName;
		if (this.damageSourceEntity != null) {
			untranslated = "death.attack.magic.attacker." + element.name();
			extraName = this.damageSourceEntity.getDisplayName();
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
	public Entity getTrueSource() {
		return super.getTrueSource();
	}
	
	@Nullable
	public Entity getImmediateSource() {
		return super.getImmediateSource();
	}
}