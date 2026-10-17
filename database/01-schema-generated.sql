
    create table act (
       actID integer not null auto_increment,
        actContent text,
        actDate date,
        actIntroduce varchar(255),
        actName varchar(255),
        actPrice integer,
        actScope integer,
        actTime date,
        approvalCondition TINYINT,
        cityAddress varchar(255),
        time time,
        actCategoryID integer,
        cityAddressID integer,
        pubID integer,
        primary key (actID)
    ) engine=InnoDB;

    create table actCategory (
       actCategoryID integer not null auto_increment,
        actCategoryName varchar(255),
        primary key (actCategoryID)
    ) engine=InnoDB;

    create table actpic (
       actPicID integer not null auto_increment,
        actPic LONGBLOB,
        actID integer,
        primary key (actPicID)
    ) engine=InnoDB;

    create table administrator (
       adminID integer not null auto_increment,
        adminAccount varchar(255),
        adminEmail varchar(255),
        adminName varchar(255),
        adminPassword varchar(255),
        primary key (adminID)
    ) engine=InnoDB;

    create table cartact (
       cartActID integer not null auto_increment,
        cartQuantity integer,
        actID integer,
        userID integer,
        primary key (cartActID)
    ) engine=InnoDB;

    create table city (
       cityAddressID integer not null auto_increment,
        cityName varchar(255),
        primary key (cityAddressID)
    ) engine=InnoDB;

    create table compic (
       comPicID integer not null auto_increment,
        comPic longblob,
        orderListID integer,
        primary key (comPicID)
    ) engine=InnoDB;

    create table discount (
       discountNO integer not null auto_increment,
        disAmount integer,
        disFinishDate datetime(6),
        disStartDate datetime(6),
        discountCode varchar(255),
        discountContent varchar(255),
        adminID integer,
        pubID integer,
        primary key (discountNO)
    ) engine=InnoDB;

    create table likes (
       likeID integer not null auto_increment,
        likeOrNot TINYINT,
        visitorID integer,
        postID integer,
        userID integer,
        primary key (likeID)
    ) engine=InnoDB;

    create table myLike (
       myLikeID integer not null auto_increment,
        actID integer,
        userID integer,
        primary key (myLikeID)
    ) engine=InnoDB;

    create table orderlist (
       orderListID integer not null auto_increment,
        OrderListTime datetime(6),
        QRcodeID longblob,
        actTotal integer,
        fiveStarReview tinyint,
        reviewContent varchar(255),
        seatRowsColumns tinyint,
        usedType tinyint,
        orderID integer,
        actID integer,
        primary key (orderListID)
    ) engine=InnoDB;

    create table orders (
       orderID integer not null auto_increment,
        actQuantity integer,
        discount integer,
        discountPrice integer,
        ordTime datetime(6),
        ordTotal integer,
        ordType tinyint,
        userID integer,
        discountNO integer,
        pubID integer,
        primary key (orderID)
    ) engine=InnoDB;

    create table post (
       postID integer not null auto_increment,
        disLikeCount integer,
        lastEditedTime datetime(6),
        likeCount integer,
        postContent varchar(255),
        postDateTime datetime(6),
        postHeader varchar(255),
        postStatus tinyint,
        postCategoryID integer,
        userID integer,
        primary key (postID)
    ) engine=InnoDB;

    create table postcategory (
       postCategoryID integer not null auto_increment,
        postCategoryName varchar(255),
        primary key (postCategoryID)
    ) engine=InnoDB;

    create table postmessage (
       postMessageID integer not null auto_increment,
        mesContent varchar(255),
        messageDate datetime(6),
        postID integer,
        userID integer,
        primary key (postMessageID)
    ) engine=InnoDB;

    create table postpic (
       postPicID integer not null auto_increment,
        postpic longblob,
        postID integer,
        primary key (postPicID)
    ) engine=InnoDB;

    create table Publisher (
       pubID integer not null auto_increment,
        pubEmail varchar(255),
        pubName varchar(255),
        primary key (pubID)
    ) engine=InnoDB;

    create table pubUser (
       pubUserID integer not null auto_increment,
        pubAccount varchar(255),
        pubAuthority tinyint,
        pubNickname varchar(255),
        pubPassword varchar(255),
        pubID integer,
        primary key (pubUserID)
    ) engine=InnoDB;

    create table report (
       reportID integer not null,
        reportContent varchar(255),
        reportStatus TINYINT,
        postID integer,
        userID integer,
        primary key (reportID)
    ) engine=InnoDB;

    create table useDiscount (
       useDisID integer not null auto_increment,
        ditUsed TINYINT,
        discountNO integer,
        userID integer,
        primary key (useDisID)
    ) engine=InnoDB;

    create table user (
       userID integer not null auto_increment,
        buyAuthority tinyint,
        speakAuthority tinyint,
        userAccount varchar(255),
        userBirth date,
        userCell varchar(255),
        userEmail varchar(255),
        userName varchar(255),
        userNickname varchar(255),
        userPassword varchar(255),
        primary key (userID)
    ) engine=InnoDB;

    alter table act 
       add constraint FKhd1ui1qgnsvw0ekyshjxifo6q 
       foreign key (actCategoryID) 
       references actCategory (actCategoryID);

    alter table act 
       add constraint FK5je0en2o0hcr4juunujfjficp 
       foreign key (cityAddressID) 
       references city (cityAddressID);

    alter table act 
       add constraint FK860fpy4wf21keyqrhq1t6kgxf 
       foreign key (pubID) 
       references Publisher (pubID);

    alter table actpic 
       add constraint FKipmxr5mtrvsyq6mhf0fnyakg3 
       foreign key (actID) 
       references act (actID);

    alter table cartact 
       add constraint FKhrc6w4md2nk0lv9pg0jygn8pb 
       foreign key (actID) 
       references act (actID);

    alter table cartact 
       add constraint FKg3hve6gfuqo68a0pcsjgqyl2j 
       foreign key (userID) 
       references user (userID);

    alter table compic 
       add constraint FKa3jvt9bgcja9wuviiunjwv3ry 
       foreign key (orderListID) 
       references orderlist (orderListID);

    alter table discount 
       add constraint FK1rk6ygw7jteba9ox1iouh4jio 
       foreign key (adminID) 
       references administrator (adminID);

    alter table discount 
       add constraint FKodu0aomuft6oprp5xgpaw6yil 
       foreign key (pubID) 
       references Publisher (pubID);

    alter table likes 
       add constraint FKn6f9rqhxbsh2xfkqx5a5aqjqg 
       foreign key (postID) 
       references post (postID);

    alter table likes 
       add constraint FKra9vb9apsde261ops74ajf77 
       foreign key (userID) 
       references user (userID);

    alter table myLike 
       add constraint FK2qd6aj4ackrfdt23usrlmt7po 
       foreign key (actID) 
       references act (actID);

    alter table myLike 
       add constraint FKcvkabsbhv5xf1r2h2wuxuycxl 
       foreign key (userID) 
       references user (userID);

    alter table orderlist 
       add constraint FKcb6jyy6ksbbra7uc79ej2b62o 
       foreign key (actID) 
       references act (actID);

    alter table orderlist 
       add constraint FKjwvax8tqyc4i4k9vw30f21uq3 
       foreign key (orderID) 
       references orders (orderID);

    alter table orders 
       add constraint FKrc2juxnyw4b6mqfmfcchppm78 
       foreign key (discountNO) 
       references discount (discountNO);

    alter table orders 
       add constraint FK5i3hun091l0ojallfddl0el91 
       foreign key (pubID) 
       references Publisher (pubID);

    alter table orders 
       add constraint FKi5xgym2hb1nukjn11h0pvkhhq 
       foreign key (userID) 
       references user (userID);

    alter table post 
       add constraint FK4cy3nu2i8g9ukhsqe63f9w94x 
       foreign key (postCategoryID) 
       references postcategory (postCategoryID);

    alter table post 
       add constraint FK48v93obu7hsmy5vph8b3h0ht 
       foreign key (userID) 
       references user (userID);

    alter table postmessage 
       add constraint FK78c2ceck9svxxqtcrobk48rsf 
       foreign key (postID) 
       references post (postID);

    alter table postmessage 
       add constraint FKgf3ttp6ot2rgq03eetlw7yiij 
       foreign key (userID) 
       references user (userID);

    alter table postpic 
       add constraint FKmw2ugabfsxr9ccsgemdkx4sau 
       foreign key (postID) 
       references post (postID);

    alter table pubUser 
       add constraint FK72nb3lwyiqp5fcr7xpgm2xr28 
       foreign key (pubID) 
       references Publisher (pubID);

    alter table report 
       add constraint FKbaawii2loowo106o9i2jf9p2p 
       foreign key (postID) 
       references post (postID);

    alter table report 
       add constraint FKol60oputga7t1uiwurhbj0mwd 
       foreign key (userID) 
       references user (userID);

    alter table useDiscount 
       add constraint FK8ivdstpvy7uf8og5jkos1dxde 
       foreign key (discountNO) 
       references discount (discountNO);

    alter table useDiscount 
       add constraint FKj3sbplqopjcka2jxobf7va561 
       foreign key (userID) 
       references user (userID);
