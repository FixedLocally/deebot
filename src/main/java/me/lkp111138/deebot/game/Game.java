package me.lkp111138.deebot.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import me.lkp111138.deebot.DeeBot;
import me.lkp111138.deebot.Main;
import me.lkp111138.deebot.game.achievement.Achievement;
import me.lkp111138.deebot.game.card.Cards;
import me.lkp111138.deebot.game.card.Cards.Card;
import me.lkp111138.deebot.misc.EmptyCallback;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Game {
    private final GroupInfo groupInfo;
    private int chips;
    private int turnWait;
    private int currentTurn = 0;
    private int autopassCount = 0;
    private long startTime;
    private long gid;
    private Chat group;
    private List<Card> currentProposal = new ArrayList<>();
    private boolean started = false;
    private boolean firstRound = true;
    private boolean allPassed = false;
    private List<User> players = new ArrayList<>();
    private Card[][] cards = new Card[4][CARDS_PER_PLAYER];
    private Card[][] startingCards = new Card[4][CARDS_PER_PLAYER];
    private int[] deckMsgid = new int[4];
    private boolean[] sortBySuit = new boolean[]{false, false, false, false};
    private Card[] deskCards = new Card[0];
    private HandInfo deskInfo;
    private User deskUser;
    private ScheduledFuture future;
    private int currentMsgid;
    private int largestSingleObgligation = -1;
    private String lang;
    private me.lkp111138.deebot.translation.Translation translation;
    private JsonObject gameSequence = new JsonObject();
    private int id;
    private String broadcastMessageCache = "";
    private ScheduledFuture sendMessageFuture;

    private boolean ended = false;
    private int[] offsets;

    private static Map<Long, Game> games = new HashMap<>();
    public static boolean maintMode = false; // true=disallow starting games
    private static TelegramBot bot;
    private static ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(2);
    private static Map<Integer, Game> uidGames = new HashMap<>();
    private static final int CARDS_PER_PLAYER = 13;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static int[] remindSeconds = new int[]{15, 30, 60, 90, 120, 180};

    public static void init(TelegramBot _bot) {
        bot = _bot;
    }

    public static Game byGroup(long gid) {
        return games.get(gid);
    }

    public Game(Message msg, int chips, int wait, GroupInfo groupInfo) throws ConcurrentGameException {
        // LOGIC :EYES:
        long gid = msg.chat().id();
        if (games.containsKey(gid)) {
            throw new ConcurrentGameException(this);
        }
        this.chips = chips;
        this.gid = gid;
        this.turnWait = groupInfo.waitTime;
        this.groupInfo = groupInfo;
        this.group = msg.chat();
        this.lang = DeeBot.lang(gid);
        this.translation = me.lkp111138.deebot.translation.Translation.get(this.lang);
        if (maintMode) {
            // disallow starting games
            this.execute(new SendMessage(gid, this.translation.MAINT_MODE_NOTICE()).parseMode(ParseMode.HTML), new EmptyCallback<>());
            return;
        }
        // refuse to start games on 5th oct 2019
        long now = System.currentTimeMillis();
        if (now >= 1570204800000L && now <= 1570291200000L) {
            this.execute(new SendMessage(gid, this.translation.JOIN_69_PROTEST() + this.translation.OCT_5_STRIKE()).parseMode(ParseMode.HTML), new EmptyCallback<>());
            return;
        }
        // create game
        try (Connection conn = Main.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO games (gid, chips) values (?, ?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, gid);
            stmt.setInt(2, chips);
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1);
                }
                else {
                    throw new SQLException("Creating game failed, no ID obtained.");
                }
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            this.execute(new SendMessage(msg.chat().id(), this.translation.ERROR() + e.getMessage()).replyToMessageId(msg.messageId()));
        }
        // notify queued users
        try (Connection conn = Main.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT tgid FROM next_game where gid=?");
            stmt.setLong(1, gid);
            ResultSet rs = stmt.executeQuery();
            String startMsg = this.translation.GAME_STARTING_IN(msg.chat().title());
            while (rs.next()) {
                SendMessage send = new SendMessage(rs.getInt(1), startMsg);
                this.execute(send);
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            this.execute(new SendMessage(msg.chat().id(), this.translation.ERROR() + e.getMessage()).replyToMessageId(msg.messageId()));
        }
        String[] checkCross = new String[]{"\uD83D\uDEAB", "\u2705"};
        this.execute(new SendMessage(gid, String.format(this.translation.GAME_START_ANNOUNCEMENT(), msg.from().id(), msg.from().firstName(), wait, checkCross[groupInfo.collectPlace ? 1 : 0], checkCross[groupInfo.fry ? 1 : 0], checkCross[1], id)).parseMode(ParseMode.HTML), new EmptyCallback<>());
        games.put(gid, this);
        addPlayer(msg);
        startTime = System.currentTimeMillis() + wait * 1000;
        int i;
        i = 0;
        while (wait > remindSeconds[i]) {
            ++i;
        }
//        this.log("scheduled remind task");
        schedule(this::remind, (wait - remindSeconds[--i]) * 1000);
        this.logf("Game created in %s [%d]", msg.chat().title(), msg.chat().id());
    }

    public static Game byUser(int tgid) {
        return uidGames.get(tgid);
    }

    public static RunInfo runInfo() {
        int total = games.size();
        int players = uidGames.size();
        int running = 0;
        for (Game game : games.values()) {
            if (game.started) {
                ++running;
            }
        }
        return new RunInfo(running, total, players, games);
    }

    public void addPlayer(Message msg) {
        // if a player is in a dead game, remove them
        User from = msg.from();
        Game g = uidGames.get(from.id());
        if (g != null && g.ended) {
            uidGames.remove(from.id());
        }
        // add to player list
        if (!started && !players.contains(from) && !uidGames.containsKey(from.id()) && playerCount() < 4) {
            // notify player
            // .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{}
            SendMessage send = new SendMessage(msg.from().id(), String.format(this.translation.JOIN_SUCCESS(), msg.chat().title().replace("*", "\\*"), this.id)).parseMode(ParseMode.Markdown);
            if (msg.chat().username() != null) {
                send.replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(this.translation.BACK_TO() + msg.chat().title()).url("https://t.me/" + msg.chat().username())}));
            }
            this.execute(send, new Callback<SendMessage, SendResponse>() {
                @Override
                public void onResponse(SendMessage request, SendResponse response) {
                    if (response.isOk()) {
                        // notify group
                        // we only actually add the player to the group if we can deliver the pm
                        if (playerCount() >= 4 || players.contains(msg.from())) {
                            // oops, you're the 5th player bye
                            return;
                        }
                        players.add(msg.from());
                        uidGames.put(msg.from().id(), Game.this);
                        int count = playerCount();
                        Game.this.execute(new SendMessage(msg.chat().id(), String.format(Game.this.translation.JOINED_ANNOUNCEMENT(), msg.from().id(), msg.from().firstName(), count)).parseMode(ParseMode.HTML), new EmptyCallback<>());
//                        Game.this.logf("%d / 4 players joined", count);
                        if (count == 4) {
                            // k we now got 4 players
                            start();
                        }
                    } else {
                        // for some reason we cant deliver the msg to the user, so we ask them to start me in the group
                        Game.this.execute(new SendMessage(msg.chat().id(), Game.this.translation.START_ME_FIRST())
                                .replyToMessageId(msg.messageId())
                                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(Game.this.translation.START_ME()).url("https://t.me/" + Main.getConfig("bot.username"))})));
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
            return uidGames.remove(tgid) != null;
        }
        return false;
    }

    public List<User> getPlayers() {
        return players;
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
        cancelFuture();
        startTime += 30000;
        startTime = Math.min(startTime, System.currentTimeMillis() + 180000);
        long seconds = (startTime - System.currentTimeMillis() + 999) / 1000;
        this.execute(new SendMessage(gid, String.format(this.translation.EXTENDED_ANNOUNCEMENT(), seconds)));
        int i;
        i = 0;
        while (i < 6 && remindSeconds[i] < seconds) {
            ++i;
        }
//        this.log("scheduled remind task");
        schedule(this::remind, (seconds - remindSeconds[--i]) * 1000);
    }

    public boolean callback(CallbackQuery callbackQuery) {
        int tgid = callbackQuery.from().id();
        String payload = callbackQuery.data();
        String[] args = payload.split(":");
        AnswerCallbackQuery answer = new AnswerCallbackQuery(callbackQuery.id());
        int i = -1;
        boolean processed = false;
        for (int j = 0; j < 4; j++) {
            if (players.get(j).id() == tgid) {
                i = j;
                break;
            }
        }
        log(payload);
        switch (args[0]) {
            case "propose":
                if (tgid != players.get(currentTurn).id()) {
                    this.execute(new EditMessageReplyMarkup(players.get(i).id(), deckMsgid[i]));
                    return true;
                }
                // we check if the player actually have the cards, if not, reset
                if (args.length > 1) {
                    Card proposed = Card.valueOf(args[1]);
                    if (currentProposal.contains(proposed)) {
                        logf("contains %s", proposed);
                        currentProposal.remove(proposed);
                    } else {
                        logf("not contains %s", proposed);
                        currentProposal.add(proposed);
                    }
                    currentProposal.sort(Comparator.comparingInt(Enum::ordinal));
                } else {
                    currentProposal = new ArrayList<>();
                }
                for (Card c : currentProposal) {
                    if (Arrays.binarySearch(cards[currentTurn], c) < 0) {
                        // non existent card!
                        currentProposal = new ArrayList<>();
                    }
                }
                // ok, proposal valid
                InlineKeyboardMarkup inlineKeyboardMarkup = buttonsFromDeck();
                this.execute(new EditMessageReplyMarkup(callbackQuery.message().chat().id(), callbackQuery.message().messageId()).replyMarkup(inlineKeyboardMarkup), new EmptyCallback<>());
                processed = true;
                break;
            case "play":
                if (tgid != players.get(currentTurn).id()) {
                    this.execute(new EditMessageReplyMarkup(players.get(i).id(), deckMsgid[i]));
                    return true;
                }
                if (args.length > 1) {
                    Card[] hand = currentProposal.toArray(new Card[0]);
                    play(hand, answer, callbackQuery);
                    updateDeck(i);
                }
                processed = true;
                break;
            case "pass":
                if (tgid != players.get(currentTurn).id()) {
                    this.execute(new EditMessageReplyMarkup(players.get(i).id(), deckMsgid[i]));
                    return true;
                }
                pass(answer, callbackQuery);
                processed = true;
                break;
            case "update":
                // updates the deck
                updateDeck(i);
                processed = true;
                break;
            case "sort":
                if ("suit".equals(args[1])) {
                    i = getPlayerIndexFromQuery(callbackQuery);
                    if (i < 4) {
                        List<Card> _cards = Arrays.asList(cards[i]);
                        _cards.sort(Comparator.comparingInt(card -> card.getSuit().ordinal()));
                        this.execute(new EditMessageText(callbackQuery.from().id(), callbackQuery.message().messageId(), this.translation.YOUR_DECK() + replaceAllSuits(String.join(" ", _cards))).replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(this.translation.SORT_FACE()).callbackData("sort:face")})));
                        _cards.sort(Comparator.comparingInt(Enum::ordinal));
                    }
                    sortBySuit[i] = true;
                } else {
                    i = getPlayerIndexFromQuery(callbackQuery);
                    if (i < 4) {
                        this.execute(new EditMessageText(callbackQuery.from().id(), callbackQuery.message().messageId(), this.translation.YOUR_DECK() + replaceAllSuits(String.join(" ", cards[i]))).replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(this.translation.SORT_SUIT()).callbackData("sort:suit")})));
                    }
                    sortBySuit[i] = false;
                }
                processed = true;
                break;
        }
        return processed;
    }

    private int getPlayerIndexFromQuery(CallbackQuery callbackQuery) {
        int i;
        i = 0;
        for (; i < 4; ++i) {
            if (players.get(i).id().equals(callbackQuery.from().id())) {
                break;
            }
        }
        return i;
    }

    private void updateDeck(int i) {
        if (sortBySuit[i]) {
            List<Card> _cards = Arrays.asList(cards[i]);
            _cards.sort(Comparator.comparingInt(card -> card.getSuit().ordinal()));
            this.execute(new EditMessageText(players.get(i).id(), deckMsgid[i], this.translation.YOUR_DECK() + replaceAllSuits(String.join(" ", _cards))).replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(this.translation.SORT_FACE()).callbackData("sort:face")})));
            _cards.sort(Comparator.comparingInt(Enum::ordinal));
        } else {
            this.execute(new EditMessageText(players.get(i).id(), deckMsgid[i], this.translation.YOUR_DECK() + replaceAllSuits(String.join(" ", cards[i]))).replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(this.translation.SORT_SUIT()).callbackData("sort:suit")})));
        }
    }

    private void pass(AnswerCallbackQuery answer, CallbackQuery callbackQuery) {
        if (!firstRound) {
            if (!allPassed) {
                if (callbackQuery != null) {
                    this.execute(new EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), this.translation.PASS()));
                    // notify group too
                }
                // we can cancel the job here cuz we no longer need auto pass for this round
                cancelFuture(); // cancel that auto pass job
                // cancel their obligation if they dont have a eligible card
                Card[] currentDeck = cards[currentTurn];
                if (cards[(currentTurn + 1) & 3].length == 1 && deskInfo.type == HandType.SINGLE) {
                    if (currentDeck[currentDeck.length - 1].ordinal() < deskInfo.leading.ordinal()) {
                        largestSingleObgligation = -1;
                    } else {
                        largestSingleObgligation = currentTurn;
                    }
                } else {
                    largestSingleObgligation = -1;
                }
                User currentPlayer = players.get(currentTurn);
                if (currentPlayer.username() != null) {
                    this.execute(new SendMessage(group.id(), String.format(this.translation.PASS_ANNOUNCEMENT_LINK(), currentPlayer.username(), currentPlayer.firstName())).parseMode(ParseMode.HTML).disableWebPagePreview(true));
                } else {
                    this.execute(new SendMessage(group.id(), String.format(this.translation.PASS_ANNOUNCEMENT(), currentPlayer.firstName())).parseMode(ParseMode.HTML).disableWebPagePreview(true));
                }
                currentTurn = (currentTurn + 1) & 3;
                startTurn();
            } else {
                answer.showAlert(true).text(this.translation.PASS_ON_EMPTY());
            }
        } else {
            answer.showAlert(true).text(this.translation.PASS_ON_FIRST());
        }
    }

    private void play(Card[] hand, AnswerCallbackQuery answer, CallbackQuery callbackQuery) {
        Arrays.sort(hand);
        HandInfo info = new HandInfo(hand);
        if (info.compare(deskInfo) && info.type != HandType.NONE) {
            Card[] currentDeck = cards[currentTurn];
            // k hand valid, now remove cards from the player's deck and next turn
            // but check if the player is playing diamond 3 for the first turn
            if (firstRound) {
                // the player must play diamond 3
                if (hand[0] != Card.D3) {
                    answer.showAlert(true).text(this.translation.NO_D3_ON_FIRST());
                    return;
                }
            }
            // we can cancel the job here cuz we no longer need auto pass for this round
            cancelFuture(); // cancel that auto pass job
            firstRound = false;
            allPassed = false;
            cancelFuture();
            for (Card card : hand) {
                currentDeck[Arrays.binarySearch(currentDeck, card)] = Card.ON99;
                Arrays.sort(cards[currentTurn]);
            }
            String cardStr = replaceAllSuits(String.join(" ", hand));
            if (callbackQuery != null) {
                autopassCount = 0;
                this.execute(new EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), cardStr), new EmptyCallback<>());
                // lets update the game sequence
                JsonArray array = gameSequence.getAsJsonArray("sequence");
                if (array == null) {
                    array = new JsonArray();
                }
                JsonObject turn = new JsonObject();
                turn.addProperty("player", players.get(currentTurn).id());
                JsonArray played = new JsonArray();
                for (Card card : hand) {
                    played.add(card.toString());
                }
                turn.add("played", played);
                array.add(turn);
                gameSequence.add("sequence", array);
            }
            User currentPlayer = players.get(currentTurn);
            broadcastMessageCache += "\n\n";
            if (currentPlayer.username() != null) {
                broadcastMessageCache += String.format(this.translation.PLAYED_ANNOUNCEMENT_LINK(), currentPlayer.username(), currentPlayer.firstName()) + cardStr;
            } else {
                broadcastMessageCache += String.format(this.translation.PLAYED_ANNOUNCEMENT(), currentPlayer.firstName()) + cardStr;
            }
            if (currentDeck.length == hand.length) {
                // player won
                cards[currentTurn] = new Card[0];
                end();
            } else {
                // remove the placeholders from player deck
                Card[] newDeck = new Card[cards[currentTurn].length - hand.length];
                int i = 0;
                int j = 0;
                for (; i < newDeck.length; ) {
                    if (cards[currentTurn][j] != Card.ON99) {
                        newDeck[i++] = cards[currentTurn][j];
                    }
                    j++;
                }
                cards[currentTurn] = newDeck;
                Arrays.sort(cards[currentTurn]);
                // check if the current played the largest possible hand, skip all if so
                int currentMax = 0;
                for (int k = 0; k < 4; k++) {
                    if (currentMax < cards[k][cards[k].length - 1].ordinal()) {
                        currentMax = cards[k][cards[k].length - 1].ordinal();
                    }
                }
                if ((info.leading.ordinal() >= currentMax) && (info.type == HandType.SINGLE || info.type == HandType.PAIR || info.type == HandType.TRIPLE)) {
                    deskCards = new Card[0];
                    deskUser = null;
                    deskInfo = null;
                    // if this skip all happens then obviously the player has used their largest card
                    // so they shouldnt be subject to largest card obligation
                    largestSingleObgligation = -1;
                } else {
                    deskCards = hand;
                    deskUser = players.get(currentTurn);
                    deskInfo = info;
                    // check the large card obligation and remove it if they used their largest card
                    if (cards[(currentTurn + 1) & 3].length == 1 && info.type == HandType.SINGLE) {
                        if (info.leading.ordinal() > newDeck[newDeck.length - 1].ordinal()) {
                            largestSingleObgligation = -1;
                        } else {
                            largestSingleObgligation = currentTurn;
                        }
                    } else {
                        largestSingleObgligation = -1;
                    }
                    currentTurn = (currentTurn + 1) & 3;
                }
                updateDeck(currentTurn);
                // wait a bit before starting the next turn
                executor.schedule(this::startTurn, 500, TimeUnit.MILLISECONDS);
            }
        } else {
            // alert them for invalid hand or hand to small
            if (info.type == HandType.NONE) {
                answer.showAlert(true).text(this.translation.INVALID_HAND());
            } else {
                answer.showAlert(true).text(this.translation.SMALL_HAND());
            }
        }
    }

    private void end() {
        // current player won
        // notify the group
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(this.translation.WON_ANNOUNCEMENT(), players.get(currentTurn).id(), players.get(currentTurn).firstName()));
        int[] deckLengths = new int[]{cards[0].length, cards[1].length, cards[2].length, cards[3].length};
        if (groupInfo.fry) {
            for (int i = 0; i < deckLengths.length; i++) {
                switch (deckLengths[i]) {
                    case 13:
                        deckLengths[i] *= 4;
                        break;
                    case 12:
                    case 11:
                    case 10:
                        deckLengths[i] *= 3;
                        break;
                    case 9:
                    case 8:
                        deckLengths[i] *= 2;
                        break;
                }
            }
        }
        int cardTotal = deckLengths[0] + deckLengths[1] + deckLengths[2] + deckLengths[3];
        offsets = new int[4];
        for (int i = 0; i < 4; ++i) {
            if (groupInfo.collectPlace) {
                offsets[i] = -4 * deckLengths[i] + cardTotal;
            } else {
                offsets[i] = i == currentTurn ? chips * cardTotal : -chips * deckLengths[i];
            }
        }
//        this.log(largestSingleObgligation);
        if (largestSingleObgligation != -1) {
            // oops someone gonna pay for their mistake
            for (int i = 0; i < 4; i++) {
                if (offsets[i] < 0 && largestSingleObgligation != i) {
                    offsets[largestSingleObgligation] += offsets[i];
                    offsets[i] = 0;
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("<a href=\"tg://user?id=%d\">%s</a> - %d [%s%d]\n", players.get(i).id(), players.get(i).firstName(), cards[i].length, offsets[i] >= 0 ? "+" : "", offsets[i]));
        }
        sb.append(this.translation.NEW_GAME_PROMPT());
        String msg = sb.toString();
        this.execute(new SendMessage(group.id(), msg).parseMode(ParseMode.HTML));
        started = false;
        kill();
        // update database chip records
        try (Connection conn = Main.getConnection()) {
            for (int i = 0; i < 4; ++i) {
                boolean won = i == currentTurn;
                PreparedStatement stmt = conn.prepareStatement("update tg_users set chips=chips+?, won_count=won_count+?, lost_cards=lost_cards+?, won_cards=won_cards+?, game_count=game_count+1 where tgid=?");
                stmt.setInt(1, offsets[i]);
                stmt.setInt(2, won ? 1 : 0);
                stmt.setInt(3, won ? 0 : cards[i].length);
                stmt.setInt(4, won ? cards[0].length + cards[1].length + cards[2].length + cards[3].length : 0);
                stmt.setInt(5, players.get(i).id());
                stmt.execute();
                stmt.close();
            }
            JsonArray turnout = new JsonArray();
            for (int i = 0; i < 4; ++i) {
                JsonObject _turnout = new JsonObject();
                _turnout.addProperty("player", players.get(i).id());
                _turnout.addProperty("offset", offsets[i]);
                JsonArray deck = new JsonArray();
                for (Card card : cards[i]) {
                    deck.add(card.toString());
                }
                _turnout.add("remaining", deck);
                turnout.add(_turnout);
            }
            gameSequence.add("turnout", turnout);
            PreparedStatement stmt = conn.prepareStatement("update games set game_sequence=? where id=?");
            stmt.setInt(2, id);
            stmt.setString(1, gameSequence.toString());
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // achievements
        for (int i = 0; i < 4; i++) {
            Achievement.executeAchievements(bot, new GameResult(), players.get(i).id(), i);
        }
    }

    private void kill(boolean isError) {
        this.log("Game ended");
        if (started) {
            // the game is forcibly killed, kill aht buttons of the current player too
            if (isError) {
                this.execute(new EditMessageText(players.get(currentTurn).id(), currentMsgid, this.translation.GAME_ENDED_ERROR()));
            } else {
                this.execute(new EditMessageText(players.get(currentTurn).id(), currentMsgid, this.translation.GAME_ENDED()));
            }
            for (int i = 0; i < 4; ++i) {
                this.execute(new EditMessageReplyMarkup(players.get(i).id(), deckMsgid[i]));
            }
        }
        cancelFuture();
        // remove this game instance
        this.execute(new SendMessage(gid, this.translation.GAME_ENDED_ANNOUNCEMENT()), new EmptyCallback<>());
        for (int i = 0; i < playerCount(); i++) {
            uidGames.remove(players.get(i).id());
        }
        games.remove(gid);
        ended = true;
    }

    public void kill() {
        kill(false);
    }

    private void start() {
        cancelFuture();
        started = true;
        if (playerCount() < 4) {
            kill();
            return;
        }
        // shuffle deck
        // for each hand, sort them so we can easily find diamond 3
        this.log("Game starting");
        while (true) {
            Card[] deck = Cards.shuffle();
            for (int i = 0; i < 4; i++) {
                System.arraycopy(deck, i * 13, this.cards[i], 0, CARDS_PER_PLAYER);
                Arrays.sort(this.cards[i]);
//            this.logf("%s %s %s\n", this.cards[i]);
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
        JsonArray startingDecks = new JsonArray();
        int j = 0;
        for (Card[] deck : cards) {
            JsonObject current = new JsonObject();
            current.addProperty("player", players.get(j).id());
            ++j;
            JsonArray _cards = new JsonArray();
            for (Card card : deck) {
                _cards.add(card.toString());
            }
            current.add("deck", _cards);
            startingDecks.add(current);
        }
        for (int i = 0; i < 4; i++) {
            System.arraycopy(cards[i], 0, startingCards[i], 0, 13);
        }
        gameSequence.add("starting_decks", startingDecks);
        this.execute(new SendMessage(gid, String.join(" > ", order)).parseMode(ParseMode.HTML), new EmptyCallback<>());
        // pm players with their cards
        for (int i = 0; i < 4; i++) {
            // 0th player gets 0th deck, etc
//            StringBuilder sb = new StringBuilder();
//            for (int j = 0; j < 13; j++) {
//                sb.append(cards[i][j].name());
//            }
            int finalI = i;
            this.execute(new SendMessage(players.get(i).id(), this.translation.STARTING_DECK() + replaceAllSuits(String.join(" ", cards[i]))), new EmptyCallback<>());
            this.execute(new SendMessage(players.get(i).id(), this.translation.YOUR_DECK() + replaceAllSuits(String.join(" ", cards[i]))).replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(this.translation.SORT_SUIT()).callbackData("sort:suit")})), new Callback<SendMessage, SendResponse>() {
                @Override
                public void onResponse(SendMessage request, SendResponse response) {
                    deckMsgid[finalI] = response.message().messageId();
                }

                @Override
                public void onFailure(SendMessage request, IOException e) {

                }
            });
        }
        // ask first player to play
        startTurn();
    }

    private void startTurn() {
        // ask the current player to play, while sending them the necessary info
        currentProposal = new ArrayList<>();
        if (deskUser == players.get(currentTurn)) {
            allPassed = true;
            deskUser = null;
            deskCards = new Card[0];
            deskInfo = null;
        }
        // we check if the player actually enough number of cards to counter the last hand
//        Game.this.log("starting turn " + currentTurn);
        if (deskInfo != null && cards[currentTurn].length < deskInfo.cardCount()) {
            pass(new AnswerCallbackQuery("0"), null);
        } else {
            // prompt the player for cards
            this.execute(new SendMessage(players.get(currentTurn).id(), this.translation.YOUR_TURN_PROMPT() + (allPassed || firstRound ? this.translation.THERE_IS_NOTHING() : (": " + replaceAllSuits(String.join("  ", deskCards))))).replyMarkup(buttonsFromDeck()), new Callback<SendMessage, SendResponse>() {
                @Override
                public void onResponse(SendMessage request, SendResponse response) {
                    currentMsgid = response.message().messageId();
                    // schedule when we are sure that the msg is sent successfully
//                    Game.this.log("scheduled auto pass job for turn " + currentTurn);
                    schedule(Game.this::autopass, turnWait * 1000);
                }

                @Override
                public void onFailure(SendMessage request, IOException e) {
                    e.printStackTrace();
                }
            });
            // notify group
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; ++i) {
                User p = players.get((currentTurn + i) & 3);
                if (p.username() != null) {
                    sb.append(String.format("<a href=\"https://t.me/%s\">%s</a> - %d\n", p.username(), p.firstName(), cards[(currentTurn + i) & 3].length));
                } else {
                    sb.append(String.format("%s - %d\n", p.firstName(), cards[(currentTurn + i) & 3].length));
                }
            }
            if (deskCards == null || deskCards.length == 0) {
                sb.append(this.translation.NOTHING_ON_DESK());
            } else {
                if (deskUser.username() != null) {
                    sb.append(String.format(this.translation.ON_DESK_LINK(), replaceAllSuits(String.join("  ", deskCards)), deskUser.username(), deskUser.firstName()));
                } else {
                    sb.append(String.format(this.translation.ON_DESK(), replaceAllSuits(String.join("  ", deskCards)), deskUser.firstName()));
                }
            }
            sb.append(String.format(this.translation.YOUR_TURN_ANNOUNCEMENT(), players.get(currentTurn).id(), players.get(currentTurn).firstName(), turnWait));
            String msg = sb.toString();
            SendMessage send = new SendMessage(gid, broadcastMessageCache + "\n\n" + msg).parseMode(ParseMode.HTML).disableWebPagePreview(true);
            broadcastMessageCache = "";
            if (sendMessageFuture != null) {
                sendMessageFuture.cancel(true);
                sendMessageFuture = null;
            }
            send.replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{new InlineKeyboardButton(this.translation.PICK_CARDS()).url("https://t.me/" + Main.getConfig("bot.username"))}));
            this.execute(send);
        }
    }

    private InlineKeyboardMarkup buttonsFromDeck() {
        Card[] deck = cards[currentTurn];
        int rows = 3 + deck.length / 4;
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[rows][0];
        for (int i = 0; i < rows - 2; ++i) {
            buttons[i] = new InlineKeyboardButton[Math.min(4, deck.length - 4 * i)];
            for (int j = 4 * i; j < Math.min(4 * i + 4, deck.length); ++j) {
                boolean chosen = currentProposal.contains(deck[j]);
//                logf("contains %s: %s", deck[j], chosen);
                buttons[i][j - 4 * i] = new InlineKeyboardButton(replaceAllSuits((chosen ? "\u2705 " : "") + deck[j].name())).callbackData("propose:" + deck[j]);
            }
        }
        String btnPlay;
        if (currentProposal.size() == 0) {
            btnPlay = this.translation.CHOOSE_SOME_CARDS();
        } else {
            HandInfo info = new HandInfo(currentProposal.toArray(new Card[0]));
            btnPlay = this.translation.HAND_TYPE(info.type) + replaceAllSuits(String.join(" ", currentProposal));
        }
        buttons[rows - 2] = new InlineKeyboardButton[]{new InlineKeyboardButton(btnPlay).callbackData("play:" + currentProposal)};
        buttons[rows - 1] = new InlineKeyboardButton[]{new InlineKeyboardButton(this.translation.PASS()).callbackData("pass")};
        return new InlineKeyboardMarkup(buttons);
    }

    private String replaceAllSuits(String in) {
        return in.replaceAll("D", " \u2666\ufe0f ").replaceAll("C", " \u2663\ufe0f ").replaceAll("H", " \u2764\ufe0f ").replaceAll("S", " \u2660\ufe0f ").trim();
    }

    private void remind() {
//        this.log("firing remind task");
        long seconds = (startTime - System.currentTimeMillis()) / 1000;
        this.execute(new SendMessage(gid, String.format(this.translation.JOIN_PROMPT(), Math.round(seconds / 15.0) * 15)));
        int i;
        i = 0;
        while (remindSeconds[i] < seconds) {
            ++i;
        }
        if (i > 0) {
//            this.log("scheduled remind task");
            schedule(this::remind, startTime - System.currentTimeMillis() - remindSeconds[--i] * 1000);
        } else {
//            this.log(String.format("scheduled kill task after %d seconds", seconds));
            schedule(this::kill, startTime - System.currentTimeMillis());
        }
    }

    private void cancelFuture() {
        if (future != null && !future.isDone() && !future.isCancelled()) {
//            this.log("cancelled task");
            future.cancel(true);
            future = null;
//            try {
//                throw new Exception();
//            } catch (Exception e) {
//                this.log(e.getStackTrace()[1]);
//            }
        }
    }

    private void schedule(Runnable runnable, long l) {
        cancelFuture();
        future = executor.schedule(runnable, l, TimeUnit.MILLISECONDS);
    }

    public String getLang() {
        return lang;
    }

    private void autopass() {
        // time is up for current round
//        this.log("firing auto pass job for turn " + currentTurn);
        ++autopassCount;
        this.execute(new EditMessageText(players.get(currentTurn).id(), currentMsgid, this.translation.TIMES_UP()));
        if (autopassCount < 8) {
            if (firstRound) {
                play(new Cards.Card[]{Cards.Card.D3}, new AnswerCallbackQuery("0"), null);
            } else {
                if (allPassed) {
                    // play the smallest single
                    play(new Cards.Card[]{cards[currentTurn][0]}, new AnswerCallbackQuery("0"), null);
                } else {
                    pass(new AnswerCallbackQuery("0"), null);
                }
            }
        } else {
            this.execute(new SendMessage(gid, this.translation.AFK_KILL()));
            kill();
        }
    }

    private void log(Object o) {
        String date = sdf.format(new Date());
        System.out.printf("[%s][Game %d] %s\n", date, id, o);
    }

    private void logf(String format, Object ...objs) {
        String date = sdf.format(new Date());
        Object[] _objs = new Object[objs.length + 2];
        _objs[1] = id;
        _objs[0] = date;
        System.arraycopy(objs, 0, _objs, 2, objs.length);
        System.out.printf("[%s][Game %d] " + format + "\n", _objs);
    }

    private <T extends BaseRequest<T, R>, R extends BaseResponse> void execute(T request) {
        this.execute(request, null);
    }

    private <T extends BaseRequest<T, R>, R extends BaseResponse> void execute(T request, Callback<T, R> callback) {
        this.execute(request, callback, 0);
    }

    private <T extends BaseRequest<T, R>, R extends BaseResponse> void execute(T request, Callback<T, R> callback, int failCount) {
        Map<String, Object> params = request.getParameters();
        boolean willThrottle = request instanceof SendMessage && params.get("reply_markup") == null && params.get("chat_id").toString().equals(String.valueOf(gid));
        if (request instanceof SendMessage) {
            ((SendMessage) request).parseMode(ParseMode.HTML).disableWebPagePreview(true);
        }
        Runnable execute = () -> {
            if (willThrottle) {
                broadcastMessageCache = "";
                sendMessageFuture = null;
            }
            bot.execute(request, new Callback<T, R>() {
                @Override
                public void onResponse(T request, R response) {
                    if (callback != null) {
                        callback.onResponse(request, response);
                    }
                }

                @Override
                public void onFailure(T request, IOException e) {
                    if (callback != null) {
                        callback.onFailure(request, e);
                    }
                    Game.this.logf("HTTP Error: %s %s\n", e.getClass().toString(), e.getMessage());
                    e.printStackTrace();
                    if (failCount < 5) { // linear backoff, max 5 retries
                        new Thread(() -> {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ignored) {
                            }
                            Game.this.execute(request, callback, failCount + 1);
                        }).start();
                    } else {
                        Game.this.kill(true);
                    }
                }
            });
        };
        if (willThrottle) {
            // is broadcast and not keyboard
            if (sendMessageFuture != null) {
                sendMessageFuture.cancel(true);
                sendMessageFuture = null;
            }
            broadcastMessageCache += "\n\n" + params.get("text");
            params.put("text", broadcastMessageCache);
            sendMessageFuture = executor.schedule(execute, 1000, TimeUnit.MILLISECONDS);
        } else {
            execute.run();
        }
    }

    public class GameResult {
        private GameResult() {
        }

        /**
         * @return remaining cards for players
         */
        public Card[][] getEndDecks() {
            return Game.this.cards;
        }

        /**
         * @return starting cards for players
         */
        public Card[][] getStartDecks() {
            return Game.this.startingCards;
        }

        /**
         * @return the user id's participated in the game
         */
        public int[] getPlayers() {
            int[] uids = new int[4];
            for (int i = 0; i < 4; i++) {
                uids[i] = players.get(i).id();
            }
            return uids;
        }

        public int[] getOffsets() {
            return offsets;
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
                    if (isFlush(cards) && isStraight(cards)) {
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
                    if (isFlush(cards)) {
                        type = HandType.FLUSH;
                        leading = cards[4];
                        break;
                    }
                    // straight
                    if (isStraight(cards)) {
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

        private static boolean isFlush(Card[] cards) {
            for (int i = 1; i < cards.length; ++i) {
                if (cards[i].getSuit() != cards[i - 1].getSuit()) {
                    return false;
                }
            }
            return true;
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

        private static boolean isStraight(Card[] cards) {
            // 3 4 5 6 7, ... , 10 J Q K A
            // A 2 3 4 5, 2 3 4 5 6
            // 3 4 5 A 2, 3 4 5 6 2
            for (int i = 1; i < cards.length; i++) {
                if ((cards[i].getFace() - cards[i - 1].getFace()) != 1) {
                    if ((cards[i].getFace() != 11) || (cards[i - 1].getFace() != 2)) {
                        if ((cards[i].getFace() != 12) || (cards[i - 1].getFace() != 3)) {
//                            this.logf("%s %s %s %s %s false\n", cards[0], cards[1], cards[2], cards[3], cards[4]);
                            return false;
                        }
                    }
                }
            }
            //                Game.this.logf("%s %s %s %s %s true\n", cards[0], cards[1], cards[2], cards[3], cards[4]);
            //                Game.this.logf("%s %s %s %s %s false\n", cards[0], cards[1], cards[2], cards[3], cards[4]);
            return cards[2].getFace() != 10;
        }

        int cardCount() {
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
    }

    public static class RunInfo {
        public final int runningCount;
        public final int gameCount;
        public final int playerCount;
        public final Map<Long, Game> games;

        RunInfo(int runningCount, int gameCount, int playerCount, Map<Long, Game> games) {
            this.runningCount = runningCount;
            this.gameCount = gameCount;
            this.playerCount = playerCount;
            this.games = games;
        }
    }

    public enum HandType {
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
