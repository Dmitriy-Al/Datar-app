package ru.alimovdev.datar.service;

import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.alimovdev.datar.model.Client;

import java.util.ArrayList;
import java.util.List;



public class TelegramBotMethods {

    // Редактирование отправленного сообщения
    protected EditMessageText createEditMessageText(long chatId, long messageId, String textForMessage) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId((int) messageId);
        editMessageText.setText(textForMessage);
        return editMessageText;
    }

    // Меню с одной экранной клавишей
    protected InlineKeyboardMarkup receiveOneButtonMenu(String buttonText, String callBackData) {
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
    protected InlineKeyboardMarkup receiveTwoButtonsMenu(String firstButtonText, String firstData,
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
    protected InlineKeyboardMarkup createButtonSet(String[] textsForButtons) {
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
    protected InlineKeyboardMarkup createDataButtonSet(String[] textsForButtons, String callBackData) {
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


    protected InlineKeyboardMarkup createClientsButtonSet(String dataSymbol, List<Client> clients, String mainMenuData) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (Client cli : clients) {
            List<InlineKeyboardButton> rowInlineButton = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton(cli.receiveShortName());
            button.setCallbackData(dataSymbol + cli.getId());
            rowInlineButton.add(button);
            rowsInline.add(rowInlineButton);
        }
        List<InlineKeyboardButton> inlineButton = new ArrayList<>();
        InlineKeyboardButton backButton = new InlineKeyboardButton(mainMenuData);
        backButton.setCallbackData(mainMenuData);
        inlineButton.add(backButton);
        rowsInline.add(inlineButton);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }


    // Экранная клавиатура в виде списка произвольной длинны с добавленным текстом для callBackData
    protected InlineKeyboardMarkup createDataButtonSet(String[] textsForButtons ,String[] textsCallBackData, String callBackDataSymbol) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // коллекция коллекций с горизонтальным рядом кнопок, создаёт вертикальный ряд кнопок

        for (int i = 0; i < textsForButtons.length; i++) {
            List<InlineKeyboardButton> rowInlineButton = new ArrayList<>(); // коллекция с горизонтальным рядом кнопок
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(textsForButtons[i]);
            button.setCallbackData(callBackDataSymbol + textsCallBackData[i]);
            rowInlineButton.add(button);
            rowsInline.add(rowInlineButton);
        }

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }


    protected SendMessage createSpecialistMenu(String stringChatId, String textForMessage, String[] textsForButtons) {
        SendMessage sendMessage = new SendMessage(stringChatId, textForMessage);
        sendMessage.setReplyMarkup(createButtonSet(textsForButtons));
        return sendMessage;
    }


    protected EditMessageText createSpecialistMenu(long longChatId, long messageId, String textForMessage, String[] textsForButtons) {
        EditMessageText editMessageText = createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(createButtonSet(textsForButtons));
        return editMessageText;
    }

    protected EditMessageText createSpecListMenu(long longChatId, long messageId, String textForMessage, String[] textsForButtons, String[] textsCallBackData, String callBackDataSymbol) {
        EditMessageText editMessageText = createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(createDataButtonSet(textsForButtons, textsCallBackData, callBackDataSymbol));
        return editMessageText;
    }

    protected SendMessage createUserMenu(String stringChatId, String textForMessage) {
        SendMessage sendMessage = new SendMessage(stringChatId, textForMessage);
        sendMessage.setReplyMarkup(receiveTwoButtonsMenu("\uD83D\uDDD3 Ваша запись",
                callData_userAppointment + stringChatId,"Настройки ⚙", callData_userSettings));
        return sendMessage;
    }

    protected EditMessageText createUserMenu(long longChatId, long messageId, String textForMessage) {
        EditMessageText editMessageText = createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(receiveTwoButtonsMenu("\uD83D\uDDD3 Ваша запись",
                callData_userAppointment + longChatId,"Настройки ⚙", callData_userSettings));
        return editMessageText;
    }

    protected EditMessageText createUserSettingsMenu(long longChatId, long messageId, String textForMessage) {
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

    // Выбор даты для записи клиента
    //   fun receiveAppointmentMonth(String stringChatId, int intMessageId, String callBackData,  clientRepository: ClientDataDao): EditMessageText {}


    protected EditMessageText searchClient(long longChatId, long messageId, String textForMessage, String callBackDataSymbol, String toMainMenu) {
        EditMessageText editMessageText = createEditMessageText(longChatId, messageId, textForMessage);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // коллекция коллекций с горизонтальным рядом кнопок, создаёт вертикальный ряд кнопок

        List<InlineKeyboardButton> firstRowInlineButton = new ArrayList<>(); // коллекция с горизонтальным рядом кнопок
        List<InlineKeyboardButton> secondRowInlineButton = new ArrayList<>();
        List<InlineKeyboardButton> thirdRowInlineButton = new ArrayList<>();
        List<InlineKeyboardButton> fourthRowInlineButton = new ArrayList<>();
        List<InlineKeyboardButton> fifthRowInlineButton = new ArrayList<>();

        for (int i = 1040; i <= 1071; i++) {
            if (i == 1049 || i == 1066 || i == 1067 || i == 1068) continue;
            if (i < 1047) {
                InlineKeyboardButton button = new InlineKeyboardButton(String.valueOf((char) i));
                button.setCallbackData(callBackDataSymbol + (char) i);
                firstRowInlineButton.add(button);
            } else if (i < 1055) {
                InlineKeyboardButton button = new InlineKeyboardButton(String.valueOf((char) i));
                button.setCallbackData(callBackDataSymbol + (char) i);
                secondRowInlineButton.add(button);
            } else if (i < 1062) {
                InlineKeyboardButton button = new InlineKeyboardButton(String.valueOf((char) i));
                button.setCallbackData(callBackDataSymbol + (char) i);
                thirdRowInlineButton.add(button);
            } else {
                InlineKeyboardButton button = new InlineKeyboardButton(String.valueOf((char) i));
                button.setCallbackData(callBackDataSymbol + (char) i);
                fourthRowInlineButton.add(button);
            }
        }
        InlineKeyboardButton firstButton = new InlineKeyboardButton(back);
        firstButton.setCallbackData(toMainMenu);
        fifthRowInlineButton.add(firstButton);
        InlineKeyboardButton secondButton = new InlineKeyboardButton("Список всех клиентов");
        secondButton.setCallbackData(TelegramBotCommands.callData_clientsList);
        fifthRowInlineButton.add(secondButton);

        rowsInline.add(firstRowInlineButton);
        rowsInline.add(secondRowInlineButton);
        rowsInline.add(thirdRowInlineButton);
        rowsInline.add(fourthRowInlineButton);
        rowsInline.add(fifthRowInlineButton);

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        return editMessageText;
    }



    final String back = "⏎  Назад в меню";

    protected final String callData_delMyData = "#delmydata";
    protected final String callData_regAsSpecialist = "#regspec";
    protected final String callData_regAsAdmin = "#regadmin";
    protected final String callData_regClient = "Добавить нового клиента";

    protected final String callData_backToUserMenu = "⏎   Назад в меню";
    protected final String callData_backToAdminMenu = "⏎  Назад в меню";
    protected final String callData_backToSpecMenu = "⏎ Назад в меню";

    protected final String callData_userSettings = "userSettings";

    protected final String callData_adminSettings = "Настройки ⚙";
    protected final String callData_specSettings = "Настройки  ⚙";
    protected final String callData_userAppointment = "usrAppointment";



}
