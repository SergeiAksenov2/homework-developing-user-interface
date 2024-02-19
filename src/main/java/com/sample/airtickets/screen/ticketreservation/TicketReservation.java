package com.sample.airtickets.screen.ticketreservation;

import com.sample.airtickets.app.TicketService;
import com.sample.airtickets.entity.Airport;
import com.sample.airtickets.entity.Flight;
import io.jmix.core.LoadContext;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.DateField;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

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

    @Subscribe("ticketSearch")
    public void onTicketSearch(final Action.ActionPerformedEvent event) {
        if (airportFromSelector.getValue() == null && airportToSelector.getValue() == null && takeOffDateSelector.getValue() == null) {
            flightsDc.setItems(Collections.emptyList());
            showNotification();
        } else {
        List<Flight> flights = ticketService.searchFlights(airportFromSelector.getValue(), airportToSelector.getValue(), takeOffDateSelector.getValue());
        flightsDc.setItems(flights);
        }
    }

    @Install(to = "flightsTableDl", target = Target.DATA_LOADER)
    private List<Flight> flightsTableDlLoadDelegate(final LoadContext<Flight> loadContext) {
        if (airportFromSelector.getValue() != null || airportToSelector.getValue() != null || takeOffDateSelector.getValue() != null) {
            return ticketService.searchFlights(airportFromSelector.getValue(), airportToSelector.getValue(), takeOffDateSelector.getValue());
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
}