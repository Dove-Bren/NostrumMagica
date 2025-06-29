package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.capabilities.ILaserReactive;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ElementalStoneBlock extends HalfTransparentBlock implements ILaserReactive, ISpellTargetBlock {
	
	protected static final String ID_FIRE_STONE = "fire_stone";
	protected static final String ID_ICE_STONE = "ice_stone";
	protected static final String ID_WIND_STONE = "wind_stone";
	protected static final String ID_EARTH_STONE = "earth_stone";
	protected static final String ID_LIGHTNING_STONE = "lightning_stone";
	protected static final String ID_ENDER_STONE = "ender_stone";

	public final EMagicElement element;
	
	public ElementalStoneBlock(Block.Properties props, EMagicElement element) {
		super(props);
		this.element = element;
	}
	
	public EMagicElement getElement() {
		return element;
	}

	@Override
	public LaserHitResult laserPassthroughTick(LevelAccessor level, BlockPos pos, BlockState state, BlockPos laserPos, EMagicElement element) {
		if (element == this.getElement()) {
			return LaserHitResult.PASSTHROUGH;
		}
		return LaserHitResult.BLOCK;
	}

	@Override
	public void laserNearbyTick(LevelAccessor level, BlockPos pos, BlockState state, BlockPos laserPos, EMagicElement element, int beamDistance) {
		
	}

	@Override
	public boolean processSpellEffect(Level level, BlockState state, BlockPos pos, LivingEntity caster, SpellLocation hitLocation, SpellEffectPart effect, SpellAction action) {
		// Blocks require ENCHANT with different elements
		final EMagicElement element = getElement();
		if (effect.getElement() != element && effect.getElement() != EMagicElement.PHYSICAL
				&& effect.getAlteration() == EAlteration.ENCHANT) {
			level.setBlockAndUpdate(pos, NostrumBlocks.elementalStone(effect.getElement()).defaultBlockState());
			return true;
		}
		
		return false;
	}
	
	
	
}
