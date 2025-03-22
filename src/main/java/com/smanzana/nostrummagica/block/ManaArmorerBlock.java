package com.smanzana.nostrummagica.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.ManaArmorerTileEntity;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ManaArmorerBlock extends BaseEntityBlock {

	public static final String ID = "mana_armorer";

	private static final double AABB_RADIUS = (4.0);
	protected static final VoxelShape BASE_AABB = Block.box(8 - AABB_RADIUS, 8 - AABB_RADIUS, 8 - AABB_RADIUS, 8 + AABB_RADIUS, 8 + AABB_RADIUS, 8 + AABB_RADIUS);
	
	public ManaArmorerBlock() {
		super(Block.Properties.of(Material.STONE)
				.strength(1.2f, 3.0f)
				.sound(SoundType.STONE)
				.noOcclusion()
				);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return BASE_AABB;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}
	
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
//	
//	@Override
//	public boolean isFullCube(BlockState state) {
//		return false;
//	}
//	
//	@Override
//	public int getLightValue(BlockState state, IBlockAccess world, BlockPos pos) {
//		return 12;
//	}
	
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ManaArmorerTileEntity(pos, state);
	}
	
//	@Override
//	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
//		super.breakBlock(world, pos, state);
//		world.removeTileEntity(pos);
//	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (!worldIn.isClientSide) {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if (te != null && te instanceof ManaArmorerTileEntity) {
				ManaArmorerTileEntity armorer = (ManaArmorerTileEntity) te;
				@Nullable IManaArmor playerArmor = NostrumMagica.getManaArmor(player);
				
				if (playerArmor != null && playerArmor.hasArmor()) {
					// Already have armor?
					NostrumMagicaSounds.CAST_FAIL.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
					player.sendMessage(new TranslatableComponent("info.mana_armorer.already_have"), Util.NIL_UUID);
				} else if (armorer.isActive()) {
					// If we're the active entity, stop it
					// Otherwise, it's busy
					if (armorer.getActiveEntity().equals(player)) {
						armorer.stop();
					} else {
						NostrumMagicaSounds.CAST_FAIL.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
						player.sendMessage(new TranslatableComponent("info.mana_armorer.busy"), Util.NIL_UUID);
					}
				} else {
					// Else become active with us
					INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
					if (attr != null && attr.isUnlocked()
							&& attr.getMaxMana() > armorer.calcManaBurnAmt(player)
							&& attr.getMana() > 0
							) {
						armorer.startEntity(player);
					} else {
						NostrumMagicaSounds.CAST_FAIL.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
						player.sendMessage(new TranslatableComponent("info.mana_armorer.locked"), Util.NIL_UUID);
					}
				}
			}
		}
		
		return InteractionResult.SUCCESS;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		
	}
	
}
