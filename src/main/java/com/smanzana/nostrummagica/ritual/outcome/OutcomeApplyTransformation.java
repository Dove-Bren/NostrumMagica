package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.util.Entities;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class OutcomeApplyTransformation implements IRitualOutcome {

	private final Predicate<LivingEntity> selector;
	private final int duration;
	
	public OutcomeApplyTransformation(int duration, Predicate<LivingEntity> selector) {
		this.selector = selector;
		this.duration = duration;
	}
	
	protected @Nullable LivingEntity findEntity(Level world, Player player, BlockPos center) {
		for (LivingEntity ent : Entities.GetEntities((ServerLevel) world, (e) -> {return e.distanceToSqr(center.getX() + .5, center.getY() + .5, center.getZ() + .5) < 25;})) {
			if (this.selector.test((LivingEntity) ent)) {
				return (LivingEntity) ent;
			}
		}
		return null;
	}
	
	@Override
	public void perform(Level world, Player player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		@Nullable LivingEntity target = findEntity(world, player, center);
		if (target != null) {
			// Apply effect to the selected entity
			target.addEffect(new MobEffectInstance(NostrumEffects.nostrumTransformation, duration, 0, true, true));
		}
	}
	
	@Override
	public String getName() {
		return "apply_transformation_effect";
	}

	@Override
	public List<Component> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.transformation_effect.desc");
	}
	
	@Override
	public boolean canPerform(Level world, Player player, BlockPos center, IRitualLayout layout) {
		if (findEntity(world, player, center) == null) {
			if (!player.level.isClientSide) {
				player.sendMessage(new TranslatableComponent("info.transformation.noentity"), Util.NIL_UUID);
			}
			return false;
		}
		return true;
	}
}
