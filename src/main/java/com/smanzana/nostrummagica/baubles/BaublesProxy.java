package com.smanzana.nostrummagica.baubles;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.baubles.inventory.BaubleInventoryHelper;
import com.smanzana.nostrummagica.baubles.items.ItemMagicBauble;
import com.smanzana.nostrummagica.baubles.items.ItemMagicBauble.ItemType;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.quests.NostrumQuest;
import com.smanzana.nostrummagica.quests.NostrumQuest.QuestType;
import com.smanzana.nostrummagica.quests.rewards.AttributeReward;
import com.smanzana.nostrummagica.quests.rewards.AttributeReward.AwardType;
import com.smanzana.nostrummagica.quests.rewards.IReward;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnItem;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementQuest;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

//
public class BaublesProxy {
	
	private boolean enabled;
	
	public BaublesProxy() {
		this.enabled = false;
	}
	
	public void enable() {
		this.enabled = true;
	}
	
	public boolean preInit() {
		if (!enabled) {
			return false;
		}
		
		registerItems();
		registerBaubleQuests();
		registerBaubleRituals();
		return true;
	}
	
	public boolean init() {
		if (!enabled) {
			return false;
		}
		
		return true;
	}
	
	public boolean postInit() {
		if (!enabled) {
			return false;
		}
		
		registerLore();
		
		return true;
	}
	
	private void registerItems() {
		ItemMagicBauble.instance().setRegistryName(NostrumMagica.MODID, ItemMagicBauble.ID);
    	GameRegistry.register(ItemMagicBauble.instance());
    	ItemMagicBauble.init();
	}
	
	private void registerBaubleQuests() {
		new NostrumQuest("ribbons", QuestType.CHALLENGE, 3,
    			0, // Control
    			0, // Technique
    			0, // Finesse
    			new String[0],
    			null, null,
    			new IReward[]{new AttributeReward(AwardType.REGEN, 0.015f)})
    		.offset(2, -1);
		
		new NostrumQuest("ribbons_enhanced", QuestType.CHALLENGE, 6,
    			0, // Control
    			0, // Technique
    			0, // Finesse
    			new String[] {"ribbons"},
    			null, null,
    			new IReward[]{new AttributeReward(AwardType.MANA, 0.02f)})
    		.offset(3, 2);
		
		new NostrumQuest("rings", QuestType.CHALLENGE, 4,
    			0, // Control
    			0, // Technique
    			0, // Finesse
    			new String[] {"ribbons"},
    			null, null,
    			new IReward[]{new AttributeReward(AwardType.REGEN, 0.025f)})
    		.offset(2, 1);
		
		new NostrumQuest("rings_true", QuestType.CHALLENGE, 7,
    			0, // Control
    			0, // Technique
    			0, // Finesse
    			new String[] {"rings"},
    			null, null,
    			new IReward[]{new AttributeReward(AwardType.REGEN, 0.025f)})
    		.offset(3, 4);
		
		new NostrumQuest("rings_corrupted", QuestType.CHALLENGE, 8,
    			0, // Control
    			0, // Technique
    			0, // Finesse
    			new String[] {"rings_true"},
    			null, null,
    			new IReward[]{new AttributeReward(AwardType.REGEN, 0.05f)})
    		.offset(4, 5);
		
		new NostrumQuest("belts", QuestType.CHALLENGE, 5,
    			0, // Control
    			0, // Technique
    			0, // Finesse
    			new String[] {"rings"},
    			null, null,
    			new IReward[]{new AttributeReward(AwardType.REGEN, 0.025f)})
    		.offset(2, 3);
		
	}
	
	private void registerBaubleRituals() {
		RitualRecipe recipe;
		
		recipe = RitualRecipe.createTier3("small_ribbon",
				ItemMagicBauble.getItem(ItemType.RIBBON_SMALL, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1),
				new ItemStack[] {new ItemStack(Items.GOLD_NUGGET), new ItemStack(Item.getItemFromBlock(Blocks.WOOL)), new ItemStack(Item.getItemFromBlock(Blocks.WOOL)), new ItemStack(Items.GOLD_NUGGET)},
				new RRequirementQuest("ribbons"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RIBBON_SMALL, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("mana_ribbon",
				ItemMagicBauble.getItem(ItemType.RIBBON_MEDIUM, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				ItemMagicBauble.getItem(ItemType.RIBBON_SMALL, 1),
				new ItemStack[] {new ItemStack(Items.GOLD_INGOT), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), null, new ItemStack(Items.GOLD_INGOT)},
				new RRequirementQuest("ribbons"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RIBBON_MEDIUM, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("jeweled_ribbon",
				ItemMagicBauble.getItem(ItemType.RIBBON_LARGE, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.SPIDER_SILK, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				ItemMagicBauble.getItem(ItemType.RIBBON_MEDIUM, 1),
				new ItemStack[] {new ItemStack(Items.DIAMOND), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), new ItemStack(Items.EMERALD), new ItemStack(Items.DIAMOND)},
				new RRequirementQuest("ribbons"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RIBBON_LARGE, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("fierce_ribbon",
				ItemMagicBauble.getItem(ItemType.RIBBON_FIERCE, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST},
				ItemMagicBauble.getItem(ItemType.RIBBON_LARGE, 1),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1), new ItemStack(Items.DIAMOND), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
				new RRequirementQuest("ribbons_enhanced"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RIBBON_FIERCE, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("kind_ribbon",
				ItemMagicBauble.getItem(ItemType.RIBBON_KIND, 1),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.CRYSTABLOOM, ReagentType.SKY_ASH, ReagentType.CRYSTABLOOM},
				ItemMagicBauble.getItem(ItemType.RIBBON_LARGE, 1),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1), new ItemStack(Items.EMERALD), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
				new RRequirementQuest("ribbons_enhanced"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RIBBON_KIND, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("belt_ender",
				ItemMagicBauble.getItem(ItemType.BELT_ENDER, 1),
				EMagicElement.ENDER,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				InfusedGemItem.instance().getGem(EMagicElement.ENDER, 1),
				new ItemStack[] {new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)},
				new RRequirementQuest("belts"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.BELT_ENDER, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("belt_lightning",
				ItemMagicBauble.getItem(ItemType.BELT_LIGHTNING, 1),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.BLACK_PEARL, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				InfusedGemItem.instance().getGem(EMagicElement.LIGHTNING, 1),
				new ItemStack[] {new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), new ItemStack(Items.LEATHER), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)},
				new RRequirementQuest("belts"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.BELT_LIGHTNING, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("ring_gold",
				ItemMagicBauble.getItem(ItemType.RING_GOLD, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST},
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack[] {new ItemStack(Items.GOLD_NUGGET), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), new ItemStack(Items.GOLD_NUGGET), new ItemStack(Items.GOLD_NUGGET)},
				new RRequirementQuest("rings"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RING_GOLD, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("ring_gold_true",
				ItemMagicBauble.getItem(ItemType.RING_GOLD_TRUE, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				ItemMagicBauble.getItem(ItemType.RING_GOLD, 1),
				new ItemStack[] {new ItemStack(Items.GOLD_INGOT), NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1), null, new ItemStack(Items.GOLD_INGOT)},
				new RRequirementQuest("rings_true"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RING_GOLD_TRUE, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("ring_gold_corrupted",
				ItemMagicBauble.getItem(ItemType.RING_GOLD_CORRUPTED, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				ItemMagicBauble.getItem(ItemType.RING_GOLD, 1),
				new ItemStack[] {new ItemStack(Items.GOLD_INGOT), NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), new ItemStack(Items.GOLD_INGOT)},
				new RRequirementQuest("rings_corrupted"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RING_GOLD_CORRUPTED, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		
		ItemStack silver;
		List<ItemStack> silvers = OreDictionary.getOres("ingotSilver");
		
		if (silvers == null || silvers.isEmpty()) {
			silver = new ItemStack(Items.IRON_INGOT);
		} else {
			silver = new ItemStack(silvers.get(0).getItem(), 1, OreDictionary.WILDCARD_VALUE);
		}
		
		recipe = RitualRecipe.createTier3("ring_silver",
				ItemMagicBauble.getItem(ItemType.RING_SILVER, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST, ReagentType.MANI_DUST},
				silver,
				new ItemStack[] {silver, NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), silver, silver},
				new RRequirementQuest("rings"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RING_SILVER, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("ring_silver_true",
				ItemMagicBauble.getItem(ItemType.RING_SILVER_TRUE, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				ItemMagicBauble.getItem(ItemType.RING_SILVER, 1),
				new ItemStack[] {silver, NostrumResourceItem.getItem(ResourceType.SLAB_KIND, 1), silver, silver},
				new RRequirementQuest("rings_true"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RING_SILVER_TRUE, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("ring_silver_corrupted",
				ItemMagicBauble.getItem(ItemType.RING_SILVER_CORRUPTED, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL},
				ItemMagicBauble.getItem(ItemType.RING_SILVER, 1),
				new ItemStack[] {silver, NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), silver},
				new RRequirementQuest("rings_corrupted"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.RING_SILVER_CORRUPTED, 1)));
		RitualRegistry.instance().addRitual(recipe);
		
		recipe = RitualRecipe.createTier3("float_guard",
				ItemMagicBauble.getItem(ItemType.TRINKET_FLOAT_GUARD, 1),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.GRAVE_DUST, ReagentType.MANI_DUST, ReagentType.SKY_ASH},
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1), NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1)},
				new RRequirementQuest("ribbons"),
				new OutcomeSpawnItem(ItemMagicBauble.getItem(ItemType.TRINKET_FLOAT_GUARD, 1)));
		RitualRegistry.instance().addRitual(recipe);
	}
	
	private void registerLore() {
		LoreRegistry.instance().register(ItemMagicBauble.instance());
	}
	
	public IInventory getBaubles(EntityPlayer player) {
		if (!enabled) {
			return null;
		}
		
		return BaubleInventoryHelper.getBaubleInventory(player);
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	
}
