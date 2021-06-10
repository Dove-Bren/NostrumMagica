package com.smanzana.nostrummagica.integration.baubles.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuser.AetherInfuserTileEntity;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.IAetherInfuserLens;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class ItemAetherLens extends Item implements ILoreTagged, IAetherInfuserLens {

	public static enum LensType {
		SPREAD("spread", true, 1, 0, new ItemStack(Items.DIAMOND)),
		CHARGE("wide_charge", true, 1, 0, NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)),
		GROW("grow", false, 20, 10, new ItemStack(Blocks.BONE_BLOCK)),
		SWIFTNESS("swiftness", false, 1, 0, PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.SWIFTNESS)), // aether taken per entity
		ELEVATOR("elevator", false, 1, 0, new ItemStack(Blocks.DISPENSER)), // aether taken per entity
		HEAL("heal", false, 5, 0, PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.HEALING)), // aether taken per entity
		BORE("bore", false, 20, 50, new ItemStack(Items.DIAMOND_PICKAXE, 1, OreDictionary.WILDCARD_VALUE)),
		BORE_REVERSED("bore_reversed", false, 20, 50, null),
		;
		
		private final String unlocName; // unlocalized name fragment
		private final boolean isMaster; // whether this lense requires an original aether infuser
		private final int aetherPerTick; // Aether we require to work each work tick
		private final int interval;
		private final ItemStack ingredient;
		
		private LensType(String unlocName, boolean isMaster, int tickInterval, int aetherPerTick, @Nullable ItemStack ingredient) {
			this.unlocName = unlocName;
			this.isMaster = isMaster;
			this.aetherPerTick = aetherPerTick;
			this.interval = tickInterval;
			this.ingredient = ingredient;
		}
		
		public String getUnlocSuffix() {
			return unlocName;
		}
		
		public boolean isMasterOnly() {
			return isMaster;
		}
		
		public int getTickInterval() {
			return this.interval;
		}
		
		public int getAetherPerTick() {
			return this.aetherPerTick;
		}
		
		public ItemStack getIngredient() {
			return this.ingredient;
		}
	}
	
	public static String ID = "aether_lens_item";
	public static String UNLOC_PREFIX = "lens_";
	
	public static void init() {
		instance().setUnlocalizedName(ID);
		
		// Reverse bore
		ItemStack reversedStack = Create(LensType.BORE_REVERSED, 1);
		ItemStack regularStack = Create(LensType.BORE, 1);
		GameRegistry.addRecipe(new ShapelessRecipes(reversedStack, Lists.newArrayList(
				regularStack
				)));
		GameRegistry.addRecipe(new ShapelessRecipes(regularStack, Lists.newArrayList(
				reversedStack
				)));
	}
	
	private static ItemAetherLens instance = null;

	public static ItemAetherLens instance() {
		if (instance == null)
			instance = new ItemAetherLens();
	
		return instance;

	}
	
	public ItemAetherLens() {
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(16);
		this.setHasSubtypes(true);
	}
	
	public static final LensType TypeFromMeta(int meta) {
		return LensType.values()[meta % LensType.values().length];
	}
	
	public static final int MetaFromType(LensType type) {
		return type.ordinal();
	}
	
	public static final ItemStack Create(LensType type, int count) {
		return new ItemStack(instance(), count, MetaFromType(type));
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item." + UNLOC_PREFIX + TypeFromMeta(stack.getMetadata()).getUnlocSuffix();
	}
	
	@SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (LensType type : LensType.values()) {
			subItems.add(Create(type, 1));
		}
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_aether_lenses";
	}

	@Override
	public String getLoreDisplayName() {
		return "Aether Lenses";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Aether lenses are used in conjunction with the Aether Infuser multiblock to provide various effects at the cost of aether!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Aether lenses are placed on altars 'affected' by Aether Infuses. Each lens does something different, and some lenses can only placed on the main altar of an infuser.", "All lenses can be placed directly in the altar above an Aether Infuser.", "Most lenses on altars that are near Aether Infuses that have a Spread lense on their main altar will also work!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		
		if (stack == null)
			return;
		
		final LensType type = TypeFromMeta(stack.getMetadata()); 
		
		if (I18n.hasKey("item." + UNLOC_PREFIX + type.getUnlocSuffix() + ".desc")) {
			// Format with placeholders for blue and red formatting
			String translation = I18n.format("item." + UNLOC_PREFIX + type.getUnlocSuffix() + ".desc", TextFormatting.GRAY, TextFormatting.BLUE, TextFormatting.DARK_RED);
			if (translation.trim().isEmpty())
				return;
			String lines[] = translation.split("\\|");
			for (String line : lines) {
				tooltip.add(line);
			}
		}
		
		if (type.isMasterOnly()) {
			tooltip.add(TextFormatting.DARK_RED + I18n.format("item.lens.master") + TextFormatting.RESET);
		}
	}
	
	@Override
	public boolean canAcceptAetherInfuse(ItemStack stack, BlockPos pos, AetherInfuserTileEntity source, int maxAether) {
		final LensType type = TypeFromMeta(stack.getMetadata());
		// Masteronly ones are handled directly in aether infuser in non-dynamic manner
		return !type.isMasterOnly()
				&& source.getWorld().getTotalWorldTime() % type.getTickInterval() == 0
				&& maxAether > 0
				&& maxAether >= type.aetherPerTick;
	}

	@Override
	public int acceptAetherInfuse(ItemStack stack, BlockPos pos, AetherInfuserTileEntity source, int maxAether) {
		final LensType type = TypeFromMeta(stack.getMetadata());
		final World world = source.getWorld();
		int cost = 0;
		
		switch (type) {
		case BORE:
			cost = doBore(world, pos, maxAether);
			break;
		case BORE_REVERSED:
			cost = doBoreReversed(world, pos, maxAether);
			break;
		case ELEVATOR:
			cost = doElevator(world, pos, maxAether);
			break;
		case GROW:
			cost = doGrow(world, pos, maxAether);
			break;
		case HEAL:
			cost = doHeal(world, pos, maxAether);
			break;
		case SWIFTNESS:
			cost = doSwiftness(world, pos, maxAether);
			break;
		case SPREAD:
		case CHARGE:
			cost = 0;
			break;
		}
		
		return maxAether - cost;
	}
	
	protected int doGrow(World world, BlockPos center, int maxAether) {
		if (EnchantedArmor.DoEarthGrow(world, center)) {
			return LensType.GROW.getAetherPerTick();
		}
		return 0;
	}
	
	protected int doSwiftness(World world, BlockPos center, int maxAether) {
		final double MAX_DIST_SQ = 900;
		int cost = 0;
		for (EntityLivingBase ent : world.getEntities(EntityLivingBase.class, (e) -> {
			return !e.isDead
					&& e.getDistanceSq(center) < MAX_DIST_SQ
					&& (e.getActivePotionEffect(Potion.getPotionFromResourceLocation("speed")) == null
						|| e.getActivePotionEffect(Potion.getPotionFromResourceLocation("speed")).getDuration() < 20);
		})) {
			ent.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("speed"), 40, 0, false, false));
			cost += 5;
		}
		return cost;
	}
	
	protected int doElevator(World world, BlockPos center, int maxAether) {
		final double HORZ_DIST_RADIUS = 2.5;
		final double MAX_HEIGHT = 30;
		int cost = 0;
		for (EntityLivingBase ent : world.getEntities(EntityLivingBase.class, (e) -> {
			return !e.isDead
					&& e.posY >= center.getY() && e.posY < center.getY() + MAX_HEIGHT
					&& Math.abs(e.posX - (center.getX() + .5)) < HORZ_DIST_RADIUS
					&& Math.abs(e.posZ - (center.getZ() + .5)) < HORZ_DIST_RADIUS;
		})) {
			if (!ent.isSneaking() && !ent.onGround) {
				cost += 1;
				ent.motionY = Math.min(.3, ent.motionY + .1);
				ent.velocityChanged = true;
			}
			ent.fallDistance = 0;
		}
		return cost;
	}
	
	protected int doHeal(World world, BlockPos center, int maxAether) {
		final double MAX_DIST_SQ = 900;
		int cost = 0;
		for (EntityLivingBase ent : world.getEntities(EntityLivingBase.class, (e) -> {
			return !e.isDead
					&& (e instanceof EntityPlayer || (e instanceof IEntityOwnable && ((IEntityOwnable) e).getOwnerId() != null))
					&& e.getDistanceSq(center) < MAX_DIST_SQ
					&& e.getHealth() < e.getMaxHealth();
		})) {
			ent.heal(.25f);
			cost += 2;
		}
		return cost;
	}
	
	protected boolean doBoreInternal(World world, BlockPos center, int maxAether, boolean down) {
		MutableBlockPos cursor = new MutableBlockPos(
				down ? center.down().down() : center.up().up().up()
				);
		while (cursor.getY() >= 0 && cursor.getY() < world.getHeight()) {
			if (world.isAirBlock(cursor)) {
				cursor.move(down ? EnumFacing.DOWN : EnumFacing.UP);
				continue;
			}
			
			final IBlockState blockstate = world.getBlockState(cursor);
			if (blockstate.getBlockHardness(world, cursor) < 0) {
				cursor.move(down ? EnumFacing.DOWN : EnumFacing.UP);
				continue;
			}
			
			break; // breakable!
		}
		
		if (cursor.getY() < 0 || cursor.getY() >= world.getHeight()) {
			return false; // nothing to do
		}
		
		List<ItemStack> drops = new ArrayList<>();
		for (int x = -2; x <= 2; x++)
		for (int z = -2; z <= 2; z++) {
			final BlockPos pos = cursor.toImmutable().add(x, 0, z);
			final IBlockState state = world.getBlockState(pos);
			drops.addAll(state.getBlock().getDrops(world, pos, state, 0));
			world.destroyBlock(pos, false);
			
		}
		
		for (ItemStack stack : drops) {
			// put drops right above bore altar
			world.spawnEntityInWorld(new EntityItem(world, center.getX() + .5, center.getY() + 1.2, center.getZ() + .5, stack));
		}
		
		return true;
	}
	
	protected int doBore(World world, BlockPos center, int maxAether) {
		return doBoreInternal(world, center, maxAether, true) ? LensType.BORE.getAetherPerTick() : 0;
	}
	
	protected int doBoreReversed(World world, BlockPos center, int maxAether) {
		return doBoreInternal(world, center, maxAether, false) ? LensType.BORE_REVERSED.getAetherPerTick() : 0;
	}

}
