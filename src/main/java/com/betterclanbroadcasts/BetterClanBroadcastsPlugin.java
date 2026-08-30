package com.betterclanbroadcasts;

import com.betterclanbroadcasts.broadcasts.BroadcastColorizer;
import com.betterclanbroadcasts.broadcasts.CaIconTracker;
import com.betterclanbroadcasts.broadcasts.ClanBroadcastHandler;
import com.betterclanbroadcasts.broadcasts.ClanCaIconMaintainer;
import com.betterclanbroadcasts.clan_notes.ClanNoteOverlay;
import com.betterclanbroadcasts.clan_notes.HoveredClanMember;
import com.betterclanbroadcasts.clan_sorter.ClanPlayerListSorter;
import com.betterclanbroadcasts.clan_sorter.ClanSortToggleButton;
import com.google.common.base.Strings;
import com.google.inject.Provides;

import java.awt.Color;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
		name = "Clan QOL",
		description = "Prepends clan rank icons to clan broadcast messages",
		tags = {"clan", "broadcast", "rank", "icon", "chat", "clanchat", "qol", "filtering", "filter", "notes"}
)
public class BetterClanBroadcastsPlugin extends Plugin
{
	private static final Set<String> NOTE_ANCHOR_OPTIONS = Set.of("Add ignore", "Remove friend");
	private static final String ADD_NOTE = "Add Note";
	private static final String EDIT_NOTE = "Edit Note";
	private static final String NOTE_KEY_PREFIX = "note_";
	private static final int NOTE_CHARACTER_LIMIT = 128;
	private static final String NOTE_PROMPT_FORMAT = "%s's Notes<br>" +
			ColorUtil.prependColorTag("(Limit %s Characters)", new Color(0, 0, 170));

	private static final int CLAN_GROUP_ID = WidgetUtil.componentToInterface(InterfaceID.ClansSidepanel.PLAYERLIST);

	@Inject
	private Client client;
	@Inject
	private BetterClanBroadcastsConfig config;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ChatIconManager chatIconManager;
	@Inject
	private ConfigManager configManager;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ChatboxPanelManager chatboxPanelManager;
	@Inject
	private ClanNoteOverlay clanNoteOverlay;

    private ClanBroadcastHandler clanBroadcastHandler;
	private ClanPlayerListSorter clanPlayerListSorter;
	private ClanCaIconMaintainer clanCaIconMaintainer;

	private static final int SORT_BUTTON_Y = 33;
	private static final int WORLD_SORT_BUTTON_X = 140;
	private static final int NAME_SORT_BUTTON_X = 40;
	private static final int RANK_SORT_BUTTON_X = 13;

	private ClanSortToggleButton worldSortButton;
	private ClanSortToggleButton nameSortButton;
	private ClanSortToggleButton rankSortButton;

	@Getter
	private HoveredClanMember hoveredClanMember = null;

    // TEST ONLY - REMOVE BEFORE SHIPPING
    private static final String TEST_BROADCAST_MEMBER = "Snape Grass";
    private static final int TEST_BROADCAST_DELAY_TICKS = 17;
    private static final int TEST_CA_BROADCAST_DELAY_TICKS = TEST_BROADCAST_DELAY_TICKS + 5;
    private static final int TEST_CA_TIER_STAGGER_TICKS = 5;
    private static final String TEST_CA_ID = "100";
    // correct article per tier, matches confirmed real broadcast wording
    private static final String[] TEST_CA_TIERS = {
            "an easy", "a medium", "a hard", "an elite", "a master", "a grandmaster"
    };
    // tier unlock broadcasts use Title Case tier names, no article
    private static final String[] TEST_CA_UNLOCK_TIERS = {
            "Easy", "Medium", "Hard", "Elite", "Master", "Grandmaster"
    };
    // starts right after the 6 staggered task-completion broadcasts finish firing
    private static final int TEST_CA_UNLOCK_START_DELAY_TICKS =
            TEST_CA_BROADCAST_DELAY_TICKS + (TEST_CA_TIERS.length * TEST_CA_TIER_STAGGER_TICKS) + TEST_CA_TIER_STAGGER_TICKS;
    private int testBroadcastTicksRemaining = -1;
    private int testCaTierIndex = -1;
    private int testCaTierTicksRemaining = -1;
    private int testCaUnlockIndex = -1;
    private int testCaUnlockTicksRemaining = -1;
    private boolean testBroadcastsScheduled = false;

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN && !testBroadcastsScheduled) {
            testBroadcastsScheduled = true;
            testBroadcastTicksRemaining = TEST_BROADCAST_DELAY_TICKS;
            testCaTierIndex = 0;
            testCaTierTicksRemaining = TEST_CA_BROADCAST_DELAY_TICKS;
            testCaUnlockIndex = 0;
            testCaUnlockTicksRemaining = TEST_CA_UNLOCK_START_DELAY_TICKS;
        }
    }
    // END TEST ONLY - REMOVE BEFORE SHIPPING

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(clanNoteOverlay);

        CaIconTracker caIconTracker = new CaIconTracker(client);
        clanBroadcastHandler = new ClanBroadcastHandler(client, clientThread, chatIconManager, new BroadcastColorizer(config), caIconTracker);
        clanPlayerListSorter = new ClanPlayerListSorter(client, configManager);

        clanCaIconMaintainer = new ClanCaIconMaintainer(config, caIconTracker);
		overlayManager.add(clanCaIconMaintainer);

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
		overlayManager.remove(clanNoteOverlay);
		overlayManager.remove(clanCaIconMaintainer);

        clanBroadcastHandler = null;
        clanCaIconMaintainer = null;

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
	public void onChatMessage(ChatMessage event)
	{
		if (!config.enabled())
		{
			return;
		}
        clanBroadcastHandler.onChatMessage(event);
	}

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!config.enabled())
        {
            return;
        }
        clanBroadcastHandler.onGameTick(event);
        clanPlayerListSorter.onGameTick();
        worldSortButton.onGameTick();
        nameSortButton.onGameTick();
        rankSortButton.onGameTick();

        // TEST ONLY - REMOVE BEFORE SHIPPING
        if (testBroadcastTicksRemaining > 0)
        {
            testBroadcastTicksRemaining--;
            if (testBroadcastTicksRemaining == 0)
            {
                client.addChatMessage(ChatMessageType.CLAN_MESSAGE, "",
                        TEST_BROADCAST_MEMBER + " has reached level 99 in Woodcutting.", null);
            }
        }
        if (testCaTierIndex >= 0 && testCaTierIndex < TEST_CA_TIERS.length)
        {
            testCaTierTicksRemaining--;
            if (testCaTierTicksRemaining == 0)
            {
                String article = TEST_CA_TIERS[testCaTierIndex];
                client.addChatMessage(ChatMessageType.CLAN_MESSAGE, "",
                        "CA_ID:" + TEST_CA_ID + "|" + TEST_BROADCAST_MEMBER
                                + " has completed " + article + " combat task: Test Task " + (testCaTierIndex + 1) + ".", null);
                testCaTierIndex++;
                testCaTierTicksRemaining = TEST_CA_TIER_STAGGER_TICKS;
            }
        }
        if (testCaUnlockIndex >= 0 && testCaUnlockIndex < TEST_CA_UNLOCK_TIERS.length) {
            testCaUnlockTicksRemaining--;
            if (testCaUnlockTicksRemaining == 0) {
                String tierName = TEST_CA_UNLOCK_TIERS[testCaUnlockIndex];
                // no CA_ID tag - confirmed real tier-unlock broadcasts dont carry one
                client.addChatMessage(ChatMessageType.CLAN_MESSAGE, "",
                        TEST_BROADCAST_MEMBER + " has unlocked the " + tierName + " tier of rewards from Combat Achievements!", null);
                testCaUnlockIndex++;
                testCaUnlockTicksRemaining = TEST_CA_TIER_STAGGER_TICKS;
            }
        }
    }

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		int groupId = WidgetUtil.componentToInterface(event.getActionParam1());

		if (groupId == CLAN_GROUP_ID && NOTE_ANCHOR_OPTIONS.contains(event.getOption()))
		{
			// clan member names carry color tags same as friends do
			setHoveredClanMember(Text.toJagexName(Text.removeTags(event.getTarget())));

			client.createMenuEntry(-1)
					.setOption(hoveredClanMember == null || hoveredClanMember.getNote() == null ? ADD_NOTE : EDIT_NOTE)
					.setType(MenuAction.RUNELITE)
					.setTarget(event.getTarget())
					.onClick(e ->
					{
						String sanitizedTarget = Text.toJagexName(Text.removeTags(e.getTarget()));
						String note = getClanMemberNote(sanitizedTarget);

						chatboxPanelManager.openTextInput(String.format(NOTE_PROMPT_FORMAT, sanitizedTarget, NOTE_CHARACTER_LIMIT))
								.value(Strings.nullToEmpty(note))
								.onDone((content) ->
								{
									if (content == null)
									{
										return;
									}

									content = Text.removeTags(content).trim();
									log.debug("Set clan note for '{}': '{}'", sanitizedTarget, content);
									setClanMemberNote(sanitizedTarget, content);
								}).build();
					});
		}
		else if (hoveredClanMember != null)
		{
			hoveredClanMember = null;
		}
	}

	private void setClanMemberNote(String displayName, String note)
	{
		if (Strings.isNullOrEmpty(note))
		{
			configManager.unsetConfiguration(BetterClanBroadcastsConfig.CONFIG_GROUP, NOTE_KEY_PREFIX + displayName);
		}
		else
		{
			configManager.setConfiguration(BetterClanBroadcastsConfig.CONFIG_GROUP, NOTE_KEY_PREFIX + displayName, note);
		}
	}

	@Nullable
	private String getClanMemberNote(String displayName)
	{
		return configManager.getConfiguration(BetterClanBroadcastsConfig.CONFIG_GROUP, NOTE_KEY_PREFIX + displayName);
	}

	private void setHoveredClanMember(String displayName)
	{
		hoveredClanMember = null;

		if (!config.showNoteTooltip() || Strings.isNullOrEmpty(displayName))
		{
			return;
		}

		String note = getClanMemberNote(displayName);
		if (note != null)
		{
			hoveredClanMember = new HoveredClanMember(displayName, note);
		}
	}

	@Provides
	BetterClanBroadcastsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BetterClanBroadcastsConfig.class);
	}
}