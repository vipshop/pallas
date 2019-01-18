use pallas_console;

REPLACE INTO `user` VALUES (1,'admin','admin','admin','','','System','Unkown Real User','1979-12-31 16:00:00','2018-11-15 05:19:58',0),(2,'guest','guest','guest','','','System','System','1979-12-31 16:00:00','2018-11-13 08:13:25',0);

REPLACE INTO `user_role` VALUES (1,1,1,'System','System','1979-12-31 16:00:00','2018-11-15 05:20:13',0),(2,2,2,'System','System','1979-12-31 16:00:00','1979-12-31 16:00:00',0);

REPLACE INTO `role` VALUES (1,'Admin','Admin','System','System','1979-12-31 16:00:00','1979-12-31 16:00:00',0),(2,'Guest','Guest','System','System','1979-12-31 16:00:00','1979-12-31 16:00:00',0);

REPLACE INTO `role_permission` VALUES (1,1,1,'System','System','1979-12-31 16:00:00','2018-11-14 17:18:53',0),(2,1,2,'System','System','1979-12-31 16:00:00','2018-11-14 17:18:55',0),(3,1,3,'System','System','1979-12-31 16:00:00','2018-11-14 17:18:57',0),(4,1,4,'System','System','1979-12-31 16:00:00','2018-11-14 17:19:08',0),(5,1,5,'System','System','1979-12-31 16:00:00','2018-11-14 17:19:03',0),(6,1,6,'System','System','1979-12-31 16:00:00','2018-11-14 17:19:02',0),(7,1,7,'System','System','1979-12-31 16:00:00','2018-11-14 17:19:01',0),(8,1,8,'System','System','1979-12-31 16:00:00','2018-11-19 03:58:49',0);

REPLACE INTO `permission` VALUES (1,'cluster.all','cluster.all.write','','System','System','1979-12-31 16:00:00','1979-12-31 16:00:00',0),(2,'index.all','index.all.write','','System','System','1979-12-31 16:00:00','1979-12-31 16:00:00',0),(3,'version.all','version.all.write','','System','System','1979-12-31 16:00:00','1979-12-31 16:00:00',0),(4,'template.all','template.all.write','','System','System','1979-12-31 16:00:00','1979-12-31 16:00:00',0),(5,'plugin.all','plugin.all.write','','System','System','1979-12-31 16:00:00','2018-11-14 17:17:14',0),(6,'pallas-search.all','pallas-search.all.write','','System','System','1979-12-31 16:00:00','2018-11-14 17:17:19',0),(7,'authorization.all','authorization.all.write','','System','System','1979-12-31 16:00:00','2018-11-14 17:17:25',0),(8,'user.all','user.all.write','','System','System','1979-12-31 16:00:00','2018-11-19 03:58:10',0);

