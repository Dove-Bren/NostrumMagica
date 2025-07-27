package com.smanzana.nostrummagica.entity.dragon;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// Very stripped down 'AI' predicates.
// Tamed dragons may use these to allow players to set up when they cast stuff!
public enum DragonGambit {

	ALWAYS("always", 10, 0),
	
	HEALTH_CRITICAL("health_critical", 20, 0),
	
	HEALTH_LOW("health_low", 30, 0),
	
	MANA_LOW("mana_low", 0, 10),
	
	OCCASIONAL("occasional", 10, 10),
	
	FREQUENT("frequent", 20, 10);
	
	private final String unlocName;
	private final int texOffsetX;
	private final int texOffsetY;
	
	// Only the client uses these
	private List<Component> desc;
	private String transName;
	
	private DragonGambit(String unlocName, int texOffsetX, int texOffsetY) {
		this.unlocName = unlocName;
		this.texOffsetX = texOffsetX;
		this.texOffsetY = texOffsetY;
		this.desc = null;
		this.transName = null;
	}
	
	public String getUnlocName() {
		return unlocName;
	}
	
	public int getTexOffsetX() {
		return texOffsetX;
	}
	
	public int getTexOffsetY() {
		return texOffsetY;
	}
	
	@OnlyIn(Dist.CLIENT)
	public List<Component> getDesc() {
		if (this.desc == null) {
			String raw = I18n.get("gambit." + getUnlocName() + ".desc", "" + ChatFormatting.DARK_GREEN + ChatFormatting.BOLD, ChatFormatting.RESET);
			String[] lines = raw.split("\\|");
			
			this.desc = new ArrayList<>(lines.length + 1);
			desc.add(new TextComponent(getName()).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.BOLD));
			for (String line : lines) {
				desc.add(new TextComponent(line));
			}
		}
		
		return this.desc;
	}
	
	@OnlyIn(Dist.CLIENT)
	public String getName() {
		if (this.transName == null) {
			this.transName = I18n.get("gambit." + getUnlocName() + ".name");
		}
		
		return this.transName;
	}
}
