DROP TABLE IF EXISTS reservation;
 
CREATE TABLE reservation (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id VARCHAR NOT NULL
);

INSERT INTO reservation (user_id) VALUES ( 0 )