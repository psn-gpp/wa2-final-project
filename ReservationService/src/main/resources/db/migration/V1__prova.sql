CREATE SEQUENCE IF NOT EXISTS availability_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS car_model_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS category_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS drivetrain_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS engine_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS infotainment_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS maintenance_history_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS note_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS safety_features_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS transmission_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS vehicle_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE availability
(
    id   BIGINT NOT NULL,
    type VARCHAR(255),
    CONSTRAINT pk_availability PRIMARY KEY (id)
);

CREATE TABLE car_model
(
    id                 BIGINT           NOT NULL,
    brand              VARCHAR(255),
    model              VARCHAR(255),
    model_year         INTEGER          NOT NULL,
    segment            VARCHAR(255),
    doors_no           INTEGER          NOT NULL,
    seating_capacity   INTEGER          NOT NULL,
    luggage_capacity   FLOAT            NOT NULL,
    manufacturer       VARCHAR(255),
    cost_per_day       DOUBLE PRECISION NOT NULL,
    motor_displacement FLOAT            NOT NULL,
    air_conditioning   BOOLEAN          NOT NULL,
    ref_category       BIGINT,
    ref_engine         BIGINT,
    ref_transmission   BIGINT,
    ref_drivetrain     BIGINT,
    CONSTRAINT pk_carmodel PRIMARY KEY (id)
);

CREATE TABLE car_model_infotainments
(
    car_model_id    BIGINT NOT NULL,
    infotainment_id BIGINT NOT NULL,
    CONSTRAINT pk_car_model_infotainments PRIMARY KEY (car_model_id, infotainment_id)
);

CREATE TABLE car_model_safety_features
(
    car_model_id      BIGINT NOT NULL,
    safety_feature_id BIGINT NOT NULL,
    CONSTRAINT pk_car_model_safety_features PRIMARY KEY (car_model_id, safety_feature_id)
);

CREATE TABLE category
(
    id       BIGINT NOT NULL,
    category VARCHAR(255),
    CONSTRAINT pk_category PRIMARY KEY (id)
);

CREATE TABLE drivetrain
(
    id   BIGINT NOT NULL,
    type VARCHAR(255),
    CONSTRAINT pk_drivetrain PRIMARY KEY (id)
);

CREATE TABLE engine
(
    id   BIGINT NOT NULL,
    type VARCHAR(255),
    CONSTRAINT pk_engine PRIMARY KEY (id)
);

CREATE TABLE infotainment
(
    id   BIGINT NOT NULL,
    type VARCHAR(255),
    CONSTRAINT pk_infotainment PRIMARY KEY (id)
);

CREATE TABLE maintenance_history
(
    id                    BIGINT  NOT NULL,
    ref_vehicle           BIGINT,
    defect                VARCHAR(255),
    completed_maintenance BOOLEAN NOT NULL,
    date                  date,
    CONSTRAINT pk_maintenancehistory PRIMARY KEY (id)
);

CREATE TABLE note
(
    id          BIGINT NOT NULL,
    ref_vehicle BIGINT,
    text        VARCHAR(255),
    author      VARCHAR(255),
    date        date,
    CONSTRAINT pk_note PRIMARY KEY (id)
);

CREATE TABLE safety_features
(
    id      BIGINT NOT NULL,
    feature VARCHAR(255),
    CONSTRAINT pk_safetyfeatures PRIMARY KEY (id)
);

CREATE TABLE transmission
(
    id   BIGINT NOT NULL,
    type VARCHAR(255),
    CONSTRAINT pk_transmission PRIMARY KEY (id)
);

CREATE TABLE vehicle
(
    id                  BIGINT  NOT NULL,
    ref_car_model       BIGINT,
    ref_availability    BIGINT,
    licence_plate       VARCHAR(255),
    vin                 VARCHAR(255),
    kilometers          FLOAT   NOT NULL,
    pending_cleaning    BOOLEAN NOT NULL,
    pending_maintenance BOOLEAN NOT NULL,
    CONSTRAINT pk_vehicle PRIMARY KEY (id)
);

ALTER TABLE car_model
    ADD CONSTRAINT FK_CARMODEL_ON_REF_CATEGORY FOREIGN KEY (ref_category) REFERENCES category (id);

ALTER TABLE car_model
    ADD CONSTRAINT FK_CARMODEL_ON_REF_DRIVETRAIN FOREIGN KEY (ref_drivetrain) REFERENCES drivetrain (id);

ALTER TABLE car_model
    ADD CONSTRAINT FK_CARMODEL_ON_REF_ENGINE FOREIGN KEY (ref_engine) REFERENCES engine (id);

ALTER TABLE car_model
    ADD CONSTRAINT FK_CARMODEL_ON_REF_TRANSMISSION FOREIGN KEY (ref_transmission) REFERENCES transmission (id);

ALTER TABLE note
    ADD CONSTRAINT FK_NOTE_ON_REF_VEHICLE FOREIGN KEY (ref_vehicle) REFERENCES vehicle (id);

ALTER TABLE vehicle
    ADD CONSTRAINT FK_VEHICLE_CAR_MODEL FOREIGN KEY (ref_car_model) REFERENCES car_model (id);

ALTER TABLE maintenance_history
    ADD CONSTRAINT FK_VEHICLE_MAINTENANCE FOREIGN KEY (ref_vehicle) REFERENCES vehicle (id);

ALTER TABLE vehicle
    ADD CONSTRAINT FK_VEHICLE_ON_REF_AVAILABILITY FOREIGN KEY (ref_availability) REFERENCES availability (id);

ALTER TABLE car_model_infotainments
    ADD CONSTRAINT fk_carmodinf_on_car_model FOREIGN KEY (car_model_id) REFERENCES car_model (id);

ALTER TABLE car_model_infotainments
    ADD CONSTRAINT fk_carmodinf_on_infotainment FOREIGN KEY (infotainment_id) REFERENCES infotainment (id);

ALTER TABLE car_model_safety_features
    ADD CONSTRAINT fk_carmodsaffea_on_car_model FOREIGN KEY (car_model_id) REFERENCES car_model (id);

ALTER TABLE car_model_safety_features
    ADD CONSTRAINT fk_carmodsaffea_on_safety_features FOREIGN KEY (safety_feature_id) REFERENCES safety_features (id);