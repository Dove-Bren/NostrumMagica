package com.smanzana.nostrummagica.potions;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MagicBoostPotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions-magboost");
	
	private static MagicBoostPotion instance;
	public static MagicBoostPotion instance() {
		if (instance == null)
			instance = new MagicBoostPotion();
		
		return instance;
	}
	
	private MagicBoostPotion() {
		super(false, 0xFF47FFAF);

		this.setBeneficial();
		this.setPotionName("potion.magicboost.name");
		NostrumMagica.registerPotion(this, Resource);
	}
	
	public boolean isReady(int duration, int amp) {
		return false; // No tick effects
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		PotionIcon.MAGICBOOST.draw(mc, x + 6, y + 7);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		PotionIcon.MAGICBOOST.draw(mc, x + 3, y + 3);
	}
	
}
