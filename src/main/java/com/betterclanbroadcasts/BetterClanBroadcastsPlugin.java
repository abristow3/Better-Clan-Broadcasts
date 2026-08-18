package com.betterclanbroadcasts;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "Better Clan Broadcasts",
		description = "Prepends clan rank icons to clan broadcast messages",
		tags = {"clan", "broadcast", "rank", "icon", "chat", "clanchat"}
)
public class BetterClanBroadcastsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BetterClanBroadcastsConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatIconManager chatIconManager;

	private ClanRankPrefixer clanRankPrefixer;

	@Override
	protected void startUp() throws Exception
	{
		clanRankPrefixer = new ClanRankPrefixer(client, clientThread, chatIconManager);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clanRankPrefixer = null;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!config.enabled())
		{
			return;
		}

		clanRankPrefixer.onChatMessage(event);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!config.enabled())
		{
			return;
		}

		clanRankPrefixer.onGameTick(event);
	}

	@Provides
	BetterClanBroadcastsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BetterClanBroadcastsConfig.class);
	}
}