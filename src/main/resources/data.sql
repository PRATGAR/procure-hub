INSERT INTO suppliers (legal_name, display_name, country, tax_id, status, website, description) VALUES
  ('Nordparts GmbH', 'Nordparts', 'DE', 'DE811234567', 'ACTIVE', 'https://www.nordparts.example.com', '<p>Precision <b>fasteners</b> and fittings for machine builders.</p>'),
  ('Acme Freight Ltd', 'Acme Freight', 'GB', 'GB123456789', 'ACTIVE', 'https://www.acmefreight.example.com', 'Road and sea freight across Europe.'),
  ('Helvetia Supply AG', 'Helvetia Supply', 'CH', 'CHE100200300', 'PENDING', 'https://www.helvetia-supply.example.com', 'Industrial consumables and packaging.');

INSERT INTO supplier_contacts (supplier_id, name, email, role, note) VALUES
  (1, 'Greta Lindqvist', 'greta.lindqvist@nordparts.example.com', 'BUYING', 'Main contact for framework agreements'),
  (1, 'Jonas Weber', 'jonas.weber@nordparts.example.com', 'FINANCE', 'Handles invoices and credit notes'),
  (2, 'Priya Nair', 'priya.nair@acmefreight.example.com', 'LOGISTICS', 'Dispatch desk');

INSERT INTO supplier_bank_accounts (supplier_id, holder, bic, iban_tail, status) VALUES
  (1, 'Nordparts GmbH', 'COBADEFFXXX', '4411', 'VERIFIED'),
  (2, 'Acme Freight Ltd', 'NWBKGB2LXXX', '9902', 'VERIFIED');

INSERT INTO supplier_certificates (supplier_id, issuer, file_name) VALUES
  (1, 'TUEV Sued', 'iso-9001.txt');

INSERT INTO catalog_categories (code, name) VALUES
  ('fasteners', 'Fasteners'),
  ('packaging', 'Packaging'),
  ('freight', 'Freight services'),
  ('electronics', 'Electronics');

INSERT INTO catalog_items (sku, name, description, category_code, tags, unit_price_cents, currency, supplier_id, status) VALUES
  ('BOLT-M8-100', 'Hex bolt M8 x 100', 'Zinc plated hex bolt, DIN 933', 'fasteners', 'bolt,zinc,din933', 1250, 'EUR', 1, 'ACTIVE'),
  ('NUT-M8-500', 'Hex nut M8 (box of 500)', 'Zinc plated hex nuts, DIN 934', 'fasteners', 'nut,zinc,din934', 3400, 'EUR', 1, 'ACTIVE'),
  ('BOX-40X30', 'Shipping carton 40x30', 'Double wall carton, brown', 'packaging', 'carton,shipping', 180, 'EUR', 3, 'ACTIVE'),
  ('PAL-EUR-1', 'Euro pallet', 'EPAL certified pallet', 'packaging', 'pallet,epal', 950, 'EUR', 3, 'ACTIVE'),
  ('PLC-S7-1200', 'Controller module', 'Compact PLC controller', 'electronics', 'plc,controller', 42000, 'EUR', NULL, 'DRAFT');

INSERT INTO catalog_media (item_id, stored_name, caption, content_type) VALUES
  (1, 'bolt-m8-datasheet.txt', 'Datasheet', 'text/plain');

INSERT INTO catalog_thumbnails (item_id, file_name) VALUES
  (1, 'bolt-m8.png');

INSERT INTO fx_rates (currency, rate_micro) VALUES
  ('EUR', 1000000),
  ('USD', 920000),
  ('GBP', 1170000),
  ('CHF', 1040000);

INSERT INTO freight_rules (incoterm, surcharge_bp) VALUES
  ('EXW', 0),
  ('FCA', 50),
  ('FOB', 120),
  ('CIF', 300),
  ('DAP', 450),
  ('DDP', 600);

INSERT INTO rfqs (title, description, category_code, region, status, closes_at, created_by) VALUES
  ('Corrugated cartons Q4', 'Frame contract for corrugated shipping cartons', 'packaging', 'EU', 'OPEN', DATE '2026-12-15', 'buyer'),
  ('M8 hardware 2027', 'Bolts, nuts and washers for the 2027 assembly lines', 'fasteners', 'EU', 'OPEN', DATE '2027-01-31', 'approver');

INSERT INTO rfq_invitations (rfq_id, supplier_id, token, access_pin, status) VALUES
  (1, 3, '5f2a91c07d3e4b6a8c1d0e9f7a6b5c4d', '482913', 'SENT'),
  (2, 1, 'a1b2c3d4e5f60718293a4b5c6d7e8f90', '710245', 'SENT');

INSERT INTO rfq_bids (rfq_id, invitation_id, amount_cents, currency, incoterm, notes, status) VALUES
  (1, 1, 1850000, 'EUR', 'DAP', 'Prices valid for 60 days', 'SUBMITTED'),
  (2, 2, 420000, 'EUR', 'FCA', 'Volume discount above 10000 units', 'SUBMITTED');

INSERT INTO rfq_questions (rfq_id, invitation_id, question, answer) VALUES
  (1, 1, 'Is delivery to two plants acceptable?', 'Yes, split deliveries are fine.');

INSERT INTO purchase_orders (po_number, supplier_id, status, total_cents, currency, instructions, label_template, return_url, delivery_hook, approval_code, ship_to, created_by) VALUES
  ('PO-1001', 1, 'DRAFT', 125000, 'EUR', 'Deliver to #{shipTo} on Euro pallets', 'PO #{poNumber} / #{supplierName}', '/api/orders/1', NULL, NULL, 'Plant 2, Dock 4, 70173 Stuttgart', 'buyer'),
  ('PO-1002', 3, 'SUBMITTED', 88000, 'EUR', 'Pack in cartons and mark every carton with #{poNumber}', 'PO #{poNumber}', '/api/orders/2', 'https://hooks.partner-one.example.com/po', '246810', 'Central warehouse, 20457 Hamburg', 'approver'),
  ('PO-1003', 1, 'APPROVED', 42000, 'EUR', 'Call the driver before arrival', 'PO #{poNumber}', NULL, 'https://hooks.partner-one.example.com/po', '135790', 'Plant 2, Dock 4, 70173 Stuttgart', 'buyer'),
  ('PO-1004', 1, 'SENT', 5000, 'EUR', NULL, NULL, NULL, NULL, '112233', 'Plant 1, 70173 Stuttgart', 'buyer');

INSERT INTO purchase_order_lines (order_id, sku, qty, received_qty, unit_cents) VALUES
  (1, 'BOLT-M8-100', 100, 0, 1250),
  (2, 'BOX-40X30', 400, 0, 180),
  (2, 'PAL-EUR-1', 20, 0, 950),
  (3, 'NUT-M8-500', 10, 0, 3400),
  (4, 'BOLT-M8-100', 4, 0, 1250);

INSERT INTO invoices (number, supplier_id, order_id, reference, currency, total_cents, paid_cents, status) VALUES
  ('INV-10001', 1, 4, 'PO-1004 delivery note 7781', 'EUR', 5000, 0, 'OPEN'),
  ('INV-10002', 3, 2, 'PO-1002 first delivery', 'EUR', 88000, 88000, 'PAID');

INSERT INTO payment_authorizations (invoice_id, auth_ref, amount_cents, captured_cents, refunded_cents, status, callback_url) VALUES
  (1, 'AUTH-0001', 5000, 0, 0, 'AUTHORIZED', 'https://hooks.partner-one.example.com/payments'),
  (2, 'AUTH-0002', 88000, 88000, 0, 'CAPTURED', NULL);

INSERT INTO shipments (order_id, carrier, tracking_no, status, last_status, label_name, notes) VALUES
  (4, 'NORDEX', 'NX20260041', 'IN_TRANSIT', 'Departed hub Hamburg', 'LBL-1.txt', 'Fragile goods'),
  (3, 'CARGOLINE', 'CL77120045', 'CREATED', 'Label created', NULL, NULL);

INSERT INTO shipment_events (shipment_id, status, detail) VALUES
  (1, 'Label created', 'Label printed at origin'),
  (1, 'Departed hub Hamburg', 'Scan at 07:42');

INSERT INTO documents (owner_type, owner_id, file_name, display_name, convert_opts, archive_name, fingerprint, status) VALUES
  ('ORDER', 4, 'delivery-note-7781.txt', 'Delivery note 7781', '--quality 80', 'ARC-seed-1.zip', 'a3f1c9d27b5e48f0916c2d7e0b4a8f35', 'REGISTERED'),
  ('SUPPLIER', 1, 'framework-agreement-2026.txt', 'Framework agreement 2026', NULL, 'ARC-seed-2.zip', 'c81e4b7a09d35f62a7b1e0c94d2f6a83', 'REGISTERED');

INSERT INTO partners (code, name, base_url, portal_url, sig_alg, shared_secret, status) VALUES
  ('ACMEFREIGHT', 'Acme Freight', 'https://api.acmefreight.example.com', 'https://portal.acmefreight.example.com/tradepost', 'MD5', 'whs-acme-7f3a91c2', 'ACTIVE'),
  ('NORDPARTS', 'Nordparts', 'https://api.nordparts.example.com', 'https://portal.nordparts.example.com', 'SHA-256', 'whs-nord-b41d09e7', 'ACTIVE');

INSERT INTO webhooks (partner_id, url, event_type, secret, status) VALUES
  (1, 'https://hooks.acmefreight.example.com/tradepost/events', 'order.approved', '9f2c41ab77d0e3c5', 'ACTIVE');

INSERT INTO inbound_events (partner_code, event_type, payload_hex, signature) VALUES
  ('ACMEFREIGHT', 'shipment.dispatched', 'aced00057372002e636f6d2e7472616465706f73742e70726f637572652e696e746567726174696f6e2e506172746e65724576656e7400000000000000010200034a000b616d6f756e7443656e74734c00096576656e74547970657400124c6a6176612f6c616e672f537472696e673b4c00097265666572656e636571007e000178700000000000000000740013736869706d656e742e6469737061746368656474000a4e583230323630303431', '6256687d1fb891af6a28107e4ae53236');

INSERT INTO price_tiers (min_qty, discount_bp) VALUES
  (10, 100),
  (50, 250),
  (100, 500),
  (500, 800);

INSERT INTO price_rules (name, scope, discount_expr, explanation, currency, active) VALUES
  ('Volume 100 plus', 'GLOBAL', 'qty >= 100 ? 0.06 : 0.0', 'Volume pricing applied for #{qty} units of #{sku}', 'EUR', TRUE),
  ('Seasonal packaging', 'CATEGORY', 'qty >= 20 ? 0.03 : 0.0', 'Seasonal promotion on packaging', 'EUR', FALSE);

INSERT INTO sso_links (user_ref, provider, external_id, unit, status) VALUES
  (1, 'KEYCLOAK', 'j.weber', 'PROCUREMENT', 'ACTIVE'),
  (2, 'ENTRA', 'p.nair', 'LOGISTICS', 'ACTIVE');

INSERT INTO saved_reports (name, entity, where_clause, description, owner) VALUES
  ('Open invoices', 'INVOICE', 'status = ''OPEN''', 'Invoices still waiting for payment', 'finance'),
  ('Active suppliers', 'SUPPLIER', 'status = ''ACTIVE''', 'Suppliers cleared for ordering', 'buyer');

INSERT INTO saved_filters (name, entity, where_clause) VALUES
  ('Large open EUR invoices', 'INVOICE', 'status = ''OPEN'' AND total_cents >= 1000 AND currency = ''EUR''');

INSERT INTO report_runs (report_id, file_name, row_count, status) VALUES
  (1, 'report-1-1.csv', 1, 'DONE');

INSERT INTO setting_entries (setting_key, setting_value, changed_by) VALUES
  ('portal.banner', 'Scheduled maintenance on Sunday 02:00-04:00 UTC', 'ops'),
  ('catalog.default-currency', 'EUR', 'ops'),
  ('orders.approval-threshold', '50000', 'ops');

INSERT INTO audit_log (actor, action, detail) VALUES
  ('ops', 'setting.updated', 'portal.banner'),
  ('buyer', 'order.approved', 'PO-1002'),
  ('approver', 'supplier.onboard', 'supplier 3');

INSERT INTO settings_bundles (name, blob_hex) VALUES
  ('Quarter-end defaults', 'aced00057372002a636f6d2e7472616465706f73742e70726f637572652e61646d696e2e53657474696e677342756e646c6500000000000000010200014c0007656e74726965737400134c6a6176612f7574696c2f486173684d61703b7870737200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f40000000000003770800000004000000027400196f72646572732e617070726f76616c2d7468726573686f6c647400053630303030740018636174616c6f672e64656661756c742d63757272656e637974000345555278');
