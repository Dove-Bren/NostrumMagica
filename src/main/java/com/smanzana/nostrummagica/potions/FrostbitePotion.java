package com.smanzana.nostrummagica.potions;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FrostbitePotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions-frostbite");
	
	private static FrostbitePotion instance;
	public static FrostbitePotion instance() {
		if (instance == null)
			instance = new FrostbitePotion();
		
		return instance;
	}
	
	private FrostbitePotion() {
		super(true, 0xFF93E0FF);
		
		this.setPotionName("potion.frostbite.name");
		this.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED,
				"60A6EF27-8A11-2213-A734-30A4B0CC4E90", -0.1D, 2);
		this.setRegistryName(Resource);
	}
	
	public boolean isReady(int duration, int amp) {
		if (duration <= 0)
			return false;
		
		int interval = Math.max(1, (int) (20.0 * (2.0 / Math.pow(2, amp))));
		return (duration % interval == 0); // 2 seconds, 1 second, .5 seconds, ...
	}

	@Override
	public void performEffect(EntityLivingBase entity, int amp) {
		float damage = 1.0f;
        entity.attackEntityFrom(DamageSource.MAGIC, damage);
    }
	
	@SideOnly(Side.CLIENT)
	@Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		PotionIcon.FROSTBITE.draw(mc, x + 6, y + 7);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		PotionIcon.FROSTBITE.draw(mc, x + 3, y + 3);
	}
	
}
