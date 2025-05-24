package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.block.ISpellTargetBlock;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.property.MagicElementProperty;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.tile.ConjureGhostBlockEntity;
import com.smanzana.nostrummagica.util.WorldUtil;
import com.smanzana.nostrummagica.util.WorldUtil.IBlockWalker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Block that is a different block, but that needs a spell effect to make it actually conjure the other block.
 * For example, a ghost ladder block that can't be used until activated.
 */
public class ConjureGhostBlock extends BaseEntityBlock implements ISpellTargetBlock {

	public static final String ID = "mechblock_ghost";
	
	public static final MagicElementProperty ELEMENT = MagicElementProperty.create("element");
	
	public ConjureGhostBlock() {
		super(Block.Properties.of(Material.GLASS)
			.strength(-1.0F, 3600000.8F)
			.sound(SoundType.GLASS)
			.noOcclusion()
			.noDrops()
			.noCollission()
			);
	
		this.registerDefaultState(this.defaultBlockState().setValue(ELEMENT, EMagicElement.PHYSICAL));
	}
	
	protected BlockState getGhostState(BlockState state, BlockGetter level, BlockPos pos) {
		BlockEntity ent = level.getBlockEntity(pos);
		if (ent == null || !(ent instanceof ConjureGhostBlockEntity blockentity)) {
			return Blocks.STONE.defaultBlockState();
		}
		
		return blockentity.getGhostState();
	}
	
	@Override
	public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return getGhostState(state, level, pos).getInteractionShape(level, pos);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return getGhostState(state, level, pos).getShape(level, pos, context);
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return getGhostState(state, level, pos).getShape(level, pos, context);
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
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
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
					return state.getBlock() == ConjureGhostBlock.this
							&& getElement(state) == originalElement;
				}

				@Override
				public boolean walk(BlockGetter world, BlockPos startPos, BlockState startState, BlockPos pos,
						BlockState state, int distance, int walkCount) {
					((ConjureGhostBlock) state.getBlock()).triggerInternal((Level) world, pos, state);
					return false;
				}
			}, 256);
		}
	}
	
	protected void triggerInternal(Level world, BlockPos blockPos, BlockState state) {
		// MAke the block entity process it since it's what's tracking the actual new block state
		BlockEntity ent = world.getBlockEntity(blockPos);
		if (ent == null || !(ent instanceof ConjureGhostBlockEntity blockentity)) {
			return;
		}
		
		final BlockState ghoststate = blockentity.getGhostState();
		blockentity.spawnBlock();
		
		// And since we're being replaced, spawn some fx manually
		((ServerLevel) world).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ghoststate),
				blockPos.getX() + .5, blockPos.getY() + .5, blockPos.getZ() + .5,
				100,
				.5, .5, .5,
				.15f);
		world.playSound(null, blockPos, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 1f, 2f);
	}

	@Override
	public boolean processSpellEffect(Level level, BlockState state, BlockPos pos, LivingEntity caster,
			SpellLocation hitLocation, Spell spell, SpellAction action) {
		// Blocks require CONJURE with matching elements
		final EMagicElement element = getElement(state);
		for (SpellEffectPart part : spell.getSpellEffectParts()) {
			if (part.getElement() == element
					&& part.getAlteration() == EAlteration.CONJURE) {
				this.trigger(level, pos, state);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ConjureGhostBlockEntity(pos, state);
	}
	
	public static final int MakeBlockColor(BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
		if (state.getBlock() instanceof ConjureGhostBlock block) {
			return block.getElement(state).getColor();
		}
		return 0xFFFFFFFF;
	}
	
	public static boolean WrapBlock(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		world.setBlock(pos, NostrumBlocks.conjureGhostBlock.defaultBlockState(), 3);
			
		ConjureGhostBlockEntity container = (ConjureGhostBlockEntity) world.getBlockEntity(pos);
		container.setContainedState(state);
		return true;
	}
}
