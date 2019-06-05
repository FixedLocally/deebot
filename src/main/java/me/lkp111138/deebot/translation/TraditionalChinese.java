package me.lkp111138.deebot.translation;

public class TraditionalChinese extends Translation {
    @Override
    public String BOT_NAME() {
        return "j0kebig2bot";
    }
    @Override
    public String ERROR() {
        return "發生錯誤：";
    }
    @Override
    public String JOIN_SUCCESS() {
        return "你已成功加入 *%s* 的遊戲！遊戲編號：%d";
    }
    @Override
    public String BACK_TO() {
        return "返回 ";
    }
    @Override
    public String JOINED_ANNOUNCEMENT() {
        return "[ <a href=\"tg://user?id=%d\">%s</a> ] 已加入遊戲！ 已有 <b>%d</b> 名玩家，總共需要 <b>4</b> 名。";
    }
    @Override
    public String START_ME_FIRST() {
        return "請先啟用我！";
    }
    @Override
    public String START_ME() {
        return "啟用";
    }
    @Override
    public String EXTENDED_ANNOUNCEMENT() {
        return "加入時間已延長30秒，尚餘 %d 秒。 /join";
    }
    @Override
    public String PASS() {
        return "Pass";
    }
    @Override
    public String PASS_ON_EMPTY() {
        return "桌面什麼也沒有，你可打出任何有效組合";
    }
    @Override
    public String PASS_ON_FIRST() {
        return "這是第一輪，你必須打出 \u2666\ufe0f 3";
    }
    @Override
    public String NO_D3_ON_FIRST() {
        return "這是第一輪，你必須打出 \u2666\ufe0f 3";
    }
    @Override
    public String PLAYED_ANNOUNCEMENT_LINK() {
        return "<a href=\"https://t.me/%s\">%s</a> 打出 ";
    }
    @Override
    public String PLAYED_ANNOUNCEMENT() {
        return "%s 打出 ";
    }
    @Override
    public String PASS_ANNOUNCEMENT_LINK() {
        return "<a href=\"https://t.me/%s\">%s</a> 選擇 Pass";
    }
    @Override
    public String PASS_ANNOUNCEMENT() {
        return "%s 選擇 Pass";
    }
    @Override
    public String INVALID_HAND() {
        return "組合無效，請再試";
    }
    @Override
    public String SMALL_HAND() {
        return "組合太小，請再試";
    }
    @Override
    public String WON_ANNOUNCEMENT() {
        return "<a href=\"tg://user?id=%d\">%s</a> 獲勝！\n\n";
    }
    @Override
    public String NEW_GAME_PROMPT() {
        return "\n按 /play 來開始新遊戲";
    }
    @Override
    public String GAME_ENDED() {
        return "遊戲已結束";
    }
    @Override
    public String GAME_ENDED_ANNOUNCEMENT() {
        return "遊戲結束。按 /play 來開始新遊戲";
    }
    @Override
    public String YOUR_DECK() {
        return "你的手牌：\n";
    }
    @Override
    public String STARTING_DECK() {
        return "起始手牌：\n";
    }
    @Override
    public String YOUR_TURN_PROMPT() {
        return "輪到你！\n桌上";
    }
    @Override
    public String THERE_IS_NOTHING() {
        return "什麼也沒有";
    }
    @Override
    public String TIMES_UP() {
        return "時間到！";
    }
    @Override
    public String AFK_KILL() {
        return "看來所有人都已離開，停止遊戲！";
    }
    @Override
    public String ON_DESK_LINK() {
        return "\n目前桌上有：<a href=\"https://t.me/%2$s\">%3$s</a> 打出的 %1$s\n";
    }
    @Override
    public String ON_DESK() {
        return "\n目前桌上有：%2$s 打出的 %1$s\n";
    }
    @Override
    public String YOUR_TURN_ANNOUNCEMENT() {
        return "輪到你了， <a href=\"tg://user?id=%d\">%s</a>，你有 %d 秒出牌！";
    }
    @Override
    public String PICK_CARDS() {
        return "按此選牌";
    }
    @Override
    public String CHOOSE_SOME_CARDS() {
        return "\u23eb 請先選牌 \u23eb";
    }
    @Override
    public String JOIN_PROMPT() {
        return "尚餘 %d 秒加入遊戲 /join";
    }
    @Override
    public String NO_GAME_TO_JOIN() {
        return "目前沒有遊戲進行中，按 /play 開新遊戲";
    }
    @Override
    public String GAME_STARTING() {
        return "遊戲即將開始！ 按 /join 加入。";
    }
    @Override
    public String GAME_STARTED() {
        return "遊戲已開始！請等遊戲結束後再開新遊戲。";
    }
    @Override
    public String GAME_START_ANNOUNCEMENT() {
        return "[ <a href=\"tg://user?id=%d\">%s</a> ] 已開始新遊戲！你有 %d 秒加入 /join\n\n計 Place：%s\n炒：%s\n包牌：%s\n遊戲編號：%d";
    }
    @Override
    public String NOTHING_ON_DESK() {
        return "\n桌面上什麼也沒有。\n";
    }
    @Override
    public String SORT_SUIT() {
        return "按花色排序";
    }
    @Override
    public String SORT_FACE() {
        return "按點數排序";
    }
    @Override
    public String FLEE_ANNOUNCEMENT() {
        return "<a href=\"tg://user?id=%d\">%s</a> 已退出遊戲！ 尚餘 %d 名玩家";
    }
    @Override
    public String MAINT_MODE_NOTICE() {
        return "正在維修，請稍後再試。";
    }
    @Override
    public String CLOSE() {
        return "關閉";
    }
}
