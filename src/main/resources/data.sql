INSERT INTO office_capacity(id,capacity_limit,start_date,end_date)
VALUES (0,0,'2020-05-01','2020-05-31');

INSERT INTO office_capacity(id,capacity_limit,start_date,end_date)
VALUES (1,250,'2020-06-01','2020-06-30');

INSERT INTO office_capacity(id,capacity_limit,start_date,end_date)
VALUES (2,0,'2020-07-01','2020-07-31');

-- INSERT INTO employee (id,in_office)
-- VALUES ('0',false);
-- INSERT INTO employee (id,in_office)
-- VALUES ('1',false);
-- INSERT INTO employee (id,in_office)
-- VALUES ('2',false);
-- INSERT INTO employee (id,in_office)
-- VALUES ('3',false);
-- INSERT INTO employee (id,in_office)
-- VALUES ('4',false);

INSERT INTO reservation (id,r_date,reservation_type,capacity_id)
VALUES (0,CURRENT_DATE,'RESERVED',(SELECT id FROM office_capacity WHERE CURRENT_DATE >= start_date AND CURRENT_DATE <=end_date));
INSERT INTO reservation(id,r_date,reservation_type,capacity_id)
VALUES (1,CURRENT_DATE,'QUEUED',(SELECT id FROM office_capacity WHERE CURRENT_DATE >= start_date AND CURRENT_DATE <=end_date));
INSERT INTO reservation (id,r_date,capacity_id)
VALUES (2,CURRENT_DATE+1,(SELECT id FROM office_capacity WHERE CURRENT_DATE+1 >= start_date AND CURRENT_DATE+1 <=end_date));
INSERT INTO reservation (id,r_date,reservation_type,capacity_id)
VALUES (3,'2020-06-02','RESERVED',(SELECT id FROM office_capacity WHERE '2020-06-02' >= start_date AND '2020-06-02' <=end_date));


INSERT INTO employee_reservation (reservation_id,employee_id)
VALUES ((SELECT id FROM reservation WHERE r_date = CURRENT_DATE AND reservation_type = 'RESERVED'),'1');
INSERT INTO employee_reservation (reservation_id,employee_id)
VALUES ((SELECT id FROM reservation WHERE r_date = '2020-06-02'),'2');
INSERT INTO employee_reservation (reservation_id,employee_id)
VALUES ((SELECT id FROM reservation WHERE r_date = '2020-06-02'),'3');
INSERT INTO employee_reservation (reservation_id,employee_id)
VALUES ((SELECT id FROM reservation WHERE r_date = '2020-06-02'),'4');
INSERT INTO employee_reservation(reservation_id,employee_id)
VALUES (1,1);
INSERT INTO employee_reservation(reservation_id,employee_id,permission_to_office)
VALUES (1,3,true);
INSERT INTO employee_reservation(reservation_id,employee_id,permission_to_office)
VALUES (1,4,false);



--INSERT INTO employee_reservation (reservation_id,employee_id)
--VALUES ((SELECT id FROM reservation WHERE r_date = CURRENT_DATE+1),'1');





