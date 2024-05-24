package com.smanzana.nostrummagica.spells;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
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
	
	public class SpellState {
		private int index;
		private float efficiency;
		private LivingEntity caster;
		private LivingEntity self;
		private LivingEntity other;
		private SpellTrigger.SpellTriggerInstance triggerInstance;
		
		public SpellState(LivingEntity caster, float efficiency) {
			index = -1;
			this.caster = this.self = this.other = caster;
			this.efficiency = efficiency;
		}
		
		/**
		 * Callback given to spawned triggers.
		 * Indicates the current trigger has been done and the spell
		 * should move forward
		 */
		public void trigger(List<LivingEntity> targets, List<LivingEntity> others, World world, List<BlockPos> locations) {
			this.trigger(targets, others, world, locations, false);
		}
		
		public void trigger(List<LivingEntity> targets, List<LivingEntity> others, World world, List<BlockPos> locations, boolean forceSplit) {
			//for each target/other pair (if more than one), break into multiple spellstates
			// persist index++ and set self/other, then start doing shapes or next trigger
			
			SpellPart next = null;
			if (others == null)
				others = new LinkedList<>();

			
			// If being forced to split, dupe state right now and continue on that
			if (forceSplit) {
				this.split().trigger(targets, others, world, locations, false);
			} else {
			
				if (ModConfig.config.spellDebug() && this.caster instanceof PlayerEntity) {
					TextComponent comp = new StringTextComponent(""),
							sib;
					
					sib = new StringTextComponent(name +  "> ");
					sib.setStyle(Style.EMPTY.setBold(true).applyFormatting(TextFormatting.GOLD));
					comp.append(sib);
					sib = new StringTextComponent("");
					
					// Get current trigger
					if (index == -1) {
						sib.appendString(" <<Start Cast>> ");
					}
					else {
						SpellPart part = parts.get(index);
						sib.appendString("[" + part.getTrigger().getDisplayName() + "] " );
						if (part.getParam().flip || Math.abs(part.getParam().level) > .001) {
							Style style = Style.EMPTY;
							String buf = "";
							if (part.getParam().flip) {
								buf = "Inverted ";
							}
							if (Math.abs(part.getParam().level) > .001) {
								buf += String.format("Level %02.1f", part.getParam().level);
							}
							style.setHoverEvent(new HoverEvent(Action.SHOW_TEXT,
									new StringTextComponent(buf)));
							sib.setStyle(style);
						}
					}
					
					if (targets != null && targets.size() > 0) {
						String buf = "";
						for (LivingEntity ent : targets) {
							buf += ent.getName() + " ";
						}
						sib.appendString("on ");
						sib.setStyle((Style.EMPTY).applyFormatting(TextFormatting.AQUA).setBold(false));
						comp.append(sib);
						sib = new StringTextComponent(targets.size() + " entities");
						sib.setStyle((Style.EMPTY).applyFormatting(TextFormatting.RED)
								.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, 
										new StringTextComponent(buf))));
						comp.append(sib);
	
						sib = new StringTextComponent(" (others)");
						Style style = Style.EMPTY;
						style.applyFormatting(TextFormatting.DARK_PURPLE);
						buf = "";
						if (others.size() > 0) {
							for (LivingEntity ent : others)
								buf += ent.getName() + " ";
						} else {
							buf += this.getSelf().getName();
						}
						style.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new StringTextComponent(buf)));
						sib.setStyle(style);
						
						comp.append(sib);
					} else if (locations != null && !locations.isEmpty()) {
						sib.appendString("on ");
						sib.setStyle((Style.EMPTY).applyFormatting(TextFormatting.AQUA).setBold(false));
						comp.append(sib);
						sib = new StringTextComponent(locations.size() + " location(s)");
						String buf = "";
						for (BlockPos pos : locations) {
							buf += "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ") ";
						}
						
						sib.setStyle(Style.EMPTY.applyFormatting(TextFormatting.DARK_GREEN).setHoverEvent(
								new HoverEvent(Action.SHOW_TEXT, new StringTextComponent(buf))));
						comp.append(sib);
					} else {
						sib.appendString("no targets");
						sib.setStyle((Style.EMPTY).applyFormatting(TextFormatting.AQUA));
						comp.append(sib);
					}
					
					//caster.addChatMessage(comp);
					NostrumMagica.instance.proxy.sendSpellDebug((PlayerEntity) this.caster, comp);
				}
			
				boolean first = true;
				while ((next = (++index < parts.size() ? parts.get(index) : null)) != null && !next.isTrigger()) {
					// it's a shape. Do it idk
					SpellShape shape = next.getShape();
					SpellAction action = solveAction(caster, next.getAlteration(),
							next.getElement(), next.getElementCount());
					SpellPartProperties param = next.getParam();
					
					INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
					if (attr != null && attr.isUnlocked()) {
						attr.setKnowledge(next.getElement(), next.getAlteration());
					}
					
					final List<LivingEntity> affectedEnts = new ArrayList<>();
					final List<BlockPos> affectedPos = new ArrayList<>();
					
					if (targets != null && !targets.isEmpty()) {
						for (LivingEntity targ : targets) {
							shape.perform(action, param, targ, null, null, this.efficiency, affectedEnts, affectedPos);
						}
					} else if (locations != null && !locations.isEmpty()) {
						// use locations
						for (BlockPos pos : locations) {
							shape.perform(action, param, null, world, pos, this.efficiency, affectedEnts, affectedPos);
						}
					} else {
						; // Drop it on the floor\
						next = null;
					}
					

					if (first && next != null) {
						final boolean harmful = action.getProperties().isHarmful;
						
						if (!affectedEnts.isEmpty())
						for (LivingEntity affected : affectedEnts) {
							SpellComponentWrapper comp;
							if (next.getAlteration() == null)
								comp = new SpellComponentWrapper(next.getElement());
							else
								comp = new SpellComponentWrapper(next.getAlteration());
							
							NostrumMagica.instance.proxy.spawnEffect(world, comp,
									caster, null, affected, null,
									new SpellComponentWrapper(next.getElement()), harmful, 0);
						}
						
						if (!affectedPos.isEmpty())
						for (BlockPos affectPos : affectedPos) {
							SpellComponentWrapper comp;
							if (next.getAlteration() == null)
								comp = new SpellComponentWrapper(next.getElement());
							else
								comp = new SpellComponentWrapper(next.getAlteration());
							
							NostrumMagica.instance.proxy.spawnEffect(world, comp,
									caster, null, null, new Vector3d(affectPos.getX() + .5, affectPos.getY(), affectPos.getZ() + .5),
									new SpellComponentWrapper(next.getElement()), harmful, 0);
						}
						
						// One more for the shape itself
						final @Nullable LivingEntity centerEnt = (targets == null || targets.isEmpty() ? null : targets.get(0));
						final @Nullable BlockPos centerBP = (locations == null || locations.isEmpty() ? null : locations.get(0));
						if (centerEnt != null || centerBP != null) {
							final Vector3d centerPos = (centerEnt == null ? new Vector3d(centerBP.getX() + .5, centerBP.getY(), centerBP.getZ() + .5) : centerEnt.getPositionVec().add(0, centerEnt.getHeight() / 2, 0));
							final float p= (shape.supportedFloats() == null || shape.supportedFloats().length == 0 ? 0 : (
									param.level == 0f ? shape.supportedFloats()[0] : param.level));
							NostrumMagica.instance.proxy.spawnEffect(world, new SpellComponentWrapper(shape),
									caster, null, null, centerPos,
									new SpellComponentWrapper(next.getElement()), harmful, p);
						}
					}
					
					first = false;
				}
				
				// next is either null or a trigger
				if (next == null) {
					// end of spell
					finish();
				} else {
					// If we have more than one target/pos we hit (or if we're being forced to split), split here so each
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
								LivingEntity targ = targets.get(i);
								LivingEntity other;
								if (others.size() >= i)
									other = others.get(i);
								else
									other = this.self;
								SpellState sub = split();//(targ, other);
								sub.trigger(Lists.newArrayList(targ), Lists.newArrayList(other),
										world, null);
							}
							index++;
						}
					} else if (locations != null && !locations.isEmpty()) {
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
							index++;
						}
					} else {
						// Last trigger couldn't carry us forward
					}
									
				}
			}
		}
		
		private void spawnTrigger(SpellTrigger trigger, LivingEntity targ, World world, BlockPos targpos, SpellPartProperties param) {
			// instantiate trigger in world
			Vector3d pos;
			if (world == null)
				world = targ.world;
			if (targ == null)
				pos = new Vector3d(targpos.getX() + .5, targpos.getY(), targpos.getZ() + .5);
			else
				pos = targ.getPositionVec();
			
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

		public LivingEntity getSelf() {
			return self;
		}

		public LivingEntity getOther() {
			return other;
		}

		public LivingEntity getCaster() {
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
		
		public EMagicElement getNextElement() {
			for (int i = this.index; i < parts.size(); i++) {
				SpellPart part = parts.get(i);
				if (part.isTrigger()) {
					continue;
				}
				
				if (part.getElement() == null) {
					System.out.print(".");
				}
				
				return part.getElement() == null ? EMagicElement.PHYSICAL : part.getElement();
			}
			
			return EMagicElement.PHYSICAL;
		}
	}
	
	private String name;
	private int iconIndex; // Basically useless on server, selects which icon to show on the client
	private int registryID;
	private List<SpellPart> parts;
	private final int manaCost;
	private final int weight;
	
	private Spell(int manaCost, int weight) {
		this.parts = new LinkedList<>();
		this.manaCost = manaCost;
		this.weight = weight;
		name = "";
		iconIndex = 0;
	}
	
	/**
	 * Creates a new spell and registers it in the registry.
	 * @param name
	 */
	public Spell(String name, int manaCost, int weight) {
		this(name, false, manaCost, weight);
	}
	
	public Spell(String name, boolean trans, int manaCost, int weight) {
		this(manaCost, weight);
		this.name = name;
		
		if (trans)
			registryID = NostrumMagica.instance.getSpellRegistry().registerTransient(this);
		else
			registryID = NostrumMagica.instance.getSpellRegistry().register(this);
	}
	
	public static Spell CreateFake(String name, int id) {
		Spell s = new Spell(0, 0);
		s.name = name;
		s.registryID = id;
		
		NostrumMagica.instance.getSpellRegistry().override(id, s);
		return s;
	}
	
	public static Spell CreateAISpell(String name) {
		return new Spell(name, true, 50, 10);
	}
	
	/**
	 * Takes a transient spell and makes it an official, non-transient spell
	 */
	public void promoteFromTrans() {
		NostrumMagica.instance.getSpellRegistry().removeTransientStatus(this);
	}
	
	public void setIcon(int index) {
		this.iconIndex = index;
	}
	
	public Spell addPart(SpellPart part) {
		this.parts.add(part);
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public int getRegistryID() {
		return registryID;
	}
	
	public int getIconIndex() {
		return this.iconIndex;
	}
	
	public void cast(LivingEntity caster, float efficiency) {
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
		return manaCost;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public Map<ReagentType, Integer> getRequiredReagents() {
		Map<ReagentType, Integer> costs = new EnumMap<ReagentType, Integer>(ReagentType.class);
		
		for (ReagentType type : ReagentType.values())
			costs.put(type, 0);
		
		ReagentType type;
		for (SpellPart part : parts) {
			if (part.isTrigger()) {
				for (ItemStack req : part.getTrigger().getReagents()) {
					type = ReagentItem.FindType(req);
					int count = costs.get(type);
					count += req.getCount();
					costs.put(type, count);
				}
			} else {
				for (ItemStack req : part.getShape().getReagents()) {
					type = ReagentItem.FindType(req);
					int count = costs.get(type);
					count += req.getCount();
					costs.put(type, count);
				}
				if (part.getAlteration() != null) {
					for (ItemStack req : part.getAlteration().getReagents()) {
						type = ReagentItem.FindType(req);
						int count = costs.get(type);
						count += req.getCount();
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
				total += 3f;
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
	
	public static final SpellAction solveAction(LivingEntity caster, EAlteration alteration,
			EMagicElement element, int elementCount) {
		
		// Could do a registry with hooks here, if wanted it to be extensible
		
		if (alteration == null) {
			// Damage spell
			return new SpellAction(caster).damage(element, (float) (elementCount + 1))
					.name("damage." + element.name().toLowerCase());
		}
		
		switch (alteration) {
		case RUIN:
			return solveRuin(caster, element, elementCount);
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
		case CORRUPT:
			return solveCorrupt(caster, element, elementCount);
		}
		
		return null;
	}
	
	private static final SpellAction solveRuin(LivingEntity caster, EMagicElement element,
			int elementCount) {
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).transmute(elementCount).name("transmute");
		case EARTH:
		case ENDER:
		case FIRE:
		case ICE:
		case LIGHTNING:
		case WIND:
			return new SpellAction(caster).damage(element, 2f + (float) (2 * (elementCount+1)))
					.name("ruin." + element.name().toLowerCase());
		}
		
		return null;
	}
	
	private static final SpellAction solveInflict(LivingEntity caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Effects.WEAKNESS, duration, amp).name("weakness");
		case EARTH:
			return new SpellAction(caster).status(NostrumEffects.rooted, duration, amp).name("rooted");
		case ENDER:
			return new SpellAction(caster).status(Effects.BLINDNESS, duration, amp).name("blindness");
		case FIRE:
			return new SpellAction(caster).status(Effects.NAUSEA, duration / 2, amp).damage(EMagicElement.FIRE, 1 + (amp / 2)).name("overheat");
		case ICE:
			return new SpellAction(caster).status(NostrumEffects.frostbite, duration, amp).name("frostbite");
		case LIGHTNING:
			return new SpellAction(caster).status(Effects.SLOWNESS, (int) (duration * .7), amp + 1).name("slowness");
		case WIND:
			return new SpellAction(caster).status(Effects.POISON, duration, amp).name("poison");
		}
		
		return null;
	}
	
	private static final SpellAction solveResist(LivingEntity caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Effects.RESISTANCE, duration, amp).name("resistance");
		case EARTH:
			return new SpellAction(caster).status(Effects.STRENGTH, duration, amp).name("strength");
		case ENDER:
			return new SpellAction(caster).status(Effects.INVISIBILITY, duration, amp).name("invisibility");
		case FIRE:
			return new SpellAction(caster).status(Effects.FIRE_RESISTANCE, duration, amp).name("fireresist");
		case ICE:
			return new SpellAction(caster).dispel(elementCount * (int) (Math.pow(3, elementCount - 1))).name("dispel");
		case LIGHTNING:
			return new SpellAction(caster).status(NostrumEffects.magicResist, duration, amp).name("magicresist");
		case WIND:
			return new SpellAction(caster).push(5f + (2 * amp), elementCount).name("push");
		}
		
		return null;
	}
	
	private static final SpellAction solveSupport(LivingEntity caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Effects.ABSORPTION, duration * 5, amp).name("lifeboost");
		case EARTH:
			return new SpellAction(caster).status(NostrumEffects.physicalShield, duration, amp + 1).name("shield.physical");
		case ENDER:
			return new SpellAction(caster).blink(15.0f * elementCount).name("blink");
		case FIRE:
			return new SpellAction(caster).status(NostrumEffects.magicBoost, duration, amp).name("magicboost");
		case ICE:
			return new SpellAction(caster).status(NostrumEffects.magicShield, duration, amp + 1).name("shield.magic");
		case LIGHTNING:
			return new SpellAction(caster).pull(5 * elementCount, elementCount).name("pull");
		case WIND:
			return new SpellAction(caster).status(Effects.SPEED, duration, amp).name("speed");
		}
		
		return null;
	}
	
	private static final SpellAction solveGrowth(LivingEntity caster, EMagicElement element,
			int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(Effects.SATURATION, 1, 4 * elementCount).name("food");
		case EARTH:
			return new SpellAction(caster).status(Effects.REGENERATION, duration, amp).name("regen");
		case ENDER:
			return new SpellAction(caster).swap().name("swap");
		case FIRE:
			return new SpellAction(caster).burnArmor(elementCount).name("burnarmor");
		case ICE:
			return new SpellAction(caster).heal((float) Math.pow(4f, elementCount)).name("heal");
		case LIGHTNING:
			return new SpellAction(caster).status(Effects.JUMP_BOOST, duration, amp).name("jumpboost");
		case WIND:
			return new SpellAction(caster).propel(elementCount).name("propel");
		}
		
		return null;
	}
	
	private static final SpellAction solveEnchant(LivingEntity caster, EMagicElement element,
			int elementCount) {
		return new SpellAction(caster).enchant(element, elementCount).name("enchant." + element.name().toLowerCase());
	}
	
	private static final SpellAction solveConjure(LivingEntity caster, EMagicElement element,
			int elementCount) {
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).blockBreak(elementCount).name("break");
		case EARTH:
			return new SpellAction(caster).grow(elementCount).name("grow");
		case ENDER:
			return new SpellAction(caster).phase(elementCount).name("phase");
		case FIRE:
			return new SpellAction(caster).burn(elementCount * 20 * 5).name("burn");
		case ICE:
			return new SpellAction(caster).cursedIce(elementCount).name("cursedice");
		case LIGHTNING:
			return new SpellAction(caster).lightning().name("lightningbolt");
		case WIND:
			return new SpellAction(caster).wall(elementCount).name("magicwall");
		}
		
		return null;
	}
	
	private static final SpellAction solveSummon(LivingEntity caster, EMagicElement element,
			int elementCount) {
		return new SpellAction(caster).summon(element, elementCount).name("summon." + element.name().toLowerCase());
	}
	
	private static final SpellAction solveCorrupt(LivingEntity caster, EMagicElement element, int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction(caster).status(NostrumEffects.rend, duration, amp).name("rend");
		case EARTH:
			return new SpellAction(caster).harvest(elementCount).name("harvest");
		case ENDER:
			return new SpellAction(caster).status(NostrumEffects.disruption, duration, amp).name("disruption");
		case FIRE:
			return new SpellAction(caster).status(NostrumEffects.sublimation, duration, amp).name("sublimation");
		case ICE:
			return new SpellAction(caster).status(NostrumEffects.healResist, duration, amp).name("healresist");
		case LIGHTNING:
			return new SpellAction(caster).status(NostrumEffects.magicRend, duration, amp).name("magicrend");
		case WIND:
			return new SpellAction(caster).status(NostrumEffects.fastFall, duration, amp).name("fastfall");
		}
		
		return null;
	}
	
	private static final String NBT_SPELL_NAME = "name";
	private static final String NBT_MANA_COST = "mana_cost";
	private static final String NBT_WEIGHT = "spell_weight";
	private static final String NBT_LIST = "parts";
	private static final String NBT_ICON_INDEX = "ico_index";
	
	// Parts
	private static final String NBT_KEY = "key";
	private static final String NBT_PARAM_LEVEL = "level";
	private static final String NBT_PARAM_FLIP = "flip";
	private static final String NBT_ELEMENT = "element";
	private static final String NBT_ALTERATION = "alteration";
	private static final String NBT_COUNT = "count";
	
	public CompoundNBT toNBT() {
		ListNBT list = new ListNBT();
		
		CompoundNBT compound;
		for (SpellPart part : parts) {
			compound = new CompoundNBT();
			if (part.isTrigger()) {
				compound.putString(NBT_KEY, part.getTrigger().getTriggerKey());
			} else {
				compound.putString(NBT_KEY, part.getShape().getShapeKey());
				compound.putString(NBT_ELEMENT, part.getElement().name());
				compound.putInt(NBT_COUNT, part.getElementCount());
				if (part.getAlteration() != null)
					compound.putString(NBT_ALTERATION, part.getAlteration().name());
			}
			
			compound.putFloat(NBT_PARAM_LEVEL, part.getParam().level);
			compound.putBoolean(NBT_PARAM_FLIP, part.getParam().flip);
			
			list.add(compound);
		}
		
		compound = new CompoundNBT();
		compound.putString(NBT_SPELL_NAME, name);
		compound.putInt(NBT_ICON_INDEX, iconIndex);
		compound.putInt(NBT_MANA_COST, manaCost);
		compound.putInt(NBT_WEIGHT, weight);
		compound.put(NBT_LIST, list);
		return compound;
	}
	
	/**
	 * Deserializes a spell from NBT.
	 * Does not register it in the registry
	 * @param nbt
	 * @param id
	 * @return
	 */
	public static Spell fromNBT(CompoundNBT nbt, int id) {
		if (nbt == null)
			return null;
		
		String name = nbt.getString(NBT_SPELL_NAME); 
		int index = nbt.getInt(NBT_ICON_INDEX);
		int manaCost = nbt.getInt(NBT_MANA_COST);
		int weight = nbt.getInt(NBT_WEIGHT);
		
		{
			if (!nbt.contains(NBT_MANA_COST)) {
				NostrumMagica.logger.warn("Found spell with no recorded mana cost! Making absurd. " + name + "[" + id + "]");
				manaCost = 10000;
			}
		}
		
		Spell spell = new Spell(manaCost, weight);
		spell.name = name;
		spell.registryID = id;
		spell.iconIndex = index;
		
		ListNBT list = nbt.getList(NBT_LIST, NBT.TAG_COMPOUND);
		CompoundNBT tag;
		String key;
		EMagicElement element;
		EAlteration alteration;
		int count;
		SpellPartProperties param;
		
		for (int i = 0; i < list.size(); i++) {
			tag = list.getCompound(i);
			key = tag.getString(NBT_KEY);
			if (key == null || key.isEmpty())
				continue;
			
			param = new SpellPartProperties(
					tag.getFloat(NBT_PARAM_LEVEL),
					tag.getBoolean(NBT_PARAM_FLIP)
					);
			
			if (tag.contains(NBT_ELEMENT, NBT.TAG_STRING)) {
				SpellShape shape = SpellShape.get(key);
				
				if (shape == null)
					continue;
				
				count = tag.getInt(NBT_COUNT);
				try {
					element = EMagicElement.valueOf(tag.getString(NBT_ELEMENT));
				} catch (Exception e) {
					NostrumMagica.logger.error("Could not parse element " + tag.getString(NBT_ELEMENT));
					element = EMagicElement.PHYSICAL;
				}
				
				alteration = null;
				if (tag.contains(NBT_ALTERATION, NBT.TAG_STRING))
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
					primaryCache = part.getElement();
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
					if (part.getAlteration() == null)
						damage = true;
					if (part.getAlteration() == EAlteration.INFLICT
							|| part.getAlteration() == EAlteration.RESIST
							|| part.getAlteration() == EAlteration.SUPPORT
							|| part.getAlteration() == EAlteration.CORRUPT)
						status = true;
					if (part.getAlteration() == EAlteration.RESIST
							|| part.getAlteration() == EAlteration.SUPPORT
							|| part.getAlteration() == EAlteration.GROWTH
							|| part.getAlteration() == EAlteration.ENCHANT)
						beneficial = true;
					if (part.getAlteration() == EAlteration.SUMMON)
						summon = true;
					if (part.getAlteration() == EAlteration.ENCHANT)
						enchant = true;
					if (part.getAlteration() == EAlteration.GROWTH)
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
		
		public void onCast(LivingEntity entity, Spell spell);
	}
	
	private static List<ICastListener> castListeners = new LinkedList<>();
	public static void registerCastListener(ICastListener listener) {
		castListeners.add(listener);
	}
	
	private static void onCast(LivingEntity entity, Spell spell) {
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
				count += part.getElementCount();
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
			if (!part.isTrigger() && part.getElement() != null) {
				int count = 0;
				if (list.get(part.getElement()) != null)
					count = list.get(part.getElement());
				count += part.getElementCount();
				list.put(part.getElement(), count);
			} 
		}
		return list;
	}
	
	public Map<EAlteration, Integer> getAlterations() {
		Map<EAlteration, Integer> list = new EnumMap<>(EAlteration.class);
		if (!parts.isEmpty())
		for (SpellPart part : parts) {
			if (!part.isTrigger() && part.getAlteration() != null) {
				int count = 0;
				if (list.get(part.getAlteration()) != null)
					count = list.get(part.getAlteration());
				count++;
				list.put(part.getAlteration(), count);
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
			return new SpellComponentWrapper(part.getTrigger());
		else {
			if (part.getAlteration() != null && NostrumMagica.rand.nextInt(3) == 0) {
				return new SpellComponentWrapper(part.getAlteration());
			}
			if (part.getElement() != null && NostrumMagica.rand.nextBoolean()) {
				return new SpellComponentWrapper(part.getElement());
			}
			return new SpellComponentWrapper(part.getShape());
		}
	}
	
	public List<SpellPart> getSpellParts() {
		return this.parts;
	}
	
	/**
	 * Whether (the first part of this spell) wants tracing, to show indicators when an enemy is being looked at.
	 * For example, seeking bullet needs the player to be looking at an enemy to select who to go after.
	 * @return
	 */
	public boolean shouldTrace() {
		if (!getSpellParts().isEmpty()) {
			SpellPart firstTrigger = getSpellParts().get(0);
			return firstTrigger.getTrigger().shouldTrace(firstTrigger.getParam());
		}
		
		return false;
	}
	
	public double getTraceRange() {
		if (!getSpellParts().isEmpty()) {
			SpellPart firstTrigger = getSpellParts().get(0);
			return firstTrigger.getTrigger().getTraceRange(firstTrigger.getParam());
		}
		
		return 0;
	}
}
