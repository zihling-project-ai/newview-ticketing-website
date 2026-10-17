-- Active redemption link. Cancel deletes this link atomically; orders retain discount history.
CREATE TABLE local_coupon_redemption (
 orderID int NOT NULL PRIMARY KEY,
 useDisID int NOT NULL UNIQUE
) ENGINE=InnoDB;
