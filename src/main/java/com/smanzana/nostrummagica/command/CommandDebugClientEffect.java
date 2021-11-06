package com.smanzana.nostrummagica.command;

import net.minecraft.command.ICommandSender;

public class CommandDebugClientEffect extends CommandDebugEffect {

	@Override
	public String getName() {
		return "nostrumdebugeffectclient";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/nostrumdebugeffectclient [effect]";
	}

}
