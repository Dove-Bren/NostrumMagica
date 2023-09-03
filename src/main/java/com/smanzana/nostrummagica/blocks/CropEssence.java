package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.ReagentSeed;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.BlockCrops;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

public class CropEssence extends BlockCrops {

	public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);
	private static final AxisAlignedBB[] AABB = new AxisAlignedBB[] {new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.165D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.275D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.275D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.275D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.7D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.7D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.7D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.8D, 1.0D)};

	public static String ID = "esscrop";
	
	private static CropEssence instance = null;
	public static CropEssence instance() {
			if (instance == null)
				instance = new CropEssence();
			
			return instance;
	}
	
	public CropEssence() {
		
	}
	
	@Override
	protected PropertyInteger getAgeProperty() {
		return AGE;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {AGE});
	}
	
	@Override
	public int getMaxAge() {
		return 3;
	}
	
	protected Item getSeed() {
		return ReagentSeed.essence;
	}
	
	protected ItemStack getSeeds(int count) {
		return new ItemStack(getSeed(), count);
	}
	
	protected ItemStack getRandomEssence(Random rand) {
		EMagicElement elem = EMagicElement.values()[rand.nextInt(EMagicElement.values().length)];
		return EssenceItem.getEssence(elem, 1);
	}

    @Override
    public void getDrops(NonNullList<ItemStack> ret, IBlockAccess world, BlockPos pos, BlockState state, int fortune) {
        final int age = getAge(state);
        Random rand = world instanceof World ? ((World)world).rand : NostrumMagica.rand;

        int cropCount = 0;
        if (age >= getMaxAge()) {
        	cropCount = 1 + rand.nextInt(2) + fortune;
        }
        if (cropCount != 0) {
        	for (int i = 0; i < cropCount; i++) {
        		ret.add(getRandomEssence(rand));
        	}
        }
        
        int seedCount = 1;
        if (age >= getMaxAge() && rand.nextBoolean() && rand.nextBoolean()) {
        	seedCount += 1 + (fortune / 3);
        }
        
        ret.add(getSeeds(seedCount));
    }

    /**
     * Get the Item that this Block should drop when harvested.
     */
    @Override
    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return EssenceItem.instance();
    }
    
    @Override
    public int damageDropped(BlockState state) {
    	return 0;
    }
    
    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, BlockState state) {
        return super.getItem(worldIn, pos, state);
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
	
	@Override
	public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.randomDisplayTick(stateIn, worldIn, pos, rand);
	}
	
}
