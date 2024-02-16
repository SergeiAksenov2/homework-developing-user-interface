package com.sample.airtickets.screen.flight;

import io.jmix.ui.screen.*;
import com.sample.airtickets.entity.Flight;

@UiController("Flight.browse")
@UiDescriptor("flight-browse.xml")
@LookupComponent("flightsTable")
public class FlightBrowse extends StandardLookup<Flight> {
}