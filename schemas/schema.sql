CREATE TABLE patches(id int PRIMARY KEY, patch bytea);
CREATE TABLE images (imgname text PRIMARY KEY, img bytea);
CREATE TABLE patch_pointers(from_image text REFERENCES images(imgname), patch_id int REFERENCES patches(id), x int, y int);
