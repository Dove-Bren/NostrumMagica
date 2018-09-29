package com.smanzana.nostrummagica.quests;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.quests.objectives.IObjective;
import com.smanzana.nostrummagica.quests.rewards.IReward;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Base class for Nostrum quests.
 * These are objectives/quests that are presented in the mirror.
 * They have parents.
 * They have rewards.
 * If they are a challenge type, they have the challenge specifications
 * <p>
 * Quests should be <emphasis>instantiated</emphasis> during the init phase.
 * The constructor in this abstract class performs registration. Registration is
 * validated during post-init. Registering quests in post-init or afterwards
 * produced undefined behavior.
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
	protected String key;
	
	/**
	 * Type of quest this is. Challenge quests have objectives that must
	 * be completed before rewards are given
	 */
	protected QuestType type;

	/**
	 * Requires attributes to unlock this quest.
	 * Also used to calculate the location of the quest on the mirror screen.
	 */
	protected int reqLevel;
	protected int reqControl;
	protected int reqTechnique;
	protected int reqFinesse;
	
	/**
	 * List of parent quests. This is a sanitized array and expects each to exist.
	 * Be careful modifying it...
	 */
	protected String[] parentKeys;
	
	/**
	 * List of lore keys that are required before this quest can be seen/taken
	 */
	protected String[] loreKeys;
	
	/**
	 * Challenge objective. Not used if this is not a challenge type.
	 */
	protected IObjective objective;
	
	/**
	 * Rewards distributes after the quest is completed.
	 * Rewards are only received once.
	 */
	protected IReward[] rewards;
	
	/**
	 * Optional graphical offsets
	 */
	protected int offsetX;
	protected int offsetY;

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
	 * @param objective What extra challenge objective must be done to finish and
	 * get rewards.
	 * @param rewards given when the quest is completed.
	 */
	public NostrumQuest(
			String key,
			QuestType type,
			int reqLevel,
			int reqControl,
			int reqTechnique,
			int reqFinesse,
			String[] parentKeys,
			String[] loreKeys,
			IObjective objective,
			IReward[] rewards
			) {
		super();
		this.key = key;
		this.type = type;
		this.reqLevel = reqLevel;
		this.reqControl = reqControl;
		this.reqTechnique = reqTechnique;
		this.reqFinesse = reqFinesse;
		this.parentKeys = parentKeys;
		this.loreKeys = loreKeys;
		this.objective = objective;
		this.rewards = rewards;
		
		if (this.objective != null)
			this.objective.setParentQuest(this);
		
		this.offsetX = 0;
		this.offsetY = 0;
		
		NostrumQuest.register(this);
	}
	
	public NostrumQuest offset(int x, int y) {
		this.offsetX = x;
		this.offsetY = y;
		return this;
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
	
	public String[] getLoreKeys() {
		return loreKeys;
	}

	public IObjective getObjective() {
		return objective;
	}

	public IReward[] getRewards() {
		return rewards;
	}

	public int getReqLevel() {
		return reqLevel;
	}

	public int getReqControl() {
		return reqControl;
	}

	public int getReqTechnique() {
		return reqTechnique;
	}

	public int getReqFinesse() {
		return reqFinesse;
	}
	
	public int getPlotX() {
		return this.offsetX + (reqControl - reqFinesse);
	}
	
	public int getPlotY() {
		return this.offsetY + (-(reqLevel != 0 ? reqLevel-1 : 0) + reqTechnique);
	}
	
	/**
	 * Marks this quest as complete for the player.
	 * This distributes reward and updates the player's quest state.
	 * @param player
	 */
	public void completeQuest(EntityPlayer player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		attr.completeQuest(getKey());
		for (IReward reward : this.rewards) {
			reward.award(player);
		}
		
		// TODO if there's some sort of listener, it should be cleaned up here
		
		if (!player.worldObj.isRemote)
			NostrumMagicaSounds.AMBIENT_WOOSH.play(player.worldObj, player.posX, player.posY, player.posZ);
		else
			NostrumMagica.proxy.syncPlayer((EntityPlayerMP) player);
	}
	
	public void startQuest(EntityPlayer player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		if (attr.getCurrentQuests().contains(getKey()))
			return;
		
		attr.addQuest(getKey());
		
		// If we have an objective, write in object data on the attribute
		// Otherwise, just complete the quest
		if (this.objective != null) {
			
		} else {
			this.completeQuest(player);
		}
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
				
			List<String> outList = new LinkedList<>();
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
			NostrumMagica.logger.info("Validated " + count + " quest dependencies");
	}
	
}
