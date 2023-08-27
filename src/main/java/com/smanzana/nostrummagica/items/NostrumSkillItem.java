package com.smanzana.nostrummagica.items;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NostrumSkillItem extends Item implements ILoreTagged {

	public static enum SkillItemType {
		MIRROR("primordial_mirror"),
		OOZE("essential_ooze"),
		PENDANT("eldrich_pendant"),
		FLUTE("living_flute"),
		WING("dragon_wing"),
		ENDER_PIN("ender_pin"),
		RESEARCH_SCROLL_SMALL("research_scroll_small"),
		RESEARCH_SCROLL_LARGE("research_scroll_large");
		
		private String key;
		
		private SkillItemType(String key) {
			this.key = key;
		}
		
		public String getUnlocalizedKey() {
			return key;
		}
		
		private String getDescKey() {
			return "item." + key + ".desc";
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
		this.setRegistryName(NostrumMagica.MODID, ID);
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
	
	@OnlyIn(Dist.CLIENT)
    @Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (this.isInCreativeTab(tab)) {
	    	for (SkillItemType type: SkillItemType.values()) {
	    		subItems.add(new ItemStack(this, 1, getMetaFromType(type)));
	    	}
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
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, EnumHand hand) {
		final @Nonnull ItemStack stack = playerIn.getHeldItem(hand);
		if (playerIn.isSneaking())
			return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
		
		SkillItemType type = getTypeFromMeta(stack.getMetadata());
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (attr != null && attr.isUnlocked() && type != SkillItemType.WING) {
			
			if (type == SkillItemType.ENDER_PIN  && attr.hasEnhancedTeleport()) {
				playerIn.sendMessage(new TranslationTextComponent("info.skillitem.advtele.unlocked", new Object[0]));
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
			}
			
			String suffix = null;
			switch (type) {
			case FLUTE:
				attr.addFinesse();
				suffix = "finesse";
				break;
			case MIRROR:
				attr.addSkillPoint();
				suffix = "point";
				break;
			case OOZE:
				attr.addControl();
				suffix = "control";
				break;
			case PENDANT:
				attr.addTech();
				suffix = "technique";
				break;
			case WING:
				// Wings don't do anything
				break;
			case ENDER_PIN:
				attr.unlockEnhancedTeleport();;
				suffix = "advtele";
				break;
			case RESEARCH_SCROLL_LARGE:
				attr.addResearchPoint();
				attr.addResearchPoint();
				attr.addResearchPoint();
				suffix = "research";
				break;
			case RESEARCH_SCROLL_SMALL:
				attr.addResearchPoint();
				suffix = "research";
				break;
			}
			
			if (worldIn.isRemote) {
				// Display message but don't do anything
				playerIn.sendMessage(new TranslationTextComponent("info.skillitem." + suffix, new Object[0]));
			} else {
				// Server side
				NostrumMagicaSounds.LORE.play(null, playerIn.world, playerIn.posX, playerIn.posY, playerIn.posZ);
				stack.shrink(1);
				NostrumMagica.proxy.syncPlayer((ServerPlayerEntity) playerIn);
			}
			
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}
		
		
		
		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
		
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		SkillItemType type = getTypeFromMeta(stack.getMetadata());
		if (type == null)
			return;
		
		if (I18n.contains(type.getDescKey())) {
			String translation = I18n.format(type.getDescKey(), new Object[0]);
			if (translation.trim().isEmpty())
				return;
			tooltip.add(TextFormatting.BLUE + translation);
		}
	}
	
	public static int getMetaFromType(SkillItemType element) {
		if (element == null)
			return 0;
		
		return element.ordinal();
	}
	
}
