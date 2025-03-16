package com.smanzana.nostrummagica.item;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Item;

/**
 * Seeds for ginseng, mandrake, and essence, which can be planted and grown
 * @author Skyler
 *
 */
public class ReagentSeed extends ItemNameBlockItem {
	
	public static final String ID_MANDRAKE_SEED = "reagentseed_mandrake";
	public static final String ID_GINSENG_SEED = "reagentseed_ginseng";
	public static final String ID_ESSENCE_SEED = "reagentseed_essence";
	
	public ReagentSeed(Block block, Item.Properties properties) {
		super(block, properties);
	}
	
//	static enum SeedType {
//		MANDRAKE("mandrake", CropMandrakeRoot.instance().getDefaultState()),
//		GINSENG("ginseng", CropGinseng.instance().getDefaultState()),
//		ESSENCE("essence", CropEssence.instance().getDefaultState());
//		
//		private final String id;
//		private final BlockState state;
//		
//		private SeedType(String id, BlockState state) {
//			this.id = id;
//			this.state = state;
//		}
//		
//		public String getRawID() {
//			return id;
//		}
//		
//		public String getItemID() {
//			return "reagentseed_" + id;
//		}
//		
//		public String getUnlocName() {
//			return "reagentseed." + id;
//		}
//		
//		public BlockState getCropState() {
//			return state;
//		}
//	}
//	
//	public static final ReagentSeed mandrake = new ReagentSeed(SeedType.MANDRAKE);
//	public static final ReagentSeed ginseng = new ReagentSeed(SeedType.GINSENG);
//	public static final ReagentSeed essence = new ReagentSeed(SeedType.ESSENCE);
//	
//	private final SeedType type;
//	
//	private ReagentSeed(SeedType type) {
//		super(type.getCropState().getBlock(), Blocks.FARMLAND);
//		
//		this.type = type;
//		
//		this.setUnlocalizedName(type.getUnlocName());
//		this.setRegistryName(NostrumMagica.MODID, this.getItemID());
//		this.setCreativeTab(NostrumMagica.creativeTab);
//	}
//	
//	 public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
//		 return EnumPlantType.Crop;
//	 }
//	 
//	 public BlockState getPlant(IBlockAccess world, BlockPos pos) {
//		 return type.getCropState();
//	 }
//	 
//	 public String getItemID() {
//		 return type.getItemID();
//	 }

}
