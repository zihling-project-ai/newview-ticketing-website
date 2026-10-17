-- Assigned seating recovery; old tinyint could not store the original row/column strings.
ALTER TABLE orderlist MODIFY seatRowsColumns varchar(32);
CREATE TABLE local_seat_layout (
 actID integer NOT NULL, seatCode varchar(16) NOT NULL,
 PRIMARY KEY(actID,seatCode), FOREIGN KEY(actID) REFERENCES act(actID)
) ENGINE=InnoDB;
CREATE TABLE local_cart_seat (
 userID integer NOT NULL, actID integer NOT NULL, seatCode varchar(16) NOT NULL, holdToken varchar(36) NOT NULL,
 PRIMARY KEY(userID,actID,seatCode),
 FOREIGN KEY(userID) REFERENCES user(userID), FOREIGN KEY(actID,seatCode) REFERENCES local_seat_layout(actID,seatCode)
) ENGINE=InnoDB;
CREATE TABLE local_seat_booking (
 actID integer NOT NULL, seatCode varchar(16) NOT NULL, orderListID integer NOT NULL UNIQUE,
 PRIMARY KEY(actID,seatCode),
 FOREIGN KEY(actID,seatCode) REFERENCES local_seat_layout(actID,seatCode),
 FOREIGN KEY(orderListID) REFERENCES orderlist(orderListID)
) ENGINE=InnoDB;
-- New synthetic 4x6 layout, not a recovered historical floor plan.
INSERT INTO local_seat_layout(actID,seatCode)
SELECT a.actID,CONCAT(r.r,LPAD(c.c,2,'0')) FROM act a
CROSS JOIN (SELECT 'A' r UNION ALL SELECT 'B' UNION ALL SELECT 'C' UNION ALL SELECT 'D') r
CROSS JOIN (SELECT 1 c UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6) c
WHERE a.actName='本機合成展示｜週末音樂會';
