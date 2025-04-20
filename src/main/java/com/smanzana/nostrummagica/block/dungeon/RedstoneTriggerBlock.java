package com.smanzana.nostrummagica.block.dungeon;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.TriggerRepeaterTileEntity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/**
 * Block that fires a trigger event when receiving redstone input.
 * @author Skyler
 *
 */
public class RedstoneTriggerBlock extends TriggerRepeaterBlock {

	public static final String ID = "redstone_trigger";
	
	public static final BooleanProperty POWERED = BooleanProperty.create("powered");
	public static final BooleanProperty ONCE = BooleanProperty.create("once");
	
	public RedstoneTriggerBlock() {
		super();
		this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false).setValue(ONCE, false));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(POWERED, ONCE);
	}
	
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		if (worldIn.isClientSide() && NostrumMagica.instance.proxy.getPlayer() != null && NostrumMagica.instance.proxy.getPlayer().isCreative()) {
			worldIn.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, Blocks.BARRIER.defaultBlockState()), pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 0, 0, 0);
		}
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TriggerRepeaterTileEntity(pos, state);
	}

	@Override
	public void trigger(Level world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		BlockEntity te = world.getBlockEntity(blockPos);
		if (te instanceof TriggerRepeaterTileEntity) {
			((TriggerRepeaterTileEntity) te).trigger(triggerPos);
		}
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (!worldIn.isClientSide() && playerIn.isCreative()) {
		
			ItemStack heldItem = playerIn.getItemInHand(hand);
			
			if (!heldItem.isEmpty() && heldItem.getItem() == Items.LEVER) {
				// Toggle 'once'
				final boolean newOnce = !state.getValue(ONCE);
				worldIn.setBlockAndUpdate(pos, state.setValue(ONCE, newOnce));
				playerIn.sendMessage(new TextComponent("Changed to " + (newOnce ? "ONCE" : "REPEATABLE")), Util.NIL_UUID);
				NostrumMagicaSounds.CAST_CONTINUE.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
				return InteractionResult.SUCCESS;
			}
		}
		
		return super.use(state, worldIn, pos, playerIn, hand, hit);
	}
	
	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
		return true;
	}
	
	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if (!worldIn.isClientSide()) {
			final boolean isPowered = state.getValue(POWERED);
			final boolean once = state.getValue(ONCE);
			
			final boolean worldPowered = worldIn.hasNeighborSignal(pos);
			if (!isPowered && worldPowered) {
				// Regardless of once, turn on. We always respond to going from not powered to powered
				worldIn.setBlockAndUpdate(pos, state.setValue(POWERED, true));
				this.trigger(worldIn, pos, state, fromPos);
				return;
			}
			
			if (isPowered && !worldPowered && !once) {
				worldIn.setBlockAndUpdate(pos, state.setValue(POWERED, false));
			}
		}
	}
}
