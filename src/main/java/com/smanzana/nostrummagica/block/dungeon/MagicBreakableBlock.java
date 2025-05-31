package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.block.ISpellTargetBlock;
import com.smanzana.nostrummagica.block.property.MagicElementProperty;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.util.WorldUtil;
import com.smanzana.nostrummagica.util.WorldUtil.IBlockWalker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MagicBreakableBlock extends Block implements ISpellTargetBlock {

	public static final String ID = "mechblock_break";
	
	public static final MagicElementProperty ELEMENT = MagicElementProperty.create("element");
	
	public MagicBreakableBlock() {
		super(Block.Properties.of(Material.GLASS)
				.strength(-1.0F, 3600000.8F)
				.sound(SoundType.GLASS)
				.noOcclusion()
				.noDrops()
				);
		
		this.registerDefaultState(this.defaultBlockState().setValue(ELEMENT, EMagicElement.PHYSICAL));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(ELEMENT);
	}
	
	public EMagicElement getElement(BlockState state) {
		return state.getValue(ELEMENT);
	}
	
	public BlockState setElement(BlockState state, EMagicElement element) {
		return state.setValue(ELEMENT, element);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		final EMagicElement element = getElement(state);
		return adjacentBlockState.getBlock() instanceof MagicBreakableBlock
				&& ((MagicBreakableBlock) adjacentBlockState.getBlock()).getElement(adjacentBlockState) == element;
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return true;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (!playerIn.isCreative()) {
			return InteractionResult.PASS;
		}
		
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		
		if (worldIn.isClientSide) {
			return InteractionResult.PASS;
		}
		
		final ItemStack heldItem = playerIn.getItemInHand(hand);
		if (!heldItem.isEmpty() && heldItem.getItem() instanceof InfusedGemItem gem) {
			final EMagicElement element = gem.getElement();
			worldIn.setBlockAndUpdate(pos, setElement(state, element));
			return InteractionResult.SUCCESS;
		}
		
		return InteractionResult.PASS;
	}
	
	protected void trigger(Level world, BlockPos blockPos, BlockState state) {
		if (!world.isClientSide()) {
			final EMagicElement originalElement = getElement(state);
			WorldUtil.WalkConnectedBlocks(world, blockPos, new IBlockWalker() {
				@Override
				public boolean canVisit(BlockGetter world, BlockPos startPos, BlockState startState, BlockPos pos,
						BlockState state, int distance) {
					return state.getBlock() == MagicBreakableBlock.this
							&& getElement(state) == originalElement;
				}

				@Override
				public boolean walk(BlockGetter world, BlockPos startPos, BlockState startState, BlockPos pos,
						BlockState state, int distance, int walkCount) {
					((MagicBreakableBlock) state.getBlock()).triggerInternal((Level) world, pos, state);
					return false;
				}
			}, 256);
		}
	}
	
	protected void triggerInternal(Level world, BlockPos blockPos, BlockState state) {
		// Remove ourselves when triggered
		world.destroyBlock(blockPos, false);
	}

	@Override
	public boolean processSpellEffect(Level level, BlockState state, BlockPos pos, LivingEntity caster,
			SpellLocation hitLocation, SpellEffectPart effect, SpellAction action) {
		// Blocks require INFLICT to break with matching elements
		final EMagicElement element = getElement(state);
		if (effect.getElement() == element
				&& effect.getAlteration() == EAlteration.INFLICT) {
			this.trigger(level, pos, state);
			return true;
		}
		
		return false;
	}
	
	public static final int MakeBlockColor(BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
		if (state.getBlock() instanceof MagicBreakableBlock block) {
			return block.getElement(state).getColor();
		}
		return 0xFFFFFFFF;
	}
}
