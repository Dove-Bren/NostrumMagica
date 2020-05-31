package com.smanzana.nostrummagica.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonEgg;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Dragon spawning egg
 * @author Skyler
 *
 */
public class DragonEgg extends Item implements ILoreTagged {

	public static void init() {
		ItemStack gold = new ItemStack(Item.getItemFromBlock(Blocks.GOLD_BLOCK), 1, OreDictionary.WILDCARD_VALUE);
		ItemStack crystal = NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1);
		ItemStack star = new ItemStack(Items.NETHER_STAR, 1, OreDictionary.WILDCARD_VALUE);
		ItemStack shell = new ItemStack(DragonEggFragment.instance());
		ItemStack egg = new ItemStack(Item.getItemFromBlock(Blocks.DRAGON_EGG));
		GameRegistry.addRecipe(new ShapedRecipes(3, 3, new ItemStack[] {
				crystal, shell, star, shell, egg, shell, gold, gold, gold
		}, new ItemStack(instance())) {
			@Override
			public ItemStack[] getRemainingItems(InventoryCrafting inv) {
				ItemStack[] aitemstack = new ItemStack[inv.getSizeInventory()];
				
				int j = 4;
				int len = inv.getSizeInventory();
				if (len < 4) {
					len = 0;
				}
				
				for (int i = 0; i < len; i++) {
					if (i == j) {
						aitemstack[i] = egg;
					} else {
						aitemstack[i] = null;
					}
				}
				
				return aitemstack;
			}
		});
	}
	
	public static final String ID = "dragon_egg";

	private static DragonEgg instance = null;

	public static DragonEgg instance() {
		if (instance == null)
			instance = new DragonEgg();
	
		return instance;

	}

	public DragonEgg() {
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(ID);
		this.setMaxDamage(0);
		this.setCreativeTab(NostrumMagica.creativeTab);
	}
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		if (worldIn.isRemote)
			return EnumActionResult.SUCCESS;
		
		if (pos == null)
			return EnumActionResult.PASS;
		
		MutableBlockPos checkPos = new MutableBlockPos(pos);
		checkPos.setY(checkPos.getY() + 1);
		if (!worldIn.isAirBlock(checkPos)) {
			return EnumActionResult.PASS;
		}
		
		checkPos.setY(checkPos.getY() + 1);
		if (!worldIn.isAirBlock(checkPos)) {
			return EnumActionResult.PASS;
		}
		
		// Spawn
//		EntityTameDragonRed dragon = new EntityTameDragonRed(worldIn);
//		dragon.rollRandomStats();
//		dragon.setPosition(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
//		worldIn.spawnEntityInWorld(dragon);
		
		EntityDragonEgg egg = new EntityDragonEgg(worldIn, playerIn, EntityTameDragonRed.rollRandomStats());
		egg.setPosition(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
		worldIn.spawnEntityInWorld(egg);
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (attr != null) {
			attr.giveFullLore(egg);
		}
		
		playerIn.addChatComponentMessage(new TextComponentTranslation("info.egg.place"));
		
		if (!playerIn.isCreative()) {
			stack.stackSize--;
		}
		
		return EnumActionResult.SUCCESS;
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_dragon_egg";
	}

	@Override
	public String getLoreDisplayName() {
		return "Dragon Eggs";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("A curious egg created from the shells of a Red Dragon's egg.", "", "Caution: Upon use, the egg will be placed. The egg must be kept warm to have any chance of hatching!");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("A curious egg created from the shells of a Red Dragon's egg.", "", "Caution: Upon use, the egg will be placed. The egg must be kept warm to have any chance of hatching! Lots of light and a hay-bale are encouraged!");
	}
	
	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
