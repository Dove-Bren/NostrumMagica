package com.smanzana.nostrummagica.loretag;

import java.util.HashMap;
import java.util.Map;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.WolfTameLore;
import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.entity.EntityLux;
import com.smanzana.nostrummagica.entity.EntitySprite;
import com.smanzana.nostrummagica.entity.EntityWisp;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonEgg;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed.TameRedDragonLore;
import com.smanzana.nostrummagica.entity.golem.EntityGolem;
import com.smanzana.nostrummagica.pet.IPetWithSoul.SoulBoundLore;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.world.World;

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
//		register(ReagentItem.instance());
//		register(SpellTome.instance());
//		register(SpellPlate.instance());
//		register(SpellTomePage.instance());
//		register(SpellScroll.instance());
//		register(SpellRune.instance());
//		register(ReagentBag.instance());
//		register(MagicSwordBase.instance());
//		register(MagicArmorBase.helm());
//		register(BlankScroll.instance());
//		register(SpellTableItem.instance());
//		register(InfusedGemItem.instance());
//		register(AltarItem.instance());
//		register(ChalkItem.instance());
//		register(EssenceItem.instance());
//		register(MasteryOrb.instance());
//		register(MirrorItem.instance());
//		register(NostrumResourceItem.instance());
//		register(PositionCrystal.instance());
//		register(PositionToken.instance());
//		register(SeekerIdol.instance());
//		register(ShrineSeekingGem.instance());
		//register(new EntityDragonRed(null));
//		register(MageStaff.instance());
//		register(ThanosStaff.instance());
//		register(ThanoPendant.instance());
//		register(MagicCharm.instance());
//		register(RuneBag.instance());
//		register(DragonEggFragment.instance());
//		register(DragonEgg.instance());
//		register(NostrumRoseItem.instance());
//		register(NostrumSkillItem.instance());
		register(TameRedDragonLore.instance());
//		register(MirrorShield.instance());
//		register(MirrorShieldImproved.instance());
//		register(HookshotItem.instance());
//		register(WarlockSword.instance());
//		register(DragonSoulItem.instance());
		register(EntityTameDragonRed.SoulBoundDragonLore.instance());
//		register(SoulDagger.instance());
//		register(ArcaneWolfSoulItem.instance());
		register(SoulBoundLore.instance());
		register(WolfTameLore.instance());
//		register(ParadoxMirrorBlock.instance());
		
		register(EntityLux.LuxLoreTag.instance());
		register(EntityWisp.WispLoreTag.instance());
		register(EntitySprite.SpriteLoreTag.instance());
		register(EntityDragonEgg.DragonEggLore.instance());
		register(EntityGolem.GolemLore.instance());
		register(EntityKoid.KoidLore.instance());
		
		
		for (Preset preset : Preset.values()) {
			register(preset);
		}
	}
	
	public static ILoreTagged getPreset(LivingEntity entityLiving) {
		if (entityLiving == null)
			return null;
		
		for (Preset preset : Preset.values()) {
			if (preset.matches(entityLiving)) {
				return preset;
			}
		}
		
		return null;
	}
	
	public static ILoreTagged getPreset(Block block) {
		if (block == null)
			return null;
		
		for (Preset preset : Preset.values()) {
			if (preset.matches(block)) {
				return preset;
			}
		}
		
		return null;
	}
	
	public static enum Preset implements ILoreTagged {
		
		UNDEAD(new Filter() {public boolean matches(LivingEntity base) {
				return base.isEntityUndead();
			}
			
		}, SkeletonEntity.class,  "Undead", new String[] {"Undead mobs are those that have literally risen from the grave.", "While bad for business, you suspect the Grave Dust they occasionally carry around could be of some use..."}, "Undead monsters are those that have been waken from death.", "Because they are not living, they are resistant to ice magics.", "Their dry skin and bones, however, are weak to fire.", "Undead creatures have a chance of dropping Grave Dust, a spell reagent."),
		LEAVES(Blocks.OAK_LEAVES, "Leaves", new String[] {"What's up there in the leaves?", "It looks like some sort of dust..."}, "Leaves catch the small amount of Sky Ash that falls during the day.");
		
		protected static interface Filter {
			public boolean matches(LivingEntity base);
		}
		
		private Filter filter;
		private Class<? extends LivingEntity> clazz;
		private Block block;
		private Lore basic;
		private Lore deep;
		private String key;
		
		private Preset(Class<? extends LivingEntity> clazz, String key, String lore[], String ... deep) {
			this.clazz = clazz;
			this.key = key;
			this.basic = new Lore();
			this.basic.add(lore);
			this.deep = new Lore();
			this.deep.add(deep);
		}
		
		private Preset(Filter filter, Class<? extends LivingEntity> icon, String key, String lore[], String ... deep) {
			this.filter = filter;
			this.key = key;
			this.basic = new Lore();
			this.basic.add(lore);
			this.deep = new Lore();
			this.deep.add(deep);
			this.clazz = icon;
		}
		
		private Preset(Block block, String key, String lore[], String ... deep) {
			this.block = block;
			this.key = key;
			this.basic = new Lore();
			this.basic.add(lore);
			this.deep = new Lore();
			this.deep.add(deep);
		}

		@Override
		public String getLoreKey() {
			return key;
		}

		@Override
		public String getLoreDisplayName() {
			return key;
		}

		@Override
		public Lore getBasicLore() {
			return basic;
		}

		@Override
		public Lore getDeepLore() {
			return deep;
		}
		
		public boolean matches(LivingEntity base) {
			if (clazz != null && clazz.isAssignableFrom(base.getClass()))
				return true;
			
			if (filter != null)
				return filter.matches(base);
			
			return false;
		}
		
		public boolean matches(Block block) {
			if (this.block != null)
				return this.block == block;
			
			return false;
		}

		@Override
		public InfoScreenTabs getTab() {
			if (this.block != null)
				return InfoScreenTabs.INFO_BLOCKS;
			else
				return InfoScreenTabs.INFO_ENTITY;
		}

		public Block getBlock() {
			return this.block;
		}
		
		public LivingEntity getEntity(World world) {
			try {
				return this.clazz.getConstructor(World.class)
						.newInstance(world);
			} catch (Exception e) {
				return null;
			}
		}
		
	}
}
