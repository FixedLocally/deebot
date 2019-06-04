package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import me.lkp111138.deebot.Main;
import me.lkp111138.deebot.game.Game;

import java.io.IOException;

public class KillGameCommand implements Command {
    @Override
    public void respond(TelegramBot bot, Message msg, String[] args) {
        switch (msg.chat().type()) {
            case Private:
            case channel:
                return;
        }
        // blacklist
        //            case 401742123:
        //                break;
        // whitelist
        if (msg.from().id() == Main.BOT_OWNER) {// kill game
            if (Game.byGroup(msg.chat().id()) != null) {
                Game.byGroup(msg.chat().id()).kill();
            }
        } else {
            bot.execute(new GetChatMember(msg.chat().id(), msg.from().id()), new Callback<GetChatMember, GetChatMemberResponse>() {
                @Override
                public void onResponse(GetChatMember request, GetChatMemberResponse response) {
                    ChatMember chatMember = response.chatMember();
                    if (chatMember.status() == ChatMember.Status.administrator || chatMember.status() == ChatMember.Status.creator) {
                        // kill game
                        if (Game.byGroup(msg.chat().id()) != null) {
                            Game.byGroup(msg.chat().id()).kill();
                        }
                    }
                }

                @Override
                public void onFailure(GetChatMember request, IOException e) {

                }
            });
        }

    }
}
