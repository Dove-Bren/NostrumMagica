package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.stats.PlayerStat;
import com.smanzana.nostrummagica.stats.PlayerStats;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class StatRequirement implements IRequirement{

	private final PlayerStat stat;
	private final float amount;
	
	public StatRequirement(PlayerStat stat, float amount) {
		this.stat = stat;
		this.amount = amount;
	}

	@Override
	public boolean matches(PlayerEntity player) {
		final PlayerStats stats = NostrumMagica.instance.getPlayerStats().get(player);
		return stats.getStat(stat) >= amount;
	}

	@Override
	public boolean isValid() {
		return true;
	}
	
	@Override
	public List<ITextComponent> getDescription(PlayerEntity player) {
		final PlayerStats stats = NostrumMagica.instance.getPlayerStats().get(player);
		final String amtString = String.format("%.2f / %.2f ", stats.getStat(stat), amount);
		return Lists.newArrayList(new StringTextComponent(amtString).append( 
				stat.getName().mergeStyle(TextFormatting.BLUE)));
	}
}
