package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.aetheria.items.AetherResourceType;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.ReagentSeed;

import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

public class CropGinseng extends BlockCrops {

	private static final AxisAlignedBB[] AABB = new AxisAlignedBB[] {new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.165D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.275D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.275D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.275D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.7D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.7D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.7D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.8D, 1.0D)};

	public static String ID = "ginseng_crop";
	
	private static CropGinseng instance = null;
	public static CropGinseng instance() {
			if (instance == null)
				instance = new CropGinseng();
			
			return instance;
	}
	
	public CropGinseng() {
		
	}
	
	protected ItemStack getCrops(int count) {
        return new ItemStack(ReagentItem.instance(), count, ReagentType.GINSENG.getMeta());
    }
	
	protected Item getSeed() {
		return ReagentSeed.ginseng;
	}
	
	protected ItemStack getSeeds(int count) {
		return new ItemStack(getSeed(), count);
	}

    @Override
    public void getDrops(NonNullList<ItemStack> ret, IBlockAccess world, BlockPos pos, BlockState state, int fortune) {
        int age = getAge(state);
        Random rand = world instanceof World ? ((World)world).rand : new Random();

        int cropCount = 0;
        if (age >= getMaxAge()) {
        	cropCount = 3 + rand.nextInt(2) + fortune;
        }
        if (cropCount != 0) {
        	ret.add(getCrops(cropCount));
        }
        
        int seedCount = 1;
        if (age >= getMaxAge()) {
        	seedCount += (rand.nextBoolean() ? rand.nextInt(2) : 0) + fortune;
        }
        
        ret.add(getSeeds(seedCount));
        
        if (NostrumMagica.aetheria.isEnabled()) {
        	if (rand.nextInt(10) + fortune >= 9) {
        		ret.add(NostrumMagica.aetheria.getResourceItem(AetherResourceType.FLOWER_GINSENG, 1));
        	}
        }
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    @Override
    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return ReagentItem.instance();
    }
    
    @Override
    public int damageDropped(BlockState state) {
    	return ReagentType.GINSENG.getMeta();
    }
    
    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, BlockState state) {
        return getCrops(1);
    }

    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
        return AABB[((Integer)state.getValue(this.getAgeProperty())).intValue()];
    }
    
	@Override
	public EnumPlantType getPlantType(net.minecraft.world.IBlockAccess world, BlockPos pos) {
		return EnumPlantType.Crop;
	}

	@Override
	public BlockState getPlant(IBlockAccess world, BlockPos pos) {
		return getDefaultState();
	}
	
}
