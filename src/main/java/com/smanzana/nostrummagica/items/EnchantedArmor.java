package com.smanzana.nostrummagica.items;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.AttributeMagicReduction;
import com.smanzana.nostrummagica.attributes.AttributeMagicResist;
import com.smanzana.nostrummagica.client.model.ModelEnchantedArmorBase;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellAction;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.EntityEquipmentSlot.Type;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EnchantedArmor extends ItemArmor implements EnchantedEquipment, ISpecialArmor {

	private static Map<EMagicElement, Map<EntityEquipmentSlot, Map<Integer, EnchantedArmor>>> items;
	
	public static final void registerArmors() {
		items = new EnumMap<EMagicElement, Map<EntityEquipmentSlot, Map<Integer, EnchantedArmor>>>(EMagicElement.class);
		for (EMagicElement element : EMagicElement.values()) {
			if (isArmorElement(element)) {
				items.put(element, new EnumMap<EntityEquipmentSlot, Map<Integer, EnchantedArmor>>(EntityEquipmentSlot.class));
				for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
					if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
						items.get(element).put(slot, new HashMap<Integer, EnchantedArmor>());
						for (int i = 0; i < 3; i++) {
							ResourceLocation location = new ResourceLocation(NostrumMagica.MODID, "armor_" + slot.name().toLowerCase() + "_" + element.name().toLowerCase() + (i + 1));
							EnchantedArmor armor =  new EnchantedArmor(location.getResourcePath(), slot, element, i);
							armor.setUnlocalizedName(location.getResourcePath());
							GameRegistry.register(armor, location);
							items.get(element).get(slot).put(i + 1, armor);
						}
					}
				}
			}
		}
	}
	
	public static boolean isArmorElement(EMagicElement element) {
		
		switch (element) {
		case EARTH:
		case ENDER:
		case FIRE:
		case PHYSICAL:
			return true;
		case WIND:
		case ICE:
		case LIGHTNING:
		default:
			return false;
		}
	}
	
	// UUIDs for modifiers for base attributes from armor
	private static final UUID[] ARMOR_MODIFIERS = new UUID[] {UUID.fromString("922AB274-1111-56FE-19AE-365AA9758B8B"), UUID.fromString("D1459204-0E61-4716-A129-61666D432E0D"), UUID.fromString("1F7D236D-1118-6524-3375-34814505B28E"), UUID.fromString("2A632266-F4E1-2E67-7836-64FD783B7A50")};
	
	// UUIDs for set-based modifiers.
	// Each corresponds to a slot that's adding its bonus
	private static final UUID[] SET_MODIFIERS = new UUID[] {UUID.fromString("29F29D77-7DC5-4B68-970F-853633662A72"), UUID.fromString("A84E2267-7D48-4943-9C1C-25A022E85930"), UUID.fromString("D48816AD-B00D-4098-B686-2FC24436CD56"), UUID.fromString("104C2D3C-3987-4D56-9727-FFBEE388F6AF")};
	
	
	private static int calcArmor(EntityEquipmentSlot slot, EMagicElement element, int level) {
		
		// Ratio compared to BASE
		// BASE is 14, 18, 22 for the whole set (with rounding errors)
		float mod;
		
		switch (element) {
						// 14, 18, 22  BASE
		case EARTH:
			mod = (22f/24f); // 11, 16.7, 18
			break;
		case ENDER:
			mod = (20f/24f); // 12, 15, 18
			break;
		case FIRE:
			mod = (20f/24f); // 12, 15, 18
			break;
		case PHYSICAL:
			mod = 1f; // 15, 18.75, 22.5
			break;
		default:
			mod = 0.5f;
		}
		
		int base;
		
		switch (slot) {
		case CHEST:
			base = 8;
			break;
		case FEET:
			base = 2;
			break;
		case HEAD:
			base = 2;
			break;
		case LEGS:
			base = 6;
			break;
		default:
			base = 0;
		}
		
		if (base != 0)
			base += level - 1;
		
		return Math.max(1, (int) ((float) base * mod));
	}
	
	// Calcs magic resist, but as if it were armor which is base 20/25
	private static int calcMagicResistBase(EntityEquipmentSlot slot, EMagicElement element, int level) {
		
		float mod;
		
		switch (element) {
		case EARTH:
			mod = (18f/24f);
			break;
		case ENDER:
			mod = (22f/24f);
			break;
		case FIRE:
			mod = 1f;
			break;
		case PHYSICAL:
			mod = (10/24f);
			break;
		default:
			mod = 0.5f;
		}
		
		int base;
		
		switch (slot) {
		case CHEST:
			base = 8;
			break;
		case FEET:
			base = 2;
			break;
		case HEAD:
			base = 2;
			break;
		case LEGS:
			base = 6;
			break;
		default:
			base = 0;
		}
		
		if (base != 0)
			base += level - 1;
		
		return Math.max(1, (int) ((float) base * mod));
	}
	
	private static final double calcMagicReduct(EntityEquipmentSlot slot, EMagicElement element, int level) {
		// each piece will give (.1, .15, .25) of their type depending on level.
		return (level == 0 ? .1 : (level == 1 ? .15 : .25));
	}
	
	private static final double calcMagicSetReductTotal(EMagicElement armorElement, int setCount, EMagicElement targetElement) {
		if (setCount < 1 || setCount > 4) {
			return 0;
		}
		
		// Fire will resist 2 fire damage (total 3 with max level set). [0, .5, 1, 2]
		// Earth will resist 1 earth damage (total 2 with max level set) AND .5 in all other elements. [0, .25, .5, 1][0, 0, 0, .5]
		// Ender will resist 5 ender damage (total 6 with max level set) but -.5 in all others. [0, 1, 3, 5][0, -.1, -.3, -.5]
		final double[] setTotalFire = {0, .5, 1, 2};
		final double[] setTotalEarth = {0, .125, .25, .5};
		final double[] setTotalEarthBonus = {0, 0, 0, .5};
		final double[] setTotalEnder = {0, 1, 3, 5};
		final double[] setTotalEnderBonus = {0, -.1, -.3, -.5};
		
		final double reduc;
		
		if (armorElement == EMagicElement.FIRE) {
			// Only affect fire
			if (targetElement == EMagicElement.FIRE) {
				reduc = setTotalFire[setCount - 1]; // (1/4 per piece split evenly)
			} else {
				reduc = 0;
			}
		} else if (armorElement == EMagicElement.EARTH) {
			if (targetElement == EMagicElement.EARTH) {
				reduc = setTotalEarth[setCount - 1];
			} else {
				reduc = setTotalEarthBonus[setCount - 1];
			}
		} else if (armorElement == EMagicElement.ENDER) {
			if (targetElement == EMagicElement.ENDER) {
				reduc = setTotalEnder[setCount - 1];
			} else {
				reduc = setTotalEnderBonus[setCount - 1];
			}
		} else {
			reduc = 0;
		}
		
		return reduc;
	}
	
	private static final double calcMagicSetReduct(EntityEquipmentSlot slot, EMagicElement armorElement, int setCount, EMagicElement targetElement) {
		return calcMagicSetReductTotal(armorElement, setCount, targetElement) / setCount; // split evenly amonst all [setCount] pieces.
		// COULD make different pieces make up bigger chunks of the pie but ehh
	}
	
	private static int calcArmorDurability(EntityEquipmentSlot slot, EMagicElement element, int level) {
		float mod = 1f;
		switch (element) {
		case EARTH:
			mod = 1.1f;
			break;
		case PHYSICAL:
			mod = 1.2f;
			break;
		case FIRE:
			mod = 0.9f;
			break;
		case ENDER:
			mod = 0.8f;
			break;
		default:
			break;
		}
		
		int iron = ArmorMaterial.DIAMOND.getDurability(slot);
		double amt = iron * Math.pow(1.5, level-1);
		
		return (int) Math.floor(amt * mod);
	}
	
	private int level;
	private int armor; // Can't use vanilla; it's final
	private double magicResistAmount;
	private double magicReducAmount;
	private EMagicElement element;
	
	private String modelID;
	
	@SideOnly(Side.CLIENT)
	private static ModelEnchantedArmorBase armorModels[];
	
	public EnchantedArmor(String modelID, EntityEquipmentSlot type, EMagicElement element, int level) {
		super(ArmorMaterial.IRON, 0, type);
		
		this.level = level;
		this.element = element;
		this.modelID = modelID;
		
		this.setCreativeTab(NostrumMagica.creativeTab);
		
		this.armor = calcArmor(type, element, level);
		this.magicResistAmount = ((double) calcMagicResistBase(type, element, level) * 2.0D); // (/50 so max is 48%, then * 100 for %, so *2)
		this.magicReducAmount = calcMagicReduct(type, element, level);
		
		this.setMaxDamage(calcArmorDurability(type, element, level));
		
		if (!NostrumMagica.proxy.isServer()) {
			if (armorModels == null) {
				armorModels = new ModelEnchantedArmorBase[4];
				for (int i = 0; i < 4; i++) {
					armorModels[i] = new ModelEnchantedArmorBase(1f, i);
				}
			}
		}
	}
	
	@Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();	

        if (equipmentSlot == this.armorType)
        {
            multimap.put(SharedMonsterAttributes.ARMOR.getAttributeUnlocalizedName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor modifier", (double)this.armor, 0));
            multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getAttributeUnlocalizedName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor toughness", 1, 0));
            multimap.put(AttributeMagicResist.instance().getAttributeUnlocalizedName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Magic Resist", (double)this.magicResistAmount, 0));
            multimap.put(AttributeMagicReduction.instance(this.element).getAttributeUnlocalizedName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Magic Reduction", (double)this.magicReducAmount, 0));
        }

        return multimap;
    }
	
	@Override
	public int getItemEnchantability() {
		return 16;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }

	@Override
	public SpellAction getTriggerAction(EntityLivingBase user, boolean offense, ItemStack stack) {
		if (offense)
			return null;
		
		SpellAction action = null;
		switch (element) {
		case EARTH:
			if (NostrumMagica.rand.nextFloat() <= 0.15f * (float) level)
				action = new SpellAction(user).status(RootedPotion.instance(), 20 * 2, 0);
			break;
		case ENDER:
			if (NostrumMagica.rand.nextFloat() <= 0.15f * (float) level)
				action = new SpellAction(user).phase(level);
			break;
		case FIRE:
			if (NostrumMagica.rand.nextFloat() <= 0.35f * (float) level)
				action = new SpellAction(user).burn(5 * 20);
			break;
		case PHYSICAL:
		case WIND:
		case LIGHTNING:
		case ICE:
		default:
			break;
		
		}
		
		return action;
	}

	@Override
	public boolean shouldTrigger(boolean offense, ItemStack stack) {
		return !offense;
	}
	
	public static EnchantedArmor get(EMagicElement element, EntityEquipmentSlot slot, int level) {
		if (items.containsKey(element) && items.get(element).containsKey(slot))
			return items.get(element).get(slot).get(level);
		
		return null;
	}
	
	public int getLevel() {
		return level;
	}
	
	public EMagicElement getElement() {
		return element;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		if (!(stack.getItem() instanceof EnchantedArmor)) {
			return null;
		}
		
		// Could support overlay. For now, have unique brightness-adjusted textures for each.
		if (type != null && type.equalsIgnoreCase("overlay")) {
			//return NostrumMagica.MODID + ":textures/models/armor/none.png";
			return NostrumMagica.MODID + ":textures/models/armor/magic_armor_" + element.name().toLowerCase() + "_overlay.png";
		}
		
		return NostrumMagica.MODID + ":textures/models/armor/magic_armor_" + element.name().toLowerCase() + ".png"; 
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
		
		int setCount = getSetPieces(entity, stack);
		ModelEnchantedArmorBase model = armorModels[Math.max(0, Math.min(3, setCount - 1))];
		model.setVisibleFrom(slot);
		
		return model;
	}
	
	public static int GetSetCount(EntityLivingBase entity, EMagicElement element, int level) {
		int count = 0;
		
		for (EntityEquipmentSlot slot : new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET}) {
			ItemStack inSlot = entity.getItemStackFromSlot(slot);
			if (inSlot == null || !(inSlot.getItem() instanceof EnchantedArmor)) {
				continue;
			}
			
			EnchantedArmor item = (EnchantedArmor) inSlot.getItem();
			if (item.getElement() == element && item.getLevel() == level) {
				count++;
			}
		}
		
		return count;
	}
	
	public int getSetPieces(EntityLivingBase entity, ItemStack stack) {
		final EMagicElement myElem = ((EnchantedArmor)stack.getItem()).getElement();
		final int myLevel = ((EnchantedArmor)stack.getItem()).getLevel();
		return GetSetCount(entity, myElem, myLevel);
	}
	
	@Override
	public boolean hasOverlay(ItemStack item) {
		return true;
	}
	
	@Override
	public int getColor(ItemStack stack) {
		// This is brightness. Different elements already tint their textures. We just make brighter with level.
		switch (level) {
		default:
		case 0:
			return 0xFF3F3F3F;
		case 1:
			return 0xFF7F7F7F;
		case 2:
			return 0xFFFFFFFF;
		}
	}
	
	public static List<EnchantedArmor> getAll() {
		List<EnchantedArmor> list = new LinkedList<>();
		
		for (EMagicElement element : EMagicElement.values())
		if (isArmorElement(element)) {
			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
			if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
				list.addAll(items.get(element).get(slot).values());
			}
		}
		
		return list;
	}
	
	public String getModelID() {
		return modelID;
	}

	@Override
	public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage,
			int slot) {
		if (source.isDamageAbsolute() || source.isUnblockable()) {
			return new ArmorProperties(1, 0.0, 0);
		}
		return new ArmorProperties(1, (double) this.armor / 25.0, Integer.MAX_VALUE);
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
		return this.armor;
	}

	@Override
	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
		stack.damageItem(damage, entity);
	}
	
	protected void onArmorDisplayTick(World world, EntityPlayer player, ItemStack itemStack, int setCount) {
		final int displayLevel = (level + 1) * (setCount * setCount);
		
		if (NostrumMagica.rand.nextInt(400) > displayLevel) {
			return;
		}
		
		final double dx;
		final double dy;
		final double dz;
		final EnumParticleTypes effect;
		final int mult;
		final float rangeMod;
		switch (element) {
		case EARTH:
			effect = EnumParticleTypes.SUSPENDED_DEPTH;
			dx = dy = dz = 0;
			mult = 1;
			rangeMod = 1;
			break;
		case FIRE:
			effect = EnumParticleTypes.FLAME;
			dx = dz = 0;
			dy = .025;
			mult = 1;
			rangeMod = 2;
			break;
		case ENDER:
			effect = EnumParticleTypes.PORTAL;
			dx = dy = dz = 0;
			mult = 2;
			rangeMod = 1.5f;
			break;
		case PHYSICAL:
		case WIND:
		case ICE:
		case LIGHTNING:
		default:
			effect = null;
			dx = dy = dz = 0;
			mult = 0;
			rangeMod = 0;
			break;
		}
		
		if (effect == null) {
			return;
		}
		
		for (int i = 0; i < mult; i++) {
			final float rd = NostrumMagica.rand.nextFloat();
			final float radius = .5f + (NostrumMagica.rand.nextFloat() * (.5f * rangeMod));
			final double px = (player.posX + radius * Math.cos(rd * Math.PI * 2));
			final double py = (player.posY + (NostrumMagica.rand.nextFloat() * 2));
			final double pz = (player.posZ + radius * Math.sin(rd * Math.PI * 2));
			world.spawnParticle(effect, px, py, pz, dx, dy, dz, new int[0]);
		}
	}
	
	protected void onServerTick(World world, EntityPlayer player, ItemStack stack, int setCount) {
		
	}
	
	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
		super.onArmorTick(world, player, itemStack);
		
		final int setCount = getSetPieces(player, itemStack);
		if (!world.isRemote) {
			onServerTick(world, player, itemStack, setCount);
		} else {
			onArmorDisplayTick(world, player, itemStack, setCount);
		}
	}
	
	// hacky little map to avoid thrashing attributes every tick
	protected static final Map<EntityLivingBase, Map<EntityEquipmentSlot, ItemStack>> LastEquipState = new HashMap<>();
	
	protected static Map<EntityEquipmentSlot, ItemStack> GetLastTickState(EntityLivingBase entity) {
		Map<EntityEquipmentSlot, ItemStack> map = LastEquipState.get(entity);
		if (map == null) {
			map = new EnumMap<>(EntityEquipmentSlot.class);
			LastEquipState.put(entity, map);
		}
		return map;
	}
	
	protected static boolean EntityChangedEquipment(EntityLivingBase entity) {
		Map<EntityEquipmentSlot, ItemStack> map = GetLastTickState(entity);
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			if (slot.getSlotType() != Type.ARMOR) {
				continue;
			}
			
			@Nullable final ItemStack inSlot = entity.getItemStackFromSlot(slot);
			@Nullable final ItemStack lastTick = map.get(slot);
			
			// Same check Vanilla uses to apply attributes
			if (!ItemStack.areItemStacksEqual(inSlot, lastTick)) {
				return true;
			}
		}
		return false;
	}
	
	protected static void UpdateEntity(EntityLivingBase entity) {
		if (EntityChangedEquipment(entity)) {
			// Figure out attributes and set.
			// Also capture current armor status and cache it.
			Map<EntityEquipmentSlot, ItemStack> cacheMap = new EnumMap<>(EntityEquipmentSlot.class);
			Multimap<String, AttributeModifier> attribMap = HashMultimap.<String, AttributeModifier>create();	

			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
				if (slot.getSlotType() != Type.ARMOR) {
					continue;
				}
				
				ItemStack inSlot = entity.getItemStackFromSlot(slot);
				final int setCount;
				final @Nullable EMagicElement armorElem;
				if (inSlot != null) {
					inSlot = inSlot.copy();
					
					if (inSlot.getItem() instanceof EnchantedArmor) {
						EnchantedArmor armorType = (EnchantedArmor) inSlot.getItem();
						setCount = armorType.getSetPieces(entity, inSlot);
						armorElem = armorType.getElement();
					} else {
						setCount = 0;
						armorElem = null;
					}
				} else {
					setCount = 0;
					armorElem = null;
				}
				
				// Figure out how much this SHOULD be giving
				for (EMagicElement elem : EMagicElement.values()) {
					final double reduct = (setCount > 0 && armorElem != null)
							? calcMagicSetReduct(slot, armorElem, setCount, elem)
							: 0;
						
					// Important to do this even with 0 to remove previous bonuses
					attribMap.put(AttributeMagicReduction.instance(elem).getAttributeUnlocalizedName(), new AttributeModifier(SET_MODIFIERS[slot.getIndex()], "Magic Reduction (Set)", reduct, 0));
				}
				
				// Add captured value to map
				cacheMap.put(slot, inSlot);
			}
			
			// Update attributes
			entity.getAttributeMap().applyAttributeModifiers(attribMap);
			
			// Create and save new map
			LastEquipState.put(entity, cacheMap);
		}
	}
	
	// Updates all entities' current set bonuses (or lack there-of) from enchanted armor
	public static void ServerWorldTick(World world) {
		for (Entity ent : world.loadedEntityList) {
			if (ent instanceof EntityLivingBase) {
				UpdateEntity((EntityLivingBase) ent);
			}
		}
	}
	
	public static Map<IAttribute, Double> FindCurrentSetBonus(@Nullable Map<IAttribute, Double> map, EntityLivingBase entity, EMagicElement element, int level) {
		if (map == null) {
			map = new HashMap<>();
		}
		
		final int setCount = GetSetCount(entity, element, level);
		
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			if (slot.getSlotType() != Type.ARMOR) {
				continue;
			}
			
			@Nullable ItemStack inSlot = entity.getItemStackFromSlot(slot);
			if (inSlot == null || !(inSlot.getItem() instanceof EnchantedArmor)) {
				continue;
			}
			
			EnchantedArmor armorType = (EnchantedArmor) inSlot.getItem();
			final int inSlotLevel = armorType.getLevel();
			final EMagicElement inSlotElement = armorType.getElement();
			if (inSlotLevel != level || inSlotElement != element) {
				continue;
			}
			
			for (EMagicElement elem : EMagicElement.values()) {
				final double reduct = calcMagicSetReduct(slot, element, setCount, elem);
				if (reduct == 0) {
					continue;
				}
				
				final AttributeMagicReduction inst = AttributeMagicReduction.instance(elem);
				Double cur = map.get(inst);
				if (cur == null) {
					cur = 0.0;
				}
				
				cur = cur + reduct;
					
				map.put(inst, cur);
			}
		}
		
		return map;
	}
	
	private Map<IAttribute, Double> setMapInst = new HashMap<>();
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		final boolean showFull = Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode());
		final int setCount = this.getSetPieces(playerIn, stack);
		
		final String setName = I18n.format("item.armor.set." + element.name().toLowerCase() + "." + level + ".name", new Object[0]);
		if (showFull) {
			tooltip.add(I18n.format("info.armor.set_total", setName, ChatFormatting.DARK_PURPLE, ChatFormatting.RESET));
		} else {
			
			final String countFormat = "" + (setCount == 4 ? ChatFormatting.GOLD : ChatFormatting.YELLOW);
			tooltip.add(I18n.format("info.armor.set_status", setName, setCount, countFormat, "" + ChatFormatting.RESET));
		}
		
		synchronized(setMapInst) {
			setMapInst.clear();
			
			if (showFull) {
				// Show total
				for (EMagicElement targElem : EMagicElement.values()) {
					final double reduc = calcMagicSetReductTotal(element, 4, targElem);
					setMapInst.put(AttributeMagicReduction.instance(targElem), reduc);
				}
			} else {
				// Show current
				FindCurrentSetBonus(setMapInst, playerIn, element, level); // puts into setMapInst
			}
			
			if (!setMapInst.isEmpty()) {
				for (Entry<IAttribute, Double> entry : setMapInst.entrySet()) {
					Double val = entry.getValue();
					if (val == null || val == 0) {
						continue;
					}
					
					// Formatting here copied from Vanilla
					if (val > 0) {
						tooltip.add(TextFormatting.BLUE + " " + I18n.format("attribute.modifier.plus.0", String.format("%.2f", val), I18n.format("attribute.name." + (String)entry.getKey().getAttributeUnlocalizedName())));
					} else {
						val = -val;
						tooltip.add(TextFormatting.RED + " " + I18n.format("attribute.modifier.take.0", String.format("%.2f", val), I18n.format("attribute.name." + (String)entry.getKey().getAttributeUnlocalizedName())));
					}
				}
			}
		}
	}
	
}
