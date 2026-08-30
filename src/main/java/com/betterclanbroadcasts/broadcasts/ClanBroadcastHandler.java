package com.betterclanbroadcasts.broadcasts;

import net.runelite.api.Client;
import net.runelite.api.ChatMessageType;
import net.runelite.api.MessageNode;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanChannelMember;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.clan.ClanTitle;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.util.Text;
import lombok.extern.slf4j.Slf4j;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Queue;

@Slf4j
public class ClanBroadcastHandler {

    private static final int VERIFY_TICKS_AFTER_APPLY = 3;
    private static final String CA_ID_REGEX = "CA_ID:\\d+\\|";

    private final Client client;
    private final ClientThread clientThread;
    private final ChatIconManager chatIconManager;
    private final BroadcastColorizer colorizer;
    private final CaIconTracker caIconTracker;
    private final Queue<PendingEdit> pendingEdits = new ArrayDeque<>();

    public ClanBroadcastHandler(Client client, ClientThread clientThread, ChatIconManager chatIconManager,
                                BroadcastColorizer colorizer, CaIconTracker caIconTracker) {
        this.client = client;
        this.clientThread = clientThread;
        this.chatIconManager = chatIconManager;
        this.colorizer = colorizer;
        this.caIconTracker = caIconTracker;
    }

    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.CLAN_MESSAGE) {
            return;
        }

        String rawMessage = event.getMessage();

        ClanChannel clanChannel = client.getClanChannel();
        ClanSettings clanSettings = client.getClanSettings();
        if (clanChannel == null || clanSettings == null) {
            log.debug("Clan channel or settings unavailable, skipping broadcast: '{}'", rawMessage);
            return;
        }

        // CA broadcasts only. same string used for detect and strip, keep in sync.
        String withoutTags = Text.removeTags(rawMessage).trim();
        boolean isCombatAchievement = withoutTags.matches("^" + CA_ID_REGEX + ".*");
        // names can have _ - nbsp instead of space, toJagexName fixes all three
        String strippedMessage = Text.toJagexName(withoutTags.replaceFirst("^" + CA_ID_REGEX, "")).trim();

        if (strippedMessage.startsWith("To talk in your clan's channel")) {
            return;
        }

        // fixing double icon adding due to timing issue with chat channels plugin join and leave messages
        if (strippedMessage.matches(".*\\bhas (joined|left)\\.?$")) {
            return;
        }

        ClanChannelMember matched = findMatchingMember(clanChannel, strippedMessage);
        if (matched == null) {
            log.debug("No clan member match for broadcast: '{}'", strippedMessage);
            return;
        }

        ClanTitle title = clanSettings.titleForRank(matched.getRank());
        if (title == null) {
            log.debug("No title found for rank {} (member {})", matched.getRank(), Text.toJagexName(matched.getName()));
            return;
        }

        int iconIndex = chatIconManager.getIconNumber(title);

        if (isCombatAchievement) {
            // View task/Open task are native, tied to the MessageNode - leave it
            // untouched, prepend the icon onto the widget text instead
            BroadcastColorizer.CaTier tier = colorizer.detectTier(strippedMessage);
            Color tierColor = colorizer.colorFor(tier);
            caIconTracker.enqueue(strippedMessage, tierColor, iconIndex);
            return;
        }

        String iconPrefix = iconPrefixFor(title, iconIndex);
        PendingEdit edit = new PendingEdit(event.getMessageNode(), rawMessage, iconPrefix, VERIFY_TICKS_AFTER_APPLY);
        applyEdit(edit);
        pendingEdits.add(edit);
    }

    private String iconPrefixFor(ClanTitle title, int iconIndex) {
        // -1 means no icon registered for rank, fall back to plain text bracket
        return iconIndex >= 0
                ? "<img=" + iconIndex + "> "
                : "[" + title.getName() + "] ";
    }

    private ClanChannelMember findMatchingMember(ClanChannel clanChannel, String strippedMessage) {
        // longest match wins. stops short names matching inside longer ones
        ClanChannelMember matched = null;
        String matchedName = null;

        for (ClanChannelMember member : clanChannel.getMembers()) {
            String name = Text.toJagexName(member.getName());
            if (strippedMessage.startsWith(name) && (matchedName == null || name.length() > matchedName.length())) {
                matched = member;
                matchedName = name;
            }
        }

        return matched;
    }

    public void onGameTick(GameTick event) {
        caIconTracker.onGameTick();

        if (pendingEdits.isEmpty()) {
            return;
        }

        int size = pendingEdits.size();
        for (int i = 0; i < size; i++) {
            PendingEdit edit = pendingEdits.poll();

            String current = edit.messageNode.getRuneLiteFormatMessage();
            if (current == null || !current.startsWith(edit.iconPrefix)) {
                applyEdit(edit);
            }

            edit.ticksRemaining--;
            if (edit.ticksRemaining > 0) {
                pendingEdits.add(edit);
            }
        }
    }

    private void applyEdit(PendingEdit edit) {
        clientThread.invokeLater(() -> {
            edit.messageNode.setRuneLiteFormatMessage(buildMessage(edit));
            client.refreshChat();
        });
    }

    // builds the message fresh at apply time so it reflects whatever formatting other plugins have added to the node
    private String buildMessage(PendingEdit edit) {
        String current = edit.messageNode.getRuneLiteFormatMessage();
        String body;
        if (current == null || current.isEmpty()) {
            body = edit.rawMessage;
        } else if (current.startsWith(edit.iconPrefix)) {
            body = current.substring(edit.iconPrefix.length());
        } else {
            body = current;
        }
        return edit.iconPrefix + body;
    }

    // an edit thats been applied once, still being watched for a few ticks
    // in case something reverts it
    private static class PendingEdit {
        final MessageNode messageNode;
        final String rawMessage;
        final String iconPrefix;
        int ticksRemaining;

        PendingEdit(MessageNode messageNode, String rawMessage, String iconPrefix, int ticksRemaining) {
            this.messageNode = messageNode;
            this.rawMessage = rawMessage;
            this.iconPrefix = iconPrefix;
            this.ticksRemaining = ticksRemaining;
        }
    }
}