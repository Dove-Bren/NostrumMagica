package com.smanzana.nostrummagica.items;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.EntityHookShot;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.utils.Entities;

import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HookshotItem extends Item implements ILoreTagged, IElytraProvider {
	
	public static enum HookshotType {
		WEAK(Rarity.UNCOMMON),
		MEDIUM(Rarity.UNCOMMON),
		STRONG(Rarity.RARE),
		CLAW(Rarity.RARE);
		
		public final Rarity rarity;
		
		private HookshotType(Rarity rarity) {
			this.rarity = rarity;
		}
	}
	
	public static final String ID_PREFIX = "hookshot_";
	public static final String MakeID(HookshotType type) {
		return ID_PREFIX + GetTypeSuffix(type);
	}
	
	private static final String NBT_HOOK_ID = "hook_uuid";
	
	protected final HookshotType type;

	public HookshotItem(HookshotType type) {
		super(NostrumItems.PropUnstackable().rarity(type.rarity));
		
		this.addPropertyOverride(new ResourceLocation("extended"), new IItemPropertyGetter() {
			@OnlyIn(Dist.CLIENT)
			@Override
			public float call(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
				return entityIn != null
						&& (IsExtended(stack)
						&& (entityIn.getHeldItem(Hand.MAIN_HAND) == stack || entityIn.getHeldItem(Hand.OFF_HAND) == stack))
						? 1.0F : 0.0F;
			}
		});
	}
	
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}
	
	public static boolean IsExtended(ItemStack stack) {
		return GetHookID(stack) != null;
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
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final ItemStack itemStackIn = playerIn.getHeldItem(hand); 
		if (true) {
			if (type == null) {
				return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn); 
			}
			
			if (IsExtended(itemStackIn)) {
				// Claws will clear an entity if it hasn't hooked yet.
				// Otherwise, right-clicking will be passed to the other hookshot if available
				if (type == HookshotType.CLAW) {
					EntityHookShot hook = GetHookEntity(worldIn, itemStackIn);
					if (hook == null) {
						return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
					}
					
					if (!hook.isHooked()) {
						ClearHookEntity(worldIn, itemStackIn);
					} else {
						// Check if there are other hookshots that might want this event if we're in the mainhand (and checked first)
						if (hand == Hand.MAIN_HAND) {
							@Nonnull ItemStack offHandStack = playerIn.getHeldItemOffhand();
							if (!offHandStack.isEmpty() && offHandStack.getItem() instanceof HookshotItem) {
								// See if it's hooked yet or not. If it's hooked, we'll handle this event. Otherwise, we'll pass it
								@Nullable EntityHookShot otherHook = GetHookEntity(worldIn, offHandStack);
								if (IsExtended(offHandStack) && otherHook != null && otherHook.isHooked()) {
									ClearHookEntity(worldIn, itemStackIn); // Clear our hook
									return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
								}
								
								// We didn't handle, so pass to other
								return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
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
					if (playerIn.dimension.getId() == ModConfig.config.sorceryDimensionIndex()) {
						playerIn.sendMessage(new TranslationTextComponent("info.hookshot.bad_dim"));
					} else {
						EntityHookShot hook = new EntityHookShot(worldIn, playerIn, getMaxDistance(itemStackIn), 
								ProjectileTrigger.getVectorForRotation(playerIn.rotationPitch, playerIn.rotationYaw).scale(getVelocity(itemStackIn)),
								this.type);
						worldIn.addEntity(hook);
						SetHook(itemStackIn, hook);
						NostrumMagicaSounds.HOOKSHOT_FIRE.play(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ);
					}
				}
			}
		}
		
//		if (worldIn.isRemote && type == HookshotType.CLAW && hand == Hand.MAIN_HAND) {
//			@Nullable ItemStack offHandStack = playerIn.getHeldItemOffhand();
//			if (offHandStack != null && offHandStack.getItem() instanceof HookshotItem) {
//				return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
//			}
//		}
		
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
	}
	
	public int getMetadata(int damage) {
		return damage;
	}
	
	public static HookshotType GetType(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof HookshotItem)) {
			return null;
		}
		
		return ((HookshotItem) stack.getItem()).type;
	}
	
	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	public static void SetHook(ItemStack stack, EntityHookShot entity) {
		CompoundNBT tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundNBT();
		}
		
		if (entity == null) {
			tag.remove(NBT_HOOK_ID);
		} else {
			tag.putUniqueId(NBT_HOOK_ID, entity.getUniqueID());
		}
		
		stack.setTag(tag);
	}
	
	@Nullable
	public static EntityHookShot GetHookEntity(World world, ItemStack stack) {
		UUID id = GetHookID(stack);
		if (id != null) {
			Entity entity = Entities.FindEntity(world, id);
			if (entity != null && entity instanceof EntityHookShot) {
				return (EntityHookShot) entity;
			}
		}
		return null;
	}
	
	protected static UUID GetHookID(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof HookshotItem)) {
			return null;
		}
		
		CompoundNBT tag = stack.getTag();
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
			hook.remove();
		}
		
		SetHook(stack, null);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		
		if (IsExtended(stack)) {
			// Clear extended if we can't find the anchor entity or it's not in our hand anymore
			if (entityIn instanceof LivingEntity)  {
				LivingEntity holder = (LivingEntity) entityIn;
				if (!(holder.getHeldItem(Hand.MAIN_HAND) == stack || holder.getHeldItem(Hand.OFF_HAND) == stack)) {
					if (entityIn instanceof PlayerEntity) {
					}
					ClearHookEntity(worldIn, stack);
					return;
				}
			}
			
			EntityHookShot anchor = GetHookEntity(worldIn, stack);
			if (anchor == null) {
				ClearHookEntity(worldIn, stack);
				if (entityIn instanceof PlayerEntity) {
				}
				return;
			}
			
			// Detect two active hookshots (since claw bypass) and clear if we're older
			// 4 is mainhand //0 is offhand
			if (entityIn instanceof LivingEntity) {
				LivingEntity living = (LivingEntity) entityIn;
				final Hand hand = (itemSlot == 0 ? Hand.OFF_HAND : Hand.MAIN_HAND);
				@Nonnull final ItemStack otherHand = living.getHeldItem(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
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
		return GetVelocity(type);
	}
	
	protected double getMaxDistance(ItemStack stack) {
		return GetMaxDistance(type);
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
	
	public static String GetTypeSuffix(HookshotType type) {
		return type.name().toLowerCase();
	}
	
	public static boolean CanBeHooked(HookshotType type, BlockState blockState) {
		switch(type) {
		case STRONG:
		case CLAW:
			return true;
		case MEDIUM:
			if (blockState.getMaterial().isFlammable()) {
				return true;
			}
			// fall through
		case WEAK:
			if (blockState.getMaterial() == Material.WOOD) {
				return true;
			}
			if (blockState.getBlock() instanceof PaneBlock && blockState.getMaterial() == Material.IRON) {
				return true;
			}
			break;
		}
		
		return false;
	}
	
	public static boolean CanBeHooked(HookshotType type, Entity entity) {
		return entity instanceof ItemEntity || entity instanceof LivingEntity;
	}
	
//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public String getItemStackDisplayName(ItemStack stack) {
//		return I18n.format(this.getUnlocalizedName() + "_" + GetTypeSuffix(TypeFromMeta(stack.getMetadata())) + ".name", (Object[])null);
//	}

	@Override
	public boolean isElytraFlying(LivingEntity entityIn, ItemStack stack) {
		if (IsExtended(stack)) {
			// See if we're being pulled
			EntityHookShot anchor = GetHookEntity(entityIn.world, stack);
			return anchor != null && anchor.isPulling();
		}
		
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderElyta(LivingEntity entity, ItemStack stack) {
		return false;
	}
	
}
