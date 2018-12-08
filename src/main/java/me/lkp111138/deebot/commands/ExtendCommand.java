package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import me.lkp111138.deebot.game.Game;

public class ExtendCommand implements BaseCommand {
    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        Game g = Game.byGroup(msg.chat().id());
        if (g != null) {
            g.extend();
        }
    }
}
