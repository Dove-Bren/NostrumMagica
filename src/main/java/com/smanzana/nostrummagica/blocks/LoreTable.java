package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.LoreTableEntity;
import com.smanzana.nostrummagica.client.gui.NostrumGui;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class LoreTable extends BlockContainer {
	
	public static final String ID = "lore_table";
	
	private static LoreTable instance = null;
	public static LoreTable instance() {
		if (instance == null)
			instance = new LoreTable();
		
		return instance;
	}
	
	public LoreTable() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(2.0f);
		this.setResistance(10.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
	}
	
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, PlayerEntity playerIn, EnumHand hand, Direction side, float hitX, float hitY, float hitZ) {
		
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.loretableID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new LoreTableEntity();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof LoreTableEntity))
			return;
		
		LoreTableEntity table = (LoreTableEntity) ent;
		ItemStack item = table.getItem();
		if (!item.isEmpty()) {
			double x, y, z;
			x = pos.getX() + .5;
			y = pos.getY() + .5;
			z = pos.getZ() + .5;
			world.spawnEntity(new EntityItem(world, x, y, z, item.copy()));
		}
		
	}
}
