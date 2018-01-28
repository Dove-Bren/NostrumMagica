package com.smanzana.nostrummagica.items;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SeekerIdol extends Item implements ILoreTagged {

	private static final String NBT_TYPE = "type";
	private static final String NBT_KEY = "key";
	private static SeekerIdol instance = null;
	
	// SpellComponentWrapper's equals and hash overriden so it can be used as a key
	private static Map<SpellComponentWrapper, List<BlockPos>> knownDungeons = new HashMap<>();
	
	public static SeekerIdol instance() {
		if (instance == null)
			instance = new SeekerIdol();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.addRecipe(new IdolRecipe());
	}
	
	public static NBTTagCompound saveRegistryToNBT() {
		NBTTagCompound base = new NBTTagCompound();
		
		for (Entry<SpellComponentWrapper, List<BlockPos>> row : knownDungeons.entrySet()) {
			NBTTagList list = new NBTTagList();
			
			for (BlockPos pos : row.getValue()) {
				list.appendTag(new NBTTagString(pos.getX() + " " + pos.getY() + " " + pos.getZ()));
			}
			
			base.setTag(row.getKey().getKeyString(), list);
		}
		
		return base;
	}
	
	public static void readRegistryFromNBT(NBTTagCompound nbt) {
		knownDungeons.clear();
		for (String key : nbt.getKeySet()) {
			SpellComponentWrapper comp = SpellComponentWrapper.fromKeyString(key);
			List<BlockPos> list = new LinkedList<>();
			NBTTagList tags = nbt.getTagList(key, NBT.TAG_STRING);
			for (int i = 0; i < tags.tagCount(); i++) {
				String serial = tags.getStringTagAt(i);
				int x = 0, y = 0, z = 0;
				try {
					int pos = serial.indexOf(' ');
					x = Integer.parseInt(serial.substring(0, pos));
					serial = serial.substring(pos + 1);
					pos = serial.indexOf(' ');
					y = Integer.parseInt(serial.substring(0, pos));
					z = Integer.parseInt(serial.substring(pos + 1));
				} catch (Exception e) {
					NostrumMagica.logger.warn("Could not reparse dungeon location");
					continue;
				}
				
				BlockPos pos = new BlockPos(x, y, z);
				list.add(pos);
			}
			
			knownDungeons.put(comp, list);
		}
	}
	
	public static void addDungeon(SpellComponentWrapper component, BlockPos center) {
		if (!knownDungeons.containsKey(component)) {
			knownDungeons.put(component, new LinkedList<BlockPos>());
		}
		
		knownDungeons.get(component).add(center);
	}
	
	public static final String id = "seeker_idol";
	
	private SeekerIdol() {
		super();
		this.setUnlocalizedName(id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(1);
		this.setMaxDamage(25);
	}
	
	private SpellComponentWrapper getNestedComponent(ItemStack itemStackIn) {
		if (itemStackIn == null || !(itemStackIn.getItem() instanceof SeekerIdol))
			return null;
		
		NBTTagCompound nbt = itemStackIn.getTagCompound();
		
		if (nbt == null || !nbt.hasKey(NBT_TYPE, NBT.TAG_STRING) || nbt.hasKey(NBT_KEY, NBT.TAG_STRING))
			return new SpellComponentWrapper(EMagicElement.PHYSICAL);
		
		String type = nbt.getString(NBT_TYPE).toLowerCase();
		String key = nbt.getString(NBT_KEY);
		SpellComponentWrapper component;
		
		switch (type) {
		case "element":
			try {
				EMagicElement elem = EMagicElement.valueOf(key.toUpperCase());
				component = new SpellComponentWrapper(elem);
			} catch (Exception e) {
				component = new SpellComponentWrapper(EMagicElement.PHYSICAL);
			}
			break;
		case "alteration":
			try {
				EAlteration altr = EAlteration.valueOf(key.toUpperCase());
				component = new SpellComponentWrapper(altr);
			} catch (Exception e) {
				component = new SpellComponentWrapper(EMagicElement.PHYSICAL);
			}
			break;
		case "shape":
			SpellShape shape = SpellShape.get(key);
			if (shape != null)
				component = new SpellComponentWrapper(shape);
			else
				component = new SpellComponentWrapper(EMagicElement.PHYSICAL);
			break;
		case "trigger":
			SpellTrigger trigger = SpellTrigger.get(key);
			if (trigger != null)
				component = new SpellComponentWrapper(trigger);
			else
				component = new SpellComponentWrapper(EMagicElement.PHYSICAL);
			break;
		default:
			component = new SpellComponentWrapper(EMagicElement.PHYSICAL);
			break;
		}
		
		return component;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (worldIn.isRemote)
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		
		SpellComponentWrapper component = getNestedComponent(itemStackIn);
		if (component == null) {
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		}
		
		Vec3d dir = findShrineDir(worldIn, playerIn.getPositionVector(), component);
		if (dir == null) {
			playerIn.addChatComponentMessage(new TextComponentString("Could not find a shrine of that type! Explore the world more."));
		} else {
			playerIn.addVelocity(dir.xCoord * 2, 0, dir.zCoord * 2);
			itemStackIn.damageItem(1, playerIn);
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		
    }
	
	private Vec3d findShrineDir(World world, Vec3d pos, SpellComponentWrapper component) {
		BlockPos targ = null;
		double min = Double.MAX_VALUE;
		BlockPos from = new BlockPos(pos);
		
		if (!knownDungeons.containsKey(component)) {
			return null;
		}
		
		for (BlockPos bp : knownDungeons.get(component)) {
			if (targ == null) {
				targ = bp;
				min = bp.distanceSq(from);
				continue;
			}
			
			double dist = bp.distanceSq(from);
			if (dist < min) {
				min = dist;
				targ = bp;
			}
		}
		
		if (targ == null)
			return null;
		
		// We make y the same here so there's no vertical pull
		Vec3d to = new Vec3d(targ.getX(), pos.yCoord, targ.getZ());
		
		return to.subtract(pos).normalize();
	}
	
	public static void setComponent(ItemStack itemStack, SpellComponentWrapper component) {
		if (itemStack == null || !(itemStack.getItem() instanceof SeekerIdol))
			return;
		
		NBTTagCompound nbt = itemStack.getTagCompound();
		
		if (nbt == null)
			nbt = new NBTTagCompound();
		String type = "";
		String key = "";
		if (component.isElement()) {
			type = "element";
			key = component.getElement().getName();
		} else if (component.isShape()) {
			type = "shape";
			key = component.getShape().getShapeKey();
		} else if (component.isTrigger()) {
			type = "trigger";
			key = component.getTrigger().getTriggerKey();
		}
		
		nbt.setString(NBT_TYPE, type);
		nbt.setString(NBT_KEY, key);
		
		itemStack.setTagCompound(nbt);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		SpellComponentWrapper component = getNestedComponent(stack);
		if (component == null)
			return;
		
		if (component.isElement()) {
			tooltip.add(TextFormatting.DARK_BLUE + component.getElement().getName() + TextFormatting.RESET);
		} else if (component.isAlteration()) {
			tooltip.add(TextFormatting.DARK_BLUE + component.getAlteration().getName() + TextFormatting.RESET);
		} else if (component.isTrigger()) {
			tooltip.add(TextFormatting.DARK_BLUE + component.getTrigger().getDisplayName() + TextFormatting.RESET);
		} else if (component.isShape()) {
			tooltip.add(TextFormatting.DARK_BLUE + component.getShape().getDisplayName() + TextFormatting.RESET);
		}
		
	}

	@Override
	public String getLoreKey() {
		return "nostrum_seeker_idol";
	}

	@Override
	public String getLoreDisplayName() {
		return "Seeker Idols";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Seeker idols are used to locate shrines.", "Somehow, they can be attuned to a specific component type.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Seeker idols are created to find shrines.", "They can be attuned to a specific element, shape, or trigger by combining a rune on a crafting table.", "Right-clicking with an idol will pull the user in the direction of the nearest shrine.");
	}
	
	public static ItemStack getItemStack(SpellComponentWrapper comp) {
		return getItemStack(comp, 1);
	}
	
	public static ItemStack getItemStack(SpellComponentWrapper comp, int count) {
		ItemStack stack = new ItemStack(instance, count);
		
		setComponent(stack, comp);
		
		return stack;
	}
	
	private static class IdolRecipe implements IRecipe {

		@Override
		public boolean matches(InventoryCrafting inv, World worldIn) {
			if (inv.getSizeInventory() < 9)
				return false;
			
			for (int i = 0; i < 9; i++) {
				if (inv.getStackInSlot(i) == null)
					return false;
			}
			
			Item stone = Item.getItemFromBlock(Blocks.STONE);
			if (!(inv.getStackInSlot(0).getItem() == stone &&
					inv.getStackInSlot(2).getItem() == stone &&
					inv.getStackInSlot(3).getItem() == stone &&
					inv.getStackInSlot(5).getItem() == stone &&
					inv.getStackInSlot(6).getItem() == stone &&
					inv.getStackInSlot(8).getItem() == stone
					))
				return false;
			
			if ((inv.getStackInSlot(1).getItem() instanceof ReagentItem))
				return false;
			
			if (inv.getStackInSlot(7).getItem() != Items.GOLD_INGOT)
				return false;
			
			return (inv.getStackInSlot(5).getItem() instanceof SpellRune);
		}

		@Override
		public ItemStack getCraftingResult(InventoryCrafting inv) {
			// Just care about the rune in the center
			ItemStack rune = inv.getStackInSlot(5);
			SpellComponentWrapper comp = SpellRune.toComponentWrapper(rune);
			if (comp == null)
				return null;
			
			return SeekerIdol.getItemStack(comp);
		}

		@Override
		public int getRecipeSize() {
			return 4;
		}

		@Override
		public ItemStack getRecipeOutput() {
			return SpellRune.getRune(EMagicElement.FIRE, 1);
		}

		@Override
		public ItemStack[] getRemainingItems(InventoryCrafting inv) {
			return new ItemStack[inv.getSizeInventory()];
		}
		
	}
}
