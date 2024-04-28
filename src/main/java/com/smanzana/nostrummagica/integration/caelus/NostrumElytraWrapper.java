package com.smanzana.nostrummagica.integration.caelus;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.client.render.LayerAetherCloak;
import com.smanzana.nostrummagica.items.ICapeProvider;
import com.smanzana.nostrummagica.items.IElytraRenderer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.caelus.api.CaelusApi;
import top.theillusivec4.caelus.api.RenderElytraEvent;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class NostrumElytraWrapper {
	
	// Make a modifier with a random UUID that turns the elytra ON
	public static final AttributeModifier MakeHasElytraModifier() {
		return MakeHasElytraModifier(UUID.randomUUID(), true);
	}
	
	// Make a modifier with a random UUID that does or doesn't turn the elytra on
	public static final AttributeModifier MakeHasElytraModifier(boolean on) {
		return MakeHasElytraModifier(UUID.randomUUID(), on);
	}

	// Make a modifier with the given UUID that turns the elytra ON
	public static final AttributeModifier MakeHasElytraModifier(@Nonnull UUID id) {
		return MakeHasElytraModifier(id,  true);
	}
	
	// 
	public static final AttributeModifier MakeHasElytraModifier(@Nonnull UUID id, boolean on) {
		return new AttributeModifier(id, "NostrumHasElytraOverride", on ? 1.0 : 0.0, AttributeModifier.Operation.ADDITION);
	}
	
	public static final void AddElytraModifier(LivingEntity entity, AttributeModifier modifier) {
		final ModifiableAttributeInstance inst = entity.getAttribute(CaelusApi.ELYTRA_FLIGHT.get());
		if (inst == null) {
			throw new RuntimeException("Caelus is required for NostrumMagica, but no Caelus attribute found.");
		}
		
		if (!inst.hasModifier(modifier)) {
			inst.applyPersistentModifier(modifier);
		}
	}
	
	public static final void AddElytraModifier(Multimap<Attribute, AttributeModifier> map, AttributeModifier modifier) {
		map.put(CaelusApi.ELYTRA_FLIGHT.get(), modifier);
	}
	
	public static final void RemoveElytraModifier(LivingEntity entity, AttributeModifier modifier) {
		final ModifiableAttributeInstance inst = entity.getAttribute(CaelusApi.ELYTRA_FLIGHT.get());
		if (inst == null) {
			throw new RuntimeException("Caelus is required for NostrumMagica, but no Caelus attribute found.");
		}
		
		if (inst.hasModifier(modifier)) { // remove doesn't cause an exception, but flags for update. So check first to avoid dirtying.
			inst.removeModifier(modifier);
		}
	}
	
	public static final void RemoveElytraModifier(Multimap<Attribute, AttributeModifier> map, AttributeModifier modifier) {
		map.remove(CaelusApi.ELYTRA_FLIGHT.get(), modifier);
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onRenderElytra(RenderElytraEvent event) {
		
		// Cancel if a cape is specifically suppressing it
		final LivingEntity entity = event.getEntityLiving();
		final boolean flying = entity.isElytraFlying();
		ItemStack cape = LayerAetherCloak.ShouldRender(entity);
		if (!flying && !cape.isEmpty() && ((ICapeProvider) cape.getItem()).shouldPreventOtherRenders(entity, cape)) {
			event.setRender(false);
			event.setEnchanted(false);
			return;
		}
		
		// Everything from here can only turn ON rendering, so if it's already on go ahead and skip the work.
		if (event.canRender()) {
			return;
		}
		
		// Check if any equipment uses our interface for things that want to render an elytra
		for (@Nonnull ItemStack stack : entity.getEquipmentAndArmor()) {
			if (!stack.isEmpty() && stack.getItem() instanceof IElytraRenderer) {
				if (((IElytraRenderer) stack.getItem()).shouldRenderElyta(entity, stack)) {
					event.setRender(true);
					event.setEnchanted(true);
					return;
				}
			}
		}
	}
}
