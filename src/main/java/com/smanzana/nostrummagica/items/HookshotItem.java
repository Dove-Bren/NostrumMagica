package com.smanzana.nostrummagica.items;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.EntityHookShot;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.integration.caelus.NostrumElytraWrapper;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.utils.Entities;
import com.smanzana.nostrummagica.world.dimension.NostrumDimensions;

import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class HookshotItem extends Item implements ILoreTagged, IElytraRenderer {
	
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
	
	private static final UUID MOD_MAINHAND_ID = UUID.fromString("f558274e-6a19-11ee-8c99-0242ac120002");
	private static final UUID MOD_OFFHAND_ID = UUID.fromString("f5582a82-6a19-11ee-8c99-0242ac120002");
	private static final AttributeModifier MAINHAND_ELYTRA_MODIFIER = NostrumElytraWrapper.MakeHasElytraModifier(MOD_MAINHAND_ID);
	private static final AttributeModifier OFFHAND_ELYTRA_MODIFIER = NostrumElytraWrapper.MakeHasElytraModifier(MOD_OFFHAND_ID);
	
	protected final HookshotType type;

	public HookshotItem(HookshotType type) {
		super(NostrumItems.PropUnstackable().rarity(type.rarity));
		this.type = type;
		
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
					if (playerIn.dimension == NostrumDimensions.EmptyDimension) {
						playerIn.sendMessage(new TranslationTextComponent("info.hookshot.bad_dim"));
					} else {
						EntityHookShot hook = new EntityHookShot(NostrumEntityTypes.hookShot, worldIn, playerIn, getMaxDistance(itemStackIn), 
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
			// Hack: tag.remove removes the passed in string, but putUniqueId creates two for a UUID :(
			//tag.remove(NBT_HOOK_ID);
			tag.remove(NBT_HOOK_ID + "Most");
			tag.remove(NBT_HOOK_ID + "Least");
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
		if (tag != null && tag.contains(NBT_HOOK_ID + "Most")) { // +"Most" because putUniqueId adds two tags
			id = tag.getUniqueId(NBT_HOOK_ID); // Returns an all-zero UUID if not present
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
		
		// todo: should be isRemote check?
		
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
			
			// If still extended, tick attributes and set pose if flying
			if (IsExtended(stack) && entityIn instanceof ServerPlayerEntity) {
				ServerPlayerEntity player = (ServerPlayerEntity) entityIn;
				updateEntityHookshotAttributes(player);
				
				// Assumes previous check that we must be in mainhand or offhand to be extended has happened
				final Hand hand = (itemSlot == 0 ? Hand.OFF_HAND : Hand.MAIN_HAND);
				if (isPulling(player, hand)) {
					((ServerPlayerEntity) player).setElytraFlying();
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

//	@Override
//	public boolean isElytraFlying(LivingEntity entityIn, ItemStack stack) {
//		if (IsExtended(stack)) {
//			// See if we're being pulled
//			EntityHookShot anchor = GetHookEntity(entityIn.world, stack);
//			return anchor != null && anchor.isPulling();
//		}
//		
//		return false;
//	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderElyta(LivingEntity entity, ItemStack stack) {
		return false;
	}
	
	@SubscribeEvent
	public static void onEntityEquipmentChange(LivingEquipmentChangeEvent event) {
		if (event.isCanceled()) {
			return;
		}
		
		if ((!event.getFrom().isEmpty() && event.getFrom().getItem() instanceof HookshotItem)
			|| (!event.getTo().isEmpty() && event.getTo().getItem() instanceof HookshotItem)) {
			// Changed from or to a hookshot. Regardless, update state.
			updateEntityHookshotAttributes(event.getEntityLiving());
		}
	}
	
	private static final boolean isPulling(LivingEntity entity, Hand hand) {
		ItemStack stack = entity.getHeldItem(hand);
		if (stack.isEmpty() || !(stack.getItem() instanceof HookshotItem)) {
			return false;
		}
		
		if (IsExtended(stack)) {
			// See if we're being pulled
			EntityHookShot anchor = GetHookEntity(entity.world, stack);
			return anchor != null && anchor.isPulling();
		}
		
		return false;
	}
	
	private static final void updateEntityHookshotAttributes(LivingEntity entity) {
		setEntityHookshotAttributes(entity,
				isPulling(entity, Hand.MAIN_HAND),
				isPulling(entity, Hand.OFF_HAND)
				);
	}
	
	private static final void setEntityHookshotAttributes(LivingEntity entity, boolean pullingMainhand, boolean pullingOffhand) {
		if (pullingMainhand) {
			NostrumElytraWrapper.AddElytraModifier(entity, MAINHAND_ELYTRA_MODIFIER);
		} else {
			NostrumElytraWrapper.RemoveElytraModifier(entity, MAINHAND_ELYTRA_MODIFIER);
		}
		
		if (pullingOffhand) {
			NostrumElytraWrapper.AddElytraModifier(entity, OFFHAND_ELYTRA_MODIFIER);
		} else {
			NostrumElytraWrapper.RemoveElytraModifier(entity, OFFHAND_ELYTRA_MODIFIER);
		}
	}
	
}
