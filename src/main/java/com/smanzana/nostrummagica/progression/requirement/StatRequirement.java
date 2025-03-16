package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.stat.PlayerStat;
import com.smanzana.nostrummagica.stat.PlayerStats;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

public class StatRequirement implements IRequirement{

	private final PlayerStat stat;
	private final float amount;
	
	public StatRequirement(PlayerStat stat, float amount) {
		this.stat = stat;
		this.amount = amount;
	}

	@Override
	public boolean matches(Player player) {
		final PlayerStats stats = NostrumMagica.instance.getPlayerStats().get(player);
		return stats.getStat(stat) >= amount;
	}

	@Override
	public boolean isValid() {
		return true;
	}
	
	@Override
	public List<Component> getDescription(Player player) {
		final PlayerStats stats = NostrumMagica.instance.getPlayerStats().get(player);
		//final String amtString = String.format("%.2f / %.2f ", stats.getStat(stat), amount);
		final String amtString = String.format("%.0f / %.0f ", stats.getStat(stat), amount);
		return Lists.newArrayList(new TextComponent(amtString).append( 
				stat.getName().withStyle(ChatFormatting.BLUE)));
	}
}
