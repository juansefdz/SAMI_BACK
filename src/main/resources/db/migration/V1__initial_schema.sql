-- Esquema inicial de SAMI (MySQL 8). Refleja las entidades JPA de com.sami.app.

CREATE TABLE accounts (
    id          CHAR(36)     NOT NULL,
    email       VARCHAR(100) NOT NULL,
    password    VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    enabled     BIT(1)       NOT NULL DEFAULT b'1',
    guest       BIT(1)       NOT NULL DEFAULT b'0',
    created_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_accounts_email UNIQUE (email)
);

CREATE TABLE users (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    first_name  VARCHAR(50) NOT NULL,
    last_name   VARCHAR(50) NOT NULL,
    phone       VARCHAR(20) NOT NULL,
    rh_type     VARCHAR(15),
    account_id  CHAR(36)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_account UNIQUE (account_id),
    CONSTRAINT fk_users_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);

CREATE TABLE hospitals (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    name              VARCHAR(100) NOT NULL,
    address           VARCHAR(200) NOT NULL,
    latitude          DOUBLE       NOT NULL,
    longitude         DOUBLE       NOT NULL,
    complexity_level  VARCHAR(15)  NOT NULL,
    specialty         VARCHAR(150) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE ambulances (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    plate             VARCHAR(10) NOT NULL,
    type              VARCHAR(15) NOT NULL,
    status            VARCHAR(20) NOT NULL,
    latitude          DOUBLE      NOT NULL,
    longitude         DOUBLE      NOT NULL,
    last_location_at  DATETIME(6),
    driver_id         BIGINT,
    emt_id            BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_ambulances_plate UNIQUE (plate),
    CONSTRAINT fk_ambulances_driver FOREIGN KEY (driver_id) REFERENCES users (id),
    CONSTRAINT fk_ambulances_emt FOREIGN KEY (emt_id) REFERENCES users (id)
);

CREATE TABLE emergencies (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    latitude        DOUBLE        NOT NULL,
    longitude       DOUBLE        NOT NULL,
    address         VARCHAR(200),
    description     VARCHAR(500)  NOT NULL,
    patients_count  INT           NOT NULL,
    severity        INT           NOT NULL,
    anamnesis       VARCHAR(4000),
    status          VARCHAR(20)   NOT NULL,
    client_id       BIGINT        NOT NULL,
    ambulance_id    BIGINT,
    hospital_id     BIGINT,
    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_emergencies_client FOREIGN KEY (client_id) REFERENCES users (id),
    CONSTRAINT fk_emergencies_ambulance FOREIGN KEY (ambulance_id) REFERENCES ambulances (id),
    CONSTRAINT fk_emergencies_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id)
);

CREATE INDEX idx_emergency_status ON emergencies (status);
CREATE INDEX idx_emergency_ambulance_status ON emergencies (ambulance_id, status);
