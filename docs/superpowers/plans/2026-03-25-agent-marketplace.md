# Agent Marketplace Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an AI agent marketplace where users create, publish, browse, chat with, clone, and export AI agents, with a virtual points payment system.

**Architecture:** Follow existing patterns — MyBatis-Flex entities/mappers, IService/ServiceImpl, BaseController pattern with `BaseResponse<T>`, SSE streaming via `Flux<ServerSentEvent<String>>`. New `AgentChatServiceFactory` dynamically builds LangChain4j AI services using agent's systemPrompt + selected tools.

**Tech Stack:** Spring Boot 3.5.4, Java 21, MyBatis-Flex 1.11.1, LangChain4j 1.1.0-beta7, Caffeine, Redis, MinIO, MySQL.

**Spec:** `docs/superpowers/specs/2026-03-25-agent-marketplace-design.md`

---

## File Structure Overview

```
src/main/java/com/chen/yuaicodemother/
├── model/
│   ├── entity/
│   │   ├── Agent.java
│   │   ├── AgentCategory.java
│   │   ├── AgentReview.java
│   │   ├── AgentUsage.java
│   │   ├── UserPoints.java
│   │   └── UserPointsLog.java
│   ├── dto/agent/
│   │   ├── AgentCreateRequest.java
│   │   ├── AgentUpdateRequest.java
│   │   ├── AgentPublishRequest.java
│   │   ├── AgentSquareQueryRequest.java
│   │   ├── AgentReviewSubmitRequest.java
│   │   └── AgentImportRequest.java
│   ├── dto/points/
│   │   └── PointsRechargeRequest.java
│   ├── vo/agent/
│   │   ├── AgentVO.java
│   │   └── AgentSquareVO.java
│   └── enums/
│       ├── AgentStatusEnum.java
│       ├── AgentPriceTypeEnum.java
│       └── PointsTypeEnum.java
├── mapper/
│   ├── AgentMapper.java
│   ├── AgentCategoryMapper.java
│   ├── AgentReviewMapper.java
│   ├── AgentUsageMapper.java
│   ├── UserPointsMapper.java
│   └── UserPointsLogMapper.java
├── service/
│   ├── AgentService.java
│   ├── AgentCategoryService.java
│   ├── AgentReviewService.java
│   ├── AgentUsageService.java
│   ├── UserPointsService.java
│   └── impl/
│       ├── AgentServiceImpl.java
│       ├── AgentCategoryServiceImpl.java
│       ├── AgentReviewServiceImpl.java
│       ├── AgentUsageServiceImpl.java
│       └── UserPointsServiceImpl.java
├── ai/
│   └── AgentChatServiceFactory.java
├── core/
│   └── AgentChatFacade.java
└── controller/
    ├── AgentController.java
    ├── AgentCategoryController.java
    └── UserPointsController.java
```

---

## Task 1: Database Tables + Entity Classes

**Files:**
- Create: `src/main/resources/sql/agent_marketplace.sql`
- Create: `src/main/java/com/chen/yuaicodemother/model/entity/Agent.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/entity/AgentCategory.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/entity/AgentReview.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/entity/AgentUsage.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/entity/UserPoints.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/entity/UserPointsLog.java`

### Step 1: Write SQL schema

```sql
-- agent_marketplace.sql
-- Run this against yu_ai_code_mother database

CREATE TABLE IF NOT EXISTS `agent` (
    `id` bigint NOT NULL,
    `name` varchar(128) NOT NULL,
    `description` varchar(512) DEFAULT NULL,
    `avatar` varchar(512) DEFAULT NULL,
    `systemPrompt` text NOT NULL,
    `toolIds` varchar(1024) DEFAULT NULL COMMENT '启用的工具ID列表，JSON数组',
    `categoryId` bigint DEFAULT NULL,
    `tags` varchar(512) DEFAULT NULL COMMENT '标签，逗号分隔',
    `isPublic` tinyint NOT NULL DEFAULT 0 COMMENT '0=私有 1=公开',
    `priceType` varchar(16) NOT NULL DEFAULT 'free' COMMENT 'free/points',
    `price` int NOT NULL DEFAULT 0 COMMENT '积分价格',
    `userId` bigint NOT NULL,
    `useCount` int NOT NULL DEFAULT 0,
    `cloneCount` int NOT NULL DEFAULT 0,
    `rating` decimal(2,1) NOT NULL DEFAULT 0.0,
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '0=草稿 1=已发布 2=已下架',
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `isDelete` tinyint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_userId` (`userId`),
    KEY `idx_categoryId` (`categoryId`),
    KEY `idx_isPublic_status` (`isPublic`, `status`),
    KEY `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体表';

CREATE TABLE IF NOT EXISTS `agent_category` (
    `id` bigint NOT NULL,
    `name` varchar(64) NOT NULL,
    `icon` varchar(256) DEFAULT NULL,
    `sortOrder` int NOT NULL DEFAULT 0,
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体分类表';

-- 预设分类
INSERT IGNORE INTO `agent_category` (`id`, `name`, `sortOrder`) VALUES
(1, '编程开发', 1),
(2, '写作创作', 2),
(3, '数据分析', 3),
(4, '设计创意', 4),
(5, '学习教育', 5),
(6, '生活助手', 6),
(7, '办公效率', 7),
(8, '其他', 8);

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体评价表';

CREATE TABLE IF NOT EXISTS `agent_usage` (
    `id` bigint NOT NULL,
    `agentId` bigint NOT NULL,
    `userId` bigint NOT NULL,
    `isFree` tinyint NOT NULL DEFAULT 0 COMMENT '0=付费 1=免费体验',
    `pointsCost` int NOT NULL DEFAULT 0,
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agentId_userId` (`agentId`, `userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体使用记录表';

CREATE TABLE IF NOT EXISTS `user_points` (
    `id` bigint NOT NULL,
    `userId` bigint NOT NULL,
    `balance` int NOT NULL DEFAULT 0,
    `totalIncome` int NOT NULL DEFAULT 0,
    `totalExpense` int NOT NULL DEFAULT 0,
    `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分表';

CREATE TABLE IF NOT EXISTS `user_points_log` (
    `id` bigint NOT NULL,
    `userId` bigint NOT NULL,
    `amount` int NOT NULL COMMENT '正数=收入 负数=支出',
    `balanceAfter` int NOT NULL,
    `type` varchar(16) NOT NULL COMMENT 'recharge/consume/income',
    `description` varchar(256) DEFAULT NULL,
    `agentId` bigint DEFAULT NULL,
    `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_userId` (`userId`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分流水表';
```

### Step 2: Create entity classes

Follow exact pattern from `App.java` — `@Table`, `@Id(keyType=Generator, value=KeyGenerators.snowFlakeId)`, `@Column`, Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor`, `implements Serializable`.

**Agent.java** — `@Table("agent")`, fields: `id(Long), name, description, avatar, systemPrompt, toolIds, categoryId(Long), tags, isPublic(Integer), priceType, price(Integer), userId(Long), useCount(Integer), cloneCount(Integer), rating(BigDecimal), status(Integer), createTime, updateTime, isDelete(Integer, isLogicDelete=true)`.

**AgentCategory.java** — `@Table("agent_category")`, fields: `id(Long), name, icon, sortOrder(Integer), createTime`.

**AgentReview.java** — `@Table("agent_review")`, fields: `id(Long), agentId(Long), userId(Long), rating(Integer), content, createTime, isDelete(Integer, isLogicDelete=true)`.

**AgentUsage.java** — `@Table("agent_usage")`, fields: `id(Long), agentId(Long), userId(Long), isFree(Integer), pointsCost(Integer), createTime`.

**UserPoints.java** — `@Table("user_points")`, fields: `id(Long), userId(Long), balance(Integer), totalIncome(Integer), totalExpense(Integer), updateTime`.

**UserPointsLog.java** — `@Table("user_points_log")`, fields: `id(Long), userId(Long), amount(Integer), balanceAfter(Integer), type, description, agentId(Long), createTime`.

### Step 3: Run SQL and verify

Run: `mysql -u root -pch yu_ai_code_mother < src/main/resources/sql/agent_marketplace.sql`

### Step 4: Commit

```bash
git add src/main/resources/sql/agent_marketplace.sql src/main/java/com/chen/yuaicodemother/model/entity/Agent*.java src/main/java/com/chen/yuaicodemother/model/entity/UserPoints*.java
git commit -m "feat(agent-marketplace): add database schema and entity classes"
```

---

## Task 2: Enum Classes

**Files:**
- Create: `src/main/java/com/chen/yuaicodemother/model/enums/AgentStatusEnum.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/enums/AgentPriceTypeEnum.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/enums/PointsTypeEnum.java`

### Step 1: Write enums

Follow exact pattern from `CodeGenTypeEnum.java` — `@Getter`, fields `text` and `value`, static `getEnumByValue(String)` method.

**AgentStatusEnum**: `DRAFT("草稿", "0"), PUBLISHED("已发布", "1"), OFF_SHELF("已下架", "2")`

**AgentPriceTypeEnum**: `FREE("免费", "free"), POINTS("积分", "points")`

**PointsTypeEnum**: `RECHARGE("充值", "recharge"), CONSUME("消费", "consume"), INCOME("收入", "income")`

### Step 2: Commit

```bash
git add src/main/java/com/chen/yuaicodemother/model/enums/Agent*.java src/main/java/com/chen/yuaicodemother/model/enums/PointsTypeEnum.java
git commit -m "feat(agent-marketplace): add enum classes for agent status, price type, points type"
```

---

## Task 3: Mapper Interfaces

**Files:**
- Create: `src/main/java/com/chen/yuaicodemother/mapper/AgentMapper.java`
- Create: `src/main/java/com/chen/yuaicodemother/mapper/AgentCategoryMapper.java`
- Create: `src/main/java/com/chen/yuaicodemother/mapper/AgentReviewMapper.java`
- Create: `src/main/java/com/chen/yuaicodemother/mapper/AgentUsageMapper.java`
- Create: `src/main/java/com/chen/yuaicodemother/mapper/UserPointsMapper.java`
- Create: `src/main/java/com/chen/yuaicodemother/mapper/UserPointsLogMapper.java`

### Step 1: Write mapper interfaces

Follow exact pattern from existing mappers — bare `extends BaseMapper<Entity>` with no custom methods.

```java
package com.chen.yuaicodemother.mapper;

import com.chen.yuaicodemother.model.entity.Agent;
import com.mybatisflex.core.BaseMapper;

public interface AgentMapper extends BaseMapper<Agent> {
}
```

Same pattern for all 6 mappers.

### Step 2: Commit

```bash
git add src/main/java/com/chen/yuaicodemother/mapper/Agent*.java src/main/java/com/chen/yuaicodemother/mapper/UserPoints*.java
git commit -m "feat(agent-marketplace): add mapper interfaces"
```

---

## Task 4: DTO and VO Classes

**Files:**
- Create: `src/main/java/com/chen/yuaicodemother/model/dto/agent/AgentCreateRequest.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/dto/agent/AgentUpdateRequest.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/dto/agent/AgentPublishRequest.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/dto/agent/AgentSquareQueryRequest.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/dto/agent/AgentReviewSubmitRequest.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/dto/agent/AgentImportRequest.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/dto/points/PointsRechargeRequest.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/vo/agent/AgentVO.java`
- Create: `src/main/java/com/chen/yuaicodemother/model/vo/agent/AgentSquareVO.java`

### Step 1: Write DTOs

Follow pattern from existing DTOs — `@Data`, `implements Serializable`, `serialVersionUID`.

**AgentCreateRequest**: `name(@NotBlank), description, avatar, systemPrompt(@NotBlank), toolIds, categoryId, tags`

**AgentUpdateRequest**: `id(@NotNull), name, description, avatar, systemPrompt, toolIds, categoryId, tags`

**AgentPublishRequest**: `id(@NotNull), isPublic, priceType, price`

**AgentSquareQueryRequest** extends `PageRequest`: `keyword, categoryId, sortField(default "rating"), sortOrder`

**AgentReviewSubmitRequest**: `agentId(@NotNull), rating(@NotNull @Min(1) @Max(5)), content`

**AgentImportRequest**: `configJson(@NotBlank)` — the raw JSON string of exported agent config

**PointsRechargeRequest**: `amount(@NotNull @Min(1))` — points to recharge

### Step 2: Write VOs

Follow pattern from `AppVO.java` — `@Data`, `implements Serializable`, nested `UserVO user`.

**AgentVO**: `id, name, description, avatar, systemPrompt, toolIds, categoryId, categoryName, tags, isPublic, priceType, price, userId, useCount, cloneCount, rating, status, createTime, updateTime, user(UserVO)`

**AgentSquareVO**: `id, name, description, avatar, categoryId, categoryName, tags, priceType, price, useCount, cloneCount, rating, user(UserVO), createTime` — excludes systemPrompt (don't expose to public)

### Step 3: Commit

```bash
git add src/main/java/com/chen/yuaicodemother/model/dto/agent/ src/main/java/com/chen/yuaicodemother/model/dto/points/ src/main/java/com/chen/yuaicodemother/model/vo/agent/
git commit -m "feat(agent-marketplace): add DTO and VO classes"
```

---

## Task 5: AgentCategoryService

**Files:**
- Create: `src/main/java/com/chen/yuaicodemother/service/AgentCategoryService.java`
- Create: `src/main/java/com/chen/yuaicodemother/service/impl/AgentCategoryServiceImpl.java`

### Step 1: Write service interface and implementation

Follow `AppService`/`AppServiceImpl` pattern.

**Interface** extends `IService<AgentCategory>`, method: `List<AgentCategory> listAllOrdered()` — returns all categories ordered by `sortOrder`.

**Implementation** — `listAllOrdered()` uses `QueryWrapper.create().orderBy("sortOrder", true)` and returns `list(queryWrapper)`.

### Step 2: Commit

```bash
git add src/main/java/com/chen/yuaicodemother/service/AgentCategoryService.java src/main/java/com/chen/yuaicodemother/service/impl/AgentCategoryServiceImpl.java
git commit -m "feat(agent-marketplace): add AgentCategoryService"
```

---

## Task 6: UserPointsService

**Files:**
- Create: `src/main/java/com/chen/yuaicodemother/service/UserPointsService.java`
- Create: `src/main/java/com/chen/yuaicodemother/service/impl/UserPointsServiceImpl.java`

### Step 1: Write service

**Interface** extends `IService<UserPoints>`, methods:
- `int getBalance(Long userId)` — get or create points record, return balance
- `boolean recharge(Long userId, int amount, String description)` — add points, log as RECHARGE
- `boolean consume(Long userId, int amount, Long agentId, String description)` — deduct points, log as CONSUME, throw if insufficient
- `boolean addIncome(Long userId, int amount, Long agentId, String description)` — add points, log as INCOME
- `Page<UserPointsLog> getPointsLogPage(Long userId, int pageNum, int pageSize)` — paginated log

**Implementation** key logic:
- `getBalance()`: Query by userId, if not exists create with 0 balance, return balance
- `consume()`: Check balance >= amount, then update `balance -= amount`, `totalExpense += amount`, insert log with negative amount
- `recharge()`: Update `balance += amount`, insert log with positive amount
- `addIncome()`: Update `balance += amount`, `totalIncome += amount`, insert log with positive amount
- All write operations use `@Transactional(rollbackFor = Exception.class)`
- Platform revenue share: `consume()` called with full price, `addIncome()` called with `price * 0.8` for creator

### Step 2: Commit

```bash
git add src/main/java/com/chen/yuaicodemother/service/UserPointsService.java src/main/java/com/chen/yuaicodemother/service/impl/UserPointsServiceImpl.java
git commit -m "feat(agent-marketplace): add UserPointsService with balance, recharge, consume, income"
```

---

## Task 7: AgentService (CRUD + Publish + Clone + Export/Import)

**Files:**
- Create: `src/main/java/com/chen/yuaicodemother/service/AgentService.java`
- Create: `src/main/java/com/chen/yuaicodemother/service/impl/AgentServiceImpl.java`

### Step 1: Write service interface

Extends `IService<Agent>`, methods:
- `Long createAgent(AgentCreateRequest request, User loginUser)` — create draft agent
- `void updateAgent(AgentUpdateRequest request, User loginUser)` — update, owner only
- `void deleteAgent(Long id, User loginUser)` — delete, owner or admin
- `void publishAgent(AgentPublishRequest request, User loginUser)` — set public + pricing + status=1
- `void offShelfAgent(Long id, User loginUser)` — set status=2, owner only
- `Page<AgentSquareVO> getSquarePage(AgentSquareQueryRequest request)` — public square list
- `AgentSquareVO getSquareDetail(Long id)` — public detail
- `AgentVO getAgentVO(Agent agent)` — single VO with user info
- `List<AgentVO> getAgentVOList(List<Agent> agents)` — batch VO
- `QueryWrapper getQueryWrapper(AgentSquareQueryRequest request)` — query builder
- `Long cloneAgent(Long agentId, User loginUser)` — clone to current user
- `String exportAgent(Long agentId, User loginUser)` — export config as JSON string
- `Long importAgent(String configJson, User loginUser)` — import from JSON
- `void incrementUseCount(Long agentId)` — atomic increment
- `void incrementCloneCount(Long agentId)` — atomic increment

### Step 2: Write service implementation

Follow `AppServiceImpl` pattern exactly.

**createAgent()**: Build Agent entity from request, set userId, status=0 (draft), isPublic=0, save. Return id.

**publishAgent()**: Load agent, verify owner, set isPublic/priceType/price/status=1, update.

**offShelfAgent()**: Load agent, verify owner, set status=2, update.

**getSquarePage()**: Query where `isPublic=1 AND status=1`, apply keyword search on `name`/`description`/`tags`, categoryId filter, ordering by sortField (rating/useCount/createTime). Convert to AgentSquareVO list using `getAgentVOList()` (but map to AgentSquareVO to exclude systemPrompt).

**getSquareDetail()**: Load agent where `isPublic=1 AND status=1`, convert to AgentSquareVO.

**cloneAgent()**: Load source agent, create new Agent copying all fields except: new snowflake id, userId=current user, useCount=0, cloneCount=0, rating=0, status=0 (draft), isPublic=0. Append "(克隆)" to name. Save. Increment source cloneCount. Return new id.

**exportAgent()**: Load agent, build JSON with fields: name, description, systemPrompt, toolIds, categoryId, tags. Return JSON string.

**importAgent()**: Parse JSON, create new Agent with parsed fields, userId=current user, status=0. Save. Return id.

**getAgentVO()**: Copy properties, load user by userId, set nested UserVO. Also set `categoryName` from AgentCategoryService.

**getQueryWrapper()**: Build QueryWrapper from AgentSquareQueryRequest fields — keyword uses `.like("name", keyword).or().like("description", keyword).or().like("tags", keyword)`, categoryId eq, isPublic=1, status=1, ordering.

### Step 3: Commit

```bash
git add src/main/java/com/chen/yuaicodemother/service/AgentService.java src/main/java/com/chen/yuaicodemother/service/impl/AgentServiceImpl.java
git commit -m "feat(agent-marketplace): add AgentService with CRUD, publish, clone, export/import"
```

---

## Task 8: AgentReviewService

**Files:**
- Create: `src/main/java/com/chen/yuaicodemother/service/AgentReviewService.java`
- Create: `src/main/java/com/chen/yuaicodemother/service/impl/AgentReviewServiceImpl.java`

### Step 1: Write service

**Interface** extends `IService<AgentReview>`, methods:
- `void submitReview(AgentReviewSubmitRequest request, User loginUser)` — submit review, update agent average rating
- `Page<AgentReview> getReviewPage(Long agentId, int pageNum, int pageSize)` — paginated reviews

**Implementation**:
- `submitReview()`: Check if user already reviewed (unique key). Save review. Recalculate agent's average rating from all reviews for this agentId. Update agent.rating.
- `getReviewPage()`: Query by agentId, paginate, order by createTime desc.

### Step 2: Commit

```bash
git add src/main/java/com/chen/yuaicodemother/service/AgentReviewService.java src/main/java/com/chen/yuaicodemother/service/impl/AgentReviewServiceImpl.java
git commit -m "feat(agent-marketplace): add AgentReviewService with submit and paginated listing"
```

---

## Task 9: AgentUsageService

**Files:**
- Create: `src/main/java/com/chen/yuaicodemother/service/AgentUsageService.java`
- Create: `src/main/java/com/chen/yuaicodemother/service/impl/AgentUsageServiceImpl.java`

### Step 1: Write service

**Interface** extends `IService<AgentUsage>`, methods:
- `boolean hasFreeTrial(Long agentId, Long userId)` — check uk_agentId_userId existence
- `void recordUsage(Long agentId, Long userId, boolean isFree, int pointsCost)` — insert usage record

**Implementation**:
- `hasFreeTrial()`: QueryWrapper on agentId + userId, return count > 0
- `recordUsage()`: Build AgentUsage entity, save. Call `agentService.incrementUseCount(agentId)`.

### Step 2: Commit

```bash
git add src/main/java/com/chen/yuaicodemother/service/AgentUsageService.java src/main/java/com/chen/yuaicodemother/service/impl/AgentUsageServiceImpl.java
git commit -m "feat(agent-marketplace): add AgentUsageService for free trial tracking"
```

---

## Task 10: AgentChatServiceFactory (Dynamic AI Service Builder)

**Files:**
- Create: `src/main/java/com/chen/yuaicodemother/ai/AgentChatServiceFactory.java`
- Create: `src/main/java/com/chen/yuaicodemother/ai/AgentChatService.java`

### Step 1: Create AgentChatService interface

LangChain4j declarative AI service interface. Unlike `AiCodeGeneratorService` which has fixed `@SystemMessage`, this one will be built dynamically.

```java
package com.chen.yuaicodemother.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface AgentChatService {
    TokenStream chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
```

### Step 2: Create AgentChatServiceFactory

Follow `AiCodeGeneratorServiceFactory` pattern — `@Configuration`, Caffeine cache, `AiServices.builder()`.

Key differences from existing factory:
- Cache key: `"agent:{agentId}:{userId}"` — per agent + per user
- System prompt: comes from `agent.getSystemPrompt()` (dynamic, not `@SystemMessage` annotation)
- Tools: filter from `toolManager.getAllTools()` based on agent's `toolIds` JSON array
- Chat memory: Redis-backed, key = `agent_chat:{agentId}:{userId}`, maxMessages=50
- Model: use `openAiStreamingChatModel` for agent chat (lighter than reasoning model)

```java
public AgentChatService getAgentChatService(Agent agent, Long userId) {
    String cacheKey = "agent:" + agent.getId() + ":" + userId;
    return serviceCache.get(cacheKey, key -> createAgentChatService(agent, userId));
}

private AgentChatService createAgentChatService(Agent agent, Long userId) {
    String memoryId = "agent_chat:" + agent.getId() + ":" + userId;

    MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
            .id(memoryId)
            .chatMemoryStore(redisChatMemoryStore)
            .maxMessages(50)
            .build();

    // Filter tools based on agent's toolIds
    Object[] selectedTools = filterTools(agent.getToolIds());

    return AiServices.builder(AgentChatService.class)
            .streamingChatModel(openAiStreamingChatModel)
            .chatMemoryProvider(memId -> chatMemory)
            .systemMessage(agent.getSystemPrompt())
            .tools(selectedTools)
            .build();
}

private Object[] filterTools(String toolIdsJson) {
    if (StrUtil.isBlank(toolIdsJson)) return new Object[0];
    List<String> enabledToolNames = JSONUtil.toList(toolIdsJson, String.class);
    BaseTool[] allTools = toolManager.getAllTools();
    return Arrays.stream(allTools)
            .filter(t -> enabledToolNames.contains(t.getToolName()))
            .toArray();
}
```

### Step 3: Commit

```bash
git add src/main/java/com/chen/yuaicodemother/ai/AgentChatService.java src/main/java/com/chen/yuaicodemother/ai/AgentChatServiceFactory.java
git commit -m "feat(agent-marketplace): add AgentChatServiceFactory for dynamic AI service building"
```

---

## Task 11: AgentChatFacade (SSE Streaming)

**Files:**
- Create: `src/main/java/com/chen/yuaicodemother/core/AgentChatFacade.java`

### Step 1: Write facade

Follow `AiCodeGeneratorFacade.processTokenStream()` pattern — convert `TokenStream` to `Flux<String>`.

```java
@Service
@Slf4j
public class AgentChatFacade {

    @Resource
    private AgentChatServiceFactory agentChatServiceFactory;

    public Flux<String> chat(Agent agent, Long userId, String userMessage) {
        AgentChatService chatService = agentChatServiceFactory.getAgentChatService(agent, userId);
        String memoryId = "agent_chat:" + agent.getId() + ":" + userId;
        TokenStream tokenStream = chatService.chat(memoryId, userMessage);

        return Flux.create(sink -> {
            tokenStream
                .onPartialResponse(partialResponse -> {
                    AiResponseMessage msg = new AiResponseMessage(partialResponse);
                    sink.next(JSONUtil.toJsonStr(msg));
                })
                .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                    ToolRequestMessage msg = new ToolRequestMessage(toolExecutionRequest);
                    sink.next(JSONUtil.toJsonStr(msg));
                })
                .onToolExecuted(toolExecution -> {
                    ToolExecutedMessage msg = new ToolExecutedMessage(toolExecution);
                    sink.next(JSONUtil.toJsonStr(msg));
                })
                .onCompleteResponse(response -> sink.complete())
                .onError(error -> sink.error(error))
                .start();
        });
    }
}
```

### Step 2: Commit

```bash
git add src/main/java/com/chen/yuaicodemother/core/AgentChatFacade.java
git commit -m "feat(agent-marketplace): add AgentChatFacade for SSE streaming agent chat"
```

---

## Task 12: AgentController

**Files:**
- Create: `src/main/java/com/chen/yuaicodemother/controller/AgentController.java`

### Step 1: Write controller

Follow `AppController` pattern exactly. `@RestController @RequestMapping("/agent")`.

Endpoints:

```java
@RestController
@RequestMapping("/agent")
public class AgentController {

    @Resource private AgentService agentService;
    @Resource private AgentReviewService agentReviewService;
    @Resource private AgentUsageService agentUsageService;
    @Resource private UserPointsService userPointsService;
    @Resource private UserService userService;
    @Resource private AgentChatFacade agentChatFacade;

    // --- Agent CRUD ---

    @PostMapping("/create")
    public BaseResponse<Long> createAgent(@Valid @RequestBody AgentCreateRequest request, HttpServletRequest httpRequest) { ... }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateAgent(@RequestBody AgentUpdateRequest request, HttpServletRequest httpRequest) { ... }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteAgent(@RequestBody DeleteRequest request, HttpServletRequest httpRequest) { ... }

    @GetMapping("/get/vo")
    public BaseResponse<AgentVO> getAgentVOById(long id) { ... }

    @PostMapping("/my/list/page")
    public BaseResponse<Page<AgentVO>> listMyAgentByPage(@RequestBody AgentSquareQueryRequest request, HttpServletRequest httpRequest) { ... }

    // --- Publish ---

    @PostMapping("/publish")
    public BaseResponse<Boolean> publishAgent(@RequestBody AgentPublishRequest request, HttpServletRequest httpRequest) { ... }

    @PostMapping("/offShelf")
    public BaseResponse<Boolean> offShelfAgent(@RequestBody DeleteRequest request, HttpServletRequest httpRequest) { ... }

    // --- Square ---

    @PostMapping("/square/list/page")
    public BaseResponse<Page<AgentSquareVO>> listSquareByPage(@RequestBody AgentSquareQueryRequest request) { ... }

    @GetMapping("/square/detail/{id}")
    public BaseResponse<AgentSquareVO> getSquareDetail(@PathVariable Long id) { ... }

    // --- Chat (SSE) ---

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatWithAgent(@RequestParam Long agentId,
                                                       @RequestParam String message,
                                                       HttpServletRequest httpRequest) {
        // 1. Get login user
        // 2. Load agent, verify isPublic=1 and status=1
        // 3. Check free trial: if not used before, record as free, skip payment
        //    if used before and priceType=points, check balance, consume points, give 80% to creator
        //    if priceType=free, just record usage
        // 4. Record usage via agentUsageService
        // 5. Call agentChatFacade.chat(agent, userId, message)
        // 6. Return Flux<ServerSentEvent<String>> with same wrapping pattern as AppController
    }

    // --- Clone & Export/Import ---

    @PostMapping("/clone")
    public BaseResponse<Long> cloneAgent(@RequestBody DeleteRequest request, HttpServletRequest httpRequest) { ... }

    @GetMapping("/export/{id}")
    public BaseResponse<String> exportAgent(@PathVariable Long id, HttpServletRequest httpRequest) { ... }

    @PostMapping("/import")
    public BaseResponse<Long> importAgent(@RequestBody AgentImportRequest request, HttpServletRequest httpRequest) { ... }

    // --- Review ---

    @PostMapping("/review/submit")
    public BaseResponse<Boolean> submitReview(@Valid @RequestBody AgentReviewSubmitRequest request, HttpServletRequest httpRequest) { ... }

    @PostMapping("/review/list/page")
    public BaseResponse<Page<AgentReview>> listReviewByPage(@RequestBody PageRequest request, @RequestParam Long agentId) { ... }

    // --- Admin ---

    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAgentByAdmin(@RequestBody AgentPublishRequest request) { ... }

    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAgentByAdmin(@RequestBody DeleteRequest request) { ... }

    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AgentVO>> listAgentByAdmin(@RequestBody AgentSquareQueryRequest request) { ... }
}
```

**Chat endpoint payment logic (critical)**:
```java
// In chatWithAgent():
boolean hasFreeTrial = agentUsageService.hasFreeTrial(agentId, loginUser.getId());
if (!hasFreeTrial && "points".equals(agent.getPriceType()) && agent.getPrice() > 0) {
    int price = agent.getPrice();
    // Consume from user
    userPointsService.consume(loginUser.getId(), price, agentId, "使用智能体: " + agent.getName());
    // Income to creator (80%)
    int creatorIncome = (int) (price * 0.8);
    userPointsService.addIncome(agent.getUserId(), creatorIncome, agentId, "智能体收入: " + agent.getName());
    agentUsageService.recordUsage(agentId, loginUser.getId(), false, price);
} else {
    agentUsageService.recordUsage(agentId, loginUser.getId(), hasFreeTrial, 0);
}
```

### Step 2: Commit

```bash
git add src/main/java/com/chen/yuaicodemother/controller/AgentController.java
git commit -m "feat(agent-marketplace): add AgentController with all endpoints"
```

---

## Task 13: UserPointsController

**Files:**
- Create: `src/main/java/com/chen/yuaicodemother/controller/UserPointsController.java`

### Step 1: Write controller

`@RestController @RequestMapping("/points")`

```java
@RestController
@RequestMapping("/points")
public class UserPointsController {

    @Resource private UserPointsService userPointsService;
    @Resource private UserService userService;

    @GetMapping("/balance")
    public BaseResponse<Integer> getBalance(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userPointsService.getBalance(loginUser.getId()));
    }

    @PostMapping("/recharge")
    public BaseResponse<Boolean> recharge(@Valid @RequestBody PointsRechargeRequest request, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        userPointsService.recharge(loginUser.getId(), request.getAmount(), "手动充值");
        return ResultUtils.success(true);
    }

    @PostMapping("/log/list/page")
    public BaseResponse<Page<UserPointsLog>> listLogByPage(@RequestBody PageRequest request, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Page<UserPointsLog> page = userPointsService.getPointsLogPage(loginUser.getId(), request.getPageNum(), request.getPageSize());
        return ResultUtils.success(page);
    }
}
```

### Step 2: Commit

```bash
git add src/main/java/com/chen/yuaicodemother/controller/UserPointsController.java
git commit -m "feat(agent-marketplace): add UserPointsController with balance, recharge, log endpoints"
```

---

## Task 14: AgentCategoryController (Admin)

**Files:**
- Create: `src/main/java/com/chen/yuaicodemother/controller/AgentCategoryController.java`

### Step 1: Write controller

`@RestController @RequestMapping("/agent/category")`

```java
@RestController
@RequestMapping("/agent/category")
public class AgentCategoryController {

    @Resource private AgentCategoryService agentCategoryService;

    @GetMapping("/list")
    public BaseResponse<List<AgentCategory>> listAll() {
        return ResultUtils.success(agentCategoryService.listAllOrdered());
    }

    @PostMapping("/admin/create")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> createCategory(@RequestBody AgentCategory category) { ... }

    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateCategory(@RequestBody AgentCategory category) { ... }
}
```

### Step 2: Commit

```bash
git add src/main/java/com/chen/yuaicodemother/controller/AgentCategoryController.java
git commit -m "feat(agent-marketplace): add AgentCategoryController with public list and admin CRUD"
```

---

## Task 15: Build Verification + Smoke Test

### Step 1: Build project

Run: `mvn clean compile -DskipTests`

Expected: BUILD SUCCESS

### Step 2: Run all existing tests

Run: `mvn test`

Expected: All existing tests pass (no regressions)

### Step 3: Verify API docs

Start application: `mvn spring-boot:run`

Visit: `http://localhost:8123/api/doc.html`

Verify: New agent endpoints appear in the API documentation under AgentController, UserPointsController, AgentCategoryController.

### Step 4: Final commit (if any fixes needed)

```bash
git add -A
git commit -m "fix(agent-marketplace): address build issues"
```
