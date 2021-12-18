package com.smanzana.nostrummagica.potions;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NaturesBlessingPotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions-naturesblessing");
	
	private static NaturesBlessingPotion instance;
	public static NaturesBlessingPotion instance() {
		if (instance == null)
			instance = new NaturesBlessingPotion();
		
		return instance;
	}
	
	private NaturesBlessingPotion() {
		super(false, 0xFF38810D);
		this.setBeneficial();
		
		this.setPotionName("potion.naturesblessing.name");

		this.setRegistryName(Resource);
	}
	
	public boolean isReady(int duration, int amp) {
		if (duration <= 0)
			return false;
		
		// 10, 5, 2.5, ...
		final int interval = Math.max(1, (int) (20.0 * (10.0 / Math.pow(2, amp))));
		return (duration % interval == 0); // 10 seconds, 5 second, 2.5 seconds, ...
	}

	@Override
	public void performEffect(EntityLivingBase entity, int amp) {
		if (!entity.world.isRemote) {
			final float amt = 1; // Doesn't depend on amp
			
			if (entity.getHealth() < entity.getMaxHealth() && entity.getRNG().nextBoolean()) {
				// Health
				entity.heal(amt);
			} else {
				// Food
				if (entity instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) entity;
					player.getFoodStats().addStats((int) amt, 0);
				} else if (entity instanceof EntityAnimal) {
					((EntityAnimal) entity).setInLove(null);
				}
			}
		}
    }
	
	@SideOnly(Side.CLIENT)
	@Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		PotionIcon.NATURESBLESSING.draw(mc, x + 6, y + 7);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		PotionIcon.NATURESBLESSING.draw(mc, x + 3, y + 3);
	}
	
}
