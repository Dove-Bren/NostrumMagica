package com.smanzana.nostrummagica.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ShrineBlock extends SymbolBlock {
	
	public static final String ID = "shrine_block";
	
	private static ShrineBlock instance = null;
	public static ShrineBlock instance() {
		if (instance == null)
			instance = new ShrineBlock();
		
		return instance;
	}
	
	public ShrineBlock() {
		super();
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		SymbolTileEntity ent = new SymbolTileEntity(1.0f);
		
		return ent;
	}
	
}
