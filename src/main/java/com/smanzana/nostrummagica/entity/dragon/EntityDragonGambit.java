package com.smanzana.nostrummagica.entity.dragon;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Very stripped down 'AI' predicates.
// Tamed dragons may use these to allow players to set up when they cast stuff!
public enum EntityDragonGambit {

	ALWAYS("always", 10),
	
	HEALTH_CRITICAL("health_critical", 20),
	
	HEALTH_LOW("health_low", 30),
	
	MANA_LOW("mana_low", 40),
	
	OCCASIONAL("occasional", 50),
	
	FREQUENT("frequent", 60);
	
	private String unlocName;
	private int texOffsetX;
	
	// Only the client uses these
	private List<String> desc;
	private String transName;
	
	private EntityDragonGambit(String unlocName, int texOffsetX) {
		this.unlocName = unlocName;
		this.texOffsetX = texOffsetX;
		this.desc = null;
		this.transName = null;
	}
	
	public String getUnlocName() {
		return unlocName;
	}
	
	public int getTexOffsetX() {
		return texOffsetX;
	}
	
	@SideOnly(Side.CLIENT)
	public List<String> getDesc() {
		if (this.desc == null) {
			String raw = I18n.format("gambit." + getUnlocName() + ".desc", "" + ChatFormatting.DARK_GREEN + ChatFormatting.BOLD, ChatFormatting.RESET);
			String[] lines = raw.split("\\|");
			
			this.desc = Lists.asList("" + ChatFormatting.BLUE + ChatFormatting.BOLD + getName() + ChatFormatting.RESET, lines);
			
		}
		
		return this.desc;
	}
	
	@SideOnly(Side.CLIENT)
	public String getName() {
		if (this.transName == null) {
			this.transName = I18n.format("gambit." + getUnlocName() + ".name");
		}
		
		return this.transName;
	}
}
