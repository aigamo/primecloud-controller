CREATE TABLE PLATFORM_VCLOUD (
    PLATFORM_NO BIGINT NOT NULL,
    URL VARCHAR(500) NOT NULL,
    ORG_NAME VARCHAR(200) NOT NULL,
    VDC_NAME VARCHAR(200) NOT NULL,
    SECURE BOOLEAN NOT NULL,
    TIMEOUT int(10) NOT NULL,
    DEF_NETWORK VARCHAR(500),
    CONSTRAINT PLATFORM_VCLOUD_PK PRIMARY KEY (PLATFORM_NO)
);