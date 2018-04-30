package com.smanzana.nostrummagica.items;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.AltarBlock;
import com.smanzana.nostrummagica.blocks.AltarBlock.AltarTileEntity;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.Candle.CandleTileEntity;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * One for each element, except physical
 * @author Skyler
 *
 */
public class InfusedGemItem extends Item implements ILoreTagged {

	public static final String ID = "nostrum_gem";
	
	private static InfusedGemItem instance = null;
	public static InfusedGemItem instance() {
		if (instance == null)
			instance = new InfusedGemItem();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.addRecipe(new ItemStack(instance), " G ", "DED", "BGB",
				'D', ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1),
				'G', ReagentItem.instance().getReagent(ReagentType.GRAVE_DUST, 1),
				'E', Items.ENDER_PEARL,
				'B', ReagentItem.instance().getReagent(ReagentType.BLACK_PEARL, 1));
	}
	
	public InfusedGemItem() {
		super();
		this.setUnlocalizedName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(16);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int i = stack.getMetadata();
		
		String suffix = getNameFromMeta(i);
		
		return this.getUnlocalizedName() + "." + suffix;
	}
	
	/**
	 * Returns an itemstack containing the gem type specefied.
	 * If element is null, returns a basic gem.
	 * @param element
	 * @param count
	 * @return
	 */
	public ItemStack getGem(EMagicElement element, int count) {
		int meta = 0;
		if (element != null && element != EMagicElement.PHYSICAL)
			meta = element.ordinal() + 1;
		
		return new ItemStack(this, count, meta);
	}
	
	/**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
    	subItems.add(new ItemStack(itemIn, 1, 0));
    	for (EMagicElement type : EMagicElement.values()) {
    		if (type == EMagicElement.PHYSICAL)
    			continue;
    		subItems.add(new ItemStack(itemIn, 1, type.ordinal() + 1));
    	}
	}
    
    public int getMetaFromElement(EMagicElement element) {
    	return element.ordinal() + 1;
    }
    
    public String getNameFromMeta(int meta) {
    	String suffix = "basic";
		
    	EMagicElement type = getTypeFromMeta(meta);
    	if (type != null)
    		suffix = type.name().toLowerCase();
    	
		return suffix;
    }
    
    public EMagicElement getTypeFromMeta(int meta) {
    	EMagicElement ret = null;
    	for (EMagicElement type : EMagicElement.values()) {
			if (type.ordinal() + 1 == meta) {
				ret = type;
				break;
			}
		}
    	
    	return ret;
    }
    
    @Override
	public String getLoreKey() {
		return "nostrum_infused_gem";
	}

	@Override
	public String getLoreDisplayName() {
		return "Infused Gems";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("By combining magical reagents with an enderpearl, you can create a Void Gem.", "The gem lacks any elemental affinity and otherwise seems useless.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("By combining magical reagents with an enderpearl, you can create a Void Gem.", "Void gems alone are not very useful.", "In order to use them, they must be inbued with the power of an element.", "Perhaps an Alter rune would do the trick...");
	}
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		//if (worldIn.isRemote)
			//return EnumActionResult.SUCCESS;
		
		IBlockState state = worldIn.getBlockState(pos);
		if (state.getBlock() == null)
			return EnumActionResult.PASS;
		
		EMagicElement element = getTypeFromMeta(stack.getMetadata());
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (state.getBlock() instanceof Candle) {
			if (!(te instanceof CandleTileEntity))
				return EnumActionResult.PASS;
			
 			if (RitualRegistry.attemptRitual(worldIn, pos, playerIn, element)) {
 				stack.stackSize--;
 			}
 			
			return EnumActionResult.SUCCESS;
		} else if (state.getBlock() instanceof AltarBlock) {
			if (!(te instanceof AltarTileEntity))
				return EnumActionResult.PASS;
			
			if (RitualRegistry.attemptRitual(worldIn, pos, playerIn, element)) {
				stack.stackSize--;
			}
			
			return EnumActionResult.SUCCESS;
		}
		
        return EnumActionResult.PASS;
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
