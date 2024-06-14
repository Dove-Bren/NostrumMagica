package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

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
	protected TriggerData getTargetData(com.smanzana.nostrummagica.spell.Spell.ISpellState state, World world,
			Vector3d pos, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		LivingEntity target = state.getCaster(); // defult to caster. That's what you get for using a trigger for AI!
		if (state.getCaster() instanceof MobEntity) {
			target = ((MobEntity) state.getCaster()).getAttackTarget();
		}
		return new TriggerData(Lists.newArrayList(target), world, null);
	}
	
	@Override
	public int getManaCost() {
		return 0;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.create();
	}

	@Override
	public String getDisplayName() {
		return "AI";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.BEDROCK);
	}

	@Override
	public boolean supportsBoolean() {
		return false;
	}

	@Override
	public float[] supportedFloats() {
		return null;
	}

	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		return null;
	}

	@Override
	public String supportedBooleanName() {
		return null;
	}

	@Override
	public String supportedFloatName() {
		return null;
	}
	
	@Override
	public int getWeight() {
		return 99;
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapePartProperties params) {
		return false;
	}

	@Override
	public SpellShapeInstance createInstance(ISpellState state, World world, Vector3d pos, float pitch, float yaw,
			SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean supportsPreview(SpellShapePartProperties params) {
		return false;
	}
}
