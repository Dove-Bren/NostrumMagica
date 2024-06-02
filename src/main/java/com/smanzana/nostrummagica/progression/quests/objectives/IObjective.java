package com.smanzana.nostrummagica.progression.quests.objectives;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.progression.quests.NostrumQuest;

public interface IObjective {
	
	public void setParentQuest(NostrumQuest quest);
	
	public IObjectiveState getBaseState();
	
	public String getDescription();
	
	public boolean isComplete(INostrumMagic attr);
}
