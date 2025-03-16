package com.smanzana.nostrummagica.integration.curios.items;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.IDragonWingRenderItem;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID)
public class DragonWingPendantItem extends NostrumCurio implements IDragonWingRenderItem, IColorableCurio {
	
	public static final String ID = "dragon_wing_pendant";

	public DragonWingPendantItem() {
		super(NostrumCurios.PropCurio(), ID);
	}
	
	@SubscribeEvent
	public static void onJump(LivingJumpEvent event) {
		// Dragonwing Pendant gives boost
		if (event.isCanceled()) {
			return;
		}

		final LivingEntity ent = event.getEntityLiving();
		if (!(ent instanceof Player)) {
			return;
		}
		
		Player player = (Player) ent;
		Container inv = NostrumMagica.instance.curios.getCurios(player);
		if (inv != null) {
			for (int i = 0; i < inv.getContainerSize(); i++) {
				ItemStack stack = inv.getItem(i);
				if (stack.isEmpty() || !(stack.getItem() instanceof DragonWingPendantItem))
					continue;
				
				// Jump-boost gives an extra .1 per level.
				ent.setDeltaMovement(ent.getDeltaMovement().add(0, .2, 0));
			}
		}
		
	}
	
	@SubscribeEvent
	public static void onFall(LivingFallEvent event) {
		// Dragonwing Pendant gives jump boost and reduces fall damage
		if (event.isCanceled()) {
			return;
		}

		final LivingEntity ent = event.getEntityLiving();
		if (!(ent instanceof Player)) {
			return;
		}
		
		Player player = (Player) ent;
		Container inv = NostrumMagica.instance.curios.getCurios(player);
		if (inv != null) {
			for (int i = 0; i < inv.getContainerSize(); i++) {
				ItemStack stack = inv.getItem(i);
				if (stack.isEmpty() || !(stack.getItem() instanceof DragonWingPendantItem))
					continue;
				
				// Jump-boost gives an extra .1 per level.
				final float reduc = 2f;
				event.setDistance(Math.max(0f, event.getDistance() - reduc));
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderDragonWings(ItemStack stack, Player player) {
		return true;
	}
	
	@Override
	public int getDragonWingColor(ItemStack stack, Player player) {
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
	
	@Override
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
	
	@Override
	public void setEmbeddedElement(ItemStack stack, EMagicElement element) {
		if (stack.isEmpty()) {
			return;
		}
		
		if (!(stack.getItem() instanceof DragonWingPendantItem)) {
			return;
		}
		
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
		}
		
		nbt.putString("element", element.name());
		
		stack.setTag(nbt);
	}

}
