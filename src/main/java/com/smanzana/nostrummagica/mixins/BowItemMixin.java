package com.smanzana.nostrummagica.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.smanzana.nostrummagica.entity.ArrowFiredEvent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

@Mixin(BowItem.class)
public class BowItemMixin extends Item {

	public BowItemMixin(Properties p_41383_) {
		super(p_41383_);
	}
	
	//                               Lnet/minecraft/world/level/LevelWriter;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z
	//at=@At(value="INVOKE", target="Lnet/minecraft/world/level/LevelWriter;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
	@Inject(method = "releaseUsing", at=@At(value="INVOKE", target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", args="log=true"), locals=LocalCapture.CAPTURE_FAILHARD)
	public void onArrowFired(ItemStack bow, Level level, LivingEntity user, int idk, CallbackInfo ci, Player player, boolean flag, ItemStack itemstack, int i, float f, boolean flag1, ArrowItem arrowitem, AbstractArrow abstractarrow) {
		ArrowFiredEvent event = new ArrowFiredEvent(player, abstractarrow, itemstack, bow);
		MinecraftForge.EVENT_BUS.post(event);
	}

}
