package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.ForwardMessage;
import me.lkp111138.deebot.Main;

public class FeedbackCommand implements Command {
    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        if (msg.replyToMessage() != null) {
            // not replied, forward the command
            msg = msg.replyToMessage();
        }
        ForwardMessage req = new ForwardMessage(Main.BOT_OWNER, msg.chat().id(), msg.messageId());
        bot.execute(req);
    }
}
