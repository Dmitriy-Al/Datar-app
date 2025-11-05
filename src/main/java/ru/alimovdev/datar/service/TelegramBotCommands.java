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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static ru.alimovdev.datar.service.ScheduleType.*;


@Slf4j
@Component
public class TelegramBotCommands extends TelegramLongPollingBot {
    private final AppointmentRepository appointmentRepository;

    private final TelegramBotMethods botMethod = new TelegramBotMethods();


    private final HashMap<String, String> tempData = new HashMap<>();
    private final HashMap<String, Long> savedClientId = new HashMap<>();
    private final HashMap<String, String> inputtedName = new HashMap<>();
    private final HashMap<String, String> inputtedSurname = new HashMap<>();
    private final HashMap<String, String> inputtedPatronymic = new HashMap<>();
    private final HashMap<String, String> returnData = new HashMap<>();
    private final HashMap<String, String> registerPassword = new HashMap<>();
    private final HashMap<String, String> savedWorkSchedule = new HashMap<>();
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


    final String backTag = "⏎ Назад в меню";
    final String specIdDataTag = "SID";
    final String clientFirstTag = "SYM";
    final String clientIdTag = "CLD";
    final String chooseDateTag = "CHM";
    final String chooseBeginTag = "CBT";
    final String chooseEndTag = "CET";
    final String beginWorkTag = "SBW";
    final String endWorkTag = "SEW";
    final String beginWeekWorkTag = "BWW";
    final String endWeekWorkTag = "EWW";
    final String beginDayWorkTag = "BDW";
    final String endDayWorkTag = "EDW";
    final String weekDayTag = "WDT";
    final String chooseWeekDayTag = "CWD";
    final String beginHourTag = "BHT";
    final String endHourTag = "EHT";
    final String scheduleTag = "SHT";
    final String chooseWeekendTag = "CWT";


    final static String callData_clientsList = "CLILIST";

    private final String[] textsOwnerAdminSettings = {"Часы работы", "Расписание специалиста", botMethod.callData_backToAdminMenu};
    private final String[] textsOwnerSpecSettings = {"Часы работы", "Расписание", botMethod.callData_backToSpecMenu};
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


    public TelegramBotCommands(AppointmentRepository appointmentRepository) {
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
        this.appointmentRepository = appointmentRepository;
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

            if (messageText.equals("3") || messageText.equals("/start")) { // клавиатура
                tempData.put(stringChatId, "");
                savedMessageId.put(stringChatId, intMessageId);
                if (adminRepository.existsById(longChatId)) {
                    String textForMenu = createTextForMenu(longChatId, stringChatId);
                    executeSendMessage(botMethod.createSpecialistMenu(stringChatId, textForMenu, textsForAdminButtons));
                } else if (specialistRepository.existsById(longChatId)) {
                    String textForMenu = createTextForMenu(longChatId, stringChatId);
                    executeSendMessage(botMethod.createSpecialistMenu(stringChatId, textForMenu, textsForSpecButtons));
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
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, botMethod.callData_backToUserMenu));
                executeEditMessageText(editMessageText);


            } else if (callbackData.equals(botMethod.callData_regAsSpecialist)) {
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Введите вашу фамилию и отправьте сообщение в чат");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, botMethod.callData_backToUserMenu));
                executeEditMessageText(editMessageText);
                tempData.put(stringChatId, input_spec_surname); // Регистрация специалиста

            } else if (callbackData.equals(botMethod.callData_regAsAdmin)) {
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Введите вашу фамилию и отправьте сообщение в чат");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, botMethod.callData_backToUserMenu));
                executeEditMessageText(editMessageText);
                tempData.put(stringChatId, input_admin_surname); // Регистрация администратора

            } else if (callbackData.equals(botMethod.callData_backToAdminMenu)) {
                String textForMenu = createTextForMenu(longChatId, stringChatId);
                executeEditMessageText(botMethod.createSpecialistMenu(longChatId, messageId, textForMenu, textsForAdminButtons));
            } else if (callbackData.equals(botMethod.callData_backToSpecMenu)) {
                String textForMenu = createTextForMenu(longChatId, stringChatId);
                executeEditMessageText(botMethod.createSpecialistMenu(longChatId, messageId, textForMenu, textsForSpecButtons));
            } else if (callbackData.equals("Добавить нового клиента")) {
                createNewClientProcess(longChatId, stringChatId, messageId);
            } else if (callbackData.equals("Выбор специалиста")) {

                String specialistIdList = adminRepository.findById(longChatId).get().getSpecialistIdList();
                if (!specialistIdList.isEmpty()) { // 777/333/111/
                    String[] idList = specialistIdList.split("/");
                    int length = idList.length;
                    String[] textsForButtons = new String[length];
                    String[] textsCallBackData = new String[length];
                    for (int i = 0; i < idList.length; i++) {
                        String name = specialistRepository.findById(Long.parseLong(idList[i])).get().receiveShortName();
                        textsForButtons[i] = name;
                        textsCallBackData[i] = idList[i];
                    }
                    executeEditMessageText(botMethod.createSpcListMenu(longChatId, messageId, "Выберите Специалиста", textsForButtons, textsCallBackData, specIdDataTag));
                } else {
                    EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "У вас нет специалистов");
                    editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, botMethod.callData_backToAdminMenu));
                    executeEditMessageText(editMessageText);
                }

            } else if (callbackData.contains(specIdDataTag)) {
                String data = callbackData.replace(specIdDataTag, "");
                String name = specialistRepository.findById(Long.parseLong(data)).get().receiveShortName();
                Administrator administrator = adminRepository.findById(longChatId).get();
                administrator.setCurrentSpecialistId(data);
                adminRepository.save(administrator);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Выбран специалист " + name);
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, botMethod.callData_backToAdminMenu));
                executeEditMessageText(editMessageText);
            } else if (callbackData.equals("Посмотреть запись")) {
                Iterable<Client> clients = clientRepository.findAll();
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Список клиентуры:\n" + clients);
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, botMethod.callData_backToAdminMenu));
                executeEditMessageText(editMessageText);
            } else if (callbackData.equals("Записать на прием")) {
                String mainMenuData = adminRepository.existsById(longChatId) ? botMethod.callData_backToAdminMenu : botMethod.callData_backToSpecMenu;
                String textForMessage = receiveTextForMessage(longChatId, stringChatId, "Выберите первую букву фамилии клиента");
                executeEditMessageText(botMethod.searchClient(longChatId, messageId, textForMessage, clientFirstTag, mainMenuData));

            } else if (callbackData.equals(callData_clientsList)) {
                showAllClients(longChatId, messageId, stringChatId);

            } else if (callbackData.contains(clientFirstTag)) {
                String dataText = callbackData.replace(clientFirstTag, "");
                executeEditMessageText(receiveClientsSet(longChatId, messageId, dataText, "Выберите клиента из списка"));

                // Выбор даты для записи клиента
            } else if (callbackData.contains(clientIdTag)) {
                long clientId = savedClientId.get(stringChatId) == null ?
                        Long.parseLong(callbackData.replace(clientIdTag, "")) : savedClientId.get(stringChatId);
                savedClientId.put(stringChatId, clientId);
                Client client = clientRepository.findById(clientId).get();
                String mainMenuData = adminRepository.existsById(longChatId) ? botMethod.callData_backToAdminMenu : botMethod.callData_backToSpecMenu;
                String textForMessage = "Выберите дату для записи клиента " + client.receiveShortName();
                LocalDate date = LocalDate.now();
                DateTimeFormatter format = DateTimeFormatter.ofPattern("d MMM");
                DateTimeFormatter formatYear = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                int count = 0;
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // коллекция коллекций с горизонтальным рядом кнопок, создаёт вертикальный ряд кнопок
                List<InlineKeyboardButton> rowInlineButtonBack = new ArrayList<>();
                for (int i = 0; i < 15; i++) {
                    List<InlineKeyboardButton> rowInlineButton = new ArrayList<>();
                    for (int x = 0; x < 4; x++) {
                        InlineKeyboardButton button = new InlineKeyboardButton(format.format(date.plus(count, DAYS)));
                        button.setCallbackData(chooseDateTag + formatYear.format(date.plus(count, DAYS)));
                        rowInlineButton.add(button);
                        count++;
                    }
                    rowsInline.add(rowInlineButton);
                }
                InlineKeyboardButton button = new InlineKeyboardButton(mainMenuData);
                button.setCallbackData(mainMenuData);
                rowInlineButtonBack.add(button);
                rowsInline.add(rowInlineButtonBack);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

                // Выбор даты для записи клиента
            } else if (callbackData.contains(chooseDateTag)) {
                String date = callbackData.replace(chooseDateTag, "");
                String mainMenuData;
                String textForMessage;
                DateTimeFormatter formatYear = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                DateTimeFormatter formatHour = DateTimeFormatter.ofPattern("k");
                boolean isToday = formatYear.format(LocalDate.now()).equals(date);

                if (adminRepository.existsById(longChatId)) {
                    mainMenuData = botMethod.callData_backToAdminMenu;
                    Administrator administrator = adminRepository.findById(longChatId).get();
                    textForMessage = createAppointmentText(administrator.getCurrentSpecialistId(), date);
                } else {
                    mainMenuData = botMethod.callData_backToSpecMenu;
                    textForMessage = createAppointmentText(stringChatId, date);
                }
                textForMessage += "\nВыберите время для записи:";
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // коллекция коллекций с горизонтальным рядом кнопок, создаёт вертикальный ряд кнопок
                List<InlineKeyboardButton> rowInlineButtonBack = new ArrayList<>();
                int beginHour = isToday ? Integer.parseInt(formatHour.format(LocalTime.now())) + 1 : 8;
                int endHour = 22;

                for (int i = beginHour; i <= endHour - 1; i++) {
                    List<InlineKeyboardButton> rowInlineButton = new ArrayList<>();
                    for (int x = 0; x < 60; x += 10) {
                        String hour = i > 9 ? String.valueOf(i) : "0" + i;
                        String minute = x > 0 ? String.valueOf(x) : "0" + x;
                        InlineKeyboardButton button = new InlineKeyboardButton(i + ":" + minute);
                        button.setCallbackData(chooseBeginTag + date + " - " + hour + ":" + minute);
                        rowInlineButton.add(button);
                    }
                    rowsInline.add(rowInlineButton);
                }
                InlineKeyboardButton menuButton = new InlineKeyboardButton(mainMenuData);
                menuButton.setCallbackData(mainMenuData);
                rowInlineButtonBack.add(menuButton);
                InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
                backButton.setCallbackData(clientIdTag);
                rowInlineButtonBack.add(backButton);
                rowsInline.add(rowInlineButtonBack);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(chooseBeginTag)) {
                String time = callbackData.replace(chooseBeginTag, ""); // 26.10.2025 - 09:00
                String mainMenuData = adminRepository.existsById(longChatId) ? botMethod.callData_backToAdminMenu : botMethod.callData_backToSpecMenu;
                String textForMessage = "Выберите время окончания " + time;
                String[] hourAndMinute = time.split(" - ")[1].split(":");
                int beginHour = Integer.parseInt(hourAndMinute[0]);
                int beginMinute = Integer.parseInt(hourAndMinute[1]);
                int endHour = 22;

                if (beginMinute == 50) {
                    beginMinute = 0;
                    beginHour += 1;
                } else {
                    beginMinute += 10;
                }

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // коллекция коллекций с горизонтальным рядом кнопок, создаёт вертикальный ряд кнопок
                List<InlineKeyboardButton> rowInlineButtonBack = new ArrayList<>();

                for (int i = beginHour; i <= endHour - 1; i++) {
                    List<InlineKeyboardButton> rowInlineButton = new ArrayList<>();
                    for (int x = beginMinute; x < 60; x += 10) {
                        String hour = i > 9 ? String.valueOf(i) : "0" + i;
                        String minute = x > 0 ? String.valueOf(x) : "0" + x;
                        InlineKeyboardButton button = new InlineKeyboardButton(i + ":" + minute);
                        button.setCallbackData(chooseEndTag + time + "/" + hour + ":" + minute);
                        rowInlineButton.add(button);
                    }
                    beginMinute = 0;
                    rowsInline.add(rowInlineButton);
                }
                InlineKeyboardButton menuButton = new InlineKeyboardButton(mainMenuData);
                menuButton.setCallbackData(mainMenuData);
                rowInlineButtonBack.add(menuButton);
                InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
                backButton.setCallbackData(chooseDateTag + (time.split(" - ")[0]));
                rowInlineButtonBack.add(backButton);
                rowsInline.add(rowInlineButtonBack);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(chooseEndTag)) {
                String timeData = callbackData.replace(chooseEndTag, ""); // 26.10.2025 - 09:00/09:10
                Client client = clientRepository.findById(savedClientId.get(stringChatId)).get();
                List<Appointment> appointments;
                String existAppointmentTime = "";
                boolean isWrongAppointment = false;

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm");
                String[] splitTimeData = timeData.split(" - ");
                String date = splitTimeData[0]; //  26.10.2025
                LocalDateTime beginTime = LocalDateTime.parse(date + " - " + splitTimeData[1].split("/")[0], formatter); //09:00
                LocalDateTime endTime = LocalDateTime.parse(date + " - " + splitTimeData[1].split("/")[1], formatter); //09:10

                if (adminRepository.existsById(longChatId)) {
                    String specialistId = adminRepository.findById(longChatId).get().getCurrentSpecialistId();
                    appointments = appointmentRepository.findBySpecialistId(specialistId);

                } else {
                    appointments = appointmentRepository.findBySpecialistId(stringChatId);
                }
                appointments.addAll(appointmentRepository.findByClientId(client.getId()));

                for (Appointment ap : appointments) {
                    // String[] splitAppointmentTimeData = ap.getAppointmentDateTime().replace("#", "").split(" - ");
                    String[] splitAppointmentTimeData = ap.getAppointmentDateTime().split(" - ");
                    String appointmentDate = splitAppointmentTimeData[0]; //  26.10.2025
                    LocalDateTime appointmentBeginTime = LocalDateTime.parse(appointmentDate + " - " + splitAppointmentTimeData[1].split("/")[0], formatter); //09:00
                    LocalDateTime appointmentEndTime = LocalDateTime.parse(appointmentDate + " - " + splitAppointmentTimeData[1].split("/")[1], formatter); //09:10

                    if (beginTime.isAfter(appointmentBeginTime) && beginTime.isBefore(appointmentEndTime) ||
                            endTime.isAfter(appointmentBeginTime) && endTime.isBefore(appointmentEndTime) ||
                            beginTime.isBefore(appointmentBeginTime) && endTime.isAfter(appointmentEndTime)) {
                        existAppointmentTime = appointmentDate + " - " + splitAppointmentTimeData[1].split("/")[0];
                        isWrongAppointment = true;
                        break;
                    }
                }

                String textForMessage = "Клиент записан на " + timeData;
                String mainMenuData = adminRepository.existsById(longChatId) ? botMethod.callData_backToAdminMenu : botMethod.callData_backToSpecMenu;
                Appointment appointment = new Appointment();
                if (specialistRepository.existsById(longChatId)) {
                    Specialist specialist1 = specialistRepository.findById(longChatId).get();
                    appointment.setOwnerId(specialist1.getOwnerId());
                    appointment.setSpecialistId(stringChatId);
                } else {
                    Administrator administrator = adminRepository.findById(longChatId).get();
                    appointment.setOwnerId(administrator.getOwnerId());
                    appointment.setSpecialistId(administrator.getCurrentSpecialistId());
                }
                appointment.setClientId(savedClientId.get(stringChatId));
                appointment.setAppointmentNote("");
                //  appointment.setAppointmentDateTime(timeData + "#"); // 26.10.2025 - 09:00/09:10#
                appointment.setAppointmentDateTime(timeData); // 26.10.2025 - 09:00/09:10

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // коллекция коллекций с горизонтальным рядом кнопок, создаёт вертикальный ряд кнопок
                List<InlineKeyboardButton> firstRowInlineButton = new ArrayList<>();

                InlineKeyboardButton menuButton = new InlineKeyboardButton(mainMenuData);
                menuButton.setCallbackData(mainMenuData);
                firstRowInlineButton.add(menuButton);

                if (isWrongAppointment) {
                    textForMessage = "Нельзя записать клиента на данную дату, т.к. у клиента или специалиста уже имеется запись на время: " + existAppointmentTime;
                    InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
                    backButton.setCallbackData(chooseBeginTag + date + " - " + splitTimeData[1].split("/")[0]);
                    firstRowInlineButton.add(backButton);
                } else {
                    appointmentRepository.save(appointment);
                }
                rowsInline.add(firstRowInlineButton);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals(botMethod.callData_specSettings)) {
                executeEditMessageText(botMethod.createSpecialistMenu(longChatId, messageId, "Настройки специалиста", textsOwnerSpecSettings));


            } else if (callbackData.equals(botMethod.callData_adminSettings)) {
                executeEditMessageText(botMethod.createSpecialistMenu(longChatId, messageId, "Настройки администратора", textsOwnerAdminSettings));

            } else if (callbackData.equals("Часы работы")) {
                String workTime;
                String backData;

                if (adminRepository.existsById(longChatId)) {
                    Administrator administrator = adminRepository.findById(longChatId).get();
                    backData = botMethod.callData_adminSettings;
                    workTime = administrator.getWorkTimeLength().replace("/", ":00 до ");
                    ;
                } else {
                    Specialist specialist = specialistRepository.findById(longChatId).get();
                    backData = botMethod.callData_specSettings;
                    workTime = specialist.getWorkTimeLength().replace("/", ":00 до ");
                }

                String textForMessage = "Установленные часы работы: с " + workTime + ":00 ч.\nВыберите новое время начала рабочего дня.";
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = beginWorkTime(beginWorkTag);
                List<InlineKeyboardButton> rowInlineButtonBack = new ArrayList<>();
                InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
                backButton.setCallbackData(backData);
                rowInlineButtonBack.add(backButton);
                rowsInline.add(rowInlineButtonBack);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(beginWorkTag)) {
                int time = Integer.parseInt(callbackData.replace(beginWorkTag, ""));
                String textForMessage = "Выберите время окончания рабочего дня";
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = endWorkTime(time, endWorkTag);
                List<InlineKeyboardButton> rowInlineButtonBack = new ArrayList<>();
                InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
                backButton.setCallbackData("Часы работы");
                rowInlineButtonBack.add(backButton);
                rowsInline.add(rowInlineButtonBack);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(endWorkTag)) {
                String date = callbackData.replace(endWorkTag, "");
                String backData;

                if (adminRepository.existsById(longChatId)) {
                    backData = botMethod.callData_adminSettings;
                    Administrator administrator = adminRepository.findById(longChatId).get();
                    administrator.setWorkTimeLength(date);
                    adminRepository.save(administrator);
                } else {
                    backData = botMethod.callData_specSettings;
                    Specialist specialist = specialistRepository.findById(longChatId).get();
                    specialist.setWorkTimeLength(date);
                    specialistRepository.save(specialist);
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Время работы установлено.");
                InlineKeyboardMarkup inlineKeyboardMarkup = botMethod.receiveOneButtonMenu("Назад", backData);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals("Расписание специалиста")) {
                String text;
                String specialistId = adminRepository.findById(longChatId).get().getCurrentSpecialistId();
                InlineKeyboardMarkup inlineKeyboardMarkup;

                if (specialistId != null) {
                    text = "Здесь вы можете установить график работы для специалиста: " + specialistRepository.findById(Long.parseLong(specialistId)).get().receiveShortName();
                    String[] textsOwnerAdminSettings = {"Фиксированный график", "Четный/нечетный график", "Скользящий график", botMethod.callData_backToAdminMenu};
                    inlineKeyboardMarkup = botMethod.createButtonSet(textsOwnerAdminSettings);
                } else {
                    text = "Специалист не выбран.";
                    inlineKeyboardMarkup = botMethod.receiveOneButtonMenu("Назад", botMethod.callData_adminSettings);
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals("Фиксированный график")) {
                savedWorkSchedule.remove(stringChatId);
                String specialistId = adminRepository.findById(longChatId).get().getCurrentSpecialistId();
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                Specialist specialist = specialistRepository.findById(Long.parseLong(specialistId)).get();
                String text = "Специалист: " + specialist.receiveShortName() + "\nАктуальный график:\n" + FIX_DAYS.receiveScheduleString(specialist.getReceptionSchedule()) + "\nУстановите часы работы  для каждого дня недели. Для установки выходных дней нажмите клавишу \"Выходной день\".\nВыберите время начала рабочего дня для понедельника.";
                List<List<InlineKeyboardButton>> rowsInline = createBeginScheduleButtonsSet();
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(beginWeekWorkTag)) {
                String time = callbackData.replace(beginWeekWorkTag, "");
                String savedScheduleTime = "";
                String[] scheduleTime = savedWorkSchedule.get(stringChatId) == null ? new String[0] : savedWorkSchedule.get(stringChatId).split("/");
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                String text = "";

                if (time.isEmpty()) {
                    text = savedScheduleTime + " Выберите время окончания рабочего дня для понедельника.";
                    List<List<InlineKeyboardButton>> rowsInline = createBeginScheduleButtonsSet();
                    inlineKeyboardMarkup.setKeyboard(rowsInline);
                } else if (scheduleTime.length == 0) {
                    savedWorkSchedule.put(stringChatId, time + "/");
                    text = FIX_DAYS.receiveScheduleString(savedWorkSchedule.get(stringChatId) + FIX_DAYS.getLabel()) + "\nВыберите время начала рабочего дня для вторника.";
                    List<List<InlineKeyboardButton>> rowsInline = createBeginScheduleButtonsSet();
                    inlineKeyboardMarkup.setKeyboard(rowsInline);
                } else if (scheduleTime.length == 1) {
                    savedScheduleTime = savedWorkSchedule.get(stringChatId);
                    savedWorkSchedule.put(stringChatId, savedScheduleTime + time + "/");
                    text = FIX_DAYS.receiveScheduleString(savedWorkSchedule.get(stringChatId) + FIX_DAYS.getLabel()) + "\nВыберите время начала рабочего дня для среды.";
                    List<List<InlineKeyboardButton>> rowsInline = createBeginScheduleButtonsSet();
                    inlineKeyboardMarkup.setKeyboard(rowsInline);
                } else if (scheduleTime.length == 2) {
                    savedScheduleTime = savedWorkSchedule.get(stringChatId);
                    savedWorkSchedule.put(stringChatId, savedScheduleTime + time + "/");
                    text = FIX_DAYS.receiveScheduleString(savedWorkSchedule.get(stringChatId) + FIX_DAYS.getLabel()) + "\nВыберите время начала рабочего дня для четверга.";
                    List<List<InlineKeyboardButton>> rowsInline = createBeginScheduleButtonsSet();
                    inlineKeyboardMarkup.setKeyboard(rowsInline);
                } else if (scheduleTime.length == 3) {
                    savedScheduleTime = savedWorkSchedule.get(stringChatId);
                    savedWorkSchedule.put(stringChatId, savedScheduleTime + time + "/");
                    text = FIX_DAYS.receiveScheduleString(savedWorkSchedule.get(stringChatId) + FIX_DAYS.getLabel()) + "\nВыберите время начала рабочего дня для пятницы.";
                    List<List<InlineKeyboardButton>> rowsInline = createBeginScheduleButtonsSet();
                    inlineKeyboardMarkup.setKeyboard(rowsInline);
                } else if (scheduleTime.length == 4) {
                    savedScheduleTime = savedWorkSchedule.get(stringChatId);
                    savedWorkSchedule.put(stringChatId, savedScheduleTime + time + "/");
                    text = FIX_DAYS.receiveScheduleString(savedWorkSchedule.get(stringChatId) + FIX_DAYS.getLabel()) + "\nВыберите время начала рабочего дня для субботы.";
                    List<List<InlineKeyboardButton>> rowsInline = createBeginScheduleButtonsSet();
                    inlineKeyboardMarkup.setKeyboard(rowsInline);
                } else if (scheduleTime.length == 5) {
                    savedScheduleTime = savedWorkSchedule.get(stringChatId);
                    savedWorkSchedule.put(stringChatId, savedScheduleTime + time + "/");
                    text = FIX_DAYS.receiveScheduleString(savedWorkSchedule.get(stringChatId) + FIX_DAYS.getLabel()) + "\nВыберите время начала рабочего дня для воскресенья.";
                    List<List<InlineKeyboardButton>> rowsInline = createBeginScheduleButtonsSet();
                    inlineKeyboardMarkup.setKeyboard(rowsInline);
                } else if (scheduleTime.length == 6) {
                    savedScheduleTime = savedWorkSchedule.get(stringChatId) + time + FIX_DAYS.getLabel();
                    Administrator administrator = adminRepository.findById(longChatId).get();
                    Specialist specialist = specialistRepository.findById(Long.parseLong(administrator.getCurrentSpecialistId())).get();
                    specialist.setReceptionSchedule(savedScheduleTime);
                    specialistRepository.save(specialist);
                    text = FIX_DAYS.receiveScheduleString(savedScheduleTime) + "\nРасписание для специалиста " + specialist.receiveShortName() + " установлено.";
                    inlineKeyboardMarkup = botMethod.receiveOneButtonMenu("Назад", "Расписание специалиста");
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);


            } else if (callbackData.contains(endWeekWorkTag)) {
                String time = callbackData.replace(endWeekWorkTag, "");
                String[] scheduleTime = savedWorkSchedule.get(stringChatId) == null ? new String[0] : savedWorkSchedule.get(stringChatId).split("/");
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                String text = "";

                if (scheduleTime.length == 0) {
                    text = "Выберите время окончания рабочего дня для понедельника.";
                    List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), beginWeekWorkTag);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);

                } else if (scheduleTime.length == 1) {
                    text = "Выберите время окончания рабочего дня для вторника.";
                    List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), beginWeekWorkTag);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);

                } else if (scheduleTime.length == 2) {
                    text = "Выберите время окончания рабочего дня для среды.";
                    List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), beginWeekWorkTag);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);

                } else if (scheduleTime.length == 3) {
                    text = "Выберите время окончания рабочего дня для четверга.";
                    List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), beginWeekWorkTag);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);

                } else if (scheduleTime.length == 4) {
                    text = "Выберите время окончания рабочего дня для пятницы.";
                    List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), beginWeekWorkTag);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);

                } else if (scheduleTime.length == 5) {
                    text = "Выберите время окончания рабочего дня для субботы.";
                    List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), beginWeekWorkTag);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);

                } else if (scheduleTime.length == 6) {
                    text = "Выберите время окончания рабочего дня для воскресенья.";
                    List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), beginWeekWorkTag);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals("Четный/нечетный график")) {
                savedWorkSchedule.remove(stringChatId);
                String specialistId = adminRepository.findById(longChatId).get().getCurrentSpecialistId();
                Specialist specialist = specialistRepository.findById(Long.parseLong(specialistId)).get();
                String text = "Специалист: " + specialist.receiveShortName() + "\nАктуальный график:\n" + EVEN_ODD_DAYS.receiveScheduleString(specialist.getReceptionSchedule()) +
                        "\nВ этом меню вы можете установить рабочее время для четных и нечетных дней месяца.\nУстановите время начала рабочего дня специалиста для нечетного дня месяца.";
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = createBeginDayButtonsSet(endDayWorkTag);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(beginDayWorkTag)) {
                String time = callbackData.replace(beginDayWorkTag, "");
                String text = "";
                String savedScheduleTime = "";
                String[] scheduleTime = savedWorkSchedule.get(stringChatId) == null ? new String[0] : savedWorkSchedule.get(stringChatId).split("/");
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                if (scheduleTime.length == 0) {
                    savedWorkSchedule.put(stringChatId, time + "/");
                    text = "График работы:\n" + EVEN_ODD_DAYS.receiveScheduleString(savedWorkSchedule.get(stringChatId) + EVEN_ODD_DAYS.getLabel()) + "\nУстановите время начала рабочего дня специалиста для четного дня месяца.";
                    inlineKeyboardMarkup.setKeyboard(beginWorkTime(endDayWorkTag));
                } else if (scheduleTime.length == 1) {
                    savedScheduleTime = savedWorkSchedule.get(stringChatId);
                    savedWorkSchedule.put(stringChatId, savedScheduleTime + time + "/");
                    text = "График работы:\n" + EVEN_ODD_DAYS.receiveScheduleString(savedWorkSchedule.get(stringChatId) + EVEN_ODD_DAYS.getLabel()) + "\nВыберите выходной день или нажмите клавишу \"Готово\".";
                    inlineKeyboardMarkup = createWeekendButtonsSet("");
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(endDayWorkTag)) {
                String time = callbackData.replace(endDayWorkTag, "");
                String text = "";
                String[] scheduleTime = savedWorkSchedule.get(stringChatId) == null ? new String[0] : savedWorkSchedule.get(stringChatId).split("/");
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                if (scheduleTime.length == 0) {
                    text = "Установите время окончания рабочего дня специалиста для нечетного дня месяца.";
                    inlineKeyboardMarkup.setKeyboard(createEndDayButtonsSet(Integer.parseInt(time)));
                } else if (scheduleTime.length == 1) {
                    text = "Установите время окончания рабочего дня специалиста для четного дня месяца.";
                    inlineKeyboardMarkup.setKeyboard(createEndDayButtonsSet(Integer.parseInt(time)));
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);
            } else if (callbackData.contains(weekDayTag)) {
                String scheduleData = callbackData.replace(weekDayTag, "");
                String savedScheduleTime = savedWorkSchedule.get(stringChatId) + scheduleData;
                savedWorkSchedule.put(stringChatId, savedScheduleTime + "/");
                String[] data = savedScheduleTime.split("/");
                String choseDays = savedScheduleTime.replace(data[0], "").replace(data[1], "");
                String text = "Выберите выходной день или нажмите клавишу \"Готово\".";
                InlineKeyboardMarkup inlineKeyboardMarkup = createWeekendButtonsSet(choseDays);

                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(chooseWeekDayTag)) {
                String savedScheduleTime = savedWorkSchedule.get(stringChatId).
                        substring(0, savedWorkSchedule.get(stringChatId).length() - 1) + EVEN_ODD_DAYS.getLabel();
               // savedWorkSchedule.put(stringChatId, savedScheduleTime);
                long specialistId = Long.parseLong(adminRepository.findById(longChatId).get().getCurrentSpecialistId());
                Specialist specialist = specialistRepository.findById(specialistId).get();
                specialist.setReceptionSchedule(savedScheduleTime);
                specialistRepository.save(specialist);
                String textForMessage = "Обновлено расписание специалиста: " + specialist.receiveShortName() + "\n" + EVEN_ODD_DAYS.receiveScheduleString(savedScheduleTime);
                InlineKeyboardMarkup inlineKeyboardMarkup = botMethod.receiveOneButtonMenu("⏎ Назад", "Расписание специалиста");
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals("Скользящий график")) {
                savedWorkSchedule.remove(stringChatId);
                String specialistId = adminRepository.findById(longChatId).get().getCurrentSpecialistId();
                Specialist specialist = specialistRepository.findById(Long.parseLong(specialistId)).get();
                String text = "Специалист: " + specialist.receiveShortName() + "\nАктуальный график:\n" + ROLLING_CHART.receiveScheduleString(specialist.getReceptionSchedule()) +
                        "\nВ этом меню вы можете настроить скользящий рабочий график.\nУстановите время начала рабочего дня.";
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = createBeginDayButtonsSet(beginHourTag);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(beginHourTag)) {
                String time = callbackData.replace(beginHourTag, "");
                String text = "Выберите время окончания рабочего дня.";
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), endHourTag);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(endHourTag)) { //  8#16
                String time = callbackData.replace(endHourTag, "") + "/";
                savedWorkSchedule.put(stringChatId, time);
                String text = "График:\n" + ROLLING_CHART.receiveScheduleString(time + ROLLING_CHART.getLabel()) + "Установите интервал (график) рабочих и выходных дней.";
                InlineKeyboardMarkup inlineKeyboardMarkup = createScheduleButtonsSet(scheduleTag); //scheduleTag
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(scheduleTag)) {
                String schedule = callbackData.replace(scheduleTag, "");
                String savedScheduleTime = savedWorkSchedule.get(stringChatId) + schedule + "/";
                savedWorkSchedule.put(stringChatId, savedScheduleTime);
                String text = "*В этом меню предстоит выбрать дату начала выходных дней специалиста. Если в данный момент наступили выходные дни, значит надо выбрать дату начала следующих выходных.\nГрафик:\n" +
                        ROLLING_CHART.receiveScheduleString(savedScheduleTime + ROLLING_CHART.getLabel()) + "\nВыберите дату начала выходных дней*.";
                InlineKeyboardMarkup inlineKeyboardMarkup = createDateButtonsSet(chooseWeekendTag);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(chooseWeekendTag)) {
                String schedule = callbackData.replace(chooseWeekendTag, "");
                String savedScheduleTime = savedWorkSchedule.get(stringChatId) + schedule + ROLLING_CHART.getLabel();
                long specialistId = Long.parseLong(adminRepository.findById(longChatId).get().getCurrentSpecialistId());
                Specialist specialist = specialistRepository.findById(specialistId).get();
                specialist.setReceptionSchedule(savedScheduleTime);
                specialistRepository.save(specialist);
                String textForMessage = "Обновлено расписание специалиста: " + specialist.receiveShortName() + "\n" + ROLLING_CHART.receiveScheduleString(savedScheduleTime);
                InlineKeyboardMarkup inlineKeyboardMarkup = botMethod.receiveOneButtonMenu("⏎ Назад", "Расписание специалиста");
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);
            }






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
            editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, callData));
            executeEditMessageText(editMessageText);
        } else {
            editMessageText.setText("Невалидный ввод");
            editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, callData));
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
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, callData));
                executeEditMessageText(editMessageText);
            }
        } catch (NumberFormatException e) {
            editMessageText.setText("Невалидный ввод");
            //log.error("SendMessage execute error: " + e.getMessage());
        }
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, callData));
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
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, callData));
                executeEditMessageText(editMessageText);
                return true;
            }
        } catch (NumberFormatException e) {
            //log.error("SendMessage execute error: " + e.getMessage());
        }
        editMessageText.setText("Невалидный ввод");
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, callData));
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
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, callData));
        executeEditMessageText(editMessageText);
    }

    private void registerSpecialist(long longChatId, String stringChatId) {
        Specialist specialist = new Specialist();
        specialist.setTimeZone(0);
        specialist.setPassword("");
        specialist.setId(longChatId);
        specialist.setProfession("");
        specialist.setPhoneNumber("");
        specialist.setReceptionSchedule("");
        specialist.setReceptionSchedule("");
        specialist.setOwnerId(stringChatId);
        specialist.setAdministratorIdList("");
        specialist.setClientAppointmentRange("");
        specialist.setSpecialistId(stringChatId);
        specialist.setWorkTimeLength("08:00/22:00");
        specialist.setName(inputtedName.get(stringChatId));
        specialist.setSurname(inputtedSurname.get(stringChatId));
        specialist.setPatronymic(inputtedPatronymic.get(stringChatId));
        specialistRepository.save(specialist);

        long messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId,
                inputtedName.get(stringChatId) + " " + inputtedPatronymic.get(stringChatId) + ", спасибо за регистрацию!");
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, botMethod.callData_backToAdminMenu));
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
        administrator.setCurrentSpecialistId(null);
        administrator.setWorkTimeLength("08:00/22:00");
        administrator.setName(inputtedName.get(stringChatId));
        administrator.setSurname(inputtedSurname.get(stringChatId));
        administrator.setPatronymic(inputtedPatronymic.get(stringChatId));
        adminRepository.save(administrator);
        long messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId,
                inputtedName.get(stringChatId) + " " + inputtedPatronymic.get(stringChatId) + ", спасибо за регистрацию!");
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, botMethod.callData_backToAdminMenu));
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
        long messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backTag, callData));
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
        InlineKeyboardMarkup inlineKeyboardMarkup = botMethod.createClientsButtonSet(clientIdTag, clients, mainMenuData);
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
        editMessageText.setReplyMarkup(botMethod.createClientsButtonSet(clientIdTag, clients, mainMenuData));
        return editMessageText;
    }

    private String createAppointmentText(String specialistId, String date) { // date = "dd.MM.yyyy"
        StringBuilder stringBuilder = new StringBuilder();
        List<Appointment> appointments = appointmentRepository.findBySpecialistId(specialistId).stream().
                filter(it -> it.getAppointmentDateTime().contains(date)).sorted().toList();
        // Собираем specialistId
        Set<Long> clientIds = appointments.stream().map(Appointment::getClientId).collect(Collectors.toSet());
        // Загружаем специалистов
        Map<Long, Client> clientsMap = clientRepository.findByIdIn(clientIds).stream().
                collect(Collectors.toMap(Client::getId, Function.identity()));

        for (Appointment appointment : appointments) {
            Client clients = clientsMap.get(appointment.getClientId());
            stringBuilder.append(appointment.getAppointmentDateTime().replace("/", " • ")).append("  ").
                    append(clients.receiveShortName()).append("\n");
        }
        return "Запись на " + date + ":\n" + stringBuilder;
    }


    private String createTextForMenu(long longChatId, String stringChatId) {
        DateTimeFormatter formatYear = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate localDate = LocalDate.now();
        String textForMenu;
        if (adminRepository.existsById(longChatId)) {
            Administrator administrator = adminRepository.findById(longChatId).get();
            if (administrator.getCurrentSpecialistId() == null) {
                textForMenu = "Специалист: специалист не выбран";
            } else {
                Specialist specialist = specialistRepository.findById(Long.parseLong(administrator.getCurrentSpecialistId())).get();
                textForMenu = "Специалист: " + specialist.receiveShortName() + "\n" + createAppointmentText(administrator.getCurrentSpecialistId(), localDate.format(formatYear));
            }
        } else {
            Specialist specialist = specialistRepository.findById(longChatId).get();
            textForMenu = "Запись на сегодня:\n" + createAppointmentText(stringChatId, localDate.format(formatYear));
        }
        return textForMenu;
    }


    private void cleanMapData(String stringChatId) {
        tempData.remove(stringChatId);
        returnData.remove(stringChatId);
        inputtedName.remove(stringChatId);
        savedClientId.remove(stringChatId);
        savedMessageId.remove(stringChatId);
        inputtedSurname.remove(stringChatId);
        registerPassword.remove(stringChatId);
        savedWorkSchedule.remove(stringChatId);
        inputtedPatronymic.remove(stringChatId);
        inputtedPhoneNumber.remove(stringChatId);
        inputtedClientBirthdate.remove(stringChatId);
    }

    private List<List<InlineKeyboardButton>> beginWorkTime(String callBackData) {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // коллекция коллекций с горизонтальным рядом кнопок, создаёт вертикальный ряд кнопок
        int beginHour = 0;
        for (int i = 0; i < 6; i++) {
            List<InlineKeyboardButton> rowInlineButton = new ArrayList<>();
            for (int x = beginHour; x <= beginHour + 4 && x < 25; x++) {
                InlineKeyboardButton button = new InlineKeyboardButton(x + ":00");
                button.setCallbackData(callBackData + x);
                rowInlineButton.add(button);
            }
            beginHour += 5;
            rowsInline.add(rowInlineButton);
        }
        return rowsInline;
    }

    private List<List<InlineKeyboardButton>> endWorkTime(int time, String callBackData) {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // коллекция коллекций с горизонтальным рядом кнопок, создаёт вертикальный ряд кнопок
        int beginHour = time + 1;
        for (int i = 0; i < 6; i++) {
            List<InlineKeyboardButton> rowInlineButton = new ArrayList<>();
            for (int x = beginHour; x <= beginHour + 4 && x < 25; x++) {
                InlineKeyboardButton button = new InlineKeyboardButton(x + ":00");
                button.setCallbackData(callBackData + time + "#" + x);
                rowInlineButton.add(button);
            }
            beginHour += 5;
            rowsInline.add(rowInlineButton);
        }
        return rowsInline;
    }

    private List<List<InlineKeyboardButton>> createBeginScheduleButtonsSet() {
        List<List<InlineKeyboardButton>> rowsInline = beginWorkTime(endWeekWorkTag);
        List<InlineKeyboardButton> rowInlineButtons = new ArrayList<>();
        InlineKeyboardButton weekendButton = new InlineKeyboardButton("Выходной день");
        weekendButton.setCallbackData(beginWeekWorkTag + "*");
        rowInlineButtons.add(weekendButton);
        InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
        backButton.setCallbackData("Расписание специалиста");
        rowInlineButtons.add(backButton);
        rowsInline.add(rowInlineButtons);
        return rowsInline;
    }

    private List<List<InlineKeyboardButton>> createEndScheduleButtonsSet(int time, String callBackData) { //TODO endWorkTime
        List<List<InlineKeyboardButton>> rowsInline = endWorkTime(time, callBackData); // beginWeekWorkTag
        List<InlineKeyboardButton> rowInlineButtonBack = new ArrayList<>();
        InlineKeyboardButton backButton = new InlineKeyboardButton("Расписание специалиста");
        backButton.setCallbackData("Расписание специалиста");
        rowInlineButtonBack.add(backButton);
        rowsInline.add(rowInlineButtonBack);
        return rowsInline;
    }

    private List<List<InlineKeyboardButton>> createBeginDayButtonsSet(String callBackData) {
        List<List<InlineKeyboardButton>> rowsInline = beginWorkTime(callBackData);
        List<InlineKeyboardButton> rowInlineButtonBack = new ArrayList<>();
        InlineKeyboardButton backButton = new InlineKeyboardButton("Расписание специалиста");
        backButton.setCallbackData("Расписание специалиста");
        rowInlineButtonBack.add(backButton);
        rowsInline.add(rowInlineButtonBack);
        return rowsInline;
    }

    private List<List<InlineKeyboardButton>> createEndDayButtonsSet(int time) {
        List<List<InlineKeyboardButton>> rowsInline = endWorkTime(time, beginDayWorkTag);
        List<InlineKeyboardButton> rowInlineButtonBack = new ArrayList<>();
        InlineKeyboardButton backButton = new InlineKeyboardButton("Расписание специалиста");
        backButton.setCallbackData("Расписание специалиста");
        rowInlineButtonBack.add(backButton);
        rowsInline.add(rowInlineButtonBack);
        return rowsInline;
    }

    private InlineKeyboardMarkup createWeekendButtonsSet(String choseDays) {
        Map<String, String> daysOfWeek = new LinkedHashMap<>();
        if (!choseDays.contains("1")) daysOfWeek.put("Понедельник", weekDayTag + "1");
        if (!choseDays.contains("2")) daysOfWeek.put("Вторник", weekDayTag + "2");
        if (!choseDays.contains("3")) daysOfWeek.put("Среда", weekDayTag + "3");
        if (!choseDays.contains("4")) daysOfWeek.put("Четверг", weekDayTag + "4");
        if (!choseDays.contains("5")) daysOfWeek.put("Пятница", weekDayTag + "5");
        if (!choseDays.contains("6")) daysOfWeek.put("Суббота", weekDayTag + "6");
        if (!choseDays.contains("0")) daysOfWeek.put("Воскресенье", weekDayTag + "0");
        daysOfWeek.put("Готово", chooseWeekDayTag);
        return botMethod.createDataButtonSet(daysOfWeek, "");
    }

    private InlineKeyboardMarkup createScheduleButtonsSet(String callBackData) {
        Map<String, String> schedule = new LinkedHashMap<>();
        schedule.put("1 через 1", "1");
        schedule.put("2 через 2", "2");
        schedule.put("3 через 3", "3");
        schedule.put("4 через 4", "4");
        schedule.put("Назад", "Расписание специалиста");
        return botMethod.createDataButtonSet(schedule, callBackData);
    }

    private InlineKeyboardMarkup createDateButtonsSet(String callBackData) {
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter buttonFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, String> weekend = new LinkedHashMap<>();
        weekend.put(buttonFormat.format(localDate.plus(1L, DAYS)), dataFormat.format(localDate.plus(1L, DAYS)));
        weekend.put(buttonFormat.format(localDate.plus(2L, DAYS)), dataFormat.format(localDate.plus(2L, DAYS)));
        weekend.put(buttonFormat.format(localDate.plus(3L, DAYS)), dataFormat.format(localDate.plus(3L, DAYS)));
        weekend.put(buttonFormat.format(localDate.plus(4L, DAYS)), dataFormat.format(localDate.plus(4L, DAYS)));
        weekend.put(buttonFormat.format(localDate.plus(5L, DAYS)), dataFormat.format(localDate.plus(5L, DAYS)));
        weekend.put("Назад", "Расписание специалиста");
        return botMethod.createDataButtonSet(weekend, callBackData);
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