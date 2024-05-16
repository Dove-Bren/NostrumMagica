package com.smanzana.nostrummagica.entity.dragon;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// Very stripped down 'AI' predicates.
// Tamed dragons may use these to allow players to set up when they cast stuff!
public enum EntityDragonGambit {

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
	private List<ITextComponent> desc;
	private String transName;
	
	private EntityDragonGambit(String unlocName, int texOffsetX, int texOffsetY) {
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
	public List<ITextComponent> getDesc() {
		if (this.desc == null) {
			String raw = I18n.format("gambit." + getUnlocName() + ".desc", "" + TextFormatting.DARK_GREEN + TextFormatting.BOLD, TextFormatting.RESET);
			String[] lines = raw.split("\\|");
			
			this.desc = new ArrayList<>(lines.length + 1);
			desc.add(new StringTextComponent(getName()).mergeStyle(TextFormatting.BLUE).mergeStyle(TextFormatting.BOLD));
			for (String line : lines) {
				desc.add(new StringTextComponent(line));
			}
		}
		
		return this.desc;
	}
	
	@OnlyIn(Dist.CLIENT)
	public String getName() {
		if (this.transName == null) {
			this.transName = I18n.format("gambit." + getUnlocName() + ".name");
		}
		
		return this.transName;
	}
}
