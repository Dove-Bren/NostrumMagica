package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class OutcomeSpawnEntity implements IRitualOutcome {

	public static interface IEntityFactory {
		public void spawn(World world, Vector3d pos, PlayerEntity invoker, ItemStack centerItem);
		
		public String getEntityName();
	}
	
	protected IEntityFactory factory;
	protected int count;
	
	public OutcomeSpawnEntity(IEntityFactory factory, int count) {
		this.factory = factory;
		this.count = count;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		if (world.isRemote)
			return;
		
		float angle = 0f;
		float interval = 360.0f / count;
		final float distance = 2f;
		for (int i = 0; i < count; i++) {
			angle = interval * i;
			Vector3d pos = new Vector3d(center.getX() + .5 + Math.cos(angle) * distance,
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
	public List<ITextComponent> getDescription() {
		String name = I18n.format(factory.getEntityName(), (Object[]) null);
		return TextUtils.GetTranslatedList("ritual.outcome.spawn_entity.desc",
				new Object[] {count, name});
	}
	
}
