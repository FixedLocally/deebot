package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import me.lkp111138.deebot.DeeBot;
import me.lkp111138.deebot.Translation;
import me.lkp111138.deebot.game.Game;

public class JoinCommand implements BaseCommand {
    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        // joins a game
        long gid = msg.chat().id();
        Game g = Game.byGroup(gid);
        if (g == null) {
            bot.execute(new SendMessage(msg.chat().id(), Translation.get(DeeBot.lang(gid), "NO_GAME_TO_JOIN")).replyToMessageId(msg.messageId()));
        } else {
            g.addPlayer(msg);
        }
    }
}
