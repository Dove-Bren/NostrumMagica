package com.smanzana.nostrummagica.block.dungeon;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.TriggerRepeaterTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(POWERED, ONCE);
	}
	
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (worldIn.isClientSide() && NostrumMagica.instance.proxy.getPlayer() != null && NostrumMagica.instance.proxy.getPlayer().isCreative()) {
			worldIn.addParticle(ParticleTypes.BARRIER, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 0, 0, 0);
		}
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader reader) {
		return new TriggerRepeaterTileEntity();
	}

	@Override
	public void trigger(World world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		TileEntity te = world.getBlockEntity(blockPos);
		if (te instanceof TriggerRepeaterTileEntity) {
			((TriggerRepeaterTileEntity) te).trigger(triggerPos);
		}
	}
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (!worldIn.isClientSide() && playerIn.isCreative()) {
		
			ItemStack heldItem = playerIn.getItemInHand(hand);
			
			if (!heldItem.isEmpty() && heldItem.getItem() == Items.LEVER) {
				// Toggle 'once'
				final boolean newOnce = !state.getValue(ONCE);
				worldIn.setBlockAndUpdate(pos, state.setValue(ONCE, newOnce));
				playerIn.sendMessage(new StringTextComponent("Changed to " + (newOnce ? "ONCE" : "REPEATABLE")), Util.NIL_UUID);
				NostrumMagicaSounds.CAST_CONTINUE.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
				return ActionResultType.SUCCESS;
			}
		}
		
		return super.use(state, worldIn, pos, playerIn, hand, hit);
	}
	
	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
		return true;
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
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
