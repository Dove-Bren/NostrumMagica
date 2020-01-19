package com.smanzana.nostrummagica.blocks;

import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DungeonBlock extends Block {
	
	public static enum Type implements IStringSerializable {
		LIGHT,
		DARK;

		@Override
		public String getName() {
			return name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return getName();
		}
	}
	
	public static PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);

	public static final String ID = "dungeon_block";
	
	private static DungeonBlock instance = null;
	public static DungeonBlock instance() {
		if (instance == null)
			instance = new DungeonBlock();
		
		return instance;
	}
	
	public DungeonBlock() {
		super(Material.ROCK, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(500.0f);
		this.setResistance(900.0f);
		this.setBlockUnbreakable();
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE,Type.LIGHT));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TYPE);
	}
	
	public IBlockState getState(Type type) {
		return getDefaultState().withProperty(TYPE, type);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta < 0)
			meta = 0;
		else if (meta > Type.values().length)
			meta = Type.values().length - 1;
		
		return getDefaultState().withProperty(TYPE, Type.values()[meta]);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return null;
    }
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		for (Type type : Type.values()) {
			list.add(new ItemStack(itemIn, 1, type.ordinal()));
		}
	}
}
