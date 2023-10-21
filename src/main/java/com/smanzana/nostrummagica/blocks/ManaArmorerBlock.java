package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tiles.ManaArmorerTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

public class ManaArmorerBlock extends ContainerBlock {

	public static final String ID = "mana_armorer";

	private static final double AABB_RADIUS = (4.0);
	protected static final VoxelShape BASE_AABB = Block.makeCuboidShape(8 - AABB_RADIUS, 8 - AABB_RADIUS, 8 - AABB_RADIUS, 8 + AABB_RADIUS, 8 + AABB_RADIUS, 8 + AABB_RADIUS);
	
	public ManaArmorerBlock() {
		super(Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(1.2f, 3.0f)
				.sound(SoundType.STONE)
				.harvestTool(ToolType.PICKAXE)
				.harvestLevel(1)
				);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return BASE_AABB;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
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
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new ManaArmorerTileEntity();
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return createNewTileEntity(world);
	}
	
//	@Override
//	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
//		super.breakBlock(world, pos, state);
//		world.removeTileEntity(pos);
//	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!worldIn.isRemote) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null && te instanceof ManaArmorerTileEntity) {
				ManaArmorerTileEntity armorer = (ManaArmorerTileEntity) te;
				@Nullable IManaArmor playerArmor = NostrumMagica.getManaArmor(player);
				
				if (playerArmor != null && playerArmor.hasArmor()) {
					// Already have armor?
					NostrumMagicaSounds.CAST_FAIL.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
					player.sendMessage(new TranslationTextComponent("info.mana_armorer.already_have"));
				} else if (armorer.isActive()) {
					// If we're the active entity, stop it
					// Otherwise, it's busy
					if (armorer.getActiveEntity().equals(player)) {
						armorer.stop();
					} else {
						NostrumMagicaSounds.CAST_FAIL.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
						player.sendMessage(new TranslationTextComponent("info.mana_armorer.busy"));
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
						player.sendMessage(new TranslationTextComponent("info.mana_armorer.locked"));
					}
				}
			}
		}
		
		return true;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		
	}
	
}
