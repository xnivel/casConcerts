DROP KEYSPACE kaskaclub;

CREATE KEYSPACE IF NOT EXISTS kaskaclub
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 4 };

USE kaskaclub;

CREATE TABLE tickets (
  concert varchar,
  type int,
  maxTickets int,
  count int,
  PRIMARY KEY (concert, type, maxTickets)
);
CREATE TABLE ticketsOld (
  concert varchar,
  type int,
  maxTickets int,
  count counter,
  PRIMARY KEY (concert, type, maxTickets)
);
CREATE TABLE ticketsboughtby (
  concert varchar,
  type int,
  name varchar,
  count int,
  timestamp bigint,
  PRIMARY KEY (concert, type,name)
);