package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class OutcomeSpawnEntity implements IRitualOutcome {

	public static interface IEntityFactory {
		public void spawn(World world, Vec3d pos, PlayerEntity invoker, ItemStack centerItem);
		
		public String getEntityName();
	}
	
	protected IEntityFactory factory;
	protected int count;
	
	public OutcomeSpawnEntity(IEntityFactory factory, int count) {
		this.factory = factory;
		this.count = count;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, ItemStack centerItem, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe) {
		if (world.isRemote)
			return;
		
		float angle = 0f;
		float interval = 360.0f / count;
		final float distance = 2f;
		for (int i = 0; i < count; i++) {
			angle = interval * i;
			Vec3d pos = new Vec3d(center.getX() + .5 + Math.cos(angle) * distance,
					center.getY(),
					center.getZ() + .5 + Math.sin(angle) * distance);
			this.factory.spawn(world, pos, player, centerItem);
		}
	}
	
	@Override
	public String getName() {
		return "spawn_entity";
	}

	@Override
	public List<String> getDescription() {
		String name = I18n.format(factory.getEntityName(), (Object[]) null);
		return Lists.newArrayList(I18n.format("ritual.outcome.spawn_entity.desc",
				new Object[] {count, name})
				.split("\\|"));
	}
	
}
