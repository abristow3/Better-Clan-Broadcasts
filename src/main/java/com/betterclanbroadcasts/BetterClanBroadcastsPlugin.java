package com.betterclanbroadcasts;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "Clan QOL",
		description = "Clan chat interface enhancements",
		tags = {"clan", "broadcast", "rank", "icon", "chat", "clanchat", "filtering", "filter"}
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
	private ClanPlayerListSorter clanPlayerListSorter;

	private static final int SORT_BUTTON_Y = 33;
	private static final int WORLD_SORT_BUTTON_X = 140;
	private static final int NAME_SORT_BUTTON_X = 45;
	private static final int RANK_SORT_BUTTON_X = 13;

	private ClanSortToggleButton worldSortButton;
	private ClanSortToggleButton nameSortButton;
	private ClanSortToggleButton rankSortButton;

	@Override
	protected void startUp() throws Exception
	{
		clanRankPrefixer = new ClanRankPrefixer(client, clientThread, chatIconManager);
		clanPlayerListSorter = new ClanPlayerListSorter(client);

		worldSortButton = new ClanSortToggleButton(client, clientThread,
				WORLD_SORT_BUTTON_X, SORT_BUTTON_Y,
				"Sort ascending", "Sort descending",
				() -> clanPlayerListSorter.getSortMode() == ClanPlayerListSorter.SortMode.WORLD_ASCENDING,
				clanPlayerListSorter::setAscending,
				clanPlayerListSorter::setDescending);
		worldSortButton.startUp();

		nameSortButton = new ClanSortToggleButton(client, clientThread,
				NAME_SORT_BUTTON_X, SORT_BUTTON_Y,
				"Sort A-Z", "Sort Z-A",
				() -> clanPlayerListSorter.getSortMode() == ClanPlayerListSorter.SortMode.NAME_ASCENDING,
				clanPlayerListSorter::setNameAscending,
				clanPlayerListSorter::setNameDescending);
		nameSortButton.startUp();

		rankSortButton = new ClanSortToggleButton(client, clientThread,
				RANK_SORT_BUTTON_X, SORT_BUTTON_Y,
				"Sort rank ascending", "Sort rank descending",
				() -> clanPlayerListSorter.getSortMode() == ClanPlayerListSorter.SortMode.RANK_THEN_SPRITE_ASCENDING,
				clanPlayerListSorter::setRankThenSpriteAscending,
				clanPlayerListSorter::setRankThenSpriteDescending);
		rankSortButton.startUp();
	}

	@Override
	protected void shutDown() throws Exception
	{
		clanRankPrefixer = null;

		clanPlayerListSorter.reset();
		clanPlayerListSorter = null;

		worldSortButton.reset();
		worldSortButton = null;

		nameSortButton.reset();
		nameSortButton = null;

		rankSortButton.reset();
		rankSortButton = null;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!config.enabled())
		{
			return;
		}
		clanRankPrefixer.onGameTick(event);
		clanPlayerListSorter.onGameTick();
		worldSortButton.onGameTick();
		nameSortButton.onGameTick();
		rankSortButton.onGameTick();
	}

	@Provides
	BetterClanBroadcastsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BetterClanBroadcastsConfig.class);
	}
}
