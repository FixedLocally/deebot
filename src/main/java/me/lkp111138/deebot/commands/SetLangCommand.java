package me.lkp111138.deebot.commands;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import me.lkp111138.deebot.DeeBot;
import me.lkp111138.deebot.Main;
import me.lkp111138.deebot.translation.Translation;

import java.io.IOException;

public class SetLangCommand implements Command {
    private static void send(TelegramBot bot, Message msg) {
        long gid = msg.chat().id();
        long uid = msg.from().id();
        bot.execute(new SendMessage(gid, "語言設定 / Language Settings").replyMarkup(new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{new InlineKeyboardButton("繁體中文").callbackData("lang:zh:" + uid)},
                new InlineKeyboardButton[]{new InlineKeyboardButton("廣東話").callbackData("lang:hk:" + uid)},
                new InlineKeyboardButton[]{new InlineKeyboardButton("English").callbackData("lang:en:" + uid)},
                new InlineKeyboardButton[]{new InlineKeyboardButton(Translation.get(DeeBot.lang(gid)).CLOSE()).callbackData("lang:close:" + uid)}
        )));
    }

    public void respond(TelegramBot bot, Message msg, String[] args) {
        if (msg.from().id() == Main.BOT_OWNER || msg.chat().type() == Chat.Type.Private) {
            send(bot, msg);
            return;
        }
        bot.execute(new GetChatMember(msg.chat().id(), msg.from().id()), new Callback<GetChatMember, GetChatMemberResponse>() {
            @Override
            public void onResponse(GetChatMember request, GetChatMemberResponse response) {
                ChatMember chatMember = response.chatMember();
                if (chatMember.status() == ChatMember.Status.administrator || chatMember.status() == ChatMember.Status.creator) {
                    send(bot, msg);
                }
            }

            @Override
            public void onFailure(GetChatMember request, IOException e) {

            }
        });
    }

    public static boolean callback(TelegramBot bot, CallbackQuery query) {
        long tgid = query.from().id();
        long gid = query.message().chat().id();
        int mid = query.message().messageId();
        String payload = query.data();
        String[] args = payload.split(":");
        AnswerCallbackQuery answer = new AnswerCallbackQuery(query.id());
        if ("lang".equals(args[0])) {
            if (args.length > 2) {
                int from_id = Integer.parseInt(args[2]);
                if (tgid == from_id) {
                    // authorized
                    if (args[1].equals("close")) {
                        // delete the message
                        bot.execute(new DeleteMessage(gid, mid));
                    } else {
                        // update
                        DeeBot.setLang(gid, args[1]);
                        bot.execute(new DeleteMessage(gid, mid));
                    }
                }
            } else {
                answer.text("oof");
            }
            bot.execute(answer);
            return true;
        }
        return false;
    }
}
