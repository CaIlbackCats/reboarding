INSERT INTO office_capacity(id,capacity_limit,start_date,end_date,employee_min_distance)
VALUES (0,0,'2020-05-01','2020-05-15',50);

INSERT INTO office_capacity(id,capacity_limit,start_date,end_date,employee_min_distance)
VALUES (3,50,'2020-05-16','2020-05-31',50);

INSERT INTO office_capacity(id,capacity_limit,start_date,end_date,employee_min_distance)
VALUES (1,3,'2020-06-01','2020-06-30',10);

INSERT INTO office_capacity(id,capacity_limit,start_date,end_date,employee_min_distance)
VALUES (2,0,'2020-07-01','2020-07-31',50);

INSERT INTO reservation (id,r_date,reservation_type,office_options_id)
VALUES (0,CURRENT_DATE,'RESERVED',(SELECT id FROM office_capacity WHERE CURRENT_DATE >= start_date AND CURRENT_DATE <=end_date));
INSERT INTO reservation(id,r_date,reservation_type,office_options_id)
VALUES (1,CURRENT_DATE,'QUEUED',(SELECT id FROM office_capacity WHERE CURRENT_DATE >= start_date AND CURRENT_DATE <=end_date));
INSERT INTO reservation (id,r_date,office_options_id)
VALUES (2,CURRENT_DATE+1,(SELECT id FROM office_capacity WHERE CURRENT_DATE+1 >= start_date AND CURRENT_DATE+1 <=end_date));
INSERT INTO reservation (id,r_date,reservation_type,office_options_id)
VALUES (3,'2020-06-02','RESERVED',(SELECT id FROM office_capacity WHERE '2020-06-02' >= start_date AND '2020-06-02' <=end_date));

INSERT INTO office_workstation(id,office_options_id,workstation_id)
VALUES(0,1,1);
INSERT INTO office_workstation(id,office_options_id,workstation_id)
VALUES(1,1,2);
INSERT INTO office_workstation(id,office_options_id,workstation_id)
VALUES(2,1,3);

INSERT INTO employee_reservation (reservation_id,employee_id,permission_to_office,work_station_id)
VALUES ((SELECT id FROM reservation WHERE r_date = CURRENT_DATE AND reservation_type = 'RESERVED'),0,true,2);
INSERT INTO employee_reservation(reservation_id,employee_id,permission_to_office)
VALUES (1,4,false);
INSERT INTO employee_reservation(reservation_id,employee_id,permission_to_office,work_station_id)
VALUES (0,2,true,1);
INSERT INTO employee_reservation(reservation_id,employee_id,permission_to_office)
VALUES (1,3,false);

