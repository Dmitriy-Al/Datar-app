package ru.alimovdev.datar.service;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static ru.alimovdev.datar.service.ScheduleType.*;


@Slf4j
@Service
@Component
@EnableScheduling
public class TelegramBotCommands extends TelegramLongPollingBot {
    private final AppointmentRepository appointmentRepository;

    private final TelegramBotUtilMethods botMethod = new TelegramBotUtilMethods();

    private final Map<String, String> tempData = new ConcurrentHashMap<>();
    private final Map<String, Long> savedClientId = new ConcurrentHashMap<>();
    private final Map<String, Long> savedClientTgId = new ConcurrentHashMap<>();
    private final Map<String, String> inputtedName = new ConcurrentHashMap<>();
    private final Map<String, String> inputtedSurname = new ConcurrentHashMap<>();
    private final Map<String, String> inputtedPatronymic = new ConcurrentHashMap<>();
    //private final Map<String, String> returnData = new ConcurrentHashMap<>();
    // private final Map<String, String> registerPassword = new ConcurrentHashMap<>();
    private final Map<String, String> savedWorkSchedule = new ConcurrentHashMap<>();
    private final Map<String, Integer> savedMessageId = new ConcurrentHashMap<>();
    private final Map<String, String> inputtedPhoneNumber = new ConcurrentHashMap<>();
    private final Map<String, String> inputtedClientBirthdate = new ConcurrentHashMap<>();
    private final Map<String, String> registrationPassword = new ConcurrentHashMap<>();
    //  private final Map<Appointment, SendMessage> approveAppointmentMap = new ConcurrentHashMap<>();
    private final List<SendMessage> approveAppointment = new CopyOnWriteArrayList<>();

    /**
     * Строки-константы, добавляемые в Map tempData. В процессе взаимодействия с ботом может понадобиться ввод некоторых
     * данных в чат и до момента отправки этих данных, функция добавляет в Map tempData строку-триггер, в таком случае
     * данные введенные пользователем интерпретируются должным образом.
     */
    /*
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
     */

    final String input_remark = "INPUT_REMARK"; // добавление заметки для клиента
    final String input_spec_surname = "INPUT_SPEC_SECOND_NAME"; //
    final String input_spec_name = "INPUT_SPEC_NAME"; //
    final String input_spec_patronymic = "INPUT_SPEC_PATRONYMIC"; //
    final String input_admin_surname = "INPUT_ADMIN_SECOND_NAME"; //
    final String input_admin_name = "INPUT_ADMIN_NAME"; //
    final String input_admin_patronymic = "INPUT_ADMIN_PATRONYMIC"; //
    final String input_client_surname = "INPUT_CLIENT_SECOND_NAME"; //
    final String input_client_name = "INPUT_CLIENT_NAME"; //
    final String input_client_patronymic = "INPUT_CLIENT_PATRONYMIC"; //
    final String input_client_phoneNumber = "INPUT_CLIENT_PHONE"; //
    final String input_client_birthdate = "INPUT_CLIENT_BIRTHDATE"; //


    final String callbackData_back = "⏎ Назад в меню";
    final String callbackData_specId = "SID";
    final String callbackData_clientFirst = "SYM";
    final String callbackData_clientIdAppointment = "CLA";
    final String callbackData_chooseDate = "CHM";
    final String callbackData_chooseBegin = "CBT";
    final String callbackData_chooseEnd = "CET";
    final String callbackData_beginWork = "SBW";
    final String callbackData_endWork = "SEW";
    final String callbackData_beginWeekWork = "BWW";
    final String callbackData_endWeekWork = "EWW";
    final String callbackData_beginDayWork = "BDW";
    final String callbackData_endDayWork = "EDW";
    final String callbackData_weekDay = "WDT";
    final String callbackData_chooseWeekDay = "CWD";
    final String callbackData_beginHour = "BHT";
    final String callbackData_endHour = "EHT";
    final String callbackData_schedule = "SHT";
    final String callbackData_chooseWeekend = "CWT";
    final String callbackData_searchClientId = "SCT"; // Поиск клиента по первому символу в фамилии
    final String callbackData_findClientId = "FCT"; // Поиск клиента по первому символу в фамилии
    final String callbackData_delMessage = "DMS"; // Поиск клиента по первому символу в фамилии
    final String callbackData_approveDelSpec = "ADS"; // Подтверждение удаления специалиста
    final String callbackData_clientsForSettings = "CFS";
    final String callbackData_putAwaitList = "PAL";
    final String callbackData_awaitList = "AAL";
    final String callbackData_delClientRemark = "DCR";
    final String callbackData_appointmentInfo = "AIN";
    final String callbackData_approveDelAppoint = "ADS";
    final String callbackData_delAppoint = "DAP";
    final String callbackData_addNewAdmin = "ANA";
    final String callbackData_adminForDel = "AFD";
    final String callbackData_delMyAdmin = "DMA";
    final String callbackData_delAdmin = "DAD";
    final String callbackData_approveDelAdmin = "ADA";
    final String callbackData_isAdmin = "ISA";
    final String callbackData_approveLeave = "ALE";
    final String callbackData_approveDelMyAdmin = "AAD";
    final String callbackData_approveDelClient = "ARC";
    final String callbackData_deleteMessage = "DLM";
    final String callbackData_showAwaitList = "SAL";
    final String callbackData_chooseTimeZone = "CTZ";
    final String callbackData_sendMessageTime = "TSM";
    final String callbackData_chooseSendTime = "CSM";


    final String callbackData_confirmAppointment = "CAP";
    final String callbackData_refuseAppointment = "RAP";


    final String callData_clientsList = "CLILIST";
    //  private final String[] textsAdministratorBaseMenu = {"Добавить нового администратора", "Удалить администратора", callbackData_backToSpecMenu};

    final String callbackData_delOrRepair = "Удаление/восстановление данных"; // Подтверждение удаления специалиста
    final String callbackData_addNewSpec = "Добавить нового специалиста";
    final String callbackData_specSchedule = "Расписание специалиста";
    final String callbackData_delSpec = "Удалить специалиста";
    final String callbackData_workTime = "Часы работы";
    final String callbackData_timeZone = "Часовой пояс";
    final String callbackData_mySchedule = "Мой график работы";
    final String callbackData_sendTime = "Время рассылки сообщений";
    final String callbackData_subscription = "Подписка";
    final String callbackData_setAppointment = "Записать на прием";
    final String callbackData_addClient = "Добавить нового клиента";
    final String callbackData_lookAppointment = "Посмотреть запись";
    final String callbackData_workWithClient = "Работа с базой клиентов";
    final String callbackData_workWithAdmin = "Администратор";
    final String callbackData_workWithAdministrators = "Администраторы";
    final String callbackData_organizationName = "Название организации";
    final String callbackData_choseSpecialist = "Выбрать специалиста";
    final String callbackData_workWithSpecialist = "Работа с базой специалистов"; // "Меню работы с базой специалистов" "Работа с базой специалистов"
    final String callbackData_backWorkWithSpecialist = "⏎ Работа с базой специалистов";
    final String callbackData_delegateOwnership = "Передать права владения БД";



    static final String backText1 = "⏎  Назад в меню";
    static final String backText2 = "⏎  Назад";
    static final String okText = "𝐎𝐊";

    static final String callbackData_delMyData = "Удалить мои данные";
    static final String callbackData_regAsSpecialist = "#regspec";
    static final String callbackData_regAsAdmin = "#regadmin";
    //   static final String callbackData_regClient = "Добавить нового клиента";
    static final String callbackData_backToUserMenu = "⏎   Назад в меню";
    static final String callbackData_backToAdminMenu = "⏎  Назад в меню";
    static final String callbackData_backToSpecMenu = "⏎ Назад в меню";
    static final String callbackData_userSettings = "userSettings";
    static final String callbackData_adminSettings = "Настройки ⚙";
    static final String callbackData_specSettings = "Настройки  ⚙";
    static final String callbackData_userAppointment = "usrAppointment";


    final int specialistToClientIndex = 0; // диапазон 0-1
    final int adminToClientIndex = 2; // диапазон 2-3
    final int specialistToAdminIndex = 4; // диапазон 4-5
    final int adminToSpecialistIndex = 6; // диапазон 6-7
    final int adminToAdminIndex = 8; // диапазон 8-9


    private final String err900 = "Error 900";


    @Autowired
    private AdministratorRepository adminRepository;
    @Autowired
    private SpecialistRepository specialistRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CacheManager cacheManager;


    public TelegramBotCommands(AppointmentRepository appointmentRepository) {
        super(AppConfig.botToken);

        // Меню команд бота
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

            if (messageText.length() == 5) {
                try {
                    Integer.parseInt(messageText);
                    try {
                        long longAppenderId = Long.parseLong(receiveIdByPassword(messageText));
                        String stringAppenderId = receiveIdByPassword(messageText);
                        int registerIndex = Integer.parseInt(String.valueOf(messageText.charAt(3)));
                        Client client;
                        Specialist specialist;
                        Administrator administrator;
                        SendMessage messageToUser = new SendMessage(stringChatId, "✹ㅤВы авторизованы.");
                        messageToUser.setReplyMarkup(botMethod.receiveOneButtonMenu("\uD835\uDC0E\uD835\uDC0A", stringChatId + callbackData_delMessage + intMessageId));

                        switch (registerIndex) {
                            case specialistToClientIndex, 1 -> { // регистрация клиента специалистом
                                if (savedClientId.get(stringAppenderId) != null) {
                                    client = clientRepository.findById(savedClientId.get(stringAppenderId)).get();
                                    specialist = specialistRepository.findById(longAppenderId).get();
                                    client.setOwnerId(specialist.getOwnerId());
                                    client.setTgId(longChatId);
                                    clientRepository.save(client);
                                    savedClientId.remove(stringAppenderId);
                                    invalidatePasswordData(messageText, stringAppenderId);
                                    executeSendMessage(messageToUser);
                                }
                            }
                            case adminToClientIndex, 3 -> { // регистрация клиента администратором
                                if (savedClientId.get(stringAppenderId) != null) {
                                    client = clientRepository.findById(savedClientId.get(stringAppenderId)).get();
                                    administrator = adminRepository.findById(longAppenderId).get();
                                    client.setOwnerId(administrator.getOwnerId());
                                    client.setTgId(longChatId);
                                    clientRepository.save(client);
                                    savedClientId.remove(stringAppenderId);
                                    invalidatePasswordData(messageText, stringAppenderId);
                                    executeSendMessage(messageToUser);
                                }
                            }
                            case specialistToAdminIndex, 5 -> { // регистрация специалистом администратора
                                if (longChatId != longAppenderId && adminRepository.findById(longChatId).isPresent()) {
                                    addAdministratorToSpecialist(longAppenderId, longChatId);
                                    invalidatePasswordData(messageText, stringAppenderId);
                                    executeSendMessage(messageToUser);
                                }
                            }
                            case adminToSpecialistIndex, 7 -> {// регистрация специалиста администратором
                                if (longChatId != longAppenderId && specialistRepository.findById(longChatId).isPresent()) {
                                    addSpecialistToAdministrators(longAppenderId, longChatId);
                                    invalidatePasswordData(messageText, stringAppenderId);
                                    executeSendMessage(messageToUser);
                                }
                            }
                            case adminToAdminIndex, 9 -> {// регистрация администратора администратором
                                if (longChatId != longAppenderId && adminRepository.findById(longChatId).isPresent()) {
                                    addAdministratorToAdministrators(longAppenderId, longChatId);
                                    invalidatePasswordData(messageText, stringAppenderId);
                                    executeSendMessage(messageToUser);
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        // log
                    }
                } catch (NumberFormatException e) {
                    // log
                }
            }

            /**
             * В процессе взаимодействия с ботом может понадобиться ввод некоторых данных в чат и до момента
             * отправки этих данных, функция добавляет в Map tempData строку-триггер, в таком случае данные
             * введенные пользователем интерпретируются должным образом. Если в Map tempData добавляется
             * строка-константа, сообщение-updateMessageText запускает одну из функций в блоке.
             */
            if (!tempData.isEmpty() && tempData.get(stringChatId) != null && !tempData.get(stringChatId).equals("")) {
                switch (tempData.get(stringChatId)) {
                    case input_spec_surname -> { // Добавление специалиста в бд
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
                    case input_admin_surname -> { // Добавление администратора в бд
                        verifyRegisterData(longChatId, stringChatId, messageText, "Введите имя", inputtedSurname, input_admin_name);
                    }
                    case input_admin_name -> {
                        verifyRegisterData(longChatId, stringChatId, messageText, "Введите отчество", inputtedName, input_admin_patronymic);
                    }
                    case input_admin_patronymic -> {
                        if (verifyRegisterData(longChatId, stringChatId, messageText, "", inputtedPatronymic, "")) {
                            //  tempData.remove(stringChatId);
                            registerAdministrator(longChatId, stringChatId);
                        }
                    }
                    case input_client_surname -> { // Добавление клиента в бд
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
                        //  tempData.remove(stringChatId);
                        if (verifyBirthDayDate(longChatId, stringChatId, messageText, "", inputtedClientBirthdate, "")) {
                            if (savedClientId.get(stringChatId) != null) {
                                updateClient(longChatId, stringChatId, savedClientId.get(stringChatId), savedClientTgId.get(stringChatId));
                            } else {
                                registerClient(longChatId, stringChatId);
                            }
                        }
                    }
                    case input_remark -> {//   note = "#" + "note text" + "/";
                        long clientId = savedClientId.get(stringChatId) == null ? -1 : savedClientId.get(stringChatId);
                        Optional<Client> optionalClient = clientRepository.findById(clientId);
                        if (optionalClient.isPresent() && messageText.length() < 1000) {
                            Client client = optionalClient.get();
                            String note = client.getClientNotes();
                            String noteAddition = messageText.replaceAll("#", "").replaceAll("/", "");
                            client.setClientNotes(note + "#" + noteAddition + "/");
                            clientRepository.save(client);
                        }
                        //  tempData.remove(stringChatId);
                        savedClientId.remove(stringChatId);
                    }
                }
                //   tempData.remove(stringChatId);
            }

            // Удаление отправленных в чат сообщений (чтобы не засорять экран чата)
            executeDeleteMessage(new DeleteMessage(stringChatId, intMessageId));

            if (messageText.equals("3") || messageText.equals("/start")) { // клавиатура
                //     tempData.put(stringChatId, "");
                savedMessageId.put(stringChatId, intMessageId);
                if (adminRepository.existsById(longChatId)) {
                    String textForMessage = createTextForMenu(longChatId, stringChatId);
                    SendMessage sendMessage = new SendMessage(stringChatId, textForMessage);
                    sendMessage.setReplyMarkup(createAdminMenu());
                    executeSendMessage(sendMessage);

                } else if (specialistRepository.existsById(longChatId)) {
                    String textForMenu = createTextForMenu(longChatId, stringChatId);
                    SendMessage sendMessage = new SendMessage(stringChatId, textForMenu);
                    sendMessage.setReplyMarkup(createSpecialistMenu());
                    executeSendMessage(sendMessage);

                } else if (userRepository.existsById(longChatId)) {
                    executeSendMessage(botMethod.createUserMenu(stringChatId, textToUser));

                } else {
                    User user = new User();
                    user.setId(longChatId);
                    user.setTgName(userName);
                    userRepository.save(user);
                    executeSendMessage(botMethod.createUserMenu(stringChatId, textToUser));
                }
            } else if (messageText.equals("/deletedata")) { // Заглушка для удаления сущностей. Позднее функционал будет переделан на адекватные методы с отношениями между сущностями
                if (adminRepository.existsById(longChatId)) {
                    deleteAdministratorData(longChatId);

                } else if (specialistRepository.existsById(longChatId)) {
                    deleteSpecialistData(longChatId);

                } else if (userRepository.existsById(longChatId)) {
                    deleteUserData(longChatId);
                }
                executeSendMessage(new SendMessage(stringChatId, "Все данные учетной записи были удалены.")); // Заглушка для удаления сущностей. Позднее функционал будет переделан на адекватные методы с отношениями между сущностями

            } else if (messageText.equals("1")) {
                Administrator administrator = adminRepository.findById(longChatId).get();
                List<Specialist> specialists = administrator.getSpecialists();
                System.out.println("TEST 1 >>>>>>>>" + specialists.toString()); //TODO

            } else if (messageText.equals("2")) {
                Specialist specialist = specialistRepository.findById(longChatId).get();
                List<Administrator> administrators = specialist.getAdministrators();
                System.out.println("TEST 2 >>>>>>>>" + administrators.toString()); //TODO

            } else if (messageText.equals("5")) {
                long id = registerTestSpecialist();
                addSpecialistToAdministrators(longChatId, id);
                Specialist specialist = specialistRepository.findById(id).get();
                System.out.println("TEST 5 >>>>>>>>" + specialist); //TODO

            } else if (messageText.contains("с")) {
                try {
                    int time = Integer.parseInt((messageText.replace("с", ""))); //TODO
                    Specialist specialist = specialistRepository.findById(longChatId).get();
                    specialist.setSendTime(time);
                    specialistRepository.save(specialist);
                } catch (NumberFormatException e) {
                    System.out.println("nfe");
                }

            } else if (messageText.length() == 3) {
                try {
                    long id = Long.parseLong(messageText);
                    Specialist specialist = specialistRepository.findById(id).get();
                    List<Administrator> administrators = specialist.getAdministrators();
                    System.out.println("TEST 6 >>>>>>>>" + administrators.toString()); //TODO
                } catch (NumberFormatException e) {
                    System.out.println("nfe"); //TODO
                }

            } else if (messageText.equals("7")) {
                List<Specialist> specialists = specialistRepository.findAll();
                System.out.println("TEST 7 >>>>>>>>" + specialists); //TODO

            }


            // Если update содержит изменённое сообщение
        } else if (update.hasCallbackQuery()) {
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long longChatId = update.getCallbackQuery().getMessage().getChatId();
            String stringChatId = String.valueOf(longChatId);
            String callbackData = update.getCallbackQuery().getData();

            if (callbackData.equals(callbackData_backToUserMenu)) {
                executeEditMessageText(botMethod.createUserMenu(longChatId, messageId, textToUser));

            } else if (callbackData.equals(callbackData_userSettings)) {
                executeEditMessageText(botMethod.createUserSettingsMenu(longChatId, messageId, "Настройки для User"));


            } else if (callbackData.equals(callbackData_userAppointment)) {
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "У тебя нет записи"); //
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, callbackData_backToUserMenu));
                executeEditMessageText(editMessageText);


            } else if (callbackData.equals(callbackData_regAsSpecialist)) {
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Введите вашу фамилию и отправьте сообщение в чат");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, callbackData_backToUserMenu));
                executeEditMessageText(editMessageText);
                tempData.put(stringChatId, input_spec_surname); // Регистрация специалиста

            } else if (callbackData.equals(callbackData_regAsAdmin)) {
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Введите вашу фамилию и отправьте сообщение в чат");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, callbackData_backToUserMenu));
                executeEditMessageText(editMessageText);
                tempData.put(stringChatId, input_admin_surname); // Регистрация администратора

            } else if (callbackData.equals(callbackData_backToAdminMenu)) {
                String textForMenu = createTextForMenu(longChatId, stringChatId);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMenu);
                editMessageText.setReplyMarkup(createAdminMenu());
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals(callbackData_backToSpecMenu)) {
                String textForMenu = createTextForMenu(longChatId, stringChatId);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMenu);
                editMessageText.setReplyMarkup(createSpecialistMenu());
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals(callbackData_addClient)) {
                createRegisterClientProcess(longChatId, stringChatId, messageId);

            } else if (callbackData.equals(callbackData_choseSpecialist)) {
                Administrator administrator = adminRepository.findById(longChatId).get();
                List<Specialist> specialistIdList = adminRepository.findById(longChatId).get().getSpecialists();
                if (!administrator.isOwner()) {
                    specialistIdList.addAll(administrator.getSpecialists_owners());
                }
                if (!specialistIdList.isEmpty()) {
                    Map<String, String> specialistsMap = new LinkedHashMap<>();
                    for (int i = 0; i < specialistIdList.size(); i++) {
                        specialistsMap.put((i + 1) + ". " + specialistIdList.get(i).receiveShortName(),
                                specialistIdList.get(i).getId() + callbackData_specId);
                    }
                    specialistsMap.put(backText1, callbackData_backToAdminMenu);
                    executeEditMessageText(botMethod.createUtilMenu(longChatId, messageId, "Выберите Специалиста", specialistsMap));
                } else {
                    EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "У вас нет специалистов");
                    editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText1, callbackData_backToAdminMenu));
                    executeEditMessageText(editMessageText);
                }
            } else if (callbackData.contains(callbackData_specId)) {
                String data = callbackData.replace(callbackData_specId, "");
                Specialist specialist = specialistRepository.findById(Long.parseLong(data)).get();
                String name = specialist.receiveShortName();
                Administrator administrator = adminRepository.findById(longChatId).get();
                if (!administrator.isOwner()) {
                    administrator.setOwnerId(specialist.getOwnerId());
                }
                administrator.setCurrentSpecialistId(Long.parseLong(data));
                adminRepository.save(administrator);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Выбран специалист " + name);
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, callbackData_backToAdminMenu));
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals("Посмотреть запись")) {
                executeEditMessageText(appointmentMenu(longChatId, messageId));

            } else if (callbackData.equals("Записать на прием")) {
                String mainMenuData = backToMenu(longChatId);
                String textForMessage = createTextForClientSearch(longChatId);
                if (textForMessage.equals("Специалист не выбран.")) {
                    EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                    editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText2, mainMenuData));
                    executeEditMessageText(editMessageText);
                } else {
                    executeEditMessageText(botMethod.searchClient(longChatId, messageId, textForMessage, callbackData_clientFirst, callData_clientsList, mainMenuData));
                }

            } else if (callbackData.equals(callData_clientsList)) {
                String textForMessage = createTextForClientSearch(longChatId);
                if (textForMessage.equals("Специалист не выбран.")) {
                    EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                    editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText2, backToMenu(longChatId)));
                    executeEditMessageText(editMessageText);
                } else {
                    showAllClients(longChatId, messageId, textForMessage, callbackData_clientIdAppointment);
                }

            } else if (callbackData.contains(callbackData_clientFirst)) {
                String dataText = callbackData.replace(callbackData_clientFirst, "");
                executeEditMessageText(receiveClientsSetByFirstSymbol(longChatId, messageId, dataText));

                // Выбор даты для записи клиента
            } else if (callbackData.contains(callbackData_clientIdAppointment)) {
                long clientId = savedClientId.get(stringChatId) == null ?
                        Long.parseLong(callbackData.replace(callbackData_clientIdAppointment, "")) : savedClientId.get(stringChatId);
                savedClientId.put(stringChatId, clientId);
                Client client = clientRepository.findById(clientId).get();
                String mainMenuData = backToMenu(longChatId);
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
                        button.setCallbackData(callbackData_chooseDate + formatYear.format(date.plus(count, DAYS)));
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
            } else if (callbackData.contains(callbackData_chooseDate)) {
                String date = callbackData.replace(callbackData_chooseDate, "");
                String mainMenuData;
                String textForMessage;
                DateTimeFormatter formatYear = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                DateTimeFormatter formatHour = DateTimeFormatter.ofPattern("k");
                boolean isToday = formatYear.format(LocalDate.now()).equals(date);

                if (adminRepository.existsById(longChatId)) {
                    mainMenuData = callbackData_backToAdminMenu;
                    Administrator administrator = adminRepository.findById(longChatId).get();
                    textForMessage = createAppointmentText(administrator.getCurrentSpecialistId(), date);
                } else {
                    mainMenuData = callbackData_backToSpecMenu;
                    textForMessage = createAppointmentText(longChatId, date);
                }
                textForMessage += "\nВыберите время для записи:";
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInlineButtonBack = new ArrayList<>();
                int beginHour = isToday ? Integer.parseInt(formatHour.format(LocalTime.now())) + 1 : 8;
                int endHour = 22;

                for (int i = beginHour; i <= endHour - 1; i++) {
                    List<InlineKeyboardButton> rowInlineButton = new ArrayList<>();
                    for (int x = 0; x < 60; x += 10) {
                        String hour = i > 9 ? String.valueOf(i) : "0" + i;
                        String minute = x > 0 ? String.valueOf(x) : "0" + x;
                        InlineKeyboardButton button = new InlineKeyboardButton(i + ":" + minute);
                        button.setCallbackData(callbackData_chooseBegin + date + " - " + hour + ":" + minute);
                        rowInlineButton.add(button);
                    }
                    rowsInline.add(rowInlineButton);
                }
                InlineKeyboardButton menuButton = new InlineKeyboardButton(mainMenuData);
                menuButton.setCallbackData(mainMenuData);
                rowInlineButtonBack.add(menuButton);
                InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
                backButton.setCallbackData(callbackData_clientIdAppointment);
                rowInlineButtonBack.add(backButton);
                rowsInline.add(rowInlineButtonBack);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_chooseBegin)) {
                String time = callbackData.replace(callbackData_chooseBegin, ""); // 26.10.2025 - 09:00
                String mainMenuData = adminRepository.existsById(longChatId) ? callbackData_backToAdminMenu : callbackData_backToSpecMenu;
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
                        button.setCallbackData(callbackData_chooseEnd + time + "/" + hour + ":" + minute);
                        rowInlineButton.add(button);
                    }
                    beginMinute = 0;
                    rowsInline.add(rowInlineButton);
                }
                InlineKeyboardButton menuButton = new InlineKeyboardButton(mainMenuData);
                menuButton.setCallbackData(mainMenuData);
                rowInlineButtonBack.add(menuButton);
                InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
                backButton.setCallbackData(callbackData_chooseDate + (time.split(" - ")[0]));
                rowInlineButtonBack.add(backButton);
                rowsInline.add(rowInlineButtonBack);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_chooseEnd)) {
                Client client = clientRepository.findById(savedClientId.get(stringChatId)).get();
                String timeData = callbackData.replace(callbackData_chooseEnd, ""); // 26.10.2025 - 09:00/09:10
                Appointment matchAppointment = null;
                List<Appointment> appointments;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm");
                String[] splitTimeData = timeData.split(" - ");
                String date = splitTimeData[0]; //  26.10.2025
                String textForMessage;
                String specialistName;
                int sendTime;
                int timeZone;
                long ownerId;
                long specialistId;

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // коллекция коллекций с горизонтальным рядом кнопок, создаёт вертикальный ряд кнопок
                List<InlineKeyboardButton> firstRowInlineButton = new ArrayList<>();

                LocalDateTime beginTime = LocalDateTime.parse(date + " - " + splitTimeData[1].split("/")[0], formatter); //09:00
                LocalDateTime endTime = LocalDateTime.parse(date + " - " + splitTimeData[1].split("/")[1], formatter); //09:10

                if (adminRepository.existsById(longChatId)) {
                    Administrator administrator = adminRepository.findById(longChatId).get();
                    ownerId = administrator.getOwnerId();
                    specialistId = administrator.getCurrentSpecialistId();
                    appointments = appointmentRepository.findBySpecialistId(administrator.getCurrentSpecialistId());
                    sendTime = administrator.getSendTime() + administrator.getTimeZone();
                    timeZone = administrator.getTimeZone();
                    specialistName = specialistRepository.findById(specialistId).get().receiveFullName();
                } else {
                    Specialist specialist = specialistRepository.findById(longChatId).get();
                    appointments = appointmentRepository.findBySpecialistId(specialist.getId());
                    ownerId = specialist.getOwnerId();
                    specialistId = specialist.getId();
                    sendTime = specialist.getSendTime() + specialist.getTimeZone();
                    timeZone = specialist.getTimeZone();
                    specialistName = specialist.receiveFullName();
                }
                appointments.addAll(appointmentRepository.findByClientId(client.getId()));

                for (Appointment ap : appointments) {
                    String[] splitAppointmentTimeData = ap.getAppointmentDateTime().split(" - ");
                    String appointmentDate = splitAppointmentTimeData[0]; //  26.10.2025
                    LocalDateTime appointmentBeginTime = LocalDateTime.parse(appointmentDate + " - " + splitAppointmentTimeData[1].split("/")[0], formatter); //09:00
                    LocalDateTime appointmentEndTime = LocalDateTime.parse(appointmentDate + " - " + splitAppointmentTimeData[1].split("/")[1], formatter); //09:10

                    if (beginTime.isAfter(appointmentBeginTime) && beginTime.isBefore(appointmentEndTime) ||
                            endTime.isAfter(appointmentBeginTime) && endTime.isBefore(appointmentEndTime) ||
                            beginTime.isBefore(appointmentBeginTime) && endTime.isAfter(appointmentEndTime)) {
                        matchAppointment = ap;
                        break;
                    }
                }

                if (matchAppointment != null) {
                    Specialist specialistFromAppointment = specialistRepository.findById(matchAppointment.getSpecialistId()).get(); // TODO No value present
                    textForMessage = "Выбраны дата и время: " + timeData.replace("-", "с").replace("/", " до ") + "\n❗ Клиент уже имеет запись у специалиста: " +
                            specialistFromAppointment.receiveFullName() + " на дату " + matchAppointment.getAppointmentDateTime().replace("-", "в").replace("/", " до ");
                    InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
                    backButton.setCallbackData(callbackData_chooseBegin + date + " - " + splitTimeData[1].split("/")[0]);
                    firstRowInlineButton.add(backButton);
                } else {
                    textForMessage = "Клиент записан на: " + timeData.replace("-", "с").replace("/", " до ");
                    String mainMenuData = backToMenu(longChatId);
                    Appointment appointment = new Appointment();
                    appointment.setOwnerId(ownerId);
                    appointment.setSpecialistId(specialistId);
                    appointment.setClientId(savedClientId.get(stringChatId)); // TODO Удалить данные перед началом процесса записи
                    appointment.setClientTgId(client.getTgId());
                    appointment.setConfirmAppointment(ConfirmAppointmentStatus.UNDEFINED.getStatusLabel());
                    appointment.setSendTime(sendTime);
                    appointment.setSpecialistName(specialistName);
                    appointment.setAppointmentNote("");
                    appointment.setTimeZone(timeZone);
                    appointment.setWaitNearAppointment(false);
                    appointment.setAppointmentDateTime(timeData); // 26.10.2025 - 09:00/09:10
                    InlineKeyboardButton menuButton = new InlineKeyboardButton(mainMenuData);
                    menuButton.setCallbackData(mainMenuData);
                    firstRowInlineButton.add(menuButton);
                    appointmentRepository.save(appointment);
                }
                rowsInline.add(firstRowInlineButton);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals(callbackData_specSettings)) {
                boolean isOwner = specialistRepository.findById(longChatId).get().isOwner();
                Map<String, String> buttonMap = new LinkedHashMap<>();
                buttonMap.put("Часы работы", "Часы работы");
                buttonMap.put("График работы", callbackData_specSchedule);
                if (isOwner) {
                    buttonMap.put("Часовой пояс", "Часовой пояс");
                    buttonMap.put("Время рассылки сообщений", callbackData_sendMessageTime);
                    buttonMap.put(callbackData_delOrRepair, callbackData_delOrRepair);
                    buttonMap.put("Передать права владельца", "Передать права");
                    buttonMap.put("Пароль учетной записи", "Пароль записи");
                    buttonMap.put("Подписка", "Подписка");
                }
                buttonMap.put("Изменить мои данные", "Изменить мои данные");
                buttonMap.put("Удалить мой профиль", "Удалить мой профиль");
                buttonMap.put(backText1, callbackData_backToSpecMenu);
                EditMessageText editMessageText = botMethod.createUtilMenu(longChatId, messageId, "Настройки специалиста", buttonMap);
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals(callbackData_adminSettings)) {
                boolean isOwner = adminRepository.findById(longChatId).get().isOwner();
                Map<String, String> cliSettingsMenu = new LinkedHashMap<>();

                if (isOwner) {
                    cliSettingsMenu.put("Часы работы", "Часы работы" + callbackData_isAdmin);
                    cliSettingsMenu.put("Часовой пояс", "Часовой пояс");
                    cliSettingsMenu.put("Название организации", "Название организации" + callbackData_isAdmin);
                    cliSettingsMenu.put("Время рассылки сообщений", callbackData_sendMessageTime);
                    cliSettingsMenu.put(callbackData_delOrRepair, callbackData_delOrRepair + callbackData_isAdmin);
                    cliSettingsMenu.put("Передать права владельца", "Передать права" + callbackData_isAdmin);
                    cliSettingsMenu.put("Пароль учетной записи", "Пароль записи" + callbackData_isAdmin);
                    cliSettingsMenu.put("Подписка", "Подписка" + callbackData_isAdmin);
                }
                cliSettingsMenu.put("Изменить мои данные", "Изменить мои данные" + callbackData_isAdmin);
                cliSettingsMenu.put("Удалить мой профиль", "Удалить мой профиль" + callbackData_isAdmin);
                cliSettingsMenu.put(backText1, callbackData_backToAdminMenu);
                EditMessageText editMessageText = botMethod.createUtilMenu(longChatId, messageId, "Настройки администратора", cliSettingsMenu);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains("Часы работы")) {
                String workTime;
                String backData;

                if (callbackData.contains(callbackData_isAdmin)) {
                    Administrator administrator = adminRepository.findById(longChatId).get();
                    backData = callbackData_adminSettings;
                    workTime = administrator.getWorkTimeLength().replace("/", ":00 до ");
                } else {
                    Specialist specialist = specialistRepository.findById(longChatId).get();
                    backData = callbackData_specSettings;
                    workTime = specialist.getWorkTimeLength().replace("/", ":00 до ");
                }

                String textForMessage = "Установленные часы работы: с " + workTime.replace("#", ":00 - ") + ":00 ч.\nВыберите новое время начала рабочего дня.";
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = beginWorkTime(callbackData_beginWork);
                List<InlineKeyboardButton> rowInlineButtonBack = new ArrayList<>();
                InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
                backButton.setCallbackData(backData);
                rowInlineButtonBack.add(backButton);
                rowsInline.add(rowInlineButtonBack);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_beginWork)) {
                int time = Integer.parseInt(callbackData.replace(callbackData_beginWork, ""));
                String textForMessage = "Выберите время окончания рабочего дня";
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = endWorkTime(time, callbackData_endWork);
                List<InlineKeyboardButton> rowInlineButtonBack = new ArrayList<>();
                InlineKeyboardButton backButton = new InlineKeyboardButton("Назад");
                backButton.setCallbackData("Часы работы");
                rowInlineButtonBack.add(backButton);
                rowsInline.add(rowInlineButtonBack);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_endWork)) {
                String date = callbackData.replace(callbackData_endWork, "");
                String backData;

                if (adminRepository.existsById(longChatId)) {
                    backData = callbackData_adminSettings;
                    Administrator administrator = adminRepository.findById(longChatId).get();
                    administrator.setWorkTimeLength(date);
                    adminRepository.save(administrator);
                    for (Specialist spc : specialistRepository.findByOwnerId(longChatId)) {
                        spc.setWorkTimeLength(date);
                        specialistRepository.save(spc);
                    }
                } else {
                    backData = callbackData_specSettings;
                    Specialist specialist = specialistRepository.findById(longChatId).get();
                    specialist.setWorkTimeLength(date);
                    specialistRepository.save(specialist);
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Время работы установлено.");
                InlineKeyboardMarkup inlineKeyboardMarkup = botMethod.receiveOneButtonMenu("Назад", backData);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_specSchedule)) {
                EditMessageText editMessageText;
                Map<String, String> buttonMap = new LinkedHashMap<>();
                String textForMessage;
                long specialistId = adminRepository.existsById(longChatId) ?
                        adminRepository.findById(longChatId).get().getCurrentSpecialistId() : longChatId;
                if (specialistId != -1) {
                    Specialist specialist = specialistRepository.findById(specialistId).get();
                    textForMessage = "Специалист: " + specialist.receiveFullName() + "\nАктуальный график:\n" + EVEN_ODD_DAYS.receiveScheduleString(specialist.getReceptionSchedule()) +
                            "\n";
                    buttonMap.put("Четный/нечетный график", "Четный/нечетный график");
                    buttonMap.put("Фиксированный график", "Фиксированный график");
                    buttonMap.put("Скользящий график", "Скользящий график");
                    buttonMap.put("Сменный график", "Сменный график");
                } else {
                    textForMessage = "Специалист не выбран.";
                }
                buttonMap.put(backText2, backToSettings(longChatId));
                editMessageText = botMethod.createUtilMenu(longChatId, messageId, textForMessage, buttonMap);
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals("Фиксированный график")) {
                savedWorkSchedule.remove(stringChatId);
                long specialistId = adminRepository.existsById(longChatId) ?
                        adminRepository.findById(longChatId).get().getCurrentSpecialistId() : longChatId;
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                Specialist specialist = specialistRepository.findById(specialistId).get();
                String text = "Специалист: " + specialist.receiveShortName() + "\nАктуальный график:\n" + FIX_DAYS.receiveScheduleString(specialist.getReceptionSchedule()) + "\nУстановите часы работы  для каждого дня недели. Для установки выходных дней нажмите клавишу \"Выходной день\".\nВыберите время начала рабочего дня для понедельника.";
                List<List<InlineKeyboardButton>> rowsInline = createBeginScheduleButtonsSet();
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals("Сменный график")) { //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                savedWorkSchedule.remove(stringChatId);
                long specialistId = adminRepository.findById(longChatId).get().getCurrentSpecialistId();
                Specialist specialist = specialistRepository.findById(specialistId).get();
                String text = "Специалист: " + specialist.receiveShortName() + "\nАктуальный график:\n" + FIX_DAYS.receiveScheduleString(specialist.getReceptionSchedule()) + "\nСменный график будет реализован в будущем";
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText2, callbackData_workWithSpecialist));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_beginWeekWork)) {
                String time = callbackData.replace(callbackData_beginWeekWork, "");
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
                    //Administrator administrator = adminRepository.findById(longChatId).get();
                    long specialistId = adminRepository.existsById(longChatId) ?
                            adminRepository.findById(longChatId).get().getCurrentSpecialistId() : longChatId;
                    Specialist specialist = specialistRepository.findById(specialistId).get();
                    specialist.setReceptionSchedule(savedScheduleTime);
                    specialistRepository.save(specialist);
                    text = FIX_DAYS.receiveScheduleString(savedScheduleTime) + "\nРасписание для специалиста " + specialist.receiveShortName() + " установлено.";
                    inlineKeyboardMarkup = botMethod.receiveOneButtonMenu("Назад", "Расписание специалиста");
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);


            } else if (callbackData.contains(callbackData_endWeekWork)) {
                String time = callbackData.replace(callbackData_endWeekWork, "");
                String[] scheduleTime = savedWorkSchedule.get(stringChatId) == null ? new String[0] : savedWorkSchedule.get(stringChatId).split("/");
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                String text = "";

                if (scheduleTime.length == 0) {
                    text = "Выберите время окончания рабочего дня для понедельника.";
                    List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), callbackData_beginWeekWork);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);

                } else if (scheduleTime.length == 1) {
                    text = "Выберите время окончания рабочего дня для вторника.";
                    List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), callbackData_beginWeekWork);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);

                } else if (scheduleTime.length == 2) {
                    text = "Выберите время окончания рабочего дня для среды.";
                    List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), callbackData_beginWeekWork);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);

                } else if (scheduleTime.length == 3) {
                    text = "Выберите время окончания рабочего дня для четверга.";
                    List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), callbackData_beginWeekWork);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);

                } else if (scheduleTime.length == 4) {
                    text = "Выберите время окончания рабочего дня для пятницы.";
                    List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), callbackData_beginWeekWork);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);

                } else if (scheduleTime.length == 5) {
                    text = "Выберите время окончания рабочего дня для субботы.";
                    List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), callbackData_beginWeekWork);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);

                } else if (scheduleTime.length == 6) {
                    text = "Выберите время окончания рабочего дня для воскресенья.";
                    List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), callbackData_beginWeekWork);
                    inlineKeyboardMarkup.setKeyboard(rowsInline);
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals("Четный/нечетный график")) {
                savedWorkSchedule.remove(stringChatId);
                long specialistId = adminRepository.existsById(longChatId) ?
                        adminRepository.findById(longChatId).get().getCurrentSpecialistId() : longChatId;
                Specialist specialist = specialistRepository.findById(specialistId).get();
                String text = "Специалист: " + specialist.receiveFullName() + "\nАктуальный график:\n" + EVEN_ODD_DAYS.receiveScheduleString(specialist.getReceptionSchedule()) +
                        "\nВ этом меню вы можете установить рабочее время для четных и нечетных дней месяца.\nУстановите время начала рабочего дня специалиста для нечетного дня месяца.";
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = createBeginDayButtonsSet(callbackData_endDayWork);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);
            } else if (callbackData.contains(callbackData_beginDayWork)) {
                String time = callbackData.replace(callbackData_beginDayWork, "");
                String text = "";
                String savedScheduleTime = "";
                String[] scheduleTime = savedWorkSchedule.get(stringChatId) == null ? new String[0] : savedWorkSchedule.get(stringChatId).split("/");
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                if (scheduleTime.length == 0) {
                    savedWorkSchedule.put(stringChatId, time + "/");
                    text = "График работы:\n" + EVEN_ODD_DAYS.receiveScheduleString(savedWorkSchedule.get(stringChatId) + EVEN_ODD_DAYS.getLabel()) + "\nУстановите время начала рабочего дня специалиста для четного дня месяца.";
                    // inlineKeyboardMarkup.setKeyboard(beginWorkTime(callbackData_endDayWork));
                    inlineKeyboardMarkup.setKeyboard(createBeginDayButtonsSet(callbackData_endDayWork));
                } else if (scheduleTime.length == 1) {
                    savedScheduleTime = savedWorkSchedule.get(stringChatId);
                    savedWorkSchedule.put(stringChatId, savedScheduleTime + time + "/");
                    text = "График работы:\n" + EVEN_ODD_DAYS.receiveScheduleString(savedWorkSchedule.get(stringChatId) + EVEN_ODD_DAYS.getLabel()) + "\nВыберите выходной день или нажмите клавишу \"Готово\".";
                    inlineKeyboardMarkup = createWeekendButtonsSet("");
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_endDayWork)) {
                String time = callbackData.replace(callbackData_endDayWork, "");
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

            } else if (callbackData.contains(callbackData_weekDay)) {
                String scheduleData = callbackData.replace(callbackData_weekDay, "");
                String savedScheduleTime = savedWorkSchedule.get(stringChatId) + scheduleData;
                savedWorkSchedule.put(stringChatId, savedScheduleTime + "/");
                String[] data = savedScheduleTime.split("/");
                String choseDays = savedScheduleTime.replace(data[0], "").replace(data[1], "");
                String text = "Выберите выходной день или нажмите клавишу \"Готово\".";
                InlineKeyboardMarkup inlineKeyboardMarkup = createWeekendButtonsSet(choseDays);

                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_chooseWeekDay)) {
                String savedScheduleTime = savedWorkSchedule.get(stringChatId).
                        substring(0, savedWorkSchedule.get(stringChatId).length() - 1) + EVEN_ODD_DAYS.getLabel();
                // savedWorkSchedule.put(stringChatId, savedScheduleTime);
                long specialistId = adminRepository.existsById(longChatId) ?
                        adminRepository.findById(longChatId).get().getCurrentSpecialistId() : longChatId;
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
                long specialistId = adminRepository.existsById(longChatId) ?
                        adminRepository.findById(longChatId).get().getCurrentSpecialistId() : longChatId;
                Specialist specialist = specialistRepository.findById(specialistId).get();
                String text = "Специалист: " + specialist.receiveShortName() + "\nАктуальный график:\n" + ROLLING_CHART.receiveScheduleString(specialist.getReceptionSchedule()) +
                        "\nВ этом меню вы можете настроить скользящий рабочий график.\nУстановите время начала рабочего дня.";
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = createBeginDayButtonsSet(callbackData_beginHour);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_beginHour)) {
                String time = callbackData.replace(callbackData_beginHour, "");
                String text = "Выберите время окончания рабочего дня.";
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = createEndScheduleButtonsSet(Integer.parseInt(time), callbackData_endHour);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_endHour)) { //  8#16
                String time = callbackData.replace(callbackData_endHour, "") + "/";
                savedWorkSchedule.put(stringChatId, time);
                String text = "График:\n" + ROLLING_CHART.receiveScheduleString(time + ROLLING_CHART.getLabel()) + "Установите интервал (график) рабочих и выходных дней.";
                InlineKeyboardMarkup inlineKeyboardMarkup = createScheduleButtonsSet(callbackData_schedule); //scheduleTag
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_schedule)) {
                String schedule = callbackData.replace(callbackData_schedule, "");
                String savedScheduleTime = savedWorkSchedule.get(stringChatId) + schedule + "/";
                savedWorkSchedule.put(stringChatId, savedScheduleTime);
                String text = "*В этом меню предстоит выбрать дату начала выходных дней специалиста. Если в данный момент наступили выходные дни, значит надо выбрать дату начала следующих выходных.\nГрафик:\n" +
                        ROLLING_CHART.receiveScheduleString(savedScheduleTime + ROLLING_CHART.getLabel()) + "\nВыберите дату начала выходных дней*.";
                InlineKeyboardMarkup inlineKeyboardMarkup = createDateButtonsSet(callbackData_chooseWeekend);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, text);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_chooseWeekend)) {
                String schedule = callbackData.replace(callbackData_chooseWeekend, "");
                String savedScheduleTime = savedWorkSchedule.get(stringChatId) + schedule + ROLLING_CHART.getLabel();
                long specialistId = adminRepository.existsById(longChatId) ?
                        adminRepository.findById(longChatId).get().getCurrentSpecialistId() : longChatId;
                Specialist specialist = specialistRepository.findById(specialistId).get();
                specialist.setReceptionSchedule(savedScheduleTime);
                specialistRepository.save(specialist);
                String textForMessage = "Обновлено расписание специалиста: " + specialist.receiveShortName() + "\n" + ROLLING_CHART.receiveScheduleString(savedScheduleTime);
                InlineKeyboardMarkup inlineKeyboardMarkup = botMethod.receiveOneButtonMenu("⏎ Назад", "Расписание специалиста");
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);
            } else if (callbackData.equals(callbackData_workWithClient)) {
                String textForMessage = createTextForClientSearch(longChatId);
                executeEditMessageText(botMethod.searchClient(longChatId, messageId, textForMessage, callbackData_searchClientId, callbackData_clientsForSettings, backToMenu(longChatId)));

            } else if (callbackData.contains(callbackData_searchClientId)) {
                String firstSurnameSymbol = callbackData.replace(callbackData_searchClientId, "");
                executeEditMessageText(receiveClientsSetByFirstSymbol(longChatId, messageId, firstSurnameSymbol, callbackData_findClientId, "Выберите клиента из списка:"));

            } else if (callbackData.contains(callbackData_findClientId)) {
                String clientId = callbackData.replace(callbackData_findClientId, ""); // callbackData_findClientId + cli.getId()
                Client client = clientRepository.findById(Long.parseLong(clientId)).get();
                boolean awaitAppointment = appointmentRepository.findByClientId(Long.parseLong(clientId)).stream().anyMatch(Appointment::isWaitNearAppointment);
                tempData.remove(stringChatId);
                String awaitText = awaitAppointment ? "да" : "нет";
                String textForMessage = "Меню для работы с данными клиента: " + client.receiveClientInfo() + "\nВ листе ожидания:  " + awaitText;

                Map<String, String> buttonMap = new LinkedHashMap<>();
                buttonMap.put("Авторизовать клиента", "Авторизовать клиента" + clientId);
                buttonMap.put("Внести в лист ожидания", callbackData_putAwaitList + clientId);
                buttonMap.put("История посещений", "История посещений" + clientId);
                buttonMap.put("Добавить заметку", "Добавить заметку" + clientId);
                buttonMap.put("Изменить данные", "Изменить данные" + clientId);
                buttonMap.put("Удалить клиента", "Удалить клиента" + clientId);
                buttonMap.put(callbackData_backToAdminMenu, backToMenu(longChatId));
                EditMessageText editMessageText = botMethod.createUtilMenu(longChatId, messageId, textForMessage, buttonMap);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_workWithSpecialist)) {
                Specialist specialist = null;
                String textForMessage = "Меню для работы с базой специалистов. Выбранный специалист: ";
                Administrator administrator = adminRepository.findById(longChatId).get();
                long specialistId = administrator.getCurrentSpecialistId();

                if (specialistId != -1) {
                    specialist = specialistRepository.findById(specialistId).get();
                    textForMessage += specialist.receiveFullName();
                } else {
                    textForMessage += " специалист не выбран.";
                }
                Map<String, String> cliSettingsMenu = new LinkedHashMap<>();
                if (administrator.isOwner()) {
                    cliSettingsMenu.put(callbackData_addNewSpec, callbackData_addNewSpec);
                    cliSettingsMenu.put(callbackData_specSchedule, callbackData_specSchedule);
                    cliSettingsMenu.put(callbackData_delSpec, callbackData_delSpec);
                } else {
                    cliSettingsMenu.put(callbackData_specSchedule, callbackData_specSchedule);
                    if (specialist != null && specialist.isOwner()) {
                        cliSettingsMenu.put("Отписаться от специалиста", "Отписаться от специалиста");
                    }
                }
                cliSettingsMenu.put(callbackData_backToAdminMenu, callbackData_backToAdminMenu);
                EditMessageText editMessageText = botMethod.createUtilMenu(longChatId, messageId, textForMessage, cliSettingsMenu);
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_addNewSpec)) {
                createRegistrationMenu(longChatId, messageId, stringChatId, adminToSpecialistIndex, callbackData_workWithSpecialist, "Сгенерирован пароль для регистрации специалиста:  ");

            } else if (callbackData.contains(callbackData_delMessage)) {
                String[] data = callbackData.split(callbackData_delMessage);
                String chatId = data[0];
                int messageIdForDel = Integer.parseInt(data[1]) + 1;
                executeDeleteMessage(new DeleteMessage(chatId, messageIdForDel));

            } else if (callbackData.equals(callbackData_delSpec)) {
                Administrator administrator = adminRepository.findById(longChatId).get();
                long specialistId = administrator.getCurrentSpecialistId();
                InlineKeyboardMarkup inlineKeyboardMarkup;
                String textForMessage;

                if (specialistId == -1) {
                    textForMessage = "Специалист не выбран.";
                    inlineKeyboardMarkup = botMethod.receiveOneButtonMenu(backText1, "Работа с базой специалистов");
                } else {
                    Specialist specialist = specialistRepository.findById(administrator.getCurrentSpecialistId()).get();
                    textForMessage = "❗ Внимание: вместе со специалистом будут также удалены все записи на прием к данному специалисту.\nПодтвердите удаление специалиста: " + specialist.receiveFullName();
                    inlineKeyboardMarkup = botMethod.receiveTwoButtonsMenu(backText1, "Работа с базой специалистов", "Удалить", callbackData_approveDelSpec);
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(inlineKeyboardMarkup);
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals(callbackData_approveDelSpec)) {
                deleteSpecialistFromAdministrators(longChatId,
                        adminRepository.findById(longChatId).get().getCurrentSpecialistId());
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Специалист удален.");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText1, "Работа с базой специалистов"));
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals("Отписаться от специалиста")) {
                EditMessageText editMessageText;
                if (adminRepository.findById(longChatId).get().getCurrentSpecialistId() != -1) {
                    editMessageText = botMethod.createEditMessageText(longChatId, messageId, "❗ Подтвердите отписку.");
                    editMessageText.setReplyMarkup(botMethod.receiveTwoButtonsMenu(backText2, callbackData_workWithSpecialist, "Отписаться", callbackData_approveLeave));
                } else {
                    editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Специалист не выбран.");
                    editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText1, "Работа с базой специалистов"));
                }
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals(callbackData_delMyAdmin)) {
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "❗ Подтвердите удаление.");
                editMessageText.setReplyMarkup(botMethod.receiveTwoButtonsMenu(backText2, callbackData_workWithAdmin, "Удалить", callbackData_approveDelMyAdmin));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_approveLeave)) {
                Administrator administrator = adminRepository.findById(longChatId).get();
                deleteAdminFromSpecialist(administrator.getCurrentSpecialistId(), longChatId);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Вы отписались от специалиста.");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText1, callbackData_backToAdminMenu));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_approveDelMyAdmin)) {
                Specialist specialist = specialistRepository.findById(longChatId).get();
                deleteAdminFromSpecialist(longChatId, specialist.getOwn_administrator().getId());
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Администратор удален.");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText1, callbackData_backToSpecMenu));
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals(callbackData_delMyData)) {
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Для удаления своей учетной записи отправьте сообщение с текстом /deletedata\n❗ Внимание: данные будут удалены безвозвратно!");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText1, backToSettings(longChatId)));
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals(callbackData_clientsForSettings)) {
                showAllClients(longChatId, messageId, "Выберите клиента из списка:", callbackData_findClientId);

            } else if (callbackData.contains("Авторизовать клиента")) {
                String clientId = callbackData.replace("Авторизовать клиента", ""); // "Авторизовать клиента" + cli.getId()
                savedClientId.put(stringChatId, Long.parseLong(clientId));
                int registrationIndex = adminRepository.findById(longChatId).isPresent() ? adminToClientIndex : specialistToClientIndex;
                createRegistrationMenu(longChatId, messageId, stringChatId, registrationIndex, callbackData_findClientId + clientId, "Сгенерирован пароль для регистрации клиента:  ");

            } else if (callbackData.contains(callbackData_putAwaitList)) {
                String clientId = callbackData.replace(callbackData_putAwaitList, ""); // callbackData_putAwaitList + cli.getId()
                String textForMessage = "Выберите запись которую хотите убрать/добавить в лист ожидания.";
                List<Appointment> appointments = appointmentRepository.findByClientId(Long.parseLong(clientId)).stream().sorted().toList();
                Map<String, String> buttonsMap = new LinkedHashMap<>();
                for (Appointment apt : appointments) {
                    String buttonText = apt.isWaitNearAppointment() ? "  убрать" : "  добавить";
                    buttonsMap.put(apt.getDateTime() + buttonText, apt.getId() + callbackData_awaitList + clientId);
                }
                buttonsMap.put(backText2, callbackData_findClientId + clientId);
                executeEditMessageText(botMethod.createUtilMenu(longChatId, messageId, textForMessage, buttonsMap));

            } else if (callbackData.contains(callbackData_awaitList)) {
                String[] data = callbackData.split(callbackData_awaitList); //  Appointment.getId() + callbackData_awaitList + clientId
                long appointmentId = Long.parseLong(data[0]);
                String clientId = data[1];
                Appointment appointment = appointmentRepository.findById(appointmentId).get();
                String textForMessage = appointment.isWaitNearAppointment() ? "Запись удалена из листа ожидания." :
                        "Запись добавлена в лист ожидания.";
                appointment.setWaitNearAppointment(!appointment.isWaitNearAppointment());
                appointmentRepository.save(appointment);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText1, callbackData_findClientId + clientId));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains("Добавить заметку")) {
                String clientId = callbackData.replace("Добавить заметку", ""); // callbackData_putAwaitList + cli.getId()
                Client client = clientRepository.findById(Long.parseLong(clientId)).get();
                StringBuilder stringBuilder = new StringBuilder();

                if (client.getClientNotes().length() > 3000) {//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                    stringBuilder.append("❗ Превышен лимит размера текста, удалите часть заметок.\n");
                } else {
                    tempData.put(stringChatId, input_remark);
                    savedClientId.put(stringChatId, Long.parseLong(clientId));
                }

                String[] notes = client.getClientNotes().split("/");
                boolean isNoteExist = !client.getClientNotes().isEmpty();

                stringBuilder.append("Заметки:\n");
                Map<String, String> remarkMenu = new LinkedHashMap<>();

                if (notes.length > 0) {
                    for (int i = 0; i < notes.length; i++) { //  String note = "#" + "note text" + "/";
                        stringBuilder.append(notes[i].replace("#", "• " + (i + 1) + ".  ")).append("\n");
                        if (isNoteExist) {
                            remarkMenu.put("Удалить заметку " + (i + 1), i + callbackData_delClientRemark + clientId);
                        }
                    }
                }
                remarkMenu.put(backText1, callbackData_findClientId + clientId);
                stringBuilder.append("\n\nДля удаления нажмите клавишу с номером заметки, которую необходимо удалить. Для добавления новой заметки введите текст в поле ввода и отправьте сообщение (новая заметка будет отображена после повторного входа в меню 'Добавить заметку').");
                String textForMessage = stringBuilder.toString();
                executeEditMessageText(botMethod.createUtilMenu(longChatId, messageId, textForMessage, remarkMenu));

            } else if (callbackData.contains(callbackData_delClientRemark)) {//   note = "#" + "note text" + "/";
                String[] data = callbackData.split(callbackData_delClientRemark); //  i + callbackData_delClientRemark + clientId
                int noteNumber = Integer.parseInt(data[0]);
                long clientId = Long.parseLong(data[1]);
                Client client = clientRepository.findById(clientId).get();
                String noteForDelete = client.getClientNotes().split("/")[noteNumber] + "/";
                String reNote = client.getClientNotes().replace(noteForDelete, "");
                client.setClientNotes(reNote);
                clientRepository.save(client);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Заметка была удалена.");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText2, "Добавить заметку" + data[1]));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains("Изменить данные")) {
                String clientId = callbackData.replace("Изменить данные", ""); // callbackData_putAwaitList + cli.getId()
                Client client = clientRepository.findById(Long.parseLong(clientId)).get();
                savedClientId.put(stringChatId, Long.parseLong(clientId));
                savedClientTgId.put(stringChatId, client.getTgId());
                savedMessageId.put(stringChatId, (int) messageId);
                tempData.put(stringChatId, input_client_surname);
                String textForMessage = "Для редактирования данных клиента необходимо ввести и отправить обновленные ФИО, телефон, дату рождения. Введите фамилию и отправьте сообщение в чат";
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, backToMenu(longChatId)));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_appointmentInfo)) {
                long appointmentId = Long.parseLong(callbackData.replace(callbackData_appointmentInfo, ""));
                executeEditMessageText(showAppointment(longChatId, messageId, appointmentId));

            } else if (callbackData.contains(callbackData_approveDelAppoint)) {
                long appointmentId = Long.parseLong(callbackData.replace(callbackData_approveDelAppoint, ""));
                executeEditMessageText(approveDeleteAppointment(longChatId, messageId, appointmentId));

            } else if (callbackData.contains(callbackData_delAppoint)) {
                long appointmentId = Long.parseLong(callbackData.replace(callbackData_delAppoint, ""));
                appointmentRepository.deleteById(appointmentId);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Запись удалена.");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText1, backToMenu(longChatId)));
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals(callbackData_workWithAdministrators)) {
                Administrator administrator = adminRepository.findById(longChatId).get();
                String adminListText = adminRepository.findByOwnerId(administrator.getOwnerId()).toString().replace("[", "").replace("]", "").replaceAll(", ", "");
                String textForMessage = "Администраторы:\n" + adminListText;

                Map<String, String> buttonMap = new LinkedHashMap<>();
                if (administrator.isOwner()) {
                    buttonMap.put("Добавить нового администратора", callbackData_addNewAdmin);
                    buttonMap.put("Удалить администратора", callbackData_adminForDel);
                }
                buttonMap.put(backText1, callbackData_backToAdminMenu);
                EditMessageText editMessageText = botMethod.createUtilMenu(longChatId, messageId, textForMessage, buttonMap);
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals(callbackData_workWithAdmin)) {
                Specialist specialist = specialistRepository.findById(longChatId).get();
                String adminListText = adminRepository.findByOwnerId(specialist.getOwnerId()).toString().replace("[", "").replace("]", "").replaceAll(", ", "");
                String textForMessage = "Администраторы:\n" + adminListText;
                Map<String, String> buttonMap = new LinkedHashMap<>();
                if (specialist.isOwner()) {
                    if (specialist.getOwn_administrator() == null) {
                        buttonMap.put("Добавить администратора", "Добавить администратора");
                    } else {
                        buttonMap.put("Удалить администратора", callbackData_delMyAdmin);
                    }
                }
                buttonMap.put(backText1, callbackData_backToSpecMenu);
                EditMessageText editMessageText = botMethod.createUtilMenu(longChatId, messageId, textForMessage, buttonMap);
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals(callbackData_addNewAdmin)) {
                createRegistrationMenu(longChatId, messageId, stringChatId, adminToAdminIndex, "Администраторы", "Сгенерирован пароль для регистрации администратора:  ");

            } else if (callbackData.equals("Добавить администратора")) {
                createRegistrationMenu(longChatId, messageId, stringChatId, specialistToAdminIndex, "Администратор", "Сгенерирован пароль для регистрации администратора:  ");

            } else if (callbackData.equals(callbackData_adminForDel)) {
                executeEditMessageText(createDeleteAdminMenu(longChatId, messageId,
                        "Выберите администратора для удаления:", callbackData_delAdmin));

            } else if (callbackData.contains(callbackData_delAdmin)) {
                String adminId = callbackData.replace(callbackData_delAdmin, "");
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "❗ Подтвердите удаление.");
                editMessageText.setReplyMarkup(botMethod.receiveTwoButtonsMenu(backText1, callbackData_backToAdminMenu,
                        "Удалить", adminId + callbackData_approveDelAdmin));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_approveDelAdmin)) {
                String adminId = callbackData.replace(callbackData_approveDelAdmin, "");
                deleteAdministratorFromAdministrators(Long.parseLong(adminId));
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Администратор удален.");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText1, callbackData_backToAdminMenu));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains("Удалить мой профиль")) {
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Для удаления учетной записи отправьте в чат команду /deletedata");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText2, backToSettings(longChatId)));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains("Удалить клиента")) {
                String clientId = callbackData.replace("Удалить клиента", ""); // "Удалить клиента" + cli.getId()
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "❗ Внимание: после подтверждения будут удалены клиент и все записи клиента.\nПодтвердите удаление.");
                editMessageText.setReplyMarkup(botMethod.receiveTwoButtonsMenu(backText1, backToMenu(longChatId),
                        "Удалить", clientId + callbackData_approveDelClient));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_approveDelClient)) { // callbackData_approveDelClient + cli.getId()
                String clientId = callbackData.replace(callbackData_approveDelClient, "");
                deleteClient(longChatId, Long.parseLong(clientId));
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Клиент удален.");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText2, backToMenu(longChatId)));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_confirmAppointment)) {
                String appointmentId = callbackData.replace(callbackData_confirmAppointment, "");
                Appointment appointment = appointmentRepository.findById(Long.parseLong(appointmentId)).get();
                appointment.setConfirmAppointment(ConfirmAppointmentStatus.CONFIRMED.getStatusLabel());
                appointmentRepository.save(appointment);
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "✔ Спасибо за подтверждение!");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(okText, stringChatId + callbackData_deleteMessage + messageId));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_refuseAppointment)) {
                String appointmentId = callbackData.replace(callbackData_refuseAppointment, "");
                Optional<Appointment> optionalAppointment = appointmentRepository.findById(Long.parseLong(appointmentId));
                if (optionalAppointment.isPresent()) {
                    Appointment appointment = optionalAppointment.get();
                    Client client = clientRepository.findById(appointment.getClientId()).get();
                    Specialist specialist = specialistRepository.findById(appointment.getSpecialistId()).get();

                    if (specialist.isOwner() && specialist.getOwn_administrator() != null) {
                        SendMessage sendMessage = new SendMessage(String.valueOf(specialist.getId()), "❗ Клиент: " + client.receiveFullName() + "\nотменил визит к: " + specialist.receiveFullName() + "\nназначенный на дату: " + appointment.visitDateText() + "\nЗапись на эту дату удалена.");
                        sendMessage.setReplyMarkup(botMethod.receiveOneButtonMenu(okText, callbackData_showAwaitList + appointment.getOwnerId()));
                        executeSendMessage(sendMessage);
                    } else if (specialist.isOwner()) {
                        SendMessage sendMessage = new SendMessage(String.valueOf(specialist.getId()), "❗ Клиент: " + client.receiveFullName() + " отменил визит назначенный на дату: " + appointment.visitDateText() + "\nЗапись на эту дату удалена.");
                        sendMessage.setReplyMarkup(botMethod.receiveOneButtonMenu(okText, callbackData_showAwaitList + appointment.getOwnerId()));
                        executeSendMessage(sendMessage);
                    } else {
                        List<Administrator> administrators = adminRepository.findByOwnerId(appointment.getOwnerId());
                        administrators.forEach(it -> {
                            SendMessage sendMessage = new SendMessage(String.valueOf(it.getId()), "❗ Клиент: " + client.receiveFullName() + "\nотменил визит к: " + specialist.receiveFullName() + "\nназначенный на дату: " + appointment.visitDateText() + "\nЗапись на эту дату удалена.");
                            sendMessage.setReplyMarkup(botMethod.receiveOneButtonMenu(okText, callbackData_showAwaitList + appointment.getOwnerId()));
                            executeSendMessage(sendMessage);
                        });
                    }
                    appointmentRepository.delete(appointment);
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Вы отменили визит.");
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(okText, stringChatId + callbackData_deleteMessage + messageId));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains(callbackData_deleteMessage)) {
                String[] messageData = callbackData.split(callbackData_deleteMessage);
                executeDeleteMessage(new DeleteMessage(messageData[0], Integer.parseInt(messageData[1])));

            } else if (callbackData.contains(callbackData_showAwaitList)) {
                String ownerId = callbackData.replace(callbackData_showAwaitList, "");
                StringBuilder stringBuilder = new StringBuilder();
                List<Appointment> appointments = appointmentRepository.findByAwaitAppointment(Long.parseLong(ownerId));
                Set<Long> clientIds = appointments.stream().map(Appointment::getClientId).collect(Collectors.toSet());
                Map<Long, Client> clientsMap = clientRepository.findByIdIn(clientIds).stream().
                        collect(Collectors.toMap(Client::getId, Function.identity()));
                stringBuilder.append("Лист ожидания:\n");
                appointments.forEach(it -> stringBuilder.append(it.visitDateText()).append(" • ").
                        append(clientsMap.get(it.getClientId()).receiveShortName()).append("\n"));
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, stringBuilder.toString());
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(okText, stringChatId + callbackData_deleteMessage + messageId));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains("История посещений")) {
                String clientId = callbackData.replace("История посещений", ""); //   "История посещений" + clientId
                String textForMessage =  "История посещений:\n" + clientRepository
                        .findById(Long.parseLong(clientId)).get().getVisitHistory();
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText2, callbackData_findClientId + clientId));
                executeEditMessageText(editMessageText);

            } else if (callbackData.contains("Часовой пояс")) {
                String textForMessage =  "По умолчанию установлен часовой пояс UTC+3 : Москва. " +
                        "Для корректной работы приложения необходимо установить часовой пояс пользователя.";
                Map<String, String> buttonMap = new LinkedHashMap<>();
                TimeZone[] timeZones = TimeZone.values();
                for (TimeZone tz : timeZones) {
                    buttonMap.put(tz.label, callbackData_chooseTimeZone + tz.name());
                }
                buttonMap.put(backText2, backToSettings(longChatId));
                executeEditMessageText(botMethod.createUtilMenu(longChatId, messageId, textForMessage, buttonMap));

            } else if (callbackData.contains(callbackData_chooseTimeZone)) {
                TimeZone timeZone = TimeZone.valueOf(callbackData.replace(callbackData_chooseTimeZone, ""));
                String textForMessage = "Вы изменили часовой пояс на: " + timeZone.label;
                if (adminRepository.existsById(longChatId)) {
                    Administrator administrator = adminRepository.findById(longChatId).get();
                    administrator.setTimeZone(timeZone.timeShift);
                    adminRepository.save(administrator);
                    //TODO РЕАЛИЗОВАТЬ UPDATE SPECIALISTS and ADMINS
                } else {
                    Specialist specialist = specialistRepository.findById(longChatId).get();
                    specialist.setTimeZone(timeZone.timeShift);
                    specialistRepository.save(specialist);
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText2, backToSettings(longChatId)));
                executeEditMessageText(editMessageText);

            } else if (callbackData.equals(callbackData_sendMessageTime)) {
                String textForMessage =  "По умолчанию установлено время рассылки в 12:00.\n" +
                        "Вы можете выбрать другое время рассылки:";
                Map<String, String> buttonMap = new LinkedHashMap<>();
                for (int i = 7; i < 23; i++) {
                    buttonMap.put(i + ":00 ч.", callbackData_chooseSendTime + i);
                }
                buttonMap.put(backText2, backToSettings(longChatId));
                executeEditMessageText(botMethod.createUtilMenu(longChatId, messageId, textForMessage, buttonMap));

            } else if (callbackData.contains(callbackData_chooseSendTime)) {
                int sendTime = Integer.parseInt(callbackData.replace(callbackData_chooseSendTime, ""));
                String textForMessage = "Вы изменили время рассылки на: " + sendTime + ":00 ч.";
                if (adminRepository.existsById(longChatId)) {
                    Administrator administrator = adminRepository.findById(longChatId).get();
                    administrator.setSendTime(sendTime);
                    adminRepository.save(administrator);
                    //TODO РЕАЛИЗОВАТЬ UPDATE SPECIALISTS and ADMINS
                } else {
                    Specialist specialist = specialistRepository.findById(longChatId).get();
                    specialist.setSendTime(sendTime);
                    specialistRepository.save(specialist);
                }
                EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText2, backToSettings(longChatId)));
                executeEditMessageText(editMessageText);

            }







        }
    }


    @Override
    public String getBotUsername() {
        return AppConfig.botUsername;
    }


    public String backToMenu(long chatId) {
        if (adminRepository.findById(chatId).isPresent()) {
            return callbackData_backToAdminMenu;
        } else if (specialistRepository.findById(chatId).isPresent()) {
            return callbackData_backToSpecMenu;
        }
        return callbackData_backToUserMenu;
    }

    public String backToSettings(long chatId) {
        if (adminRepository.findById(chatId).isPresent()) {
            return callbackData_adminSettings;
        } else if (specialistRepository.findById(chatId).isPresent()) {
            return callbackData_specSettings;
        }
        return callbackData_userSettings;
    }

    // Сохранение пароля
    public void storePassword(String password, String id) {
        Cache cachePasswordToId = cacheManager.getCache("passwordToId");
        Cache cacheIdToPassword = cacheManager.getCache("idToPassword");
        cachePasswordToId.put(password, id);
        cacheIdToPassword.put(id, password);
    }


    // Получение добавленного Id, при отсутствии такового возвращает строку "-1"
    public String receiveIdByPassword(String password) {
        Cache cache = cacheManager.getCache("passwordToId");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(password);
            if (wrapper != null) {
                return wrapper.get().toString();
            }
        }
        return "null";
    }

    // Получение добавленного пароля по id, при отсутствии такового возвращает строку "null"
    public String receivePasswordByUserId(String id) {
        Cache cache = cacheManager.getCache("idToPassword");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(id);
            if (wrapper != null) {
                return wrapper.get().toString();
            }
        }
        return "null";
    }


    // Удаление добавленного пароля
    public void invalidatePasswordData(String password, String id) {
        Cache passCache = cacheManager.getCache("passwordToId");
        Cache idCache = cacheManager.getCache("idToPassword");
        if (passCache != null) {
            passCache.evict(password);
        }
        if (idCache != null) {
            idCache.evict(id);
        }
    }

    // Метод создает строку-пароль, последний символ которого определяет, для чего пароль был создан
    private String generatePassword(int registrationIndex) {
        int iteration = 0;
        int lastIndex = ThreadLocalRandom.current().nextInt(0, 10);
        int midIndex = ThreadLocalRandom.current().nextInt(registrationIndex, registrationIndex + 2);
        int password = ThreadLocalRandom.current().nextInt(100, 1000);

        while (!receiveIdByPassword(password + "" + midIndex + "" + lastIndex).equals("null")) {
            if (iteration == 900) {
                return err900;
            }
            password = ThreadLocalRandom.current().nextInt(100, 1000);
            iteration++;
        }
        return password + "" + midIndex + "" + lastIndex;
    }

    // Проверка валидности ФИО
    private boolean verifyRegisterData(long longChatId, String stringChatId, String messageText, String textForMessage, Map<String, String> registerData, String nextStepData) {
        tempData.put(stringChatId, "");
        String dataText = messageText.trim();
        EditMessageText editMessageText = new EditMessageText();
        int messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        editMessageText.setChatId(stringChatId);
        editMessageText.setMessageId(messageId);

        if (dataText.length() < 15 || !dataText.contains(" ") || !dataText.contains("#") || !dataText.contains("*") ||
                !dataText.contains("/")) {
            registerData.put(stringChatId, dataText);
            editMessageText.setText(textForMessage);
            tempData.put(stringChatId, nextStepData);
            editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, backToMenu(longChatId)));
            executeEditMessageText(editMessageText);
        } else {
            editMessageText.setText("Невалидный ввод");
            editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, backToMenu(longChatId)));
            executeEditMessageText(editMessageText);
            return false;
        }
        return true;
    }

    private void verifyPhoneNumber(long longChatId, String stringChatId, String messageText, String textForMessage, Map<String, String> registerData, String nextStepData) {
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
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, backToMenu(longChatId)));
                executeEditMessageText(editMessageText);
            }
        } catch (NumberFormatException e) {
            editMessageText.setText("Невалидный ввод");
            //log.error("SendMessage execute error: " + e.getMessage());
        }
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, backToMenu(longChatId)));
        executeEditMessageText(editMessageText);
    }

    private boolean verifyBirthDayDate(long longChatId, String stringChatId, String messageText, String textForMessage,
                                       Map<String, String> registerData, String nextStepData) {
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
                editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, backToMenu(longChatId)));
                executeEditMessageText(editMessageText);
                return true;
            }
        } catch (NumberFormatException e) {
            //log.error("SendMessage execute error: " + e.getMessage());
        }
        editMessageText.setText("Невалидный ввод");
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, backToMenu(longChatId)));
        executeEditMessageText(editMessageText);
        return false;
    }


    public void updateClient(long longChatId, String stringChatId, long clientId, long clientTgId) {
        Client client = clientRepository.findById(clientId).get();
        client.setTgId(clientTgId);
        client.setName(inputtedName.get(stringChatId));
        client.setSurname(inputtedSurname.get(stringChatId));
        client.setPatronymic(inputtedPatronymic.get(stringChatId));
        client.setPhoneNumber(inputtedPhoneNumber.get(stringChatId));
        client.setBirthdate(inputtedClientBirthdate.get(stringChatId));
        clientRepository.save(client);

        long messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        savedClientId.remove(stringChatId);
        savedClientTgId.remove(stringChatId);// TODO
        savedMessageId.remove(stringChatId);
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Данные клиента были изменены.");
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, backToMenu(longChatId)));
        executeEditMessageText(editMessageText);
    }


    public void createRegisterClientProcess(long longChatId, String stringChatId, long messageId) { // TODO не работает отбраковка
        boolean isAdminExist = adminRepository.existsById(longChatId);
        String textForMessage;
        if (isAdminExist && adminRepository.findById(longChatId).get().isOwner() ||
                specialistRepository.existsById(longChatId)) {
            textForMessage = "Введите фамилию и отправьте сообщение в чат";
            tempData.put(stringChatId, input_client_surname);
        } else if (isAdminExist && adminRepository.findById(longChatId).get().getCurrentSpecialistId() != -1) {
            Specialist specialist = specialistRepository.
                    findById(adminRepository.findById(longChatId).get().getCurrentSpecialistId()).get();
            textForMessage = "Вы добавляете нового клиента для специалиста " + specialist.receiveShortName() +
                    "\nВведите фамилию и отправьте сообщение в чат";
            tempData.put(stringChatId, input_client_surname);
        } else {
            textForMessage = "Сначала необходимо выбрать специалиста.";
        }
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, backToMenu(longChatId)));
        executeEditMessageText(editMessageText);
    }

    private void registerClient(long longChatId, String stringChatId) { // TODO утилизация данных и проверка наличия specialistId "UNCERTAIN"
        String callData;
        long ownerId;
        String textForMessage = "Новый клиент был добавлен. Вы можете авторизовать клиента выбрав соответствующую опцию в меню '" + callbackData_workWithClient + "'.";

        if (adminRepository.existsById(longChatId)) {
            callData = callbackData_backToAdminMenu;
            Administrator administrator = adminRepository.findById(longChatId).get();
            ownerId = administrator.getOwnerId();
            saveClientInDB(stringChatId, ownerId);
        } else {
            callData = callbackData_backToSpecMenu;
            ownerId = specialistRepository.findById(longChatId).get().getOwnerId();
            saveClientInDB(stringChatId, ownerId);
        }
        long messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, callData));
        executeEditMessageText(editMessageText);
    }

    private void registerSpecialist(long longChatId, String stringChatId) {
        Specialist specialist = new Specialist();
        specialist.setUseTime("");
        specialist.setOwner(true);
        specialist.setTimeZone(0);
        specialist.setSendTime(12);
        specialist.setPassword("");
        specialist.setId(longChatId);
        specialist.setProfession("");
        specialist.setPhoneNumber("");
        specialist.setOwnSendText("");
        specialist.setTgId(longChatId);
        specialist.setSubscribeData("");
        specialist.setOwnerId(longChatId);
        specialist.setReceptionSchedule("");
        specialist.setClientAppointmentRange("");
        specialist.setWorkTimeLength("8#21");
        specialist.setName(inputtedName.get(stringChatId));
        specialist.setSurname(inputtedSurname.get(stringChatId));
        specialist.setPatronymic(inputtedPatronymic.get(stringChatId));
        specialistRepository.save(specialist);

        long messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId,
                inputtedName.get(stringChatId) + " " + inputtedPatronymic.get(stringChatId) + ", спасибо за регистрацию!");
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, callbackData_backToSpecMenu));
        executeEditMessageText(editMessageText);
    }

    private void registerAdministrator(long longChatId, String stringChatId) {
        Administrator administrator = new Administrator();
        administrator.setOwner(true);
        administrator.setUseTime("");
        administrator.setTimeZone(0);
        administrator.setSendTime(12);
        administrator.setPassword("");
        administrator.setId(longChatId);
        administrator.setOwnSendText("");
        administrator.setPhoneNumber("");
        administrator.setOrganization("");
        administrator.setTgId(longChatId);
        administrator.setSubscribeData("");
        administrator.setOwnerId(longChatId);
        administrator.setWorkTimeLength("8#21");
        administrator.setCurrentSpecialistId(-1);
        administrator.setName(inputtedName.get(stringChatId));
        administrator.setSurname(inputtedSurname.get(stringChatId));
        administrator.setPatronymic(inputtedPatronymic.get(stringChatId));
        adminRepository.save(administrator);
        long messageId = savedMessageId.get(stringChatId) == null ? 0 : savedMessageId.get(stringChatId) + 1;
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId,
                inputtedName.get(stringChatId) + " " + inputtedPatronymic.get(stringChatId) + ", спасибо за регистрацию!");
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(callbackData_back, callbackData_backToAdminMenu));
        executeEditMessageText(editMessageText);
    }

    private void saveClientInDB(String stringChatId, long ownerId) {
        Client client = new Client();
        client.setTgId(-1);
        client.setClientNotes("");
        client.setOwnerId(ownerId);
        client.setVisitHistory("");
        client.setName(inputtedName.get(stringChatId));
        client.setConfirmAppointment(ConfirmAppointmentStatus.UNDEFINED.getStatusLabel()); // delete>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        client.setSurname(inputtedSurname.get(stringChatId));
        client.setPatronymic(inputtedPatronymic.get(stringChatId));
        client.setPhoneNumber(inputtedPhoneNumber.get(stringChatId));
        client.setBirthdate(inputtedClientBirthdate.get(stringChatId));
        clientRepository.save(client);
    }

    // показать список всех клиентов
    private void showAllClients(long longChatId, long messageId, String textForMessage, String callbackData) {
        Optional<Administrator> adminOptional = adminRepository.findById(longChatId);
        long ownerId;
        String mainMenuData;

        if (adminOptional.isPresent()) {
            mainMenuData = callbackData_backToAdminMenu;
            Administrator administrator = adminOptional.get();
            ownerId = administrator.getOwnerId();
        } else {
            ownerId = specialistRepository.findById(longChatId).get().getOwnerId();
            mainMenuData = callbackData_backToSpecMenu;
        }
        List<Client> clients = clientRepository.findByOwnerId(ownerId);
        if (clients.isEmpty()) {
            textForMessage = "Клиенты отсутствуют.";
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = botMethod.createClientsButtonSet(callbackData, clients, mainMenuData);
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        executeEditMessageText(editMessageText);
    }

    private EditMessageText receiveClientsSetByFirstSymbol(long longChatId, long messageId, String dataSymbol) {
        long ownerId;
        String mainMenuData;
        if (adminRepository.existsById(longChatId)) {
            Administrator administrator = adminRepository.findById(longChatId).get();
            mainMenuData = callbackData_backToAdminMenu;
            ownerId = administrator.getOwnerId();
        } else {
            Specialist specialist = specialistRepository.findById(longChatId).get();
            mainMenuData = callbackData_backToSpecMenu;
            ownerId = specialist.getOwnerId();
        }
        List<Client> clients = clientRepository.findByOwnerId(ownerId).stream().filter(cli -> cli.getSurname().toUpperCase().startsWith(dataSymbol)).toList();
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, "Выберите клиента из списка");
        editMessageText.setReplyMarkup(botMethod.createClientsButtonSet(callbackData_clientIdAppointment, clients, mainMenuData));
        return editMessageText;
    }

    // Метод, формирующий строку-сообщение (для меню поиска клиента по первому символу в фамилии) в зависимости от того, кто запрашивает метод
    private String createTextForClientSearch(long longChatId) { // TODO Проверить функционал на предмет возможности записи не овнером-админом
        String text;
        boolean isAdminExist = adminRepository.existsById(longChatId);
        if (isAdminExist && adminRepository.findById(longChatId).get().getCurrentSpecialistId() != -1) {
            Specialist specialist = specialistRepository.
                    findById(adminRepository.findById(longChatId).get().getCurrentSpecialistId()).get();
            text = "Специалист: " + specialist.receiveShortName() + "\n" + "Выберите первую букву фамилии клиента";
        } else if (specialistRepository.existsById(longChatId)) {
            text = "Выберите первую букву фамилии клиента";
        } else {
            text = "Специалист не выбран.";
        }
        return text;
    }

    private String createAppointmentText(long specialistId, String date) { // date = "dd.MM.yyyy"
        StringBuilder stringBuilder = new StringBuilder();
        List<Appointment> appointments = appointmentRepository.findBySpecialistId(specialistId).stream().
                filter(it -> it.getAppointmentDateTime().contains(date)).sorted().toList();
        // Собираем specialistId
        Set<Long> clientIds = appointments.stream().map(Appointment::getClientId).collect(Collectors.toSet());
        // Загружаем специалистов
        Map<Long, Client> clientsMap = clientRepository.findByIdIn(clientIds).stream().
                collect(Collectors.toMap(Client::getId, Function.identity()));

        for (Appointment appointment : appointments) { // TODO при удалении клиентов и наличии записи для этих клиентов меню не запускается
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
            if (administrator.getCurrentSpecialistId() == -1) {
                textForMenu = "Специалист: специалист не выбран";
            } else {
                Specialist specialist = specialistRepository.findById(administrator.getCurrentSpecialistId()).get();
                textForMenu = "Специалист: " + specialist.receiveFullName() + "\n" + createAppointmentText(administrator.getCurrentSpecialistId(), localDate.format(formatYear));// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
            }
        } else {
            // Specialist specialist = specialistRepository.findById(longChatId).get(); >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
            textForMenu = "Запись на сегодня:\n" + createAppointmentText(longChatId, localDate.format(formatYear));
        }
        return textForMenu;
    }


    private void cleanMapData(String stringChatId) {
        tempData.remove(stringChatId);
        // returnData.remove(stringChatId);
        inputtedName.remove(stringChatId);
        savedClientId.remove(stringChatId);
        savedMessageId.remove(stringChatId);
        inputtedSurname.remove(stringChatId);
        // registerPassword.remove(stringChatId);
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
        List<List<InlineKeyboardButton>> rowsInline = beginWorkTime(callbackData_endWeekWork);
        List<InlineKeyboardButton> rowInlineButtons = new ArrayList<>();
        InlineKeyboardButton weekendButton = new InlineKeyboardButton("Выходной день");
        weekendButton.setCallbackData(callbackData_beginWeekWork + "*");
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
        List<List<InlineKeyboardButton>> rowsInline = endWorkTime(time, callbackData_beginDayWork);
        List<InlineKeyboardButton> rowInlineButtonBack = new ArrayList<>();
        InlineKeyboardButton backButton = new InlineKeyboardButton("Расписание специалиста");
        backButton.setCallbackData("Расписание специалиста");
        rowInlineButtonBack.add(backButton);
        rowsInline.add(rowInlineButtonBack);
        return rowsInline;
    }

    private InlineKeyboardMarkup createWeekendButtonsSet(String choseDays) {
        Map<String, String> daysOfWeek = new LinkedHashMap<>();
        if (!choseDays.contains("1")) daysOfWeek.put("Понедельник", callbackData_weekDay + "1");
        if (!choseDays.contains("2")) daysOfWeek.put("Вторник", callbackData_weekDay + "2");
        if (!choseDays.contains("3")) daysOfWeek.put("Среда", callbackData_weekDay + "3");
        if (!choseDays.contains("4")) daysOfWeek.put("Четверг", callbackData_weekDay + "4");
        if (!choseDays.contains("5")) daysOfWeek.put("Пятница", callbackData_weekDay + "5");
        if (!choseDays.contains("6")) daysOfWeek.put("Суббота", callbackData_weekDay + "6");
        if (!choseDays.contains("0")) daysOfWeek.put("Воскресенье", callbackData_weekDay + "0");
        daysOfWeek.put("⏎  Готово", callbackData_chooseWeekDay);
        return botMethod.createDataButtonSet(daysOfWeek);
    }

    private InlineKeyboardMarkup createScheduleButtonsSet(String callBackData) {
        Map<String, String> schedule = new LinkedHashMap<>();
        schedule.put("1 через 1", "1" + callBackData);
        schedule.put("2 через 2", "2" + callBackData);
        schedule.put("3 через 3", "3" + callBackData);
        schedule.put("4 через 4", "4" + callBackData);
        schedule.put("Назад", "Расписание специалиста");
        return botMethod.createDataButtonSet(schedule);
    }

    private InlineKeyboardMarkup createDateButtonsSet(String callBackData) {
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter buttonFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, String> weekend = new LinkedHashMap<>();
        weekend.put(buttonFormat.format(localDate.plus(1L, DAYS)), dataFormat.format(localDate.plus(1L, DAYS)) + callBackData);
        weekend.put(buttonFormat.format(localDate.plus(2L, DAYS)), dataFormat.format(localDate.plus(2L, DAYS)) + callBackData);
        weekend.put(buttonFormat.format(localDate.plus(3L, DAYS)), dataFormat.format(localDate.plus(3L, DAYS)) + callBackData);
        weekend.put(buttonFormat.format(localDate.plus(4L, DAYS)), dataFormat.format(localDate.plus(4L, DAYS)) + callBackData);
        weekend.put(buttonFormat.format(localDate.plus(5L, DAYS)), dataFormat.format(localDate.plus(5L, DAYS)) + callBackData);
        weekend.put("Назад", "Расписание специалиста");
        return botMethod.createDataButtonSet(weekend);
    }

    private EditMessageText receiveClientsSetByFirstSymbol(long longChatId, long messageId, String firstSurnameSymbol, String callBackData, String textForMessage) {
        long ownerId;
        String mainMenuData;
        if (adminRepository.existsById(longChatId)) {
            Administrator administrator = adminRepository.findById(longChatId).get();
            mainMenuData = callbackData_backToAdminMenu;
            ownerId = administrator.getOwnerId();
        } else {
            Specialist specialist = specialistRepository.findById(longChatId).get();
            mainMenuData = callbackData_backToSpecMenu;
            ownerId = specialist.getOwnerId();
        }
        List<Client> clients = clientRepository.findByOwnerId(ownerId).stream().filter(cli -> cli.getSurname().toUpperCase().startsWith(firstSurnameSymbol)).toList();
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(botMethod.createClientsButtonSet(callBackData, clients, mainMenuData));
        return editMessageText;
    }

    public void createRegistrationMenu(long longChatId, long messageId, String stringChatId, int registrationIndex, String callbackData, String textForMessage) {
        String password = generatePassword(registrationIndex);
        boolean isPasswordExist = !receivePasswordByUserId(stringChatId).equals("null");
        boolean isOwner = adminRepository.existsById(longChatId) ? adminRepository.findById(longChatId).get().isOwner() :
                specialistRepository.findById(longChatId).get().isOwner();

        if (isPasswordExist) {
            textForMessage = "Вы уже сгенерировали пароль: " + receivePasswordByUserId(stringChatId) + "\nСрок действия пароля еще не истек.";
        } else if (password.equals(err900)) {
            textForMessage = "Ошибка генерации пароля...";
        } else if (isOwner) {
            textForMessage += password;
            storePassword(password, stringChatId);
        } else {
            textForMessage = "У вас нет прав для добавления. Обратитесь к администратору-владельцу базы данных.";
        }
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(botMethod.receiveOneButtonMenu(backText1, callbackData));
        executeEditMessageText(editMessageText);
    }

    private EditMessageText createDeleteAdminMenu(long longChatId, long messageId, String textForMessage, String callbackData) {
        Administrator administrator = adminRepository.findById(longChatId).get();
        Map<String, String> buttonMap = new LinkedHashMap<>();
        List<Administrator> administrators = adminRepository.findByOwnerId(administrator.getOwnerId()).stream().filter(it -> it.getId() != longChatId).toList();
        for (int i = 0; i < administrators.size(); i++) {
            buttonMap.put((i + 1) + ". " + administrators.get(i).receiveShortName(),
                    administrators.get(i).getId() + callbackData);
        }
        buttonMap.put(backText1, callbackData_backToAdminMenu);
        return botMethod.createUtilMenu(longChatId, messageId, textForMessage, buttonMap);
    }



    // @Scheduled(cron = "0 0 12 * * ?", zone = "Europe/Moscow") отправка строго по времени
    // @Scheduled(cron = "1 0 * * * *", zone = "Europe/Moscow")
    //  @Scheduled(cron = "0 * * * * *", zone = "Europe/Moscow")
    //  @Scheduled(cron = "0 */2 * * * *")
    public void createEveryHourEventsTest(){ //TODO
        collectMessagesForClients();
        deleteOldAppointments();
    }

    public void sendMessageIntervalTest(){ //TODO
        approveAppointment.forEach(this::executeSendMessage);
        approveAppointment.clear();
    }



    @Scheduled(cron = "0 0 * * * *", zone = "Europe/Moscow")
    public void createEveryHourEvents() {
        collectMessagesForClients();
        deleteOldAppointments();
    }

    @Scheduled(cron = "10 0 * * * *", zone = "Europe/Moscow")
    public void sendMessageInterval(){
        approveAppointment.forEach(this::executeSendMessage);
        approveAppointment.clear();
    }


    public void collectMessagesForClients() {
        LocalDateTime localDateTime = LocalDateTime.now().plus(1, DAYS);
        DateTimeFormatter testDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - hh");//TODO
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter sendTimeFormatter = DateTimeFormatter.ofPattern("k");
        int sendTime = Integer.parseInt(sendTimeFormatter.format(localDateTime));
        for (int timeZone = 1; timeZone > -10; timeZone--) {
            LocalDateTime todayDate = LocalDateTime.now().plus(timeZone + 24, HOURS);
            List<Appointment> appointments = appointmentRepository.findAppointmentByDateTimeZone(
                    dateFormatter.format(todayDate), sendTime, timeZone, -1);
            appointments.forEach(it -> {
                it.setConfirmAppointment(ConfirmAppointmentStatus.EXPECTANT.getStatusLabel());
                SendMessage sendMessage = new SendMessage(String.valueOf(it.getClientTgId()), "У вас запланирован визит к: " + it.getSpecialistName() + "\nна дату: " + it.visitDateText() + "\nПожалуйста подтвердите визит.");
                sendMessage.setReplyMarkup(botMethod.receiveTwoButtonsMenu("❌ Отказаться", callbackData_refuseAppointment + it.getId(),
                        "Подтвердить ✅", callbackData_confirmAppointment + it.getId()));
                approveAppointment.add(sendMessage);
            });
            System.out.println("TEST timeZone = " + timeZone + "; todayDate = " + testDateFormatter.format(todayDate) + "; sendTime = " + sendTime + ";>>>>>>>>>  List<Appointment> appointments = " + appointments);//TODO
            appointmentRepository.saveAll(appointments);
        }
    }

    @Transactional
    public void deleteOldAppointments() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"); // 29.11.2025
        List<Appointment> appointments = new ArrayList<>();
        for (int timeZone = 1; timeZone > -10; timeZone--) {
            LocalDateTime yesterdayDate = LocalDateTime.now().minus(timeZone + 24, HOURS);
           // appointmentRepository.removeAllByDateTime(dateFormatter.format(yesterdayDate), timeZone);
            appointments.addAll(appointmentRepository
                    .findAppointmentsByDateTime(dateFormatter.format(yesterdayDate), timeZone));
        }
        List <Long> clientsIds = new ArrayList<>();
        Map<Long, String> dataMap = new HashMap<>();
        appointments.forEach(it -> {
            dataMap.put(it.getClientId(), "• " + it.visitDateText() + " ▸ " + it.getAppointmentNote() + "\n");
            clientsIds.add(it.getClientId());
        });
        List<Client> clients = clientRepository.findByIdIn(clientsIds).stream().peek(
                it -> it.setVisitHistory(dataMap.get(it.getId()) + it.getVisitHistory())).toList();
        clientRepository.saveAll(clients);
        appointmentRepository.deleteAll(appointments);
    }

    /*
        public void sendMessagesToClients() {
        LocalDateTime localDateTime = LocalDateTime.now().plus(1, DAYS);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter sendTimeFormatter = DateTimeFormatter.ofPattern("h");
        int sendTime = Integer.parseInt(sendTimeFormatter.format(localDateTime));
        List<Appointment> appointments = appointmentRepository.findAppointmentByDateTime(
                dateFormatter.format(localDateTime), sendTime, -1);
        appointments.forEach(it -> {
            it.setConfirmAppointment(ConfirmAppointmentStatus.EXPECTANT.getStatusLabel());
            SendMessage sendMessage = new SendMessage(String.valueOf(it.getClientTgId()), "Подтвердите пожалуйста визит на дату: " + it.visitDateText());
            sendMessage.setReplyMarkup(botMethod.receiveTwoButtonsMenu("❌ Отказаться", callbackData_refuseAppointment + it.getId(),
                    "Подтвердить ✅", callbackData_confirmAppointment + it.getId()));
            approveAppointment.add(sendMessage);
        });
        appointmentRepository.saveAll(appointments);
    }


    @Transactional
    public void manageAppointments() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDateTime tomorrowDateTime = LocalDateTime.now().plus(1, DAYS);
        LocalDateTime yesterdayDateTime = LocalDateTime.now().minus(1, DAYS);
        appointmentRepository.removeAllByDate(dateFormatter.format(yesterdayDateTime));
        List<Appointment> appointments = appointmentRepository.findAppointmentByDate(
                dateFormatter.format(tomorrowDateTime), -1);
        appointments = appointments.stream()
                .peek(it -> it.setConfirmAppointment(ConfirmAppointmentStatus.EXPECTANT.getStatusLabel())).toList();
        appointmentRepository.saveAll(appointments);
    }
     */


    ///////////////////////////////////////////////////////////////////////////////////////////   MODEL   ////////////////////////////////////////////////////////////////////////////////////////////

    @Transactional
    public void deleteSpecialistFromAdministrators(long administratorId, long specialistId) {
        Specialist specialist = specialistRepository.findById(specialistId).get();
        specialistRepository.removeFromAllAdministrators(specialistId);
        List<Appointment> appointments = appointmentRepository.findBySpecialistId(specialistId);
        appointmentRepository.deleteAll(appointments);
        List<Administrator> administrators = adminRepository.findByOwnerId(adminRepository.
                        findById(administratorId).get().getOwnerId()).stream().
                filter(it -> it.getCurrentSpecialistId() == specialistId).
                peek(it -> it.setCurrentSpecialistId(-1)).toList();
        adminRepository.saveAll(administrators);
        specialist.getAdministrators().clear();
        specialist.setOwnerId(specialistId);
        specialist.setOwner(true);
        specialistRepository.save(specialist);
    }

    @Transactional
    public void addSpecialistToAdministrators(long administratorId, long specialistId) {
        Administrator administrator = adminRepository.findById(administratorId).get();
        Specialist specialist = specialistRepository.findById(specialistId).get();
        // Добавление атрибутов администратора-владельца приглашенному специалисту
        specialist.setOwnerId(administrator.getOwnerId());
        specialist.setOwner(false);
        specialist.setSendTime(administrator.getSendTime());
        specialist.setTimeZone(administrator.getTimeZone());
        specialist.setOwnSendText(administrator.getOwnSendText());
        specialist.setWorkTimeLength(administrator.getWorkTimeLength());
        specialistRepository.save(specialist);
        // Все личные клиенты специалиста должны быть удалены
        clientRepository.deleteAll(clientRepository.findByOwnerId(specialistId));
        Specialist addedSpecialist = specialistRepository.findById(specialistId).get();
        // Добавление специалиста всем администратором
        List<Administrator> administrators = adminRepository.findByOwnerId(administrator.getOwnerId()).stream().
                peek(it -> it.getSpecialists().add(addedSpecialist)).toList();
        adminRepository.saveAll(administrators);

    }

    @Transactional
    public void addAdministratorToAdministrators(long appenderId, long administratorId) {
        Administrator superAdministrator = adminRepository.findById(appenderId).get();
        Administrator administrator = adminRepository.findById(administratorId).get();
        administrator.setOwnerId(superAdministrator.getOwnerId());
        administrator.setOwner(false);
        administrator.setSendTime(superAdministrator.getSendTime());
        administrator.setTimeZone(superAdministrator.getTimeZone());
        administrator.setOwnSendText(superAdministrator.getOwnSendText());
        administrator.setCurrentSpecialistId(superAdministrator.getCurrentSpecialistId());
        adminRepository.save(administrator);
        adminRepository.addAdministratorToAllSpecialists(administratorId);
    }

    @Transactional
    public void deleteAdministratorFromAdministrators(long administratorId) {
        adminRepository.removeFromAllAdministrators(administratorId);
        Administrator administrator = adminRepository.findById(administratorId).get();
        administrator.setOwner(true);
        administrator.setCurrentSpecialistId(-1);
        administrator.setOwnerId(administratorId);
        adminRepository.save(administrator);
    }

    @Transactional
    public void addAdministratorToSpecialist(long specialistId, long administratorId) {
        Administrator administrator = adminRepository.findById(administratorId).get();
        clientRepository.deleteAllByOwnerId(administratorId);
        Specialist specialist = specialistRepository.findById(specialistId).get();
        administrator.setOwnerId(specialist.getOwnerId());
        administrator.setCurrentSpecialistId(specialistId);
        administrator.setOwner(false);
        administrator.setSendTime(specialist.getSendTime());
        administrator.setTimeZone(specialist.getTimeZone());
        adminRepository.save(administrator);
        administrator.getSpecialists_owners().add(specialist);
        administrator = adminRepository.findById(administratorId).get();
        specialist.setOwn_administrator(administrator);
        specialistRepository.save(specialist);
    }


    public void deleteAdminFromSpecialist(long specialistId, long administratorId) {
        Specialist specialist = specialistRepository.findById(specialistId).get();
        Administrator administrator = adminRepository.findById(administratorId).get();
        Specialist specialistForDel = administrator.getSpecialists_owners().stream().filter(it -> it.getId() == specialistId).findFirst().get();
        administrator.getSpecialists_owners().remove(specialistForDel);
        if (administrator.getSpecialists_owners().isEmpty()) {
            administrator.setOwnerId(administratorId);
            administrator.setOwner(true);
            administrator.setCurrentSpecialistId(-1);
        }
        adminRepository.save(administrator);
        specialist.setOwn_administrator(null);
        specialistRepository.save(specialist);
    }

    @Transactional
    public void deleteSpecialistData(long specialistId) {
        Specialist specialist = specialistRepository.findById(specialistId).get();
        if (specialist.isOwner()) {
            clientRepository.deleteAllByOwnerId(specialistId);
            appointmentRepository.removeAllByOwnerId(specialistId);
            if (specialist.getOwn_administrator() != null) {
                deleteAdminFromSpecialist(specialistId, specialist.getOwn_administrator().getId());
            }
        }
        specialistRepository.removeFromAllAdministrators(specialistId);
        List<Administrator> administrators = adminRepository.findByCurrentSpecialistId(specialistId).stream().peek(it -> it.setCurrentSpecialistId(-1)).toList();
        adminRepository.saveAll(administrators);
        specialistRepository.delete(specialist);
    }

    @Transactional
    public void deleteAdministratorData(long administratorId) {
        Administrator administrator = adminRepository.findById(administratorId).get();
        if (administrator.isOwner()) {
            List<Administrator> administrators = adminRepository.findByOwnerId(administratorId).stream().peek(it -> {
                it.setCurrentSpecialistId(-1);
                it.setOwnerId(it.getId());
                it.setOrganization("");
                it.setOwner(true);
                it.getSpecialists().clear();
            }).toList();
            adminRepository.removeAllByOwnerId(administratorId);
            adminRepository.saveAll(administrators);

            List<Specialist> specialists = specialistRepository.findByOwnerId(administratorId).stream().peek(it -> {
                it.getAdministrators().clear();
                it.setOwnerId(it.getId());
                it.setOwner(true);
            }).toList();
            specialistRepository.saveAll(specialists);

            clientRepository.deleteAllByOwnerId(administratorId);
            appointmentRepository.removeAllByOwnerId(administratorId);
        } else {
            List<Specialist> specialists = specialistRepository.findByIdIn(
                    administrator.getSpecialists_owners().stream().map(Specialist::getId).toList()
            ).stream().peek(it -> it.setOwn_administrator(null)).toList();
            specialistRepository.saveAll(specialists);
        }
        adminRepository.delete(administrator);
    }

    public void deleteUserData(long userId) {
        List<Client> clients = clientRepository.findByTgId(userId).stream().peek(it -> it.setTgId(0)).toList();
        clientRepository.saveAll(clients);
        User user = userRepository.findById(userId).get();
        userRepository.delete(user);
    }

    public void deleteClient(long ownerId, long clientId) {
        clientRepository.deleteById(clientId);
        appointmentRepository.deleteAll(appointmentRepository.findByClientId(ownerId));
    }

///////////////////////////////////////////////////////////////////////////////////////////   MODEL   ////////////////////////////////////////////////////////////////////////////////////////////

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
            // log.error("SendMessage execute error: " + e.getMessage());
            System.out.println("Err: " + e.getMessage()); //TODO
        }
        return invoiceLincUrl;
    }


    private void executeDeleteMessage(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            System.out.println("TEST ERROR >>>>" + e.getMessage()); //TODO
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

    private EditMessageText appointmentMenu(long longChatId, long messageId) {
        DateTimeFormatter parseDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMM");
        String mainMenuData;
        String textForMessage;
        long specialistId;

        if (adminRepository.existsById(longChatId)) {
            mainMenuData = callbackData_backToAdminMenu;
            Administrator administrator = adminRepository.findById(longChatId).get();

            if (administrator.getCurrentSpecialistId() != -1) {
                Specialist specialist = specialistRepository.findById(administrator.getCurrentSpecialistId()).get();
                textForMessage = "Запись к специалисту: " + specialist.receiveFullName() + "\nДля просмотра информации о записи нажмите соответствующую клавишу.";
                specialistId = specialist.getId();
            } else {
                textForMessage = "Специалист не выбран.";
                specialistId = -1;
            }
        } else {
            mainMenuData = callbackData_backToSpecMenu;
            textForMessage = "Запись к вам:\nДля просмотра информации о записи нажмите соответствующую клавишу.";
            specialistId = longChatId;
        }
        List<Appointment> appointments = appointmentRepository.findBySpecialistId(specialistId).stream().sorted().toList();
        Set<Long> clientIds = appointments.stream().map(Appointment::getClientId).collect(Collectors.toSet());
        // Загрузка клиентов
        Map<Long, Client> clientsMap = clientRepository.findByIdIn(clientIds).stream().
                collect(Collectors.toMap(Client::getId, Function.identity()));
        Map<String, String> buttonData = new LinkedHashMap<>();
        for (Appointment apt : appointments) {
            String[] dateAndTime = apt.getAppointmentDateTime().split(" - ");
            LocalDate date = LocalDate.parse(dateAndTime[0], parseDate);
            buttonData.put(dateFormatter.format(date) + " " + dateAndTime[1].replace("/", ":") + " • " +
                    clientsMap.get(apt.getClientId()).receiveShortName(), apt.getId() + callbackData_appointmentInfo);
        }
        buttonData.put(backText1, mainMenuData);
        return botMethod.createUtilMenu(longChatId, messageId, textForMessage, buttonData);
    }

    private EditMessageText showAppointment(long longChatId, long messageId, long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId).get();
        Client client = clientRepository.findById(appointment.getClientId()).get();
        String textForMessage = "Клиент: " + client.receiveFullName() + "\n" +
                appointment.getAppointmentDateTime().replace("/", " • ") + "\n" + "Комментарий: " +
                appointment.getAppointmentNote();
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(botMethod.receiveTwoButtonsMenu(backText2, "Посмотреть запись", "Удалить запись", appointmentId + callbackData_approveDelAppoint));
        return editMessageText;
    }

    private EditMessageText approveDeleteAppointment(long longChatId, long messageId, long appointmentId) {
        String mainMenuData = adminRepository.existsById(longChatId) ? callbackData_backToAdminMenu : callbackData_backToSpecMenu;
        String textForMessage = "❗ Необходимо подтвердить удаление.";
        EditMessageText editMessageText = botMethod.createEditMessageText(longChatId, messageId, textForMessage);
        editMessageText.setReplyMarkup(botMethod.receiveTwoButtonsMenu("Отмена", mainMenuData, "Удалить", appointmentId + callbackData_delAppoint));
        return editMessageText;
    }


    String textToUser = "Здравствуйте! Вы можете смотреть и изменять свою запись, " +
            "можете зарегистрироваться в качестве специалиста или администратора";

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private InlineKeyboardMarkup createSpecialistMenu() {
        Map<String, String> dataButtonSet = new LinkedHashMap<>();
        dataButtonSet.put(callbackData_addClient, callbackData_addClient);
        dataButtonSet.put("Записать на прием", "Записать на прием");
        dataButtonSet.put("Посмотреть запись", "Посмотреть запись");
        dataButtonSet.put(callbackData_workWithClient, callbackData_workWithClient);
        dataButtonSet.put(callbackData_workWithAdmin, callbackData_workWithAdmin);
        dataButtonSet.put(callbackData_specSettings, callbackData_specSettings);
        return botMethod.createDataButtonSet(dataButtonSet);
    }

    private InlineKeyboardMarkup createAdminMenu() {
        Map<String, String> dataButtonSet = new LinkedHashMap<>();
        dataButtonSet.put(callbackData_choseSpecialist, callbackData_choseSpecialist);
        dataButtonSet.put(callbackData_addClient, callbackData_addClient);
        dataButtonSet.put("Записать на прием", "Записать на прием");
        dataButtonSet.put("Посмотреть запись", "Посмотреть запись");
        dataButtonSet.put(callbackData_workWithClient, callbackData_workWithClient);
        dataButtonSet.put(callbackData_workWithSpecialist, callbackData_workWithSpecialist);
        dataButtonSet.put(callbackData_workWithAdministrators, callbackData_workWithAdministrators);
        dataButtonSet.put(callbackData_adminSettings, callbackData_adminSettings);
        return botMethod.createDataButtonSet(dataButtonSet);
    }


    private long registerTestSpecialist() {
        long id = ThreadLocalRandom.current().nextInt(100, 1000);
        Specialist specialist = new Specialist();
        specialist.setOwner(true);
        specialist.setTimeZone(0);
        specialist.setSendTime(12);
        specialist.setPassword("");
        specialist.setId(id);
        specialist.setProfession("");
        specialist.setPhoneNumber("");
        specialist.setReceptionSchedule("");
        specialist.setOwnerId(id);
        specialist.setClientAppointmentRange("");
        specialist.setWorkTimeLength("8#21");
        specialist.setName("Иван" + id);
        specialist.setSurname("Иванович" + id);
        specialist.setPatronymic("Иванов" + id);
        specialistRepository.save(specialist);
        return id;
    }


}