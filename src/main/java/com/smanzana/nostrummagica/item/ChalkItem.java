package com.smanzana.nostrummagica.item;

import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChalkItem extends Item implements ILoreTagged {

	public static final String ID = "nostrum_chalk";

	public ChalkItem() {
		super(NostrumItems.PropUnstackable().durability(25));
	}
	
	public boolean isEnchantable(ItemStack stack) {
		return false;
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
	public ActionResultType useOn(ItemUseContext context) {
		final BlockPos pos = context.getClickedPos();
		final PlayerEntity player = context.getPlayer();
		final World world = context.getLevel();
		final Direction facing = context.getClickedFace();
		
		ItemStack stack = context.getItemInHand();
        if (facing == Direction.UP && player.mayUseItemAt(pos.relative(facing), facing, stack) && Block.canSupportRigidBlock(world, pos) && world.isEmptyBlock(pos.above())) {
        	world.setBlockAndUpdate(pos.above(), NostrumBlocks.chalk.defaultBlockState());
        	ItemStacks.damageItem(stack, player, context.getHand(), 1);
            return ActionResultType.SUCCESS;
        } else {
        	return ActionResultType.FAIL;
        }
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
}
