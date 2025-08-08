package com.smanzana.nostrummagica.item.equipment;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.listener.ClientPlayerListener;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.api.ICrystalEnchantableItem;
import com.smanzana.nostrummagica.item.api.IRaytraceOverlay;
import com.smanzana.nostrummagica.loretag.IItemLoreTagged;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCastProperties;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.component.shapes.ProjectileShape;
import com.smanzana.nostrummagica.util.ItemStacks;
import com.smanzana.nostrummagica.util.SpellUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ProjectileStaff extends SwordItem implements IItemLoreTagged, IRaytraceOverlay, ICrystalEnchantableItem {

	public static final String ID = "staff_projectile";
	private static final double CAST_RANGE = ProjectileShape.PROJECTILE_RANGE;
	private static final int MANA_COST = 40;
	private static final String NBT_ELEMENT = "element";
	
	protected static UUID PROJECTILESTAFF_POTENCY_UUID = UUID.fromString("ccb2050f-d6bf-4d78-8100-d47cd66631d6");
	
	protected static Map<EMagicElement, Spell> Spells = null;
	
	protected Multimap<Attribute, AttributeModifier> defaultMainhandAttribs;
	protected Multimap<Attribute, AttributeModifier> defaultOffhandAttribs;
	
	public ProjectileStaff() {
		super(Tiers.GOLD, 2, -2.6F, NostrumItems.PropEquipment().durability(1240));
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		if (defaultMainhandAttribs == null) {
			defaultMainhandAttribs = makeMainhandAttribs();
			defaultOffhandAttribs = makeOffhandAttribs();
		}
		return EquipmentSlot.MAINHAND == equipmentSlot ? defaultMainhandAttribs : 
			EquipmentSlot.OFFHAND == equipmentSlot ? defaultOffhandAttribs :
				super.getDefaultAttributeModifiers(equipmentSlot);
	}
	
	protected Multimap<Attribute, AttributeModifier> makeMainhandAttribs() {
		Multimap<Attribute, AttributeModifier> multimap = super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.putAll(multimap);
		
		builder.put(NostrumAttributes.magicPotency, new AttributeModifier(PROJECTILESTAFF_POTENCY_UUID, "Potency modifier", 25, AttributeModifier.Operation.ADDITION));
		
		return builder.build();
    }
	
	protected Multimap<Attribute, AttributeModifier> makeOffhandAttribs() {
		Multimap<Attribute, AttributeModifier> multimap = super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.putAll(multimap);
		
		builder.put(NostrumAttributes.magicPotency, new AttributeModifier(PROJECTILESTAFF_POTENCY_UUID, "Potency modifier", 25, AttributeModifier.Operation.ADDITION));
		
		return builder.build();
    }
	
	public @Nullable EMagicElement getElement(ItemStack stack) {
		EMagicElement stored = EMagicElement.NEUTRAL;
		
		if (stack.hasTag() && stack.getTag().contains(NBT_ELEMENT)) {
			try {	
				stored = EMagicElement.parse(stack.getTag().getString(NBT_ELEMENT));
			} catch (Exception e) {
				stored = EMagicElement.NEUTRAL;
			}
		}
		
		return stored;
	}
	
	public void setElement(ItemStack stack, @Nullable EMagicElement element) {
		if (stack.isEmpty() || !(stack.getItem() instanceof ProjectileStaff)) {
			return;
		}
		
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundTag();
		}
		
		if (element == null) {
			tag.remove(NBT_ELEMENT);
		} else {
			tag.putString(NBT_ELEMENT, element.name());
		}
		
		stack.setTag(tag);
	}
	
	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		if (repair.isEmpty()) {
			return false;
		} else {
			return repair.is(NostrumTags.Items.CrystalSmall);
		}
    }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}
	
	protected static final void InitSpells() {
		Spells = new EnumMap<>(EMagicElement.class);
		for (EMagicElement elem : EMagicElement.values()) {
			Spells.put(elem, SpellUtils.MakeSpell("proj_staff_%s".formatted(elem.name().toLowerCase()), 
					NostrumSpellShapes.Projectile,
					elem, 2, null
					));
		}
	}
	
	protected static final Spell GetSpell(EMagicElement element) {
		if (Spells == null) {
			InitSpells();
		}
		return Spells.get(element);
	}
	
	protected boolean castStaff(Level worldIn, LivingEntity caster, ItemStack staff) {
		@Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
		if (attr == null || attr.getMana() < MANA_COST) {
			return false;
		}
		
		if (!(caster instanceof Player player) || !player.isCreative()) {
			attr.addMana(-MANA_COST);
			if (caster instanceof Player player) {
				NostrumMagica.Proxy.sendMana(player);
			}
		}
		
		if (!worldIn.isClientSide()) {
			GetSpell(this.getElement(staff)).cast(caster, SpellCastProperties.BASE);
		}
		
		return true;
	}
	
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
//		if (!playerIn.level.isClientSide()) {
//			if (castOn(playerIn, target)) {
//				ItemStacks.damageItem(stack, playerIn, playerIn.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, 1);
//			}
//		}
		
		return InteractionResult.PASS;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final ItemStack stack = playerIn.getItemInHand(hand);
		
		if (playerIn.getAttackStrengthScale(1f) < .8f) {
			return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, stack); 
		}
		
		if (castStaff(worldIn, playerIn, stack)) {
			if (!worldIn.isClientSide()) {
				ItemStacks.damageItem(stack, playerIn, hand, 1);
			}
		} else {
			if (worldIn.isClientSide()) {
				((ClientPlayerListener) NostrumMagica.playerListener).doManaWiggle(2);
				NostrumMagicaSounds.CAST_FAIL.play(playerIn);
			}
		}
		
		playerIn.swing(hand);
		playerIn.getCooldowns().addCooldown(this, 20);
		return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public boolean shouldTrace(Level world, Player player, ItemStack stack) {
		return true;
	}

	@Override
	public double getTraceRange(Level world, Player player, ItemStack stack) {
		return CAST_RANGE;
	}

	@Override
	public Item getItem() {
		return this;
	}

	@Override
	public ResourceLocation getItemRegistryName() {
		return this.getRegistryName();
	}

	@Override
	public boolean canEnchant(ItemStack stack, EMagicElement element) {
		return this.getElement(stack) == null ? (element != EMagicElement.NEUTRAL) : (this.getElement(stack) != element);
	}

	@Override
	public Result attemptEnchant(ItemStack stack, EMagicElement element) {
		ProjectileStaff staff = (ProjectileStaff) stack.getItem();
		staff.setElement(stack, element);
		return new Result(true, stack);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelElement(ItemStack stack, @Nullable Level worldIn, @Nullable LivingEntity entityIn, int entID) {
		final EMagicElement elem = ((ProjectileStaff) stack.getItem()).getElement(stack);
		if (elem == null || elem == EMagicElement.NEUTRAL) {
			return 0;
		} else {
			return elem.ordinal() + 1;
		}
	}

}
