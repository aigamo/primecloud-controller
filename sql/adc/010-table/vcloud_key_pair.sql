CREATE TABLE VCLOUD_KEY_PAIR (
    KEY_NO BIGINT NOT NULL AUTO_INCREMENT,
    USER_NO BIGINT NOT NULL,
    PLATFORM_NO BIGINT NOT NULL,
    KEY_NAME VARCHAR(100) NOT NULL,
    KEY_PUBLIC VARCHAR(1000),
    CONSTRAINT VCLOUD_KEY_PAIR_PK PRIMARY KEY(KEY_NO)
);