package com.smanzana.nostrummagica.block;

import java.util.Random;
import java.util.function.Consumer;

import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.tile.NostrumBlockEntities;
import com.smanzana.nostrummagica.tile.TeleportRuneTileEntity;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ShortcutRuneBlock extends TeleportRuneBlock  {
	
	public static final String ID = "shortcut_rune";
	
	public ShortcutRuneBlock() {
		super();
	}
	
	protected boolean doTeleport(Level level, BlockState state, BlockPos pos, Entity entity) {
		BlockEntity te = level.getBlockEntity(pos);
		if (te == null || !(te instanceof TeleportRuneTileEntity ent)) {
			return false;
		}
		ent.doTeleport(entity, true);
		return true;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		ItemStack heldItem = playerIn.getItemInHand(hand);
		if (playerIn.isCreative() && !heldItem.isEmpty()) {
			return super.use(state, worldIn, pos, playerIn, hand, hit);
		}
		
		// Normal adventure interaction will just be teleporting if possible. No setting
		
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof TeleportRuneTileEntity)) {
			return InteractionResult.SUCCESS;
		}
		
		if (heldItem.isEmpty() || !(heldItem.getItem() instanceof PositionCrystal)) {
			if (!worldIn.isClientSide) {
				doTeleport(worldIn, state, pos, playerIn);
			}
			return InteractionResult.SUCCESS;
		}
		
		return InteractionResult.PASS;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TeleportRuneTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, NostrumBlockEntities.TeleportRune);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof TeleportRuneTileEntity)) {
			return;
		}
		
		TeleportRuneTileEntity ent = (TeleportRuneTileEntity) te;
		if (ent.getOffset() != null) {
			double dx = pos.getX() + .5;
			double dy = pos.getY() + .1;
			double dz = pos.getZ() + .5;
			
			double mx = 1 * (rand.nextFloat() - .5f);
			double mz = 1 * (rand.nextFloat() - .5f);
			
			worldIn.addParticle(ParticleTypes.PORTAL, dx + mx, dy, dz + mz, mx / 3, 0.0D, mz / 3);
		}
	}

	@Override
	public void visitBridge(BlockGetter level, BlockState state, BlockPos pos, BlockEntity ent, Consumer<BlockPos> addBlock) {
		; // do not have scanning follow shortcut runes
	}
}
