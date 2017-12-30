package com.smanzana.nostrummagica.blocks;

import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;

import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Represents:
 * 	- Midnight Iris
 *  - Crystabloom
 *  
 *  Randomly generated plants
 * @author Skyler
 *
 */
public class NostrumMagicaFlower extends BlockBush {
	
	private static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);
	
	public static enum Type implements IStringSerializable {
		MIDNIGHT_IRIS(ReagentType.BLACK_PEARL),
		CRYSTABLOOM(ReagentType.CRYSTABLOOM);
		
		private int key;
		
		private Type(ReagentType type) {
			this.key = type.getMeta();
		}
		
		public int getMeta() {
			return ordinal();
		}
		
		public int getKey() {
			return key;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return this.name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}

	private static NostrumMagicaFlower instance = null;
	public static NostrumMagicaFlower instance() {
		if (instance == null)
			instance = new NostrumMagicaFlower();
		
		return instance;
	};
	public static NostrumMagicaFlower crystabloom;
	
	public static void init() {
		instance();
		GameRegistry.register(instance,
    			new ResourceLocation(NostrumMagica.MODID, instance.getID()));
//    	GameRegistry.register(
//    			(new ItemBlock(instance)).setRegistryName(instance.getID())
//    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(instance.getID()));
	}
	
	private String id;
	
	public NostrumMagicaFlower() {
		super(Material.PLANTS);
		
		this.id = "nostrum_flower";
		this.setUnlocalizedName(id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		//this.setRegistryName(id);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, Type.MIDNIGHT_IRIS));
	}
	
	public String getID() {
		return id;
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
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
		
		if (meta == 0)
			return getDefaultState().withProperty(TYPE, Type.MIDNIGHT_IRIS);
		if (meta == 1)
			return getDefaultState().withProperty(TYPE, Type.CRYSTABLOOM);
		
		return getDefaultState();
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        switch (state.getValue(TYPE)) {
		case CRYSTABLOOM:
		case MIDNIGHT_IRIS:
			return ReagentItem.instance();
        }
        
        // fall through
        return null;
    }
	
	@SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		for (Type type : Type.values()) {
			list.add(new ItemStack(itemIn, 1, type.getKey()));
		}
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return getReagentMetaFromType(state.getValue(TYPE));
	}
	
	public int getReagentMetaFromType(Type type) {
		return type.getKey();
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).getMeta();
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(Item.getItemFromBlock(this), 1, getMetaFromState(state));
	}
	
	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return true;
	}
}
