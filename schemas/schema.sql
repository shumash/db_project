CREATE TABLE patches(id int PRIMARY KEY, img bytea);
CREATE TABLE images (id int PRIMARY KEY, imgname text, img bytea);
CREATE TABLE patch_pointers(from_image int REFERENCES images(id), patch_id int REFERENCES patches(id), x int, y int);
--CREATE TABLE patch_hashes(patch_id int PRIMARY KEY REFERENCES patches(id),
--       hash0 int, hash1 int, hash2 int, hash3 int, hash4 int, hash5 int,
--       hash6 int, hash7 int, hash8 int, hash9 int);
CREATE TABLE patch_hashes(patch_id int PRIMARY KEY REFERENCES patches(id), hash int);
CREATE INDEX patch_hash ON patch_hashes(hash);