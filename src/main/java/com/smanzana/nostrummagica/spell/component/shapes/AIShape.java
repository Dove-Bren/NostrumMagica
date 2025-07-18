package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

/**
 * Instantly affects the entity being targetted (attacktarget) by the caster.
 * This will not work for a player. It's made of AI spells
 * @author Skyler
 *
 */
public class AIShape extends InstantShape {

	private static final String ID = "ai";
	
	public AIShape() {
		super(ID);
	}
	
	@Override
	protected TriggerData getTargetData(ISpellState state, LivingEntity entity,
			SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		LivingEntity target = state.getCaster(); // defult to caster. That's what you get for using a trigger for AI!
		if (state.getCaster() instanceof Mob) {
			target = ((Mob) state.getCaster()).getTarget();
		}
		
		if (target != null) {
			return new TriggerData(Lists.newArrayList(target), null);
		} else {
			return new TriggerData(null, null);
		}
	}
	
	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 0;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.create();
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.BEDROCK);
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 99;
	}

	@Override
	public boolean shouldTrace(Player player, SpellShapeProperties params) {
		return false;
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return false;
	}
	
	@Override
	public boolean canIncant() {
		return false;
	}
}
