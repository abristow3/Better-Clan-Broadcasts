package com.betterclanbroadcasts.broadcasts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.util.Text;

public class CaIconTracker {

    // overall lifetime ~6 hours then deleted
    private static final int CA_ICON_TTL_TICKS = 36000;
    private static final int CHATBOX_SCROLLAREA_ID = 10616890;

    private final Client client;
    private final List<PendingCaIcon> pendingCaIcons = new ArrayList<>();

    public CaIconTracker(Client client) {
        this.client = client;
    }

    // no-op if this exact broadcast text is already pending
    void enqueue(String cleanText, Color tierColor, int iconIndex) {
        boolean alreadyPending = pendingCaIcons.stream().anyMatch(p -> p.cleanText.equals(cleanText));
        if (!alreadyPending) {
            pendingCaIcons.add(new PendingCaIcon(cleanText, tierColor, iconIndex, CA_ICON_TTL_TICKS));
        }
    }

    void onGameTick() {
        Iterator<PendingCaIcon> it = pendingCaIcons.iterator();
        while (it.hasNext()) {
            PendingCaIcon icon = it.next();
            icon.ticksRemaining--;
            if (icon.ticksRemaining <= 0) {
                it.remove();
            }
        }
    }

    // called every rendered frame for responsiveness
    void processPendingCaIcons() {
        if (pendingCaIcons.isEmpty()) {
            return;
        }

        Widget scrollArea = client.getWidget(CHATBOX_SCROLLAREA_ID);
        Widget[] children = scrollArea == null ? null : scrollArea.getDynamicChildren();
        if (children == null) {
            children = new Widget[0];
        }

        for (PendingCaIcon icon : pendingCaIcons) {
            Widget match = findMatchingLine(children, icon.cleanText);
            if (match == null) {
                continue;
            }

            String currentText = match.getText();
            String iconTag = "<img=" + icon.iconIndex + ">";
            if (currentText == null || !currentText.contains(iconTag)) {
                // build from whatever's currently on the line (may already
                // carry another plugin's formatting, e.g. Chat Notifications
                // highlighting the player's own name) rather than a version
                // captured early, before that formatting was applied
                String base = currentText == null ? "" : currentText;
                String body = icon.tierColor != null ? BroadcastColorizer.colorize(base, icon.tierColor) : base;
                match.setText(iconTag + " " + body);
                match.revalidate();
            }
        }
    }

    private Widget findMatchingLine(Widget[] children, String cleanText) {
        for (Widget child : children) {
            if (child == null || child.isHidden() || child.getText() == null || child.getText().isEmpty()) {
                continue;
            }

            if (Text.removeTags(child.getText()).contains(cleanText)) {
                return child;
            }
        }

        return null;
    }

    // a CA broadcast waiting for its icon, matched against widget text each tick.
    // tierColor is null unless the detected tier has coloring enabled
    private static class PendingCaIcon {
        final String cleanText;
        final Color tierColor;
        final int iconIndex;
        int ticksRemaining;

        PendingCaIcon(String cleanText, Color tierColor, int iconIndex, int ticksRemaining) {
            this.cleanText = cleanText;
            this.tierColor = tierColor;
            this.iconIndex = iconIndex;
            this.ticksRemaining = ticksRemaining;
        }
    }
}