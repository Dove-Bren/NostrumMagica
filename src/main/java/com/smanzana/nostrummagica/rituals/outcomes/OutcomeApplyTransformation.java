package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.potions.NostrumTransformationPotion;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRecipe.RitualMatchInfo;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class OutcomeApplyTransformation implements IRitualOutcome {

	private final Predicate<EntityLivingBase> selector;
	private final int duration;
	
	public OutcomeApplyTransformation(int duration, Predicate<EntityLivingBase> selector) {
		this.selector = selector;
		this.duration = duration;
	}
	
	protected @Nullable EntityLivingBase findEntity(World world, EntityPlayer player, BlockPos center) {
		for (Entity ent : world.loadedEntityList) {
			if (ent instanceof EntityLivingBase
					&& ent.getDistanceSq(center) < 25
					&& this.selector.test((EntityLivingBase) ent)) {
				return (EntityLivingBase) ent;
			}
		}
		return null;
	}
	
	@Override
	public void perform(World world, EntityPlayer player, ItemStack centerItem, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe) {
		@Nullable EntityLivingBase target = findEntity(world, player, center);
		if (target != null) {
			// Apply effect to the selected entity
			target.addPotionEffect(new PotionEffect(NostrumTransformationPotion.instance(), duration, 0, true, true));
		}
	}
	
	@Override
	public String getName() {
		return "apply_transformation_effect";
	}

	@Override
	public List<String> getDescription() {
		return Lists.newArrayList(I18n.format("ritual.outcome.transformation_effect.desc")
				.split("\\|"));
	}
	
	@Override
	public boolean canPerform(World world, EntityPlayer player, BlockPos center, RitualMatchInfo ingredients) {
		if (findEntity(world, player, center) == null) {
			if (!player.world.isRemote) {
				player.sendMessage(new TextComponentTranslation("info.transformation.noentity"));
			}
			return false;
		}
		return true;
	}
}
