package com.betterclanbroadcasts.broadcasts;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.betterclanbroadcasts.BetterClanBroadcastsConfig;
import net.runelite.client.util.ColorUtil;

public class BroadcastColorizer {

    // confirmed against a real broadcast: "X has completed an elite combat task: Y."
    private static final Pattern CA_TIER_PATTERN =
            Pattern.compile(
                    "has completed (?:a|an) (easy|medium|hard|elite|master|grandmaster) combat task:"
                            + "|has unlocked the (easy|medium|hard|elite|master|grandmaster) tier of rewards from Combat Achievements",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern DIARY_PATTERN =
            Pattern.compile("has completed the (.*) diary\\.", Pattern.CASE_INSENSITIVE);
    private static final Pattern TIER_KEYWORD_PATTERN =
            Pattern.compile("\\b(easy|medium|hard|elite)\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern QUEST_PATTERN =
            Pattern.compile("has completed a quest:", Pattern.CASE_INSENSITIVE);

    enum CaTier {
        EASY, MEDIUM, HARD, ELITE, MASTER, GRANDMASTER
    }

    enum DiaryTier {
        EASY, MEDIUM, HARD, ELITE
    }

    private final BetterClanBroadcastsConfig config;

    public BroadcastColorizer(BetterClanBroadcastsConfig config) {
        this.config = config;
    }

    // null if this isnt a recognized CA broadcast, or the tier keyword didnt match
    CaTier detectCaTier(String cleanText) {
        Matcher matcher = CA_TIER_PATTERN.matcher(cleanText);
        if (!matcher.find()) {
            return null;
        }

        // group 1 = task completion phrasing, group 2 = tier unlock phrasing -
        // only one is ever non-null for a given match
        String tierName = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        if (tierName == null) {
            return null;
        }

        try {
            return CaTier.valueOf(tierName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // null if this isnt a recognized diary broadcast, or no tier keyword found within it
    DiaryTier detectDiaryTier(String cleanText) {
        Matcher matcher = DIARY_PATTERN.matcher(cleanText);
        if (!matcher.find()) {
            return null;
        }

        Matcher tierMatcher = TIER_KEYWORD_PATTERN.matcher(matcher.group(1));
        if (!tierMatcher.find()) {
            return null;
        }

        try {
            return DiaryTier.valueOf(tierMatcher.group(1).toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    boolean isQuestCompletion(String cleanText) {
        return QUEST_PATTERN.matcher(cleanText).find();
    }

    // returns the configured color for this tier if the user has enabled it
    Color colorForCa(CaTier tier) {
        if (tier == null || !isCaEnabled(tier)) {
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

    Color colorForDiary(DiaryTier tier) {
        if (tier == null || !isDiaryEnabled(tier)) {
            return null;
        }

        switch (tier) {
            case EASY:
                return config.diaryEasyColor();
            case MEDIUM:
                return config.diaryMediumColor();
            case HARD:
                return config.diaryHardColor();
            case ELITE:
                return config.diaryEliteColor();
            default:
                return null;
        }
    }

    Color colorForQuest() {
        return config.questCompletionEnabled() ? config.questCompletionColor() : null;
    }

    static String colorize(String text, Color color) {
        String probe = ColorUtil.wrapWithColorTag("X", color);
        String openTag = probe.substring(0, probe.indexOf('X'));

        String recolored = text
                .replaceAll("(?i)<col=000000>", openTag)
                .replaceAll("(?i)<colNORMAL>", openTag);
        if (recolored.equals(text)) {
            // no default tags found, e.g. a plain unformatted line - wrap directly
            return openTag + text + "</col>";
        }
        return recolored;
    }

    private boolean isCaEnabled(CaTier tier) {
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

    private boolean isDiaryEnabled(DiaryTier tier) {
        switch (tier) {
            case EASY:
                return config.diaryEasyEnabled();
            case MEDIUM:
                return config.diaryMediumEnabled();
            case HARD:
                return config.diaryHardEnabled();
            case ELITE:
                return config.diaryEliteEnabled();
            default:
                return false;
        }
    }
}