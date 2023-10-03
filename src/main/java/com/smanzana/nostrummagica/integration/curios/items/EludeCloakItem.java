package com.smanzana.nostrummagica.integration.curios.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.baubles.items.ItemMagicBauble.ItemType;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EludeCloakItem extends NostrumCurio {

	public static final String ID = "elude_cloak";
	
	public EludeCloakItem() {
		super(NostrumCurios.PropCurio(), ID);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onAttack(LivingAttackEvent event) {
		if (event.isCanceled()) {
			return;
		}
		
		if (event.getAmount() > 0f && event.getEntityLiving() instanceof PlayerEntity && event.getSource() instanceof EntityDamageSource) {
			Entity source = ((EntityDamageSource) event.getSource()).getTrueSource();
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			IInventory inv = NostrumMagica.instance.curios.getCurios(player);
			if (inv != null) {
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					ItemStack stack = inv.getStackInSlot(i);
					if (stack.isEmpty() || !(stack.getItem() instanceof EludeCloakItem))
						continue;
						
					float chance = .15f;
					int cost = 150;
					
					// Check to see if we're facing the enemy that attacked us
					Vec3d attackFrom = source.getPositionVector().subtract(player.getPositionVector());
					double attackFromYaw = -Math.atan2(attackFrom.x, attackFrom.z) * 180.0F / (float)Math.PI;
					
					if (Math.abs(((player.rotationYaw + 360f) % 360f) - ((attackFromYaw + 360f) % 360f)) < 30f) {
						if (NostrumMagica.rand.nextFloat() < chance) {
							// If there's aether, dodge!
							int taken = APIProxy.drawFromInventory(player.world, player, player.inventory, cost, stack);
							if (taken > 0) {
								// Dodge!
								event.setCanceled(true);
								NostrumMagicaSounds.DAMAGE_WIND.play(player.world, player.posX, player.posY, player.posZ);
								float dir = player.rotationYaw + (NostrumMagica.rand.nextBoolean() ? -1 : 1) * 90f;
								float velocity = .5f;
								player.getMotion().x = velocity * MathHelper.cos(dir);
								player.getMotion().z = velocity * MathHelper.sin(dir);
								player.velocityChanged = true;
							}
						}
					}
				}
			}
		}
	}
	
}
