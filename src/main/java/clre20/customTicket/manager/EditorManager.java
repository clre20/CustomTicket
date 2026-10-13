package clre20.customTicket.manager;

import clre20.customTicket.model.Ticket;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 管理每位玩家目前正在編輯的門票草稿。 */
public class EditorManager {

    public static final class EditorSession {
        private final Ticket draft;
        private final boolean isNew;

        private EditorSession(Ticket draft, boolean isNew) {
            this.draft = draft;
            this.isNew = isNew;
        }

        public Ticket getDraft() {
            return draft;
        }

        public boolean isNew() {
            return isNew;
        }
    }

    private final Map<UUID, EditorSession> sessions = new ConcurrentHashMap<>();

    public EditorSession start(UUID player, Ticket draft, boolean isNew) {
        EditorSession session = new EditorSession(draft, isNew);
        sessions.put(player, session);
        return session;
    }

    public EditorSession get(UUID player) {
        return sessions.get(player);
    }

    public void end(UUID player) {
        sessions.remove(player);
    }

    /** 回傳正在編輯此門票的玩家，若無則為 null。 */
    public UUID getEditor(String ticketId) {
        String key = ticketId.toLowerCase(Locale.ROOT);
        for (Map.Entry<UUID, EditorSession> entry : sessions.entrySet()) {
            if (entry.getValue().draft.getId().toLowerCase(Locale.ROOT).equals(key)) return entry.getKey();
        }
        return null;
    }

    public void clearAll() {
        sessions.clear();
    }
}
