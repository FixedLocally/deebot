package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import me.lkp111138.deebot.game.Game;

public class RunInfoCommand implements BaseCommand {
    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        Game.RunInfo runInfo = Game.runInfo();
        String text = String.format("Total games: %d\nRunning games: %d\nPlayers: %d", runInfo.game_count, runInfo.running_count, runInfo.player_count);
        bot.execute(new SendMessage(msg.chat().id(), text));
    }
}
