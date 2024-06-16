package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;
import com.smanzana.nostrummagica.tile.SymbolTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class SymbolBlock extends Block {
	
	public static final String ID = "symbol_block";
	
	public SymbolBlock() {
		this(Block.Properties.create(Material.BARRIER)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.setLightLevel((state) -> 16)
				);
	}
	
	public SymbolBlock(Block.Properties builder) {
		super(builder);
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return true;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		SymbolTileEntity ent = new SymbolTileEntity(1.0f);
		return ent;
	}
	
	public void setInWorld(IWorld world, BlockPos pos, SpellComponentWrapper component) {
		world.setBlockState(pos, this.getDefaultState(), 3);
		SymbolTileEntity te = (SymbolTileEntity) world.getTileEntity(pos);
		te.setComponent(component);
	}
}
