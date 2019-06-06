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

public class AchvCommand implements Command {
    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        User target = msg.from();
        if (msg.replyToMessage() != null) {
            target = msg.replyToMessage().from();
        }
        try (Connection conn = Main.getConnection()) {
            Translation translation = Translation.get(DeeBot.lang(msg.chat().id()));
            PreparedStatement stmt = conn.prepareStatement("SELECT achv FROM achv_log WHERE tgid=?");
            stmt.setInt(1, target.id());
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder(translation.ACHV_UNLOCKED());
            int count = 0;
            while (rs.next()) {
                ++count;
                sb.append(translation.ACHIEVEMENT_TITLE(rs.getString(1)));
            }
            sb.append(String.format(translation.A_TOTAL_OF(), count));
            bot.execute(new SendMessage(msg.chat().id(), sb.toString()).replyToMessageId(msg.messageId()).parseMode(ParseMode.Markdown));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
