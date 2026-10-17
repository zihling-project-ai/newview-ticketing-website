-- Local recovery shopping cart and durable idempotent simulation receipts.
ALTER TABLE cartact ADD CONSTRAINT uq_member_cart UNIQUE(userID,actID);
CREATE TABLE local_checkout_receipt (
 requestID varchar(36) PRIMARY KEY,
 userID integer NOT NULL,
 orderIDs text NOT NULL,
 createdAt datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 CONSTRAINT fk_local_receipt_user FOREIGN KEY(userID) REFERENCES user(userID)
) ENGINE=InnoDB;
CREATE TABLE local_simulation_order (
 orderID integer PRIMARY KEY,
 requestID varchar(36) NOT NULL,
 CONSTRAINT fk_local_sim_order FOREIGN KEY(orderID) REFERENCES orders(orderID),
 CONSTRAINT fk_local_sim_receipt FOREIGN KEY(requestID) REFERENCES local_checkout_receipt(requestID)
) ENGINE=InnoDB;
