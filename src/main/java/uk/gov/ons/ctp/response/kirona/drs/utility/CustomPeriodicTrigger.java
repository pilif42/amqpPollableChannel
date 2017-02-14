package uk.gov.ons.ctp.response.kirona.drs.utility;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import uk.gov.ons.ctp.response.kirona.drs.config.PollerConfig;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A trigger to meet Kirona DRS needs
 */
@Slf4j
@Data
public class CustomPeriodicTrigger implements Trigger {

    private static final String TIME_FORMAT = "%d.%d";

    @Inject
    private PollerConfig pollerConfig;

    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        Date currentDate = new Date(System.currentTimeMillis());
        Date earliestActiveDate = provideEarliestActiveDate(currentDate);
        if (earliestActiveDate != null) {
            return new Date(earliestActiveDate.getTime() + pollerConfig.getInitialDelay());
        } else {
            if (triggerContext.lastScheduledExecutionTime() == null) {
                return new Date(System.currentTimeMillis() + pollerConfig.getInitialDelay());
            } else if (pollerConfig.isFixedRate()) {
                return new Date(triggerContext.lastScheduledExecutionTime().getTime() +
                        pollerConfig.getFixedDelay());
            }
            return new Date(triggerContext.lastCompletionTime().getTime() + pollerConfig.getFixedDelay());
        }
    }

    /**
     * This returns null if the current date is already active, ie outside of support hours.
     * If the current date is within support hours, it returns the earliest active date.
     *
     * @param date the current date
     * @return aDate
     */
    private Date provideEarliestActiveDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dateHours = calendar.get(Calendar.HOUR_OF_DAY);
        int dateMinutes = calendar.get(Calendar.MINUTE);
        String dateValue = String.format(TIME_FORMAT, dateHours, dateMinutes);
        Double dateValueDouble = Double.valueOf(dateValue);

        List<String> supportHoursStarts = pollerConfig.getSupportHoursStart();
        int numberOfSupportSlots = supportHoursStarts.size();

        int buffer = -1;
        for (int i = 0; i < numberOfSupportSlots; i++) {
            if (dateValueDouble >= calculateBufferedStartBoundary(supportHoursStarts.get(i))) {
                buffer = i;
            } else {
                break;
            }
        }

        Date result = null;
        if (buffer >= 0) {
            String supportHoursEndBoundary = pollerConfig.getSupportHoursEnd().get(buffer);
            if (dateValueDouble < Double.valueOf(supportHoursEndBoundary)) {
                result = buildDate(supportHoursEndBoundary);
            }
        }
        return result;
    }

  /**
   * This applies a buffer to the start of support hours. The buffer is a number of minutes.
   *
   * We need a buffer as we should NOT process ActionInstructions until the last minute because they take time to be
   * processed and we would encounter failures with DRS.
   *
   * @param supportHoursStart the start of support hours, ie 4.30 for 04h30
   * @return the buffered start of support hours
   */
    private double calculateBufferedStartBoundary(String supportHoursStart) {
        int commaPosition = supportHoursStart.indexOf('.');
        int startBoundaryyHour = new Integer(supportHoursStart.substring(0, commaPosition));
        int startBoundaryMinute = new Integer(supportHoursStart.substring(commaPosition + 1,
                supportHoursStart.length()));
        int buffer = pollerConfig.getSupportHoursBuffer();

        String result = null;
        if (startBoundaryMinute >= buffer) {
            result = String.format(TIME_FORMAT, startBoundaryyHour, startBoundaryMinute - buffer);
        } else if (startBoundaryMinute < buffer) {
            result = String.format(TIME_FORMAT, startBoundaryyHour - 1, 60 - (buffer - startBoundaryMinute));
        }

        return Double.valueOf(result);
    }

    private Date buildDate(String endBoundary) {
        int commaPosition = endBoundary.indexOf('.');
        int endBoundaryHour = new Integer(endBoundary.substring(0, commaPosition));
        int endBoundaryMinute = new Integer(endBoundary.substring(commaPosition + 1, endBoundary.length()));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, endBoundaryHour);
        calendar.set(Calendar.MINUTE, endBoundaryMinute);
        return calendar.getTime();
    }
}
