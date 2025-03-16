package com.smanzana.nostrummagica.loretag;

import java.util.HashMap;
import java.util.Map;

import com.smanzana.nostrummagica.block.PoisonWaterBlock.PoisonWaterTag;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity.WolfTameLore;
import com.smanzana.nostrummagica.entity.KoidEntity;
import com.smanzana.nostrummagica.entity.LuxEntity;
import com.smanzana.nostrummagica.entity.SpriteEntity;
import com.smanzana.nostrummagica.entity.WilloEntity;
import com.smanzana.nostrummagica.entity.WispEntity;
import com.smanzana.nostrummagica.entity.dragon.DragonEggEntity;
import com.smanzana.nostrummagica.entity.dragon.RedDragonEntity;
import com.smanzana.nostrummagica.entity.dragon.ShadowRedDragonEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity.TameRedDragonLore;
import com.smanzana.nostrummagica.entity.golem.MagicGolemEntity;
import com.smanzana.nostrummagica.pet.IPetWithSoul.SoulBoundLore;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Skeleton;

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
	
	private void init() {
		// All of the compile-time known lore elements are here.
		register(TameRedDragonLore.instance());
		register(TameRedDragonEntity.SoulBoundDragonLore.instance());
		register(SoulBoundLore.instance());
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
		
		register(UndeadLore.instance());
		register(Leaves.instance());
		register(PoisonWaterTag.instance());
	}
	
	public static final class UndeadLore implements IEntityLoreTagged<Skeleton> {
		
		private static UndeadLore instance = null;
		public static UndeadLore instance() {
			if (instance == null) {
				instance = new UndeadLore();
			}
			return instance;
		}

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
		public InfoScreenTabs getTab() {
			return InfoScreenTabs.INFO_ENTITY;
		}

		@Override
		public EntityType<Skeleton> getEntityType() {
			return EntityType.SKELETON;
		}
	}
	
	public static final class Leaves implements IBlockLoreTagged {
		
		private static Leaves instance = null;
		public static Leaves instance() {
			if (instance == null) {
				instance = new Leaves();
			}
			return instance;
		}

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
		public InfoScreenTabs getTab() {
			return InfoScreenTabs.INFO_BLOCKS;
		}

		@Override
		public Block getBlock() {
			return Blocks.OAK_LEAVES;
		}
	}
}
