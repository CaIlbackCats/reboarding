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

INSERT INTO office_capacity(id,capacity_limit,start_date,end_date)
VALUES (0,250,'2020-06-01','2020-06-30');

INSERT INTO office_capacity(id,capacity_limit,start_date,end_date)
VALUES (1,0,'2020-07-01','2020-07-31');