package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import me.lkp111138.deebot.DeeBot;
import me.lkp111138.deebot.Main;
import me.lkp111138.deebot.translation.Translation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatCommand implements Command {
    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        User target = msg.from();
        if (msg.replyToMessage() != null) {
            target = msg.replyToMessage().from();
        }
        Translation translation;
        translation = Translation.get(DeeBot.lang(msg.chat().id()));
        try (Connection conn = Main.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT chips, won_cards, lost_cards, won_count, game_count FROM tg_users WHERE tgid=?");
            stmt.setInt(1, target.id());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (rs.getInt(5) > 0) {
                    String sb = translation.STAT(target.id(), target.firstName(), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(1));
                    bot.execute(new SendMessage(msg.chat().id(), sb).replyToMessageId(msg.messageId()).parseMode(ParseMode.HTML));
                } else {
                    bot.execute(new SendMessage(msg.chat().id(), "You haven't played a game yet!").replyToMessageId(msg.messageId()).parseMode(ParseMode.HTML));
                }
            } else {
                bot.execute(new SendMessage(msg.chat().id(), "You haven't played a game yet!").replyToMessageId(msg.messageId()).parseMode(ParseMode.HTML));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
