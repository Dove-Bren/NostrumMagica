package com.smanzana.nostrummagica.spell;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.criteria.CastSpellCriteriaTrigger;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.spell.log.ISpellLogBuilder;
import com.smanzana.nostrummagica.spell.log.SpellLogBuilder;
import com.smanzana.nostrummagica.spell.log.SpellLogEntry;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;
import com.smanzana.nostrummagica.stat.PlayerStat;
import com.smanzana.nostrummagica.stat.PlayerStatTracker;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A collection of spell shapes and effects.
 * Shapes all are resolved first, after which effects are applied.
 * If shapes affect multiple people, effects are applied to all.
 * @author Skyler
 *
 */
public class Spell {
	
	public static interface ISpellState {
		public LivingEntity getSelf();
		public LivingEntity getCaster();
		public @Nullable LivingEntity getTargetHint();
		
		public boolean isPreview();
		// May not be supported if isPreview() is true
		public void triggerFail(SpellLocation pos);
		public default void trigger(List<LivingEntity> targets, List<SpellLocation> locations) {
			this.trigger(targets, locations, 1f, false);
		}
		public void trigger(List<LivingEntity> targets, List<SpellLocation> locations, float stageEfficiency, boolean forceSplit);
	}
	
	private final String name;
	private final int manaCost;
	private final int weight;
	private final List<SpellShapePart> shapes;
	private final List<SpellEffectPart> parts;

	private int registryID;
	private @Nonnull SpellCharacteristics characteristics;
	private int iconIndex; // Basically useless on server, selects which icon to show on the client
	
	private Spell(String name, int manaCost, int weight, boolean dummy) {
		this.shapes = new ArrayList<>();
		this.parts = new ArrayList<>();
		this.manaCost = manaCost;
		this.weight = weight;
		this.name = name;
		
		iconIndex = 0;
		determineCharacteristics();
	}

	/**
	 * Creates a new spell and registers it in the registry.
	 * @param name
	 */
	public Spell(String name, int manaCost, int weight) {
		this(name, false, manaCost, weight);
	}
	
	public Spell(String name, boolean trans, int manaCost, int weight) {
		this(name, manaCost, weight, false);
		
		if (trans)
			registryID = NostrumMagica.instance.getSpellRegistry().registerTransient(this);
		else
			registryID = NostrumMagica.instance.getSpellRegistry().register(this);
	}
	
	public static Spell CreateFake(String name, int id) {
		Spell s = new Spell(name, 0, 0);
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
	
	protected void determineCharacteristics() {
		boolean harmful = false;
		EMagicElement element = EMagicElement.PHYSICAL;
		
		if (!parts.isEmpty()) {
			element = parts.get(0).getElement();
			
			for (SpellEffectPart part : parts) {
				SpellAction action = SpellEffects.solveAction(part.getAlteration(), part.getElement(), part.getElementCount());
				if (action.getProperties().isHarmful) {
					harmful = true;
					break;
				}
			}
		}
		
		this.characteristics = new SpellCharacteristics(harmful, element);
	}
	
	public void setIcon(int index) {
		this.iconIndex = index;
	}
	
	public Spell addPart(SpellShapePart part) {
		this.shapes.add(part);
		return this;
	}
	
	public Spell addPart(SpellEffectPart part) {
		this.parts.add(part);
		determineCharacteristics();
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
		cast(caster, efficiency, null);
	}
	
	public void cast(LivingEntity caster, float efficiency, @Nullable LivingEntity targetHint) {
		if (!caster.getServer().isSameThread()) {
			throw new IllegalStateException("Can't cast spell on a thread other than the game thread");
		}
		
		final ISpellLogBuilder logger;
		if (caster instanceof Player && NostrumMagica.instance.proxy.hasIntegratedServer()) {
			SpellLogEntry log = new SpellLogEntry(this, caster);
			logger = new SpellLogBuilder(log);
		} else {
			logger = ISpellLogBuilder.Dummy;
		}
		SpellState state = new SpellState(this, caster, efficiency, targetHint, logger);
		state.trigger(Lists.newArrayList(caster), null);
		
		NostrumMagicaSounds.CAST_LAUNCH.play(caster);
		if (caster instanceof Player) {
			PlayerStatTracker.Update((Player) caster, (stats) -> stats.incrStat(PlayerStat.SpellsCast).addStat(PlayerStat.TotalSpellWeight, weight));
			
			if (caster instanceof ServerPlayer) {
				CastSpellCriteriaTrigger.Instance.trigger((ServerPlayer) caster);
			}
		}
	}
	
	public String crc() {
		String s = "";
		for (SpellShapePart part : shapes) {
				s += part.getShape().getShapeKey();
		}
		for (SpellEffectPart part : parts) {
			s += part.getElement().name();
			if (part.getAlteration() != null) {
				s += part.getAlteration().name();
			}
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
		
		for (SpellShapePart part : shapes) {
			ReagentType type;
			for (ItemStack req : part.getShape().getReagents()) {
				type = ReagentItem.FindType(req);
				int count = costs.get(type);
				count += req.getCount();
				costs.put(type, count);
			}
		}
		
		for (SpellEffectPart part : parts) {
			ReagentType type;
			if (part.getAlteration() != null) {
				for (ItemStack req : part.getAlteration().getReagents()) {
					type = ReagentItem.FindType(req);
					int count = costs.get(type);
					count += req.getCount();
					costs.put(type, count);
				}
			}
		}
		
		return costs;
	}
	
	// seen is if they've seen it before or not
	public float getXP(boolean seen) {
		// Shapes add some
		// More elements mean more xp
		// Alterations give some
		// 300% first time you use it
		
		float total = 0f;
		
//		for (SpellShapePart part : shapes) {
//			total += 1f;
//		}
		total += shapes.size();
		
		for (SpellEffectPart part : parts) {
			total += 1f;
			if (part.getElementCount() > 1)
				total += (float) (Math.pow(2, part.getElementCount() - 1));
			if (part.getAlteration() != null)
				total += 5f;
		}
		
		if (!seen)
			total *= 3f;
		return total;
	}
	
	private static final String NBT_SPELL_NAME = "name";
	private static final String NBT_MANA_COST = "mana_cost";
	private static final String NBT_WEIGHT = "spell_weight";
	private static final String NBT_SHAPE_LIST = "shapes";
	private static final String NBT_EFFECT_LIST = "effects";
	private static final String NBT_ICON_INDEX = "ico_index";
	
	public CompoundTag toNBT() {
		CompoundTag compound = new CompoundTag();
		compound.putString(NBT_SPELL_NAME, name);
		compound.putInt(NBT_ICON_INDEX, iconIndex);
		compound.putInt(NBT_MANA_COST, manaCost);
		compound.putInt(NBT_WEIGHT, weight);
		
		ListTag list = new ListTag();
		for (SpellShapePart part : shapes) {
			CompoundTag tag = part.toNBT(null);
			list.add(tag);
		}
		compound.put(NBT_SHAPE_LIST, list);
		
		list = new ListTag();
		for (SpellEffectPart part : parts) {
			CompoundTag tag = part.toNBT(null);
			list.add(tag);
		}
		compound.put(NBT_EFFECT_LIST, list);
		
		return compound;
	}
	
	/**
	 * Deserializes a spell from Tag.
	 * Does not register it in the registry
	 * @param nbt
	 * @param id
	 * @return
	 */
	public static Spell fromNBT(CompoundTag nbt, int id) {
		Spell spell = fromNBT(nbt); 
		spell.registryID = id;
		return spell;
	}
	
	public static Spell transientFromNBT(CompoundTag nbt) {
		Spell spell = fromNBT(nbt);
		spell.registryID = NostrumMagica.instance.getSpellRegistry().registerTransient(spell);
		return spell;
	}
	
	protected static Spell fromNBT(CompoundTag nbt) {
		if (nbt == null)
			return null;
		
		String name = nbt.getString(NBT_SPELL_NAME); 
		int index = nbt.getInt(NBT_ICON_INDEX);
		int manaCost = nbt.getInt(NBT_MANA_COST);
		int weight = nbt.getInt(NBT_WEIGHT);
		
		Spell spell = new Spell(name, manaCost, weight, false);
		spell.iconIndex = index;
		
		ListTag list = nbt.getList(NBT_SHAPE_LIST, Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag tag = list.getCompound(i);
			spell.addPart(SpellShapePart.FromNBT(tag));
		}
		
		list = nbt.getList(NBT_EFFECT_LIST, Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag tag = list.getCompound(i);
			spell.addPart(SpellEffectPart.FromNBT(tag));
		}
		
		return spell;
	}
	
	public SpellCharacteristics getCharacteristics() {
		return this.characteristics;
	}

	public EMagicElement getPrimaryElement() {
		return this.getCharacteristics().element;
	}

	public boolean isEmpty() {
		return parts.isEmpty();
	}
	
	public int getComponentCount() {
		return shapes.size() + parts.size();
	}
	
	public int getElementCount() {
		int count = 0;
		for (SpellEffectPart part : parts) {
			count += part.getElementCount();
		}
		return count;
	}
	
	public int getShapeCount() {
		return shapes.size();
	}
	
	public Map<EMagicElement, Integer> getElements() {
		Map<EMagicElement, Integer> list = new EnumMap<>(EMagicElement.class);
		for (SpellEffectPart part : parts) {
			EMagicElement element = part.getElement() == null ? EMagicElement.PHYSICAL : part.getElement();
			int count = 0;
			if (list.get(element) != null)
				count = list.get(element);
			count += part.getElementCount();
			list.put(element, count);
		}
		return list;
	}
	
	public Map<EAlteration, Integer> getAlterations() {
		Map<EAlteration, Integer> list = new EnumMap<>(EAlteration.class);
		if (!parts.isEmpty())
		for (SpellEffectPart part : parts) {
			if (part.getAlteration() != null) {
				int count = 0;
				if (list.get(part.getAlteration()) != null)
					count = list.get(part.getAlteration());
				count++;
				list.put(part.getAlteration(), count);
			}
		}
		return list;
	}
	
	public Map<SpellShape, Integer> getShapes() {
		Map<SpellShape, Integer> list = new HashMap<>();
		if (!parts.isEmpty())
		for (SpellShapePart part : shapes) {
			int count = 0;
			if (list.get(part.getShape()) != null)
				count = list.get(part.getShape());
			count++;
			list.put(part.getShape(), count);
		}
		return list;
	}

	public List<SpellEffectPart> getSpellEffectParts() {
		return this.parts;
	}
	
	public List<SpellShapePart> getSpellShapeParts() {
		return this.shapes;
	}
	
	/**
	 * Whether (the first part of this spell) wants tracing, to show indicators when an enemy is being looked at.
	 * For example, seeking bullet needs the player to be looking at an enemy to select who to go after.
	 * @return
	 */
	public boolean shouldTrace(Player player) {
		if (!getSpellShapeParts().isEmpty()) {
			SpellShapePart firstShape = getSpellShapeParts().get(0);
			return firstShape.getShape().shouldTrace(player, firstShape.getProperties());
		}
		
		return false;
	}
	
	public double getTraceRange(Player player) {
		if (!getSpellShapeParts().isEmpty()) {
			SpellShapePart firstShape = getSpellShapeParts().get(0);
			return firstShape.getShape().getTraceRange(player, firstShape.getProperties());
		}
		
		return 0;
	}
	
	public boolean supportsPreview() {
		if (!getSpellShapeParts().isEmpty()) {
			for (SpellShapePart shapePart : this.getSpellShapeParts()) {
				if (!shapePart.getShape().supportsPreview(shapePart.getProperties())) {
					return false;
				}
			}
			return true;
		}
		
		return true; // Affects caster
	}
	
	public @Nullable SpellShapePreview getPreview(LivingEntity caster, float partialTicks) {
		if (!getSpellShapeParts().isEmpty()) {
			SpellShapePreview preview = new SpellShapePreview();
			PreviewSpellState previewState = new PreviewSpellState(this, caster, preview, partialTicks);
			previewState.trigger(Lists.newArrayList(caster), null);
			return preview;
		}
		
		// Affects caster
		return new SpellShapePreview().add(new SpellShapePreviewComponent.Ent(caster));
	}
}
