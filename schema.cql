CREATE KEYSPACE IF NOT EXISTS kaskaclub
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 4 };

USE klubukasi;

CREATE TABLE tickets (
  concert varchar,
  type int,
  count counter,
  PRIMARY KEY (concert, type)
);
