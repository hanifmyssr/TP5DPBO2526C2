-- Database untuk TP5 DPBO (Data Anggota Polri, data fiktif)
CREATE DATABASE IF NOT EXISTS db_people;
USE db_people;

DROP TABLE IF EXISTS person;

CREATE TABLE person (
    id          VARCHAR(20)  NOT NULL PRIMARY KEY,
    nama        VARCHAR(100) NOT NULL,
    tahun_lahir INT          NOT NULL,
    kategori    VARCHAR(50)  NOT NULL,  -- satuan Polri
    pangkat     VARCHAR(30)  NOT NULL   -- atribut baru
);

INSERT INTO person (id, nama, tahun_lahir, kategori, pangkat) VALUES
('AGT-001', 'Budi Santoso', 1990, 'Bareskrim',    'Iptu'),
('AGT-002', 'Siti Rahma',   1995, 'Korlantas',    'Bripda'),
('AGT-003', 'Andi Wijaya',  1988, 'Densus 88 AT', 'Aiptu'),
('AGT-004', 'Dewi Lestari', 1992, 'Div Propam',   'AKP'),
('AGT-005', 'Rudi Hartono', 1980, 'Korbrimob',    'Kompol');
