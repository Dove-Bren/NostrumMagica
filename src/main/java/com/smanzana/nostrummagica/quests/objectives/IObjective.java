package com.smanzana.nostrummagica.quests.objectives;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.quests.NostrumQuest;

public interface IObjective {
	
	public void setParentQuest(NostrumQuest quest);
	
	public IObjectiveState getBaseState();
	
	public String getDescription();
	
	public boolean isComplete(INostrumMagic attr);
}
