package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.CropEssence;
import com.smanzana.nostrummagica.blocks.CropGinseng;
import com.smanzana.nostrummagica.blocks.CropMandrakeRoot;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSeeds;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;

/**
 * Seeds for ginseng, mandrake, and essence, which can be planted and grown
 * @author Skyler
 *
 */
public class ReagentSeed extends ItemSeeds {
	
	static enum SeedType {
		MANDRAKE("mandrake", CropMandrakeRoot.instance().getDefaultState()),
		GINSENG("ginseng", CropGinseng.instance().getDefaultState()),
		ESSENCE("essence", CropEssence.instance().getDefaultState());
		
		private final String id;
		private final IBlockState state;
		
		private SeedType(String id, IBlockState state) {
			this.id = id;
			this.state = state;
		}
		
		public String getRawID() {
			return id;
		}
		
		public String getItemID() {
			return "reagentseed_" + id;
		}
		
		public String getUnlocName() {
			return "reagentseed." + id;
		}
		
		public IBlockState getCropState() {
			return state;
		}
	}
	
	public static final ReagentSeed mandrake = new ReagentSeed(SeedType.MANDRAKE);
	public static final ReagentSeed ginseng = new ReagentSeed(SeedType.GINSENG);
	public static final ReagentSeed essence = new ReagentSeed(SeedType.ESSENCE);
	
	private final SeedType type;
	
	private ReagentSeed(SeedType type) {
		super(type.getCropState().getBlock(), Blocks.FARMLAND);
		this.setUnlocalizedName(type.getUnlocName());
		this.setCreativeTab(NostrumMagica.creativeTab);
		
		this.type = type;
	}
	
	 public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		 return EnumPlantType.Crop;
	 }
	 
	 public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
		 return type.getCropState();
	 }
	 
	 public String getItemID() {
		 return type.getItemID();
	 }

}
