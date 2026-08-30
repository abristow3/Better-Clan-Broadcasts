package com.betterclanbroadcasts.broadcasts;

import java.awt.Dimension;
import java.awt.Graphics2D;

import com.betterclanbroadcasts.BetterClanBroadcastsConfig;
import net.runelite.client.ui.overlay.Overlay;

public class ClanCaIconMaintainer extends Overlay
{
    private final BetterClanBroadcastsConfig config;
    private final CaIconTracker caIconTracker;

    public ClanCaIconMaintainer(BetterClanBroadcastsConfig config, CaIconTracker caIconTracker)
    {
        this.config = config;
        this.caIconTracker = caIconTracker;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (config.enabled())
        {
            caIconTracker.processPendingCaIcons();
        }
        return null;
    }
}