package ru.mastkey.randomfactsbot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardUtil {
    public ReplyKeyboardMarkup getCancelButton() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> list = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();


        row.add(new KeyboardButton("Назад"));

        list.add(row);

        keyboardMarkup.setKeyboard(list);


        return  keyboardMarkup;
    }

    public ReplyKeyboardMarkup getFactKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> list = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Случайный факт"));
        row.add(new KeyboardButton("Назад"));
        list.add(row);

        keyboardMarkup.setKeyboard(list);

        return keyboardMarkup;
    }

    public ReplyKeyboardMarkup getMainKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> list = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Новый факт"));
        row.add(new KeyboardButton("Подписка"));
        list.add(row);

        keyboardMarkup.setKeyboard(list);

        return keyboardMarkup;
    }

    public ReplyKeyboardMarkup getSubscribeMenuForRegisteredUser() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> list = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Отписаться"));
        row.add(new KeyboardButton("Поменять время"));
        row.add(new KeyboardButton("Назад"));
        list.add(row);

        keyboardMarkup.setKeyboard(list);

        return keyboardMarkup;
    }

    public ReplyKeyboardMarkup getSubscribeMenuForNotRegisteredUser() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> list = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Подписаться"));
        row.add(new KeyboardButton("Назад"));
        list.add(row);

        keyboardMarkup.setKeyboard(list);

        return keyboardMarkup;
    }
}
