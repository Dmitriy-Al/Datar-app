package ru.alimovdev.datar.service;

@lombok.Getter
public enum ScheduleType { // "Фиксированный график", "Четный/нечетный график", "Скользящий график"

    FIX_DAYS("FIX_DAYS") { // "Фиксированный график"
        @Override
        public String createSchedule(String scheduleData) { // 8#17/8#17/7#17/8#17/8#17/*/* -
            String scheduleString = "график не установлен.";
            if (!scheduleData.isEmpty()) {
                String day;
                String[] days = scheduleData.replace(this.getLabel(),  "").replace("*",  "выходной").split("/");
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < days.length; i++) {
                    day = switch(i) {
                        case 0 ->  "Понедельник";
                        case 1 ->  "Вторник";
                        case 2 ->  "Среда";
                        case 3 ->  "Четверг";
                        case 4 ->  "Пятница";
                        case 5 ->  "Суббота";
                        case 6 ->  "Воскресенье";
                        default -> "err. day";
                    };
                    String workTime = days[i].contains("#") ? days[i].replace("#",  ":00 - ") + ":00 " : days[i];
                    stringBuilder.append(day).append(":  ").append(workTime).append("\n");
                }
                scheduleString = stringBuilder.toString();
            }
            return scheduleString;
        }
    },
    EVEN_ODD_DAYS("EVEN_ODD_DAYS") { // "Четный/нечетный график"
        @Override
        public String createSchedule(String scheduleData) { // 8#17/17#22/6/0/
            String scheduleString = "график не установлен.";
            if (!scheduleData.isEmpty()) {
                String[] splitScheduleData = scheduleData.replace(this.getLabel(),  "").split("/");
                String oddDayWorkTime = splitScheduleData[0].replace("#", ":00 - ") + ":00";
                String evenDayWorkTime = splitScheduleData.length > 1 ? splitScheduleData[1].replace("#", ":00 - ") + ":00" : "";
                String weekend;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Часы работы для нечетных дней месяца:  ").append(oddDayWorkTime).append("\n")
                        .append("Часы работы для четных дней месяца:  ").append(evenDayWorkTime).append("\n");

               for (int i = 2; i < splitScheduleData.length; i++) {
                   int dayNumber = Integer.parseInt(splitScheduleData[i]);
                   weekend = switch(dayNumber) {
                        case 1 ->  "Понедельник";
                        case 2 ->  "Вторник";
                        case 3 ->  "Среда";
                        case 4 ->  "Четверг";
                        case 5 ->  "Пятница";
                        case 6 ->  "Суббота";
                        case 0 ->  "Воскресенье";
                        default -> "err. day";
                    };
                    stringBuilder.append(weekend).append(" - выходной день\n");
             }
                scheduleString = stringBuilder.toString();
            }
            return scheduleString;
        }
    },
    ROLLING_CHART("ROLLING_CHART") { // "Скользящий график"
        public String createSchedule(String scheduleData) { // 8#17/2/2025-11-07
            String scheduleString = "график не установлен.";
            if (!scheduleData.isEmpty()) {
                String[] splitScheduleData = scheduleData.replace(this.getLabel(),  "").split("/");
                String workTime = "Часы работы :  " + splitScheduleData[0].replace("#", ":00 - ") + ":00\n";
                String schedule = splitScheduleData.length > 1 ? "График работы:  " + splitScheduleData[1] +
                        " через " + splitScheduleData[1] : "График работы:  \n";
                scheduleString = workTime + schedule;
            }
            return scheduleString;
        }
    };


    // Да, да, да, я в знаю что есть .name()
    private final String label;

    ScheduleType(String label) {
        this.label = label;
    }

    abstract String createSchedule(String scheduleData);

    public String receiveScheduleString(String scheduleData) {
        for (ScheduleType st : ScheduleType.values()) {
            if (scheduleData.contains(st.label)) {
                return  st.createSchedule(scheduleData);
            }
        }
        return "";
    }




}
