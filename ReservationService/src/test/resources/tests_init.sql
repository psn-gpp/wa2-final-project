DELETE FROM note;
DELETE FROM maintenance_history;
DELETE FROM vehicle;

DELETE FROM car_model_safety_features;
DELETE FROM car_model_infotainments;
DELETE FROM car_model;

--- insert new car model ---
INSERT INTO car_model
    (id, brand, model, model_year, segment, doors_no, seating_capacity, luggage_capacity, manufacturer, cost_per_day, motor_displacement, air_conditioning, ref_category, ref_engine, ref_transmission, ref_drivetrain)
VALUES
    (1, 'Ferrari', 'LaFerrari',2020, 'Luxury', 3, 2, 200, 'Ferrari', 2000, 2000, true, 1, 1, 1, 1 ) ON CONFLICT (id) DO NOTHING ;

INSERT INTO car_model_infotainments (car_model_id, infotainment_id) VALUES (1,1)  ON CONFLICT (car_model_id, infotainment_id) DO NOTHING ;
INSERT INTO car_model_safety_features (car_model_id, safety_feature_id) VALUES (1,1)  ON CONFLICT (car_model_id, safety_feature_id) DO NOTHING ;

--- insert new vehicle ---
INSERT INTO vehicle
    (id, ref_car_model, ref_availability, licence_plate, vin, kilometers, pending_cleaning, pending_maintenance)
VALUES
    (1, 1, 1, 'AB123CD', '12345678901234567', 10000, false, false)  ON CONFLICT (id) DO NOTHING ;

INSERT INTO maintenance_history
    (id, ref_vehicle, defect, completed_maintenance, date)
VALUES
    (1, 1, 'brake', true, '2023-10-01')  ON CONFLICT (id) DO NOTHING;


