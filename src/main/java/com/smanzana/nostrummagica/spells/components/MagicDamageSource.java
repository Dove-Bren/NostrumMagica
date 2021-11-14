package com.smanzana.nostrummagica.spells.components;

import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class MagicDamageSource extends EntityDamageSource {
	
	private EMagicElement element;
	
	public MagicDamageSource(Entity source, EMagicElement element) {
		super("nostrummagic", source);
		this.element = element;
		
		this.setDamageBypassesArmor();
	}
	
	@Override
	public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
		
        String untranslated = "death.attack.magic." + element.name();
        return new TextComponentTranslation(untranslated, new Object[] {entityLivingBaseIn.getDisplayName(), this.damageSourceEntity.getDisplayName()});
    }

	public EMagicElement getElement() {
		return element;
	}
}