package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;

public class TermsCommand implements Command {
    private static String TERMS = "Terms of Service (Sorry English only, updated Feb 15, 2020):\n" +
            "0. If you have troubles understanding this text, use https://translate.google.com\n" +
            "1. By using @jokebig2bot (referred by \"the bot\" below), you agree to our terms.\n" +
            "2. You agree to not take any actions to gain chips unfairly, including but not limited to repeatedly playing with other self-controlled accounts.\n" +
            "3. You agree to not take any actions that may or will damage the integrity or efficiency of the systems that host the bot, including but not limit to spamming commands and compromising the bot's servers.\n" +
            "4. You further agree that you will not take any actions that may or will damage other players' interests within the bot.n\n" +
            "5. Being able to use this bot is a privilege, not a right and we reserve the right to block any user or group from accessing this bot for any reason. The reason behind this is that you do not pay for server costs and spend time to do maintenance so I don't need troubles from you.\n" +
            "6. The terms can be updated from time to time with or without prior notice, however they will be accessible by /terms\n" +
            "7. If there's any discrepancies between different versions of the terms, the English version shall prevail.\n";

    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        bot.execute(new SendMessage(msg.chat().id(), TERMS).replyToMessageId(msg.messageId()));
    }
}
