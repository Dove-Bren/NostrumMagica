package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class OutcomeSpawnEntity implements IRitualOutcome {

	public static interface IEntityFactory {
		public void spawn(Level world, Vec3 pos, Player invoker, ItemStack centerItem);
		
		public String getEntityName();
	}
	
	protected IEntityFactory factory;
	protected int count;
	
	public OutcomeSpawnEntity(IEntityFactory factory, int count) {
		this.factory = factory;
		this.count = count;
	}
	
	@Override
	public void perform(Level world, Player player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		if (world.isClientSide)
			return;
		
		float angle = 0f;
		float interval = 360.0f / count;
		final float distance = 2f;
		for (int i = 0; i < count; i++) {
			angle = interval * i;
			Vec3 pos = new Vec3(center.getX() + .5 + Math.cos(angle) * distance,
					center.getY(),
					center.getZ() + .5 + Math.sin(angle) * distance);
			this.factory.spawn(world, pos, player, layout.getCenterItem(world, center));
		}
	}
	
	@Override
	public String getName() {
		return "spawn_entity";
	}

	@Override
	public List<Component> getDescription() {
		String name = I18n.get(factory.getEntityName(), (Object[]) null);
		return TextUtils.GetTranslatedList("ritual.outcome.spawn_entity.desc",
				new Object[] {count, name});
	}
	
}
