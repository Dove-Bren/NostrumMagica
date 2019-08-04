package com.smanzana.nostrummagica.items;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NostrumSkillItem extends Item implements ILoreTagged {

	public static enum SkillItemType {
		MIRROR("primordial_mirror"),
		OOZE("essential_ooze"),
		PENDANT("eldrich_pendant"),
		FLUTE("living_flute"),
		WING("dragon_wing");
		
		private String key;
		
		private SkillItemType(String key) {
			this.key = key;
		}
		
		public String getUnlocalizedKey() {
			return key;
		}
	}
	
	public static final String ID = "SkillItem";
	
	private static NostrumSkillItem instance = null;
	public static NostrumSkillItem instance() {
		if (instance == null)
			instance = new NostrumSkillItem();
		
		return instance;
	}
	
	public NostrumSkillItem() {
		super();
		this.setUnlocalizedName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int i = stack.getMetadata();
		
		SkillItemType type = getTypeFromMeta(i);
		return "item." + type.getUnlocalizedKey();
	}
	
	public static SkillItemType getTypeFromMeta(int meta) {
		SkillItemType ret = null;
    	for (SkillItemType type : SkillItemType.values()) {
			if (type.ordinal() == meta) {
				ret = type;
				break;
			}
		}
    	
    	return ret;
    }
	
	public static ItemStack getItem(SkillItemType type, int count) {
		int meta = getMetaFromType(type);
		
		return new ItemStack(instance(), count, meta);
	}
	
	@SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
    	for (SkillItemType type: SkillItemType.values()) {
    		subItems.add(new ItemStack(itemIn, 1, getMetaFromType(type)));
    	}
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_skill_item";
	}

	@Override
	public String getLoreDisplayName() {
		return "Skill Items";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Some items possess an insane amount of magical energies. These items can be combined in certain ways that you might be able to utilize...");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Some items possess an insane amount of magical energies.", "The most useful is the Primordial Mirror, which you can use to gain an extra Skill Point!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (playerIn.isSneaking())
			return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
		
		if (worldIn.isRemote)
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		
		SkillItemType type = getTypeFromMeta(stack.getMetadata());
		
		if (type == SkillItemType.MIRROR) {
			// Grant a skill point if they have magic
			INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
			if (attr != null && attr.isUnlocked()) {
				attr.addSkillPoint();
				
				NostrumMagicaSounds.AMBIENT_WOOSH.play(playerIn);
				stack.stackSize--;
				NostrumMagica.proxy.syncPlayer((EntityPlayerMP) playerIn);
			}
			
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		
	}
	
	public static int getMetaFromType(SkillItemType element) {
		if (element == null)
			return 0;
		
		return element.ordinal();
	}
	
}
