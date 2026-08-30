package com.betterclanbroadcasts.broadcasts;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.betterclanbroadcasts.BetterClanBroadcastsConfig;
import net.runelite.client.util.ColorUtil;

// detects which CA tier a broadcast belongs to from its text, and applies
// the client configured color for that tier if theyve enabled it
public class BroadcastColorizer {

    // confirmed against a real broadcast: "X has completed an elite combat task: Y."
    private static final Pattern CA_TIER_PATTERN =
            Pattern.compile("has completed (?:a|an) (easy|medium|hard|elite|master|grandmaster) combat task:",
                    Pattern.CASE_INSENSITIVE);

    enum CaTier {
        EASY, MEDIUM, HARD, ELITE, MASTER, GRANDMASTER
    }

    private final BetterClanBroadcastsConfig config;

    public BroadcastColorizer(BetterClanBroadcastsConfig config) {
        this.config = config;
    }

    // null if this isnt a recognized CA broadcast, or the tier keyword didnt match
    CaTier detectTier(String cleanText) {
        Matcher matcher = CA_TIER_PATTERN.matcher(cleanText);
        if (!matcher.find()) {
            return null;
        }

        try {
            return CaTier.valueOf(matcher.group(1).toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // returns the configured color for this tier if the user has enabled it
    Color colorFor(CaTier tier) {
        if (tier == null || !isEnabled(tier)) {
            return null;
        }

        switch (tier) {
            case EASY:
                return config.caEasyColor();
            case MEDIUM:
                return config.caMediumColor();
            case HARD:
                return config.caHardColor();
            case ELITE:
                return config.caEliteColor();
            case MASTER:
                return config.caMasterColor();
            case GRANDMASTER:
                return config.caGrandmasterColor();
            default:
                return null;
        }
    }

    static String colorize(String text, Color color) {
        String probe = ColorUtil.wrapWithColorTag("X", color);
        String openTag = probe.substring(0, probe.indexOf('X'));

        String recolored = text.replaceAll("(?i)<col=000000>", openTag);
        if (recolored.equals(text)) {
            // no default-black tags found, e.g. a plain unformatted line - wrap directly
            return openTag + text + "</col>";
        }
        return recolored;
    }

    private boolean isEnabled(CaTier tier) {
        switch (tier) {
            case EASY:
                return config.caEasyEnabled();
            case MEDIUM:
                return config.caMediumEnabled();
            case HARD:
                return config.caHardEnabled();
            case ELITE:
                return config.caEliteEnabled();
            case MASTER:
                return config.caMasterEnabled();
            case GRANDMASTER:
                return config.caGrandmasterEnabled();
            default:
                return false;
        }
    }
}