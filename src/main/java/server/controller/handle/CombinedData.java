package server.controller.handle;

import server.model.User;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CombinedData implements Serializable {
    private List<User> onlineUsers;
    private List<Map<String, Object>> matchHistory;

    public CombinedData(List<User> onlineUsers, List<Map<String, Object>> matchHistory) {
        this.onlineUsers = onlineUsers;
        this.matchHistory = matchHistory;
    }

    public List<User> getOnlineUsers() {
        return onlineUsers;
    }

    public List<Map<String, Object>> getMatchHistory() {
        return matchHistory;
    }
}
