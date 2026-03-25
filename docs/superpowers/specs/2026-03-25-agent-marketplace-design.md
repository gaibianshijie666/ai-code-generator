# 智能体广场设计文档

## 1. 概述

在现有 yu-ai-code-mother 平台上新增「智能体广场」功能。用户可以创建、配置、发布 AI 智能体，其他用户可以在广场浏览、在线体验、克隆或导出智能体。支持免费和付费（虚拟积分）两种模式。

### 智能体定义

智能体是一个可配置的 AI 助手，具备以下能力：
- **问答**：基于系统提示词的专业对话
- **代码生成**：调用现有 LangGraph4j 工作流生成代码
- **工具调用**：文件读写、图片搜索、Logo 生成、Mermaid 图表等

创建者通过配置系统提示词和工具集来定义智能体的专业能力。

## 2. 数据模型

### 2.1 agent（智能体表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键，雪花 ID |
| name | varchar(128) | 智能体名称 |
| description | varchar(512) | 智能体描述 |
| avatar | varchar(512) | 头像 URL（MinIO） |
| systemPrompt | text | 系统提示词（角色设定） |
| toolIds | varchar(1024) | 启用的工具 ID 列表，JSON 数组 |
| categoryId | bigint | 分类 ID，关联 agent_category |
| tags | varchar(512) | 标签，逗号分隔 |
| isPublic | tinyint | 是否公开（0=私有草稿，1=公开到广场） |
| priceType | varchar(16) | 定价类型：free / points |
| price | int | 积分价格（priceType=points 时有效） |
| userId | bigint | 创建者 ID |
| useCount | int | 累计使用次数，默认 0 |
| cloneCount | int | 累计克隆次数，默认 0 |
| rating | decimal(2,1) | 平均评分（1.0-5.0），默认 0 |
| status | tinyint | 状态：0=草稿，1=已发布，2=已下架 |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |
| isDelete | tinyint | 逻辑删除 |

索引：`idx_userId`，`idx_categoryId`，`idx_isPublic_status`，`idx_rating`

### 2.2 agent_category（分类表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| name | varchar(64) | 分类名称 |
| icon | varchar(256) | 分类图标 URL |
| sortOrder | int | 排序权重，越小越靠前 |
| createTime | datetime | 创建时间 |

预设分类（初始化时写入）：
- 编程开发
- 写作创作
- 数据分析
- 设计创意
- 学习教育
- 生活助手
- 办公效率
- 其他

### 2.3 agent_review（评价表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| agentId | bigint | 智能体 ID |
| userId | bigint | 评价者 ID |
| rating | tinyint | 评分 1-5 |
| content | varchar(512) | 评价内容 |
| createTime | datetime | 创建时间 |
| isDelete | tinyint | 逻辑删除 |

索引：`idx_agentId`，唯一约束 `uk_agentId_userId`（每个用户对同一智能体只能评价一次）

### 2.4 agent_usage（使用记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| agentId | bigint | 智能体 ID |
| userId | bigint | 使用者 ID |
| isFree | tinyint | 是否免费体验（0=付费，1=免费） |
| pointsCost | int | 消耗积分 |
| createTime | datetime | 使用时间 |

索引：`uk_agentId_userId`（用于判断是否已免费体验过）

### 2.5 user_points（用户积分表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| userId | bigint | 用户 ID |
| balance | int | 当前积分余额，默认 0 |
| totalIncome | int | 累计收入积分，默认 0 |
| totalExpense | int | 累计支出积分，默认 0 |
| updateTime | datetime | 更新时间 |

唯一约束 `uk_userId`

### 2.6 user_points_log（积分流水表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| userId | bigint | 用户 ID |
| amount | int | 变动金额（正数=收入，负数=支出） |
| balanceAfter | int | 变动后余额 |
| type | varchar(16) | 类型：recharge / consume / income |
| description | varchar(256) | 描述 |
| agentId | bigint | 关联智能体 ID（consume/income 时有值） |
| createTime | datetime | 创建时间 |

索引：`idx_userId`，`idx_type`

## 3. API 设计

### 3.1 智能体管理

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /agent/create | 登录 | 创建智能体（草稿状态） |
| POST | /agent/update | 创建者 | 更新智能体信息 |
| POST | /agent/delete | 创建者/管理员 | 删除智能体 |
| GET | /agent/get/vo | 登录 | 获取智能体详情 |
| POST | /agent/my/list/page | 登录 | 我的智能体列表（分页） |
| POST | /agent/publish | 创建者 | 发布到广场（设置公开+定价） |
| POST | /agent/offShelf | 创建者 | 从广场下架 |

### 3.2 广场

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /agent/square/list/page | 公开 | 广场列表（搜索+分类筛选+排序） |
| GET | /agent/square/detail/{id} | 公开 | 广场详情页（含评价） |
| POST | /agent/review/list/page | 公开 | 评价列表（分页） |

### 3.3 使用

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /agent/chat | 登录 | SSE 对话（在线体验） |
| POST | /agent/clone | 登录 | 克隆智能体到自己的账号 |
| GET | /agent/export/{id} | 登录 | 导出智能体配置 JSON |
| POST | /agent/review/submit | 登录 | 提交评价 |
| POST | /agent/import | 登录 | 导入智能体配置 JSON |

### 3.4 积分

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | /points/balance | 登录 | 查询积分余额 |
| POST | /points/recharge | 登录 | 充值积分 |
| POST | /points/log/list/page | 登录 | 积分流水（分页） |

### 3.5 管理员

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /agent/admin/list/page | 管理员 | 智能体列表 |
| POST | /agent/admin/update | 管理员 | 管理智能体（下架等） |
| POST | /agent/admin/delete | 管理员 | 删除智能体 |
| POST | /agent/category/admin/list | 管理员 | 分类列表 |
| POST | /agent/category/admin/create | 管理员 | 创建分类 |
| POST | /agent/category/admin/update | 管理员 | 更新分类 |

## 4. 核心流程

### 4.1 创建与发布

```
用户填写名称/描述/系统提示词
→ 选择分类和标签
→ 选择工具集（代码生成、文件读写、图片搜索等）
→ 上传头像
→ 保存为草稿（status=0）
→ 点击发布 → 设置定价（免费/付费+价格）
→ isPublic=1, status=1 → 出现在广场
```

### 4.2 在线体验（SSE 对话）

```
用户点击「在线体验」
→ 检查是否公开智能体
→ 检查是否已免费体验过（查 agent_usage 表）
  ├── 未体验过 → 标记为免费体验，不扣积分
  └── 已体验过 → 检查定价
       ├── 免费 → 直接使用
       └── 付费 → 检查积分余额 → 扣除积分 → 记录流水
→ 根据 agent 的 systemPrompt + toolIds 动态构建 AI Service
→ SSE 流式返回对话内容
→ useCount++
```

### 4.3 克隆

```
用户点击「克隆」
→ 复制 agent 记录（除 id、userId、useCount、rating、status 外全部复制）
→ 新 agent 的 userId = 当前用户，status = 0（草稿）
→ 加入「我的智能体」列表
→ cloneCount++
```

### 4.4 导出与导入

**导出**：将智能体配置（name、description、systemPrompt、toolIds、tags、categoryId）序列化为 JSON 返回下载。

**导入**：用户上传 JSON 文件 → 解析配置 → 创建新智能体（草稿状态）。

### 4.5 积分与收入分成

```
创建者设置 priceType=points, price=N
→ 使用者付费 → 扣除使用者 N 积分
→ 创建者收入 N * 80%（平台抽成 20%）
→ 分别记录两条流水（consume + income）
→ 更新 user_points 的 balance、totalIncome、totalExpense
```

## 5. 关键设计决策

### 5.1 AI Service 动态构建

在线体验时，根据智能体的 `systemPrompt` 和 `toolIds` 动态构建 LangChain4j AI Service：

```java
// 根据智能体配置动态构建
AiServices.builder(AgentChatService.class)
    .streamingChatModel(streamingChatModel)
    .systemMessage(agent.getSystemPrompt())
    .tools(selectedTools)  // 根据 toolIds 筛选
    .chatMemory(memory)
    .build();
```

复用现有的 `ToolManager` 工具注册机制，通过 `toolIds` 筛选启用哪些工具。

### 5.2 对话记忆隔离

每个用户对每个智能体的对话独立存储，使用 Redis 按 `agent:usage:{agentId}:{userId}` 为 key 存储对话历史。

### 5.3 充值

初期采用管理员手动充值或兑换码方式，后续可接入支付。积分 1 元 = 10 积分（可配置）。

## 6. 预设分类初始化

系统启动时自动初始化分类数据（如已存在则跳过）：

| 分类 | 说明 |
|------|------|
| 编程开发 | 代码编写、调试、架构设计 |
| 写作创作 | 文案、小说、翻译 |
| 数据分析 | 数据处理、可视化 |
| 设计创意 | UI 设计、配色、创意 |
| 学习教育 | 学习辅导、知识问答 |
| 生活助手 | 日常建议、规划 |
| 办公效率 | 文档、汇报、流程 |
| 其他 | 未分类 |

## 7. 文件结构

```
src/main/java/com/chen/yuaicodemother/
├── controller/
│   ├── AgentController.java           # 智能体 CRUD + 广场 + 使用
│   ├── AgentCategoryController.java   # 分类管理
│   └── UserPointsController.java      # 积分管理
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
│   │   └── AgentSquareQueryRequest.java
│   ├── vo/agent/
│   │   ├── AgentVO.java
│   │   └── AgentSquareVO.java
│   └── dto/points/
│       └── PointsRechargeRequest.java
├── service/
│   ├── AgentService.java
│   ├── AgentCategoryService.java
│   ├── UserPointsService.java
│   └── impl/
│       ├── AgentServiceImpl.java
│       ├── AgentCategoryServiceImpl.java
│       └── UserPointsServiceImpl.java
├── ai/
│   └── AgentChatServiceFactory.java   # 动态构建智能体 AI Service
├── mapper/
│   ├── AgentMapper.java
│   ├── AgentCategoryMapper.java
│   ├── AgentReviewMapper.java
│   ├── AgentUsageMapper.java
│   ├── UserPointsMapper.java
│   └── UserPointsLogMapper.java
└── core/
    └── AgentChatFacade.java           # 智能体对话门面（SSE）
```
