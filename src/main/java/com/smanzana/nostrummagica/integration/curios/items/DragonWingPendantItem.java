package com.smanzana.nostrummagica.integration.curios.items;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.IDragonWingRenderItem;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DragonWingPendantItem extends NostrumCurio implements IDragonWingRenderItem {
	
	public static final String ID = "dragon_wing_pendant";

	public DragonWingPendantItem() {
		super(NostrumCurios.PropCurio(), ID);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onJump(LivingJumpEvent event) {
		// Dragonwing Pendant gives boost
		if (event.isCanceled()) {
			return;
		}

		final LivingEntity ent = event.getEntityLiving();
		if (!(ent instanceof PlayerEntity)) {
			return;
		}
		
		PlayerEntity player = (PlayerEntity) ent;
		IInventory inv = NostrumMagica.instance.curios.getCurios(player);
		if (inv != null) {
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (stack.isEmpty() || !(stack.getItem() instanceof DragonWingPendantItem))
					continue;
				
				// Jump-boost gives an extra .1 per level.
				ent.setMotion(ent.getMotion().add(0, .2, 0));
			}
		}
		
	}
	
	@SubscribeEvent
	public void onFall(LivingFallEvent event) {
		// Dragonwing Pendant gives jump boost and reduces fall damage
		if (event.isCanceled()) {
			return;
		}

		final LivingEntity ent = event.getEntityLiving();
		if (!(ent instanceof PlayerEntity)) {
			return;
		}
		
		PlayerEntity player = (PlayerEntity) ent;
		IInventory inv = NostrumMagica.instance.curios.getCurios(player);
		if (inv != null) {
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (stack.isEmpty() || !(stack.getItem() instanceof DragonWingPendantItem))
					continue;
				
				// Jump-boost gives an extra .1 per level.
				final float reduc = 2f;
				event.setDistance(Math.max(0f, event.getDistance() - reduc));
			}
		}
	}

	@Override
	public boolean shouldRenderDragonWings(ItemStack stack, PlayerEntity player) {
		return true;
	}
	
	@Override
	public int getDragonWingColor(ItemStack stack, PlayerEntity player) {
		if (stack.isEmpty()) {
			return 0xFFFFFFFF;
		}
		
		if (!(stack.getItem() instanceof DragonWingPendantItem)) {
			return 0xFFFFFFFF;
		}
		
		EMagicElement elem = this.getEmbeddedElement(stack);
		if (elem == null) {
			elem = EMagicElement.PHYSICAL;
		}
		
		return elem.getColor();
	}
	
	public @Nullable EMagicElement getEmbeddedElement(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		
		if (!(stack.getItem() instanceof DragonWingPendantItem)) {
			return null;
		}
		
		if (stack.getTag() == null || !stack.getTag().contains("element", NBT.TAG_STRING)) {
			return null;
		}
		
		String name = stack.getTag().getString("element");
		EMagicElement ret = null;
		try {
			ret = EMagicElement.valueOf(name.toUpperCase());
		} catch (Exception e) {
			ret = EMagicElement.PHYSICAL;
		}
		
		return ret;
	}
	
	public void setEmbeddedElement(ItemStack stack, EMagicElement element) {
		if (stack.isEmpty()) {
			return;
		}
		
		if (!(stack.getItem() instanceof DragonWingPendantItem)) {
			return;
		}
		
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		nbt.putString("element", element.name());
		
		stack.setTag(nbt);
	}

}
