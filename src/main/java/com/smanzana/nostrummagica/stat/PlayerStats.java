package com.smanzana.nostrummagica.stat;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.NetUtils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

public class PlayerStats {

	protected final Map<PlayerStat, Float> stats;
	
	public PlayerStats() {
		stats = new HashMap<>();
	}
	
	public float getStat(PlayerStat stat) {
		return stats.computeIfAbsent(stat, (s) -> 0f);
	}
	
	public PlayerStats addStat(PlayerStat stat, float amount) {
		stats.merge(stat, amount, Float::sum);
		return this;
	}
	
	public PlayerStats incrStat(PlayerStat stat) {
		return addStat(stat, 1f);
	}
	
	public PlayerStats takeMax(PlayerStat stat, float amount) {
		stats.merge(stat, amount, Float::max);
		return this;
	}

	public PlayerStats addDamageReceived(float amount, @Nullable EMagicElement magicElement) {
		this.addStat(PlayerStat.DamageReceivedTotal, amount);
		if (magicElement != null) {
			this.addStat(PlayerStat.MagicDamageReceivedTotal, amount);
			this.addStat(PlayerStat.ElementalDamageReceived(magicElement), amount);
		}
		return this;
	}

	public PlayerStats addMagicDamageDealt(float amount, @Nonnull EMagicElement element) {
		this.addStat(PlayerStat.MagicDamageDealtTotal, amount);
		this.addStat(PlayerStat.ElementalDamgeDealt(element), amount);
		return this;
	}

	public @Nonnull CompoundNBT toNBT(@Nullable CompoundNBT nbt) {
		CompoundNBT tag = NetUtils.ToNBT(this.stats, (k) -> k.getID().toString(), FloatNBT::valueOf);
		if (nbt == null) {
			return tag;
		} else {
			nbt.put("nostrum_stats", tag);
			return nbt;
		}
	}
	
	public static final PlayerStats FromNBT(@Nonnull CompoundNBT nbt) {
		PlayerStats stats = new PlayerStats();
		
		CompoundNBT tag;
		if (nbt.contains("nostrum_stats", NBT.TAG_COMPOUND)) {
			tag = nbt.getCompound("nostrum_stats");
		} else {
			tag = nbt;
		}
		NetUtils.FromNBT(stats.stats, tag, (k) -> new PlayerStat(new ResourceLocation(k)), (n) -> ((FloatNBT) n).getFloat());
		
		return stats;
	}
	
}
