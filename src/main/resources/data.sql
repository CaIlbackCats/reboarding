INSERT INTO reservation (r_date)
VALUES (CURRENT_DATE);
INSERT INTO employee (id,in_office)
VALUES ('0',false);
INSERT INTO employee_reservation (reservation_id,employee_id)
VALUES ((SELECT id FROM reservation WHERE r_date = CURRENT_DATE),'0');

INSERT INTO reservation (r_date)
VALUES (CURRENT_DATE+1);
INSERT INTO employee (id,in_office)
VALUES ('1',false);
INSERT INTO employee_reservation (reservation_id,employee_id)
VALUES ((SELECT id FROM reservation WHERE r_date = CURRENT_DATE+1),'1');