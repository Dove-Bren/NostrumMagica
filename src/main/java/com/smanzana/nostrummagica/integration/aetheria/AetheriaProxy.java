package com.smanzana.nostrummagica.integration.aetheria;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrumaetheria.api.proxy.APIProxy;
import com.smanzana.nostrumaetheria.api.recipes.IAetherUnravelerRecipe;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityWisp;
import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuser;
import com.smanzana.nostrummagica.integration.aetheria.blocks.AetherInfuserTileEntity;
import com.smanzana.nostrummagica.integration.aetheria.blocks.WispBlock;
import com.smanzana.nostrummagica.integration.aetheria.blocks.WispBlock.WispBlockTileEntity;
import com.smanzana.nostrummagica.integration.aetheria.items.AetherResourceType;
import com.smanzana.nostrummagica.integration.aetheria.items.ItemAetherLens;
import com.smanzana.nostrummagica.integration.aetheria.items.ItemAetherLens.LensType;
import com.smanzana.nostrummagica.integration.aetheria.items.NostrumAetherResourceItem;
import com.smanzana.nostrummagica.items.NostrumItemTags;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.research.NostrumResearch.NostrumResearchTab;
import com.smanzana.nostrummagica.research.NostrumResearch.Size;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeCreateAetherInfuser;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnItem;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementResearch;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(NostrumMagica.MODID)
public class AetheriaProxy {
	private boolean enabled;
	
	public AetheriaProxy() {
		this.enabled = false;
	}
	
	public void enable() {
		this.enabled = true;
	}
	
	@ObjectHolder(NostrumAetherResourceItem.ID_GINSENG_FLOWER) public static Item ginsengFlower;
	@ObjectHolder(NostrumAetherResourceItem.ID_MANDRAKE_FLOWER) public static Item mandrakeFlower;
	@ObjectHolder(ItemAetherLens.ID_SPREAD) public static Item spreadAetherLens;
	@ObjectHolder(ItemAetherLens.ID_CHARGE) public static Item chargeAetherLens;
	@ObjectHolder(ItemAetherLens.ID_GROW) public static Item growAetherLens;
	@ObjectHolder(ItemAetherLens.ID_SWIFTNESS) public static Item swiftnessAetherLens;
	@ObjectHolder(ItemAetherLens.ID_ELEVATOR) public static Item elevatorAetherLens;
	@ObjectHolder(ItemAetherLens.ID_HEAL) public static Item healAetherLens;
	@ObjectHolder(ItemAetherLens.ID_BORE) public static Item boreAetherLens;
	@ObjectHolder(ItemAetherLens.ID_BORE_REVERSED) public static Item reversedBoreAetherLens;
	@ObjectHolder(ItemAetherLens.ID_MANA_REGEN) public static Item manaRegenAetherLens;
	@ObjectHolder(ItemAetherLens.ID_NO_SPAWN) public static Item noSpawnAetherLens;
	
	public static Item ItemLens = null;
	public static Block BlockWisp = null;
	public static Block BlockerInfuser = null;
	
	public boolean preInit() {
		if (!enabled) {
			return false;
		}
		
		MinecraftForge.EVENT_BUS.register(this);
		
		return true;
	}
	
	public boolean init() {
		if (!enabled) {
			return false;
		}

		registerAetheriaQuests();
		registerAetheriaRituals();
		registerAetheriaResearch();
		
		// register unravel recipes
		APIProxy.addUnravelerRecipe(new TomeUnravelerRecipe());
		
		return true;
	}
	
	public boolean postInit() {
		if (!enabled) {
			return false;
		}
		
		registerLore();
		
		return true;
	}
	
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
    	final IForgeRegistry<Item> registry = event.getRegistry();
    	
    	registry.register(new NostrumAetherResourceItem(300, 450, NostrumItems.PropBase()).setRegistryName(NostrumAetherResourceItem.ID_GINSENG_FLOWER));
		registry.register(new NostrumAetherResourceItem(300, 350, NostrumItems.PropBase()).setRegistryName(NostrumAetherResourceItem.ID_MANDRAKE_FLOWER));
    	registry.register(new ItemAetherLens(ItemAetherLens.LensType.SPREAD, NostrumItems.PropBase()).setRegistryName(ItemAetherLens.ID_SPREAD));
    	registry.register(new ItemAetherLens(ItemAetherLens.LensType.CHARGE, NostrumItems.PropBase()).setRegistryName(ItemAetherLens.ID_CHARGE));
    	registry.register(new ItemAetherLens(ItemAetherLens.LensType.GROW, NostrumItems.PropBase()).setRegistryName(ItemAetherLens.ID_GROW));
    	registry.register(new ItemAetherLens(ItemAetherLens.LensType.SWIFTNESS, NostrumItems.PropBase()).setRegistryName(ItemAetherLens.ID_SWIFTNESS));
    	registry.register(new ItemAetherLens(ItemAetherLens.LensType.ELEVATOR, NostrumItems.PropBase()).setRegistryName(ItemAetherLens.ID_ELEVATOR));
    	registry.register(new ItemAetherLens(ItemAetherLens.LensType.HEAL, NostrumItems.PropBase()).setRegistryName(ItemAetherLens.ID_HEAL));
    	registry.register(new ItemAetherLens(ItemAetherLens.LensType.BORE, NostrumItems.PropBase()).setRegistryName(ItemAetherLens.ID_BORE));
    	registry.register(new ItemAetherLens(ItemAetherLens.LensType.BORE_REVERSED, NostrumItems.PropBase()).setRegistryName(ItemAetherLens.ID_BORE_REVERSED));
    	registry.register(new ItemAetherLens(ItemAetherLens.LensType.MANA_REGEN, NostrumItems.PropBase()).setRegistryName(ItemAetherLens.ID_MANA_REGEN));
    	registry.register(new ItemAetherLens(ItemAetherLens.LensType.NO_SPAWN, NostrumItems.PropBase()).setRegistryName(ItemAetherLens.ID_NO_SPAWN));
    	
    	registry.register(
    			(new BlockItem(WispBlock.instance()).setRegistryName(WispBlock.ID)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(WispBlock.ID))
    	);
    	registry.register(
    			(new BlockItem(AetherInfuser.instance()).setRegistryName(AetherInfuser.ID)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(AetherInfuser.ID))
    	);
	}
	
	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
    	final IForgeRegistry<Block> registry = event.getRegistry();
    	
		BlockWisp = WispBlock.instance();
		registry.register(BlockWisp);
		BlockerInfuser = AetherInfuser.instance();
		registry.register(BlockerInfuser);
		
    	GameRegistry.registerTileEntity(WispBlockTileEntity.class, new ResourceLocation(NostrumMagica.MODID, WispBlock.ID + "_entity"));
    	GameRegistry.registerTileEntity(AetherInfuserTileEntity.class, new ResourceLocation(NostrumMagica.MODID, AetherInfuser.ID + "_entity"));
	}
	
	private void registerAetheriaQuests() {
		
	}
	
	private void registerAetheriaRituals() {
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("wisp_crystal",
						new ItemStack(WispBlock.instance()),
						null,
						new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST},
						Ingredient.fromItems(NostrumItems.altarItem),
						new Ingredient[] {
								Ingredient.fromTag(NostrumItemTags.Items.CrystalMedium),
								Ingredient.fromStacks(APIProxy.AetherBatterySmallBlock),
								Ingredient.fromTag(Tags.Items.OBSIDIAN),
								Ingredient.fromTag(NostrumItemTags.Items.CrystalMedium),
								},
						new RRequirementResearch("wispblock"),
						new OutcomeSpawnItem(new ItemStack(WispBlock.instance()))
						)
				);
		
		RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("construct_aether_infuser",
						new ItemStack(BlockerInfuser),
						null,
						new ReagentType[] {ReagentType.GINSENG, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
						Ingredient.fromTag(Tags.Items.OBSIDIAN),
						new Ingredient[] {
								Ingredient.fromStacks(new ItemStack(APIProxy.AetherBatterySmallBlock)),
								Ingredient.fromTag(NostrumItemTags.Items.CrystalMedium),
								Ingredient.fromStacks(new ItemStack(APIProxy.AetherBatterySmallBlock)),
								Ingredient.fromStacks(new ItemStack(APIProxy.AetherBatterySmallBlock))},
						new RRequirementResearch("aether_infusers"),
						new OutcomeCreateAetherInfuser()
						)
				);
		
		for (LensType type : LensType.values()) {
			Ingredient ingredient = type.getIngredient();
			if (ingredient == Ingredient.EMPTY) {
				continue;
			}
			
			RitualRegistry.instance().addRitual(
				RitualRecipe.createTier3("make_lens_" + type.getUnlocSuffix(),
						new ItemStack(ItemAetherLens.GetLens(type)),
						null,
						new ReagentType[] {ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
						Ingredient.fromTag(Tags.Items.GLASS_PANES),
						new Ingredient[] {Ingredient.EMPTY, ingredient, Ingredient.fromTag(NostrumItemTags.Items.CrystalSmall), Ingredient.EMPTY},
						new RRequirementResearch("aether_infusers"),
						new OutcomeSpawnItem(new ItemStack(ItemAetherLens.GetLens(type)))
				)
			);
		}
	}
	
	private void registerAetheriaResearch() {
		NostrumResearch.startBuilding()
			.hiddenParent("kani")
			.hiddenParent("aether_battery")
			.lore(EntityWisp.LoreKey)
			.reference("ritual::wisp_crystal", "ritual.wisp_crystal.name")
		.build("wispblock", (NostrumResearchTab) APIProxy.ResearchTab, Size.NORMAL, -3, 2, true, new ItemStack(WispBlock.instance()));
		
		NostrumResearch.startBuilding()
			.hiddenParent("aether_battery")
			.parent("aether_charger")
			.reference("ritual::construct_aether_infuser", "ritual.construct_aether_infuser.name")
			.reference("ritual::make_lens_spread", "ritual.make_lens_spread.name")
			.reference("ritual::make_lens_charge", "ritual.make_lens_wide_charge.name")
			.reference("ritual::make_lens_grow", "ritual.make_lens_grow.name")
			.reference("ritual::make_lens_heal", "ritual.make_lens_heal.name")
			.reference("ritual::make_lens_bore", "ritual.make_lens_bore.name")
			.reference("ritual::make_lens_elevator", "ritual.make_lens_elevator.name")
			.reference("ritual::make_lens_swiftness", "ritual.make_lens_swiftness.name")
		.build("aether_infusers", (NostrumResearchTab) APIProxy.ResearchTab, Size.NORMAL, 0, 2, true, new ItemStack(BlockerInfuser));
	}
	
	private void registerLore() {
		LoreRegistry.instance().register((ILoreTagged) APIProxy.PassivePendantItem);
		LoreRegistry.instance().register((ILoreTagged) APIProxy.ActivePendantItem);
		LoreRegistry.instance().register((ILoreTagged) ItemLens);
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public ItemStack getResourceItem(AetherResourceType type, int count) {
		Item item = null;
		switch (type) {
		case FLOWER_GINSENG:
			item = ginsengFlower;
			break;
		case FLOWER_MANDRAKE:
			item = mandrakeFlower;
			break;
		default:
			break;
		}
		
		if (item == null) {
			return ItemStack.EMPTY;
		} else {
			return new ItemStack(item, count);
		}
	}

	public boolean CreateAetherInfuser(World world, BlockPos pos) {
		if (isEnabled()) {
			AetherInfuser.SetBlock(world, pos, true);
			for (BlockPos offset : new BlockPos[] {
				pos.north(), pos.south(), pos.east(), pos.west(), pos.north().east(), pos.north().west(), pos.south().east(), pos.south().west()
			}) {
				AetherInfuser.SetBlock(world, offset, false);
			}
			return true;
		}
		
		return false;
	}

	public void reinitResearch() {
		registerAetheriaResearch();
	}
	
	protected static class TomeUnravelerRecipe implements IAetherUnravelerRecipe {

		private static final int AETHER_COST = 5000;
		private static final int DURATION = 20 * 120;
		
		@Override
		public boolean matches(@Nonnull ItemStack stack) {
			return stack.getItem() instanceof SpellTome && !SpellTome.getSpells(stack).isEmpty();
		}

		@Override
		public int getAetherCost(ItemStack stack) {
			return AETHER_COST;
		}
		
		@Override
		public int getDuration(ItemStack stack) {
			return DURATION;
		}

		@Override
		public NonNullList<ItemStack> unravel(ItemStack stack) {
			NonNullList<ItemStack> ret = NonNullList.create();
			ret.add(stack.copy());
			
			List<Spell> spells = SpellTome.getSpells(stack);
			if (spells == null || spells.isEmpty()) {
				;
			} else {
				for (Spell spell : spells) {
					ItemStack scroll = SpellScroll.create(spell);
					ret.add(scroll);
				}
			}
			
			// Clear tome
			SpellTome.clearSpells(ret.get(0));
			
			return ret;
		}
		
	}
}
