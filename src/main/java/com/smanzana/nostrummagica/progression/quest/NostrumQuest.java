package com.smanzana.nostrummagica.progression.quest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.progression.requirement.IRequirement;
import com.smanzana.nostrummagica.progression.reward.IReward;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

/**
 * Base class for Nostrum quests.
 * These are objectives/quests that are presented in the quest tab of the mirror.
 * They have parents.
 * They have rewards.
 * <p>
 * The constructor in this base class performs registration.
 * </p>
 * @author Skyler
 *
 */
public class NostrumQuest {

	public static enum QuestType {
		CHALLENGE,
		REGULAR,
	}
	
	/**
	 * Unique key for this quest. Used for parent lookups and translations
	 */
	protected final String key;
	
	/**
	 * Type of quest this is. Challenge quests have objectives that must
	 * be completed before rewards are given
	 */
	protected final QuestType type;
	
	/**
	 * List of parent quests. This is a sanitized array and expects each to exist.
	 * Be careful modifying it...
	 */
	protected String[] parentKeys;
	
	/**
	 * Reward distributed after the quest is completed.
	 * Rewards are only received once.
	 */
	protected final IReward reward;

	/**
	 * Requirements to be able to complete this quest.
	 */
	protected /*final*/ IRequirement[] requirements;
	
	/**
	 * Graphical offsets from the center of the quest screen
	 */
	protected final int offsetX;
	protected final int offsetY;

	/**
	 * Creates a new quest.
	 * <p>
	 * This is more-or-less a template. Make one instance per quest.
	 * </p>
	 * <p>
	 * Quests are registered as part of this constructor.
	 * </p>
	 * @param key unique key to identify the quest.
	 * @param type quest type used for display
	 * @param reqLevel level requirement
	 * @param reqControl control requirement
	 * @param reqTechnique tech requirement
	 * @param reqFinesse finesse requirement
	 * @param parentKeys all potential parents. This list is later validated.
	 * <strong>You are free to add all potential dependencies!</strong> During
	 * validation, any dependencies that weren't registered are simply ignored.
	 * @param loreKeys required lore key names that must be known before this quest can be taken.
	 * @param objective What extra challenge objective must be done to finish and
	 * get rewards.
	 * @param rewards given when the quest is completed.
	 */
	public NostrumQuest(
			String key,
			QuestType type,
			String[] parentKeys,
			int x,
			int y,
			IReward reward,
			IRequirement ... requirements
			) {
		this.key = key;
		this.type = type;
		this.parentKeys = parentKeys;
		this.reward = reward;
		this.requirements = requirements;
		
		this.offsetX = x;
		this.offsetY = y;
		
		NostrumQuest.register(this);
	}
	
	public NostrumQuest(
			String key,
			String[] parentKeys,
			int x,
			int y,
			IReward reward,
			IRequirement ... requirements
			) {
		this(key, QuestType.REGULAR, parentKeys, x, y, reward, requirements);
	}
	
	public NostrumQuest(
			String key,
			String parentKey,
			int x,
			int y,
			IReward reward,
			IRequirement ... requirements
			) {
		this(key, QuestType.REGULAR, new String[] {parentKey}, x, y, reward, requirements);
	}
	
	public NostrumQuest(
			String key,
			int x,
			int y,
			IReward reward,
			IRequirement ... requirements
			) {
		this(key, QuestType.REGULAR, (String[]) null, x, y, reward, requirements);
	}
	
	public String getKey() {
		return key;
	}

	public QuestType getType() {
		return type;
	}

	public String[] getParentKeys() {
		return parentKeys;
	}
	
	public IRequirement[] getRequirements() {
		return requirements;
	}

	public IReward getReward() {
		return reward;
	}

	public int getPlotX() {
		return this.offsetX;
	}
	
	public int getPlotY() {
		return this.offsetY;
	}
	
	public void grantReward(Player player) {
		reward.award(player);
	}
	
	/**
	 * Marks this quest as complete for the player.
	 * This distributes reward and updates the player's quest state.
	 * @param player
	 */
	public void completeQuest(Player player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		attr.completeQuest(getKey());
		grantReward(player);
		
		// TODO if there's some sort of listener, it should be cleaned up here
		
		if (!player.level.isClientSide)
			NostrumMagicaSounds.SUCCESS_QUEST.play(player.level, player.getX(), player.getY(), player.getZ());
		else
			NostrumMagica.Proxy.syncPlayer((ServerPlayer) player);
	}
	
	public void startQuest(Player player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		if (attr.getCurrentQuests().contains(getKey()))
			return;
		
		// Remnant of old system where some quests had middle parts
		attr.addQuest(getKey());
		this.completeQuest(player);
	}

	private static Map<String, NostrumQuest> Registry = new HashMap<>();
	
	private static void register(NostrumQuest quest) {
		if (Registry.containsKey(quest.key)) {
			NostrumMagica.logger.error("Duplicate quest registration for key " + quest.key);
			return;
		}
		
		Registry.put(quest.key, quest);
	}
	
	public static NostrumQuest lookup(String key) {
		return Registry.get(key);
	}
	
	public static Collection<NostrumQuest> allQuests() {
		return Registry.values();
	}
	
	/**
	 * Iterate over all registered quests.
	 * Perform parent checks. Fix up dependencies.
	 */
	public static void Validate() {
		int count = 0;
		for (NostrumQuest quest : Registry.values()) {
			if (quest.parentKeys == null || quest.parentKeys.length == 0) {
				quest.parentKeys = null;
				continue;
			}
			count++;
				
			List<String> outList = new ArrayList<>();
			for (String dep : quest.parentKeys) {
				if (Registry.containsKey(dep))
					outList.add(dep);
			}
			
			if (outList.isEmpty())
				quest.parentKeys = null;
			else
				quest.parentKeys = outList.toArray(new String[0]);
		}
		
		if (count != 0)
			NostrumMagica.logger.info("Validated " + count + " quest parent dependencies");
		
		count = 0;
		for (NostrumQuest quest : Registry.values()) {
			if (quest.requirements == null || quest.requirements.length == 0) {
				continue;
			}
			
			count++;
			
			List<IRequirement> outList = new ArrayList<>();
			for (IRequirement req : quest.getRequirements()) {
				if (req.isValid()) {
					outList.add(req);
				}
			}
			
			if (outList.isEmpty())
				quest.requirements = null;
			else
				quest.requirements = outList.toArray(new IRequirement[0]);
		}
		
		if (count != 0)
			NostrumMagica.logger.info("Validated " + count + " quest requirements");
	}
	
	public static void ClearAllQuests() {
		Registry.clear();
	}
	
}
