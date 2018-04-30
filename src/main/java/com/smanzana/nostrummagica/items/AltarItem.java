package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.AltarBlock;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class AltarItem extends Item implements ILoreTagged {

	public static void init() {
		
		GameRegistry.addShapedRecipe(new ItemStack(instance),
				"SSS", " T ", "TRT",
				'S', Blocks.STONE_SLAB,
				'T', Blocks.STONE,
				'R', NostrumResourceItem.getItem(ResourceType.TOKEN, 1)
				);
	}
	
	private static AltarItem instance = null;

	public static AltarItem instance() {
		if (instance == null)
			instance = new AltarItem();
	
		return instance;

	}
	
	public static final String ID = "altar_item";

	public AltarItem() {
		this.setUnlocalizedName(ID);
		this.setMaxStackSize(8);
		this.setCreativeTab(NostrumMagica.creativeTab);
	}
	
	@Override
	public String getLoreKey() {
		return "altar_item";
	}

	@Override
	public String getLoreDisplayName() {
		return "Ritual Altar";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Altars can be used to hold items.", "There's probably a better use for them...");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Ritual Altars hold items for display or use in a ritual.", "Only tier III rituals use altars.", "Up to 5 altars can be used in a single ritual.");
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IBlockState state = worldIn.getBlockState(pos);
        if (facing == EnumFacing.UP && playerIn.canPlayerEdit(pos.offset(facing), facing, stack) && state.isFullBlock() && worldIn.isAirBlock(pos.up())) {
        	worldIn.setBlockState(pos.up(), AltarBlock.instance().getDefaultState());
            stack.damageItem(1, playerIn);
            return EnumActionResult.SUCCESS;
        } else {
        	return EnumActionResult.FAIL;
        }
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.RITUALS;
	}
	
}
