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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.alimovdev.datar.config.AppConfig;
import ru.alimovdev.datar.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private final HashMap<String, String> inputtedPhoneNumber = new HashMap<>();
    private final HashMap<String, String> inputtedClientBirthdate = new HashMap<>();

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

    private final String undefinedConfirmStatus = "UNDEFINED";

    private final String input_spec_surname = "INPUT_SPEC_SECOND_NAME"; //
    private final String input_spec_name = "INPUT_SPEC_NAME"; //
    private final String input_spec_patronymic = "INPUT_SPEC_PATRONYMIC"; //

    private final String input_admin_surname = "INPUT_ADMIN_SECOND_NAME"; //
    private final String input_admin_name = "INPUT_ADMIN_NAME"; //
    private final String input_admin_patronymic = "INPUT_ADMIN_PATRONYMIC"; //

    private final String input_client_surname = "INPUT_CLIENT_SECOND_NAME"; //
    private final String input_client_name = "INPUT_CLIENT_NAME"; //
    private final String input_client_patronymic = "INPUT_CLIENT_PATRONYMIC"; //
    private final String input_client_phoneNumber = "INPUT_CLIENT_PHONE"; //
    private final String input_client_birthdate = "INPUT_CLIENT_BIRTHDATE"; //

    final String back = "⏎ Назад в меню";
    final String specIdDataSymbol = "IDSP";
    final String clientIdDataSymbol = "IDCL";
    final String clientFirstSymbol = "SURSYM";
    final String clientIdTeg = "CLIID";
    final static String callData_clientsList = "CLILIST";

    private final String[] textsForSpecButtons = {"Записать на прием", "Добавить нового клиента", "Посмотреть запись", "Работа с базой клиентов", botMethod.callData_specSettings};
    private final String[] textsForAdminButtons = {"Выбор специалиста", "Записать на прием", "Добавить нового клиента", "Посмотреть запись", "Работа с базой клиентов", botMethod.callData_adminSettings};


    @Autowired
    private AdministratorRepository adminRepository;
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
                        verifyRegisterData(longChatId, stringChatId, messageText, "Введите имя", inputtedSurname, input_spec_name);
                    }
                    case input_spec_name -> {
                        verifyRegisterData(longChatId, stringChatId, messageText, "Введите отчество", inputtedName, input_spec_patronymic);
                    }
                    case input_spec_patronymic -> {
                        if (verifyRegisterData(longChatId, stringChatId, messageText, "", inputtedPatronymic, "")) {
                            registerSpecialist(longChatId, stringChatId);
                        }
                    }
                    case input_admin_surname -> {
                        verifyRegisterData(longChatId, stringChatId, messageText, "Введите имя", inputtedSurname, input_admin_name);
                    }
                    case input_admin_name -> {
                        verifyRegisterData(longChatId, stringChatId, messageText, "Введите отчество", inputtedName, input_admin_patronymic);
                    }
                    case input_admin_patronymic -> {
                        if (verifyRegisterData(longChatId, stringChatId, messageText, "", inputtedPatronymic, "")) {
                            registerAdministrator(longChatId, stringChatId);
                        }
                    }
                    case input_client_surname -> {
                        verifyRegisterData(longChatId, stringChatId, messageText, "Введите имя", inputtedSurname, input_client_name);
                    }
                    case input_client_name -> {
                        verifyRegisterData(longChatId, stringChatId, messageText, "Введите отчество", inputtedName, input_client_patronymic);
                    }
                    case input_client_patronymic -> {
                        verifyRegisterData(longChatId, stringChatId, messageText, "Введите номер телефона или символ - ", inputtedPatronymic, input_client_phoneNumber);
                    }
                    case input_client_phoneNumber -> {
                        verifyPhoneNumber(longChatId, stringChatId, messageText, "Введите дату и год рождения клиента в формате 01.01.2020 или символ - ", inputtedPhoneNumber, input_client_birthdate);
                    }
                    case input_client_birthdate -> {
                        if (verifyBirthDayDate(longChatId, stringChatId, messageText, "", inputtedClientBirthdate, "")) {
                            registerClient(longChatId, stringChatId);
                        }
                    }

                }
            }

            // Удаление отправленных в чат сообщений (чтобы не засорять экран чата)
            executeDeleteMessage(new DeleteMessage(stringChatId, intMessageId));

            if (messageText.equals("/start")) { // клавиатура
                tempData.put(stringChatId, "");
                savedMessageId.put(stringChatId, intMessageId);
                if (adminRepository.existsById(longChatId)) {
                    executeSendMessage(botMethod.createSpecialistMenu(stringChatId, "Меню администратора", textsForAdminButtons));
                } else if (specialistRepository.existsById(longChatId)) {
                    executeSendMessage(botMethod.createSpecialistMenu(stringChatId, "Меню специалиста", textsForSpecButtons));
                } else if (userRepository.existsById(longChatId)) {
                    executeSendMessage(botMethod.createUserMenu(stringChatId, textToUser));
                } else {
                    User user = new User();
                    user.setId(longChatId);
                    user.setTgName(userName);
                    userRepository.save(user);
                    executeSendMessage(botMethod.createUserMenu(stringChatId, textToUser));
                }
            } else if (messageText.equals("1")) {
                Administrator administrator = adminRepository.findById(longChatId).get();
                administrator.setOwner(true);
                adminRepository.save(administrator);

            } else if (messageText.equals("0")) {
                Administrator administrator = adminRepository.findById(longChatId).get();
                administrator.setOwner(false);
                adminRepository.save(administrator);
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

            } else if (callbackData.equals(botMethod.callData_backToAdminMenu)) {
                executeEditMessageText(botMethod.createSpecialistMenu(longChatId, messageId, "Меню  администратора", textsForAdminButtons));
            } else if (callbackData.equals(botMethod.callData_backToSpecMenu)) {
                executeEditMessageText(botMethod.createSpecialistMenu(longChatId, messageId, "Меню специалиста", textsForSpecButtons));
            } else if (callbackData.equals("Добавить нового клиента")) {
                createNewClientProcess(longChatId, stringChatId, messageId);
            } else if (callbackData.equals("Выбор специалиста")) {
                String[] idList = adminRepository.findById(longChatId).get().getSpecialistIdList().split("/");
                int length = idList.length;
                if (length > 0) {
                    String[] textsForButtons = new String[length];
                    String[] textsCallBackData = new String[length];

                    for (int i = 0; i < idList.length; i++) {
                        String name = specialistRepository.findById(Long.parseLong(idList[i])).get().receiveShortName();
                        textsForButtons[i] = name;
                        textsCallBackData[i] = idList[i];
                    }
                    executeEditMessageText(botMethod.createSpecListMenu(longChatId, messageId, "Выберите Специалиста", textsForButtons, textsCallBackData, specIdDataSymbol));
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "У вас нет специалистов");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToAdminMenu));

            } else if (callbackData.contains(specIdDataSymbol)) {
                String data = callbackData.replace(specIdDataSymbol, "");
                String name = specialistRepository.findById(Long.parseLong(data)).get().receiveShortName();
                Administrator administrator = adminRepository.findById(longChatId).get();
                administrator.setCurrentSpecialistId(data);
                adminRepository.save(administrator);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Выбран специалист " + name);
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToAdminMenu));
                executeEditMessageText(editMessageText);
            } else if (callbackData.equals("Посмотреть запись")) {
                Iterable<Client> clients = clientRepository.findAll();
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Список клиентуры:\n" + clients);
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToAdminMenu));
                executeEditMessageText(editMessageText);
            } else if (callbackData.equals("Записать на прием")) {
                String mainMenuData = adminRepository.existsById(longChatId) ? botMethod.callData_backToAdminMenu : botMethod.callData_backToSpecMenu;
                String textForMessage = receiveTextForMessage(longChatId, stringChatId, "Выберите первую букву фамилии клиента");
                executeEditMessageText(botMethod.searchClient(longChatId, messageId, textForMessage, clientFirstSymbol, mainMenuData));

            } else if (callbackData.equals(callData_clientsList)) {
                showAllClients(longChatId, messageId, stringChatId);

            } else if (callbackData.contains(clientFirstSymbol)) {
                String dataText = callbackData.replace(clientFirstSymbol, "");
                executeEditMessageText(receiveClientsSet(longChatId, messageId, dataText, "Выберите клиента из списка"));

            } else if (callbackData.contains(clientIdTeg)) {
                String clientId = callbackData.replace(clientIdDataSymbol, "");
                Client client = clientRepository.findById(Long.parseLong(clientIdDataSymbol)).get();
                EditMessageText editMessageText = new EditMessageText();
                LocalDate date = LocalDate.now();
                DateTimeFormatter numFormat = DateTimeFormatter.ofPattern("MM");
                DateTimeFormatter buttonFormat = DateTimeFormatter.ofPattern("MM.yyyy");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

                String nameSymbol = callbackData.replace(clientFirstSymbol, "");
                String callData;
                String textForMessage;
                if (adminRepository.existsById(longChatId)) {
                    callData = botMethod.callData_backToAdminMenu;
                    long specialistId = Long.parseLong(adminRepository.findById(longChatId).get().getCurrentSpecialistId());
                    String name = specialistRepository.findById(specialistId).get().receiveShortName();
                    textForMessage = "Вы выбираете клиента для специалиста " + name + ". Выберите первую букву фамилии клиента";
                } else {
                    callData = botMethod.callData_backToSpecMenu;
                    textForMessage = "Выберите первую букву фамилии клиента";
                }



            /*

                val inlineKeyboardMarkup = InlineKeyboardMarkup()
                val rowsInline = ArrayList<List<InlineKeyboardButton>>()
                val firstRowInlineButton = ArrayList<InlineKeyboardButton>()

                val firstButton = InlineKeyboardButton()
                firstButton.putData(buttonFormat.format(date), "${numFormat.format(date)}$callData_appointmentDay$clientId")
                firstRowInlineButton.add(firstButton)

                val secondButton = InlineKeyboardButton()
                secondButton.putData(buttonFormat.format(date.plusMonths(1)), "${numFormat.
                        format(date.plusMonths(1))}$callData_appointmentDay$clientId")
            firstRowInlineButton.add(secondButton)

            val thirdButton = InlineKeyboardButton()
            thirdButton.putData(buttonFormat.format(date.plusMonths(2)), "${numFormat.
                    format(date.plusMonths(2))}$callData_appointmentDay$clientId")
        firstRowInlineButton.add(thirdButton)

        val secondRowInlineButton = ArrayList<InlineKeyboardButton>()
        val returnButton = InlineKeyboardButton()
        returnButton.putData(text_cancelButton, callData_backToMenu)
        secondRowInlineButton.add(returnButton)

        rowsInline.add(firstRowInlineButton)
        rowsInline.add(secondRowInlineButton)
        inlineKeyboardMarkup.keyboard = rowsInline

        editMessageText.replyMarkup = inlineKeyboardMarkup

        val textForMessage: String = if (client.appointmentDate.length == 10 && LocalDate.now().
                isBefore(LocalDate.parse(client.appointmentDate))) {
            "$text_cliAppointmentOne${formatter.format(LocalDate.parse(client.appointmentDate))} в " +
                    "${client.appointmentTime}$text_cliAppointmentTwo"
        } else {
            text_chooseMonth
        }
        editMessageText.putData(stringChatId, intMessageId, textForMessage)
        return editMessageText

             */

            } // TODO


        }
    }


    @Override
    public String getBotUsername() {
        return AppConfig.botUsername;
    }


    // Проверка валидности ФИО
    private boolean verifyRegisterData(long longChatId, String stringChatId, String messageText, String textForMessage, HashMap<String, String> registerData, String nextStepData) {
        String callData;
        if (adminRepository.existsById(longChatId)) {
            callData = botMethod.callData_backToAdminMenu;
        } else if (specialistRepository.existsById(longChatId)) {
            callData = botMethod.callData_backToUserMenu;
        } else {
            callData = botMethod.callData_backToUserMenu;
        }
        tempData.put(stringChatId, "");
        String dataText = messageText.trim();
        EditMessageText editMessageText = new EditMessageText();
        int messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        editMessageText.setChatId(stringChatId);
        editMessageText.setMessageId(messageId);

        if (dataText.length() < 15 || !dataText.contains(" ") || !dataText.contains("#") || !dataText.contains("*") ||
                !dataText.contains("/") || !dataText.contains("$") || !dataText.contains("@")) {
            registerData.put(stringChatId, dataText);
            editMessageText.setText(textForMessage);
            tempData.put(stringChatId, nextStepData);
            editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, callData));
            executeEditMessageText(editMessageText);
        } else {
            editMessageText.setText("Невалидный ввод");
            editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, callData));
            executeEditMessageText(editMessageText);
            return false;
        }
        return true;
    }

    private void verifyPhoneNumber(long longChatId, String stringChatId, String messageText, String textForMessage, HashMap<String, String> registerData, String nextStepData) {
        String callData = adminRepository.existsById(longChatId) ? botMethod.callData_backToAdminMenu :
                botMethod.callData_backToSpecMenu;

        EditMessageText editMessageText = new EditMessageText();
        int messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        editMessageText.setChatId(stringChatId);
        editMessageText.setMessageId(messageId);

        tempData.put(stringChatId, "");
        String dataText = messageText.trim();
        try {
            Long.parseLong(dataText.replace("+", "").replace("(", "").replace(")", "").replace("-", "") + 0);

            if (dataText.length() <= 13) {
                registerData.put(stringChatId, dataText);
                editMessageText.setText(textForMessage);
                tempData.put(stringChatId, nextStepData);
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, callData));
                executeEditMessageText(editMessageText);
            }
        } catch (NumberFormatException e) {
            editMessageText.setText("Невалидный ввод");
            //log.error("SendMessage execute error: " + e.getMessage());
        }
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, callData));
        executeEditMessageText(editMessageText);
    }

    private boolean verifyBirthDayDate(long longChatId, String stringChatId, String messageText, String textForMessage,
                                       HashMap<String, String> registerData, String nextStepData) {
        String callData = adminRepository.existsById(longChatId) ? botMethod.callData_backToAdminMenu :
                botMethod.callData_backToSpecMenu;

        EditMessageText editMessageText = new EditMessageText();
        int messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        editMessageText.setChatId(stringChatId);
        editMessageText.setMessageId(messageId);

        tempData.put(stringChatId, "");
        String dataText = messageText.trim();
        try {
            Long.parseLong(dataText.replace(".", "").replace("-", "") + 0);

            if (dataText.length() == 10 || dataText.length() == 1) {
                registerData.put(stringChatId, dataText);
                editMessageText.setText(textForMessage);
                tempData.put(stringChatId, nextStepData);
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, callData));
                executeEditMessageText(editMessageText);
                return true;
            }
        } catch (NumberFormatException e) {
            //log.error("SendMessage execute error: " + e.getMessage());
        }
        editMessageText.setText("Невалидный ввод");
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, callData));
        executeEditMessageText(editMessageText);
        return false;
    }

    public void createNewClientProcess(long longChatId, String stringChatId, long messageId) { // TODO не работает отбраковка
        boolean isAdminExist = adminRepository.existsById(longChatId);
        String textForMessage;
        String callData = isAdminExist ? botMethod.callData_backToAdminMenu : botMethod.callData_backToSpecMenu;

        if (isAdminExist && adminRepository.findById(longChatId).get().getOwnerId().equals(stringChatId) ||
                specialistRepository.existsById(longChatId)) {
            textForMessage = "Введите фамилию и отправьте сообщение в чат";
            tempData.put(stringChatId, input_client_surname);
        } else if (isAdminExist && !adminRepository.findById(longChatId).get().getCurrentSpecialistId().isEmpty()) {
            Specialist specialist = specialistRepository.
                    findById(Long.parseLong(adminRepository.findById(longChatId).get().getCurrentSpecialistId())).get();
            textForMessage = "Вы добавляете нового клиента для специалиста " + specialist.receiveShortName() +
                    "\nВведите фамилию и отправьте сообщение в чат";
            tempData.put(stringChatId, input_client_surname);
        } else {
            textForMessage = "Сначала необходимо выбрать специалиста.";
        }
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, callData));
        executeEditMessageText(editMessageText);
    }

    private void registerSpecialist(long longChatId, String stringChatId) {
        Specialist specialist = new Specialist();
        specialist.setTimeZone(0);
        specialist.setPassword("");
        specialist.setId(longChatId);
        specialist.setProfession("");
        specialist.setPhoneNumber("");
        specialist.setWorkTimeLength("");
        specialist.setReceptionSchedule("");
        specialist.setReceptionSchedule("");
        specialist.setOwnerId(stringChatId);
        specialist.setAdministratorIdList("");
        specialist.setSpecialistId(stringChatId);
        specialist.setClientAppointmentRange("");
        specialist.setName(inputtedName.get(stringChatId));
        specialist.setSurname(inputtedSurname.get(stringChatId));
        specialist.setPatronymic(inputtedPatronymic.get(stringChatId));
        specialistRepository.save(specialist);

        long messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId,
                inputtedName.get(stringChatId) + " " + inputtedPatronymic.get(stringChatId) + ", спасибо за регистрацию!");
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToAdminMenu));
        executeEditMessageText(editMessageText);
    }

    private void registerAdministrator(long longChatId, String stringChatId) {
        Administrator administrator = new Administrator();
        administrator.setOwner(true);
        administrator.setPassword("");
        administrator.setId(longChatId);
        administrator.setPhoneNumber("");
        administrator.setSpecialistIdList("");
        administrator.setOwnerId(stringChatId);
        administrator.setName(inputtedName.get(stringChatId));
        administrator.setSurname(inputtedSurname.get(stringChatId));
        administrator.setPatronymic(inputtedPatronymic.get(stringChatId));
        adminRepository.save(administrator);
        long messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId,
                inputtedName.get(stringChatId) + " " + inputtedPatronymic.get(stringChatId) + ", спасибо за регистрацию!");
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, botMethod.callData_backToAdminMenu));
        executeEditMessageText(editMessageText);
    }


    private void registerClient(long longChatId, String stringChatId) { // TODO утилизация данных и проверка наличия specialistId
        String callData;
        String ownerId;
        String textForMessage = "Новый клиент был добавлен";

        if (adminRepository.existsById(longChatId)) {
            callData = botMethod.callData_backToAdminMenu;
            Administrator administrator = adminRepository.findById(longChatId).get();
            ownerId = administrator.getOwnerId().equals(stringChatId) ? administrator.getOwnerId() :
                    administrator.getCurrentSpecialistId();
            saveClientInDB(stringChatId, ownerId);
        } else {
            callData = botMethod.callData_backToSpecMenu;
            ownerId = specialistRepository.findById(longChatId).get().getOwnerId();
            saveClientInDB(stringChatId, ownerId);
        }
        long  messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(back, callData));
        executeEditMessageText(editMessageText);
    }

    private void saveClientInDB(String stringChatId, String ownerId) {
        Client client = new Client();
        client.setTgId(0);
        client.setClientNotes("");
        client.setOwnerId(ownerId);
        client.setVisitHistory("");
        client.setWaitNearAppointment(false);
        client.setName(inputtedName.get(stringChatId));
        client.setConfirmAppointment(undefinedConfirmStatus);
        client.setSurname(inputtedSurname.get(stringChatId));
        client.setPatronymic(inputtedPatronymic.get(stringChatId));
        client.setPhoneNumber(inputtedPhoneNumber.get(stringChatId));
        client.setBirthdate(inputtedClientBirthdate.get(stringChatId));
        clientRepository.save(client);
    }

    // показать список всех клиентов
    private void showAllClients(long longChatId, long messageId, String stringChatId) {
        boolean isAdminExist = adminRepository.existsById(longChatId);
        String specialistId;
        String ownerId;
        String textForMessage = "Записать на прием";
        String mainMenuData;
        if (isAdminExist) {
            mainMenuData = botMethod.callData_backToAdminMenu;
            Administrator administrator = adminRepository.findById(longChatId).get();
            ownerId = administrator.getOwnerId();
            if (!administrator.getOwnerId().equals(stringChatId)) {
                specialistId = administrator.getCurrentSpecialistId();
                String name = specialistRepository.findById(Long.parseLong(specialistId)).get().receiveShortName();
                textForMessage = "Записать на прием к специалисту " + name;
            }
        } else {
            ownerId = specialistRepository.findById(longChatId).get().getOwnerId();
            mainMenuData = botMethod.callData_backToSpecMenu;
        }

        List<Client> clients = clientRepository.findByOwnerId(ownerId);
        if (clients.isEmpty()) {
            textForMessage = "Нет клиентов для записи";
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = botMethod.createClientsButtonSet(clientIdTeg, clients, mainMenuData);
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        executeEditMessageText(editMessageText);
    }

    private String receiveTextForMessage(long longChatId, String stringChatId, String textForMessage) {
        String text;
        boolean isAdminExist = adminRepository.existsById(longChatId);
        if (isAdminExist && adminRepository.findById(longChatId).get().getOwnerId().equals(stringChatId) ||
                specialistRepository.existsById(longChatId)) {
            text = textForMessage;
            tempData.put(stringChatId, input_client_surname);
        } else if (isAdminExist && !adminRepository.findById(longChatId).get().getCurrentSpecialistId().isEmpty()) {
            Specialist specialist = specialistRepository.
                    findById(Long.parseLong(adminRepository.findById(longChatId).get().getCurrentSpecialistId())).get();
            text = "Специалист: " + specialist.receiveShortName() + "\n" + textForMessage;
            tempData.put(stringChatId, input_client_surname);
        } else {
            text = "Сначала необходимо выбрать специалиста.";
        }
        return text;
    }

    private EditMessageText receiveClientsSet(long longChatId, long messageId, String dataSymbol, String textForMessage) {
        String ownerId;
        String mainMenuData;
        if (adminRepository.existsById(longChatId)) {
            Administrator administrator = adminRepository.findById(longChatId).get();
            mainMenuData = botMethod.callData_backToAdminMenu;
            ownerId = administrator.getOwnerId();
        } else {
            Specialist specialist = specialistRepository.findById(longChatId).get();
            mainMenuData = botMethod.callData_backToSpecMenu;
            ownerId = specialist.getOwnerId();
        }
        List<Client> clients = clientRepository.findByOwnerId(ownerId).stream().filter(cli -> cli.getSurname().toUpperCase().startsWith(dataSymbol)).toList();
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(botMethod.createClientsButtonSet(clientIdTeg, clients, mainMenuData));
        return editMessageText;
    }

/*
    private EditeSendMessage receiveClientsMenu(long longChatId, String stringChatId, long messageId, String clientId, String textForMessage, String backToMenu) {
        EditeSendMessage editeSendMessage = receiveCreatedEditMessage(longChatId, messageId, textForMessage);
        Client client = clientRepository.findById(Long.parseLong(clientId)).get();

    }

 */


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