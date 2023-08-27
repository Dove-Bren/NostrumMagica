package com.smanzana.nostrummagica.quests.rewards;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;

public class AttributeReward implements IReward {

	public static enum AwardType {
		MANA,
		REGEN,
		COST
	}
	
	private AwardType type;
	private float modifier;
	
	public AttributeReward(AwardType type, float modifier) {
		this.type = type;
		this.modifier = modifier;
	}
	
	@Override
	public void award(PlayerEntity player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		switch (type) {
		case COST:
			attr.addManaCostModifer(modifier);
			break;
		case MANA:
			attr.addManaModifier(modifier);
			break;
		case REGEN:
			attr.addManaRegenModifier(modifier);
			break;
		}
	}
	
	@Override
	public String getDescription() {
		return I18n.format("reward.attrib." + type.name().toLowerCase(), new Object[]{modifier * 100f});
	}
	
	public AwardType getType() {
		return this.type;
	}
}
