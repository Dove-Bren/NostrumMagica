package com.smanzana.nostrummagica.items;

import java.util.UUID;

import javax.annotation.Nonnull;
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HookshotItem extends Item implements ILoreTagged, IElytraProvider {
	
	public static enum HookshotType {
		WEAK,
		MEDIUM,
		STRONG,
		CLAW;
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
		this.setRegistryName(NostrumMagica.MODID, ID);
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
	
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}
	
	public static boolean IsExtended(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof HookshotItem)) {
			return false;
		}
		
		return ExtendedFromMeta(stack.getMetadata());
	}
	
	public static void SetExtended(ItemStack stack, boolean extended) {
		if (stack.isEmpty() || !(stack.getItem() instanceof HookshotItem)) {
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
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		final ItemStack itemStackIn = playerIn.getHeldItem(hand); 
		final HookshotType type = GetType(itemStackIn);
		if (true) {
			if (type == null) {
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn); 
			}
			
			if (IsExtended(itemStackIn)) {
				// Claws will clear an entity if it hasn't hooked yet.
				// Otherwise, right-clicking will be passed to the other hookshot if available
				if (type == HookshotType.CLAW) {
					EntityHookShot hook = GetHookEntity(worldIn, itemStackIn);
					if (hook == null) {
						return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
					}
					
					if (!hook.isHooked()) {
						ClearHookEntity(worldIn, itemStackIn);
					} else {
						// Check if there are other hookshots that might want this event if we're in the mainhand (and checked first)
						if (hand == EnumHand.MAIN_HAND) {
							@Nonnull ItemStack offHandStack = playerIn.getHeldItemOffhand();
							if (!offHandStack.isEmpty() && offHandStack.getItem() instanceof HookshotItem) {
								// See if it's hooked yet or not. If it's hooked, we'll handle this event. Otherwise, we'll pass it
								@Nullable EntityHookShot otherHook = GetHookEntity(worldIn, offHandStack);
								if (IsExtended(offHandStack) && otherHook != null && otherHook.isHooked()) {
									ClearHookEntity(worldIn, itemStackIn); // Clear our hook
									return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
								}
								
								// We didn't handle, so pass to other
								return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
							}
						}
						// Other item wasn't a hookshot or we're in the mainhand.
						// Handle.
						ClearHookEntity(worldIn, itemStackIn);
					}
				} else {
					// Other hookshots use right-click to mean we should de-activate
					ClearHookEntity(worldIn, itemStackIn);
				}
			} else {
				if (!worldIn.isRemote) {
					if (playerIn.dimension == ModConfig.config.sorceryDimensionIndex()) {
						playerIn.sendMessage(new TextComponentTranslation("info.hookshot.bad_dim"));
					} else {
						EntityHookShot hook = new EntityHookShot(worldIn, playerIn, getMaxDistance(itemStackIn), 
								ProjectileTrigger.getVectorForRotation(playerIn.rotationPitch, playerIn.rotationYaw).scale(getVelocity(itemStackIn)),
								TypeFromMeta(itemStackIn.getMetadata()));
						worldIn.spawnEntity(hook);
						SetHook(itemStackIn, hook);
						NostrumMagicaSounds.HOOKSHOT_FIRE.play(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ);
					}
				}
			}
		}
		
//		if (worldIn.isRemote && type == HookshotType.CLAW && hand == EnumHand.MAIN_HAND) {
//			@Nullable ItemStack offHandStack = playerIn.getHeldItemOffhand();
//			if (offHandStack != null && offHandStack.getItem() instanceof HookshotItem) {
//				return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
//			}
//		}
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
	}
	
	public int getMetadata(int damage) {
		return damage;
	}
	
	public static HookshotType GetType(ItemStack stack) {
		if (stack.isEmpty()) {
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
		if (world.isRemote) {
			return;
		}
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
			
			// Detect two active hookshots (since claw bypass) and clear if we're older
			// 4 is mainhand //0 is offhand
			if (entityIn instanceof EntityLivingBase) {
				EntityLivingBase living = (EntityLivingBase) entityIn;
				final EnumHand hand = (itemSlot == 0 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
				@Nonnull final ItemStack otherHand = living.getHeldItem(hand == EnumHand.MAIN_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
				if (!otherHand.isEmpty() && otherHand.getItem() instanceof HookshotItem && IsExtended(otherHand)) {
					EntityHookShot otherHook = GetHookEntity(worldIn, otherHand);
					if (otherHook != null && otherHook.isPulling() && otherHook.ticksExisted < anchor.ticksExisted) {
						ClearHookEntity(worldIn, stack);
						return;
					}
				}
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
		case CLAW:
			return 50.0;
		}
	}
	
	public static float GetVelocity(HookshotType type) {
		return 1f;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (this.isInCreativeTab(tab)) {
			for (HookshotType type : HookshotType.values()) {
				subItems.add(new ItemStack(this, 1, MakeMeta(type, false)));
			}
		}
	}
	
	public static String GetTypeSuffix(HookshotType type) {
		return type.name().toLowerCase();
	}
	
	public static boolean CanBeHooked(HookshotType type, IBlockState blockState) {
		switch(type) {
		case STRONG:
		case CLAW:
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
		return entity instanceof EntityItem || entity instanceof EntityLivingBase;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack) {
		return I18n.format(this.getUnlocalizedName() + "_" + GetTypeSuffix(TypeFromMeta(stack.getMetadata())) + ".name", (Object[])null);
	}

	@Override
	public boolean isElytraFlying(EntityLivingBase entityIn, ItemStack stack) {
		if (IsExtended(stack)) {
			// See if we're being pulled
			EntityHookShot anchor = GetHookEntity(entityIn.world, stack);
			return anchor != null && anchor.isPulling();
		}
		
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldRenderElyta(EntityLivingBase entity, ItemStack stack) {
		return false;
	}
	
}
