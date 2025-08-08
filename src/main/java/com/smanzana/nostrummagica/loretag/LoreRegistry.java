package com.smanzana.nostrummagica.loretag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.PoisonWaterBlock.PoisonWaterTag;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.WolfTameLore;
import com.smanzana.nostrummagica.entity.KoidEntity;
import com.smanzana.nostrummagica.entity.LuxEntity;
import com.smanzana.nostrummagica.entity.SpriteEntity;
import com.smanzana.nostrummagica.entity.WilloEntity;
import com.smanzana.nostrummagica.entity.WispEntity;
import com.smanzana.nostrummagica.entity.boss.reddragon.RedDragonEntity;
import com.smanzana.nostrummagica.entity.dragon.DragonEggEntity;
import com.smanzana.nostrummagica.entity.dragon.ShadowRedDragonEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity.TameRedDragonLore;
import com.smanzana.nostrummagica.entity.golem.MagicGolemEntity;
import com.smanzana.nostrummagica.item.NostrumItems;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Provides lookup from key to ILoreTagged to support offlining and onlining
 * of earned lore
 * @author Skyler
 *
 */
public class LoreRegistry {

	private static LoreRegistry instance = null;
	public static LoreRegistry instance() {
		if (instance == null)
			instance = new LoreRegistry();
		
		return instance;
	}
	
	private Map<String, ILoreTagged> lore;
	
	private LoreRegistry() {
		lore = new HashMap<>();
		
		init();
	}
	
	public void register(ILoreTagged tagged) {
		lore.put(tagged.getLoreKey(), tagged);
	}
	
	public ILoreTagged lookup(String key) {
		return lore.get(key);
	}
	
	public Collection<ILoreTagged> allLore() {
		return lore.values();
	}
	
	private void init() {
		// All of the compile-time known lore elements are here.
		register(TameRedDragonLore.instance());
		register(TameRedDragonEntity.SoulBoundDragonLore.instance());
		register(WolfTameLore.instance());
		
		register(LuxEntity.LuxLoreTag.instance());
		register(WispEntity.WispLoreTag.instance());
		register(SpriteEntity.SpriteLoreTag.instance());
		register(DragonEggEntity.DragonEggLore.instance());
		register(MagicGolemEntity.GolemLore.instance());
		register(KoidEntity.KoidLore.instance());
		register(WilloEntity.WilloLoreTag.instance());
		register(RedDragonEntity.RedDragonLore.instance());
		register(ShadowRedDragonEntity.ShadowRedDragonLore.instance());
		register(MagicGolemEntity.GolemLore.instance());
		//register(PlantBossEntity.PlantBossLore.instance());
		
		register(UndeadLore.instance);
		register(Leaves.instance);
		register(PoisonWaterTag.instance);
		register(PushBlockLore);
		register(HitSwitchLore);
		register(BreakBlockLore);
		register(ProgressDoorLore);
		register(SealedDoorLore);
		register(ToggleDoorLore);
		register(LockedDoorLore);
		register(TeleportTileLore);
		register(ShortcutTileLore);
		register(ShrineLore);
		register(TogglePlatformLore);
		register(CursedGlassLore);
		register(KeyChestLore);
		register(KeySwitchLore);
		register(LockedChestLore);
		register(LaserBlockLore);
		register(FogBlockLore);
		register(MysticAnchorLore);
		register(RootingAirLore);
		register(GhostBlockLore);
		
		register(IncantationCastingLore);
		register(QuickCastLore);
		register(SpellSavingLore);
		register(SpellOverchargingLore);
		register(CraftedCastingLore);
		register(ScrollCastingLore);
	}
	
	public static final class UndeadLore implements IEntityLoreTagged<Skeleton> {
		
		public static final UndeadLore instance = new UndeadLore();

		@Override
		public String getLoreKey() {
			return "preset_undead";
		}

		@Override
		public String getLoreDisplayName() {
			return "The Undead";
		}
		
		@Override
		public Lore getBasicLore() {
			return new Lore().add("Undead mobs are those that have literally risen from the grave.", "While bad for business, you suspect the Grave Dust they occasionally carry around could be of some use...");
		}
		
		@Override
		public Lore getDeepLore() {
			return new Lore().add("Undead monsters are those that have been waken from death.", "Because they are not living, they are resistant to ice magics.", "Their dry skin and bones, however, are weak to fire.", "Undead creatures have a chance of dropping Grave Dust, a spell reagent.");
		}

		@Override
		public ELoreCategory getCategory() {
			return ELoreCategory.ENTITY;
		}

		@Override
		public EntityType<Skeleton> getEntityType() {
			return EntityType.SKELETON;
		}
	}
	
	public static final class Leaves implements IBlockLoreTagged {
		
		public static final Leaves instance = new Leaves();

		@Override
		public String getLoreKey() {
			return "preset_leaves";
		}

		@Override
		public String getLoreDisplayName() {
			return "Leaves";
		}
		
		@Override
		public Lore getBasicLore() {
			return new Lore().add("What's up there in the leaves?", "It looks like some sort of dust...");
		}
		
		@Override
		public Lore getDeepLore() {
			return new Lore().add("Leaves catch the small amount of Sky Ash that falls during the day.");
		}

		@Override
		public ELoreCategory getCategory() {
			return ELoreCategory.BLOCK;
		}

		@Override
		public Block getBlock() {
			return Blocks.OAK_LEAVES;
		}
	}
	
	public static MechBlockLore PushBlockLore = new MechBlockLore("push_block", () -> NostrumBlocks.pushBlock);
	public static MechBlockLore HitSwitchLore = new MechBlockLore("hitswitches", () -> NostrumBlocks.switchBlock);
	public static MechBlockLore BreakBlockLore = new MechBlockLore("inflict_block", () -> NostrumBlocks.breakBlock);
	public static MechBlockLore ProgressDoorLore = new MechBlockLore("progression_door", () -> NostrumBlocks.progressionDoor);
	public static MechBlockLore SealedDoorLore = new MechBlockLore("logic_door", () -> NostrumBlocks.logicDoor);
	public static MechBlockLore ToggleDoorLore = new MechBlockLore("toggle_door", () -> NostrumBlocks.toggleDoor);
	public static MechBlockLore LockedDoorLore = new MechBlockLore("locked_door", () -> NostrumBlocks.smallDungeonDoor);
	public static MechBlockLore TeleportTileLore = new MechBlockLore("teleport_rune", () -> NostrumBlocks.teleportRune);
	public static MechBlockLore ShortcutTileLore = new MechBlockLore("shortcut_rune", () -> NostrumBlocks.shortcutRune);
	public static MechBlockLore ShrineLore = new MechBlockLore("shrine", () -> NostrumBlocks.elementShrineBlock);
	public static MechBlockLore TogglePlatformLore = new MechBlockLore("toggle_platform", () -> NostrumBlocks.togglePlatform);
	public static MechBlockLore CursedGlassLore = new MechBlockLore("cursed_glass", () -> NostrumBlocks.cursedGlass);
	public static MechBlockLore KeyChestLore = new MechBlockLore("key_chest", () -> NostrumBlocks.smallDungeonKeyChest);
	public static MechBlockLore KeySwitchLore = new MechBlockLore("key_switch", () -> NostrumBlocks.keySwitch);
	public static MechBlockLore LockedChestLore = new MechBlockLore("locked_chest", () -> Blocks.CHEST);
	public static MechBlockLore LaserBlockLore = new MechBlockLore("laser", () -> NostrumBlocks.laser);
	public static MechBlockLore FogBlockLore = new MechBlockLore("fog", () -> NostrumBlocks.fogBlock);
	public static MechBlockLore MysticAnchorLore = new MechBlockLore("mystic_anchor", () -> NostrumBlocks.mysticAnchor);
	public static MechBlockLore RootingAirLore = new MechBlockLore("rooting_air", () -> NostrumBlocks.rootingAir);
	public static MechBlockLore GhostBlockLore = new MechBlockLore("ghost_block", () -> NostrumBlocks.summonGhostBlock);
	public static MechItemLore IncantationCastingLore = new MechItemLore("casting_incantations", () -> new ItemStack(Items.PLAYER_HEAD));
	public static MechItemLore QuickCastLore = new MechItemLore("quick_cast", () -> new ItemStack(NostrumItems.crystalMedium));
	public static MechItemLore SpellSavingLore = new MechItemLore("spell_saving", () -> new ItemStack(NostrumItems.crystalLarge));
	public static MechItemLore SpellOverchargingLore = new MechItemLore("spell_overcharge", () -> new ItemStack(NostrumItems.spellTomePage));
	public static MechItemLore CraftedCastingLore = new MechItemLore("crafted_casting", () -> new ItemStack(NostrumItems.spellTomeAdvanced));
	public static MechItemLore ScrollCastingLore = new MechItemLore("scroll_casting", () -> new ItemStack(NostrumItems.spellScroll));
	
	private static abstract class MechLore implements ILoreTagged {

		private final String key;
		
		public MechLore(String key) {
			this.key = key;
		}
		
		@Override
		public String getLoreKey() {
			return key;
		}

		@Override
		public String getLoreDisplayName() {
			return I18n.get("lore.%s.name".formatted(key));
		}
		
		@Override
		public Lore getBasicLore() {
			return getDeepLore();
		}

		@Override
		public Lore getDeepLore() {
			return new Lore("lore.%s.desc".formatted(key));
		}

		@Override
		public ELoreCategory getCategory() {
			return ELoreCategory.DUNGEON;
		}
		
	}
	
	private static class MechBlockLore extends MechLore implements IBlockLoreTagged {

		private final Supplier<Block> block;
		
		private @Nullable Block cacheBlock;
		
		public MechBlockLore(String key, Supplier<Block> block) {
			super(key);
			this.block = block;
		}

		@Override
		public Block getBlock() {
			if (cacheBlock == null) {
				cacheBlock = block.get();
			}
			return cacheBlock;
		}
		
	}
	
	private static class MechItemLore extends MechLore implements IItemLoreTagged {

		private final Supplier<ItemStack> stack;
		
		private @Nullable ItemStack cacheStack;
		
		public MechItemLore(String key, Supplier<ItemStack> item) {
			super(key);
			this.stack = item;
		}

		@Override
		public Item getItem() {
			if (cacheStack == null) {
				cacheStack = stack.get();
			}
			return cacheStack.getItem();
		}

		@Override
		public ItemStack makeStack() {
			if (cacheStack == null) {
				cacheStack = stack.get();
			}
			return cacheStack;
		}

		@Override
		public ResourceLocation getItemRegistryName() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
