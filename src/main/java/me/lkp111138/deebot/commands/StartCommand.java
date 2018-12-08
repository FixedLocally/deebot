package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import me.lkp111138.deebot.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StartCommand implements BaseCommand {
    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        // init the user if needed
        // TODO
        try (Connection conn = Main.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("replace into tg_users (tgid, username) values (?, ?)");
            stmt.setInt(1, msg.from().id());
            stmt.setString(2, msg.from().username());
            stmt.execute();
            bot.execute(new SendMessage(msg.chat().id(), "Hello world!").replyToMessageId(msg.messageId()));
        } catch (SQLException e) {
            e.printStackTrace();
            bot.execute(new SendMessage(msg.chat().id(), "An error occured: " + e.getMessage()).replyToMessageId(msg.messageId()));
        }
    }
}
