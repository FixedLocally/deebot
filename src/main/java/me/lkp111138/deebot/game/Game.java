package me.lkp111138.deebot.game;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import me.lkp111138.deebot.DeeBot;
import me.lkp111138.deebot.Main;
import me.lkp111138.deebot.Translation;
import me.lkp111138.deebot.game.card.Cards;
import me.lkp111138.deebot.game.card.Cards.Card;
import me.lkp111138.deebot.misc.EmptyCallback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Game {
    private final GroupInfo groupInfo;
    private int chips;
    private int turn_wait;
    private int current_turn = 0;
    private int autopass_count = 0;
    private long start_time;
    private long gid;
    private Chat group;
    private String current_proposal = "";
    private boolean started = false;
    private boolean first_round = true;
    private boolean all_passed = false;
    private List<User> players = new ArrayList<>();
    private Card[][] cards = new Card[4][CARDS_PER_PLAYER];
    private int[] deck_msgid = new int[4];
    private boolean[] sort_by_suit = new boolean[]{false, false, false, false};
    private Card[] desk_cards = new Card[0];
    private HandInfo desk_info;
    private User desk_user;
    private ScheduledFuture future;
    private int current_msgid;
    private int largest_single_obgligation = -1;
    private String lang;

    private static Map<Long, Game> games = new HashMap<>();
    private static Map<Integer, Game> uid_games = new HashMap<>();
    private static DeeBot bot;
    private static ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(2);
    private static int[] remind_seconds = new int[]{15, 30, 60, 90, 120, 180};
    private static final int CARDS_PER_PLAYER = 13;

    public static void init(DeeBot _bot) {
        bot = _bot;
    }

    public static Game byGroup(long gid) {
        return games.get(gid);
    }

    public static Game byUser(int tgid) {
        return uid_games.get(tgid);
    }

    public Game(Message msg, int chips, int wait, GroupInfo groupInfo) throws ConcurrentGameException {
        // LOGIC :EYES:
        long gid = msg.chat().id();
        if (games.containsKey(gid)) {
            throw new ConcurrentGameException(this);
        }
        this.chips = chips;
        this.gid = gid;
        this.turn_wait = groupInfo.wait_time;
        this.groupInfo = groupInfo;
        this.group = msg.chat();
        this.lang = DeeBot.lang(gid);
        try (Connection conn = Main.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("insert into games (gid, chips) values (?, ?)");
            stmt.setLong(1, gid);
            stmt.setInt(2, chips);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            bot.execute(new SendMessage(msg.chat().id(), getTranslation("ERROR") + e.getMessage()).replyToMessageId(msg.messageId()));
        }
        bot.execute(new SendMessage(gid, String.format(getTranslation("GAME_START_ANNOUNCEMENT"), msg.from().id(), msg.from().firstName(), wait)).parseMode(ParseMode.HTML), new EmptyCallback<>());
        games.put(gid, this);
        addPlayer(msg);
        start_time = System.currentTimeMillis() + wait * 1000;
        int i;
        i = 0;
        while (wait > remind_seconds[i]) {
            ++i;
        }
        System.out.println("scheduled remind task");
        schedule(this::remind, (wait - remind_seconds[--i]) * 1000);
    }

    public void addPlayer(Message msg) {
        // add to player list
        if (!started && !players.contains(msg.from()) && !uid_games.containsKey(msg.from().id()) && playerCount() < 4) {
            // notify player
            // .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{}
            SendMessage send = new SendMessage(msg.from().id(), String.format(getTranslation("JOIN_SUCCESS"), msg.chat().title().replace("*", "\\*"))).parseMode(ParseMode.Markdown);
            if (msg.chat().username() != null) {
                send.replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(getTranslation("BACK_TO") + msg.chat().title()).url("https://t.me/" + msg.chat().username())}));
            }
            bot.execute(send, new Callback<SendMessage, SendResponse>() {
                @Override
                public void onResponse(SendMessage request, SendResponse response) {
                    if (response.isOk()) {
                        // notify group
                        // we only actually add the player to the group if we can deliver the pm
                        players.add(msg.from());
                        uid_games.put(msg.from().id(), Game.this);
                        int count = playerCount();
                        bot.execute(new SendMessage(msg.chat().id(), String.format(getTranslation("JOINED_ANNOUNCEMENT"), msg.from().id(), msg.from().firstName(), count)).parseMode(ParseMode.HTML), new EmptyCallback<>());
                        if (count == 4) {
                            // k we now got 4 players
                            start();
                        }
                    } else {
                        // for some reason we cant deliver the msg to the user, so we ask them to start me in the group
                        bot.execute(new SendMessage(msg.chat().id(), getTranslation("START_ME_FIRST")).replyToMessageId(msg.messageId()).replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(getTranslation("START_ME")).url("https://t.me/" + System.getProperty("bot.username"))})));
                    }
                }

                @Override
                public void onFailure(SendMessage request, IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public boolean removePlayer(int tgid) {
        // if not in game then do nothing
        // otherwise remove them
        if (!started && players.removeIf(user -> user.id() == tgid)) {
            return uid_games.remove(tgid) != null;
        }
        return false;
    }

    public int playerCount() {
        return players.size();
    }

    public boolean started() {
        return started;
    }

    public void extend() {
        if (started) {
            return;
        }
        cancel_future();
        start_time += 30000;
        start_time = Math.min(start_time, System.currentTimeMillis() + 180000);
        long seconds = (start_time - System.currentTimeMillis() + 999) / 1000;
        bot.execute(new SendMessage(gid, String.format(getTranslation("EXTENDED_ANNOUNCEMENT"), seconds)));
        int i;
        i = 0;
        while (i < 6 && remind_seconds[i] < seconds) {
            ++i;
        }
        System.out.println("scheduled remind task");
        schedule(this::remind, (seconds - remind_seconds[--i]) * 1000);
    }

    public void callback(CallbackQuery callbackQuery) {
        int tgid = callbackQuery.from().id();
        String payload = callbackQuery.data();
        System.out.println(payload);
        String[] args = payload.split(":");
        AnswerCallbackQuery answer = new AnswerCallbackQuery(callbackQuery.id());
        cancel_future();
        switch (args[0]) {
            case "propose":
                if (tgid != players.get(current_turn).id()) {
                    return;
                }
                // we check if the player actually have the cards, if not, reset
                Card[] proposed = new Card[0];
                if (args.length > 1) {
                    proposed = map_to_card(args[1].split("_"));
                    current_proposal = args[1];
                } else {
                    current_proposal = "";
                }
                for (Card c : proposed) {
                    if (Arrays.binarySearch(cards[current_turn], c) < 0) {
                        // non existent card!
                        current_proposal = "";
                    }
                }
                // ok, proposal valid
                InlineKeyboardMarkup inlineKeyboardMarkup = buttons_from_deck();
                bot.execute(new EditMessageReplyMarkup(callbackQuery.message().chat().id(), callbackQuery.message().messageId()).replyMarkup(inlineKeyboardMarkup), new EmptyCallback<>());
                break;
            case "play":
                if (tgid != players.get(current_turn).id()) {
                    return;
                }
                if (args.length > 1) {
                    Card[] hand = map_to_card(args[1].split("_"));
                    play(hand, answer, callbackQuery, args);
                }
                break;
            case "pass":
                if (tgid != players.get(current_turn).id()) {
                    return;
                }
                pass(answer, callbackQuery);
                break;
            case "update":
                // updates the deck
                int i = 0;
                for (; i < 4; ++i) {
                    if (players.get(i).id().equals(callbackQuery.from().id())) {
                        break;
                    }
                }
                if (i < 4) {
                    if (sort_by_suit[i]) {
                        List<Card> _cards = Arrays.asList(cards[i]);
                        _cards.sort(Comparator.comparingInt(card -> card.getSuit().ordinal()));
                        bot.execute(new EditMessageText(callbackQuery.from().id(), callbackQuery.message().messageId(), getTranslation("YOUR_DECK") + replace_all_suits(String.join(" ", _cards))).replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(getTranslation("UPDATE_DECK")).callbackData("update")}, new InlineKeyboardButton[]{new InlineKeyboardButton(getTranslation("SORT_FACE")).callbackData("sort:face")})));
                        _cards.sort(Comparator.comparingInt(Enum::ordinal));
                    } else {
                        bot.execute(new EditMessageText(callbackQuery.from().id(), callbackQuery.message().messageId(), getTranslation("YOUR_DECK") + replace_all_suits(String.join(" ", cards[i]))).replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(getTranslation("UPDATE_DECK")).callbackData("update")}, new InlineKeyboardButton[]{new InlineKeyboardButton(getTranslation("SORT_SUIT")).callbackData("sort:suit")})));
                    }
                }
                break;
            case "sort":
                switch (args[1]) {
                    case "suit":
                        i = 0;
                        for (; i < 4; ++i) {
                            if (players.get(i).id().equals(callbackQuery.from().id())) {
                                break;
                            }
                        }
                        if (i < 4) {
                            List<Card> _cards = Arrays.asList(cards[i]);
                            _cards.sort(Comparator.comparingInt(card -> card.getSuit().ordinal()));
                            bot.execute(new EditMessageText(callbackQuery.from().id(), callbackQuery.message().messageId(), getTranslation("YOUR_DECK") + replace_all_suits(String.join(" ", _cards))).replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(getTranslation("UPDATE_DECK")).callbackData("update")}, new InlineKeyboardButton[]{new InlineKeyboardButton(getTranslation("SORT_FACE")).callbackData("sort:face")})));
                            _cards.sort(Comparator.comparingInt(Enum::ordinal));
                        }
                        sort_by_suit[i] = true;
                        break;
                    default:
                        i = 0;
                        for (; i < 4; ++i) {
                            if (players.get(i).id().equals(callbackQuery.from().id())) {
                                break;
                            }
                        }
                        if (i < 4) {
                            bot.execute(new EditMessageText(callbackQuery.from().id(), callbackQuery.message().messageId(), getTranslation("YOUR_DECK") + replace_all_suits(String.join(" ", cards[i]))).replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(getTranslation("UPDATE_DECK")).callbackData("update")}, new InlineKeyboardButton[]{new InlineKeyboardButton(getTranslation("SORT_SUIT")).callbackData("sort:suit")})));
                        }
                        sort_by_suit[i] = false;
                        break;
                }

        }
        bot.execute(answer);
    }

    private void pass(AnswerCallbackQuery answer, CallbackQuery callbackQuery) {
        if (!first_round) {
            if (!all_passed) {
                if (callbackQuery != null) {
                    bot.execute(new EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), getTranslation("PASS")));
                    // notify group too
                }
                // cancel their obligation if they dont have a eligible card
                Card[] current_deck = cards[current_turn];
                if (cards[(current_turn + 1) & 3].length == 1 && desk_info.type == HandType.SINGLE) {
                    System.out.println("next player has 1 card and single on desk, checking obligation!");
                    if (current_deck[current_deck.length - 1].ordinal() < desk_info.leading.ordinal()) {
                        // they cant even try
                        System.out.println("they cant even try");
                        largest_single_obgligation = -1;
                    } else {
                        // they didnt try
                        System.out.println("they didnt try their best");
                        largest_single_obgligation = current_turn;
                    }
                } else {
                    largest_single_obgligation = -1;
                }
                current_turn = (current_turn + 1) & 3;
                start_turn();
            } else {
                answer.showAlert(true).text(getTranslation("PASS_ON_EMPTY"));
            }
        } else {
            answer.showAlert(true).text(getTranslation("PASS_ON_FIRST"));
        }
    }

    private void play(Card[] hand, AnswerCallbackQuery answer, CallbackQuery callbackQuery, String[] args) {
        Arrays.sort(hand);
        HandInfo info = new HandInfo(hand);
        if (info.compare(desk_info) && info.type != HandType.NONE) {
            final Card[] current_deck = cards[current_turn];
            // k hand valid, now remove cards from the player's deck and next turn
            // but check if the player is playing diamond 3 for the first turn
            if (first_round) {
                // the player must play diamond 3
                if (hand[0] != Card.D3) {
                    answer.showAlert(true).text(getTranslation("NO_D3_ON_FIRST"));
                    return;
                }
            }
            first_round = false;
            all_passed = false;
            cancel_future();
            for (Card card : hand) {
                current_deck[Arrays.binarySearch(current_deck, card)] = Card.ON99;
                Arrays.sort(cards[current_turn]);
            }
            if (callbackQuery != null) {
                autopass_count = 0;
                bot.execute(new EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), replace_all_suits(args[1].replaceAll("_", "  "))), new EmptyCallback<>());
            }
            User current_player = players.get(current_turn);
            if (current_player.username() != null) {
                bot.execute(new SendMessage(group.id(), String.format(getTranslation("PLAYED_ANNOUNCEMENT_LINK"), current_player.username(), current_player.firstName()) + replace_all_suits(args[1].replaceAll("_", "  "))).parseMode(ParseMode.HTML).disableWebPagePreview(true));
            } else {
                bot.execute(new SendMessage(group.id(), String.format(getTranslation("PLAYED_ANNOUNCEMENT"), current_player.firstName()) + replace_all_suits(args[1].replaceAll("_", "  "))).parseMode(ParseMode.HTML).disableWebPagePreview(true));
            }
            if (current_deck.length == hand.length) {
                // player won
                cards[current_turn] = new Card[0];
                end();
            } else {
                // remove the placeholders from player deck
                Card[] new_deck = new Card[cards[current_turn].length - hand.length];
                int i = 0;
                int j = 0;
                for (; i < new_deck.length; ) {
                    if (cards[current_turn][j] != Card.ON99) {
                        new_deck[i++] = cards[current_turn][j];
                    }
                    j++;
                }
                cards[current_turn] = new_deck;
                // check the large card obligation and remove it if they used their largest card
                if (cards[(current_turn + 1) & 3].length == 1 && info.type == HandType.SINGLE) {
                    System.out.println("next player has 1 card and single on desk, checking obligation!");
                    if (info.leading.ordinal() > current_deck[current_deck.length - 1].ordinal()) {
                        // they tried their best
                        System.out.println("current player tried their best");
                        largest_single_obgligation = -1;
                    } else {
                        System.out.printf("current player didnt try their best, leading=%s, largest=%s\n", info.leading, new_deck[new_deck.length - 1]);
                        largest_single_obgligation = current_turn;
                    }
                } else {
                    largest_single_obgligation = -1;
                }
                Arrays.sort(cards[current_turn]);
                if (info.leading == Card.S2 && (info.type == HandType.SINGLE || info.type == HandType.PAIR || info.type == HandType.TRIPLE)) {
                    desk_cards = new Card[0];
                    desk_user = null;
                    desk_info = null;
                } else {
                    desk_cards = hand;
                    desk_user = players.get(current_turn);
                    desk_info = info;
                    current_turn = (current_turn + 1) & 3;
                }
                start_turn();
            }
        } else {
            // alert them for invalid hand or hand to small
            if (info.type == HandType.NONE) {
                answer.showAlert(true).text(getTranslation("INVALID_HAND"));
            } else {
                answer.showAlert(true).text(getTranslation("SMALL_HAND"));
            }
        }
    }

    private void end() {
        // current player won
        // notify the group
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(getTranslation("WON_ANNOUNCEMENT"), players.get(current_turn).id(), players.get(current_turn).firstName()));
        int[] deck_lengths = new int[]{cards[0].length, cards[1].length, cards[2].length, cards[3].length};
        if (groupInfo.fry) {
            for (int i = 0; i < deck_lengths.length; i++) {
                switch (deck_lengths[i]) {
                    case 13:
                        deck_lengths[i] *= 4;
                        break;
                    case 12:
                    case 11:
                    case 10:
                        deck_lengths[i] *= 3;
                        break;
                    case 9:
                    case 8:
                        deck_lengths[i] *= 2;
                        break;
                }
            }
        }
        int card_total = deck_lengths[0] + deck_lengths[1] + deck_lengths[2] + deck_lengths[3];
        int[] offsets = new int[4];
        for (int i = 0; i < 4; ++i) {
            if (groupInfo.collect_place) {
                offsets[i] = -4 * deck_lengths[i] + card_total;
            } else {
                offsets[i] = i == current_turn ? chips * card_total : -chips * deck_lengths[i];
            }
        }
        System.out.println(largest_single_obgligation);
        if (largest_single_obgligation != -1) {
            // oops someone gonna pay for their mistake
            for (int i = 0; i < 4; i++) {
                if (offsets[i] < 0 && largest_single_obgligation != i) {
                    offsets[largest_single_obgligation] += offsets[i];
                    offsets[i] = 0;
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("<a href=\"tg://user?id=%d\">%s</a> - %d [%s%d]\n", players.get(i).id(), players.get(i).firstName(), cards[i].length, offsets[i] >= 0 ? "+" : "", offsets[i]));
        }
        sb.append(getTranslation("NEW_GAME_PROMPT"));
        String msg = sb.toString();
        bot.execute(new SendMessage(group.id(), msg).parseMode(ParseMode.HTML));
        started = false;
        kill();
        // update database chip records
        try (Connection conn = Main.getConnection()) {
            for (int i = 0; i < 4; ++i) {
                boolean won = i == current_turn;
                PreparedStatement stmt = conn.prepareStatement("update tg_users set chips=chips+?, won_count=won_count+?, lost_cards=lost_cards+?, won_cards=won_cards+?, game_count=game_count+1 where tgid=?");
                stmt.setInt(1, offsets[i]);
                stmt.setInt(2, won ? 1 : 0);
                stmt.setInt(3, won ? 0 : cards[i].length);
                stmt.setInt(4, won ? cards[0].length + cards[1].length + cards[2].length + cards[3].length : 0);
                stmt.setInt(5, players.get(i).id());
                stmt.execute();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void kill() {
        if (started) {
            // the game is forcibly killed, kill aht buttons of the current player too
            bot.execute(new EditMessageText(players.get(current_turn).id(), current_msgid, getTranslation("GAME_ENDED")));
            for (int i = 0; i < 4; ++i) {
                bot.execute(new EditMessageReplyMarkup(players.get(i).id(), deck_msgid[i]));
            }
        }
        cancel_future();
        // remove this game instance
        bot.execute(new SendMessage(gid, getTranslation("GAME_ENDED_ANNOUNCEMENT")), new EmptyCallback<>());
        for (int i = 0; i < playerCount(); i++) {
            uid_games.remove(players.get(i).id());
        }
        games.remove(gid);

    }

    private void start() {
        cancel_future();
        started = true;
        if (playerCount() < 4) {
            kill();
            return;
        }
        // shuffle deck
        // for each hand, sort them so we can easily find diamond 3
        while (true) {
            Card[] deck = Cards.shuffle();
            for (int i = 0; i < 4; i++) {
                System.arraycopy(deck, i * 13, this.cards[i], 0, CARDS_PER_PLAYER);
                Arrays.sort(this.cards[i]);
//            System.out.printf("%s %s %s\n", this.cards[i]);
            }
            // here, if determine if we need to re-shuffle
            boolean _break = true;
            for (int i = 0; i < 4; ++i) {
                if (cards[i][CARDS_PER_PLAYER - 1].ordinal() < 44 && cards[i][CARDS_PER_PLAYER - 3].ordinal() < 32 || cards[i][CARDS_PER_PLAYER - 4].ordinal() > 48) {
                    _break = false;
                }
            }
            if (_break) {
                break;
            }
        }
        // now we find the deck with diamond 3 and swap it to the front
        for (int i = 0; i < 4; i++) {
            if (cards[i][0] == Card.D3) {
                Card[] tmp = cards[0];
                cards[0] = cards[i];
                cards[i] = tmp;
            }
        }
        Collections.shuffle(players);
        // tell the group the order
        String[] order = new String[4];
        for (int i = 0; i < 4; i++) {
            order[i] = String.format("<a href=\"tg://user?id=%d\">%s</a>", players.get(i).id(), players.get(i).firstName());
        }
        cards[0][0] = Card.D3;
        bot.execute(new SendMessage(gid, String.join(" > ", order)).parseMode(ParseMode.HTML), new EmptyCallback<>());
        // pm players with their cards
        for (int i = 0; i < 4; i++) {
            // 0th player gets 0th deck, etc
//            StringBuilder sb = new StringBuilder();
//            for (int j = 0; j < 13; j++) {
//                sb.append(cards[i][j].name());
//            }
            int finalI = i;
            bot.execute(new SendMessage(players.get(i).id(), getTranslation("STARTING_DECK") + replace_all_suits(String.join(" ", cards[i]))), new EmptyCallback<>());
            bot.execute(new SendMessage(players.get(i).id(), getTranslation("YOUR_DECK") + replace_all_suits(String.join(" ", cards[i]))).replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(getTranslation("UPDATE_DECK")).callbackData("update")}, new InlineKeyboardButton[]{new InlineKeyboardButton(getTranslation("SORT_SUIT")).callbackData("sort:suit")})), new Callback<SendMessage, SendResponse>() {
                @Override
                public void onResponse(SendMessage request, SendResponse response) {
                    deck_msgid[finalI] = response.message().messageId();
                }

                @Override
                public void onFailure(SendMessage request, IOException e) {

                }
            });
        }
        // ask first player to play
        start_turn();
    }

    private void start_turn() {
        // ask the current player to play, while sending them the necessary info
        current_proposal = "";
        if (desk_user == players.get(current_turn)) {
            all_passed = true;
            desk_user = null;
            desk_cards = new Card[0];
            desk_info = null;
        }
        // we check if the player actually enough number of cards to counter the last hand
        if (desk_info != null && cards[current_turn].length < desk_info.card_count()) {
            pass(new AnswerCallbackQuery("0"), null);
        } else {
            // prompt the player for cards
            bot.execute(new SendMessage(players.get(current_turn).id(), getTranslation("YOUR_TURN_PROMPT") + (all_passed || first_round ? getTranslation("THERE_IS_NOTHING") : (": " + replace_all_suits(String.join("  ", desk_cards))))).replyMarkup(buttons_from_deck()), new Callback<SendMessage, SendResponse>() {
                @Override
                public void onResponse(SendMessage request, SendResponse response) {
                    current_msgid = response.message().messageId();

                }

                @Override
                public void onFailure(SendMessage request, IOException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("scheduled auto pass job");
            schedule(this::autopass, turn_wait * 1000 + 5000);
            // notify group
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; ++i) {
                User p = players.get((current_turn + i) & 3);
                if (p.username() != null) {
                    sb.append(String.format("<a href=\"https://t.me/%s\">%s</a> - %d\n", p.username(), p.firstName(), cards[(current_turn + i) & 3].length));
                } else {
                    sb.append(String.format("%s - %d\n", p.firstName(), cards[(current_turn + i) & 3].length));
                }
            }
            if (desk_cards == null || desk_cards.length == 0) {
                sb.append(getTranslation("NOTHING_ON_DESK"));
            } else {
                if (desk_user.username() != null) {
                    sb.append(String.format(getTranslation("ON_DESK_LINK"), replace_all_suits(String.join("  ", desk_cards)), desk_user.username(), desk_user.firstName()));
                } else {
                    sb.append(String.format(getTranslation("ON_DESK"), replace_all_suits(String.join("  ", desk_cards)), desk_user.firstName()));
                }
            }
            sb.append(String.format(getTranslation("YOUR_TURN_ANNOUNCEMENT"), players.get(current_turn).id(), players.get(current_turn).firstName()));
            String msg = sb.toString();
            SendMessage send = new SendMessage(gid, msg).parseMode(ParseMode.HTML).disableWebPagePreview(true);
            group.username();
            send.replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(getTranslation("PICK_CARDS")).url("https://t.me/" + System.getProperty("bot.username"))}));
            bot.execute(send);
        }
    }

    private Card[] map_to_card(String[] _proposed_cards) {
//        String[] _proposed_cards = in.split("_");
        List<Card> _proposed = new ArrayList<>();
        for (String _proposed_card : _proposed_cards) {
            try {
                _proposed.add(Card.valueOf(_proposed_card));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return _proposed.toArray(new Card[0]);
    }

    private InlineKeyboardMarkup buttons_from_deck() {
        Card[] deck = cards[current_turn];
        Card[] proposed_cards = map_to_card(current_proposal.split("_"));
        int rows = 3 + deck.length / 4;
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[rows][0];
        String _proposal = current_proposal.length() > 0 ? current_proposal + "_" : "";
        for (int i = 0; i < rows - 2; ++i) {
            buttons[i] = new InlineKeyboardButton[Math.min(4, deck.length - 4 * i)];
            for (int j = 4 * i; j < Math.min(4 * i + 4, deck.length); ++j) {
                Arrays.sort(proposed_cards);
                boolean chosen = Arrays.binarySearch(proposed_cards, deck[j]) >= 0;
                String new_proposal;
                if (chosen) {
                    new_proposal = _proposal.replace(deck[j].name() + "_", "");
                } else {
                    new_proposal = _proposal + deck[j].name();
                }
                buttons[i][j - 4 * i] = new InlineKeyboardButton(replace_all_suits((chosen ? "\u2705 " : "") + deck[j].name())).callbackData("propose:" + new_proposal.replaceAll("__", "_").replaceAll("^_?([^_]+)_?$", "$1"));
            }
        }
        Card[] proposal_cards = map_to_card(current_proposal.split("_"));
        Arrays.sort(proposal_cards);
        String btn_play = replace_all_suits(String.join("  ", proposal_cards));
        if (proposal_cards.length == 0) {
            btn_play = getTranslation("CHOOSE_SOME_CARDS");
        } else {
            HandInfo info = new HandInfo(proposal_cards);
            btn_play = info.type + btn_play;
        }
        buttons[rows - 2] =  new InlineKeyboardButton[]{new InlineKeyboardButton(btn_play).callbackData("play:" + current_proposal)};
        buttons[rows - 1] = new InlineKeyboardButton[]{new InlineKeyboardButton(getTranslation("PASS")).callbackData("pass")};
        return new InlineKeyboardMarkup(buttons);
    }

    private String replace_all_suits(String in) {
        return in.replaceAll("D", " \u2666\ufe0f ").replaceAll("C", " \u2663\ufe0f").replaceAll("H", " \u2764\ufe0f ").replaceAll("S", " \u2660\ufe0f ").trim();
    }

    private void remind() {
        System.out.println("firing remind task");
        long seconds = (start_time - System.currentTimeMillis()) / 1000;
        bot.execute(new SendMessage(gid, String.format(getTranslation("JOIN_PROMPT"), Math.round(seconds / 15.0) * 15)));
        int i;
        i = 0;
        while (remind_seconds[i] < seconds) {
            ++i;
        }
        if (i > 0) {
            System.out.println("scheduled remind task");
            schedule(this::remind, start_time - System.currentTimeMillis() - remind_seconds[--i] * 1000);
        } else {
            System.out.println(String.format("scheduled kill task after %d seconds", seconds));
            schedule(this::kill, start_time - System.currentTimeMillis());
        }
    }

    private void cancel_future() {
        if (future != null && !future.isDone() && !future.isCancelled()) {
            System.out.println("cancelled task");
            future.cancel(true);
            future = null;
        }
    }

    private void schedule(Runnable runnable, long l) {
        cancel_future();
        future = executor.schedule(runnable, l, TimeUnit.MILLISECONDS);
    }

    private String getTranslation(String key) {
        return Translation.get(lang, key);
    }

    public String getLang() {
        return lang;
    }

    private void autopass() {
        // time is up for current round
        System.out.println("firing auto pass job");
        ++autopass_count;
        bot.execute(new EditMessageText(players.get(current_turn).id(), current_msgid, getTranslation("TIMES_UP")));
        if (autopass_count < 8) {
            if (first_round) {
                play(new Cards.Card[]{Cards.Card.D3}, new AnswerCallbackQuery("0"), null, new String[]{"play", "D3"});
            } else {
                if (all_passed) {
                    // play the smallest single
                    play(new Cards.Card[]{cards[current_turn][0]}, new AnswerCallbackQuery("0"), null, new String[]{"play", cards[current_turn][0].toString()});
                } else {
                    pass(new AnswerCallbackQuery("0"), null);
                }
            }
        } else {
            bot.execute(new SendMessage(gid, getTranslation("AFK_KILL")));
            kill();
        }
    }


    public static class HandInfo {
        final HandType type;
        final Card leading;

        @Override
        public String toString() {
            return "HandInfo{" +
                    "type=" + type +
                    ", leading=" + leading +
                    '}';
        }


        int card_count() {
            switch (type) {
                case TRIPLE:
                    return 3;
                case PAIR:
                    return 2;
                case SINGLE:
                    return 1;
                default:
                    return 5;
            }
        }

        HandInfo(Card[] _cards) {
            Card[] cards = Arrays.copyOf(_cards, _cards.length);
            Arrays.sort(cards);
            switch (cards.length) {
                case 1:
                    type = HandType.SINGLE;
                    leading = cards[0];
                    break;
                case 2:
                    if (cards[0].getFace() == cards[1].getFace()) {
                        type = HandType.PAIR;
                        leading = cards[1];
                    } else {
                        type = HandType.NONE;
                        leading = null;
                    }
                    break;
                case 3:
                    if ((cards[0].getFace() == cards[1].getFace()) && (cards[2].getFace() == cards[1].getFace())) {
                        type = HandType.TRIPLE;
                        leading = cards[1];
                    } else {
                        type = HandType.NONE;
                        leading = null;
                    }
                    break;
                case 5:
                    // straight flush
                    if (is_flush(cards) && is_straight(cards)) {
                        type = HandType.STRAIGHT_FLUSH;
                        leading = cards[4];
                        break;
                    }
                    // 4 of a kind (aaaab/baaaa)
                    if ((cards[3].getFace() == cards[2].getFace()) && (cards[2].getFace() == cards[1].getFace())) {
                        if (cards[0].getFace() == cards[1].getFace()) {
                            type = HandType.FOUR_OF_A_KIND;
                            leading = cards[3];
                            break;
                        }
                        if (cards[4].getFace() == cards[1].getFace()) {
                            type = HandType.FOUR_OF_A_KIND;
                            leading = cards[4];
                            break;
                        }
                    }
                    // full house (aaabb/aabbb)
                    if ((cards[0].getFace() == cards[1].getFace()) && (cards[3].getFace() == cards[4].getFace())) {
                        if (cards[2].getFace() == cards[0].getFace()) {
                            type = HandType.FULL_HOUSE;
                            leading = cards[2];
                            break;
                        }
                        if (cards[2].getFace() == cards[4].getFace()) {
                            type = HandType.FULL_HOUSE;
                            leading = cards[4];
                            break;
                        }
                    }
                    // flush
                    if (is_flush(cards)) {
                        type = HandType.FLUSH;
                        leading = cards[4];
                        break;
                    }
                    // straight
                    if (is_straight(cards)) {
                        type = HandType.STRAIGHT;
                        leading = cards[4];
                        break;
                    }
                    type = HandType.NONE;
                    leading = null;
                    break;
                default:
                    type = HandType.NONE;
                    leading = null;
            }
        }

        private boolean compare(HandInfo other) {
            if (other == null) {
                return true;
            }
            switch (other.type) {
                case SINGLE:
                case PAIR:
                case TRIPLE:
                    if (other.type == type) {
                        return leading.ordinal() > other.leading.ordinal();
                    } else {
                        return false;
                    }
                case NONE:
                    return type != HandType.NONE;
                default:
                    if (type.ordinal() < other.type.ordinal()) {
                        return false;
                    }
                    if (type.ordinal() > other.type.ordinal()) {
                        return true;
                    }
                    return leading.ordinal() > other.leading.ordinal();
            }
        }

        private static boolean is_flush(Card[] cards) {
            for (int i = 1; i < cards.length; ++i) {
                if (cards[i].getSuit() != cards[i - 1].getSuit()) {
                    return false;
                }
            }
            return true;
        }

        private static boolean is_straight(Card[] cards) {
            // 3 4 5 6 7, ... , 10 J Q K A
            // A 2 3 4 5, 2 3 4 5 6
            // 3 4 5 A 2, 3 4 5 6 2
            for (int i = 1; i < cards.length; i++) {
                if ((cards[i].getFace() - cards[i - 1].getFace()) != 1) {
                    if ((cards[i].getFace() != 11) || (cards[i - 1].getFace() != 2)) {
                        if ((cards[i].getFace() != 12) || (cards[i - 1].getFace() != 3)) {
//                            System.out.printf("%s %s really no good\n", cards[i], cards[i - 1]);
//                            System.out.printf("%s %s\n", cards[i].getFace(), cards[i - 1].getFace());
                            System.out.printf("%s %s %s %s %s false\n", cards[0], cards[1], cards[2], cards[3], cards[4]);
                            return false;
                        }
                    }
                }
            }
            if (cards[2].getFace() != 10) {
                System.out.printf("%s %s %s %s %s true\n", cards[0], cards[1], cards[2], cards[3], cards[4]);
                return true;
            } else {
                System.out.printf("%s %s %s %s %s false\n", cards[0], cards[1], cards[2], cards[3], cards[4]);
                return false;
            }
        }
    }

    enum HandType {
        NONE,
        SINGLE,
        PAIR,
        TRIPLE,
        STRAIGHT,
        FLUSH,
        FULL_HOUSE,
        FOUR_OF_A_KIND,
        STRAIGHT_FLUSH
    }
}
