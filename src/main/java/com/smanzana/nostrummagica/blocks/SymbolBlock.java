package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.SymbolTileEntity;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SymbolBlock extends Block implements ITileEntityProvider {
	
	public static final String ID = "symbol_block";
	
	private static SymbolBlock instance = null;
	public static SymbolBlock instance() {
		if (instance == null)
			instance = new SymbolBlock();
		
		return instance;
	}
	
		public SymbolBlock() {
		super(Material.BARRIER, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(500.0f);
		this.setResistance(900.0f);
		this.setBlockUnbreakable();
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		
		this.hasTileEntity = true;
		this.setLightLevel(0.8f);
		this.setLightOpacity(16);
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
	public boolean isSideSolid(BlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
		return false;
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return true;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public EnumBlockRenderType getRenderType(BlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
	@Override
	public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return null;
    }

	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		SymbolTileEntity ent = new SymbolTileEntity(5.0f);
		
		return ent;
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) { broke();
		super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int eventID, int eventParam) {
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
	}
	
	public void setInWorld(World world, BlockPos pos, SpellComponentWrapper component) {
		world.setBlockState(pos, this.getDefaultState());
		SymbolTileEntity te = (SymbolTileEntity) world.getTileEntity(pos);
		te.setComponent(component);
	}
}
