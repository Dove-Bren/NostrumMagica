package com.smanzana.nostrummagica.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.EntityHookShot;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;

import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class HookshotItem extends Item implements ILoreTagged {
	
	public static enum HookshotType {
		WEAK,
		MEDIUM,
		STRONG,
	}

	public static void init() {
		GameRegistry.addShapedRecipe(new ItemStack(instance(), 1, MakeMeta(HookshotType.WEAK, false)),
				" RF", "DIR", "WD ",
				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
				'R', new ItemStack(Items.REDSTONE),
				'D', new ItemStack(Items.DIAMOND),
				'I', new ItemStack(Blocks.IRON_BLOCK, 1, OreDictionary.WILDCARD_VALUE),
				'F', new ItemStack(Items.FLINT));
	}
	
	private static HookshotItem instance = null;

	public static HookshotItem instance() {
		if (instance == null)
			instance = new HookshotItem();
	
		return instance;

	}
	
	public static final String ID = "hookshot";
	
	private static final String NBT_HOOK_ID = "hook_uuid";

	public HookshotItem() {
		super();
		this.setUnlocalizedName(ID);
		this.setMaxStackSize(1);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.hasSubtypes = true;
		
		this.addPropertyOverride(new ResourceLocation("extended"), new IItemPropertyGetter() {
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				return entityIn != null
						&& (IsExtended(stack)
						&& (entityIn.getHeldItem(EnumHand.MAIN_HAND) == stack || entityIn.getHeldItem(EnumHand.OFF_HAND) == stack))
						? 1.0F : 0.0F;
			}
		});
	}
	
	public static boolean IsExtended(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof HookshotItem)) {
			return false;
		}
		
		return ExtendedFromMeta(stack.getMetadata());
	}
	
	public static void SetExtended(ItemStack stack, boolean extended) {
		if (stack == null || !(stack.getItem() instanceof HookshotItem)) {
			return;
		}
		
		stack.setItemDamage(MakeMeta(GetType(stack), extended));
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_hookshot";
	}

	@Override
	public String getLoreDisplayName() {
		return "HookShots";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Hookshots are items that let you attach onto distant objects and use them to move around!", "The basic one appears to certain types of blocks. You might be able to improve it somehow...");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Hookshots are items that let you attach onto distant objects and use them to move around!", "Hookshots pull you to the hooked location and are handy for moving around!", "Basic hookshots can only attach to wood things, while the improved versions can attach to many more things and have longer chains!");
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (!worldIn.isRemote) {
			if (IsExtended(itemStackIn)) {
				ClearHookEntity(worldIn, itemStackIn);
			} else {
				if (playerIn.dimension == ModConfig.config.sorceryDimensionIndex()) {
					playerIn.addChatComponentMessage(new TextComponentTranslation("info.hookshot.bad_dim"));
				} else {
					EntityHookShot hook = new EntityHookShot(worldIn, playerIn, getMaxDistance(itemStackIn), 
							ProjectileTrigger.getVectorForRotation(playerIn.rotationPitch, playerIn.rotationYaw).scale(getVelocity(itemStackIn)),
							TypeFromMeta(itemStackIn.getMetadata()));
					worldIn.spawnEntityInWorld(hook);
					SetHook(itemStackIn, hook);
					NostrumMagicaSounds.HOOKSHOT_FIRE.play(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ);
				}
			}
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
	}
	
	public int getMetadata(int damage) {
		return damage;
	}
	
	public static HookshotType GetType(ItemStack stack) {
		if (stack == null) {
			return HookshotType.WEAK;
		}
		
		return TypeFromMeta(stack.getMetadata());
	}
	
	protected static HookshotType TypeFromMeta(int meta) {
		// 2-3rd bits are type
		int idx = (meta >> 1) & 0x3;
		try {
			return HookshotType.values()[idx];
		} catch (Exception e) {
			return HookshotType.WEAK;
		}
	}
	
	protected static boolean ExtendedFromMeta(int meta) {
		// 1st bit is extended bit
		return ((meta & 0x1) == 1);
	}
	
	public static int MakeMeta(HookshotType type, boolean extended) {
		return (extended ? 1 : 0)
				| (type.ordinal() << 1)
				;
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	public static void SetHook(ItemStack stack, EntityHookShot entity) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) {
			tag = new NBTTagCompound();
		}
		
		if (entity == null) {
			tag.removeTag(NBT_HOOK_ID);
		} else {
			tag.setUniqueId(NBT_HOOK_ID, entity.getUniqueID());
		}
		
		stack.setTagCompound(tag);
		
		SetExtended(stack, entity != null);
	}
	
	@Nullable
	public static EntityHookShot GetHookEntity(World world, ItemStack stack) {
		EntityHookShot entity = null;
		UUID id = GetHookID(stack);
		if (id != null) {
			for (Entity ent : world.loadedEntityList) {
				if (ent != null && ent instanceof EntityHookShot && ent.getUniqueID().equals(id)) {
					entity = (EntityHookShot) ent;
					break;
				}
			}
		}
		return entity;
	}
	
	protected static UUID GetHookID(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		UUID id = null;
		if (tag != null) {
			id = tag.getUniqueId(NBT_HOOK_ID);
		}
		
		return id;
	}
	
	protected static void ClearHookEntity(World world, ItemStack stack) {
		EntityHookShot hook = GetHookEntity(world, stack);
		if (hook != null) {
			hook.setDead();
		}
		
		SetExtended(stack, false);
		SetHook(stack, null);
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
		
		if (IsExtended(stack)) {
			// Clear extended if we can't find the anchor entity or it's not in our hand anymore
			if (entityIn instanceof EntityLivingBase)  {
				EntityLivingBase holder = (EntityLivingBase) entityIn;
				if (!(holder.getHeldItem(EnumHand.MAIN_HAND) == stack || holder.getHeldItem(EnumHand.OFF_HAND) == stack)) {
					if (entityIn instanceof EntityPlayer) {
					}
					ClearHookEntity(worldIn, stack);
					return;
				}
			}
			
			EntityHookShot anchor = GetHookEntity(worldIn, stack);
			if (anchor == null) {
				ClearHookEntity(worldIn, stack);
				if (entityIn instanceof EntityPlayer) {
				}
				return;
			}
		}
	}
	
	protected float getVelocity(ItemStack stack) {
		return GetVelocity(GetType(stack));
	}
	
	protected double getMaxDistance(ItemStack stack) {
		return GetMaxDistance(GetType(stack));
	}
	
	public static double GetMaxDistance(HookshotType type) {
		switch (type) {
		case WEAK:
		default:
			return 20.0;
		case MEDIUM:
			return 35.0;
		case STRONG:
			return 50.0;
		}
	}
	
	public static float GetVelocity(HookshotType type) {
		return 1f;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (HookshotType type : HookshotType.values()) {
			subItems.add(new ItemStack(itemIn, 1, MakeMeta(type, false)));
		}
	}
	
	public static String GetTypeSuffix(HookshotType type) {
		return type.name().toLowerCase();
	}
	
	public static boolean CanBeHooked(HookshotType type, IBlockState blockState) {
		switch(type) {
		case STRONG:
			return true;
		case MEDIUM:
			if (blockState.getMaterial().getCanBurn()) {
				return true;
			}
			if (blockState.getBlock() instanceof BlockPane && blockState.getMaterial() == Material.IRON) {
				return true;
			}
			// fall through
		case WEAK:
			if (blockState.getMaterial() == Material.WOOD) {
				return true;
			}
			break;
		}
		
		return false;
	}
	
	public static boolean CanBeHooked(HookshotType type, Entity entity) {
		return entity instanceof EntityItem || entity instanceof EntityLiving;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack) {
		return I18n.format(this.getUnlocalizedName() + "_" + GetTypeSuffix(TypeFromMeta(stack.getMetadata())) + ".name", (Object[])null);
	}
	
}
