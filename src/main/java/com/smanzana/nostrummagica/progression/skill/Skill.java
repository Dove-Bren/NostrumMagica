package com.smanzana.nostrummagica.progression.skill;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Base class for Nostrum skills.
 * Skills are a 'have them or not' thing. Whether you have a skill is determined by looking for it
 * in your magic capability.
 * They have parents.
 * <p>
 * The constructor in this base class performs registration.
 * </p>
 * @author Skyler
 *
 */
public class Skill {

	/**
	 * Unique key for this skill. Used for parent lookups and translations
	 */
	protected final ResourceLocation key;
	
	/**
	 * Category
	 */
	protected final SkillCategory category;
	
	/**
	 * ID of the parent skill to this skill.
	 */
	protected @Nullable ResourceLocation parent;
	
	/**
	 * Icon to use in the GUI
	 */
	protected final Supplier<ItemStack> icon;
	private ItemStack iconCached;
	
	/**
	 * Graphical offsets from the center of the skill screen
	 */
	protected final int offsetX;
	protected final int offsetY;
	
	private final ITextComponent name;

	/**
	 * Creates a new skill.
	 * Skills are registered as part of this constructor.
	 */
	public Skill(
			ResourceLocation key,
			SkillCategory category,
			@Nullable ResourceLocation parent,
			Supplier<ItemStack> icon,
			int x,
			int y
			) {
		this.key = key;
		this.category = category;
		this.parent = parent;
		
		this.icon = icon;
		this.offsetX = x;
		this.offsetY = y;
		
		name = new TranslationTextComponent("skill." + key.getNamespace() + "." + key.getPath());
		
		Skill.register(this);
	}
	
	public ResourceLocation getKey() {
		return key;
	}

	public SkillCategory getCategory() {
		return category;
	}
	
	public ITextComponent getName() {
		return name;
	}
	
	public List<ITextComponent> getDescription() {
		return TextUtils.GetTranslatedList("skill." + key.getNamespace() + "." + key.getPath() + ".desc");
	}
	
	public ItemStack getIcon() {
		if (iconCached == null) {
			iconCached = this.icon.get();
		}
		return iconCached;
	}

	public @Nullable ResourceLocation getParentKey() {
		return parent;
	}
	
	public int getPlotX() {
		return this.offsetX;
	}
	
	public int getPlotY() {
		return this.offsetY;
	}
	
	public void addToPlayer(PlayerEntity player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		attr.addSkill(this);
		
		if (!player.level.isClientSide)
			NostrumMagicaSounds.SUCCESS_QUEST.play(player.level, player.getX(), player.getY(), player.getZ());
		else
			NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) player);
	}
	
	/**
	 * Whether this skill is hidden. If so, it will not show up
	 * for viewing or purchase even if the player meets any other
	 * requirements (like parent being purchased, enough skillpoints, etc.)
	 * @param player
	 * @return
	 */
	public boolean isHidden(PlayerEntity player) {
		return false;
	}
	
	public boolean meetsRequirements(PlayerEntity player) {
		// Only thing to check here is that player has any parents already
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (this.parent != null) {
			@Nullable Skill parentSkill = lookup(parent);
			if (parentSkill != null
					&& !attr.hasSkill(parentSkill)) {
				return false;
			}
		}
		
		return true;
	}
	
	protected void validate() {
		; // Nothing to check
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null && o instanceof Skill && ((Skill) o).key.equals(key);
	}
	
	@Override
	public int hashCode() {
		return key.hashCode() * 7907;
	}
	
	
	private static Map<ResourceLocation, Skill> Registry = new HashMap<>();
	
	private static void register(Skill skill) {
		if (Registry.containsKey(skill.key)) {
			NostrumMagica.logger.error("Duplicate skill registration for key " + skill.key);
			return;
		}
		
		Registry.put(skill.key, skill);
	}
	
	public static Skill lookup(ResourceLocation key) {
		return Registry.get(key);
	}
	
	public static Collection<Skill> allSkills() {
		return Registry.values();
	}
	
	public static void ClearSkills() {
		Registry.clear();
	}
	
	/**
	 * Iterate over all registered skills.
	 * Perform parent checks. Fix up dependencies.
	 */
	public static void Validate() {
		int count = 0;
		
		for (Skill skill : allSkills()) {
			count++;
			skill.validate();
		}
		
		if (count != 0)
			NostrumMagica.logger.info("Validated " + count + " skills");
	}
}
