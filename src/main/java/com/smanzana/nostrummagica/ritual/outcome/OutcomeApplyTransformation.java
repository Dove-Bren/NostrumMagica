package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.util.Entities;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class OutcomeApplyTransformation implements IRitualOutcome {

	private final Predicate<LivingEntity> selector;
	private final int duration;
	
	public OutcomeApplyTransformation(int duration, Predicate<LivingEntity> selector) {
		this.selector = selector;
		this.duration = duration;
	}
	
	protected @Nullable LivingEntity findEntity(World world, PlayerEntity player, BlockPos center) {
		for (LivingEntity ent : Entities.GetEntities((ServerWorld) world, (e) -> {return e.getDistanceSq(center.getX() + .5, center.getY() + .5, center.getZ() + .5) < 25;})) {
			if (this.selector.test((LivingEntity) ent)) {
				return (LivingEntity) ent;
			}
		}
		return null;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		@Nullable LivingEntity target = findEntity(world, player, center);
		if (target != null) {
			// Apply effect to the selected entity
			target.addPotionEffect(new EffectInstance(NostrumEffects.nostrumTransformation, duration, 0, true, true));
		}
	}
	
	@Override
	public String getName() {
		return "apply_transformation_effect";
	}

	@Override
	public List<ITextComponent> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.transformation_effect.desc");
	}
	
	@Override
	public boolean canPerform(World world, PlayerEntity player, BlockPos center, IRitualLayout layout) {
		if (findEntity(world, player, center) == null) {
			if (!player.world.isRemote) {
				player.sendMessage(new TranslationTextComponent("info.transformation.noentity"), Util.DUMMY_UUID);
			}
			return false;
		}
		return true;
	}
}
