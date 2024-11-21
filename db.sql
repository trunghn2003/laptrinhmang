CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       score INT,
                       status TINYINT(1)
);


CREATE TABLE matches (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         player1_id INT NOT NULL,
                         player2_id INT NOT NULL,
                         player1_score INT DEFAULT 0,
                         player2_score INT DEFAULT 0,
                         result ENUM('WIN', 'LOSE', 'DRAW') NOT NULL,
                         start_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                         end_time DATETIME,
                         FOREIGN KEY (player1_id) REFERENCES users(id),
                         FOREIGN KEY (player2_id) REFERENCES users(id)
);


ALTER TABLE matches MODIFY COLUMN result VARCHAR(100);

ALTER TABLE users
ADD COLUMN wins INT DEFAULT 0,
ADD COLUMN losses INT DEFAULT 0,
ADD COLUMN draws INT DEFAULT 0;