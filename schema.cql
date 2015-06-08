DROP KEYSPACE kaskaclub;

CREATE KEYSPACE IF NOT EXISTS kaskaclub
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 4 };

USE kaskaclub;

CREATE TABLE tickets (
  concert varchar,
  type int,
  count counter,
  maxTickets int,
  PRIMARY KEY (concert, type)
);
