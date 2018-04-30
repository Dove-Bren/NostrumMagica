package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.ChalkBlock;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class ChalkItem extends Item implements ILoreTagged {

	public static void init() {
		
		/*
		 * In the future, something cool.
		 * For now, just a wooden sword </3
		 */
		GameRegistry.addShapelessRecipe(new ItemStack(instance),
				new ItemStack(Items.DYE, 1, 15),
				new ItemStack(Items.DYE, 1, 15),
				new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE));
	}
	
	private static ChalkItem instance = null;

	public static ChalkItem instance() {
		if (instance == null)
			instance = new ChalkItem();
	
		return instance;

	}
	
	public static final String ID = "nostrum_chalk";

	public ChalkItem() {
		this.setUnlocalizedName(ID);
		this.setMaxStackSize(1);
		this.setMaxDamage(20);
		this.setCreativeTab(NostrumMagica.creativeTab);
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_chalk";
	}

	@Override
	public String getLoreDisplayName() {
		return "Ritual Chalk";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Most rituals require drawing symbols with chalk.", "It's very easy to make, making it perfect for the traveling mage.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Chalk is used to create symbols used for rituals.", "Even though it's very easy to make, it might be worth holding on to one or two for rituals in a pinch!");
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IBlockState state = worldIn.getBlockState(pos);
        if (facing == EnumFacing.UP && playerIn.canPlayerEdit(pos.offset(facing), facing, stack) && state.isFullBlock() && worldIn.isAirBlock(pos.up())) {
        	worldIn.setBlockState(pos.up(), ChalkBlock.instance().getDefaultState());
            stack.damageItem(1, playerIn);
            return EnumActionResult.SUCCESS;
        } else {
        	return EnumActionResult.FAIL;
        }
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
}
