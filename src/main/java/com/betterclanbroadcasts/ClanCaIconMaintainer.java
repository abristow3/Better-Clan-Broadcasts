package com.betterclanbroadcasts;

import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.client.ui.overlay.Overlay;

class ClanCaIconMaintainer extends Overlay
{
	private final BetterClanBroadcastsConfig config;
	private final ClanRankPrefixer clanRankPrefixer;

	ClanCaIconMaintainer(BetterClanBroadcastsConfig config, ClanRankPrefixer clanRankPrefixer)
	{
		this.config = config;
		this.clanRankPrefixer = clanRankPrefixer;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.enabled())
		{
			clanRankPrefixer.processPendingCaIcons();
		}
		return null;
	}
}