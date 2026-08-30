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
}