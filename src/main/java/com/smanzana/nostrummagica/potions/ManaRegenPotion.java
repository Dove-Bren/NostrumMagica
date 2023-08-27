package com.smanzana.nostrummagica.potions;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeManaRegen;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ManaRegenPotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions-mana-regen");
	
	
	private static ManaRegenPotion instance;
	public static ManaRegenPotion instance() {
		if (instance == null)
			instance = new ManaRegenPotion();
		
		return instance;
	}
	
	private ManaRegenPotion() {
		super(false, 0xFFBB6DFF);

		this.setBeneficial();
		this.setPotionName("potion.mana-regen.name");
		
		this.registerPotionAttributeModifier(AttributeManaRegen.instance(),
				"74149d64-b22a-4dd9-ab68-030fc195ecfc", 50D, 0);

		this.setRegistryName(Resource);
	}
	
	public boolean isReady(int duration, int amp) {
		return false; // No special effects
	}

	@Override
	public void performEffect(LivingEntity entity, int amp) {
		;
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		PotionIcon.MANAREGEN.draw(mc, x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		PotionIcon.MANAREGEN.draw(mc, x + 3, y + 3);
	}
	
	public String getEffectName() {
		return "mana-regen";
	}
	
}
