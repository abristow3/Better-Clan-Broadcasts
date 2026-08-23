package com.betterclanbroadcasts;

import net.runelite.api.ChatLineBuffer;
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

import java.util.ArrayDeque;
import java.util.Queue;

@Slf4j
public class ClanRankPrefixer {

	private static final int INITIAL_APPLY_DELAY_TICKS = 1;
	private static final int VERIFY_TICKS_AFTER_APPLY = 3;
	private static final String CA_ID_REGEX = "CA_ID:\\d+\\|";

	private final Client client;
	private final ClientThread clientThread;
	private final ChatIconManager chatIconManager;
	private final Queue<PendingEdit> pendingEdits = new ArrayDeque<>();
	private String lastInjectedMessage = null;

	public ClanRankPrefixer(Client client, ClientThread clientThread, ChatIconManager chatIconManager) {
		this.client = client;
		this.clientThread = clientThread;
		this.chatIconManager = chatIconManager;
	}

	public void onChatMessage(ChatMessage event) {
		if (event.getType() != ChatMessageType.CLAN_MESSAGE) {
			return;
		}

		String rawMessage = event.getMessage();

		if (rawMessage.equals(lastInjectedMessage)) {
			lastInjectedMessage = null;
			return;
		}

		ClanChannel clanChannel = client.getClanChannel();
		ClanSettings clanSettings = client.getClanSettings();
		if (clanChannel == null || clanSettings == null) {
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

		// guard against double-prefixing (e.g. the client re-firing an event
		// for our own republished CA line, which the exact-string
		// lastInjectedMessage check above doesnt always catch). checks for
		// OUR specific icon number right after an optional CA_ID tag, not
		// just any leading <img=> tag - a message can legitimately start
		// with an unrelated icon (the player's ironman icon) even on a
		// genuine first pass, and that must not be mistaken for our prefix
		if (iconIndex >= 0 && rawMessage.trim().matches("^(?:" + CA_ID_REGEX + ")?<img=" + iconIndex + ">\\s.*")) {
			log.debug("Message already prefixed with our icon ({}), skipping: '{}'", iconIndex, rawMessage);
			return;
		}

		if (isCombatAchievement) {
			// client renders CA lines its own way, editing text does nothing here.
			// current workaround delete node, publish fresh one
			replaceCombatAchievementBroadcast(event.getMessageNode(), rawMessage, title, iconIndex);
			return;
		}

		String newMessage = buildPrefixedMessage(rawMessage, title, iconIndex);
		pendingEdits.add(new PendingEdit(event.getMessageNode(), newMessage, INITIAL_APPLY_DELAY_TICKS));
	}

	private void replaceCombatAchievementBroadcast(MessageNode originalNode, String rawMessage, ClanTitle title, int iconIndex) {
		// strip only CA_ID tag, keep everything else (ironman icons)
		String cleanText = rawMessage.replaceFirst(CA_ID_REGEX, "").trim();
		String injectedMessage = buildPrefixedMessage(cleanText, title, iconIndex);

		lastInjectedMessage = injectedMessage;
		clientThread.invokeLater(() -> {
			// remove old line, then add new one. order matters, do not swap.
			ChatLineBuffer buffer = client.getChatLineMap().get(ChatMessageType.CLAN_MESSAGE.getType());
			if (buffer != null) {
				buffer.removeMessageNode(originalNode);
			}
			client.addChatMessage(ChatMessageType.CLAN_MESSAGE, "", injectedMessage, null);
			client.refreshChat();
		});
	}

	private String buildPrefixedMessage(String message, ClanTitle title, int iconIndex) {
		// -1 means no icon registered for rank, fall back to plain text bracket
		return iconIndex >= 0
				? "<img=" + iconIndex + "> " + message
				: "[" + title.getName() + "] " + message;
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
		if (pendingEdits.isEmpty()) {
			return;
		}

		int size = pendingEdits.size();
		for (int i = 0; i < size; i++) {
			PendingEdit edit = pendingEdits.poll();

			if (!edit.applied) {
				edit.ticksRemaining--;
				if (edit.ticksRemaining <= 0) {
					applyEdit(edit);
					edit.applied = true;
					edit.ticksRemaining = VERIFY_TICKS_AFTER_APPLY;
					pendingEdits.add(edit);
				} else {
					pendingEdits.add(edit);
				}
				continue;
			}

			// recheck after apply. non-CA lines don't get reclobbered, but cheap safety net.
			if (!edit.message.equals(edit.messageNode.getRuneLiteFormatMessage())) {
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
			edit.messageNode.setRuneLiteFormatMessage(edit.message);
			client.refreshChat();
		});
	}

	// one edit waiting for tick delay, then a few more ticks to verify it stuck
	private static class PendingEdit {
		final MessageNode messageNode;
		final String message;
		int ticksRemaining;
		boolean applied = false;

		PendingEdit(MessageNode messageNode, String message, int ticksRemaining) {
			this.messageNode = messageNode;
			this.message = message;
			this.ticksRemaining = ticksRemaining;
		}
	}
}