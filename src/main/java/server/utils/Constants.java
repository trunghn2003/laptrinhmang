package server.utils;

public class Constants {
    // Trạng thái người chơi
    public static final int STATUS_OFFLINE = 0;
    public static final int STATUS_ONLINE = 1;
    public static final int STATUS_PLAYING = 2;

    // Các hành động
    public static final String ACTION_LOGIN = "login";
    public static final String ACTION_REGISTER = "register";
    public static final String ACTION_INVITE = "invite";
    public static final String ACTION_INVITE_RESPONSE = "invite_response";
    public static final String ACTION_GAME_MOVE = "game_move";
    public static final String ACTION_GAME_RESULT = "game_result";
    public static final String ACTION_MATCH_RESULT = "match_result";
    public static final String ACTION_SEND_COLORS = "send_colors";
    public static final String ACTION_EXIT_MID_GAME = "exit_mid_game";
    public static final String ACTION_START_GAME = "start_game";
    public static final String ACTION_START_ROUND = "start_round";
    public static final String ACTION_FINISH_GAME = "finish_game";


    // Phản hồi
    public static final String RESPONSE_INVITE = "response_invite";
    public static final String RESPONSE_INVITE_RESPONSE = "response_invite_response";
    public static final String RESPONSE_GAME_START = "response_game_start";
    public static final String RESPONSE_GAME_RESULT = "response_game_result";
    public static final String RESPONSE_RANDOM_COLORS = "response_random_colors";
    public static final String RESPONSE_EXIT_MIDDLE_GAME = "response_exit_middle_game";
    public static final String RESPONSE_MATCH_RESULT = "response_match_result";
    public static final String RESPONSE_FINISH_GAME = "response_finish_game";
    public static final String RESPONSE_GET_ENEMY_SCORE_THIS_ROUND = "response_get_enemy_score_this_round";

    // Thông báo đăng nhập và đăng ký
    public static final String LOGIN_SUCCESS = "Login successful!";
    public static final String LOGIN_FAILURE = "Login failed";
    public static final String REGISTER_SUCCESS = "Registration successful!";
    public static final String REGISTER_FAILURE = "Registration failed";
    public static final String ACTION_GET_HISTORY  = "Get History";
}
