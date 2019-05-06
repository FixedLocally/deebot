package me.lkp111138.deebot;

import java.util.HashMap;
import java.util.Map;

public class Translation {
    // get the strings in commands and Game
    private static Map<String, String> zh = new HashMap<>();
    private static Map<String, String> en = new HashMap<>();
    private static Map<String, Map<String, String>> langs = new HashMap<>();

    static {
        en();
        zh();
        langs.put("zh", zh);
        langs.put("en", en);
    }

    private static void en() {
        en.put("ERROR", "An error occurred: ");
        en.put("JOIN_SUCCESS", "You have successfully joined the game in *%s*! ");
        en.put("BACK_TO", "Back to ");
        en.put("JOINED_ANNOUNCEMENT", "[ <a href=\"tg://user?id=%d\">%s</a> ] has joined the game! <b>%d</b> out of <b>4</b> has joined.");
        en.put("START_ME_FIRST", "Please start me first!");
        en.put("START_ME", "Start me");
        en.put("EXTENDED_ANNOUNCEMENT", "Extended for 30 seconds. %d seconds left to /join");
        en.put("PASS", "Pass");
        en.put("PASS_ON_EMPTY", "The desk is empty, you are free to play any hand");
        en.put("PASS_ON_FIRST", "It's the first turn, your hand must include \u2666\ufe0f 3");
        en.put("NO_D3_ON_FIRST", "You must play \u2666\ufe0f 3 in the first turn");
        en.put("PLAYED_ANNOUNCEMENT_LINK", "<a href=\"https://t.me/%s\">%s</a> played ");
        en.put("PLAYED_ANNOUNCEMENT", "%s played ");
        en.put("PASS_ANNOUNCEMENT", "%s passed");
        en.put("PASS_ANNOUNCEMENT_LINK", "<a href=\"https://t.me/%s\">%s</a> passed");
        en.put("INVALID_HAND", "Invalid combination, please try again");
        en.put("SMALL_HAND", "Hand too small, please try again");
        en.put("WON_ANNOUNCEMENT", "<a href=\"tg://user?id=%d\">%s</a> Won!\n\n");
        en.put("NEW_GAME_PROMPT", "\nType /play to start a new game");
        en.put("GAME_ENDED", "Game ended");
        en.put("GAME_ENDED_ANNOUNCEMENT", "Game ended. /play to start a new one");
        en.put("YOUR_DECK", "Your deck:\n");
        en.put("STARTING_DECK", "Starting deck:\n");
        en.put("YOUR_TURN_PROMPT", "It's your turn!\nOn the desk");
        en.put("THERE_IS_NOTHING", " there is nothing");
        en.put("TIMES_UP", "Time's up!");
        en.put("AFK_KILL", "Looks like everybody is away, stopping game!");
        en.put("ON_DESK_LINK", "\nOn the desk: %s by <a href=\"https://t.me/%s\">%s</a>\n");
        en.put("ON_DESK", "\nOn the desk: %s by %s\n");
        en.put("YOUR_TURN_ANNOUNCEMENT", "It's your turn, <a href=\"tg://user?id=%d\">%s</a>, you have %d seconds to play your cards!");
        en.put("PICK_CARDS", "Pick your cards");
        en.put("CHOOSE_SOME_CARDS", "\u23eb Choose some cards \u23eb");
        en.put("JOIN_PROMPT", "You have %d seconds left to /join");
        en.put("NO_GAME_TO_JOIN", "There is no game to join here. /play to start one.");
        en.put("GAME_STARTING", "A game is about to start! Type /join to join.");
        en.put("GAME_STARTED", "The game has already started! Wait for it to finish before starting a new one.");
        en.put("GAME_START_ANNOUNCEMENT", "[ <a href=\"tg://user?id=%d\">%s</a> ] has started a new game! You have %d seconds to /join");
        en.put("NOTHING_ON_DESK", "\nThere is nothing on the desk.\n");
        en.put("UPDATE_DECK", "Update deck");
        en.put("SORT_SUIT", "Sort by suit");
        en.put("SORT_FACE", "Sort by face");
        en.put("FLEE_ANNOUNCEMENT", "<a href=\"tg://user?id=%d\">%s</a> has fled from the game! %d player%s remaining.");
        en.put("", "");
    }

    private static void zh() {
        zh.put("ERROR", "發生錯誤：");
        zh.put("JOIN_SUCCESS", "你已成功加入 *%s* 的遊戲！ ");
        zh.put("BACK_TO", "返回 ");
        zh.put("JOINED_ANNOUNCEMENT", "[ <a href=\"tg://user?id=%d\">%s</a> ] 已加入遊戲！ 已有 <b>%d</b> 名玩家，總共需要 <b>4</b> 名。");
        zh.put("START_ME_FIRST", "請先啟用我！");
        zh.put("START_ME", "啟用");
        zh.put("EXTENDED_ANNOUNCEMENT", "加入時間已延長30秒，尚餘 %d 秒。 /join");
        zh.put("PASS", "Pass");
        zh.put("PASS_ON_EMPTY", "桌面什麼也沒有，你可打出任何手牌");
        zh.put("PASS_ON_FIRST", "這是第一輪，你必須打出 \u2666\ufe0f 3");
        zh.put("NO_D3_ON_FIRST", "這是第一輪，你必須打出 \u2666\ufe0f 3");
        zh.put("PLAYED_ANNOUNCEMENT_LINK", "<a href=\"https://t.me/%s\">%s</a> 打出 ");
        zh.put("PLAYED_ANNOUNCEMENT", "%s 打出 ");
        zh.put("PASS_ANNOUNCEMENT_LINK", "<a href=\"https://t.me/%s\">%s</a> 選擇 Pass");
        zh.put("PASS_ANNOUNCEMENT", "%s 選擇 Pass");
        zh.put("INVALID_HAND", "組合無效，請再試");
        zh.put("SMALL_HAND", "組合太小，請再試");
        zh.put("WON_ANNOUNCEMENT", "<a href=\"tg://user?id=%d\">%s</a> 獲勝！\n\n");
        zh.put("NEW_GAME_PROMPT", "\n按 /play 來開始新遊戲");
        zh.put("GAME_ENDED", "遊戲已結束");
        zh.put("GAME_ENDED_ANNOUNCEMENT", "遊戲結束。按 /play 來開始新遊戲");
        zh.put("YOUR_DECK", "你的手牌：\n");
        zh.put("STARTING_DECK", "起始手牌：\n");
        zh.put("YOUR_TURN_PROMPT", "輪到你！\n桌上");
        zh.put("THERE_IS_NOTHING", "什麼也沒有");
        zh.put("TIMES_UP", "時間到！");
        zh.put("AFK_KILL", "看來所有人都已離開，停止遊戲！");
        zh.put("ON_DESK_LINK", "\n目前桌上有：<a href=\"https://t.me/%2$s\">%3$s</a> 打出的 %1$s\n");
        zh.put("ON_DESK", "\n目前桌上有：%2$s 打出的 %1$s\n");
        zh.put("YOUR_TURN_ANNOUNCEMENT", "輪到你了， <a href=\"tg://user?id=%d\">%s</a>，你有 %d 秒出牌！");
        zh.put("PICK_CARDS", "按此選牌");
        zh.put("CHOOSE_SOME_CARDS", "\u23eb 請先選牌 \u23eb");
        zh.put("JOIN_PROMPT", "尚餘 %d 秒加入遊戲 /join");
        zh.put("NO_GAME_TO_JOIN", "目前沒有遊戲進行中，按 /play 開新遊戲");
        zh.put("GAME_STARTING", "遊戲即將開始！ 按 /join 加入。");
        zh.put("GAME_STARTED", "遊戲已開始！請等遊戲結束後再開新遊戲。");
        zh.put("GAME_START_ANNOUNCEMENT", "[ <a href=\"tg://user?id=%d\">%s</a> ] 已開始新遊戲！你有 %d 秒加入 /join");
        zh.put("NOTHING_ON_DESK", "\n桌面上什麼也沒有。\n");
        zh.put("UPDATE_DECK", "更新手牌");
        zh.put("SORT_SUIT", "按花色排序");
        zh.put("SORT_FACE", "按點數排序");
        zh.put("FLEE_ANNOUNCEMENT", "<a href=\"tg://user?id=%d\">%s</a> 已退出遊戲！ 尚餘 %d 名玩家");
        zh.put("", "");
    }

    public static String get(String lang, String key) {
        return langs.get(lang).get(key);
    }
}
