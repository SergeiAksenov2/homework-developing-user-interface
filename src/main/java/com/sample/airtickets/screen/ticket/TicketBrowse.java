package com.sample.airtickets.screen.ticket;

import io.jmix.ui.screen.*;
import com.sample.airtickets.entity.Ticket;

@UiController("Ticket.browse")
@UiDescriptor("ticket-browse.xml")
@LookupComponent("ticketsTable")
public class TicketBrowse extends StandardLookup<Ticket> {
}