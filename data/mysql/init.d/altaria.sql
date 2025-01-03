CREATE DATABASE altaria DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_general_ci;

use altaria;;



DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uid` bigint DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `aaaaaa_uid_index` (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=1858044708940967937 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



LOCK TABLES `category` WRITE;
INSERT INTO `category` VALUES (1857789765671759872,1,'asd'),(1857796112928813056,1,'aaa'),(1857796735443197952,1,'aaaa'),(1857796795253972992,1,'aadaede'),(1857796845900193792,1,'aaaaaaa'),(1857797876600025088,1,'aaaaa'),(1857797934179430400,1,'a1q'),(1857797978718744576,1,'aa2aa'),(1857797991335211008,1,'3333'),(1857798031378231296,1,'56454'),(1857825280265580544,1,'1111new'),(1857841528865116160,1,'qqq');
UNLOCK TABLES;



DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nid` bigint NOT NULL,
  `uid` bigint NOT NULL,
  `pid` bigint DEFAULT NULL,
  `to_id` bigint DEFAULT NULL,
  `content` varchar(200) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `nid_index` (`nid`),
  KEY `pid_index` (`pid`)
) ENGINE=InnoDB AUTO_INCREMENT=1860616682108215297 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `comments` WRITE;
INSERT INTO `comments` VALUES (1858201370326777856,1858061143826288640,1,NULL,NULL,'111','2024-11-18 01:31:45'),(1858201390992113664,1858061143826288640,1,NULL,NULL,'222','2024-11-18 01:31:50'),(1858201408583024640,1858061143826288640,1,1858201370326777856,1858201370326777856,'122','2024-11-18 01:31:54'),(1858201429864923136,1858061143826288640,1,1858201370326777856,1858201370326777856,'122','2024-11-18 01:31:59'),(1858201476153262080,1858061143826288640,1,1858201370326777856,1858201370326777856,'122','2024-11-18 01:32:10'),(1858201497670041600,1858061143826288640,1,1858201370326777856,1858201370326777856,'233','2024-11-18 01:32:15'),(1858201522080890880,1858061143826288640,1,1858201370326777856,1858201429864923136,'123','2024-11-18 01:32:21'),(1858201856643743744,1858061143826288640,1,1858201390992113664,1858201390992113664,'211','2024-11-18 01:33:41'),(1858201898838441984,1858061143826288640,1,NULL,NULL,'123','2024-11-18 01:33:51'),(1858201927317766144,1858061143826288640,1,1858201898838441984,1858201898838441984,'233','2024-11-18 01:33:58'),(1858202293195292672,1858061143826288640,1,NULL,NULL,'dot','2024-11-18 01:35:25'),(1858202374833225728,1858061143826288640,1,1858202293195292672,1858202293195292672,'123dot','2024-11-18 01:35:45'),(1858202638323597312,1858061143826288640,1,NULL,NULL,'123','2024-11-18 01:36:47'),(1858203179296538624,1858061143826288640,1,NULL,NULL,'doc','2024-11-18 01:38:56'),(1858203254861119488,1858061143826288640,1,1858203179296538624,1858203179296538624,'doc2','2024-11-18 01:39:14'),(1858203472042180608,1858061143826288640,1,NULL,NULL,'dox','2024-11-18 01:40:06'),(1858203497254141952,1858061143826288640,1,1858203472042180608,1858203472042180608,'dox','2024-11-18 01:40:12'),(1858203682042593280,1858061143826288640,1,NULL,NULL,'awq','2024-11-18 01:40:56'),(1858203724178571264,1858061143826288640,1,1858203682042593280,1858203682042593280,'qq','2024-11-18 01:41:06'),(1858204000474152960,1858061143826288640,1,NULL,NULL,'docp\n','2024-11-18 01:42:12'),(1858204047089647616,1858061143826288640,1,1858204000474152960,1858204000474152960,'asas','2024-11-18 01:42:23'),(1858204349704486912,1858061143826288640,1,NULL,NULL,'bom','2024-11-18 01:43:35'),(1858204372697661440,1858061143826288640,1,1858204349704486912,1858204349704486912,'bom','2024-11-18 01:43:41'),(1858204437310914560,1858061143826288640,1,1858204349704486912,1858204349704486912,'bom','2024-11-18 01:43:56'),(1858204551760887808,1858061143826288640,1,NULL,NULL,'aaaaaaaaaaaaa','2024-11-18 01:44:24'),(1858204568424857600,1858061143826288640,1,1858204551760887808,1858204551760887808,'aaaaaaaaaaaaaaaaaa','2024-11-18 01:44:28'),(1858204585764110336,1858061143826288640,1,NULL,NULL,'aaaaaaaaaaa','2024-11-18 01:44:32'),(1858204597394915328,1858061143826288640,1,NULL,NULL,'ccccc','2024-11-18 01:44:34'),(1858204614042107904,1858061143826288640,1,1858204597394915328,1858204597394915328,'cccccc','2024-11-18 01:44:38'),(1858204630521528320,1858061143826288640,1,NULL,NULL,'cccccccc','2024-11-18 01:44:42'),(1858204767197118464,1858061143826288640,1,1858204630521528320,1858204630521528320,'ccccccc','2024-11-18 01:45:15'),(1858204781206093824,1858061143826288640,1,1858204630521528320,1858204767197118464,'ccccccccc','2024-11-18 01:45:18'),(1858204796485943296,1858061143826288640,1,1858204630521528320,1858204781206093824,'cccccccccc','2024-11-18 01:45:22'),(1858204923023900672,1858061143826288640,5,1858201370326777856,1858201370326777856,'weqedq','2024-11-18 01:45:52'),(1858204941508198400,1858061143826288640,5,1858201370326777856,1858201522080890880,'dqwq','2024-11-18 01:45:56'),(1858204958822285312,1858061143826288640,5,1858201370326777856,1858204941508198400,'wdqqw','2024-11-18 01:46:01'),(1858204980217430016,1858061143826288640,5,NULL,NULL,'wqdw','2024-11-18 01:46:06'),(1859257686872883200,1857815414318874624,1,NULL,NULL,'bbb','2024-11-20 23:29:11'),(1859258463100141568,1857815414318874624,1,NULL,NULL,'ccc','2024-11-20 23:32:16'),(1859258937719193600,1857815414318874624,1,NULL,NULL,'aaa','2024-11-20 23:34:09'),(1859260314398818304,1857815414318874624,1,NULL,NULL,'aaa','2024-11-20 23:39:37'),(1859261056920612864,1857815414318874624,1,NULL,NULL,'aaa','2024-11-20 23:42:34'),(1860616667121967104,1857815414318874624,1,1859257686872883200,1859257686872883200,'121','2024-11-24 17:29:17'),(1860616682108215296,1857815414318874624,1,1859257686872883200,1860616667121967104,'211','2024-11-24 17:29:20');
UNLOCK TABLES;



DROP TABLE IF EXISTS `file`;
CREATE TABLE `file` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uid` bigint NOT NULL,
  `pid` bigint DEFAULT '0',
  `type` tinyint NOT NULL,
  `status` tinyint DEFAULT '0',
  `transformed` int DEFAULT '0',
  `file_name` varchar(100) NOT NULL,
  `size` bigint DEFAULT '0',
  `url` varchar(255) DEFAULT NULL,
  `md5` varchar(255) DEFAULT NULL,
  `cover` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `index_userid` (`uid`),
  KEY `index_pid` (`pid`),
  KEY `index_md5` (`md5`),
  KEY `index_status` (`status`),
  KEY `index_type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=1860648044731699201 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


LOCK TABLES `file` WRITE;
INSERT INTO `file` VALUES (0,0,-1,0,0,0,'全部文件',0,NULL,NULL,NULL,'2024-10-13 17:10:16','2024-10-13 17:10:16'),(1858897187132542976,1,0,2,0,0,'3494146119kePANfVadk.mp4',56516103,'d090e85c445c4733b0d64481b54d526b.mp4','5068e1e23619d41eb7e1c637537e51be','d090e85c445c4733b0d64481b54d526b_.jpg','2024-11-19 23:36:40','2024-11-24 19:26:15'),(1860645937836331008,1,0,1,0,0,'altaria.png(Mx)',43063,'60729f78409c4f68aeab7f2dfc74ee29.png','950f52eb056aa2a31f64cc265a049505','60729f78409c4f68aeab7f2dfc74ee29_.jpg','2024-11-24 19:25:35','2024-11-24 19:36:30'),(1860646416406417408,1,0,1,0,0,'back.png',44626,'d109d1ee26214429bcabb472e6f10eed.png','3a542619ef173d5f80380ab4dca6de4a','d109d1ee26214429bcabb472e6f10eed_.jpg','2024-11-24 19:27:29','2024-11-24 19:36:30'),(1860646433619841024,1,0,1,0,0,'back.png(0m)',44626,'d109d1ee26214429bcabb472e6f10eed.png','3a542619ef173d5f80380ab4dca6de4a','d109d1ee26214429bcabb472e6f10eed_.jpg','2024-11-24 19:27:33','2024-11-24 19:36:30'),(1860646450803904512,1,0,1,0,0,'back.png(iI)',44626,'d109d1ee26214429bcabb472e6f10eed.png','3a542619ef173d5f80380ab4dca6de4a','d109d1ee26214429bcabb472e6f10eed_.jpg','2024-11-24 19:27:37','2024-11-24 19:36:30'),(1860646468432564224,1,0,1,0,0,'back.jpg',29056,'c6d68a89e4cd43cd897475a1cfbe84d3.jpg','5d413ee980ad4fe63427b6b8aa262e5b','c6d68a89e4cd43cd897475a1cfbe84d3_.jpg','2024-11-24 19:27:41','2024-11-24 19:36:30'),(1860646502171545600,1,0,1,0,0,'wp3087455-altaria-hd-wallpapers-removebg-preview (1).png',50572,'945054247c1c42b3a60eadb4d12c144d.png','95f9f6048c3d311f2cee19673a0a2cfc','945054247c1c42b3a60eadb4d12c144d_.jpg','2024-11-24 19:27:49','2024-11-24 19:36:30'),(1860646515874336768,1,0,1,0,0,'wp3087455-altaria-hd-wallpapers-removebg-preview (1).png(dI)',50572,'945054247c1c42b3a60eadb4d12c144d.png','95f9f6048c3d311f2cee19673a0a2cfc','945054247c1c42b3a60eadb4d12c144d_.jpg','2024-11-24 19:27:53','2024-11-24 19:36:30'),(1860646561344786432,1,0,1,0,0,'wp3087455-altaria-hd-wallpapers-removebg-preview (1).png(CY)',50572,'945054247c1c42b3a60eadb4d12c144d.png','95f9f6048c3d311f2cee19673a0a2cfc','945054247c1c42b3a60eadb4d12c144d_.jpg','2024-11-24 19:28:04','2024-11-24 19:36:30'),(1860647327363108864,1,0,1,0,0,'wp3087455-altaria-hd-wallpapers-removebg-preview (1).png(iR)',50572,'945054247c1c42b3a60eadb4d12c144d.png','95f9f6048c3d311f2cee19673a0a2cfc','945054247c1c42b3a60eadb4d12c144d_.jpg','2024-11-24 19:31:06','2024-11-24 19:36:30'),(1860647445621510144,1,0,1,0,0,'back.png(Qx)',44626,'d109d1ee26214429bcabb472e6f10eed.png','3a542619ef173d5f80380ab4dca6de4a','d109d1ee26214429bcabb472e6f10eed_.jpg','2024-11-24 19:31:34','2024-11-24 19:36:30'),(1860647987068407808,1,0,1,0,0,'wp3087455-altaria-hd-wallpapers-removebg-preview (1).png(4W)',50572,'945054247c1c42b3a60eadb4d12c144d.png','95f9f6048c3d311f2cee19673a0a2cfc','945054247c1c42b3a60eadb4d12c144d_.jpg','2024-11-24 19:33:44','2024-11-24 19:36:30'),(1860648044731699200,1,0,7,0,0,'.gitignore',231,'f97941c53d204535a62fd0e68f57423e.gitignore','57bb31095d802f29058cab98d2250bb8',NULL,'2024-11-24 19:33:57','2024-11-24 19:36:30');
UNLOCK TABLES;


DROP TABLE IF EXISTS `note`;
CREATE TABLE `note` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uid` bigint NOT NULL,
  `cid` bigint DEFAULT NULL,
  `title` varchar(100) NOT NULL,
  `text` text NOT NULL,
  `is_private` tinyint DEFAULT '1',
  `comment_count` int DEFAULT '0',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `index_uid` (`uid`),
  KEY `index_cid` (`cid`)
) ENGINE=InnoDB AUTO_INCREMENT=1860341120936493057 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `note` WRITE;
INSERT INTO `note` VALUES (1857815229178101760,1,1857797978718744576,'DefaultTitle','Hello!aaa',1,0,'2024-11-16 23:57:21','2024-11-16 23:57:21'),(1857815414318874624,1,NULL,'DefaultTitleAA@A','Hello!aaa\n\ndqddeqw\nwdqd\n```\nqqq\nq\nq\nqq\n```',0,7,'2024-11-23 17:54:02','2024-11-16 23:58:05'),(1857819271010758656,1,1857841528865116160,'DefaultTitleQQ','Hello!wdefgfhj\n# qqq\n# aaaa',1,0,'2024-11-17 01:43:50','2024-11-17 00:13:25'),(1857825402009448448,1,NULL,'11111','Hello!aaaaaaaaaaaa',0,7,'2024-11-20 01:08:22','2024-11-17 00:37:47'),(1857833160930828288,1,NULL,'DefaultTitle','Hello!sadcsvb vsfdasxdcv sdssxc',0,0,'2024-11-20 00:11:47','2024-11-17 01:08:37'),(1857836835078230016,1,1857789765671759872,'DefaultTitle','aaaa',1,0,'2024-11-17 01:27:38','2024-11-17 01:23:13'),(1857840879276478464,1,1857825280265580544,'DefaultTitle','sqwefvfecdvfvedevf vfvedvf vfveddevfv fved3eevf vfve3eef\n# cw\n## casc\n### fav\n```\nsa\nas\nsave\nvsea\n```',1,0,'2024-11-17 17:32:16','2024-11-17 01:39:17'),(1857840950248296448,1,1857789765671759872,'DefaultTitleASD','',1,0,'2024-11-17 01:39:34','2024-11-17 01:39:34'),(1857841504034836480,1,NULL,'DefaultTitle','q',0,0,'2024-11-20 00:12:14','2024-11-17 01:41:46'),(1857841555872239616,1,1857841528865116160,'DefaultTitle','qqqqq',1,0,'2024-11-17 01:41:58','2024-11-17 01:41:58'),(1858058169410441216,1,1857841528865116160,'DefaultTitleaaa','axqwxqe',0,0,'2024-11-17 16:03:15','2024-11-17 16:02:43'),(1858061143826288640,1,NULL,'test','## 😲 md-editor-v3\n\nMarkdown 编辑器，vue3 版本，使用 jsx 模板 和 typescript 开发，支持切换主题、prettier 美化文本等。\n\n### 🤖 基本演示\n\n**加粗**，<u>下划线</u>，_斜体_，~~删除线~~，上标^26^，下标~1~，`inline code`，[超链接](https://github.com/imzbf)\n\n> 引用：《I Have a Dream》\n\n1. So even though we face the difficulties of today and tomorrow, I still have a dream.\n2. It is a dream deeply rooted in the American dream.\n3. I have a dream that one day this nation will rise up.\n\n- [ ] 周五\n- [ ] 周六\n- [x] 周天\n\n![图片](https://imzbf.github.io/md-editor-rt/imgs/mark_emoji.gif)\n\n## 🤗 代码演示\n\n```vue\n<template>\n  <MdEditor v-model=\"text\" />\n</template>\n\n<script setup>\nimport { ref } from \'vue\';\nimport { MdEditor } from \'md-editor-v3\';\nimport \'md-editor-v3/lib/style.css\';\n\nconst text = ref(\'Hello Editor!\');\n</script>\n```\n\n## 🖨 文本演示\n\n依照普朗克长度这项单位，目前可观测的宇宙的直径估计值（直径约 930 亿光年，即 8.8 × 10<sup>26</sup> 米）即为 5.4 × 10<sup>61</sup>倍普朗克长度。而可观测宇宙体积则为 8.4 × 10<sup>184</sup>立方普朗克长度（普朗克体积）。\n\n## 📈 表格演示\n\n| 表头1  |  表头2   |  表头3 |\n| :----- | :------: | -----: |\n| 左对齐 | 中间对齐 | 右对齐 |\n\n## 📏 公式\n\n行内：$x+y^{2x}$\n\n$$\n\\sqrt[3]{x}\n$$\n\n## 🧬 图表\n\n```mermaid\nflowchart TD\n  Start --> Stop\n```\n\n## 🪄 提示\n\n!!! note 支持的类型\n\nnote、abstract、info、tip、success、question、warning、failure、danger、bug、example、quote、hint、caution、error、attention\n\n!!!\n\n## ☘️ 占个坑@！\n',0,37,'2024-11-20 00:10:57','2024-11-17 16:14:32');
UNLOCK TABLES;


DROP TABLE IF EXISTS `share`;

CREATE TABLE `share` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uid` bigint NOT NULL,
  `name` varchar(50) NOT NULL,
  `fids` text,
  `visit` bigint DEFAULT '0',
  `expire` datetime NOT NULL,
  `url` varchar(255) DEFAULT NULL,
  `sign` varchar(255) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `index_uid` (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=1860631065710297089 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


LOCK TABLES `share` WRITE;
INSERT INTO `share` VALUES (1860339489440362496,1,'3494146119kePANfVadk.mp4等1个文件','1858897187132542976',0,'2024-11-24 23:07:52','http://localhost:9000/shareDetail/1860339489440362496','1111','2024-11-23 15:07:52'),(1860630121274671104,1,'3494146119kePANfVadk.mp4等1个文件','1858897187132542976',0,'2024-11-25 18:22:44','1860630121274671104','6551','2024-11-24 10:22:44'),(1860630741947777024,1,'3494146119kePANfVadk.mp4等1个文件','1858897187132542976',0,'2024-11-25 18:25:12','1860630741947777024','1111','2024-11-24 10:25:12'),(1860631065710297088,1,'3494146119kePANfVadk.mp4等1个文件','1858897187132542976',0,'2024-11-25 18:26:30','1860631065710297088','3725','2024-11-24 10:26:29');
UNLOCK TABLES;



DROP TABLE IF EXISTS `space`;
CREATE TABLE `space` (
  `id` int NOT NULL AUTO_INCREMENT,
  `uid` bigint NOT NULL,
  `note_coount` int DEFAULT '0',
  `use_space` bigint DEFAULT '0',
  `total_space` bigint DEFAULT '1073741824',
  PRIMARY KEY (`id`),
  UNIQUE KEY `space_pk` (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



LOCK TABLES `space` WRITE;
INSERT INTO `space` VALUES (8,5,0,0,1073741824),(9,8,0,0,1073741824),(10,1841074506819571712,0,0,1073741824),(11,1841139786635677696,0,0,1073741824),(12,1,12,57381297,1073741824);
UNLOCK TABLES;



DROP TABLE IF EXISTS `tb_user`;

CREATE TABLE `tb_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `user_name` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL DEFAULT '123456' COMMENT '用户密码',
  `email` varchar(50) NOT NULL COMMENT '邮箱',
  `nick_name` varchar(50) NOT NULL COMMENT '昵称',
  `avatar` varchar(200) DEFAULT NULL COMMENT '头像',
  `role` int NOT NULL DEFAULT '0' COMMENT '权限',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `tb_user_email` (`email`) COMMENT '邮箱索引',
  UNIQUE KEY `unidx_username` (`user_name`),
  KEY `uindex_uame_pwd` (`user_name`,`password`) COMMENT '用户名密码索引'
) ENGINE=InnoDB AUTO_INCREMENT=1841139786635677697 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';



LOCK TABLES `tb_user` WRITE;
INSERT INTO `tb_user` VALUES (1,'takaki','e10adc3949ba59abbe56e057f20f883e','123@qq.com','ti','2f7a94adbd404d4d907ea17b33e0768b.webp',0,'2024-09-21 16:35:37','2024-11-07 19:40:53'),(5,'xiao','e10adc3949ba59abbe56e057f20f883e','2918628219@qq.com','~~','e084921af3214f63928e94f6dcbec088.jpeg',1,'2024-09-22 18:07:23','2024-11-09 16:56:05'),(8,'kiss','7cee193d865f0347cd6be41287d3ed7e','1234567@qq.com','cloud用户_qW8d3','3494146119fHjZSCaucS_.jpeg',0,'2024-09-24 21:30:04','2024-09-24 21:30:04'),(1841074506819571712,'saber','e10adc3949ba59abbe56e057f20f883e','1234@qq.com','i3','924c66cdc32046fd9eab79615d250ecb.webp',0,'2024-09-21 16:36:31','2024-10-21 22:02:55'),(1841139786635677696,'altaria','e10adc3949ba59abbe56e057f20f883e','2744974948@qq.com','cloud用户_NCvvE','3494146119fHjZSCaucS_.jpeg',0,'2024-10-01 23:35:07','2024-10-01 23:35:07');
UNLOCK TABLES;



DROP TABLE IF EXISTS `undo_log`;

CREATE TABLE `undo_log` (
  `branch_id` bigint NOT NULL COMMENT 'branch transaction id',
  `xid` varchar(128) NOT NULL COMMENT 'global transaction id',
  `context` varchar(128) NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` longblob NOT NULL COMMENT 'rollback info',
  `log_status` int NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created` datetime(6) NOT NULL COMMENT 'create datetime',
  `log_modified` datetime(6) NOT NULL COMMENT 'modify datetime',
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AT transaction mode undo table';



LOCK TABLES `undo_log` WRITE;

UNLOCK TABLES;