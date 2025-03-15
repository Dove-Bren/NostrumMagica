package com.smanzana.nostrummagica.progression.reward;

import java.util.UUID;

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
		
		// Hacky. We never expect to undo and redo these anymore.
		UUID id = UUID.randomUUID();
		
		switch (type) {
		case COST:
			attr.addManaCostModifier(id, modifier);
			break;
		case MANA:
			attr.addManaModifier(id, modifier);
			break;
		case REGEN:
			attr.addManaRegenModifier(id, modifier);
			break;
		}
	}
	
	@Override
	public String getDescription() {
		return I18n.get("reward.attrib." + type.name().toLowerCase(), new Object[]{modifier * 100f});
	}
	
	public AwardType getType() {
		return this.type;
	}
}
