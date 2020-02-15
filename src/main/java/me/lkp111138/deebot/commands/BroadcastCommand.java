package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import me.lkp111138.deebot.Main;
import me.lkp111138.deebot.misc.EmptyCallback;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BroadcastCommand implements Command {
    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        if (msg.chat().id() != Main.BOT_OWNER) {
            return;
        }
        String message = msg.text().substring(11);
        List<Long> groups = new ArrayList<>();
        try {
            Connection conn = Main.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("select gid from `groups`");
            while (rs.next()) {
                groups.add(rs.getLong(1));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (Long group : groups) {
            bot.execute(new SendMessage(group, message), new EmptyCallback<>());
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
