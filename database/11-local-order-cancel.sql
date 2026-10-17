ALTER TABLE local_simulation_order ADD cancelledAt datetime(6) NULL;
-- Remember the exact original hold so cancellation cleanup cannot remove a newer hold.
ALTER TABLE local_seat_booking ADD holdToken varchar(36) NULL;
