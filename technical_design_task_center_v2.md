# 简化版任务中台技术设计文档

**文档版本**：v2.0  
**适用对象**：产品、研发、测试、运维  
**系统定位**：面向内部协作的简化任务中台  
**部署形态**：单体 Java 应用，JAR 一键启动  
**数据库**：PostgreSQL  
**页面形态**：服务端渲染 HTML，打开即用，无需登录

---

## 1. 背景与目标

本系统用于统一管理“项目”和“项目下的任务”，并提供简单、稳定、易于二次开发的接口，供页面、脚本、AI 工具或其他内部系统调用。

本版设计刻意做减法，去掉与特定机器人框架、聊天系统相关的集成设计，只保留通用任务中台能力。

本系统目标如下：

1. 提供一个可直接查看的大盘页面。
2. 提供项目列表、项目详情、任务详情页面。
3. 提供稳定、简单、适合 AI 调用的 REST API。
4. 支持任务状态流转、日志记录和阻塞联系人字段。
5. 采用单体 Java 工程，支持 JAR 一键启动。
6. 结构尽量简单，方便后续二次开发。

---

## 2. 设计原则

### 2.1 简单优先

- 不做复杂微服务拆分。
- 不做前后端分离。
- 不做登录与权限系统。
- 不做复杂工作流引擎。

### 2.2 面向二次开发

- 模块边界清晰。
- 表结构尽量直白。
- 接口动作有限、语义明确。
- 允许后续增加更多字段和扩展表。

### 2.3 面向 AI 友好

- 使用固定枚举状态。
- 返回结构统一。
- 字段命名清晰。
- 提供 OpenAPI 文档。
- 避免“万能接口”。

### 2.4 单机优先

- 一个工程。
- 一个 JAR。
- 一个 PostgreSQL。
- 一套配置。

---

## 3. 总体架构

### 3.1 架构说明

系统采用单体架构，包含三层：

1. 页面层：项目、任务、大盘页面。
2. 接口层：提供 REST API。
3. 数据层：PostgreSQL 存储项目、任务、日志、事件。

### 3.2 架构图

```text
┌──────────────────────────────────────────────┐
│                浏览器 / AI / 脚本            │
└──────────────────────┬───────────────────────┘
                       │ HTTP
┌──────────────────────▼───────────────────────┐
│         单体任务中台（Spring Boot）          │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │ 页面层（Thymeleaf + HTMX）             │  │
│  └────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────┐  │
│  │ 接口层（REST API + OpenAPI）           │  │
│  └────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────┐  │
│  │ 服务层（项目、任务、日志、状态流转）   │  │
│  └────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────┐  │
│  │ 数据访问层（JPA/MyBatis）              │  │
│  └────────────────────────────────────────┘  │
└──────────────────────┬───────────────────────┘
                       │ JDBC
                ┌──────▼──────┐
                │ PostgreSQL  │
                └─────────────┘
```

---

## 4. 技术选型

### 4.1 技术栈

| 层级 | 技术选型 | 说明 |
|---|---|---|
| 后端框架 | Spring Boot 3.x | 单体 Java 应用 |
| 页面模板 | Thymeleaf | 服务端渲染 HTML |
| 局部刷新 | HTMX | 简单局部刷新 |
| 数据库 | PostgreSQL 15+ | 存储项目、任务、日志 |
| ORM | Spring Data JPA | 简单直接，方便开发 |
| API 文档 | springdoc-openapi | 输出 OpenAPI 文档 |
| 构建工具 | Maven | Java 常用构建工具 |
| 监控 | Spring Boot Actuator | 提供健康检查和基础指标 |
| 日志 | Logback | 应用日志输出 |

### 4.2 选型说明

#### Spring Boot

适合快速搭建单体系统，生态成熟，便于后续扩展接口、页面和监控。

#### Thymeleaf

适合本项目这种简单中后台，页面直接由服务端生成，省掉独立前端工程。

#### HTMX

用于任务列表、项目列表的局部刷新，不需要引入复杂前端框架。

#### PostgreSQL

适合任务型系统，支持事务、索引、JSON 字段和后续扩展。

#### OpenAPI

方便研发联调，也方便 AI 工具读取接口说明。

---

## 5. 功能范围

### 5.1 本期功能

#### 5.1.1 大盘

- 查看项目总数
- 查看任务总数
- 查看待处理任务数
- 查看执行中任务数
- 查看已完成任务数
- 查看失败任务数
- 查看阻塞任务数
- 查看最近任务趋势

#### 5.1.2 项目

- 项目列表
- 项目详情
- 项目状态展示
- 项目下任务列表

#### 5.1.3 任务

- 创建任务
- 查询任务
- 更新任务
- 开始任务
- 完成任务
- 失败任务
- 阻塞任务
- 取消任务
- 查看任务日志
- 查看任务事件
- 支持父子任务

### 5.2 本期不做

- 登录与鉴权
- 审批流
- 多租户
- 消息系统集成
- 复杂调度引擎
- 分布式部署

---

## 6. 页面设计

### 6.1 页面清单

| 页面 | 路径 | 说明 |
|---|---|---|
| 首页大盘 | `/` | 查看总体情况 |
| 项目列表 | `/projects` | 查看所有项目 |
| 项目详情 | `/projects/{id}` | 查看项目详情和任务列表 |
| 任务详情 | `/tasks/{id}` | 查看任务详情、日志、事件 |
| 监控页 | `/monitor` | 查看系统健康和基础指标 |

### 6.2 首页大盘内容

建议展示：

- 项目总数
- 任务总数
- 待处理任务数
- 执行中任务数
- 已完成任务数
- 失败任务数
- 阻塞任务数

建议图表：

- 按项目统计任务数
- 按状态统计任务数
- 最近 7 天任务新增趋势

### 6.3 项目详情页内容

- 项目基本信息
- 项目状态
- 项目下任务列表
- 按状态筛选任务

### 6.4 任务详情页内容

- 任务基本信息
- 任务状态
- 计划完成时间
- 实际开始/结束时间
- 阻塞联系人
- 任务日志
- 状态流转事件

---

## 7. 任务状态设计

### 7.1 状态枚举

```text
PENDING
RUNNING
BLOCKED
COMPLETED
FAILED
CANCELLED
```

### 7.2 状态说明

| 状态 | 说明 |
|---|---|
| PENDING | 待处理 |
| RUNNING | 执行中 |
| BLOCKED | 已阻塞，等待外部处理 |
| COMPLETED | 已完成 |
| FAILED | 已失败 |
| CANCELLED | 已取消 |

### 7.3 允许的状态流转

```text
PENDING   -> RUNNING / CANCELLED
RUNNING   -> COMPLETED / FAILED / BLOCKED / CANCELLED
BLOCKED   -> RUNNING / FAILED / CANCELLED
FAILED    -> RUNNING（可选，表示重试）
COMPLETED -> 结束
CANCELLED -> 结束
```

---

## 8. 数据模型设计

### 8.1 核心实体

本版仅保留 4 个核心实体：

1. 项目 `project`
2. 任务 `task`
3. 任务日志 `task_log`
4. 任务事件 `task_event`

### 8.2 项目字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键 |
| project_code | VARCHAR(64) | 项目标识 |
| project_name | VARCHAR(200) | 项目名称 |
| status | VARCHAR(32) | 项目状态 |
| description | TEXT | 项目说明 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 8.3 任务字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键 |
| task_code | VARCHAR(64) | 任务编号 |
| project_id | BIGINT | 所属项目 |
| parent_task_id | BIGINT | 父任务，可为空 |
| title | VARCHAR(200) | 任务名称 |
| task_type | VARCHAR(64) | 任务类型 |
| status | VARCHAR(32) | 任务状态 |
| priority | VARCHAR(32) | 优先级 |
| detail | TEXT | 任务详情 |
| initiator | VARCHAR(100) | 发起人 |
| owner_name | VARCHAR(100) | 当前负责人 |
| blocker_contact | VARCHAR(200) | 阻塞联系人 |
| block_reason | VARCHAR(500) | 阻塞原因 |
| planned_finish_at | TIMESTAMP | 计划完成时间 |
| actual_start_at | TIMESTAMP | 实际开始时间 |
| actual_finish_at | TIMESTAMP | 实际结束时间 |
| input_json | JSONB | 输入参数 |
| output_json | JSONB | 输出结果 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 8.4 任务日志字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键 |
| task_id | BIGINT | 任务 ID |
| log_type | VARCHAR(32) | 日志类型 |
| message | TEXT | 日志内容 |
| payload_json | JSONB | 扩展内容 |
| created_at | TIMESTAMP | 创建时间 |

### 8.5 任务事件字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGSERIAL | 主键 |
| task_id | BIGINT | 任务 ID |
| event_type | VARCHAR(32) | 事件类型 |
| from_status | VARCHAR(32) | 原状态 |
| to_status | VARCHAR(32) | 新状态 |
| operator_name | VARCHAR(100) | 操作人 |
| note | VARCHAR(500) | 备注 |
| created_at | TIMESTAMP | 创建时间 |

---

## 9. 建表语句

```sql
CREATE TABLE project (
    id              BIGSERIAL PRIMARY KEY,
    project_code    VARCHAR(64) NOT NULL UNIQUE,
    project_name    VARCHAR(200) NOT NULL,
    status          VARCHAR(32) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE task (
    id                  BIGSERIAL PRIMARY KEY,
    task_code           VARCHAR(64) NOT NULL UNIQUE,
    project_id          BIGINT NOT NULL REFERENCES project(id),
    parent_task_id      BIGINT REFERENCES task(id),
    title               VARCHAR(200) NOT NULL,
    task_type           VARCHAR(64) NOT NULL,
    status              VARCHAR(32) NOT NULL,
    priority            VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    detail              TEXT,
    initiator           VARCHAR(100),
    owner_name          VARCHAR(100),
    blocker_contact     VARCHAR(200),
    block_reason        VARCHAR(500),
    planned_finish_at   TIMESTAMP,
    actual_start_at     TIMESTAMP,
    actual_finish_at    TIMESTAMP,
    input_json          JSONB,
    output_json         JSONB,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_task_project_id ON task(project_id);
CREATE INDEX idx_task_parent_task_id ON task(parent_task_id);
CREATE INDEX idx_task_status ON task(status);
CREATE INDEX idx_task_task_type ON task(task_type);
CREATE INDEX idx_task_priority ON task(priority);

CREATE TABLE task_log (
    id              BIGSERIAL PRIMARY KEY,
    task_id         BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
    log_type        VARCHAR(32) NOT NULL,
    message         TEXT NOT NULL,
    payload_json    JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_task_log_task_id ON task_log(task_id);

CREATE TABLE task_event (
    id              BIGSERIAL PRIMARY KEY,
    task_id         BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
    event_type      VARCHAR(32) NOT NULL,
    from_status     VARCHAR(32),
    to_status       VARCHAR(32),
    operator_name   VARCHAR(100),
    note            VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_task_event_task_id ON task_event(task_id);
```

---

## 10. 接口设计

### 10.1 设计原则

- 使用 REST 风格。
- 接口数量控制在最小可用范围。
- 页面和 AI 共用同一套业务能力。
- 返回格式统一。

### 10.2 统一返回结构

```json
{
  "success": true,
  "data": {},
  "error": null,
  "requestId": "req_001"
}
```

失败示例：

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_STATUS_TRANSITION",
    "message": "Task status transition is not allowed"
  },
  "requestId": "req_002"
}
```

### 10.3 项目接口

#### 查询项目列表

```http
GET /api/projects
```

#### 查询项目详情

```http
GET /api/projects/{id}
```

### 10.4 任务接口

#### 查询任务列表

```http
GET /api/tasks?projectId=1&status=PENDING
```

#### 查询任务详情

```http
GET /api/tasks/{id}
```

#### 创建任务

```http
POST /api/tasks
```

请求示例：

```json
{
  "projectId": 1,
  "parentTaskId": null,
  "title": "整理需求文档",
  "taskType": "DOCUMENT",
  "priority": "HIGH",
  "detail": "需要完成需求文档初稿",
  "initiator": "张三",
  "ownerName": "李四",
  "plannedFinishAt": "2026-03-20T18:00:00",
  "input": {
    "source": "会议纪要"
  }
}
```

#### 开始任务

```http
POST /api/tasks/{id}/start
```

请求示例：

```json
{
  "operatorName": "李四"
}
```

#### 阻塞任务

```http
POST /api/tasks/{id}/block
```

请求示例：

```json
{
  "operatorName": "李四",
  "blockerContact": "王五",
  "blockReason": "缺少上游确认信息"
}
```

#### 完成任务

```http
POST /api/tasks/{id}/complete
```

请求示例：

```json
{
  "operatorName": "李四",
  "output": {
    "summary": "文档已完成"
  }
}
```

#### 失败任务

```http
POST /api/tasks/{id}/fail
```

请求示例：

```json
{
  "operatorName": "李四",
  "reason": "依赖数据异常"
}
```

#### 取消任务

```http
POST /api/tasks/{id}/cancel
```

请求示例：

```json
{
  "operatorName": "张三",
  "reason": "需求取消"
}
```

### 10.5 日志与事件接口

#### 查询任务日志

```http
GET /api/tasks/{id}/logs
```

#### 查询任务事件

```http
GET /api/tasks/{id}/events
```

---

## 11. AI 友好接口说明

### 11.1 为什么这套接口适合 AI

原因如下：

- 资源固定：只有项目、任务、日志、事件。
- 动作固定：创建、开始、阻塞、完成、失败、取消。
- 状态有限：只有 6 个枚举值。
- 输入清楚：阻塞联系人、阻塞原因是明确字段。
- 输出统一：统一返回结构。
- 可读文档：提供 OpenAPI 文档。

### 11.2 推荐给 AI 暴露的最小接口集

```text
GET  /api/projects
GET  /api/projects/{id}
GET  /api/tasks
GET  /api/tasks/{id}
POST /api/tasks
POST /api/tasks/{id}/start
POST /api/tasks/{id}/block
POST /api/tasks/{id}/complete
POST /api/tasks/{id}/fail
POST /api/tasks/{id}/cancel
GET  /api/tasks/{id}/logs
GET  /api/tasks/{id}/events
```

### 11.3 OpenAPI 要求

建议所有接口补充：

- summary
- description
- request schema
- response schema
- example
- operationId

---

## 12. 监控设计

### 12.1 应用监控

通过 Spring Boot Actuator 暴露：

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`

### 12.2 业务大盘指标

建议在首页展示：

- 项目总数
- 任务总数
- 按状态统计任务数
- 今日新增任务数
- 今日完成任务数
- 当前阻塞任务数

### 12.3 异常关注项

重点关注：

- 长时间未完成任务
- 长时间阻塞任务
- 最近失败任务
- 数据库连接异常

---

## 13. 工程结构建议

```text
task-center/
  src/main/java/com/example/taskcenter/
    controller/
      DashboardController.java
      ProjectPageController.java
      TaskPageController.java
      ProjectApiController.java
      TaskApiController.java
    service/
      DashboardService.java
      ProjectService.java
      TaskService.java
      TaskEventService.java
      TaskLogService.java
    repository/
      ProjectRepository.java
      TaskRepository.java
      TaskLogRepository.java
      TaskEventRepository.java
    entity/
      Project.java
      Task.java
      TaskLog.java
      TaskEvent.java
    dto/
      request/
      response/
    config/
      OpenApiConfig.java
  src/main/resources/
    templates/
      index.html
      projects.html
      project-detail.html
      task-detail.html
    static/
      css/
      js/
    application.yml
  pom.xml
```

---

## 14. 部署方案

### 14.1 运行要求

- JDK 17+
- PostgreSQL 15+

### 14.2 application.yml 示例

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/task_center
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        format_sql: true
  thymeleaf:
    cache: false

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### 14.3 打包启动

```bash
mvn clean package
java -jar target/task-center.jar
```

### 14.4 一键启动建议

可提供一个简单启动脚本：

```bash
#!/bin/bash
java -jar task-center.jar
```

或通过 Docker Compose 同时启动：

- PostgreSQL
- task-center.jar

---

## 15. 迭代建议

### 15.1 第一阶段

先完成：

- 项目表、任务表、日志表、事件表
- 首页大盘
- 项目列表和详情页
- 任务详情页
- 基础 REST API
- OpenAPI 文档

### 15.2 第二阶段

按需扩展：

- 任务筛选增强
- 子任务树展示
- 任务批量操作
- 更丰富的图表统计
- 导出能力

### 15.3 第三阶段

按业务需要再补：

- 登录与权限
- 消息通知
- 外部系统集成
- 自动调度能力

---

## 16. 结论

本版方案的核心是：

- 用最简单的单体 Java 方式实现一个任务中台。
- 用项目、任务、日志、事件四类核心对象支撑业务。
- 用 Thymeleaf 页面满足查看需求。
- 用 REST API + OpenAPI 满足 AI 和系统调用需求。
- 用 `blocker_contact` 字段支持任务阻塞联系人的记录。
- 去掉与特定机器人框架、聊天系统的耦合，便于长期演进和二次开发。

这是一套适合快速落地、容易理解、后续可逐步增强的技术方案。
