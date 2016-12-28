SELECT CASE WHEN a.id IS NOT NULL THEN a.id ELSE da.entity_id END AS agreement_id,
       CASE WHEN a.id IS NOT NULL THEN a.id % 100000 ELSE da.entity_id % 100000 END AS agreement,
       CASE WHEN c.id IS NOT NULL THEN c.id ELSE dc.entity_id END AS customer_id,
       CASE WHEN c.id IS NOT NULL THEN c.name ELSE dc.entity_name END AS customer_name,
       SUBSTRING(d.data FROM '"location_street" ?: ?"([^"]*)",?') AS street,
       SUBSTRING(d.data FROM '"location_town" ?: ?"([^"]*)",?') AS city,
       c.info AS customer_info,
       d.entity_id AS service_id,
       SUBSTRING(d.data FROM '"product_name" ?: ?"([^"]*)",?') AS service_name,
       SUBSTRING(d.data FROM '"downlink" ?: ?"([0-9]+)",?')::INT AS service_download,
       SUBSTRING(d.data FROM '"uplink" ?: ?"([0-9]+)",?')::INT AS service_upload,
       SUBSTRING(d.data FROM '"price" ?: ?"([0-9]+)",?')::INT AS service_price,
       '' AS service_info
FROM drafts2 AS d
  LEFT JOIN draft_links AS la
         ON la.draft_id = d.id
        AND la.entity = 'agreements'
  LEFT JOIN draft_links AS lda
         ON lda.draft_id = d.id
        AND lda.entity = 'drafts.agreements'
  LEFT JOIN draft_links AS lc
         ON lc.draft_id = d.id
        AND lc.entity = 'customers'
  LEFT JOIN draft_links AS ldc
         ON ldc.draft_id = d.id
        AND ldc.entity = 'drafts.customers'
  LEFT JOIN agreements AS a ON la.entity_id = a.id
  LEFT JOIN drafts2 AS da ON lda.entity_id = da.entity_id
  LEFT JOIN customers AS c ON lc.entity_id = c.id
  LEFT JOIN drafts2 AS dc ON ldc.entity_id = dc.entity_id
WHERE d.entity_type = 'services'
AND   d.status != 'IMPORTED'
ORDER BY d.id
