package com.smanzana.nostrummagica.spells;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.potions.FrostbitePotion;
import com.smanzana.nostrummagica.potions.MagicBoostPotion;
import com.smanzana.nostrummagica.potions.MagicResistPotion;
import com.smanzana.nostrummagica.potions.MagicShieldPotion;
import com.smanzana.nostrummagica.potions.PhysicalShieldPotion;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Collection of triggers and shapes.
 * @author Skyler
 *
 */
public class Spell {
	
	public static class SpellPartParam {
		 public float level;
		 public boolean flip;
		 
		 public SpellPartParam(float level, boolean flip) {
			 this.level = level;
			 this.flip = flip;
		 }
	}
	
	public class SpellState {
		private int index;
		private float efficiency;
		private EntityLivingBase caster;
		private EntityLivingBase self;
		private EntityLivingBase other;
		private SpellTrigger.SpellTriggerInstance triggerInstance;
		
		public SpellState(EntityLivingBase caster, float efficiency) {
			index = -1;
			this.caster = this.self = this.other = caster;
			this.efficiency = efficiency;
		}
		
		/**
		 * Callback given to spawned triggers.
		 * Indicates the current trigger has been done and the spell
		 * should move forward
		 */
		public void trigger(List<EntityLivingBase> targets, List<EntityLivingBase> others, World world, List<BlockPos> locations) {
			//for each target/other pair (if more than one), break into multiple spellstates
			// persist index++ and set self/other, then start doing shapes or next trigger
			
			SpellPart next = null;
			if (others == null)
				others = new LinkedList<>();
			
			if (ModConfig.config.spellDebug() && this.caster instanceof EntityPlayer) {
				ITextComponent comp = new TextComponentString(name +  "> "),
						sib;
				
				// Get current trigger
				if (index != -1) {
					SpellPart part = parts.get(index);
					comp.appendText("[" + part.getTrigger().getDisplayName() + "]" );
				}
				
				if (targets != null && targets.size() > 0) {
					String buf = "";
					for (EntityLivingBase ent : targets) {
						buf += ent.getName() + " ";
					}
					comp.appendText("on " + targets.size() + " entities");
					comp.setStyle((new Style()).setColor(TextFormatting.AQUA)
							.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, 
									new TextComponentString(buf))));

					sib = new TextComponentString(" (others)");
					Style style = new Style();
					style.setColor(TextFormatting.DARK_AQUA);
					buf = "";
					if (others != null && others.size() > 0) {
						for (EntityLivingBase ent : others)
							buf += ent.getName() + " ";
					} else {
						buf += this.getSelf().getName();
					}
					style.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new TextComponentString(buf)));
					sib.setStyle(style);
					
					comp.appendSibling(sib);
				} else if (locations != null && !locations.isEmpty()) {
					comp.appendText("on " + locations.size() + " location(s)");
					String buf = "";
					for (BlockPos pos : locations) {
						buf += "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ") ";
					}
					
					comp.setStyle(new Style().setColor(TextFormatting.DARK_GREEN).setHoverEvent(
							new HoverEvent(Action.SHOW_TEXT, new TextComponentString(buf))));
				}
				
				//caster.addChatMessage(comp);
				NostrumMagica.proxy.sendSpellDebug((EntityPlayer) this.caster, comp);
			}
			
			while ((next = (++index < parts.size() ? parts.get(index) : null)) != null && !next.isTrigger()) {
				// it's a shape. Do it idk
				SpellShape shape = next.getShape();
				SpellAction action = solveAction(caster, next.getAlteration(),
						next.getElement(), next.getElementCount());
				SpellPartParam param = next.getParam();
				
//				if (param.flip) {
//					// use other instead of self
//					targs = other;
//				} else {
//					targs = targets;
//				}
				
				if (targets != null && !targets.isEmpty()) {
					for (EntityLivingBase targ : targets) {
						shape.perform(action, param, targ, null, null, this.efficiency);
					}
				} else if (locations != null && !locations.isEmpty()) {
					// use locations
					for (BlockPos pos : locations) {
						shape.perform(action, param, null, world, pos, this.efficiency);
					}
				} else {
					; // Drop it on the floor\
					next = null;
				}
			}
			
			// next is either null or a trigger
			if (next == null) {
				// end of spell
				finish();
			} else {
				// If we have more than one target/pos we hit, split here so each
				// can proceed at their own pace
				
				// targs is correct (targets or others) based on last spell shape (or
				// just targets if no previous shape)
				
				if (index > 0) {
					NostrumMagicaSounds.CAST_CONTINUE.play(self);
				}
				
				if (targets != null && !targets.isEmpty()) {
					if (targets.size() == 1) {
						// don't need to split
						// Also base case after a split happens
						
						// Adjust self, other like if we split
						if (others.size() > 0)
							this.other = others.get(0);
						else
							this.other = this.self;
						this.self = targets.get(0);
						spawnTrigger(next.getTrigger(), this.self, null, null, next.getParam());
					} else {
						index--; // Make splits have same trigger as we're performing now
						for (int i = 0; i < targets.size(); i++) {
							EntityLivingBase targ = targets.get(i);
							EntityLivingBase other;
							if (others.size() >= i)
								other = others.get(i);
							else
								other = this.self;
							SpellState sub = split();//(targ, other);
							sub.trigger(Lists.newArrayList(targ), Lists.newArrayList(other),
									world, null);
						}
					}
				} else {
					if (locations.size() == 1) {
						// Base case here, too. Instantiate trigger!!!!
						spawnTrigger(next.getTrigger(), null, world, locations.get(0), next.getParam());
					} else {
						index--; // Make splits have same trigger as we're performing now
						for (BlockPos targ : locations) {
							SpellState sub = split();
							sub.trigger(null, null,
									world, Lists.newArrayList(targ));
						}
					}
				}
								
			}
		}
		
		private void spawnTrigger(SpellTrigger trigger, EntityLivingBase targ, World world, BlockPos targpos, SpellPartParam param) {
			// instantiate trigger in world
			Vec3d pos;
			if (world == null)
				world = targ.worldObj;
			if (targ == null)
				pos = new Vec3d(targpos.getX(), targpos.getY(), targpos.getZ());
			else
				pos = targ.getPositionVector().addVector(0, targ.getEyeHeight(), 0);
			
			this.triggerInstance = trigger.instance(this, world, pos,
					(targ == null ? -90.0f : targ.rotationPitch),
					(targ == null ? 0.0f : targ.rotationYaw),
					param);
			this.triggerInstance.init(caster);
		}
		
		private SpellState split() {
			SpellState spawn = new SpellState(caster, this.efficiency);
			spawn.index = this.index;
//			spawn.self = self;
//			spawn.other = other;
			
			return spawn;
		}

		public EntityLivingBase getSelf() {
			return self;
		}

		public EntityLivingBase getOther() {
			return other;
		}

		public EntityLivingBase getCaster() {
			return caster;
		}

		/**
		 * Called when triggers fail to be triggered and have failed.
		 */
		public void triggerFail() {
			finish();
		}
		
		private void finish() {
			; // Nothing I can think of right now, but maybe in the future...
		}
	}
	
	public static class SpellPart {
		private SpellTrigger trigger;
		private SpellShape shape;
		private EAlteration alteration;
		private EMagicElement element;
		private int elementCount;
		private SpellPartParam param;
		
		public SpellPart(SpellTrigger trigger, SpellPartParam param) {
			this.trigger = trigger;
			this.param = param;
		}
		
		public SpellPart(SpellTrigger trigger) {
			this(trigger, new SpellPartParam(0, false));
		}
		
		public SpellPart(SpellShape shape, EMagicElement element, int count, EAlteration alt,
				SpellPartParam param) {
			this.shape = shape;
			this.element = element;
			this.elementCount = count;
			this.alteration = alt;
			this.param = param;
		}
		
		public SpellPart(SpellShape shape, EMagicElement element, int count,
				EAlteration alteration) {
			this(shape, element, count, alteration, new SpellPartParam(0, false));
		}
		
		public boolean isTrigger() {
			return trigger != null;
		}

		public SpellTrigger getTrigger() {
			return trigger;
		}

		public SpellShape getShape() {
			return shape;
		}

		public EAlteration getAlteration() {
			return alteration;
		}

		public EMagicElement getElement() {
			return element;
		}

		public int getElementCount() {
			return elementCount;
		}

		public SpellPartParam getParam() {
			return param;
		}
		
		public void setParam(SpellPartParam param) {
			this.param = param;
		}
	}

	private String name;
	private int registryID;
	private List<SpellPart> parts;
	private int manaCost;
	
	// TODO some cool effects and stuff
	
	private Spell() {
		this.parts = new LinkedList<>();
		manaCost = -1; // un-calculated value
		name = "";
	}
	
	/**
	 * Creates a new spell and registers it in the registry.
	 * @param name
	 */
	public Spell(String name) {
		this(name, false);
	}
	
	public Spell(String name, boolean trans) {
		this();
		this.name = name;
		
		if (trans)
			registryID = NostrumMagica.spellRegistry.registerTransient(this);
		else
			registryID = NostrumMagica.spellRegistry.register(this);
	}
	
	public static Spell CreateInternal(String name, int id) {
		Spell s = new Spell();
		s.name = name;
		s.registryID = id;
		
		NostrumMagica.spellRegistry.override(id, s);
		return s;
	}
	
	public void addPart(SpellPart part) {
		this.parts.add(part);
		manaCost = -1;
	}
	
	public String getName() {
		return name;
	}
	
	public int getRegistryID() {
		return registryID;
	}
	
	public void cast(EntityLivingBase caster, float efficiency) {
		SpellState state = new SpellState(caster, efficiency);
		Spell.onCast(caster, this);
		state.trigger(Lists.newArrayList(caster), null, null, null);
		
		NostrumMagicaSounds.CAST_LAUNCH.play(caster);
	}
	
	public String crc() {
		String s = "";
		for (SpellPart part : parts) {
			if (part.isTrigger())
				s += part.getTrigger().getTriggerKey();
			else
				s += part.getShape().getShapeKey();
		}
		
		return s;
	}
	
	public int getManaCost() {
		if (manaCost != -1)
			return manaCost;
		
		// Triggers can report their  cost
		// Alterations are in enum
		// Shapes cost 15
		// First elem is free. Extra costs 30 ea
		// Rolling multiplier makes it more expensive for one long spell vs many small
		// (rate of 1.2x)
		float cost = 0f;
		float multiplier = 1f;
		
		for (SpellPart part : parts) {
			if (part.isTrigger())
				cost += multiplier * (float) part.getTrigger().getManaCost();
			else {
				cost += multiplier * 15f;
				if (part.getElementCount() > 1)
					cost += multiplier * (float) (30 * (part.getElementCount() - 1));
				if (part.getAlteration() != null)
					cost += multiplier * (float) part.getAlteration().getCost();
			}
			multiplier *= 1.2;
		}
		
		manaCost = (int) Math.ceil(cost);
		return manaCost;
	}
	
	public Map<ReagentType, Integer> getRequiredReagents() {
		Map<ReagentType, Integer> costs = new EnumMap<ReagentType, Integer>(ReagentType.class);
		
		for (ReagentType type : ReagentType.values())
			costs.put(type, 0);
		
		ReagentType type;
		for (SpellPart part : parts) {
			if (part.isTrigger()) {
				for (ItemStack req : part.getTrigger().getReagents()) {
					type = ReagentItem.findType(req);
					int count = costs.get(type);
					count += req.stackSize;
					costs.put(type, count);
				}
			} else {
				for (ItemStack req : part.getShape().getReagents()) {
					type = ReagentItem.findType(req);
					int count = costs.get(type);
					count += req.stackSize;
					costs.put(type, count);
				}
				if (part.getAlteration() != null) {
					for (ItemStack req : part.getAlteration().getReagents()) {
						type = ReagentItem.findType(req);
						int count = costs.get(type);
						count += req.stackSize;
						costs.put(type, count);
					}
				}
			}
				
		}
		
		return costs;
	}
	
	// seen is if they've seen it before or not
	public float getXP(boolean seen) {
		// Triggers add some
		// Shapes add some
		// More elements mean more xp
		// Alterations give some
		// 300% first time you use it
		
		float total = 0f;
		
		for (SpellPart part : parts) {
			if (part.isTrigger())
				total += 2f;
			else {
				total += 1f;
				if (part.getElementCount() > 1)
					total += (float) (Math.pow(2, part.getElementCount() - 1));
				if (part.getAlteration() != null)
					total += 5f;
			}
		}
		
		if (!seen)
			total *= 3f;
		return total;
	}
	
	public static final SpellAction solveAction(EntityLivingBase caster, EAlteration alteration,
			EMagicElement element, int elementCount) {
		
		// Could do a registry with hooks here, if wanted it to be extensible
		
		if (alteration == null) {
			// Damage spell
			return new SpellAction(caster).damage(element, 5.0f * elementCount);
		}
		
		switch (alteration) {
		case ALTER:
			return solveAlter(caster, element, elementCount);
		case CONJURE:
			return solveConjure(caster, element, elementCount);
		case ENCHANT:
			return solveEnchant(caster, element, elementCount);
		case GROWTH:
			return solveGrowth(caster, element, elementCount);
		case INFLICT:
			return solveInflict(caster, element, elementCount);
		case RESIST:
			return solveResist(caster, element, elementCount);
		case SUMMON:
			return solveSummon(caster, element, elementCount);
		case SUPPORT:
			return solveSupport(caster, element, elementCount);
		default:
			return null;
		}
	}
	
	private static final SpellAction solveAlter(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).transmute(elementCount);
		case EARTH:
		case ENDER:
		case FIRE:
		case ICE:
		case LIGHTNING:
		case WIND:
			return new SpellAction(caster).infuse(element, elementCount);
		}
		
		return null;
	}
	
	private static final SpellAction solveInflict(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("weakness"), duration, amp);
		case EARTH:
			return new SpellAction(caster).status(RootedPotion.instance(), duration, amp);
		case ENDER:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("blindness"), duration, amp);
		case FIRE:
			return new SpellAction(caster).burn(duration);
		case ICE:
			return new SpellAction(caster).status(FrostbitePotion.instance(), duration, amp);
		case LIGHTNING:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("slowness"), (int) (duration * .7), 5);
		case WIND:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("poison"), duration, amp);
		}
		
		return null;
	}
	
	private static final SpellAction solveResist(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("resistance"), duration, amp);
		case EARTH:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("strength"), duration, amp);
		case ENDER:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("invisibility"), duration, amp);
		case FIRE:
			return new SpellAction(caster).status(MagicBoostPotion.instance(), duration, amp);
		case ICE:
			return new SpellAction(caster).dispel(elementCount * (int) (Math.pow(3, elementCount - 1)));
		case LIGHTNING:
			return new SpellAction(caster).status(MagicResistPotion.instance(), duration, amp);
		case WIND:
			return new SpellAction(caster).push(5f + (2 * amp), elementCount);
		}
		
		return null;
	}
	
	private static final SpellAction solveSupport(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("absorption"), duration * 5, amp);
		case EARTH:
			return new SpellAction(caster).status(PhysicalShieldPotion.instance(), duration, amp);
		case ENDER:
			return new SpellAction(caster).blink(15.0f * elementCount);
		case FIRE:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("fire_resistance"), duration, amp);
		case ICE:
			return new SpellAction(caster).status(MagicShieldPotion.instance(), duration, amp);
		case LIGHTNING:
			return new SpellAction(caster).pull(5 * elementCount, elementCount);
		case WIND:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("speed"), duration, amp);
		}
		
		return null;
	}
	
	private static final SpellAction solveGrowth(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("saturation"), 1, 4 * elementCount);
		case EARTH:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("regeneration"), duration, amp);
		case ENDER:
			return new SpellAction(caster).swap();
		case FIRE:
			return new SpellAction(caster).burnArmor(elementCount);
		case ICE:
			return new SpellAction(caster).heal((float) Math.pow(4f, elementCount));
		case LIGHTNING:
			return new SpellAction(caster).status(Potion.getPotionFromResourceLocation("jump_boost"), duration, amp);
		case WIND:
			return new SpellAction(caster).propel(elementCount);
		}
		
		return null;
	}
	
	private static final SpellAction solveEnchant(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		return new SpellAction(caster).enchant(element, elementCount);
	}
	
	private static final SpellAction solveConjure(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).blockBreak(elementCount);
		case EARTH:
			return new SpellAction(caster).grow(elementCount);
		case ENDER:
			return new SpellAction(caster).phase(elementCount);
		case FIRE:
			return new SpellAction(caster).burn(elementCount * 20 * 5);
		case ICE:
			return new SpellAction(caster).cursedIce(elementCount);
		case LIGHTNING:
			return new SpellAction(caster).lightning();
		case WIND:
			return new SpellAction(caster).wall(elementCount);
		}
		
		return null;
	}
	
	private static final SpellAction solveSummon(EntityLivingBase caster, EMagicElement element,
			int elementCount) {
		return new SpellAction(caster).summon(element, elementCount);
	}
	
	private static final String NBT_SPELL_NAME = "name";
	private static final String NBT_LIST = "parts";
	
	// Parts
	private static final String NBT_KEY = "key";
	private static final String NBT_PARAM_LEVEL = "level";
	private static final String NBT_PARAM_FLIP = "flip";
	private static final String NBT_ELEMENT = "element";
	private static final String NBT_ALTERATION = "alteration";
	private static final String NBT_COUNT = "count";
	
	public NBTTagCompound toNBT() {
		NBTTagList list = new NBTTagList();
		
		NBTTagCompound compound;
		for (SpellPart part : parts) {
			compound = new NBTTagCompound();
			if (part.isTrigger()) {
				compound.setString(NBT_KEY, part.getTrigger().getTriggerKey());
			} else {
				compound.setString(NBT_KEY, part.getShape().getShapeKey());
				compound.setString(NBT_ELEMENT, part.getElement().name());
				compound.setInteger(NBT_COUNT, part.getElementCount());
				if (part.getAlteration() != null)
					compound.setString(NBT_ALTERATION, part.getAlteration().name());
			}
			
			compound.setFloat(NBT_PARAM_LEVEL, part.getParam().level);
			compound.setBoolean(NBT_PARAM_FLIP, part.getParam().flip);
			
			list.appendTag(compound);
		}
		
		compound = new NBTTagCompound();
		compound.setString(NBT_SPELL_NAME, name);
		compound.setTag(NBT_LIST, list);
		return compound;
	}
	
	/**
	 * Deserializes a spell from NBT.
	 * Does not register it in the registry
	 * @param nbt
	 * @param id
	 * @return
	 */
	public static Spell fromNBT(NBTTagCompound nbt, int id) {
		if (nbt == null)
			return null;
		
		String name = nbt.getString(NBT_SPELL_NAME); 
		Spell spell = new Spell();
		spell.name = name;
		spell.registryID = id;
		
		NBTTagList list = nbt.getTagList(NBT_LIST, NBT.TAG_COMPOUND);
		NBTTagCompound tag;
		String key;
		EMagicElement element;
		EAlteration alteration;
		int count;
		SpellPartParam param;
		
		for (int i = 0; i < list.tagCount(); i++) {
			tag = list.getCompoundTagAt(i);
			key = tag.getString(NBT_KEY);
			if (key == null || key.isEmpty())
				continue;
			
			param = new SpellPartParam(
					tag.getFloat(NBT_PARAM_LEVEL),
					tag.getBoolean(NBT_PARAM_FLIP)
					);
			
			if (tag.hasKey(NBT_ELEMENT, NBT.TAG_STRING)) {
				SpellShape shape = SpellShape.get(key);
				
				if (shape == null)
					continue;
				
				count = tag.getInteger(NBT_COUNT);
				try {
					element = EMagicElement.valueOf(tag.getString(NBT_ELEMENT));
				} catch (Exception e) {
					NostrumMagica.logger.error("Could not parse element " + tag.getString(NBT_ELEMENT));
					element = EMagicElement.PHYSICAL;
				}
				
				alteration = null;
				if (tag.hasKey(NBT_ALTERATION, NBT.TAG_STRING))
				try {
					alteration = EAlteration.valueOf(tag.getString(NBT_ALTERATION));
				} catch (Exception e) {
					NostrumMagica.logger.error("Could not parse alteration " + tag.getString(tag.getString(NBT_ALTERATION)));
					alteration = null;
				}
				
				spell.addPart(new SpellPart(shape, element, count, alteration, param));
				
			} else {
				SpellTrigger trigger = SpellTrigger.get(key);
				if (trigger == null)
					continue;
				
				spell.addPart(new SpellPart(trigger, param));
			}
		}
		
		return spell;
	}

	private EMagicElement primaryCache = null;
	public EMagicElement getPrimaryElement() {
		if (primaryCache == null) {
			primaryCache = EMagicElement.PHYSICAL;
			
			for (SpellPart part : parts) {
				if (!part.isTrigger()) {
					primaryCache = part.element;
					break;
				}
			}
		}
		return primaryCache;
	}

	private String descriptionCache = null;
	/**
	 * Run through all parts and come up with some sort of descriptiion
	 * of the spell
	 * @return
	 */
	public String getDescription() {
		if (descriptionCache == null) {
			descriptionCache = "A";
			
			int triggers = 0;
			int shapes = 0;
			boolean beneficial = false;
			boolean damage = false;
			boolean restorative = false;
			boolean status = false;
			boolean highPotency = false;
			boolean enchant = false;
			boolean summon = false;
			
			for (SpellPart part : parts) {
				if (part.isTrigger()) {
					triggers++;
				} else {
					shapes++;
					if (part.getElementCount() > 2)
						highPotency = true;
					if (part.alteration == null)
						damage = true;
					if (part.alteration == EAlteration.INFLICT
							|| part.alteration == EAlteration.RESIST
							|| part.alteration == EAlteration.SUPPORT)
						status = true;
					if (part.alteration == EAlteration.RESIST
							|| part.alteration == EAlteration.SUPPORT
							|| part.alteration == EAlteration.GROWTH
							|| part.alteration == EAlteration.ENCHANT)
						beneficial = true;
					if (part.alteration == EAlteration.SUMMON)
						summon = true;
					if (part.alteration == EAlteration.ENCHANT)
						enchant = true;
					if (part.alteration == EAlteration.GROWTH)
						restorative = true;
				}
			}
			
			if (highPotency)
				descriptionCache += " high-potency";
			if (triggers > 3)
				descriptionCache += " complex";
			if (shapes > 5)
				descriptionCache += " invocation";
			else
				descriptionCache += " spell";
			
			descriptionCache += " that";
			if (summon) {
				descriptionCache += " summons a creature.";
			} else if (beneficial) {
				descriptionCache += " provides aid";
				if (enchant) {
					descriptionCache += " by providing an offensive enchantment.";
				} else if (restorative) {
					descriptionCache += " through restorative magics.";
				} else if (status) {
					descriptionCache += " in the form of magical augments.";
				} else if (damage) {
					descriptionCache += " as well as harms.";
				} else {
					// This cast can't be hit currently.
					descriptionCache += ".";
				}
			} else {
				if (status) {
					descriptionCache += " debilitates the target.";
				} else if (damage) {
					descriptionCache += " deals damage to the enemy.";
				} else {
					descriptionCache += " performs some sort of alteration.";
				}
			}
			
		}
		
		return descriptionCache;
	}

	public boolean isEmpty() {
		if (parts.isEmpty())
			return true;
		
		boolean trig = false;
		for (SpellPart part : parts) {
			if (part.isTrigger()) {
				trig = true;
				continue;
			}
				
			 if (!part.isTrigger() && trig)
				 return false;
		}
		
		return true;
	}
	
	public static interface ICastListener {
		
		public void onCast(EntityLivingBase entity, Spell spell);
	}
	
	private static List<ICastListener> castListeners = new LinkedList<>();
	public static void registerCastListener(ICastListener listener) {
		castListeners.add(listener);
	}
	
	private static void onCast(EntityLivingBase entity, Spell spell) {
		if (castListeners.isEmpty())
			return;
		
		for (ICastListener l : castListeners) {
			l.onCast(entity, spell);
		}
	}
	
	public int getComponentCount() {
		int count = 0;
		if (!parts.isEmpty())
			count = parts.size();
		return count;
	}
	
	public int getElementCount() {
		int count = 0;
		if (!parts.isEmpty())
		for (SpellPart part : parts) {
			if (!part.isTrigger())
				count += part.elementCount;
		}
		return count;
	}
	
	public int getTriggerCount() {
		int count = 0;
		if (!parts.isEmpty())
		for (SpellPart part : parts) {
			if (part.isTrigger())
				count++;
		}
		return count;
	}
	
	public Map<EMagicElement, Integer> getElements() {
		Map<EMagicElement, Integer> list = new EnumMap<>(EMagicElement.class);
		if (!parts.isEmpty())
		for (SpellPart part : parts) {
			if (!part.isTrigger() && part.element != null) {
				int count = 0;
				if (list.get(part.element) != null)
					count = list.get(part.element);
				count += part.elementCount;
				list.put(part.element, count);
			} 
		}
		return list;
	}
	
	public Map<EAlteration, Integer> getAlterations() {
		Map<EAlteration, Integer> list = new EnumMap<>(EAlteration.class);
		if (!parts.isEmpty())
		for (SpellPart part : parts) {
			if (!part.isTrigger() && part.alteration != null) {
				int count = 0;
				if (list.get(part.alteration) != null)
					count = list.get(part.alteration);
				count++;
				list.put(part.alteration, count);
			}
		}
		return list;
	}
	
	public Map<SpellTrigger, Integer> getTriggers() {
		Map<SpellTrigger, Integer> list = new HashMap<>();
		if (!parts.isEmpty())
		for (SpellPart part : parts) {
			if (part.isTrigger()) {
				int count = 0;
				if (list.get(part.getTrigger()) != null)
					count = list.get(part.getTrigger());
				count++;
				list.put(part.getTrigger(), count);
			}
		}
		return list;
	}
	
	public Map<SpellShape, Integer> getShapes() {
		Map<SpellShape, Integer> list = new HashMap<>();
		if (!parts.isEmpty())
		for (SpellPart part : parts) {
			if (!part.isTrigger()) {
				int count = 0;
				if (list.get(part.getShape()) != null)
					count = list.get(part.getShape());
				count++;
				list.put(part.getShape(), count);
			}
		}
		return list;
	}

	public SpellComponentWrapper getRandomComponent() {
		if (this.parts == null || this.parts.size() == 0)
			return null;
		int index = NostrumMagica.rand.nextInt(this.parts.size());
		SpellPart part = parts.get(index);
		if (part.isTrigger())
			return new SpellComponentWrapper(part.trigger);
		else {
			if (part.alteration != null && NostrumMagica.rand.nextInt(3) == 0) {
				return new SpellComponentWrapper(part.alteration);
			}
			if (part.element != null && NostrumMagica.rand.nextBoolean()) {
				return new SpellComponentWrapper(part.element);
			}
			return new SpellComponentWrapper(part.shape);
		}
	}
	
}
