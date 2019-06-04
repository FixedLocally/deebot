package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;

public interface Command {
    void respond(TelegramBot bot, Message msg, String[] args);
}
