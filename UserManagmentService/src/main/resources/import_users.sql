INSERT INTO public.role (id, name_role) VALUES (1, 'Staff');
INSERT INTO public.role (id, name_role) VALUES (2, 'Manager');
INSERT INTO public.role (id, name_role) VALUES (3, 'Fleet Manager');


INSERT INTO public.generic_user (id, name, surname, email, phone, address, city, keycloak_id) VALUES (1, 'Luigi', 'Caliò', 'luigi@calio.com', '123 456 6789', '', '', 'ad201b03-8285-4257-af66-1309cd2ce95a');
INSERT INTO public.generic_user (id, name, surname, email, phone, address, city, keycloak_id) VALUES (2, 'Laura', 'Verdi', 'laura@verdi.com', '456 123 9789', '', '', '1697f321-d979-4f3a-9792-dddde724c884');
INSERT INTO public.generic_user (id, name, surname, email, phone, address, city, keycloak_id) VALUES (3, 'Staff', 'Staff', 'staff@staff.com', '565 555 5555', '', '', '5158cf1c-7741-4c09-8fbb-90c51b78eb6b');
INSERT INTO public.generic_user (id, name, surname, email, phone, address, city, keycloak_id) VALUES (4, 'Fleet', 'Manager', 'fleet@manager.com', '666 666 6666', '', '', '10b99f20-8284-407c-a994-a5f1ef699403');


INSERT INTO public.employee (generic_user_data_id, role_id, salary) VALUES (1, 2, 1000);
INSERT INTO public.employee (generic_user_data_id, role_id, salary) VALUES (3, 1, 1000);
INSERT INTO public.employee (generic_user_data_id, role_id, salary) VALUES (4, 3, 1000);

INSERT INTO public.customer (generic_user_data_id, date_of_birth, reliability_scores, driving_license, expiration_date) VALUES (2, '1980-01-01', 5, 'ASDFGHJKL', '2080-01-01');
