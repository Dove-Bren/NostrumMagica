package com.smanzana.nostrummagica.entity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Event injected with a mixin for when an arrow is shot by a player and is about to be added o the world.
 * Much 'later' than the 'ArrowLooseEvent' in that it's when the actual arrow entity is constructed and about to be added.
 */
public class ArrowFiredEvent extends PlayerEvent {

	protected AbstractArrow arrow;
	protected ItemStack ammo;
	protected ItemStack bow;
	
	public ArrowFiredEvent(Player player, AbstractArrow arrow, ItemStack ammo, ItemStack bow) {
		super(player);
		this.arrow = arrow;
		this.bow = bow;
		this.ammo = ammo;
	}

	public AbstractArrow getArrow() {
		return arrow;
	}

	public ItemStack getAmmo() {
		return ammo;
	}

	public ItemStack getBow() {
		return bow;
	}
}
