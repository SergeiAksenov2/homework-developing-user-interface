package com.sample.airtickets.screen.flight;

import io.jmix.ui.screen.*;
import com.sample.airtickets.entity.Flight;

@UiController("Flight.edit")
@UiDescriptor("flight-edit.xml")
@EditedEntityContainer("flightDc")
@DialogMode(width = "AUTO", height = "AUTO", forceDialog = true)
public class FlightEdit extends StandardEditor<Flight> {
}