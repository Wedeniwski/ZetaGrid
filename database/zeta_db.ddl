--
-- Data definition of ZetaGrid
--
-- Author: Sebastian Wedeniwski
-- Date:   02/06/2001 (start)
--

-- db2 -tf zeta_db.sql

-- CREATE DB zeta;
-- CONNECT TO zeta;


--====================================================================
CREATE TABLE zeta.approve
--====================================================================
(
  server_id               SMALLINT NOT NULL,
  user_id                 INTEGER NOT NULL,
  key                     VARCHAR(500) NOT NULL,
  
  requested               TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
  approved                TIMESTAMP,

  data                    BLOB(50k)
);

CREATE INDEX zeta.approve_key ON zeta.approve (server_id,user_id,key);


--====================================================================
CREATE TABLE zeta.computation
--====================================================================
(
  task_id                 INTEGER NOT NULL,
  work_unit_id            BIGINT NOT NULL,
--....................................................................

  range                   INTEGER NOT NULL,
  redistributed_YN        CHAR(1) NOT NULL DEFAULT 'N',

  server_id               SMALLINT NOT NULL,
  workstation_id          INTEGER NOT NULL,
  user_id                 INTEGER NOT NULL,
  version                 CHAR(4) NOT NULL,

  start                   TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
  parameters              VARCHAR(1024),

  PRIMARY KEY(task_id, work_unit_id)
);

CREATE INDEX zeta.computation_ws ON zeta.computation (server_id,workstation_id);
CREATE INDEX zeta.computation_user ON zeta.computation (server_id,user_id);


--====================================================================
CREATE TABLE zeta.error
--====================================================================
(
  server_id               SMALLINT NOT NULL,
  timestamp               TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
  sql_statement           LONG VARCHAR NOT NULL
);


--====================================================================
CREATE TABLE zeta.found
--====================================================================
(
  task_id                 INTEGER NOT NULL,
  work_unit_id            BIGINT NOT NULL,
  type                    VARCHAR(100) NOT NULL,
  approved_YN             CHAR(1) NOT NULL DEFAULT 'N',
  timestamp               TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
  found                   LONG VARCHAR NOT NULL
);

CREATE INDEX zeta.found_type ON zeta.found (type);

--====================================================================
CREATE TABLE zeta.image
--====================================================================
(
  classname               VARCHAR(250) NOT NULL,
  imagename               VARCHAR(250) NOT NULL,
--....................................................................

  last_update             TIMESTAMP NOT NULL,
  image                   BLOB(100k),

  PRIMARY KEY(classname, imagename)
);


--====================================================================
CREATE TABLE zeta.iso_country_code
--====================================================================
(
  code                    VARCHAR(5) NOT NULL,
--....................................................................

  country                 VARCHAR(200) NOT NULL,
  x                       SMALLINT,
  y                       SMALLINT,

  PRIMARY KEY(code)
);


--====================================================================
CREATE TABLE zeta.page
--====================================================================
(
  classname               VARCHAR(250) NOT NULL,
--....................................................................

  last_update             TIMESTAMP NOT NULL,
  page                    BLOB(500k),
  data                    BLOB(4M),

  PRIMARY KEY(classname)
);


--====================================================================
CREATE TABLE zeta.parameter
--====================================================================
(
  task_id                 INTEGER NOT NULL,
  parameter               VARCHAR(50) NOT NULL,
--....................................................................

  value                   VARCHAR(100),

  PRIMARY KEY(task_id, parameter)
);


--====================================================================
CREATE TABLE zeta.program
--====================================================================
(
  task_id                 INTEGER NOT NULL,
  name                    VARCHAR(100) NOT NULL,
  os_name                 VARCHAR(100) NOT NULL,
  os_version              VARCHAR(20) NOT NULL,
  os_arch                 VARCHAR(20) NOT NULL,
  processors              SMALLINT NOT NULL,
--....................................................................

  version                 CHAR(4) NOT NULL,
  key_class_name          VARCHAR(250) NOT NULL DEFAULT 'zeta.crypto.DefaultKey',
  program_from_user       VARCHAR(100) NOT NULL,
  compressed_YN           CHAR(1) NOT NULL,
  last_update             TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
  signature               VARCHAR(1000) NOT NULL,
  overall_signature       VARCHAR(1000) NOT NULL,
  program                 BLOB(5M),

  PRIMARY KEY(task_id, name, os_name, os_version, os_arch, processors)
);

--====================================================================
CREATE TABLE zeta.recomputation
--====================================================================
(
  task_id                 INTEGER NOT NULL,
  work_unit_id            BIGINT NOT NULL,
--....................................................................

  range                   INTEGER NOT NULL,

  server_id               SMALLINT NOT NULL,
  workstation_id          INTEGER,
  user_id                 INTEGER,
  version                 CHAR(4),
  count                   SMALLINT NOT NULL DEFAULT 1,
  reason                  LONG VARCHAR,

  start                   TIMESTAMP,
  parameters              VARCHAR(1024),

  stop                    TIMESTAMP,
  result                  BLOB(100M),

  PRIMARY KEY(task_id, work_unit_id)
);

--====================================================================
CREATE TABLE zeta.result
--====================================================================
(
  task_id                 INTEGER NOT NULL,
  work_unit_id            BIGINT NOT NULL,
--....................................................................

  stop                    TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
  result                  BLOB(100M),

  PRIMARY KEY(task_id, work_unit_id)
);

CREATE INDEX zeta.result_stop ON zeta.result (stop);

--====================================================================
CREATE TABLE zeta.server
--====================================================================
(
  server_id               SMALLINT NOT NULL,
--....................................................................

  post_YN                 CHAR(1) NOT NULL,
  get_YN                  CHAR(1) NOT NULL,
  web_hostname            VARCHAR(100) NOT NULL,
  web_port                INTEGER NOT NULL,
  db_hostname             VARCHAR(100) NOT NULL,
  db_hostaddress          CHAR(15) NOT NULL,
  smtp_hostname           VARCHAR(100),
  smtp_port               INTEGER,
  smtp_login_name         VARCHAR(100),
  smtp_login_password     VARCHAR(100),

  proxy_host              VARCHAR(500),
  proxy_port              INTEGER,

  range                   BIGINT NOT NULL,
  synchronization_url     VARCHAR(100) NOT NULL,
  approve_team_url        VARCHAR(100) NOT NULL,
  last_synchronization    TIMESTAMP,
  key                     BLOB(500),

  PRIMARY KEY(server_id)
);

--====================================================================
CREATE TABLE zeta.server_synchronization
--====================================================================
(
  server_id               SMALLINT NOT NULL,
  timestamp               TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
  sql_statement           LONG VARCHAR NOT NULL
);

--====================================================================
CREATE TABLE zeta.server_range
--====================================================================
(
  server_id               SMALLINT NOT NULL,
  task_id                 INTEGER NOT NULL,
  work_unit_id            BIGINT NOT NULL,
--....................................................................

  range                   BIGINT NOT NULL,
  start                   TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,

  PRIMARY KEY(server_id, task_id, work_unit_id)
);

--====================================================================
CREATE TABLE zeta.task
--====================================================================
(
  id                      INTEGER NOT NULL,
--....................................................................

  name                    VARCHAR(100) NOT NULL UNIQUE,
  client_task_class_name  VARCHAR(250) NOT NULL,
  work_unit_class_name    VARCHAR(250) NOT NULL,
  encryption_class        LONG VARCHAR NOT NULL,
  encryption_signature    VARCHAR(1000) NOT NULL,
  decryption_number       VARCHAR(500) NOT NULL,
  request_processor       VARCHAR(250) NOT NULL,
  result_processor        VARCHAR(250) NOT NULL,
  verifier_class_name     VARCHAR(250),
  overall_signature       VARCHAR(1000),

  PRIMARY KEY(id)
);

--====================================================================
CREATE TABLE zeta.user
--====================================================================
(
  server_id                     SMALLINT NOT NULL,
  id                            INTEGER NOT NULL,
--....................................................................

  fail                          INTEGER NOT NULL DEFAULT 0,
  trust                         INTEGER NOT NULL DEFAULT 0,
  active_YN                     CHAR(1) NOT NULL DEFAULT 'Y',
  recomputation_YN              CHAR(1) NOT NULL DEFAULT 'N',

  name                          VARCHAR(100) NOT NULL,
  email                         VARCHAR(100) NOT NULL,
  email_valid_YN                CHAR(1) NOT NULL DEFAULT 'Y',

  team_name                     VARCHAR(100),
  join_in_team                  TIMESTAMP,

  number_of_redistributions     INTEGER NOT NULL DEFAULT 0,
  last_redistributed_work_unit  BIGINT,
  last_redistributed_timestamp  TIMESTAMP,

  properties                    VARCHAR(1024),

  PRIMARY KEY(server_id, id)
);

--====================================================================
CREATE TABLE zeta.workstation
--====================================================================
(
  server_id                     SMALLINT NOT NULL,
  id                            INTEGER NOT NULL,
--................................................................

  active_YN                     CHAR(1) NOT NULL DEFAULT 'Y',

  key                           VARCHAR(200),

  hostname                      VARCHAR(100) NOT NULL,
  hostaddress                   CHAR(15) NOT NULL,

  os_name                       VARCHAR(50) NOT NULL,
  os_version                    VARCHAR(50) NOT NULL,
  os_arch                       VARCHAR(10) NOT NULL,
  processors                    INTEGER NOT NULL DEFAULT 1,
  processors_approved           INTEGER NOT NULL DEFAULT 1,

  number_of_redistributions     INTEGER NOT NULL DEFAULT 0,
  last_redistributed_work_unit  BIGINT,
  last_redistributed_timestamp  TIMESTAMP,

  last_update                   TIMESTAMP,
  last_local_files              VARCHAR(500),

  PRIMARY KEY(server_id, id)
);


--====================================================================
CREATE TABLE zeta.work_unit_size
--====================================================================
--
-- Defines the range of a work unit for a specified task and size with
-- the id less than or equal to 'work_unit_id'.
--
-- For the task 'zeta-zeros' we have the sizes:
-- t: tiny work unit ~  60 minutes
-- s: small work unit ~  90 minutes
-- m: medium work unit ~ 3 hours
-- l: large work unit ~  4 hours
-- h: huge work unit ~  6 hours
--
(
  task_id                 INTEGER NOT NULL,
  size                    CHAR(1) NOT NULL,
  work_unit_id            BIGINT NOT NULL,
--....................................................................

  range                   INTEGER NOT NULL,

  PRIMARY KEY(task_id, size, work_unit_id)
);


-- INSERT INTO zeta.computation (task_id,work_unit_id,range,server_id,workstation_id,user_id,version) VALUES (1,-1,0,1,0,0,'0100');

INSERT INTO zeta.image (classname, imagename, last_update) VALUES
  ('zeta.handler.statistic.OperatingSystemsHandler', 'pie', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.WorkLoadTodayHandler', 'histogram', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.LocationsHandler', 'map', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.StatisticOverviewHandler', 'computational_results', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.StatisticOverviewHandler', 'resource_providers', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.StatisticOverviewHandler', 'computers', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.StatisticOverviewHandler', 'reserved_zeros', CURRENT TIMESTAMP);

INSERT INTO zeta.page (classname, last_update) VALUES
  ('zeta.handler.statistic.Top100ProducersHandler', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.Top1000ProducersHandler', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.TopProducersHandler', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.TopProducers24HoursHandler', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.TopProducers7DaysHandler', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.TopTeamsHandler', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.TeamMembersHandler', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.OperatingSystemsHandler', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.WorkstationsHandler', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.WorkLoadTodayHandler', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.CloseZerosHandler', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.LocationsHandler', CURRENT TIMESTAMP),
  ('zeta.handler.statistic.StatisticOverviewHandler', CURRENT TIMESTAMP);

INSERT INTO zeta.parameter (task_id,parameter,value) VALUES
  (0, 'new_workstation_is_active', 'Y'),
  (1, 'work_unit_id_overlap', '100'),
  (1, 'work_unit_id_complete', '0'),
  (0, 'max_stmt_synchronization', '0'),
  (0, 'all_tasks_signature', ''),
  (0, 'encryption', 'Y'),
  (0, 'path_log', ''),
  (0, 'path_data', ''),
  (1, 'path_data', ''),
  (0, 'path_page', ''),
  (1, 'path_out', ''),
  (0, 'grant_data_dir', 'Y'),
  (0, 'grant_data_list', 'Y'),
  (0, 'grant_data_get', 'Y'),
  (0, 'grant_data_del', 'Y'),
  (0, 'grant_data_put', 'N'),
  (0, 'grant_data_hostnames', ''),
  (0, 'last_redistribution', NULL),
  (0, 'diff_to_last_redistribution', '432000000'),
  (0, 'deadline_connected_redistribution_in_days', '28'),
  (0, 'deadline_not_connected_redistribution_in_days', '14');

-- INSERT INTO zeta.user (id,server_id,name,email) VALUES (0,0,'','');

INSERT INTO zeta.server
  (server_id, post_YN, get_YN, web_hostname, web_port, db_hostname, db_hostaddress, proxy_host, proxy_port, range, synchronization_url, approve_team_url)
VALUES
  (2, 'Y', 'Y', 'www.zetagrid.net', 80, 'zetagrid', '', NULL, NULL, 5000000000, '/servlet/service/synchronization', '/servlet/service/approveteam');


INSERT INTO zeta.task
  (id, name, client_task_class_name, work_unit_class_name, encryption_class, encryption_signature, decryption_number, request_processor, result_processor, verifier_class_name, overall_signature)
VALUES
  (0,
   'default',
   'zeta.ClientTask',
   'zeta.WorkUnit',
   'yv66vgAAAC4AOQoAEAAjBwAkCAAlCgACACYJAA8AJwoAAgAoCAApCgACACoIACsJAA8ALAgALQgALgkADwAvCAAwBwAxBwAyBwAzAQABcAEAFkxqYXZhL21hdGgvQmlnSW50ZWdlcjsBAAFnAQABQQEABjxpbml0PgEAAygpVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAR0aGlzAQAfTHpldGEvY3J5cHRvL0RlZmF1bHRLZXlFbmNyeXB0OwEAB2dldEJhc2UBABgoKUxqYXZhL21hdGgvQmlnSW50ZWdlcjsBAAlnZXRNb2R1bG8BAAxnZXRHZW5lcmF0b3IBAApTb3VyY2VGaWxlAQAWRGVmYXVsdEtleUVuY3J5cHQuamF2YQwAFgAXAQAUamF2YS9tYXRoL0JpZ0ludGVnZXIBAGkxMkxCSzVUTU9COTlDMTMxSkIzUTlVTVZTMThTRlU3T09LTDI1SzNPSDUxUEwxSDZHSDNIRFY2T0lLTDJMUUk4NktDQjRMR01BU0RGTEFQTEpHNU5KVVYwNUVMQzJQS0xHMktHMlNDQjIMABYANAwAEgATDAA1ADYBAGRTR0pMQTFLOTM2SEJIMEpWS0tMNEJRQkw3Nkc1N0tPMUtIOTAzNDVCSUxUNDg1MEk5TUw1Ukc5UTgwNTNLSkFHUjdOOENKRkxRSTlMVjBPMTVVVjlJTkhVRk1JSUkzTEw1SjUzDAA3ADgBAGhCTEtUODgzRkFCNlYxQkNGNjRHNFBEVU5NSDlCOUQzMTdFQ1ZIRUFPSzNPSFBPUzJFNEQ3Q0k4UEsyNkVQMzZNSDFHSlMxTkc4Qjc0RDEwRzZFM0tLSk5JN1FNRkdSNFE4SjdGUThCOAwAFAATAQBkQzJOSFJWUVVMR0JKOEg0RjJVVTFHUzBBVEJKNUZERUlCN0hHSU41VTFLMUVTMEVCN05QOE5RQVBSNDFCNVBRMVRCT1VTNFBKNUFBQzJHUlNKU0VNS0JFTjQ5SUdIT0E4T1JGQQEAaFU1M0VPOTQ2VFQwRVEwUUhHUzZHQ0JCVVZWRFJGOVU1MFFWQjM3OTVNSVAzT0gxOFM5MUFBRlFWNTRONVJGQkwzSTdVSVBBVU00UjY3UFVRRVYzSlFIOVBOUEVKODRJVTZMM0ZWTTdMDAAVABMBAGQ5VlUxRjRSNlREVjlIUUgwUkFLTDdWUUVJM0c4N000Rjg2Q1JRS1E5UDVDODkyRjJRUFUxTTZVQ1NFSURMMlZQOFVESzk4MDhVSEhHVlZQVkZCVkQ2QUU3TkM4REpIU1VLSUVHAQAdemV0YS9jcnlwdG8vRGVmYXVsdEtleUVuY3J5cHQBABBqYXZhL2xhbmcvT2JqZWN0AQAPemV0YS9jcnlwdG8vS2V5AQAWKExqYXZhL2xhbmcvU3RyaW5nO0kpVgEACXNoaWZ0TGVmdAEAGShJKUxqYXZhL21hdGgvQmlnSW50ZWdlcjsBAAJvcgEALihMamF2YS9tYXRoL0JpZ0ludGVnZXI7KUxqYXZhL21hdGgvQmlnSW50ZWdlcjsAIQAPABAAAQARAAMAAgASABMAAAACABQAEwAAAAIAFQATAAAABAABABYAFwABABgAAADwAAYAAQAAAJ4qtwABKrsAAlkSAxAgtwAEtQAFKiq0AAURAfS2AAa1AAUqKrQABbsAAlkSBxAgtwAEtgAItQAFKrsAAlkSCRAgtwAEtQAKKiq0AAoRAfS2AAa1AAoqKrQACrsAAlkSCxAgtwAEtgAItQAKKrsAAlkSDBAgtwAEtQANKiq0AA0RAfS2AAa1AA0qKrQADbsAAlkSDhAgtwAEtgAItQANsQAAAAIAGQAAAC4ACwAAADAABAAyABMAMwAhADQANwA2AEYANwBUADgAagA6AHkAOwCHADwAnQA9ABoAAAAMAAEAAACeABsAHAAAAAEAHQAeAAEAGAAAAC8AAQABAAAABSq0AA2wAAAAAgAZAAAABgABAAAAQAAaAAAADAABAAAABQAbABwAAAABAB8AHgABABgAAAAvAAEAAQAAAAUqtAAFsAAAAAIAGQAAAAYAAQAAAEQAGgAAAAwAAQAAAAUAGwAcAAAAAQAgAB4AAQAYAAAALwABAAEAAAAFKrQACrAAAAACABkAAAAGAAEAAABIABoAAAAMAAEAAAAFABsAHAAAAAEAIQAAAAIAIg**',
   '8*4*32(2801237585,2057860854,3257779099,1621825353,3888992362,2101149048,1492870311,4072420530,3364777131,4096903281,2701282255,2568445771,3272757614,720389596,2691249757,905346194,4280257587,2316451669,4029782253,2670893506,1603846862,1275953329,3039467633,3367940933,280295506,2253934928,3831085459,1289792349,61881556,3969169559,2919855113,193259310)8*4*32(1499762900,2009566925,3240642080,550161640,3820892278,3720913497,669969511,3225061745,3336569630,2119909336,1304232647,983072257,3273037718,3502993771,3505171659,1221059285,642633813,2839771809,2809981327,2057197818,1853439344,586684512,2683337445,3636218268,3271087493,1085579251,4294164637,3595706067,1782580017,1636978768,1203424646,257014512)',
   '',
   'zeta.server.processor.DefaultWorkUnitProcessor',
   'zeta.server.processor.SimpleFileProcessor',
   NULL,
   NULL);


INSERT INTO zeta.work_unit_size
  (task_id, size, work_unit_id, range)
VALUES
  (1, 't', 0,   200000),
  (1, 's', 0,   300000),
  (1, 'm', 0,   500000),
  (1, 'l', 0,   700000),
  (1, 'h', 0,  1000000);


INSERT INTO zeta.iso_country_code (code,country,x,y) VALUES
  ('1ad','Andorra, Principality of', NULL, NULL),
  ('1ae','United Arab Emirates', NULL, NULL),
  ('1af','Afghanistan, Islamic State of', NULL, NULL),
  ('1ag','Antigua and Barbuda', NULL, NULL),
  ('1ai','Anguilla', NULL, NULL),
  ('1al','Albania', NULL, NULL),
  ('1am','Armenia', NULL, NULL),
  ('1an','Netherlands Antilles', NULL, NULL),
  ('1ao','Angola', NULL, NULL),
  ('1aq','Antarctica', NULL, NULL),
  ('1ar','Argentina', 230, 330),
  ('1as','American Samoa', NULL, NULL),
  ('1at','Austria', 408, 139),
  ('1au','Australia', 667, 307),
  ('1aw','Aruba', NULL, NULL),
  ('1az','Azerbaidjan', NULL, NULL),
  ('1ba','Bosnia-Herzegovina', NULL, NULL),
  ('1bb','Barbados', NULL, NULL),
  ('1bd','Bangladesh', NULL, NULL),
  ('1be','Belgium', 386, 131),
  ('1bf','Burkina Faso', NULL, NULL),
  ('1bg','Bulgaria', NULL, NULL),
  ('1bh','Bahrain', NULL, NULL),
  ('1bi','Burundi', NULL, NULL),
  ('1bj','Benin', NULL, NULL),
  ('1bm','Bermuda', NULL, NULL),
  ('1bn','Brunei Darussalam', NULL, NULL),
  ('1bo','Bolivia', NULL, NULL),
  ('1br','Brazil', 268, 276),
  ('1bs','Bahamas', NULL, NULL),
  ('1bt','Bhutan', NULL, NULL),
  ('1bv','Bouvet Island', NULL, NULL),
  ('1bw','Botswana', NULL, NULL),
  ('1by','Belarus', NULL, NULL),
  ('1bz','Belize', NULL, NULL),
  ('1ca','Canada', 137, 115),
  ('1cc','Cocos (Keeling) Islands', 608, 279),
  ('1cf','Central African Republic', NULL, NULL),
  ('1cd','Congo, The Democratic Republic of the', NULL, NULL),
  ('1cg','Congo', NULL, NULL),
  ('1ch','Switzerland', 392, 142),
  ('1ci','Ivory Coast', NULL, NULL),
  ('1ck','Cook Islands', NULL, NULL),
  ('1cl','Chile', 214, 342),
  ('1cm','Cameroon', NULL, NULL),
  ('1cn','China', 625, 176),
  ('1co','Colombia', NULL, NULL),
  ('1cr','Costa Rica', NULL, NULL),
  ('1cs','Former Czechoslovakia', 406, 133),
  ('1cu','Cuba', NULL, NULL),
  ('1cv','Cape Verde', NULL, NULL),
  ('1cx','Christmas Island', 589, 285),
  ('1cy','Cyprus', NULL, NULL),
  ('1cz','Czech Republic', 406, 133),
  ('1de','Germany', 396, 130),
  ('1dj','Djibouti', NULL, NULL),
  ('1dk','Denmark', 395, 114),
  ('1dm','Dominica', NULL, NULL),
  ('1do','Dominican Republic', NULL, NULL),
  ('1dz','Algeria', NULL, NULL),
  ('1ec','Ecuador', NULL, NULL),
  ('1ee','Estonia', NULL, NULL),
  ('1eg','Egypt', NULL, NULL),
  ('1eh','Western Sahara', NULL, NULL),
  ('1er','Eritrea', NULL, NULL),
  ('1es','Spain', 367, 160),
  ('1et','Ethiopia', 463, 234),
  ('1fi','Finland', 431, 93),
  ('1fj','Fiji', NULL, NULL),
  ('1fk','Falkland Islands', NULL, NULL),
  ('1fm','Micronesia', 716, 248),
  ('1fo','Faroe Islands', NULL, NULL),
  ('1fr','France', 384, 138),
  ('1fx','France (European Territory)', 384, 138),
  ('1ga','Gabon', NULL, NULL),
  ('1gb','Great Britain', NULL, NULL),
  ('1gd','Grenada', NULL, NULL),
  ('1ge','Georgia', NULL, NULL),
  ('1gf','French Guyana', NULL, NULL),
  ('1gh','Ghana', NULL, NULL),
  ('1gi','Gibraltar', NULL, NULL),
  ('1gl','Greenland', 281, 70),
  ('1gm','Gambia', NULL, NULL),
  ('1gn','Guinea', NULL, NULL),
  ('1gp','Guadeloupe (French)', NULL, NULL),
  ('1gq','Equatorial Guinea', NULL, NULL),
  ('1gr','Greece', 424, 161),
  ('1gs','S. Georgia & S. Sandwich Isls.', NULL, NULL),
  ('1gt','Guatemala', 172, 218),
  ('1gu','Guam (USA)', NULL, NULL),
  ('1gw','Guinea Bissau', NULL, NULL),
  ('1gy','Guyana', NULL, NULL),
  ('1hk','Hong Kong', 630, 204),
  ('1hm','Heard and McDonald Islands', NULL, NULL),
  ('1hn','Honduras', NULL, NULL),
  ('1hr','Croatia', NULL, NULL),
  ('1ht','Haiti', NULL, NULL),
  ('1hu','Hungary', 419, 141),
  ('1id','Indonesia', NULL, NULL),
  ('1ie','Ireland', NULL, NULL),
  ('1il','Israel', 453, 183),
  ('1in','India', 548, 216),
  ('1io','British Indian Ocean Territory', NULL, NULL),
  ('1iq','Iraq', NULL, NULL),
  ('1ir','Iran', NULL, NULL),
  ('1is','Iceland', 334, 86),
  ('1it','Italy', 404, 152),
  ('1jm','Jamaica', NULL, NULL),
  ('1jo','Jordan', NULL, NULL),
  ('1jp','Japan', 684, 168),
  ('1ke','Kenya', NULL, NULL),
  ('1kg','Kyrgyz Republic (Kyrgyzstan)', NULL, NULL),
  ('1kh','Cambodia, Kingdom of', NULL, NULL),
  ('1ki','Kiribati', NULL, NULL),
  ('1km','Comoros', NULL, NULL),
  ('1kn','Saint Kitts & Nevis Anguilla', NULL, NULL),
  ('1kp','North Korea', NULL, NULL),
  ('1kr','South Korea', 659, 169),
  ('1kw','Kuwait', NULL, NULL),
  ('1ky','Cayman Islands', NULL, NULL),
  ('1kz','Kazakhstan', NULL, NULL),
  ('1la','Laos', NULL, NULL),
  ('1lb','Lebanon', NULL, NULL),
  ('1lc','Saint Lucia', NULL, NULL),
  ('1li','Liechtenstein', 396, 137),
  ('1lk','Sri Lanka', NULL, NULL),
  ('1lr','Liberia', NULL, NULL),
  ('1ls','Lesotho', NULL, NULL),
  ('1lt','Lithuania', NULL, NULL),
  ('1lu','Luxembourg', 390, 133),
  ('1lv','Latvia', NULL, NULL),
  ('1ly','Libya', NULL, NULL),
  ('1ma','Morocco', NULL, NULL),
  ('1mc','Monaco', 393, 146),
  ('1md','Moldavia', NULL, NULL),
  ('1mg','Madagascar', NULL, NULL),
  ('1mh','Marshall Islands', NULL, NULL),
  ('1mk','Macedonia', NULL, NULL),
  ('1ml','Mali', 373, 209),
  ('1mm','Myanmar', NULL, NULL),
  ('1mn','Mongolia', NULL, NULL),
  ('1mo','Macau', NULL, NULL),
  ('1mp','Northern Mariana Islands', NULL, NULL),
  ('1mq','Martinique (French)', NULL, NULL),
  ('1mr','Mauritania', NULL, NULL),
  ('1ms','Montserrat', 235, 216),
  ('1mt','Malta', NULL, NULL),
  ('1mu','Mauritius', 502, 298),
  ('1mv','Maldives', 539, 243),
  ('1mw','Malawi', NULL, NULL),
  ('1mx','Mexico', 146, 194),
  ('1my','Malaysia', 627, 249),
  ('1mz','Mozambique', NULL, NULL),
  ('1na','Namibia', NULL, NULL),
  ('1nc','New Caledonia (French)', 741, 300),
  ('1ne','Niger', NULL, NULL),
  ('1nf','Norfolk Island', NULL, NULL),
  ('1ng','Nigeria', NULL, NULL),
  ('1ni','Nicaragua', NULL, NULL),
  ('1nl','Netherlands', 386, 127),
  ('1no','Norway', 391, 101),
  ('1np','Nepal', NULL, NULL),
  ('1nr','Nauru', NULL, NULL),
  ('1nt','Neutral Zone', NULL, NULL),
  ('1nu','Niue', NULL, NULL),
  ('1nz','New Zealand', 755, 357),
  ('1om','Oman', NULL, NULL),
  ('1pa','Panama', NULL, NULL),
  ('1pe','Peru', 205, 270),
  ('1pf','Polynesia (French)', NULL, NULL),
  ('1pg','Papua New Guinea', NULL, NULL),
  ('1ph','Philippines', 644, 219),
  ('1pk','Pakistan', NULL, NULL),
  ('1pl','Poland', 413, 127),
  ('1pm','Saint Pierre and Miquelon', NULL, NULL),
  ('1pn','Pitcairn Island', NULL, NULL),
  ('1pr','Puerto Rico', NULL, NULL),
  ('1pt','Portugal', 356, 159),
  ('1pw','Palau', NULL, NULL),
  ('1py','Paraguay', NULL, NULL),
  ('1qa','Qatar', NULL, NULL),
  ('1re','Reunion (French)', NULL, NULL),
  ('1ro','Romania', 430, 144),
  ('1ru','Russian Federation', 485, 108),
  ('1rw','Rwanda', NULL, NULL),
  ('1sa','Saudi Arabia', NULL, NULL),
  ('1sb','Solomon Islands', NULL, NULL),
  ('1sc','Seychelles', NULL, NULL),
  ('1sd','Sudan', NULL, NULL),
  ('1se','Sweden', 407, 106),
  ('1sg','Singapore', NULL, NULL),
  ('1sh','Saint Helena', NULL, NULL),
  ('1si','Slovenia', NULL, NULL),
  ('1sj','Svalbard and Jan Mayen Islands', NULL, NULL),
  ('1sk','Slovak Republic', NULL, NULL),
  ('1sl','Sierra Leone', NULL, NULL),
  ('1sm','San Marino', NULL, NULL),
  ('1sn','Senegal', NULL, NULL),
  ('1so','Somalia', NULL, NULL),
  ('1sr','Suriname', NULL, NULL),
  ('1st','Saint Tome (Sao Tome) and Principe', NULL, NULL),
  ('1su','Former USSR', NULL, NULL),
  ('1sv','El Salvador', NULL, NULL),
  ('1sy','Syria', NULL, NULL),
  ('1sz','Swaziland', NULL, NULL),
  ('1tc','Turks and Caicos Islands', NULL, NULL),
  ('1td','Chad', NULL, NULL),
  ('1tf','French Southern Territories', NULL, NULL),
  ('1tg','Togo', NULL, NULL),
  ('1th','Thailand', 600, 219),
  ('1tj','Tadjikistan', NULL, NULL),
  ('1tk','Tokelau', NULL, NULL),
  ('1tm','Turkmenistan', NULL, NULL),
  ('1tn','Tunisia', NULL, NULL),
  ('1to','Tonga', 793, 299),
  ('1tp','East Timor', NULL, NULL),
  ('1tr','Turkey', 453, 162),
  ('1tt','Trinidad and Tobago', NULL, NULL),
  ('1tv','Tuvalu', 764, 277),
  ('1tw','Taiwan', 644, 200),
  ('1tz','Tanzania', NULL, NULL),
  ('1ua','Ukraine', 446, 134),
  ('1ug','Uganda', NULL, NULL),
  ('1uk','United Kingdom', 372, 126),
  ('1um','USA Minor Outlying Islands', NULL, NULL),
  ('1us','United States', 156, 161),
  ('1uy','Uruguay', 251, 329),
  ('1uz','Uzbekistan', NULL, NULL),
  ('1va','Holy See (Vatican City State)', NULL, NULL),
  ('1vc','Saint Vincent & Grenadines', NULL, NULL),
  ('1ve','Venezuela', NULL, NULL),
  ('1vg','Virgin Islands (British)', NULL, NULL),
  ('1vi','Virgin Islands (USA)', NULL, NULL),
  ('1vn','Vietnam', NULL, NULL),
  ('1vu','Vanuatu', NULL, NULL),
  ('1wf','Wallis and Futuna Islands', NULL, NULL),
  ('1ws','Samoa', NULL, NULL),
  ('1ye','Yemen', NULL, NULL),
  ('1yt','Mayotte', NULL, NULL),
  ('1yu','Yugoslavia', NULL, NULL),
  ('1za','South Africa', 428, 327),
  ('1zm','Zambia', NULL, NULL),
  ('1zr','Zaire', NULL, NULL),
  ('1zw','Zimbabwe', NULL, NULL),
  ('2arpa','Old style Arpanet (.arpa)', NULL, NULL),
  ('2com','Commercial (.com)', NULL, NULL),
  ('2edu','Educational (.edu)', NULL, NULL),
  ('2gov','USA Government (.gov)', NULL, NULL),
  ('2int','International (.int)', NULL, NULL),
  ('2mil','USA Military (.mil)', NULL, NULL),
  ('2nato','NATO', NULL, NULL),
  ('2net','Network (.net)', NULL, NULL),
  ('2org','Non-Profit Making Organisations (.org)', NULL, NULL);


GRANT SELECT ON TABLE zeta.computation TO USER zeta;
GRANT SELECT ON TABLE zeta.found TO USER zeta;
GRANT SELECT ON TABLE zeta.parameter TO USER zeta;
GRANT SELECT ON TABLE zeta.recomputation TO USER zeta;
GRANT SELECT ON TABLE zeta.result TO USER zeta;
GRANT SELECT ON TABLE zeta.task TO USER zeta;
GRANT SELECT ON TABLE zeta.user TO USER zeta;
GRANT SELECT ON TABLE zeta.workstation TO USER zeta;

GRANT SELECT,INSERT,UPDATE ON TABLE zeta.approve TO USER zetacalc;
GRANT SELECT,INSERT ON TABLE zeta.computation TO USER zetacalc;
GRANT SELECT,INSERT ON TABLE zeta.error TO USER zetacalc;
GRANT SELECT,INSERT ON TABLE zeta.found TO USER zetacalc;
GRANT SELECT,UPDATE ON TABLE zeta.image TO USER zetacalc;
GRANT SELECT ON TABLE zeta.iso_country_code TO USER zetacalc;
GRANT SELECT,UPDATE ON TABLE zeta.page TO USER zetacalc;
GRANT SELECT,UPDATE ON TABLE zeta.parameter TO USER zetacalc;
GRANT SELECT ON TABLE zeta.program TO USER zetacalc;
GRANT SELECT,INSERT,UPDATE ON TABLE zeta.recomputation TO USER zetacalc;
GRANT SELECT,INSERT,UPDATE ON TABLE zeta.result TO USER zetacalc;
GRANT SELECT,UPDATE ON TABLE zeta.server TO USER zetacalc;
GRANT SELECT,INSERT,DELETE ON TABLE zeta.server_synchronization TO USER zetacalc;
GRANT SELECT,INSERT ON TABLE zeta.server_range TO USER zetacalc;
GRANT SELECT ON TABLE zeta.task TO USER zetacalc;
GRANT SELECT,UPDATE,INSERT ON TABLE zeta.user TO USER zetacalc;
GRANT SELECT,INSERT,UPDATE ON TABLE zeta.workstation TO USER zetacalc;
GRANT SELECT ON TABLE zeta.work_unit_size TO USER zetacalc;

ALTER BUFFERPOOL IBMDEFAULTBP SIZE 50000;

UPDATE DATABASE CONFIGURATION FOR zeta USING dbheap          35000;
UPDATE DATABASE CONFIGURATION FOR zeta USING catalogcache_sz 1024;
UPDATE DATABASE CONFIGURATION FOR zeta USING logbufsz        4096;
UPDATE DATABASE CONFIGURATION FOR zeta USING locklist        200;
UPDATE DATABASE CONFIGURATION FOR zeta USING locktimeout     240;
UPDATE DATABASE CONFIGURATION FOR zeta USING sortheap        2000;
UPDATE DATABASE CONFIGURATION FOR zeta USING stmtheap        4096;
UPDATE DATABASE CONFIGURATION FOR zeta USING applheapsz      1024;
UPDATE DATABASE CONFIGURATION FOR zeta USING app_ctl_heap_sz 1024;
UPDATE DATABASE CONFIGURATION FOR zeta USING buffpage        1000;

UPDATE DBM CFG USING udf_mem_sz                              1000;

UPDATE DBM CFG USING maxagents                               500;
UPDATE DBM CFG USING num_initagents                          50;

UPDATE DATABASE CONFIGURATION FOR zeta USING maxappls        500;
UPDATE DATABASE CONFIGURATION FOR zeta USING avg_appls       30;
UPDATE DATABASE CONFIGURATION FOR zeta USING maxlocks        50;

UPDATE DATABASE CONFIGURATION FOR zeta USING logfilsiz       2000;
UPDATE DATABASE CONFIGURATION FOR zeta USING logprimary      25;
UPDATE DATABASE CONFIGURATION FOR zeta USING logsecond       50;
