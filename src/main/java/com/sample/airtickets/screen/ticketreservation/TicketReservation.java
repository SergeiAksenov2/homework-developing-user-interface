package com.sample.airtickets.screen.ticketreservation;

import com.sample.airtickets.app.TicketService;
import com.sample.airtickets.entity.Airport;
import com.sample.airtickets.entity.Flight;
import io.jmix.core.LoadContext;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.DateField;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundTaskHandler;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@UiController("TicketReservation")
@UiDescriptor("ticket-reservation.xml")
@LookupComponent("ticketReservationTable")
public class TicketReservation extends Screen {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private EntityComboBox<Airport> airportFromSelector;
    @Autowired
    private EntityComboBox<Airport> airportToSelector;
    @Autowired
    private DateField<LocalDate> takeOffDateSelector;
    @Autowired
    private CollectionContainer<Flight> flightsDc;
    @Autowired
    private Notifications notifications;

    @Autowired
    private BackgroundWorker backgroundWorker;

    @Subscribe("ticketSearch")
    public void onTicketSearch(final Action.ActionPerformedEvent event) {
        if (airportFromSelector == null && airportToSelector == null && takeOffDateSelector == null) {
            flightsDc.setItems(Collections.emptyList());
            showNotification();
        } else {
            BackgroundTask<Integer, List<Flight>> getFlightsTask = new FlightsTask(airportFromSelector, airportToSelector, takeOffDateSelector);
            BackgroundTaskHandler<List<Flight>> handle = backgroundWorker.handle(getFlightsTask);
            handle.execute();
            List<Flight> result = handle.getResult();
            flightsDc.setItems(result);
        }
    }

    @Install(to = "flightsTableDl", target = Target.DATA_LOADER)
    private List<Flight> flightsTableDlLoadDelegate(final LoadContext<Flight> loadContext) {
        if (airportFromSelector.getValue() != null || airportToSelector.getValue() != null || takeOffDateSelector.getValue() != null) {
            BackgroundTask<Integer, List<Flight>> getFlightsTask = new FlightsTask(airportFromSelector, airportToSelector, takeOffDateSelector);
            BackgroundTaskHandler<List<Flight>> handle = backgroundWorker.handle(getFlightsTask);
            handle.execute();
            return handle.getResult();
        }
        //showNotification();
        return Collections.emptyList();
    }

    private void showNotification() {
        notifications.create()
                .withType(Notifications.NotificationType.WARNING)
                .withDescription("Please fill at least one filter field")
                .show();
    }

    // Simple way
    private List<Flight> getFlights_(EntityComboBox<Airport> airportFromSelector, EntityComboBox<Airport> airportToSelector, DateField<LocalDate> takeOffDateSelector) {
        return ticketService.searchFlights(airportFromSelector.getValue(), airportToSelector.getValue(), takeOffDateSelector.getValue());
    }



    private class FlightsTask extends BackgroundTask<Integer, List<Flight>> {
        private final EntityComboBox<Airport> airportFromSelector;
        private final EntityComboBox<Airport> airportToSelector;
        private final DateField<LocalDate> takeOffDateSelector;

        public FlightsTask(EntityComboBox<Airport> airportFromSelector, EntityComboBox<Airport> airportToSelector, DateField<LocalDate> takeOffDateSelector) {
            super(1000, TimeUnit.MINUTES, TicketReservation.this);
            this.airportFromSelector = airportFromSelector;
            this.airportToSelector = airportToSelector;
            this.takeOffDateSelector = takeOffDateSelector;
        }

        @SuppressWarnings("NullableProblems")
        public List<Flight> run(TaskLifeCycle<Integer> taskLifeCycle) throws Exception {
            int i = 0;
            if (taskLifeCycle.isCancelled() || taskLifeCycle.isInterrupted()) {
                return null;
            }
            TimeUnit.SECONDS.sleep(2);
            i++;
            taskLifeCycle.publish(i);
            return ticketService.searchFlights(airportFromSelector.getValue(), airportToSelector.getValue(), takeOffDateSelector.getValue());
        }

        @Override
        public void done(List<Flight> result) {
            notifications.create()
                    .withCaption("Got flights!")
                    .withType(Notifications.NotificationType.TRAY)
                    .show();
        }

        @Override
        public void progress(List<Integer> changes) {
            super.progress(changes);
        }

        @Override
        public void canceled() {
            super.canceled();
        }

        @Override
        public boolean handleTimeoutException() {
            return super.handleTimeoutException();
        }

        @Override
        public boolean handleException(Exception ex) {
            return super.handleException(ex);
        }
    }

}