package me.lkp111138.deebot.translation;

public class HongKongChinese extends Translation {
    @Override
    public String BOT_NAME() {
        return "jokebig2bot";
    }
    @Override
    public String ERROR() {
        return "出咗個問題：";
    }
    @Override
    public String JOIN_SUCCESS() {
        return "你成功加入咗 *%s* 嘅遊戲！ ";
    }
    @Override
    public String BACK_TO() {
        return "翻去 ";
    }
    @Override
    public String JOINED_ANNOUNCEMENT() {
        return "[ <a href=\"tg://user?id=%d\">%s</a> ] 加入咗遊戲！ 已經有 <b>%d</b> 個玩家，一共需要 <b>4</b> 個。";
    }
    @Override
    public String START_ME_FIRST() {
        return "撳下面個制先！";
    }
    @Override
    public String START_ME() {
        return "撳！";
    }
    @Override
    public String EXTENDED_ANNOUNCEMENT() {
        return "加入時間延長咗30秒，仲有 %d 秒。 /join";
    }
    @Override
    public String PASS() {
        return "Pass";
    }
    @Override
    public String PASS_ON_EMPTY() {
        return "檯面上乜都冇，你可以出任何有效嘅組合";
    }
    @Override
    public String PASS_ON_FIRST() {
        return "而家係第一輪，你一定要出 \u2666\ufe0f 3";
    }
    @Override
    public String NO_D3_ON_FIRST() {
        return "而家係第一輪，你一定要出 \u2666\ufe0f 3";
    }
    @Override
    public String PLAYED_ANNOUNCEMENT_LINK() {
        return "<a href=\"https://t.me/%s\">%s</a> 出咗 ";
    }
    @Override
    public String PLAYED_ANNOUNCEMENT() {
        return "%s 出咗 ";
    }
    @Override
    public String PASS_ANNOUNCEMENT_LINK() {
        return "<a href=\"https://t.me/%s\">%s</a> 揀咗 Pass";
    }
    @Override
    public String PASS_ANNOUNCEMENT() {
        return "%s 揀咗 Pass";
    }
    @Override
    public String INVALID_HAND() {
        return "組合無效，試多次";
    }
    @Override
    public String SMALL_HAND() {
        return "組合大唔到上家，試多次";
    }
    @Override
    public String WON_ANNOUNCEMENT() {
        return "<a href=\"tg://user?id=%d\">%s</a> 贏咗！\n\n";
    }
    @Override
    public String NEW_GAME_PROMPT() {
        return "\n撳 /play 嚟開新遊戲";
    }
    @Override
    public String GAME_ENDED() {
        return "遊戲完咗";
    }
    @Override
    public String GAME_ENDED_ANNOUNCEMENT() {
        return "遊戲完咗。撳 /play 嚟開新遊戲";
    }
    @Override
    public String YOUR_DECK() {
        return "你嘅手牌：\n";
    }
    @Override
    public String STARTING_DECK() {
        return "起始手牌：\n";
    }
    @Override
    public String YOUR_TURN_PROMPT() {
        return "輪到你！\n檯上";
    }
    @Override
    public String THERE_IS_NOTHING() {
        return "乜都冇";
    }
    @Override
    public String TIMES_UP() {
        return "夠鐘！";
    }
    @Override
    public String AFK_KILL() {
        return "好似所有人都忙緊，遊戲結束！";
    }
    @Override
    public String ON_DESK_LINK() {
        return "\n而家檯上面：<a href=\"https://t.me/%2$s\">%3$s</a> 打出嘅 %1$s\n";
    }
    @Override
    public String ON_DESK() {
        return "\n而家檯上面：%2$s 打出嘅 %1$s\n";
    }
    @Override
    public String YOUR_TURN_ANNOUNCEMENT() {
        return "輪到你啦， <a href=\"tg://user?id=%d\">%s</a>，你有 %d 秒出牌！";
    }
    @Override
    public String PICK_CARDS() {
        return "撳呢度揀牌";
    }
    @Override
    public String CHOOSE_SOME_CARDS() {
        return "\u23eb 揀牌先 \u23eb";
    }
    @Override
    public String JOIN_PROMPT() {
        return "仲有 %d 秒，快啲入嚟一齊玩 /join";
    }
    @Override
    public String NO_GAME_TO_JOIN() {
        return "而家冇人玩緊，撳 /play 開新遊戲";
    }
    @Override
    public String GAME_STARTING() {
        return "遊戲就嚟開始！ 撳 /join 加入。";
    }
    @Override
    public String GAME_STARTED() {
        return "遊戲開咗始！請等呢場完咗再開過";
    }
    @Override
    public String GAME_START_ANNOUNCEMENT() {
        return "[ <a href=\"tg://user?id=%d\">%s</a> ] 開始咗新遊戲！你有 %d 秒加入 /join\n\n計 Place：%s\n炒：%s\n包牌：%s\n遊戲編號：%d";
    }
    @Override
    public String NOTHING_ON_DESK() {
        return "\n檯面上乜都冇。\n";
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
        return "<a href=\"tg://user?id=%d\">%s</a> 退出咗遊戲！ 仲有 %d 個玩家";
    }
    @Override
    public String MAINT_MODE_NOTICE() {
        return "維修緊，請遲啲再試。";
    }
    @Override
    public String CLOSE() {
        return "關閉";
    }
}
