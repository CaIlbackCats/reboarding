INSERT INTO employee(id,in_office,vip)
VALUES (0,false,true), (1, false, true), (2, false, true), (3, false, true), (4, false, false);

INSERT INTO reservation (id,r_date,reservation_type,office_options_id)
VALUES (0,CURRENT_DATE,'RESERVED',(SELECT id FROM office_capacity WHERE CURRENT_DATE >= start_date AND CURRENT_DATE <=end_date));
INSERT INTO reservation(id,r_date,reservation_type,office_options_id)
VALUES (1,CURRENT_DATE,'QUEUED',(SELECT id FROM office_capacity WHERE CURRENT_DATE >= start_date AND CURRENT_DATE <=end_date));
INSERT INTO reservation (id,r_date,office_options_id)
VALUES (2,CURRENT_DATE+1,(SELECT id FROM office_capacity WHERE CURRENT_DATE+1 >= start_date AND CURRENT_DATE+1 <=end_date));
INSERT INTO reservation (id,r_date,reservation_type,office_options_id)
VALUES (3,'2020-06-02','RESERVED',(SELECT id FROM office_capacity WHERE '2020-06-02' >= start_date AND '2020-06-02' <=end_date));

INSERT INTO employee_reservation (reservation_id,employee_id,permission_to_office,work_station_id)
VALUES ((SELECT id FROM reservation WHERE r_date = CURRENT_DATE AND reservation_type = 'RESERVED'),0,true,4);
INSERT INTO employee_reservation(reservation_id,employee_id,permission_to_office)
VALUES (1,4,true);
INSERT INTO employee_reservation(reservation_id,employee_id,permission_to_office)
VALUES (1,3,true);
INSERT INTO employee_reservation(reservation_id,employee_id,permission_to_office,work_station_id)
VALUES (0,2,true,1);
INSERT INTO employee_reservation(reservation_id,employee_id,permission_to_office)
VALUES (1,3,false);