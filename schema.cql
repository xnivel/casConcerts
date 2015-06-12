DROP KEYSPACE casConcerts;

CREATE KEYSPACE IF NOT EXISTS casConcerts
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 4 };

USE casConcerts;

CREATE TABLE tickets (
  concert varchar,
  type int,
  id int,
  owner varchar,
  candidates set<varchar>,
  PRIMARY KEY (concert, type, id)
);

CREATE TABLE freetickets (
  concert varchar,
  type int,
  id int,
  PRIMARY KEY ((concert, type), id)
);
CREATE TABLE ticketsInfo (
  concert varchar,
  type int,
  maxTickets int,
  PRIMARY KEY (concert, type)
);