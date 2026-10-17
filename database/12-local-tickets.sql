ALTER TABLE orderlist ADD localTicketToken varchar(43) NULL,
 ADD localRedeemedAt datetime(6) NULL,
 ADD CONSTRAINT uq_local_ticket_token UNIQUE(localTicketToken);
