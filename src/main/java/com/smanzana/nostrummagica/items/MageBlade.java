package com.smanzana.nostrummagica.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.attributes.AttributeMagicPotency;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.MagicDamageSource;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MageBlade extends SwordItem implements ILoreTagged, ISpellArmor, IEnchantableItem {

	public static final String ID = "mage_blade";
	
	protected static UUID MAGEBLADE_POTENCY_UUID = UUID.fromString("83088ef2-c0cc-401d-999c-0aeaf9e511b5");
	private static final String NBT_ELEMENT = "element";
	private static final String NBT_CHARGES = "charges";
	
	public MageBlade() {
		super(ItemTier.DIAMOND, 3, -2.0F, NostrumItems.PropEquipment());
	}
	
	public @Nullable EMagicElement getElement(ItemStack stack) {
		EMagicElement stored = null;
		
		if (stack.hasTag() && stack.getTag().contains(NBT_ELEMENT)) {
			try {
				stored = EMagicElement.valueOf(stack.getTag().getString(NBT_ELEMENT));
			} catch (Exception e) {
				stored = null;
			}
		}
		
		return stored;
	}
	
	public void setElement(ItemStack stack, @Nullable EMagicElement element) {
		if (stack.isEmpty() || !(stack.getItem() instanceof MageBlade)) {
			return;
		}
		
		CompoundNBT tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundNBT();
		}
		
		if (element == null) {
			tag.remove(NBT_ELEMENT);
		} else {
			tag.putString(NBT_ELEMENT, element.name());
		}
		
		stack.setTag(tag);
	}
	
	public int getCharges(ItemStack stack) {
		int stored = 0;
		
		if (stack.hasTag()) {
			stored = stack.getTag().getInt(NBT_CHARGES);
		}
		
		return stored;
	}
	
	public void setCharges(ItemStack stack, int charges) {
		if (stack.isEmpty() || !(stack.getItem() instanceof MageBlade)) {
			return;
		}
		
		CompoundNBT tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundNBT();
		}
		
		if (charges <= 0) {
			tag.remove(NBT_CHARGES);
		} else {
			tag.putInt(NBT_CHARGES, charges);
		}
		
		stack.setTag(tag);
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
		Multimap<Attribute, AttributeModifier> multimap = super.getAttributeModifiers(equipmentSlot);//HashMultimap.<String, AttributeModifier>create();

		if (equipmentSlot == EquipmentSlotType.MAINHAND || equipmentSlot == EquipmentSlotType.OFFHAND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.putAll(multimap);
			builder.put(AttributeMagicPotency.instance(), new AttributeModifier(MAGEBLADE_POTENCY_UUID, "Potency modifier", 10, AttributeModifier.Operation.ADDITION));
			multimap = builder.build();
		}

        return multimap;
    }
	
	@Override
	public String getLoreKey() {
		return "nostrum_mage_blade";
	}

	@Override
	public String getLoreDisplayName() {
		return "Mage Blades";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("An enhanced blade that's sharp on it's own, but seems to be a good conductor of magical elements as well!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("An enhanced blade that's sharp on it's own, but seems to be a good conductor of magical elements as well!", "Can be enchanted with an enchant spell to add elemental damage to the blade!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		if (repair.isEmpty()) {
			return false;
		} else {
			return NostrumTags.Items.CrystalSmall.contains(repair.getItem());
		}
    }

	@Override
	public void apply(LivingEntity caster, SpellCastSummary summary, ItemStack stack) {
		// We provide -10% reagent cost, +20% potency
		summary.addReagentCost(-.1f);
		//summary.addEfficiency(.2f);
		ItemStacks.damageItem(stack, caster, caster.getHeldItem(Hand.MAIN_HAND) == stack ? Hand.MAIN_HAND : Hand.OFF_HAND, 1);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		//tooltip.add("Magic Potency Bonus: 20%");
		tooltip.add(new StringTextComponent("Reagent Cost Discount: 10%"));
	}
	
	protected void doEffect(LivingEntity entity, EMagicElement element) {
		NostrumParticles.GLOW_ORB.spawn(entity.world, new SpawnParams(
				3,
				entity.getPosX(), entity.getPosY() + entity.getHeight(), entity.getPosZ(), 1, 30, 5,
				new Vector3d(0, -0.05, 0), null
				).color(0x80000000 | (0x00FFFFFF & element.getColor())));
		NostrumMagicaSounds.DAMAGE_FIRE.play(entity);
	}
	
	protected void spendCharge(ItemStack stack) {
		int charges = this.getCharges(stack) - 1;
		this.setCharges(stack, charges);
		if (charges <= 0) {
			this.setElement(stack, null);
		}
	}
	
	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		// Add magic damage, but only if weapon cooldown is recovered
		final EMagicElement elem = this.getElement(stack);
		if (elem != null) {
			// Want to make sure you're not spam clicking it, but hitEntity is called after regular hit stuff including cooldown
			//if (!(attacker instanceof PlayerEntity) || ((PlayerEntity)attacker).getCooledAttackStrength(0.5F) > .95)
			{
				target.setInvulnerable(false);
				target.hurtResistantTime = 0;
				target.attackEntityFrom(new MagicDamageSource(attacker, elem), 
						SpellAction.calcDamage(attacker, target, 4f, elem));
				
				if (!target.world.isRemote()) {
					doEffect(target, elem);
					spendCharge(stack);
					
					if (this.getCharges(stack) <= 0) {
						NostrumMagicaSounds.HOOKSHOT_TICK.play(attacker);
					}
				}
			}
		}
		
		return super.hitEntity(stack, target, attacker);
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return true;
	}

	@Override
	public Result attemptEnchant(ItemStack stack, LivingEntity entity, EMagicElement element, int power) {
		final int charges = (int) (10 * Math.pow(2, power-1)); // 10, 20, 40, ...
		
		MageBlade blade = (MageBlade) stack.getItem();
		blade.setElement(stack, element);
		blade.setCharges(stack, charges);
		return new Result(true, ItemStack.EMPTY, false);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelElement(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
		final EMagicElement elem = ((MageBlade) stack.getItem()).getElement(stack);
		if (elem == null) {
			return 0;
		} else {
			return elem.ordinal() + 1;
		}
	}
	
}
