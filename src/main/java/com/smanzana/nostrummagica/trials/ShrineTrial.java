package com.smanzana.nostrummagica.trials;

import java.util.EnumMap;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class ShrineTrial {

	private static Map<EMagicElement, ShrineTrial> shrineTrials = new EnumMap<>(EMagicElement.class);
	
	public static ShrineTrial getTrial(EMagicElement element) {
		return shrineTrials.get(element);
	}
	
	public static void setTrial(EMagicElement element, ShrineTrial trial) {
		shrineTrials.put(element, trial);
	}
	
	protected EMagicElement element;
	
	public ShrineTrial(EMagicElement element) {
		this.element = element;
	}
	
	public boolean canTake(PlayerEntity entityPlayer, INostrumMagic attr) {
		Boolean bool = attr.getKnownElements().get(this.element);
		if (bool == null || !bool)
			return false;
		
		Integer mastery = attr.getElementMastery().get(this.element);
		if (mastery != null && mastery > 2)
			return false;
		
		return !attr.hasTrial(this.element);
	}
	
	public void start(PlayerEntity player, INostrumMagic attr) {
		attr.startTrial(this.element);
		
		Integer mastery = attr.getElementMastery().get(this.element);
		if (mastery == null || mastery == 1) {
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
		
		Integer mastery = attr.getElementMastery().get(this.element);
		if (mastery == null)
			mastery = 0;
		
		mastery = mastery + 1;
		
		attr.endTrial(element);
		attr.setElementMastery(this.element, mastery);
		
		if (!player.world.isRemote) {
			NostrumMagicaSounds.LEVELUP.play(player);
			player.sendMessage(new TranslationTextComponent("info.element.mastery" + mastery.intValue(), new Object[] {this.element.getName()}));
			NostrumMagica.proxy.syncPlayer((ServerPlayerEntity) player);
		}
			
	}
		
}
