package me.lkp111138.deebot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.model.Update;
import me.lkp111138.deebot.commands.*;
import me.lkp111138.deebot.game.Game;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DeeBot {
    private final BaseCommand fallback = new FallbackCommand();
    private final Map<String, BaseCommand> commands = new HashMap<>();
    private final TelegramBot bot;

    private static Map<Long, String> group_lang = new HashMap<>();

    DeeBot(TelegramBot bot) {
        this.bot = bot;
        init();
    }

    private void init() {
        this.bot.setUpdatesListener(list -> {
            for (Update update : list) {
                processUpdate(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        commands.put("start", new StartCommand());
        commands.put("play", new PlayCommand());
        commands.put("startgame", commands.get("play"));
        commands.put("join", new JoinCommand());
        commands.put("flee", new FleeCommand());
        commands.put("stats", new StatCommand());
        commands.put("killgame", new KillGameCommand());
        commands.put("extend", new ExtendCommand());
    }

    private void processUpdate(Update update) {
        // get update type
        Message msg = update.message();
        CallbackQuery query = update.callbackQuery();
        if (msg != null) {
            MessageEntity[] entities = msg.entities();
            int sender = msg.from().id();
            // blacklist
            switch (sender) {
                case 290485640:
                case 654217056:
                case 665228326:
                case 246596279:
                case 37622951:
                    return;
            }
            if (entities != null && entities.length > 0 && entities[0].type().equals(MessageEntity.Type.bot_command)) {
                int offset = entities[0].offset();
                String command = msg.text().substring(offset + 1);
                String[] segments = command.split(" ");
                String[] cmd = segments[0].split("@");
                if (cmd.length > 1 && !cmd[1].equals(System.getProperty("bot.username"))) {
                    // command not intended for me
                    return;
                }
                try {
                    commands.getOrDefault(cmd[0], fallback).respond(bot, msg, segments);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (query != null) {
            Game g = Game.byUser(query.from().id());
            if (g != null) {
                try {
                    g.callback(query);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String lang(long gid) {
        return group_lang.computeIfAbsent(gid, aLong -> {
            try (Connection conn = Main.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("select lang from groups where gid=?");
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
}
