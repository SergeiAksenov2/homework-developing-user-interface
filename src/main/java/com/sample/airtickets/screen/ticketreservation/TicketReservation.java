package com.sample.airtickets.screen.ticketreservation;

import com.sample.airtickets.app.TicketService;
import com.sample.airtickets.entity.Airport;
import com.sample.airtickets.entity.Flight;
import com.sample.airtickets.entity.Ticket;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.ui.*;
import io.jmix.ui.action.Action;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.DialogOutcome;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.*;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundTaskHandler;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import io.jmix.ui.screen.LookupComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.jmix.ui.screen.OpenMode.NEW_TAB;

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
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private ScreenBuilders screenBuilders;

    @Subscribe("ticketSearch")
    public void onTicketSearch(final Action.ActionPerformedEvent event) {
        if (airportFromSelector == null && airportToSelector == null && takeOffDateSelector == null) {
            flightsDc.setItems(Collections.emptyList());
            showNotification();
        } else {
            getFlightsByBackgroundWorker();
        }
    }

    @Install(to = "flightsTableDl", target = Target.DATA_LOADER)
    private List<Flight> flightsTableDlLoadDelegate(final LoadContext<Flight> loadContext) {
        if (airportFromSelector.getValue() != null || airportToSelector.getValue() != null || takeOffDateSelector.getValue() != null) {
            getFlightsByBackgroundWorker();
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

    private void getFlightsByBackgroundWorker() {
        BackgroundTask<Integer, List<Flight>> getFlightsTask = new FlightsTask(airportFromSelector, airportToSelector, takeOffDateSelector);
        BackgroundTaskHandler<List<Flight>> handle = backgroundWorker.handle(getFlightsTask);
        dialogs.createBackgroundWorkDialog(this, getFlightsTask)
                .withCaption("Getting Flight Tasks")
                .withMessage("Please wait until the Flight Tasks are received")
                .withTotal(1)
                .withShowProgressInPercentage(true)
                .withCancelAllowed(true)
                .show();
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
            flightsDc.setItems(result);
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

    // Simple way
    private List<Flight> getFlights_(EntityComboBox<Airport> airportFromSelector, EntityComboBox<Airport> airportToSelector, DateField<LocalDate> takeOffDateSelector) {
        return ticketService.searchFlights(airportFromSelector.getValue(), airportToSelector.getValue(), takeOffDateSelector.getValue());
    }

    @Install(to = "ticketReservationTable.actions", subject = "columnGenerator")
    private Component ticketReservationTableActionsColumnGenerator(final Flight flight) {
        LinkButton link = uiComponents.create(LinkButton.class);
        link.setCaption("Reserve");
        link.addClickListener(e -> {
            confirmReserve(flight);
        });
        return link;
    }

    private void confirmReserve(Flight flight) {
        dialogs.createInputDialog(this)
                .withCaption("Reserve flight")
                .withParameters(
                        InputParameter.stringParameter("passengerName").withCaption("Passenger name").withRequired(true),
                        InputParameter.stringParameter("passportNumber").withCaption("Passport number").withRequired(true),
                        InputParameter.stringParameter("telephone").withCaption("Telephone").withRequired(true))
                .withActions(DialogActions.OK_CANCEL)
                .withCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(DialogOutcome.OK)) {
                        String passengerName = closeEvent.getValue("passengerName");
                        String passportNumber = closeEvent.getValue("passportNumber");
                        String telephone = closeEvent.getValue("telephone");
                        Ticket ticket = dataManager.create(Ticket.class);
                        ticket.setPassengerName(passengerName);
                        ticket.setPassportNumber(passportNumber);
                        ticket.setTelephone(telephone);
                        ticket.setFlight(flight);
                        ticketService.saveTicket(ticket);

                        screenBuilders.lookup(Ticket.class, this)
                                .withOpenMode(NEW_TAB)
                                .build()
                                .show();
                    }
                })
                .show();
    }
}