-- MySQL dump 10.13  Distrib 5.5.8, for Win32 (x86)
--
-- Host: localhost    Database: pcr
-- ------------------------------------------------------
-- Server version	5.5.8

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `contentattribute`
--

DROP TABLE IF EXISTS `contentattribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `contentattribute` (
  `contentid` varchar(32) NOT NULL DEFAULT '',
  `name` varchar(255) NOT NULL DEFAULT '',
  `value_text` varchar(255) DEFAULT NULL,
  `value_bin` mediumblob,
  `value_int` int(11) DEFAULT NULL,
  `sortorder` int(11) DEFAULT NULL,
  `value_blob` longblob,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `value_clob` mediumtext,
  `value_long` bigint(20) DEFAULT NULL,
  `value_double` double DEFAULT NULL,
  `value_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `value_text` (`value_text`),
  KEY `contentid` (`contentid`,`name`),
  KEY `contentattribute_idx3` (`name`),
  KEY `contentattribute_idx2` (`contentid`)
) ENGINE=MyISAM AUTO_INCREMENT=3435527 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contentattribute`
--

LOCK TABLES `contentattribute` WRITE;
/*!40000 ALTER TABLE `contentattribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `contentattribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contentattributetype`
--

DROP TABLE IF EXISTS `contentattributetype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `contentattributetype` (
  `name` varchar(255) DEFAULT NULL,
  `attributetype` int(11) DEFAULT NULL,
  `optimized` int(11) DEFAULT NULL,
  `quickname` varchar(255) DEFAULT NULL,
  `multivalue` int(11) NOT NULL DEFAULT '0',
  `objecttype` int(11) NOT NULL DEFAULT '0',
  `linkedobjecttype` int(11) NOT NULL DEFAULT '0',
  `foreignlinkattribute` varchar(255) DEFAULT NULL,
  `foreignlinkattributerule` mediumtext,
  `exclude_versioning` int(11) NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contentattributetype`
--

LOCK TABLES `contentattributetype` WRITE;
/*!40000 ALTER TABLE `contentattributetype` DISABLE KEYS */;
INSERT INTO `contentattributetype` VALUES ('userid',3,0,NULL,0,35000,0,NULL,NULL,0),('useremail',1,0,NULL,0,35000,0,NULL,NULL,0),('text',5,0,NULL,0,35000,0,NULL,NULL,0),('forcontentid',1,0,NULL,0,35000,0,NULL,NULL,0),('ts',10,NULL,NULL,0,35000,0,NULL,NULL,0),('username',1,NULL,NULL,0,35000,0,NULL,NULL,0),('forcontentid',1,0,NULL,0,36000,0,NULL,NULL,0),('userid',3,NULL,NULL,0,36000,0,NULL,NULL,0),('username',1,NULL,NULL,0,36000,0,NULL,NULL,0),('useremail',1,NULL,NULL,0,36000,0,NULL,NULL,0),('name',1,NULL,NULL,0,37000,0,NULL,NULL,0),('forcontentid',1,NULL,NULL,0,37000,0,NULL,NULL,0),('name',1,0,NULL,0,12000,0,'','',0),('surename',1,0,NULL,0,12000,0,'','',0),('permissions',1,0,NULL,1,12000,0,'','',0),('name',1,0,NULL,0,13000,0,'','',0),('permissions',1,0,NULL,1,13000,0,'','',0),('street',1,0,NULL,0,14000,0,'','',0),('city',1,0,NULL,0,14000,0,'','',0),('zip',3,0,NULL,0,14000,0,'','',0),('address',2,0,NULL,0,13000,14000,'','',0),('company',2,0,NULL,0,12000,13000,'','',0),('employees',7,0,NULL,0,13000,12000,'company','',0);
/*!40000 ALTER TABLE `contentattributetype` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contentmap`
--

DROP TABLE IF EXISTS `contentmap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `contentmap` (
  `contentid` varchar(32) NOT NULL DEFAULT '',
  `obj_id` int(11) NOT NULL DEFAULT '0',
  `obj_type` int(11) NOT NULL DEFAULT '0',
  `motherid` varchar(32) DEFAULT NULL,
  `mother_obj_id` int(11) NOT NULL DEFAULT '0',
  `mother_obj_type` int(11) NOT NULL DEFAULT '0',
  `updatetimestamp` int(11) NOT NULL DEFAULT '0',
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `contentmap_idx2` (`contentid`),
  KEY `obj_type` (`obj_type`),
  KEY `contentmap_idx5` (`motherid`,`contentid`),
  KEY `contentmap_idx3` (`obj_id`)
) ENGINE=MyISAM AUTO_INCREMENT=624987 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contentmap`
--

LOCK TABLES `contentmap` WRITE;
/*!40000 ALTER TABLE `contentmap` DISABLE KEYS */;
/*!40000 ALTER TABLE `contentmap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contentobject`
--

DROP TABLE IF EXISTS `contentobject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `contentobject` (
  `type` int(11) NOT NULL DEFAULT '0',
  `name` varchar(32) DEFAULT NULL,
  `exclude_versioning` int(11) NOT NULL DEFAULT '0',
  `id_counter` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`type`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contentobject`
--

LOCK TABLES `contentobject` WRITE;
/*!40000 ALTER TABLE `contentobject` DISABLE KEYS */;
INSERT INTO `contentobject` VALUES (12000,'person',0,30307),(13000,'company',0,0),(14000,'adresse',0,0);
/*!40000 ALTER TABLE `contentobject` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contentstatus`
--

DROP TABLE IF EXISTS `contentstatus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `contentstatus` (
  `name` varchar(255) NOT NULL DEFAULT '',
  `stringvalue` mediumtext,
  `intvalue` int(11) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contentstatus`
--

LOCK TABLES `contentstatus` WRITE;
/*!40000 ALTER TABLE `contentstatus` DISABLE KEYS */;
/*!40000 ALTER TABLE `contentstatus` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2012-10-18 17:36:31
