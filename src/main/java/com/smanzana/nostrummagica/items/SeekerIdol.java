package com.smanzana.nostrummagica.items;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
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
import net.minecraft.item.crafting.ShapedRecipes;
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
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

public class SeekerIdol extends Item implements ILoreTagged {

	private static final String NBT_TYPE = "type";
	private static final String NBT_KEY = "key";
	private static SeekerIdol instance = null;
	
	// SpellComponentWrapper's equals and hash overriden so it can be used as a key
	private static Map<String, Map<SpellComponentWrapper, List<BlockPos>>> knownDungeons = new HashMap<>();
	
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
		
		for (Entry<String, Map<SpellComponentWrapper, List<BlockPos>>> worldMap : knownDungeons.entrySet()) {
			NBTTagCompound nbt = new NBTTagCompound();
			for (Entry<SpellComponentWrapper, List<BlockPos>> row : worldMap.getValue().entrySet()) {
				NBTTagList list = new NBTTagList();
				
				for (BlockPos pos : row.getValue()) {
					list.appendTag(new NBTTagString(pos.getX() + " " + pos.getY() + " " + pos.getZ()));
				}
				
				nbt.setTag(row.getKey().getKeyString(), list);
			}
			base.setTag(worldMap.getKey(), nbt);
		}
		
		return base;
	}
	
	public static void readRegistryFromNBT(NBTTagCompound nbt) {
		knownDungeons.clear();
		for (String worldid : nbt.getKeySet()) {
			Map<SpellComponentWrapper, List<BlockPos>> worldMap = new HashMap<>();
			for (String key : nbt.getCompoundTag(worldid).getKeySet()) {
				SpellComponentWrapper comp = SpellComponentWrapper.fromKeyString(key);
				List<BlockPos> list = new LinkedList<>();
				NBTTagList tags = nbt.getCompoundTag(worldid).getTagList(key, NBT.TAG_STRING);
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
				
				worldMap.put(comp, list);
			}
			knownDungeons.put(worldid, worldMap);
		}
	}
	
	private static final String GetWorldID(World world) {
		String id = world.getWorldInfo().getWorldName()
				+ "_" + world.getSeed();
		return id;
	}
	
	public static void addDungeon(World world, SpellComponentWrapper component, BlockPos center) {
		String id = GetWorldID(world);
		
		if (!knownDungeons.containsKey(id)) {
			knownDungeons.put(id, new HashMap<SpellComponentWrapper, List<BlockPos>>());
		}
		
		if (!knownDungeons.get(id).containsKey(component)){
			knownDungeons.get(id).put(component, new LinkedList<BlockPos>());
		}
		
		knownDungeons.get(id).get(component).add(center);
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
		
		if (nbt == null || !nbt.hasKey(NBT_TYPE, NBT.TAG_STRING) || !nbt.hasKey(NBT_KEY, NBT.TAG_STRING))
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
			playerIn.addVelocity(dir.xCoord * 1, 0, dir.zCoord * 1);
			playerIn.velocityChanged = true;
			itemStackIn.damageItem(1, playerIn);
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		
    }
	
	private Vec3d findShrineDir(World world, Vec3d pos, SpellComponentWrapper component) {
		BlockPos targ = null;
		double min = Double.MAX_VALUE;
		BlockPos from = new BlockPos(pos);
		String worldID = GetWorldID(world);
		
		if (!knownDungeons.containsKey(worldID)) {
			return null;
		}
		if (!knownDungeons.get(worldID).containsKey(component))
			return null;
		
		for (BlockPos bp : knownDungeons.get(worldID).get(component)) {
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
		
		NostrumMagica.logger.info("SeekerIdol targetting (" + targ.getX() + ", " + targ.getZ());
		
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
			key = component.getElement().name();
		} else if (component.isAlteration()) {
			type = "alteration";
			key = component.getAlteration().name();
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
	
	private static class IdolRecipe extends ShapedRecipes {

		//public ShapedRecipes(int width, int height, ItemStack[] p_i1917_3_, ItemStack output)
		public IdolRecipe() {
			super(3, 3, new ItemStack[] {
				new ItemStack(Blocks.COBBLESTONE),
				NostrumResourceItem.getItem(ResourceType.TOKEN, 1),
				new ItemStack(Blocks.COBBLESTONE),
				new ItemStack(Blocks.COBBLESTONE),
				new ItemStack(SpellRune.instance(), 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack(Blocks.COBBLESTONE),
				new ItemStack(Blocks.COBBLESTONE),
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack(Blocks.COBBLESTONE),	
			}, SeekerIdol.getItemStack(new SpellComponentWrapper(EMagicElement.PHYSICAL)));
			
			RecipeSorter.register(NostrumMagica.MODID + ":IdolRecipe",
					this.getClass(), Category.SHAPED, "after:minecraft:shaped");
		}
		
		@Override
		public ItemStack getCraftingResult(InventoryCrafting inv) {
			// Just care about the rune in the center
			ItemStack rune = inv.getStackInSlot(4);
			SpellComponentWrapper comp = SpellRune.toComponentWrapper(rune);
			if (comp == null) {
				return null;
			}
			
			return SeekerIdol.getItemStack(comp);
		}
	}
}
