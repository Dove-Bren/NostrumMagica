package com.smanzana.nostrummagica.items.equipment;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.NostrumAttributes;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.entity.IElementalEntity;
import com.smanzana.nostrummagica.items.IRaytraceOverlay;
import com.smanzana.nostrummagica.items.ISpellEquipment;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.MagicDamageSource;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.spells.components.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spells.components.shapes.SeekingBulletShape;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.utils.ItemStacks;
import com.smanzana.nostrummagica.utils.RayTrace;
import com.smanzana.nostrummagica.utils.SpellUtils;
import com.smanzana.petcommand.api.entity.ITameableEntity;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.Rarity;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WarlockSword extends SwordItem implements ILoreTagged, ISpellEquipment, IRaytraceOverlay {

	public static final String ID = "warlock_sword";
	private static final String NBT_LEVELS = "levels";
	private static final String NBT_CAPACITY = "capacity";
	private static final String NBT_ENDERIO_TRAVEL_CAP = "enderio_travel";
	
	private static final UUID WARLOCKBLADE_POTENCY_UUID = UUID.fromString("2d5dd2dc-3f5c-4dce-be8f-fa93627fe560");
	
	public WarlockSword() {
		super(ItemTier.DIAMOND, 3, -2.4F, NostrumItems.PropEquipment().maxDamage(1200).rarity(Rarity.UNCOMMON));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();

		if (equipmentSlot == EquipmentSlotType.MAINHAND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.putAll(multimap);
			builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 7, AttributeModifier.Operation.ADDITION));
			builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.7000000953674316D, AttributeModifier.Operation.ADDITION));
			builder.put(NostrumAttributes.magicPotency, new AttributeModifier(WARLOCKBLADE_POTENCY_UUID, "Potency modifier", 10, AttributeModifier.Operation.ADDITION));
			multimap = builder.build();
		}

		return multimap;
    }
	
	@Override
	public String getLoreKey() {
		return "nostrum_warlock_sword";
	}

	@Override
	public String getLoreDisplayName() {
		return "Warlock Blades";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return !repair.isEmpty() && NostrumTags.Items.CrystalMedium.contains(repair.getItem());
    }

	@Override
	public void apply(LivingEntity caster, SpellCastSummary summary, ItemStack stack) {
		// +10% potency
		//summary.addEfficiency(.1f);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		
		boolean extra = Screen.hasShiftDown();
		
		Map<EMagicElement, Float> levels = getLevels(stack);
		for (EMagicElement elem : EMagicElement.values()) {
			Float f = levels.get(elem);
			if (f == null || (!extra && Math.floor(f) <= 0)) {
				continue;
			}
			
			String str = " + " + elem.getChatColor() + Math.floor(f) + " " + elem.getName() + TextFormatting.RESET + " damage";
			if (extra) {
				str += " (" + Math.floor(100 * (f - Math.floor(f))) + "%)";
			}
			tooltip.add(new StringTextComponent(str));
		}
		
		if (extra) {
			tooltip.add(new StringTextComponent("Capacity: " + getCapacity(stack)));
			if (hasEnderIOTravel(stack)) {
				tooltip.add(new StringTextComponent("EnderIO Travel Anchor Support").mergeStyle(TextFormatting.DARK_PURPLE));
			}
		} else {
			tooltip.add(new StringTextComponent("[Hold Shift]"));			
		}
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			items.add(addCapacity(new ItemStack(this), 10));
			items.add(setLevel(setLevel(setLevel(setLevel(
					new ItemStack(this),
					EMagicElement.PHYSICAL, 1),
					EMagicElement.FIRE, 2),
					EMagicElement.WIND, 2),
					EMagicElement.ENDER, 2));
			items.add(setLevel(setLevel(setLevel(setLevel(
					new ItemStack(this),
					EMagicElement.PHYSICAL, 1),
					EMagicElement.ICE, 2),
					EMagicElement.EARTH, 2),
					EMagicElement.LIGHTNING, 2));
		}
	}
	
	public static Map<EMagicElement, Float> getLevels(ItemStack stack) {
		Map<EMagicElement, Float> map = new EnumMap<>(EMagicElement.class);
		for (EMagicElement elem : EMagicElement.values()) {
			map.put(elem, getLevel(stack, elem));
		}
		
		return map;
	}
	
	public static Float getLevel(ItemStack stack, EMagicElement element) {
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			return 0f;
		}
		
		return nbt.getCompound(NBT_LEVELS).getFloat(element.name().toLowerCase());
	}
	
	public static ItemStack setLevel(ItemStack stack, EMagicElement element, float level) {
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		CompoundNBT tag = nbt.getCompound(NBT_LEVELS);
		tag.putFloat(element.name().toLowerCase(), Math.max(0, level));
		nbt.put(NBT_LEVELS, tag);
		stack.setTag(nbt);
		return stack;
	}
	
	public static ItemStack addLevel(ItemStack stack, EMagicElement element, float diff) {
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		CompoundNBT tag = nbt.getCompound(NBT_LEVELS);
		float amt = tag.getFloat(element.name().toLowerCase());
		
		tag.putFloat(element.name().toLowerCase(), Math.max(0, amt + diff));
		nbt.put(NBT_LEVELS, tag);
		
		stack.setTag(nbt);
		return stack;
	}
	
	public static int getCapacity(ItemStack stack) {
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			return 0;
		}
		
		return nbt.getInt(NBT_CAPACITY);
	}
	
	public static ItemStack addCapacity(ItemStack stack, int diff) {
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		int amt = nbt.getInt(NBT_CAPACITY);
		nbt.putInt(NBT_CAPACITY, Math.max(0, amt+ diff));
		
		stack.setTag(nbt);
		return stack;
	}
	
	public static boolean hasEnderIOTravel(ItemStack stack) {
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			return false;
		}
		
		return nbt.getBoolean(NBT_ENDERIO_TRAVEL_CAP);
	}
	
	public static ItemStack setEnderIOTravel(ItemStack stack, boolean hasTravel) {
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		nbt.putBoolean(NBT_ENDERIO_TRAVEL_CAP, hasTravel);
		stack.setTag(nbt);
		return stack;
	}
	
//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
//		if (this.isInCreativeTab(tab)) {
//			subItems.add(addCapacity(new ItemStack(this), 10));
//			subItems.add(setLevel(setLevel(setLevel(setLevel(
//					new ItemStack(this),
//					EMagicElement.PHYSICAL, 1),
//					EMagicElement.FIRE, 2),
//					EMagicElement.WIND, 2),
//					EMagicElement.ENDER, 2));
//			subItems.add(setLevel(setLevel(setLevel(setLevel(
//					new ItemStack(this),
//					EMagicElement.PHYSICAL, 1),
//					EMagicElement.ICE, 2),
//					EMagicElement.EARTH, 2),
//					EMagicElement.LIGHTNING, 2));
//			
//			if (NostrumMagica.enderIO.isEnabled()) {
//				subItems.add(setEnderIOTravel(addCapacity(new ItemStack(this), 10), true));
//			}
//		}
//	}
	
	public static void doEffect(LivingEntity entity, EMagicElement element) {
		if (entity.world.isRemote) {
			return;
		}
		
		NostrumParticles.GLOW_ORB.spawn(entity.world, new SpawnParams(
				3,
				entity.getPosX(), entity.getPosY() + entity.getHeight(), entity.getPosZ(), 1, 30, 5,
				new Vector3d(0, -0.05, 0), null
				).color(0x80000000 | (0x00FFFFFF & element.getColor())));
	}
	
	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		
		// Add magic damage, but only if weapon cooldown is recovered
		// Except hitEntity is called after cooldown is checked and reset, so can't actually check
		//if (!(attacker instanceof PlayerEntity) || ((PlayerEntity)attacker).getCooledAttackStrength(0.5F) > .95) {
		{
			Map<EMagicElement, Float> levels = getLevels(stack);
			for (EMagicElement elem : EMagicElement.values()) {
				Float level = levels.get(elem);
				if (level != null && level >= 1f) {
					target.hurtResistantTime = 0;
					target.attackEntityFrom(new MagicDamageSource(attacker, elem), 
							SpellAction.calcDamage(attacker, target, (float) Math.floor(level), elem));
					doEffect(target, elem);
				}
			}
		}
		
		// Get experience if attacking an elemental!
		if (target instanceof IElementalEntity) {
			EMagicElement element = ((IElementalEntity) target).getElement();
			if (element != null) {
				awardExperience(stack, element);
			}
		}
		
		return super.hitEntity(stack, target, attacker);
	}
	
	public static void awardExperience(ItemStack stack, EMagicElement elem) {
		awardExperience(stack, elem, NostrumMagica.rand.nextFloat() * 0.2f);
	}
	
	public static void awardExperience(ItemStack stack, EMagicElement elem, float amt) {
		amt = Math.min(amt, 1f);
		float f = getLevel(stack, elem);
		final int cur = (int) Math.floor(f);
		f += amt;
		
		// if we move up a level, need to consume capacity
		final int attempted = (int) Math.floor(f);
		if (cur == attempted) {
			setLevel(stack, elem, f);
		} else {
			final int capacity = getCapacity(stack);
			if (capacity >= attempted) {
				setLevel(stack, elem, f);
				addCapacity(stack, -attempted);
			} else {
				// Can't bump level. Cap at 99%
				setLevel(stack, elem, cur + 0.99f);
			}
		}
	}
	
	private boolean canEnderTravel(ItemStack item, PlayerEntity player) {
		return hasEnderIOTravel(item)//getLevel(item, EMagicElement.ENDER) > 0
				&& (NostrumMagica.getMagicWrapper(player) != null)
				&& (NostrumMagica.getMagicWrapper(player).isUnlocked());
	}

	private static Spell[] MissleSpells = null;
	
	private static void InitMissleSpells() {
		if (MissleSpells == null) {
			MissleSpells = new Spell[EMagicElement.values().length];
			for (EMagicElement elem : EMagicElement.values()) {
				Spell spell = SpellUtils.MakeSpell("WarlockMissle_" + elem.name(),
						NostrumSpellShapes.SeekingBullet,
						elem, 1, null);
				MissleSpells[elem.ordinal()] = spell;
			}
		}
	}
	
	private static Spell GetMissleSpell(EMagicElement elem) {
		InitMissleSpells();
		return MissleSpells[elem.ordinal()];
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {

		final @Nonnull ItemStack stack = playerIn.getHeldItem(hand);
		if (playerIn.getCooledAttackStrength(0.5F) > .95) {
		
			// Earlier right-click stuff here
			if (playerIn.isSneaking()) {
				// else if nothign else, try client-side enderIO teleport?
				if (canEnderTravel(stack, playerIn)) {
					if (worldIn.isRemote) {
//						if (NostrumMagica.instance.enderIO.AttemptEnderIOTravel(stack, hand, worldIn, playerIn, TravelSourceWrapper.STAFF)) {
//							playerIn.resetCooldown();
//							playerIn.swingArm(hand);
//							return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
//						}
					}
				}
			}
		}
		
		return new ActionResult<ItemStack>(ActionResultType.PASS, stack);
	}
	
	@Override
	public boolean shouldTrace(World world, PlayerEntity player, ItemStack stack) {
		Map<EMagicElement, Float> power = getLevels(stack);
		for (EMagicElement elem : EMagicElement.values()) {
			Float val = power.get(elem);
			if (val != null && val >= 1f) {
				return true;
			}
		}
		
		return false;
	}

	protected boolean tryCast(World worldIn, PlayerEntity playerIn, Hand hand, ItemStack stack) {
		boolean used = false;
		if (playerIn.getCooledAttackStrength(0.5F) > .95) {
			
			// Earlier right-click stuff here
			if (!worldIn.isRemote) {
				// We have a target?
				RayTraceResult result = RayTrace.raytraceApprox(worldIn, playerIn, playerIn.getPositionVec().add(0, playerIn.getEyeHeight(), 0),
						playerIn.rotationPitch, playerIn.rotationYaw, SeekingBulletShape.MAX_DIST, (ent) -> {
							if (ent != null && playerIn != ent) {
								if (ent instanceof ITameableEntity && ((ITameableEntity) ent).getOwner() != null) {
									if (playerIn.getUniqueID().equals(((ITameableEntity) ent).getOwner().getUniqueID())) {
										return false; // We own the target entity
									}
								}
							}
							
							return true;
						}, .5);
				
				
				if (RayTrace.entFromRaytrace(result) != null) {
					boolean any = false;
					Map<EMagicElement, Float> power = getLevels(stack);
					for (EMagicElement elem : EMagicElement.values()) {
						Float val = power.get(elem);
						if (val != null && val >= 1f) {
							Spell missle = GetMissleSpell(elem);
							missle.cast(playerIn, .5f * (int) (float) val);
							any = true;
						}
					}
					
					if (any) {
						ItemStacks.damageItem(stack, playerIn, hand, 1);
						NostrumMagicaSounds.DAMAGE_LIGHTNING.play(playerIn);
						playerIn.resetCooldown();
						playerIn.swingArm(hand);
						used = true;
					}
				}
			}
		}
		
		return used;
	}
	
	public static boolean DoCast(PlayerEntity player) {
		// Try to find weapon
		Hand hand = Hand.MAIN_HAND;
		@Nonnull ItemStack stack = player.getHeldItem(hand);
		if (stack.getItem() instanceof WarlockSword) {
			if (((WarlockSword) stack.getItem()).tryCast(player.world, player, hand, stack)) {
				return true;
			}
		}
		
		// Try with offhand
		hand = Hand.OFF_HAND;
		stack = player.getHeldItem(hand);
		if (stack.getItem() instanceof WarlockSword) {
			if (((WarlockSword) stack.getItem()).tryCast(player.world, player, hand, stack)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public double getTraceRange(World world, PlayerEntity player, ItemStack stack) {
		return SeekingBulletShape.MAX_DIST;
	}

}
