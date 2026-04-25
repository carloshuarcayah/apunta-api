ALTER TABLE `categories`
    ADD COLUMN `system` bit(1) NOT NULL DEFAULT b'0';

UPDATE `categories` c
JOIN (
    SELECT MIN(c2.`id`) AS id
    FROM `categories` c2
    JOIN `users` u ON u.`id` = c2.`user_id`
    WHERE u.`email_verified` = b'1'
      AND c2.`active` = b'1'
      AND LOWER(TRIM(c2.`name`)) IN ('sin categoria', 'sin categoría')
    GROUP BY c2.`user_id`
) pick ON pick.id = c.`id`
SET c.`name` = 'Sin categoria',
    c.`system` = b'1';

INSERT INTO `categories` (`active`, `created_at`, `updated_at`, `description`, `name`, `user_id`, `system`)
SELECT b'1', NOW(6), NOW(6), NULL, 'Sin categoria', u.`id`, b'1'
FROM `users` u
WHERE u.`email_verified` = b'1'
  AND NOT EXISTS (
      SELECT 1 FROM `categories` c
      WHERE c.`user_id` = u.`id`
        AND c.`system` = b'1'
  );