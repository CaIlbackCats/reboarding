INSERT INTO reservation (r_date,reservation_type)
VALUES (CURRENT_DATE,'RESERVED');
INSERT INTO employee (id,in_office)
VALUES ('0',false);
INSERT INTO employee (id,in_office)
VALUES ('4',false);
INSERT INTO employee (id,in_office)
VALUES ('2',false);
INSERT INTO employee (id,in_office)
VALUES ('3',false);

INSERT INTO employee_reservation (reservation_id,employee_id)
VALUES ((SELECT id FROM reservation WHERE r_date = CURRENT_DATE AND reservation_type = 'RESERVED'),'0');


INSERT INTO reservation (r_date)
VALUES (CURRENT_DATE+1);
INSERT INTO employee (id,in_office)
VALUES ('1',false);
--INSERT INTO employee_reservation (reservation_id,employee_id)
--VALUES ((SELECT id FROM reservation WHERE r_date = CURRENT_DATE+1),'1');

INSERT INTO office_capacity(id,capacity_limit,start_date,end_date)
VALUES (0,250,'2020-06-01','2020-06-30');

INSERT INTO office_capacity(id,capacity_limit,start_date,end_date)
VALUES (1,0,'2020-07-01','2020-07-31');

INSERT INTO reservation (r_date,capacity_id,reservation_type)
VALUES ('2020-06-02',0,'RESERVED');

INSERT INTO employee_reservation (reservation_id,employee_id)
VALUES ((SELECT id FROM reservation WHERE r_date = '2020-06-02'),'4');
INSERT INTO employee_reservation (reservation_id,employee_id)
VALUES ((SELECT id FROM reservation WHERE r_date = '2020-06-02'),'2');
INSERT INTO employee_reservation (reservation_id,employee_id)
VALUES ((SELECT id FROM reservation WHERE r_date = '2020-06-02'),'3');

INSERT INTO reservation(id,r_date,capacity_id,reservation_type)
VALUES (10,CURRENT_DATE,1,'QUEUED');

INSERT INTO employee_reservation(reservation_id,employee_id)
VALUES (10,0);
INSERT INTO employee_reservation(reservation_id,employee_id)
VALUES (10,2);
INSERT INTO employee_reservation(reservation_id,employee_id)
VALUES (10,3);