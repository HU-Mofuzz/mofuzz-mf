package de.hub.mse.emf.multifile;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.Size;
import de.hub.mse.emf.multifile.impl.calendar.CalendarGenerator;
import de.hub.mse.emf.multifile.impl.calendar.CalendarLogic;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import static java.util.Calendar.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@RunWith(JQF.class)
public class CalendarTest {

    @Fuzz
    public void testLeapYear(@From(CalendarGenerator.class) GregorianCalendar cal) {
        // Assume that the date is Feb 29
        Assume.assumeTrue(cal.get(MONTH) == FEBRUARY);
        Assume.assumeTrue(cal.get(DAY_OF_MONTH) == 29);

        // Under this assumption, validate leap year rules
        Assert.assertTrue(cal.get(YEAR) + " should be a leap year", CalendarLogic.isLeapYear(cal));
    }

    @Fuzz
    public void testCompare(@Size(max=100) List<@From(CalendarGenerator.class) GregorianCalendar> cals) {
        // Sort list of calendar objects using our custom comparator function
        Collections.sort(cals, CalendarLogic::compare);

        // If they have an ordering, then the sort should succeed
        for (int i = 1; i < cals.size(); i++) {
            Calendar c1 = cals.get(i-1);
            Calendar c2 = cals.get(i);
            Assume.assumeFalse(c1.equals(c2)); // Assume that we have distinct dates
            Assert.assertTrue(c1 + " should be before " + c2, c1.before(c2));  // Then c1 < c2
        }
    }
}
