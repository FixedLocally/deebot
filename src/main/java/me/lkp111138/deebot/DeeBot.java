package me.lkp111138.deebot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.AnswerPreCheckoutQuery;
import com.pengrad.telegrambot.request.SendMessage;
import io.sentry.Sentry;
import me.lkp111138.deebot.commands.*;
import me.lkp111138.deebot.game.Game;
import me.lkp111138.deebot.game.achievement.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DeeBot {
    private final static int COMMAND_COUNT_THRESHOLD = 10;
    private final static int COMMAND_INTERVAL = 15;

    private final Command fallback = new FallbackCommand();
    private final Map<String, Command> commands = new HashMap<>();
    private final TelegramBot bot;
    private final Map<Long, int[]> commandTimestamps = new HashMap<>();

    private static Map<Long, String> group_lang = new HashMap<>();
    private static Map<Long, Ban> bans = new HashMap<>();

    DeeBot(TelegramBot bot) {
        this.bot = bot;
        init();
    }

    public static boolean executeBan(long tgid, String type, int length, String reason) {
        if (length < 0) {
            return executeUnban(tgid);
        }
        try (Connection conn = Main.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT into bans (tgid, until, count, type, reason) VALUES (?, ?, ?, ?, ?)");
            int expiry = (int) (System.currentTimeMillis() / 1000) + length;
            stmt.setLong(1, tgid);
            stmt.setInt(2, expiry);
            stmt.setInt(3, 0);
            stmt.setString(4, type);
            stmt.setString(5, reason);
            stmt.execute();
            stmt.close();
            Ban ban = new Ban(tgid, expiry, type, reason);
            Ban oldBan = bans.get(tgid);
            if (oldBan == null || oldBan.expiry < ban.expiry) {
                bans.put(tgid, ban);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void processUpdate(Update update) {
        // get update type
        Message msg = update.message();
        CallbackQuery query = update.callbackQuery();
        PreCheckoutQuery preCheckoutQuery = update.preCheckoutQuery();
        if (msg != null) {
            long from = msg.from().id();
            MessageEntity[] entities = msg.entities();
            long sender = msg.from().id();
            if (msg.migrateFromChatId() != null) {
                // migrate settings if any
                try (Connection conn = Main.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("update `groups` set gid=? where gid=?");
                    stmt.setLong(1, msg.migrateFromChatId());
                    stmt.setLong(2, msg.chat().id());
                    stmt.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (msg.newChatMembers() != null) {
                for (User user : msg.newChatMembers()) {
                    if (user.id().toString().equals(Main.getConfig("bot.uid"))) {
                        Ban ban = queryBanObject(msg.chat().id());
                        if (ban == null) {
                            return;
                        }
                        if (ban.reason.equals("kick autoban")) {
                            bot.execute(new SendMessage(msg.chat().id(), String.format("The bot was just kicked from this group therefore there is a cooldown applied to this group. Please wait %1$s minutes before trying to start a game. Sorry for inconvenience.\n本bot剛才被本群組移除，因此需要等待%1$s分鐘才能開始新遊戲。不便之處，謹此致歉。", (ban.expiry - System.currentTimeMillis() / 1000) / 60 + 1)));
                        }
                    }
                }
            }
            if (msg.leftChatMember() != null && msg.leftChatMember().id().toString().equals(Main.getConfig("bot.uid"))) {
                System.out.printf("was kicked in group %s [%s], 10min ban applied\n", msg.chat().id(), msg.chat().title());
                // is kicked, ban for 10mins
                executeBan(msg.chat().id(), "COMMAND", 600, "kick autoban");
                // kill existing game
                Game g = Game.byGroup(msg.chat().id());
                if (g != null) {
                    g.kill();
                }
            }
            if (entities != null && entities.length > 0 && entities[0].type().equals(MessageEntity.Type.bot_command)) {
                int offset = entities[0].offset();
                String command = msg.text().substring(offset + 1);
                String[] segments = command.split(" ");
                String[] cmd = segments[0].split("@");
                if (cmd.length > 1 && !cmd[1].equals(Main.getConfig("bot.username"))) {
                    // command not intended for me
                    return;
                }
                if (!cmd[0].equals("terms")) { // still allow access to /terms
                    // blacklist
                    if ("COMMAND".equals(queryBan(sender))) {
                        return;
                    }
                    if ("COMMAND".equals(queryBan(msg.chat().id()))) {
                        return;
                    }
                }
                try {
                    // rate limiting
                    int[] times = commandTimestamps.getOrDefault(from, new int[COMMAND_COUNT_THRESHOLD + 1]);
                    int index = (times[COMMAND_COUNT_THRESHOLD] + 1) % COMMAND_COUNT_THRESHOLD;
                    int date = times[index];
                    times[index] = msg.date();
                    commandTimestamps.put(from, times);
//                    System.out.println(Arrays.toString(times));
                    int s = times[index] - date;
                    if (s < COMMAND_INTERVAL && s >= 0) {
                        // ok boomer
                        try (Connection conn = Main.getConnection()) {
                            PreparedStatement stmt = conn.prepareStatement("select max(count) from bans where reason='spam cmd autoban' and tgid=?");
                            stmt.setLong(1, from);
                            ResultSet rs = stmt.executeQuery();
                            int count = 0;
                            if (rs.next()) {
                                count = rs.getInt(1);
                            }
                            int secs = (int) (1800 * Math.pow(2, count));
                            executeBan(from, "COMMAND", secs, "spam cmd autoban");
                            bot.execute(new SendMessage(msg.chat().id(), String.format("Spamming detected. You have been banned for %s seconds.", secs)));
                        }
                    } else {
                        commands.getOrDefault(cmd[0], fallback).respond(bot, msg, segments);
                    }
                    times[COMMAND_COUNT_THRESHOLD] = index;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        if (query != null) {
            Game g = Game.byUser(query.from().id());
            if (g != null) {
                try {
                    if (g.callback(query)) {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (ConfigCommand.callback(bot, query)) {
                return;
            }
            if (SetLangCommand.callback(bot, query)) {
                return;
            }
            if (NextGameCommand.callback(bot, query)) {
                return;
            }
            System.out.println("unknown query: " + query.data());
            bot.execute(new AnswerCallbackQuery(query.id()));
            return;
        }
        if (preCheckoutQuery != null) {
            bot.execute(new AnswerPreCheckoutQuery(preCheckoutQuery.id()));
            return;
        }
        System.out.println("unknown update: " + update.toString());
    }

    public static String lang(long gid) {
        return group_lang.computeIfAbsent(gid, aLong -> {
            try (Connection conn = Main.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT lang FROM `groups` WHERE gid=?");
                stmt.setLong(1, gid);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString(1);
                } else {
                    return "en";
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "en";
            }
        });
    }

    public static void setLang(long gid, String lang) {
        try (Connection conn = Main.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("update `groups` set lang=? where gid=?");
            stmt.setString(1, lang);
            stmt.setLong(2, gid);
            group_lang.put(gid, lang);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Query user or group ban status
     * @param tgid the id to be queried
     * @return the type of ban, or null if none
     */
    public static String queryBan(long tgid) {
        Ban ban = bans.get(tgid);
        if (ban == null) {
            return null;
        }
        if (ban.expiry > System.currentTimeMillis() / 1000) {
            return ban.type;
        } else {
            return null;
        }
    }

    /**
     * Query user or group ban status
     *
     * @param tgid the id to be queried
     * @return the type of ban, or null if none
     */
    public static Ban queryBanObject(long tgid) {
        Ban ban = bans.get(tgid);
        if (ban != null && ban.expiry < System.currentTimeMillis() / 1000) {
            return ban;
        } else {
            return null;
        }
    }

    private static boolean executeUnban(long tgid) {
        try (Connection conn = Main.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("update bans set until=unix_timestamp() where until>unix_timestamp() and tgid=?");
            stmt.setLong(1, tgid);
            stmt.execute();
            stmt.close();
            bans.remove(tgid);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void init() {
        this.bot.setUpdatesListener(list -> {
            for (Update update : list) {
                try {
                    processUpdate(update);
                } catch (Throwable e) {
                    Sentry.capture(e);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        // commands
        commands.put("start", new StartCommand());
        commands.put("play", new PlayCommand());
        commands.put("startgame", commands.get("play"));
        commands.put("join", new JoinCommand());
        commands.put("flee", new FleeCommand());
        commands.put("stats", new StatCommand());
        commands.put("killgame", new KillGameCommand());
        commands.put("extend", new ExtendCommand());
        commands.put("config", new ConfigCommand());
        commands.put("maintmode", new MaintModeCommand());
        commands.put("runinfo", new RunInfoCommand());
        commands.put("setlang", new SetLangCommand());
        commands.put("help", new HelpCommand());
        commands.put("feedback", new FeedbackCommand());
        commands.put("achv", new AchvCommand());
        commands.put("toggle69", new Toggle69Command());
        commands.put("forcestart", new ForceStartCommand());
        commands.put("fs", commands.get("forcestart"));
        commands.put("donate", new DonateCommand());
        commands.put("nextgame", new NextGameCommand());
        commands.put("bana", new BanCommand("ADMIN"));
        commands.put("banc", new BanCommand("COMMAND"));
        commands.put("terms", new TermsCommand());
        commands.put("broadcast", new BroadcastCommand());

        // achievements
        Achievement.registerAchievement(new FirstGameAchievement());
        Achievement.registerAchievement(new FirstWinAchievement());
        Achievement.registerAchievement(new PlayWithMintAchievement());
        Achievement.registerAchievement(new FirstBloodAchievement());
        Achievement.registerAchievement(new RookieAchievement());
        Achievement.registerAchievement(new FamiliarizedAchievement());
        Achievement.registerAchievement(new AddictedAchievement());
        Achievement.registerAchievement(new AmateurAchievement());
        Achievement.registerAchievement(new AdeptAchievement());
        Achievement.registerAchievement(new ExpertAchievement());
        Achievement.registerAchievement(new LoseItAllAchievement());
        Achievement.registerAchievement(new DeepFriedAchievement());

        // bans
        try (PreparedStatement stmt = Main.getConnection().prepareStatement("SELECT tgid, until, type, reason from bans WHERE until>?")) {
            stmt.setInt(1, (int) (System.currentTimeMillis() / 1000));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                long tgid = rs.getLong(1);
                Ban oldBan = bans.get(tgid);
                Ban ban = new Ban(rs.getLong(1), rs.getInt(2), rs.getString(3), rs.getString(4));
                if (oldBan == null || oldBan.expiry < ban.expiry) {
                    bans.put(tgid, ban);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Ban {
        public final long tgid;
        public final int expiry;
        public final String type;
        public final String reason;

        private Ban(long tgid, int expiry, String type, String reason) {
            this.tgid = tgid;
            this.expiry = expiry;
            this.type = type;
            this.reason = reason;
        }

        @Override
        public String toString() {
            return "Ban{" +
                    "tgid=" + tgid +
                    ", expiry=" + expiry +
                    ", type='" + type + '\'' +
                    '}';
        }
    }
}
