package hospital.utils;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {
    
    public static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
    
    public static final DateTimeFormatter DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern(Constants.DATETIME_FORMAT);
    
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = 
        DateTimeFormatter.ofPattern(Constants.DISPLAY_DATE_FORMAT);
    
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern(Constants.DISPLAY_DATETIME_FORMAT);
    
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE_FORMATTER) : "";
    }
    
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_DATETIME_FORMATTER) : "";
    }
    
    public static LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DISPLAY_DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static LocalDateTime parseDateTime(String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString, DISPLAY_DATETIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return 0;
        return (int) ChronoUnit.YEARS.between(dateOfBirth, LocalDate.now());
    }
    
    public static boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.toLocalDate().equals(LocalDate.now());
    }
    
    public static boolean isFutureDate(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.isAfter(LocalDateTime.now());
    }
    
    public static boolean isPastDate(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.isBefore(LocalDateTime.now());
    }
}
