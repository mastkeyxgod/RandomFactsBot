package ru.mastkey.randomfactsbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.mastkey.randomfactsbot.config.BotConfig;
import ru.mastkey.randomfactsbot.keyboard.KeyboardUtil;
import ru.mastkey.randomfactsbot.model.Subscriber;
import ru.mastkey.randomfactsbot.repository.SubscriberRepository;
import ru.mastkey.randomfactsbot.states.UserState;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Bot extends TelegramLongPollingBot {
    private final String START_TEXT = "Привет! Я бот случайных фактов. \uD83E\uDD16✨\n" +
            "\n" +
            "Чтобы узнать интересный факт, выбери \"Новый факт\" в меню ниже. Если хочешь подписаться на регулярную рассылку фактов, выбери \"Подписка\".\n";

    private final String HELP_TEXT = "Добро пожаловать в раздел помощи! \uD83E\uDD14❓\n" +
            "\n" +
            "Ты можешь использовать следующие команды:\n" +
            "\n" +
            "- /start: Начать взаимодействие с ботом.\n" +
            "- /help: Показать это сообщение.\n" +
            "- Новый факт: Получить случайный факт.\n" +
            "- Подписка: Управление подпиской на регулярные факты.\n" +
            "- Отписаться: Отменить подписку на рассылку фактов.\n" +
            "- Поменять время: Изменить время получения фактов по подписке.\n" +
            "- Назад: Вернуться в главное меню.\n" +
            "\n" +
            "Если в ответ на ваш запрос вы получили \"Ошибка в запросе.\", то попробуйте переформулировать запрос или поменять тему.\n" +
            "\n" +
            "Так как все факты генерируются через нейросеть, что позволяет отказаться от больших баз данных с фактами, то иногда они могут повторяться :). Так же иногда бывает, что нейросеть выдает неправдивый факт.\uD83C\uDF1F\n";
    private final String SUBSCRIBE_CMD = "Подписаться";
    private final String CHANGE_TIME_CMD = "Поменять время";
    private final String FACT_CMD = "Новый факт";
    private final String CANCEL = "Назад";

    private final String SUBSCRIBE = "Подписка";
    private final String HELP_CMD_DESCRIPTION = "";
    private final KeyboardUtil keyboardUtil;
    private final SubscriberRepository subscriberRepository;
    private final FactService factService;
    private final BotConfig botConfig;

    @Autowired
    public Bot(KeyboardUtil keyboardUtil, SubscriberRepository subscriberRepository, FactService factService, BotConfig botConfig) {
        this.keyboardUtil = keyboardUtil;
        this.subscriberRepository = subscriberRepository;
        this.factService = factService;
        this.botConfig = botConfig;
    }

    private Map<Long, UserState> userStates = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            Message message = update.getMessage();
            long chatId = message.getChatId();
            System.out.println(chatId);
            String text = message.getText();

            switch (text) {
                case SUBSCRIBE_CMD -> {

                    if (subscriberRepository.getSubscriberByChatId(chatId) != null) {
                        sendMessage(chatId, "Вы уже подписаны на рассылку фактов", keyboardUtil.getMainKeyboard());
                        return;
                    }

                    userStates.put(chatId, UserState.SUBSCRIBE);
                    sendMessage(chatId, "Введите желаемое время в формате 00:00 - 23:59. Например, 15:30", keyboardUtil.getCancelButton());

                }
                case CHANGE_TIME_CMD -> {

                    if (subscriberRepository.getSubscriberByChatId(chatId) == null) {
                        sendMessage(chatId, "Вы не подписаны на рассылку фактов", keyboardUtil.getMainKeyboard());
                        return;
                    }

                    userStates.put(chatId, UserState.CHANGE_TIME);

                    sendMessage(chatId, "Введите желаемое время в формате 00:00 - 23:59. Например, 15:30", keyboardUtil.getCancelButton());

                }
                case FACT_CMD -> {

                    userStates.put(chatId, UserState.FACT);
                    sendMessage(chatId, "Введите тему для факта или нажмите на кнопку \"Случайный факт\" ",
                            keyboardUtil.getFactKeyboard());

                }

                case CANCEL -> {
                    if (userStates.containsKey(chatId)) {
                        UserState userState = userStates.get(chatId);

                        switch (userState) {
                            case SUBSCRIBE -> {
                                userStates.remove(chatId);
                                sendMessage(chatId, "Вы перешли в раздел управления подпиской", keyboardUtil.getSubscribeMenuForNotRegisteredUser());
                                return;
                            }

                            case CHANGE_TIME -> {
                                userStates.remove(chatId);
                                sendMessage(chatId, "Вы перешли в раздел управления подпиской", keyboardUtil.getSubscribeMenuForRegisteredUser());
                                return;
                            }

                            case FACT -> {
                                userStates.remove(chatId);
                                sendMessage(chatId, "Главное меню", keyboardUtil.getMainKeyboard());
                                return;
                            }

                        }
                    }

                    sendMessage(chatId, "Главное меню", keyboardUtil.getMainKeyboard());

                }

                default -> {

                    if (userStates.containsKey(chatId)) {
                        handleUserState(chatId, text);
                    } else {
                        processCommands(chatId, text);
                    }

                }
            }
        }
    }
    private void handleUserState(long chatId, String text) {
        UserState state = userStates.get(chatId);
        switch (state) {
            case SUBSCRIBE:
                if (isValidTimeFormat(text)) {

                    Time time = Time.valueOf(text + ":00");
                    subscriberRepository.save(new Subscriber(chatId, time));
                    sendMessage(chatId, "Вы успешно подписались на рассылку фактов", keyboardUtil.getSubscribeMenuForRegisteredUser());
                    userStates.remove(chatId);
                } else {
                    sendMessage(chatId, "Неверный формат времени. Пожалуйста, введите время в формате 00:00 - 23:59. Например, 17:30",
                            keyboardUtil.getCancelButton());
                }

                break;

            case CHANGE_TIME:
                if (isValidTimeFormat(text)) {
                    // Сохраните время в базе данных или другом месте
                    Time time = Time.valueOf(text + ":00");
                    subscriberRepository.delete(subscriberRepository.getSubscriberByChatId(chatId));
                    subscriberRepository.save(new Subscriber(chatId, time));
                    sendMessage(chatId, "Вы успешно поменяли время рассылки фактов", keyboardUtil.getSubscribeMenuForRegisteredUser());
                    userStates.remove(chatId);
                } else {
                    sendMessage(chatId, "Неверный формат времени. Пожалуйста, введите время в формате 00:00 - 23:59. Например, 17:30",
                            keyboardUtil.getCancelButton());
                }
                break;

            case FACT:
                    if (text.equals("Случайный факт")) {
                        sendMessage(chatId, factService.getFact(""), keyboardUtil.getMainKeyboard());
                    } else {
                        sendMessage(chatId, factService.getFact(text), keyboardUtil.getMainKeyboard());
                    }
                    userStates.remove(chatId);
                break;

            default:
                processCommands(chatId, text);
        }
    }

    private void processCommands(long chatId, String text) {
        String[] strMessage = text.split(" ");
        if (strMessage[0].equals("/start")) {
            sendMessage(chatId, START_TEXT, keyboardUtil.getMainKeyboard());
        } else if (strMessage[0].equals("Отписаться")) {

            Subscriber subscriber = subscriberRepository.getSubscriberByChatId(chatId);
            if (subscriber == null) {
                sendMessage(chatId, "Вы не подписаны на рассылку фактов", keyboardUtil.getSubscribeMenuForNotRegisteredUser());
                return;
            }

            subscriberRepository.delete(subscriber);
            sendMessage(chatId, "Вы успешно отписались от рассылки фактов", keyboardUtil.getSubscribeMenuForNotRegisteredUser());

        } else if (strMessage[0].equals("/help")) {

            sendMessage(chatId, HELP_TEXT);

        } else if(strMessage[0].equals("Подписка")) {
            if (subscriberRepository.getSubscriberByChatId(chatId) != null) {
                sendMessage(chatId, "Вы перешли в раздел управления подпиской",
                        keyboardUtil.getSubscribeMenuForRegisteredUser());
            } else {
                sendMessage(chatId, "Вы перешли в раздел управления подпиской",
                        keyboardUtil.getSubscribeMenuForNotRegisteredUser());
            }
        } else {

            sendMessage(chatId, "Неизвестная команда", keyboardUtil.getMainKeyboard());
        }
    }


    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        }catch (TelegramApiException e) {
            e.getLocalizedMessage();
        }
    }

    public void sendMessage(long chatId, String text, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        }catch (TelegramApiException e) {
            e.getLocalizedMessage();
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }


    public static boolean isValidTimeFormat(String time) {
        final String TIME_REGEX = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$";

        Pattern pattern = Pattern.compile(TIME_REGEX);
        Matcher matcher = pattern.matcher(time);
        return matcher.matches();
    }

    //0 0 * * * * = @hourly
    //Рассылка фактов по подписчикам
    @Scheduled(cron = "0 * * * * *")
    public void factSender() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String formattedTime = formatter.format(currentTime);

        Time sqlTime = Time.valueOf(formattedTime);
        System.out.println(sqlTime);

        List<Subscriber> subscribers = subscriberRepository.getSubscribersByTime(sqlTime);
        if (subscribers == null || subscribers.isEmpty()) {
            return;
        }
        for (Subscriber subscriber : subscribers) {
            sendMessage(subscriber.getChatId(), factService.getFact(""));
        }
    }
}
