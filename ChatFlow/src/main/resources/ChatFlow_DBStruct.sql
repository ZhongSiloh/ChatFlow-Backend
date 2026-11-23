-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: chatflow
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cf_ai_chat_message`
--

DROP TABLE IF EXISTS `cf_ai_chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cf_ai_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '消息所属会话 ID',
  `user_id` int NOT NULL COMMENT '消息所属用户 ID',
  `role` varchar(20) NOT NULL COMMENT '消息角色，user 或 assistant',
  `content` mediumtext NOT NULL COMMENT '消息正文',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_message_session_created` (`session_id`,`created_at`),
  CONSTRAINT `fk_ai_message_session` FOREIGN KEY (`session_id`) REFERENCES `cf_ai_chat_session` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 消息表：保存用户提问和 AI 回答';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cf_ai_chat_session`
--

DROP TABLE IF EXISTS `cf_ai_chat_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cf_ai_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户 ID',
  `title` varchar(120) NOT NULL DEFAULT '新对话' COMMENT '会话标题；新建会话时使用“新对话”作为默认标题',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会话创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_session_user_updated` (`user_id`,`updated_at`)
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 会话表：每条记录代表一个用户与 AI 的会话';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cf_chat_message`
--

DROP TABLE IF EXISTS `cf_chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cf_chat_message` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增，兼作排序和分页游标',
  `session_key` varchar(64) NOT NULL COMMENT '会话标识：私聊 P_小id_大id；群 G_群id；聊天室 R_房间id',
  `chat_type` tinyint NOT NULL COMMENT '1私聊 2群聊 3聊天室',
  `sender_id` bigint unsigned NOT NULL,
  `target_id` bigint unsigned NOT NULL COMMENT '私聊=接收人id；群聊=群id；聊天室=房间id',
  `msg_type` tinyint NOT NULL DEFAULT '1' COMMENT '1文本 2图片 3文件 4系统消息',
  `content` text NOT NULL,
  `file_url` varchar(255) DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_key`,`id`),
  KEY `idx_session_time` (`session_key`,`created_at`),
  KEY `idx_sender` (`sender_id`,`created_at`),
  KEY `idx_type_time` (`chat_type`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cf_chat_read_state`
--

DROP TABLE IF EXISTS `cf_chat_read_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cf_chat_read_state` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `session_key` varchar(64) NOT NULL,
  `last_read_msg_id` bigint unsigned NOT NULL DEFAULT '0',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_session` (`user_id`,`session_key`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会话已读位置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cf_chat_room`
--

DROP TABLE IF EXISTS `cf_chat_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cf_chat_room` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1开放 0关闭',
  `created_by` bigint unsigned DEFAULT NULL COMMENT '创建者（管理员）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='在线群聊室';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cf_chat_room_member`
--

DROP TABLE IF EXISTS `cf_chat_room_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cf_chat_room_member` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `room_id` bigint unsigned NOT NULL,
  `user_id` bigint unsigned NOT NULL,
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `leave_time` datetime DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1在聊天室 0已离开',
  `active_key` tinyint GENERATED ALWAYS AS (if((`status` = 1),1,NULL)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_active` (`room_id`,`user_id`,`active_key`),
  KEY `idx_user` (`user_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天室成员（用于记录可见范围）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cf_friend_request`
--

DROP TABLE IF EXISTS `cf_friend_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cf_friend_request` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `from_user_id` bigint unsigned NOT NULL COMMENT '申请人',
  `to_user_id` bigint unsigned NOT NULL COMMENT '被申请人',
  `message` varchar(200) DEFAULT NULL COMMENT '验证消息',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待处理 1已同意 2已拒绝',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `handled_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_to_user` (`to_user_id`,`status`),
  KEY `idx_from_user` (`from_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='好友申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cf_friendship`
--

DROP TABLE IF EXISTS `cf_friendship`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cf_friendship` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL COMMENT '当前用户',
  `friend_id` bigint unsigned NOT NULL COMMENT '好友',
  `remark` varchar(50) DEFAULT NULL COMMENT '好友备注（只有user_id看得到）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1正常 0已删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成为好友时间，私聊记录可见起点',
  `deleted_at` datetime DEFAULT NULL COMMENT '删除好友时间',
  `active_key` tinyint GENERATED ALWAYS AS (if((`status` = 1),1,NULL)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_active` (`user_id`,`friend_id`,`active_key`),
  KEY `idx_user_status` (`user_id`,`status`),
  KEY `idx_friend` (`friend_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='好友关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cf_group`
--

DROP TABLE IF EXISTS `cf_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cf_group` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `group_code` varchar(20) NOT NULL COMMENT '群号，唯一',
  `group_name` varchar(50) NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL COMMENT '群简介/公告',
  `owner_id` bigint unsigned NOT NULL COMMENT '群主',
  `max_members` int NOT NULL DEFAULT '200',
  `join_mode` tinyint NOT NULL DEFAULT '1' COMMENT '0自由加入 1需群主审核',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1正常 0已解散',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `dissolved_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_code` (`group_code`),
  KEY `idx_owner` (`owner_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='群表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cf_group_join_request`
--

DROP TABLE IF EXISTS `cf_group_join_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cf_group_join_request` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `group_id` bigint unsigned NOT NULL,
  `user_id` bigint unsigned NOT NULL COMMENT '申请人',
  `message` varchar(200) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待处理 1同意 2拒绝',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `handled_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_group_status` (`group_id`,`status`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入群申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cf_group_member`
--

DROP TABLE IF EXISTS `cf_group_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cf_group_member` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `group_id` bigint unsigned NOT NULL,
  `user_id` bigint unsigned NOT NULL,
  `role` tinyint NOT NULL DEFAULT '0' COMMENT '0普通成员 1群主',
  `group_nick` varchar(50) DEFAULT NULL COMMENT '群昵称',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间，记录可见起点',
  `leave_time` datetime DEFAULT NULL COMMENT '退出时间，记录可见终点',
  `leave_type` tinyint DEFAULT NULL COMMENT '1主动退群 2被踢 3群解散',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1在群中 0已离开',
  `active_key` tinyint GENERATED ALWAYS AS (if((`status` = 1),1,NULL)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_active` (`group_id`,`user_id`,`active_key`),
  KEY `idx_user_status` (`user_id`,`status`),
  KEY `idx_group_status` (`group_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='群成员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cf_user`
--

DROP TABLE IF EXISTS `cf_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cf_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_code` varchar(20) NOT NULL COMMENT '用户代码，用于加好友，唯一',
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT 'BCrypt加密后的密码',
  `nickname` varchar(50) NOT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `role` varchar(20) NOT NULL DEFAULT 'USER' COMMENT 'USER普通用户 / ADMIN管理员',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1正常 0禁用',
  `last_login_time` datetime DEFAULT NULL,
  `last_active_time` datetime DEFAULT NULL COMMENT '最后在线时间（离线时更新）',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0否 1是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_code` (`user_code`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role_status` (`role`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-22 14:07:29
