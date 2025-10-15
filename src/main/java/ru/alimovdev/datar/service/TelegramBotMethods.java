package ru.alimovdev.datar.service;

import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class TelegramBotMethods {

    // Редактирование отправленного сообщения
    public EditMessageText createEditMessageText(long chatId, long messageId, String textForMessage) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int) messageId);
        editMessageText.setText(textForMessage);
        return editMessageText;
    }

    // Меню с одной экранной клавишей
    public InlineKeyboardMarkup receiveOneButtonMenu(String buttonText, String callBackData) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // коллекция коллекций с горизонтальным рядом кнопок, создаёт вертикальный ряд кнопок
        List<InlineKeyboardButton> firstRowInlineButton = new ArrayList<>(); // коллекция с горизонтальным рядом кнопок

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(buttonText);
        button.setCallbackData(callBackData);
        firstRowInlineButton.add(button);
        rowsInline.add(firstRowInlineButton);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }

    // Меню с двумя экранными клавишами
    public InlineKeyboardMarkup receiveTwoButtonsMenu(String firstButtonText, String firstData,
                                                      String secondButtonText, String secondData) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // коллекция коллекций с горизонтальным рядом кнопок, создаёт вертикальный ряд кнопок
        List<InlineKeyboardButton> firstRowInlineButton = new ArrayList<>(); // коллекция с горизонтальным рядом кнопок

        InlineKeyboardButton firstButton = new InlineKeyboardButton();
        firstButton.setText(firstButtonText);
        firstButton.setCallbackData(firstData);
        firstRowInlineButton.add(firstButton);

        InlineKeyboardButton secondButton = new InlineKeyboardButton();
        secondButton.setText(secondButtonText);
        secondButton.setCallbackData(secondData);
        firstRowInlineButton.add(secondButton);
        rowsInline.add(firstRowInlineButton);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }

    // Экранная клавиатура в виде списка произвольной длинны
    public InlineKeyboardMarkup createButtonSet(String[] textsForButtons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // коллекция коллекций с горизонтальным рядом кнопок, создаёт вертикальный ряд кнопок

        for (String data : textsForButtons) {
            List<InlineKeyboardButton> rowInlineButton = new ArrayList<>(); // коллекция с горизонтальным рядом кнопок
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(data);
            button.setCallbackData(data);
            rowInlineButton.add(button);
            rowsInline.add(rowInlineButton);
        }

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }

    // Экранная клавиатура в виде списка произвольной длинны с добавленным текстом для callBackData
    public InlineKeyboardMarkup createDataButtonSet(String[] textsForButtons, String callBackData) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // коллекция коллекций с горизонтальным рядом кнопок, создаёт вертикальный ряд кнопок

        for (String data : textsForButtons) {
            List<InlineKeyboardButton> rowInlineButton = new ArrayList<>(); // коллекция с горизонтальным рядом кнопок
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(data);
            button.setCallbackData(callBackData + data);
            rowInlineButton.add(button);
            rowsInline.add(rowInlineButton);
        }

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }


    public SendMessage createSpecialistMenu(String stringChatId, String textForMessage, String[] textsForButtons) {
        SendMessage sendMessage = new SendMessage(stringChatId, textForMessage);
        sendMessage.setReplyMarkup(createButtonSet(textsForButtons));
        return sendMessage;
    }


    public EditMessageText createSpecialistMenu(long longChatId, long messageId, String textForMessage, String[] textsForButtons) {
        EditMessageText editMessageText = createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(createButtonSet(textsForButtons));
        return editMessageText;
    }



    public SendMessage createUserMenu(String stringChatId, String textForMessage) {
        SendMessage sendMessage = new SendMessage(stringChatId, textForMessage);
        sendMessage.setReplyMarkup(receiveTwoButtonsMenu("\uD83D\uDDD3 Ваша запись",
                callData_userAppointment + stringChatId,"Настройки ⚙", callData_userSettings));
        return sendMessage;
    }

    public EditMessageText createUserMenu(long longChatId, long messageId, String textForMessage) {
        EditMessageText editMessageText = createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(receiveTwoButtonsMenu("\uD83D\uDDD3 Ваша запись",
                callData_userAppointment + longChatId,"Настройки ⚙", callData_userSettings));
        return editMessageText;
    }

    public EditMessageText createUserSettingsMenu(long longChatId, long messageId, String textForMessage) {
        EditMessageText editMessageText = createEditMessageText(longChatId, messageId, textForMessage);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // коллекция коллекций с горизонтальным рядом кнопок, создаёт вертикальный ряд кнопок

        List<InlineKeyboardButton> firstRowInlineButton = new ArrayList<>(); // коллекция с горизонтальным рядом кнопок
        List<InlineKeyboardButton> secondRowInlineButton = new ArrayList<>();
        List<InlineKeyboardButton> thirdRowInlineButton = new ArrayList<>();
        List<InlineKeyboardButton> fourthRowInlineButton = new ArrayList<>();

        InlineKeyboardButton firstButton = new InlineKeyboardButton();
        firstButton.setText("Зарегистрироваться как администратор");
        firstButton.setCallbackData(callData_regAsAdmin); // TODO
        firstRowInlineButton.add(firstButton);
        InlineKeyboardButton secondButton = new InlineKeyboardButton();
        secondButton.setText("Зарегистрироваться как специалист");
        secondButton.setCallbackData(callData_regAsSpecialist);// TODO
        secondRowInlineButton.add(secondButton);
        InlineKeyboardButton thirdButton = new InlineKeyboardButton();
        thirdButton.setText("Удалить мою учетную запись");
        thirdButton.setCallbackData(callData_delMyData);// TODO
        thirdRowInlineButton.add(thirdButton);
        InlineKeyboardButton fourthButton = new InlineKeyboardButton();
        fourthButton.setText(back);
        fourthButton.setCallbackData(callData_backToUserMenu);// TODO
        fourthRowInlineButton.add(fourthButton);

        rowsInline.add(firstRowInlineButton);
        rowsInline.add(secondRowInlineButton);
        rowsInline.add(thirdRowInlineButton);
        rowsInline.add(fourthRowInlineButton);

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        return editMessageText;
    }



    final String back = "⏎  Назад в меню";

    protected final String callData_delMyData = "#delmydata";
    protected final String callData_regAsSpecialist = "#regspec";
    protected final String callData_regAsAdmin = "#regadmin";
    protected final String callData_backToUserMenu = "⏎   Назад в меню";

    protected final String callData_backToAdminMenu = "⏎ Назад в меню";
    protected final String callData_backToSpecMenu = "⏎  Назад в меню";
    protected final String callData_userSettings = "userSettings";

    protected final String callData_adminSettings = "Настройки ⚙";
    protected final String callData_specialistSettings = "Настройки  ⚙";
    protected final String callData_userAppointment = "usrAppointment";



}
