/**
 *
 *  @author Woźnicki Piotr SO0139
 *
 */

package zad1;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class Time {

    public static String passed(String from, String to) {
        Locale.setDefault(new Locale("pl", "PL"));

        try {
            boolean isDateTime = from.contains("T") && to.contains("T");
            LocalDateTime fromDateTime = isDateTime ? LocalDateTime.parse(from) : LocalDate.parse(from).atStartOfDay();
            LocalDateTime toDateTime = isDateTime ? LocalDateTime.parse(to) : LocalDate.parse(to).atStartOfDay();

            return generateInfo(fromDateTime, toDateTime, isDateTime);

        } catch (DateTimeParseException ex) {
            return "*** java.time.format.DateTimeParseException: " + ex.getMessage();
        }
    }

    private static String generateInfo(LocalDateTime from, LocalDateTime to, boolean isDateTime) {
        ZonedDateTime zFrom = ZonedDateTime.of(from, ZoneId.of("Europe/Warsaw"));
        ZonedDateTime zTo = ZonedDateTime.of(to, ZoneId.of("Europe/Warsaw"));

        long daysBetween = ChronoUnit.DAYS.between(zFrom.toLocalDate(), zTo.toLocalDate());
        double weeksBetween = daysBetween / 7.0;
        String weeks = String.format("%.2f", weeksBetween).replace(',', '.');

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(isDateTime ? "d MMMM yyyy (EEEE) 'godz.' hh:mm" : "d MMMM yyyy (EEEE)");

        StringBuilder sb = new StringBuilder();
        sb.append("Od ").append(zFrom.format(dtf))
                .append(" do ").append(zTo.format(dtf))
                .append("\n- mija: ").append(daysBetween).append(daysBetween == 1 ? " dzień, " : " dni, ")
                .append("tygodni ").append(weeks);

        if (isDateTime) {
            long hours = Duration.between(zFrom, zTo).toHours();
            long minutes = Duration.between(zFrom, zTo).toMinutes();
            sb.append("\n- godzin: ").append(hours).append(", minut: ").append(minutes);
        }

        sb.append(getCalendarInfo(zFrom.toLocalDate(), zTo.toLocalDate()));
        return sb.toString();
    }

    public static String getCalendarInfo(LocalDate from, LocalDate to) {
        long daysBetween = ChronoUnit.DAYS.between(from, to);
        String result = "";
        if (daysBetween != 0) {
            Period period = Period.between(from, to);
            int years = period.getYears();
            int months = period.getMonths();
            int days = period.getDays();

            result += "\n- kalendarzowo: ";

            if (years != 0) {
                if (years == 1) {
                    result += years + " rok, ";
                } else if (years < 5) {
                    result += years + " lata, ";
                } else {
                    result += years + " lat, ";
                }
            }
            if (months != 0) {
                if (months == 1) {
                    result += months + " miesiąc, ";
                } else if (months < 5) {
                    result += months + " miesiące, ";
                } else {
                    result += months + " miesięcy, ";
                }
            }
            if (days != 0) {
                if ( days == 1) {
                    result += days + " dzień, ";
                } else {
                    result += days + " dni, ";
                }
            }
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }
}
