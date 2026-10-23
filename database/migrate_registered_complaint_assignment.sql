-- The entity maps this table as `registered_complaint` (singular).
-- Only admin_id changes nullability; all other required columns remain constrained.
ALTER TABLE registered_complaint
  MODIFY COLUMN admin_id INT NULL;
