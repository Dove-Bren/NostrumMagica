package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.blocks.CropGinseng;
import com.smanzana.nostrummagica.blocks.CropMandrakeRoot;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSeeds;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;

/**
 * Seeds for ginseng and mandrake root, which can be planted and grown
 * @author Skyler
 *
 */
public class ReagentSeed extends ItemSeeds {
	
	public static final ReagentSeed Mandrake = new ReagentSeed(true);
	public static final ReagentSeed Ginseng = new ReagentSeed(false);
	
	public static final String MANDRAKE_ID = "reagentseed_mandrake";
	public static final String GINSENG_ID = "reagentseed_ginseng";

	private final boolean isMandrake;
	
	protected static Block getCrops(boolean mandrake) {
		if (mandrake) {
			return CropMandrakeRoot.instance();
		} else {
			return CropGinseng.instance();
		}
	}
	
	private ReagentSeed(boolean mandrake) {
		super(getCrops(mandrake), Blocks.FARMLAND);
		this.isMandrake = mandrake;
		
		if (mandrake) {
			this.setUnlocalizedName("reagentseed.mandrake");
		} else {
			this.setUnlocalizedName("reagentseed.ginseng");
		}
	}
	
	 public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		 return EnumPlantType.Crop;
	 }
	 
	 public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
		 return getCrops(isMandrake).getDefaultState();
	 }

}
