package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import me.lkp111138.deebot.game.Game;

public class FleeCommand implements BaseCommand {
    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        Game g = Game.byGroup(msg.chat().id());
        if (g != null && g.removePlayer(msg.from().id())) {
            int remaining = g.playerCount();
            bot.execute(new SendMessage(msg.chat().id(), String.format("<a href=\"tg://user?id=%d\">%s</a> has fled from the game! %d player%s remaining.", msg.from().id(), msg.from().firstName(), remaining, remaining != 1 ? "s" : "")).parseMode(ParseMode.HTML).replyToMessageId(msg.messageId()));
        }
    }
}
