package me.lkp111138.deebot.translation;

import me.lkp111138.deebot.game.Game;

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
        return "你成功加入咗 <b>%s</b> 嘅遊戲！遊戲編號：%d";
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
        return "遊戲開始咗！請等呢場完咗再開過";
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
    @Override
    public String ACHIEVEMENT_UNLOCKED() {
        return "解鎖成就！\n";
    }
    @Override
    public String ACHV_UNLOCKED() {
        return "解鎖咗嘅成就：\n";
    }
    @Override
    public String A_TOTAL_OF() {
        return "總共 %d 個";
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
                return "啤";
            case FLUSH:
                return "花";
            case SINGLE:
                return "單張";
            case TRIPLE:
                return "三條";
            case STRAIGHT:
                return "蛇";
            case FULL_HOUSE:
                return "夫佬";
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
        return "出事！我完咗場遊戲先！";
    }

    @Override
    public String NEXT_GAME_QUEUED(String name) {
        return String.format("當遊戲喺 %s 度開始嗰陣，我會叫你", name);
    }

    @Override
    public String GAME_STARTING_IN(String name) {
        return String.format("遊戲即將喺 %s 開始", name);
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
