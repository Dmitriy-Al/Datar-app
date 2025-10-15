package ru.alimovdev.datar.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.invoices.CreateInvoiceLink;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.alimovdev.datar.config.AppConfig;
import ru.alimovdev.datar.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Slf4j
@Component
public class TelegramBotCommands extends TelegramLongPollingBot {

    private final TelegramBotMethods botMethod = new TelegramBotMethods();


    private final HashMap<String, String> tempData = new HashMap<>();
    private final HashMap<String, Long> savedId = new HashMap<>();
    private final HashMap<String, String> inputtedName = new HashMap<>();
    private final HashMap<String, String> inputtedSurname = new HashMap<>();
    private final HashMap<String, String> inputtedPatronymic = new HashMap<>();
    private final HashMap<String, String> returnData = new HashMap<>();
    private final HashMap<String, String> registerPassword = new HashMap<>();
    private final HashMap<String, Integer> savedMessageId = new HashMap<>();
    private final HashMap<String, Long> savedSpecialistId = new HashMap<>();


    /**
     * Строки-константы, добавляемые в Map tempData. В процессе взаимодействия с ботом может понадобиться ввод некоторых
     * данных в чат и до момента отправки этих данных, функция добавляет в Map tempData строку-триггер, в таком случае
     * данные введенные пользователем интерпретируются должным образом.
     */

    private final String input_remark = "INPUT_REMARK"; // добавление заметки для клиента
    private final String input_findClient = "FIND_CLIENT"; // поиск клиента для записи на приём
    private final String input_password = "INPUT_PASSWORD"; // добавить пароль для учётной записи user
    private final String input_profession = "INPUT_PROFESSION"; // запись созданного user в бд
    private final String input_changeUser = "INPUT_CHANGE_USER"; // изменить данные для учётной записи user
    private final String input_oldPassword = "INPUT_OLD_PASSWORD"; // ввести старый пароль учётной записи user
    private final String input_uploadBackup = "INPUT_UPLOAD_BACKUP"; // выгрузка backup в чат
    private final String input_messageForAll = "INPUT_MESSAGE_FOR_ALL"; // сообщение для всех пользователей
    private final String input_repairPassword = "INPUT_REPAIR_PASSWORD"; // восстановление учётной записи user
    private final String input_supportMessage = "INPUT_SUPPORT_MESSAGE"; // сообщение от user администратору
    private final String input_messageForUser = "INPUT_MESSAGE_FOR_USER"; // отправка сообщения пользователю
    private final String input_loadUserBackup = "INPUT_LOAD_USER_BACKUP"; // восстановить user-сервер из backup-файла в заданной директории
    private final String input_saveUserBackup = "INPUT_SAVE_USER_BACKUP"; // создать backup-файл user в заданной директории
    private final String input_saveClientBackup = "INPUT_SAVE_CLIENT_BACKUP"; // создать backup-файл client в заданной директории
    private final String input_loadClientBackup = "INPUT_LOAD_CLIENT_BACKUP"; // восстановить client-сервер из backup-файла в заданной директории
    private final String input_clientSecondName = "INPUT_CLIENT_SECOND_NAME"; // применение функций в зависимости от содержимого введённого текста
    private final String input_loadSettingsPath = "INPUT_LOAD_SETTINGS_PATH"; // установка настроек из xml-файла в указанной директории
    private final String input_textForStartMessage = "INPUT_FOR_START_MESSAGE"; // добавить текст в стартовое сообщение
    private final String input_findClientForSettings = "FIND_CLIENT_FOR_SETTINGS"; // поиск клиента для работы с его данными


    private final String input_spec_surname = "INPUT_SPEC_SECOND_NAME"; //
    private final String input_spec_name = "INPUT_SPEC_NAME"; //
    private final String input_spec_patronymic = "INPUT_SPEC_PATRONYMIC"; //

    private final String input_admin_surname = "INPUT_ADMIN_SECOND_NAME"; //
    private final String input_admin_name = "INPUT_ADMIN_FIRST_NAME"; //
    private final String input_admin_patronymic = "INPUT_ADMIN_PATRONYMIC"; //



    final String back = "⏎ Назад в меню";

    private final String[] textsForSpecButtons = {"Работа с базой клиентов", "Запись ко мне", "Записать на приём", botMethod.callData_specialistSettings};
    private final String[] textsForAdminButtons = {"Работа с базой клиентов", "Добавить нового клиента", "Добавить нового специалиста", botMethod.callData_specialistSettings};


    @Autowired
    private AdministratorRepository administratorRepository;
    @Autowired
    private SpecialistRepository specialistRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private UserRepository userRepository;


    public TelegramBotCommands() {
        super(AppConfig.botToken);

        /** Меню команд бота */
        List<BotCommand> listOfCommands = new ArrayList<>(); // BotCommand - класс определённый в библиотеке telegrambots
        listOfCommands.add(new BotCommand("/start", "set a welcome message"));  /** первый параметр - команда, второй - краткое описание команды */
        listOfCommands.add(new BotCommand("/mydata", "info about user")); // правильно для бота организовать вывод информации о пользователе
        listOfCommands.add(new BotCommand("/deletedata", "delete info about user"));
        listOfCommands.add(new BotCommand("/help", "help to use bot"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null)); /** Реализация меню бота */
        } catch (TelegramApiException e) {
            log.error("Error bot's command list" + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long longChatId = update.getMessage().getChatId();
            int intMessageId = update.getMessage().getMessageId();
            String stringChatId = String.valueOf(longChatId);
            String userName = update.getMessage().getChat().getUserName() == null ? "" : update.getMessage().getChat().getUserName();

            /**
             * В процессе взаимодействия с ботом может понадобиться ввод некоторых данных в чат и до момента
             * отправки этих данных, функция добавляет в Map tempData строку-триггер, в таком случае данные
             * введенные пользователем интерпретируются должным образом. Если в Map tempData добавляется
             * строка-константа, сообщение-updateMessageText запускает одну из функций в блоке.
             */
            if (tempData.get(stringChatId) != null && !tempData.get(stringChatId).equals("")) {

                switch (tempData.get(stringChatId)) {
                    case input_spec_surname -> {
                        inputRegisterData(stringChatId, messageText, "Введите имя", inputtedSurname, input_spec_name);
                    }
                    case input_spec_name -> {
                        inputRegisterData(stringChatId, messageText, "Введите отчество", inputtedName, input_spec_patronymic);
                    }
                    case input_spec_patronymic -> {
                        if (inputRegisterData(stringChatId, messageText, "", inputtedPatronymic, "")) {
                            registerSpecialist(longChatId, stringChatId);
                        }
                    }
                    case input_admin_surname -> {
                        inputRegisterData(stringChatId, messageText, "Введите имя", inputtedSurname, input_admin_name);
                    }
                    case input_admin_name -> {
                        inputRegisterData(stringChatId, messageText, "Введите отчество", inputtedName, input_admin_patronymic);
                    }
                    case input_admin_patronymic -> {
                        if (inputRegisterData(stringChatId, messageText, "", inputtedPatronymic, "")) {
                            registerAdministrator(longChatId, stringChatId);
                        }
                    }


                }
            }

            // Удаление отправленных в чат сообщений (чтобы не засорять экран чата)
            executeDeleteMessage(new DeleteMessage(stringChatId, intMessageId));

            if (messageText.equals("/start")) { // клавиатура
                tempData.put(stringChatId, "");
                savedMessageId.put(stringChatId, intMessageId);
                if (administratorRepository.existsById(longChatId)) {
                    executeSendMessage(botMethod.createSpecialistMenu(stringChatId, "Меню администратора", textsForAdminButtons));

                } else if (specialistRepository.existsById(longChatId)) {
                    executeSendMessage(botMethod.createSpecialistMenu(stringChatId, "Меню администратора", textsForSpecButtons));

                } else if (userRepository.existsById(longChatId)) {
                    executeSendMessage(botMethod.createUserMenu(stringChatId, textToUser));

                } else {
                    User user = new User();
                    user.setId(longChatId);
                    user.setTgName(userName);
                    userRepository.save(user);
                    executeSendMessage(botMethod.createUserMenu(stringChatId, textToUser));
                }

            } else if (messageText.equals("2")) {
                executeSendMessage(new SendMessage(stringChatId, "Yo, new user!"));
            }


            // Если update содержит изменённое сообщение
        } else if (update.hasCallbackQuery()) {
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long longChatId = update.getCallbackQuery().getMessage().getChatId();
            String stringChatId = String.valueOf(longChatId);
            String callbackData = update.getCallbackQuery().getData();

            if (callbackData.equals(botMethod.callData_backToUserMenu)) {
                tempData.put(stringChatId, "");
                executeEditMessageText(botMethod.createUserMenu(longChatId, messageId, textToUser));

            } else if (callbackData.equals(botMethod.callData_userSettings)) {
                executeEditMessageText(botMethod.createUserSettingsMenu(longChatId, messageId, "Настройки для User"));


            } else if (callbackData.equals(botMethod.callData_userAppointment)) {
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "У тебя нет записи, нищеброд...");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToUserMenu));
                executeEditMessageText(editMessageText);


            } else if (callbackData.equals(botMethod.callData_regAsSpecialist)) {
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Введите вашу фамилию и отправьте сообщение в чат");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToUserMenu));
                executeEditMessageText(editMessageText);
                tempData.put(stringChatId, input_spec_surname); // Регистрация специалиста

            } else if (callbackData.equals(botMethod.callData_regAsAdmin)) {
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Введите вашу фамилию и отправьте сообщение в чат");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToUserMenu));
                executeEditMessageText(editMessageText);
                tempData.put(stringChatId, input_admin_surname); // Регистрация администратора


            } else if (callbackData.equals(botMethod.callData_backToSpecMenu)) {
                executeEditMessageText(botMethod.createSpecialistMenu(longChatId, messageId, "Меню  специалиста", textsForSpecButtons));


            } else if (callbackData.equals(botMethod.callData_backToAdminMenu)) {
                executeEditMessageText(botMethod.createSpecialistMenu(longChatId, messageId, "Меню  администратора", textsForAdminButtons));

            }

            else if (callbackData.equals("Добавить нового специалиста")) {
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "У вас теперь есть специалист-сантехник");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToAdminMenu));
                executeEditMessageText(editMessageText);

                Specialist specialist = new Specialist();
                specialist.setId(204102424);
                specialist.setSpecialistId("204102424"); // specialistIdCreator("Дырожоп", "Сергей", "Сергеевич", "204102424")
                specialist.setSurname("Алимова");
                specialist.setName("Оксана");
                specialist.setPatronymic("Викторовна");
                specialist.setProfession("стоматолог-терапевт");
                specialist.setReceptionSchedule("");
                specialist.setAdministratorIdList("/2041024245");
                specialist.setPhoneNumber("");
                specialist.setPassword("");
                specialistRepository.save(specialist);
            }

            else if (callbackData.equals("Добавить нового клиента")) {

                byte[] bytes = new byte[10];

                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = (byte) (Math.random() * 1050);
                }

                String randomName = new String(bytes);

                Client client = new Client();
                client.setTgId(0);
                client.setSpecialistId("204102424");
                client.setName(randomName);
                client.setSurname("Говнососов");
                client.setPatronymic("Олегович");
                client.setPhoneNumber("1234567");
                client.setClientNotes("");
                client.setAppointmentDateTime("");
                client.setBirthdate("");
                client.setAppointmentNote("");
                client.setVisitHistory("");
                client.setVisitDuration("");
                client.setWaitNearAppointment(false);
                client.setConfirmAppointment("");
                clientRepository.save(client);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Добавлен новый пацик");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToAdminMenu));
                executeEditMessageText(editMessageText);

            }
            else if (callbackData.equals("Работа с базой клиентов")) {
                Iterable<Client> clients = clientRepository.findAll();
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Список клиентуры:\n" + clients);
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToAdminMenu));
                executeEditMessageText(editMessageText);
            }

        }
    }


    public String specialistIdCreator(String name, String surname, String patronymic, String stringChatId) {
        return surname + " " + name.charAt(0) + ". " + patronymic.charAt(0) + ".#" + stringChatId;
    }


    @Override
    public String getBotUsername() {
        return AppConfig.botUsername;
    }


    // Проверка валидности ФИО
    private boolean inputRegisterData(String stringChatId, String messageText, String textForMessage, HashMap<String, String> registerData, String nextStepData) {
        tempData.put(stringChatId, "");
        String dataText = messageText.trim();
        EditMessageText editMessageText = new EditMessageText();
        int messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        editMessageText.setChatId(stringChatId);
        editMessageText.setMessageId(messageId);

        if (dataText.length() < 15 && !dataText.contains(" ")) {
            registerData.put(stringChatId, dataText);
            editMessageText.setText(textForMessage);
            tempData.put(stringChatId, nextStepData);
            editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToUserMenu));
            executeEditMessageText(editMessageText);
        } else {
            editMessageText.setText("Невалидный ввод");
            editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToUserMenu));
            executeEditMessageText(editMessageText);
            return false;
        }
        return true;
    }


    private void registerSpecialist(long longChatId, String stringChatId) {
        Specialist specialist = new Specialist();
        specialist.setId(longChatId);
        specialist.setSpecialistId(stringChatId);
        specialist.setSurname(inputtedSurname.get(stringChatId));
        specialist.setName(inputtedName.get(stringChatId));
        specialist.setPatronymic(inputtedPatronymic.get(stringChatId));
        specialist.setProfession("");
        specialist.setReceptionSchedule("");
        specialist.setAdministratorIdList("");
        specialist.setPhoneNumber("");
        specialist.setPassword("");
        specialistRepository.save(specialist);

        EditMessageText editMessageText = new EditMessageText();
        int messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        editMessageText.setChatId(stringChatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(inputtedName.get(stringChatId) + " " + inputtedPatronymic.get(stringChatId) + ", спасибо за регистрацию!");
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToSpecMenu));
        executeEditMessageText(editMessageText);
    }

    private void registerAdministrator(long longChatId, String stringChatId) {
        Administrator administrator = new Administrator();
        administrator.setId(longChatId);
        administrator.setSurname(inputtedSurname.get(stringChatId));
        administrator.setName(inputtedName.get(stringChatId));
        administrator.setPatronymic(inputtedPatronymic.get(stringChatId));
        administrator.setSpecialistIdList("");
        administrator.setPhoneNumber("");
        administrator.setPassword("");
        administratorRepository.save(administrator);

        EditMessageText editMessageText = new EditMessageText();
        int messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        editMessageText.setChatId(stringChatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(inputtedName.get(stringChatId) + " " + inputtedPatronymic.get(stringChatId) + ", спасибо за регистрацию!");
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToAdminMenu));
        executeEditMessageText(editMessageText);
    }


    public void executeSendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            //log.error("SendMessage execute error: " + e.getMessage());
        }
    }

    private void executeEditMessageText(EditMessageText editMessageText) {
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            //log.error("SendMessage execute error: " + e.getMessage());
        }
    }

    private void executePhotoMessage(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            //log.error("SendMessage execute error: " + e.getMessage());
        }
    }


    private void executeSendAudioMessage(SendAudio sendAudio) {
        try {
            execute(sendAudio);
        } catch (TelegramApiException e) {
            //log.error("SendMessage execute error: " + e.getMessage());
        }
    }

    private void executeAnswerCallbackQuery(AnswerCallbackQuery answerCallbackQuery) {
        try {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            //log.error("SendMessage execute error: " + e.getMessage());
        }
    }

    private void executeSendAudioMessage(SendAnimation sendAnimation) {
        try {
            execute(sendAnimation);
        } catch (TelegramApiException e) {
            //log.error("SendMessage execute error: " + e.getMessage());
        }
    }

    private void executeSendDocument(SendDocument sendDocument) {
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            //log.error("SendMessage execute error: " + e.getMessage());
        }
    }

    // Используйте этот метод, когда вам нужно сообщить пользователю, что на стороне бота что-то происходит
    private void executeSendChatAction(SendChatAction sendChatAction) {
        try {
            execute(sendChatAction);
        } catch (TelegramApiException e) {
            //log.error("SendMessage execute error: " + e.getMessage());
        }
    }


    private String executeInvoiceLincAndSendMessage(CreateInvoiceLink createInvoiceLink) {
        String invoiceLincUrl = "null";
        try {
            invoiceLincUrl = execute(createInvoiceLink);
        } catch (TelegramApiException e) {
            log.error("SendMessage execute error: " + e.getMessage());
            System.out.println("Err: " + e.getMessage());
        }
        return invoiceLincUrl;
    }


    private void executeDeleteMessage(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            //log.error("SendMessage execute error: " + e.getMessage());
        }
    }


    public void executeEditMessageMedia(EditMessageMedia editMessageMedia) {
        try {
            execute(editMessageMedia);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }


    String textToUser = "Здравствуйте! Вы можете смотреть и изменять свою запись, " +
            "можете зарегистрироваться в качестве специалиста или администратора";


}