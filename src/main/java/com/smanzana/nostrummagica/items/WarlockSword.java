package com.smanzana.nostrummagica.items;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.entity.IEntityTameable;
import com.smanzana.nostrummagica.entity.golem.EntityGolem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.utils.RayTrace;

import crazypants.enderio.api.teleport.IItemOfTravel;
import crazypants.enderio.api.teleport.TravelSource;
import info.loenwind.autoconfig.factory.IValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WarlockSword extends ItemSword implements ILoreTagged, ISpellArmor, IItemOfTravel, IRaytraceOverlay {

	public static String ID = "warlock_sword";
	private static final String NBT_LEVELS = "levels";
	private static final String NBT_CAPACITY = "capacity";
	private static final String NBT_ENDERIO_TRAVEL_CAP = "enderio_travel";
	
	private static WarlockSword instance = null;

	public static WarlockSword instance() {
		if (instance == null)
			instance = new WarlockSword();
	
		return instance;

	}

	public WarlockSword() {
		super(ToolMaterial.DIAMOND);
		this.setMaxDamage(1200);
		this.setUnlocalizedName(ID);
		this.setRegistryName(NostrumMagica.MODID, ID);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
	}
	
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND)
        {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 6, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.7000000953674316D, 0));
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
        return !repair.isEmpty() && repair.getItem() == NostrumResourceItem.instance()
        		&& NostrumResourceItem.getTypeFromMeta(repair.getMetadata()) == ResourceType.CRYSTAL_MEDIUM;
    }

	@Override
	public void apply(EntityLivingBase caster, SpellCastSummary summary, ItemStack stack) {
		// +10% potency
		summary.addEfficiency(.1f);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add("Magic Potency Bonus: 10%");
		
		boolean extra = Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode());
		
		Map<EMagicElement, Float> levels = getLevels(stack);
		for (EMagicElement elem : EMagicElement.values()) {
			Float f = levels.get(elem);
			if (f == null || (!extra && Math.floor(f) <= 0)) {
				continue;
			}
			
			String str = " + " + elem.getChatColor() + Math.floor(f) + " " + elem.getName() + ChatFormatting.RESET + " damage";
			if (extra) {
				str += " (" + Math.floor(100 * (f - Math.floor(f))) + "%)";
			}
			tooltip.add(str);
		}
		
		if (extra) {
			tooltip.add("Capacity: " + getCapacity(stack));
			if (hasEnderIOTravel(stack)) {
				tooltip.add(ChatFormatting.DARK_PURPLE + "EnderIO Travel Anchor Support" + ChatFormatting.RESET);
			}
		} else {
			tooltip.add("[Hold Shift]");			
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
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			return 0f;
		}
		
		return nbt.getCompoundTag(NBT_LEVELS).getFloat(element.name().toLowerCase());
	}
	
	public static ItemStack setLevel(ItemStack stack, EMagicElement element, float level) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		NBTTagCompound tag = nbt.getCompoundTag(NBT_LEVELS);
		tag.setFloat(element.name().toLowerCase(), Math.max(0, level));
		nbt.setTag(NBT_LEVELS, tag);
		stack.setTagCompound(nbt);
		return stack;
	}
	
	public static ItemStack addLevel(ItemStack stack, EMagicElement element, float diff) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		NBTTagCompound tag = nbt.getCompoundTag(NBT_LEVELS);
		float amt = tag.getFloat(element.name().toLowerCase());
		
		tag.setFloat(element.name().toLowerCase(), Math.max(0, amt + diff));
		nbt.setTag(NBT_LEVELS, tag);
		
		stack.setTagCompound(nbt);
		return stack;
	}
	
	public static int getCapacity(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			return 0;
		}
		
		return nbt.getInteger(NBT_CAPACITY);
	}
	
	public static ItemStack addCapacity(ItemStack stack, int diff) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		int amt = nbt.getInteger(NBT_CAPACITY);
		nbt.setInteger(NBT_CAPACITY, Math.max(0, amt+ diff));
		
		stack.setTagCompound(nbt);
		return stack;
	}
	
	public static boolean hasEnderIOTravel(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			return false;
		}
		
		return nbt.getBoolean(NBT_ENDERIO_TRAVEL_CAP);
	}
	
	public static ItemStack setEnderIOTravel(ItemStack stack, boolean hasTravel) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		nbt.setBoolean(NBT_ENDERIO_TRAVEL_CAP, hasTravel);
		stack.setTagCompound(nbt);
		return stack;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (this.isInCreativeTab(tab)) {
			subItems.add(addCapacity(new ItemStack(this), 10));
			subItems.add(setLevel(setLevel(setLevel(setLevel(
					new ItemStack(this),
					EMagicElement.PHYSICAL, 1),
					EMagicElement.FIRE, 2),
					EMagicElement.WIND, 2),
					EMagicElement.ENDER, 2));
			subItems.add(setLevel(setLevel(setLevel(setLevel(
					new ItemStack(this),
					EMagicElement.PHYSICAL, 1),
					EMagicElement.ICE, 2),
					EMagicElement.EARTH, 2),
					EMagicElement.LIGHTNING, 2));
			
			if (NostrumMagica.enderIO.isEnabled()) {
				subItems.add(setEnderIOTravel(addCapacity(new ItemStack(this), 10), true));
			}
		}
	}
	
	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		Map<EMagicElement, Float> levels = getLevels(stack);
		for (EMagicElement elem : EMagicElement.values()) {
			Float level = levels.get(elem);
			if (level != null && level >= 1f) {
				target.attackEntityFrom(new SpellAction.MagicDamageSource(attacker, elem), 
						SpellAction.calcDamage(attacker, target, (float) Math.floor(level), elem));
				target.setEntityInvulnerable(false);
				target.hurtResistantTime = 0;
			}
		}
		
		// Get experience if attacking a golem!
		if (target instanceof EntityGolem) {
			EntityGolem golem = (EntityGolem) target;
			awardExperience(stack, golem.getElement());
		} else if (target instanceof EntityKoid) {
			EntityKoid koid = (EntityKoid) target;
			awardExperience(stack, koid.getElement());
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
	
	private boolean canEnderTravel(ItemStack item, EntityPlayer player) {
		return hasEnderIOTravel(item)//getLevel(item, EMagicElement.ENDER) > 0
				&& (NostrumMagica.getMagicWrapper(player) != null)
				&& (NostrumMagica.getMagicWrapper(player).isUnlocked());
	}

	@Override
	public void extractInternal(ItemStack item, int power) {
		item.attemptDamageItem(1, NostrumMagica.rand, null);
	}

	@Override
	public int getEnergyStored(ItemStack item) {
		return 100000;
	}

	@Override
	public boolean isActive(EntityPlayer player, ItemStack item) {
		return player.isSneaking()
				&& canEnderTravel(item, player);
	}
	
	private static Spell[] MissleSpells = null;
	
	private static void InitMissleSpells() {
		if (MissleSpells == null) {
			MissleSpells = new Spell[EMagicElement.values().length];
			for (EMagicElement elem : EMagicElement.values()) {
				Spell spell = new Spell("WarlockMissle_" + elem.name(), true);
				spell.addPart(new SpellPart(SeekingBulletTrigger.instance()));
				spell.addPart(new SpellPart(SingleShape.instance(), elem, 1, null));
				MissleSpells[elem.ordinal()] = spell;
			}
		}
	}
	
	private static Spell GetMissleSpell(EMagicElement elem) {
		InitMissleSpells();
		return MissleSpells[elem.ordinal()];
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {

		final @Nonnull ItemStack stack = playerIn.getHeldItem(hand);
		if (playerIn.getCooledAttackStrength(0.5F) > .95) {
		
			// Earlier right-click stuff here
			if (!playerIn.isSneaking()) {
				if (!worldIn.isRemote) {
					// We have a target?
					RayTraceResult result = RayTrace.raytraceApprox(worldIn, playerIn.getPositionVector().addVector(0, playerIn.eyeHeight, 0),
							playerIn.rotationPitch, playerIn.rotationYaw, SeekingBulletTrigger.MAX_DIST, (ent) -> {
								if (ent != null && playerIn != ent) {
									if (ent instanceof IEntityTameable) {
										if (playerIn.getUniqueID().equals(((IEntityTameable) ent).getOwnerId())) {
											return false; // We own the target entity
										}
									}
								}
								
								return true;
							}, .5);
					
					if (result != null && result.entityHit != null) {
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
							stack.damageItem(1, playerIn);
							NostrumMagicaSounds.DAMAGE_LIGHTNING.play(playerIn);
						}
					}
				}
			} else {
				// else if nothign else, try client-side enderIO teleport?
				if (canEnderTravel(stack, playerIn)) {
					if (worldIn.isRemote) {
						NostrumMagica.enderIO.AttemptEnderIOTravel(stack, hand, worldIn, playerIn, TravelSource.STAFF);
					}
				}
			}
		}
		
		playerIn.resetCooldown();
		playerIn.swingArm(hand);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}
	
	@Override
	public boolean shouldTrace(World world, EntityPlayer player, ItemStack stack) {
		Map<EMagicElement, Float> power = getLevels(stack);
		for (EMagicElement elem : EMagicElement.values()) {
			Float val = power.get(elem);
			if (val != null && val >= 1f) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void extractInternal(ItemStack item, IValue<Integer> power) {
		extractInternal(item, power.get());
	}

}
