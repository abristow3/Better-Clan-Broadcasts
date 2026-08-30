package com.betterclanbroadcasts;

import java.awt.Color;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(BetterClanBroadcastsConfig.CONFIG_GROUP)
public interface BetterClanBroadcastsConfig extends Config {
    String CONFIG_GROUP = "betterclanbroadcasts";

    @ConfigSection(name = "General Settings", description = "General plugin settings", position = 0)
    String GENERAL_SECTION = "generalSection";

    @ConfigItem(keyName = "enabled", name = "Broadcast Ranks", description = "Prepend clan rank icons to clan broadcast messages", section = GENERAL_SECTION, position = 0)
    default boolean enabled() {
        return true;
    }

    @ConfigItem(keyName = "showNoteTooltip", name = "Show note tooltip", description = "Show a tooltip with your note when hovering a clan member who has one", section = GENERAL_SECTION, position = 1)
    default boolean showNoteTooltip() {
        return true;
    }

    @ConfigItem(keyName = "showIcons", name = "Show note icons", description = "Show an icon in the clan member list next to members who have a note", section = GENERAL_SECTION, position = 2)
    default boolean showIcons() {
        return true;
    }

    @ConfigSection(name = "Combat Achievement Colors", description = "Color combat achievement broadcasts by tier", position = 100)
    String CA_COLOR_SECTION = "caColorSection";

    @ConfigItem(keyName = "caEasyEnabled", name = "Easy", description = "Color Easy combat achievement broadcasts", section = CA_COLOR_SECTION, position = 101)
    default boolean caEasyEnabled() {
        return false;
    }

    @ConfigItem(keyName = "caEasyColor", name = "Easy color", description = "Color used for Easy combat achievement broadcasts", section = CA_COLOR_SECTION, position = 102)
    default Color caEasyColor() {
        return new Color(0x066B3F);
    }

    @ConfigItem(keyName = "caMediumEnabled", name = "Medium", description = "Color Medium combat achievement broadcasts", section = CA_COLOR_SECTION, position = 103)
    default boolean caMediumEnabled() {
        return false;
    }

    @ConfigItem(keyName = "caMediumColor", name = "Medium color", description = "Color used for Medium combat achievement broadcasts", section = CA_COLOR_SECTION, position = 104)
    default Color caMediumColor() {
        return new Color(0x7C7212);
    }

    @ConfigItem(keyName = "caHardEnabled", name = "Hard", description = "Color Hard combat achievement broadcasts", section = CA_COLOR_SECTION, position = 105)
    default boolean caHardEnabled() {
        return false;
    }

    @ConfigItem(keyName = "caHardColor", name = "Hard color", description = "Color used for Hard combat achievement broadcasts", section = CA_COLOR_SECTION, position = 106)
    default Color caHardColor() {
        return new Color(0x99560C);
    }

    @ConfigItem(keyName = "caEliteEnabled", name = "Elite", description = "Color Elite combat achievement broadcasts", section = CA_COLOR_SECTION, position = 107)
    default boolean caEliteEnabled() {
        return false;
    }

    @ConfigItem(keyName = "caEliteColor", name = "Elite color", description = "Color used for Elite combat achievement broadcasts", section = CA_COLOR_SECTION, position = 108)
    default Color caEliteColor() {
        return new Color(0x0F739B);
    }

    @ConfigItem(keyName = "caMasterEnabled", name = "Master", description = "Color Master combat achievement broadcasts", section = CA_COLOR_SECTION, position = 109)
    default boolean caMasterEnabled() {
        return false;
    }

    @ConfigItem(keyName = "caMasterColor", name = "Master color", description = "Color used for Master combat achievement broadcasts", section = CA_COLOR_SECTION, position = 110)
    default Color caMasterColor() {
        return new Color(0x8B0C0C);
    }

    @ConfigItem(keyName = "caGrandmasterEnabled", name = "Grandmaster", description = "Color Grandmaster combat achievement broadcasts", section = CA_COLOR_SECTION, position = 111)
    default boolean caGrandmasterEnabled() {
        return false;
    }

    @ConfigItem(keyName = "caGrandmasterColor", name = "Grandmaster color", description = "Color used for Grandmaster combat achievement broadcasts", section = CA_COLOR_SECTION, position = 112)
    default Color caGrandmasterColor() {
        return new Color(0x790A62);
    }

    @ConfigSection(name = "Quest & Diary Colors", description = "Color quest completion and achievement diary broadcasts", position = 200)
    String QUEST_DIARY_COLOR_SECTION = "questDiaryColorSection";

    @ConfigItem(keyName = "diaryEasyEnabled", name = "Easy Diary", description = "Color Easy achievement diary broadcasts", section = QUEST_DIARY_COLOR_SECTION, position = 201)
    default boolean diaryEasyEnabled() {
        return false;
    }

    @ConfigItem(keyName = "diaryEasyColor", name = "Easy color", description = "Color used for Easy achievement diary broadcasts", section = QUEST_DIARY_COLOR_SECTION, position = 202)
    default Color diaryEasyColor() {
        return new Color(0x066B3F);
    }

    @ConfigItem(keyName = "diaryMediumEnabled", name = "Medium Diary", description = "Color Medium achievement diary broadcasts", section = QUEST_DIARY_COLOR_SECTION, position = 203)
    default boolean diaryMediumEnabled() {
        return false;
    }

    @ConfigItem(keyName = "diaryMediumColor", name = "Medium color", description = "Color used for Medium achievement diary broadcasts", section = QUEST_DIARY_COLOR_SECTION, position = 204)
    default Color diaryMediumColor() {
        return new Color(0x7C7212);
    }

    @ConfigItem(keyName = "diaryHardEnabled", name = "Hard Diary", description = "Color Hard achievement diary broadcasts", section = QUEST_DIARY_COLOR_SECTION, position = 205)
    default boolean diaryHardEnabled() {
        return false;
    }

    @ConfigItem(keyName = "diaryHardColor", name = "Hard color", description = "Color used for Hard achievement diary broadcasts", section = QUEST_DIARY_COLOR_SECTION, position = 206)
    default Color diaryHardColor() {
        return new Color(0x99560C);
    }

    @ConfigItem(keyName = "diaryEliteEnabled", name = "Elite Diary", description = "Color Elite achievement diary broadcasts", section = QUEST_DIARY_COLOR_SECTION, position = 207)
    default boolean diaryEliteEnabled() {
        return false;
    }

    @ConfigItem(keyName = "diaryEliteColor", name = "Elite color", description = "Color used for Elite achievement diary broadcasts", section = QUEST_DIARY_COLOR_SECTION, position = 208)
    default Color diaryEliteColor() {
        return new Color(0x0F739B);
    }

    @ConfigItem(keyName = "questCompletionEnabled", name = "Quest completion", description = "Color quest completion broadcasts", section = QUEST_DIARY_COLOR_SECTION, position = 209)
    default boolean questCompletionEnabled() {
        return false;
    }

    @ConfigItem(keyName = "questCompletionColor", name = "Quest completion color", description = "Color used for quest completion broadcasts", section = QUEST_DIARY_COLOR_SECTION, position = 210)
    default Color questCompletionColor() {
        return Color.WHITE;
    }
}