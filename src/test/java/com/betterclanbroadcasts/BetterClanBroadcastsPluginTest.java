package com.betterclanbroadcasts;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BetterClanBroadcastsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BetterClanBroadcastsPlugin.class);
		RuneLite.main(args);
	}
}