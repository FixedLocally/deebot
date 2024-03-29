package me.lkp111138.deebot.translation;

import me.lkp111138.deebot.game.Game;

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
        return "你已成功加入 <b>%s</b> 的遊戲！遊戲編號：%d";
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
    @Override
    public String ACHIEVEMENT_UNLOCKED() {
        return "解鎖成就！\n";
    }
    @Override
    public String ACHV_UNLOCKED() {
        return "已解鎖成就：\n";
    }
    @Override
    public String A_TOTAL_OF() {
        return "合共 %d 個";
    }

    @Override
    public String HELP() {
        return "**指令列表**\n" +
                "\n" +
                "/stats - 顯示統計數據。\n" +
                "/achv - 顯示你已解鎖的成就。\n" +
                "/help - 顯示此列表。\n" +
                "\n" +
                "**群組指令**\n" +
                "\n" +
                "/play - 開始新遊戲。\n" +
                "/startgame - 與 /play 相同\n" +
                "/join - 加入遊戲。\n" +
                "/flee - 在遊戲開始前離開遊戲。\n" +
                "/extend - 延長加入遊戲階段\n" +
                "\n" +
                "**僅限群組管理員**\n" +
                "\n" +
                "/config - 在私訊中更改群組遊戲設定。\n" +
                "/setlang - 更改群組語言\n" +
                "\n" +
                "**技術指令**\n" +
                "\n" +
                "/runinfo - 顯示某些資料。\n" +
                "/ping - 未知的指令。";
    }

    @Override
    public String ACHIEVEMENT_TITLE(String achv_key) {
        switch (achv_key) {
            case "FIRST_GAME":
                return "**新手**\n";
            case "FIRST_WIN":
                return "**我贏了！**\n";
            case "PLAY_WITH_MINT":
                return "**You need a mint?**\n";
            case "FIRST_BLOOD":
                return "**第一滴血**\n";
            case "ROOKIE":
                return "**初出茅廬**\n";
            case "FAMILIARIZED":
                return "**熟手**\n";
            case "ADDICTED":
                return "**上癮**\n";
            case "AMATEUR":
                return "**業餘選手**\n";
            case "ADEPT":
                return "**老手**\n";
            case "EXPERT":
                return "**專家**\n";
            case "LOSE_IT_ALL":
                return "**一敗塗地**\n";
            case "DEEP_FRIED":
                return "**油炸**\n";
            default:
                return achv_key;
        }
    }

    @Override
    public String ACHIEVEMENT_DESC(String achv_key) {
        switch (achv_key) {
            case "FIRST_GAME":
                return "玩一場遊戲。";
            case "FIRST_WIN":
                return "贏一場遊戲。";
            case "PLAY_WITH_MINT":
                return "個遊戲開發者玩一場。";
            case "FIRST_BLOOD":
                return "輸一場遊戲，同時失去籌碼。";
            case "ROOKIE":
                return "玩 50 場遊戲。";
            case "FAMILIARIZED":
                return "玩 200 場遊戲。";
            case "ADDICTED":
                return "玩 1000 場遊戲。";
            case "AMATEUR":
                return "贏 20 場遊戲。";
            case "ADEPT":
                return "贏 100 場遊戲。";
            case "EXPERT":
                return "贏 500 場遊戲。";
            case "LOSE_IT_ALL":
                return "在一場遊戲中輸 13 張牌。";
            case "DEEP_FRIED":
                return "贏一場遊戲，當中你的對手並未打出任何一張牌。";
            default:
                return achv_key;
        }
    }

    @Override
    public String JOIN_69_PROTEST() {
        return "He died because he fell off a carpark of a few meters high over a wall of over 4 feet, allegedly running avoid the police and tear gas.\n" +
                "https://www.scmp.com/news/hong-kong/politics/article/3036833/hong-kong-student-who-suffered-severe-brain-injury-after\n" +
                "打 /toggle69 唔再顯示呢個訊息";
    }

    @Override
    public String OCT_5_STRIKE() {
        return "\n\n本 bot 今日暫停服務。";
    }

    @Override
    public String HAND_TYPE(Game.HandType type) {
        switch (type) {
            case NONE:
                return "";
            case PAIR:
                return "對子";
            case FLUSH:
                return "同花";
            case SINGLE:
                return "散牌";
            case TRIPLE:
                return "三條";
            case STRAIGHT:
                return "順子";
            case FULL_HOUSE:
                return "葫蘆";
            case FOUR_OF_A_KIND:
                return "四條";
            case STRAIGHT_FLUSH:
                return "同花順";
            default:
                // unreachable code
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    @Override
    public String GAME_ENDED_ERROR() {
        return "發生錯誤！遊戲提前結束！";
    }

    @Override
    public String NEXT_GAME_QUEUED(String name) {
        return String.format("當遊戲喺 %s 度開始嗰陣，我會通知你", name);
    }

    @Override
    public String GAME_STARTING_IN(String name) {
        return String.format("遊戲即將於 %s 開始", name);
    }

    @Override
    public String CANCEL() {
        return "取消";
    }

    @Override
    public String STAT(long id, String name, int wonCards, int lostCards, int wonGames, int totalGames, int chips) {
        return String.format("<a href=\"tg://user?id=%d\">%s</a> 的大老二統計數字：\n", id, name) +
                String.format("贏牌數：%d\n", wonCards) +
                String.format("輸牌數：%d\n", lostCards) +
                String.format("贏/輸場數：%d / %d (%.2f%%)\n", wonGames, totalGames, 100.0 * wonGames / totalGames) +
                String.format("贏/輸牌數：%d / %d (%.2f%%)\n", wonCards, lostCards, 100.0 * wonCards / lostCards) +
                String.format("籌碼數：%d\n", chips) +
                String.format("平均每場贏得籌碼：%.1f", (chips - 2000.0) / totalGames);
    }
}
