CREATE TABLE IF NOT EXISTS `agent` (
    `id` bigint NOT NULL,
    `name` varchar(128) NOT NULL,
    `description` varchar(512) DEFAULT NULL,
    `avatar` varchar(512) DEFAULT NULL,
    `systemPrompt` text NOT NULL,
    `toolIds` varchar(1024) DEFAULT NULL COMMENT 'JSON array of enabled tool IDs',
    `categoryId` bigint DEFAULT NULL,
    `tags` varchar(512) DEFAULT NULL COMMENT 'comma-separated tags',
    `isPublic` tinyint NOT NULL DEFAULT 0 COMMENT '0=private 1=public',
    `priceType` varchar(16) NOT NULL DEFAULT 'free' COMMENT 'free/points',
    `price` int NOT NULL DEFAULT 0 COMMENT 'points price',
    `userId` bigint NOT NULL,
    `useCount` int NOT NULL DEFAULT 0,
    `cloneCount` int NOT NULL DEFAULT 0,
    `rating` decimal(2,1) NOT NULL DEFAULT 0.0,
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '0=draft 1=published 2=off-shelf',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `isDelete` tinyint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_userId` (`userId`),
    KEY `idx_categoryId` (`categoryId`),
    KEY `idx_isPublic_status` (`isPublic`, `status`),
    KEY `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent table';

CREATE TABLE IF NOT EXISTS `agent_category` (
    `id` bigint NOT NULL,
    `name` varchar(64) NOT NULL,
    `icon` varchar(256) DEFAULT NULL,
    `sortOrder` int NOT NULL DEFAULT 0,
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent category table';

INSERT IGNORE INTO `agent_category` (`id`, `name`, `sortOrder`) VALUES
(1, '编程开发', 1), (2, '写作创作', 2), (3, '数据分析', 3),
(4, '设计创意', 4), (5, '学习教育', 5), (6, '生活助手', 6),
(7, '办公效率', 7), (8, '其他', 8);

CREATE TABLE IF NOT EXISTS `agent_review` (
    `id` bigint NOT NULL,
    `agentId` bigint NOT NULL,
    `userId` bigint NOT NULL,
    `rating` tinyint NOT NULL COMMENT '1-5',
    `content` varchar(512) DEFAULT NULL,
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `isDelete` tinyint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_agentId` (`agentId`),
    UNIQUE KEY `uk_agentId_userId` (`agentId`, `userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent review table';

CREATE TABLE IF NOT EXISTS `agent_usage` (
    `id` bigint NOT NULL,
    `agentId` bigint NOT NULL,
    `userId` bigint NOT NULL,
    `isFree` tinyint NOT NULL DEFAULT 0 COMMENT '0=paid 1=free trial',
    `pointsCost` int NOT NULL DEFAULT 0,
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agentId_userId` (`agentId`, `userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent usage table';

CREATE TABLE IF NOT EXISTS `user_points` (
    `id` bigint NOT NULL,
    `userId` bigint NOT NULL,
    `balance` int NOT NULL DEFAULT 0,
    `totalIncome` int NOT NULL DEFAULT 0,
    `totalExpense` int NOT NULL DEFAULT 0,
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User points table';

CREATE TABLE IF NOT EXISTS `user_points_log` (
    `id` bigint NOT NULL,
    `userId` bigint NOT NULL,
    `amount` int NOT NULL COMMENT 'positive=income negative=expense',
    `balanceAfter` int NOT NULL,
    `type` varchar(16) NOT NULL COMMENT 'recharge/consume/income',
    `description` varchar(256) DEFAULT NULL,
    `agentId` bigint DEFAULT NULL,
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_userId` (`userId`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User points log table';
