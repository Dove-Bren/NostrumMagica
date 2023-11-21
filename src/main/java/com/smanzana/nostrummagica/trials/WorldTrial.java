package com.smanzana.nostrummagica.trials;

import java.util.EnumMap;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.INostrumMagic.ElementalMastery;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class WorldTrial {

	private static Map<EMagicElement, WorldTrial> shrineTrials = new EnumMap<>(EMagicElement.class);
	
	public static WorldTrial getTrial(EMagicElement element) {
		return shrineTrials.get(element);
	}
	
	public static void setTrial(EMagicElement element, WorldTrial trial) {
		shrineTrials.put(element, trial);
	}
	
	protected EMagicElement element;
	
	public WorldTrial(EMagicElement element) {
		this.element = element;
	}
	
	public boolean canTake(PlayerEntity entityPlayer, INostrumMagic attr) {
		final ElementalMastery mastery = attr.getElementalMastery(this.element);
		
		return !attr.hasTrial(this.element) // Can't already have this trial
				&& mastery.isGreaterOrEqual(ElementalMastery.NOVICE) // Have to have at least novice
				&& !mastery.isGreaterOrEqual(ElementalMastery.MASTER); // Can't be master or better
	}
	
	public void start(PlayerEntity player, INostrumMagic attr) {
		attr.startTrial(this.element);
		
		final ElementalMastery mastery = attr.getElementalMastery(this.element);
		if (mastery == ElementalMastery.NOVICE) {
			if (!player.world.isRemote) {
				NostrumMagicaSounds.STATUS_DEBUFF3.play(player);
				player.sendMessage(new TranslationTextComponent("info.element.starttrial", new Object[] {this.element.getName()}));
			}
		} else {
			complete(player);
		}
	}
	
	protected void complete(PlayerEntity player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		final ElementalMastery currentMastery = attr.getElementalMastery(this.element);
		final ElementalMastery newMastery;
		switch (currentMastery) {
		case MASTER:
		default:
			newMastery = currentMastery; // Shouldn't have gotten here
			break;
		case UNKNOWN:
		case NOVICE:
			newMastery = ElementalMastery.ADEPT;
			break;
		case ADEPT:
			newMastery = ElementalMastery.MASTER;
			break;
		}
		
		attr.endTrial(element);
		attr.setElementalMastery(this.element, newMastery);
		
		if (!player.world.isRemote) {
			NostrumMagicaSounds.LEVELUP.play(player);
			// Message done in attr
			//player.sendMessage(new TranslationTextComponent("info.element.mastery" + mastery.intValue(), new Object[] {this.element.getName()}));
			NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) player);
		}
			
	}
		
}
