package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;

import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

public class CropMandrakeRoot extends BlockCrops {

	private static final AxisAlignedBB[] AABB = new AxisAlignedBB[] {new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1875D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.3125D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.4375D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5625D, 1.0D)};

	public static String ID = "mandrake_crop";
	
	private static CropMandrakeRoot instance = null;
	public static CropMandrakeRoot instance() {
			if (instance == null)
				instance = new CropMandrakeRoot();
			
			return instance;
	}
	
	public CropMandrakeRoot() {
		
	}
	
	protected @Nonnull ItemStack getCrops(int count) {
        return ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, count);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> ret, net.minecraft.world.IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int age = getAge(state);
        Random rand = world instanceof World ? ((World)world).rand : new Random();

        int count = 0;
        if (age >= getMaxAge()){
        	count = 2 + rand.nextInt(2) + fortune;
        }
        
        if (count != 0)
        	ret.add(getCrops(count));
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ReagentItem.instance();
    }
    
    @Override
    public int damageDropped(IBlockState state) {
    	return ReagentType.MANDRAKE_ROOT.getMeta();
    }
    
    @Override
    public @Nonnull ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return getCrops(1);
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB[((Integer)state.getValue(this.getAgeProperty())).intValue()];
    }
    
	@Override
	public EnumPlantType getPlantType(net.minecraft.world.IBlockAccess world, BlockPos pos) {
		return EnumPlantType.Crop;
	}

	@Override
	public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
		return getDefaultState();
	}
	
}
