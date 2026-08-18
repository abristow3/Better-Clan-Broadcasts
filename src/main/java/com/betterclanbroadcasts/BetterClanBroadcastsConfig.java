package com.betterclanbroadcasts;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("betterclanbroadcasts")
public interface BetterClanBroadcastsConfig extends Config
{
	@ConfigItem(
			keyName = "enabled",
			name = "Enabled",
			description = "Prepend clan rank icons to clan broadcast messages"
	)
	default boolean enabled()
	{
		return true;
	}
}