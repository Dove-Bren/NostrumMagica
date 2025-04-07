package com.smanzana.nostrummagica.trial;

import java.util.EnumMap;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.EElementalMastery;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;

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
	
	public boolean canTake(Player entityPlayer, INostrumMagic attr) {
		final EElementalMastery mastery = attr.getElementalMastery(this.element);
		
		return !attr.hasTrial(this.element) // Can't already have this trial
				&& mastery.isGreaterOrEqual(EElementalMastery.NOVICE) // Have to have at least novice
				&& !mastery.isGreaterOrEqual(EElementalMastery.MASTER); // Can't be master or better
	}
	
	public void start(Player player, INostrumMagic attr) {
		attr.startTrial(this.element);
		
		final EElementalMastery mastery = attr.getElementalMastery(this.element);
		if (mastery == EElementalMastery.NOVICE) {
			if (!player.level.isClientSide) {
				NostrumMagicaSounds.STATUS_DEBUFF3.play(player);
				player.sendMessage(new TranslatableComponent("info.element.starttrial", new Object[] {this.element.getName()}), Util.NIL_UUID);
			}
		} else {
			complete(player);
		}
	}
	
	protected void complete(Player player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		final EElementalMastery currentMastery = attr.getElementalMastery(this.element);
		final EElementalMastery newMastery;
		switch (currentMastery) {
		case MASTER:
		default:
			newMastery = currentMastery; // Shouldn't have gotten here
			break;
		case UNKNOWN:
		case NOVICE:
			newMastery = EElementalMastery.ADEPT;
			break;
		case ADEPT:
			newMastery = EElementalMastery.MASTER;
			break;
		}
		
		attr.endTrial(element);
		NostrumMagica.UnlockElementalMastery(player, this.element, newMastery);
		
		if (!player.level.isClientSide) {
			NostrumMagicaSounds.LEVELUP.play(player);
			// Message done in attr
			//player.sendMessage(new TranslationTextComponent("info.element.mastery" + mastery.intValue(), new Object[] {this.element.getName()}));
			NostrumMagica.instance.proxy.syncPlayer((ServerPlayer) player);
		}
			
	}
		
}
