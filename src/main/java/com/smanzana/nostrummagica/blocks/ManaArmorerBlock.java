package com.smanzana.nostrummagica.blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.ManaArmorerTileEntity;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ManaArmorerBlock extends Block {

public static final String ID = "mana_armorer";

private static final double AABB_RADIUS = (4.0 / 16.0);
protected static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(.5 - AABB_RADIUS, .5 - AABB_RADIUS, .5 - AABB_RADIUS, .5 + AABB_RADIUS, .5 + AABB_RADIUS, .5 + AABB_RADIUS);
	
	private static ManaArmorerBlock instance = null;
	public static ManaArmorerBlock instance() {
		if (instance == null)
			instance = new ManaArmorerBlock();
		
		return instance;
	}
	
	public ManaArmorerBlock() {
		super(Material.ROCK, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(1.2f);
		this.setResistance(3.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		this.setHarvestLevel("pickaxe", 1);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
		return BASE_AABB;
	}
	
	@Override
	public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_AABB);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public EnumBlockRenderType getRenderType(BlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
	@Override
	public boolean isOpaqueCube(BlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(BlockState state) {
		return false;
	}
	
	@Override
	public int getLightValue(BlockState state, IBlockAccess world, BlockPos pos) {
		return 12;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, BlockState state) {
		return new ManaArmorerTileEntity();
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) { broke();
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!worldIn.isRemote) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null && te instanceof ManaArmorerTileEntity) {
				ManaArmorerTileEntity armorer = (ManaArmorerTileEntity) te;
				@Nullable IManaArmor playerArmor = NostrumMagica.getManaArmor(playerIn);
				
				if (playerArmor != null && playerArmor.hasArmor()) {
					// Already have armor?
					NostrumMagicaSounds.CAST_FAIL.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
					playerIn.sendMessage(new TranslationTextComponent("info.mana_armorer.already_have"));
				} else if (armorer.isActive()) {
					// If we're the active entity, stop it
					// Otherwise, it's busy
					if (armorer.getActiveEntity().equals(playerIn)) {
						armorer.stop();
					} else {
						NostrumMagicaSounds.CAST_FAIL.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
						playerIn.sendMessage(new TranslationTextComponent("info.mana_armorer.busy"));
					}
				} else {
					// Else become active with us
					INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
					if (attr != null && attr.isUnlocked()
							&& attr.getMaxMana() > armorer.calcManaBurnAmt(playerIn)
							&& attr.getMana() > 0
							) {
						armorer.startEntity(playerIn);
					} else {
						NostrumMagicaSounds.CAST_FAIL.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
						playerIn.sendMessage(new TranslationTextComponent("info.mana_armorer.locked"));
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
