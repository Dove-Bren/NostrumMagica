package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

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
		if (state.getCaster() instanceof MobEntity) {
			target = ((MobEntity) state.getCaster()).getTarget();
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
	public boolean shouldTrace(PlayerEntity player, SpellShapeProperties params) {
		return false;
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return false;
	}
}
