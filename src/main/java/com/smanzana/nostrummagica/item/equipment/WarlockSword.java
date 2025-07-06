package com.smanzana.nostrummagica.item.equipment;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.entity.IElementalEntity;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.api.IRaytraceOverlay;
import com.smanzana.nostrummagica.item.api.ISpellEquipment;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCastProperties;
import com.smanzana.nostrummagica.spell.SpellDamage;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.component.shapes.SeekingBulletShape;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.util.ItemStacks;
import com.smanzana.nostrummagica.util.RayTrace;
import com.smanzana.nostrummagica.util.SpellUtils;
import com.smanzana.petcommand.api.entity.ITameableEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WarlockSword extends SwordItem implements ILoreTagged, ISpellEquipment, IRaytraceOverlay {

	public static final String ID = "warlock_sword";
	private static final String NBT_LEVELS = "levels";
	private static final String NBT_CAPACITY = "capacity";
	private static final String NBT_ENDERIO_TRAVEL_CAP = "enderio_travel";
	
	private static final UUID WARLOCKBLADE_POTENCY_UUID = UUID.fromString("2d5dd2dc-3f5c-4dce-be8f-fa93627fe560");
	
	public WarlockSword() {
		super(Tiers.DIAMOND, 3, -2.4F, NostrumItems.PropEquipment().durability(1200).rarity(Rarity.UNCOMMON));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = HashMultimap.<Attribute, AttributeModifier>create();

		if (equipmentSlot == EquipmentSlot.MAINHAND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.putAll(multimap);
			builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 7, AttributeModifier.Operation.ADDITION));
			builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.7000000953674316D, AttributeModifier.Operation.ADDITION));
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
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return !repair.isEmpty() && repair.is(NostrumTags.Items.CrystalMedium);
    }

	@Override
	public void apply(LivingEntity caster, Spell spell, SpellCastSummary summary, ItemStack stack) {
		// +10% potency
		//summary.addEfficiency(.1f);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		
		boolean extra = Screen.hasShiftDown();
		
		Map<EMagicElement, Float> levels = getLevels(stack);
		for (EMagicElement elem : EMagicElement.values()) {
			Float f = levels.get(elem);
			if (f == null || (!extra && Math.floor(f) <= 0)) {
				continue;
			}
			
			String str = " + " + elem.getChatColor() + Math.floor(f) + " " + elem.getBareName() + ChatFormatting.RESET + " damage";
			if (extra) {
				str += " (" + Math.floor(100 * (f - Math.floor(f))) + "%)";
			}
			tooltip.add(new TextComponent(str));
		}
		
		if (extra) {
			tooltip.add(new TextComponent("Capacity: " + getCapacity(stack)));
			if (hasEnderIOTravel(stack)) {
				tooltip.add(new TextComponent("EnderIO Travel Anchor Support").withStyle(ChatFormatting.DARK_PURPLE));
			}
		} else {
			tooltip.add(new TextComponent("[Hold Shift]"));			
		}
	}
	
	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		if (this.allowdedIn(group)) {
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
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			return 0f;
		}
		
		return nbt.getCompound(NBT_LEVELS).getFloat(element.name().toLowerCase());
	}
	
	public static ItemStack setLevel(ItemStack stack, EMagicElement element, float level) {
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
		}
		
		CompoundTag tag = nbt.getCompound(NBT_LEVELS);
		tag.putFloat(element.name().toLowerCase(), Math.max(0, level));
		nbt.put(NBT_LEVELS, tag);
		stack.setTag(nbt);
		return stack;
	}
	
	public static ItemStack addLevel(ItemStack stack, EMagicElement element, float diff) {
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
		}
		
		CompoundTag tag = nbt.getCompound(NBT_LEVELS);
		float amt = tag.getFloat(element.name().toLowerCase());
		
		tag.putFloat(element.name().toLowerCase(), Math.max(0, amt + diff));
		nbt.put(NBT_LEVELS, tag);
		
		stack.setTag(nbt);
		return stack;
	}
	
	public static int getCapacity(ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			return 0;
		}
		
		return nbt.getInt(NBT_CAPACITY);
	}
	
	public static ItemStack addCapacity(ItemStack stack, int diff) {
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
		}
		
		int amt = nbt.getInt(NBT_CAPACITY);
		nbt.putInt(NBT_CAPACITY, Math.max(0, amt+ diff));
		
		stack.setTag(nbt);
		return stack;
	}
	
	public static boolean hasEnderIOTravel(ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			return false;
		}
		
		return nbt.getBoolean(NBT_ENDERIO_TRAVEL_CAP);
	}
	
	public static ItemStack setEnderIOTravel(ItemStack stack, boolean hasTravel) {
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
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
		if (entity.level.isClientSide) {
			return;
		}
		
		NostrumParticles.GLOW_ORB.spawn(entity.level, new SpawnParams(
				3,
				entity.getX(), entity.getY() + entity.getBbHeight(), entity.getZ(), 1, 30, 5,
				new Vec3(0, -0.05, 0), null
				).color(0x80000000 | (0x00FFFFFF & element.getColor())));
	}
	
	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		
		// Add magic damage, but only if weapon cooldown is recovered
		// Except hitEntity is called after cooldown is checked and reset, so can't actually check
		//if (!(attacker instanceof PlayerEntity) || ((PlayerEntity)attacker).getCooledAttackStrength(0.5F) > .95) {
		{
			Map<EMagicElement, Float> levels = getLevels(stack);
			for (EMagicElement elem : EMagicElement.values()) {
				Float level = levels.get(elem);
				if (level != null && level >= 1f) {
					target.invulnerableTime = 0;
					SpellDamage.DamageEntity(target, elem, (float) Math.floor(level), attacker);
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
		
		return super.hurtEnemy(stack, target, attacker);
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
	
	private boolean canEnderTravel(ItemStack item, Player player) {
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
						elem, 1, EAlteration.HARM);
				MissleSpells[elem.ordinal()] = spell;
			}
		}
	}
	
	private static Spell GetMissleSpell(EMagicElement elem) {
		InitMissleSpells();
		return MissleSpells[elem.ordinal()];
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {

		final @Nonnull ItemStack stack = playerIn.getItemInHand(hand);
		if (playerIn.getAttackStrengthScale(0.5F) > .95) {
		
			// Earlier right-click stuff here
			if (playerIn.isShiftKeyDown()) {
				// else if nothign else, try client-side enderIO teleport?
				if (canEnderTravel(stack, playerIn)) {
					if (worldIn.isClientSide) {
//						if (NostrumMagica.instance.enderIO.AttemptEnderIOTravel(stack, hand, worldIn, playerIn, TravelSourceWrapper.STAFF)) {
//							playerIn.resetCooldown();
//							playerIn.swingArm(hand);
//							return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
//						}
					}
				}
			}
		}
		
		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, stack);
	}
	
	@Override
	public boolean shouldTrace(Level world, Player player, ItemStack stack) {
		Map<EMagicElement, Float> power = getLevels(stack);
		for (EMagicElement elem : EMagicElement.values()) {
			Float val = power.get(elem);
			if (val != null && val >= 1f) {
				return true;
			}
		}
		
		return false;
	}

	protected boolean tryCast(Level worldIn, Player playerIn, InteractionHand hand, ItemStack stack) {
		boolean used = false;
		if (playerIn.getAttackStrengthScale(0.5F) > .95) {
			
			// Earlier right-click stuff here
			if (!worldIn.isClientSide) {
				// We have a target?
				HitResult result = RayTrace.raytraceApprox(worldIn, playerIn, playerIn.position().add(0, playerIn.getEyeHeight(), 0),
						playerIn.getXRot(), playerIn.getYRot(), SeekingBulletShape.MAX_DIST, (ent) -> {
							if (ent != null && playerIn != ent) {
								if (ent instanceof ITameableEntity && ((ITameableEntity) ent).getOwner() != null) {
									if (playerIn.getUUID().equals(((ITameableEntity) ent).getOwner().getUUID())) {
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
							missle.cast(playerIn, SpellCastProperties.makeSimple(.5f * (int) (float) val));
							any = true;
						}
					}
					
					if (any) {
						ItemStacks.damageItem(stack, playerIn, hand, 1);
						NostrumMagicaSounds.DAMAGE_LIGHTNING.play(playerIn);
						playerIn.resetAttackStrengthTicker();
						playerIn.swing(hand);
						used = true;
					}
				}
			}
		}
		
		return used;
	}
	
	public static boolean DoCast(Player player) {
		// Try to find weapon
		InteractionHand hand = InteractionHand.MAIN_HAND;
		@Nonnull ItemStack stack = player.getItemInHand(hand);
		if (stack.getItem() instanceof WarlockSword) {
			if (((WarlockSword) stack.getItem()).tryCast(player.level, player, hand, stack)) {
				return true;
			}
		}
		
		// Try with offhand
		hand = InteractionHand.OFF_HAND;
		stack = player.getItemInHand(hand);
		if (stack.getItem() instanceof WarlockSword) {
			if (((WarlockSword) stack.getItem()).tryCast(player.level, player, hand, stack)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public double getTraceRange(Level world, Player player, ItemStack stack) {
		return SeekingBulletShape.MAX_DIST;
	}

}
