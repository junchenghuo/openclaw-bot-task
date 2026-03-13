# OpenClaw 任务中心项目（openclaw-bot-task）

本仓库用于构建一个**面向 OpenClaw 的任务编排与过程管理中心**，支持在业务协作中统一管理项目、任务状态、执行日志和升级流程。

技术实现为简化版任务中台（Spring Boot 3 + Thymeleaf + PostgreSQL）。

上游项目：`https://github.com/openclaw/openclaw`

扩展文档：

- `technical_design_task_center_v2.md`（任务中心详细技术方案）
- `AI_API_DOC.md`（面向 Agent 的结构化接口说明）

## 作者简介

### 霍钧城（分布式 AI 架构师）

具备多年企业级研发与架构经验，长期聚焦“高并发分布式系统 + 业务中台 + AI Agent 工程化落地”的融合实践。

联系方式：`howard_007@163.com`

- 擅长企业级架构设计与高可用治理，具备从单体到微服务的演进经验。
- 深度参与 B2B2C 电商与供应链核心链路建设，具备支付中台与分账结算实践。
- 聚焦 AI Agent 工程化落地，覆盖 RAG、Tool Calling、MCP 与 Skills 资产化建设。

## 本次改进点（2026-03-13）

- 为多角色协议文件补充 Mattermost 快速已读规则：收到消息先加 `:ok_hand:`，再回执 `已接单/done/blocked`。
- 清理历史示例交付物（`projects/login-page-delivery` 与 `projects/common-ui-components` 下旧文档与图片），降低仓库冗余。
- 保留并强化“Leader 统一调度、角色标准回执”的协作约束，便于后续任务编排与过程审计。

## 运行环境

- JDK 17+
- PostgreSQL 15+

## 快速启动

1. 创建数据库：

```sql
CREATE DATABASE task_center;
```

2. 如需自定义连接信息，可设置环境变量：

```bash
export DB_URL=jdbc:postgresql://127.0.0.1:5432/task_center
export DB_USER=postgres
export DB_PASS=postgres
```

3. 启动应用：

```bash
mvn spring-boot:run
```

手动重启脚本：

```bash
./scripts/restart.sh
```

4. 或使用 Docker 仅启动应用（数据库仍使用你本机 PostgreSQL）：

```bash
docker compose up --build -d
```

如果你的本机 PostgreSQL 账号密码不是默认值，可先设置：

```bash
export DB_URL=jdbc:postgresql://host.docker.internal:5432/task_center
export DB_USER=postgres
export DB_PASS=postgres
docker compose up --build -d
```

## 页面入口

- 大盘：`http://127.0.0.1:18080/`
- 项目列表：`http://127.0.0.1:18080/projects`
- 监控页：`http://127.0.0.1:18080/monitor`
- OpenAPI：`http://127.0.0.1:18080/swagger-ui.html`

## 主要 API

- `GET /api/projects`
- `GET /api/projects/{id}`
- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `POST /api/tasks`
- `POST /api/tasks/{id}/start`
- `POST /api/tasks/{id}/block`
- `POST /api/tasks/{id}/complete`
- `POST /api/tasks/{id}/fail`
- `POST /api/tasks/{id}/cancel`
- `PUT /api/tasks/{id}`
- `GET /api/tasks/{id}/logs`
- `GET /api/tasks/{id}/events`

可直接执行的 curl 示例见：`docs/api-curl-examples.md`

面向 AI Agent 的结构化接口说明见：`AI_API_DOC.md`

## 郑吒专用 Skill：`openclaw-task`

以下规则用于让机器人在“分派任务”场景下，强制落任务、持续跟进、并按层级升级。

### 1) 触发条件

- 只要出现“分派任务/安排任务/跟进任务/催办任务”等动作，就必须启用 `openclaw-task`。
- 启用后第一步必须创建任务，不允许只口头确认、不落库。

### 2) 项目归属规则（必须执行）

1. 指令中明确了项目名/项目编码：挂到对应项目。
2. 指令中出现“爱你过目”：优先匹配项目名包含“爱你过目”的项目并挂载。
3. 未指定项目时：默认项目固定为 `日常工作`（建议项目编码 `DAILY_WORK`，且全系统只保留一个）。
4. 若目标项目不存在：
   - 先回退到 `日常工作`；
   - 若 `日常工作` 也不存在，立即通知 `leader` 初始化项目后再重试创建任务。

### 3) 标准执行流程（分派即建单）

1. `GET /api/projects`：解析项目并拿到 `projectId`。
2. `POST /api/tasks`：创建任务（初始 `PENDING`）。
3. 返回任务回执给发起人：`taskId`、`taskCode`、项目、负责人、计划完成时间。
4. 开工时 `POST /api/tasks/{id}/start`。
5. 推进过程中持续更新（必要时 `PUT /api/tasks/{id}`），并记录关键日志/事件。
6. 完成时 `POST /api/tasks/{id}/complete`；失败时 `POST /api/tasks/{id}/fail`。

### 4) 跟进与状态管理规则

- 每次状态变化后，都要 `GET /api/tasks/{id}` 二次确认状态一致。
- 对 `PENDING/RUNNING/BLOCKED/FAILED` 任务进行周期巡检，输出待办清单与风险清单。
- 出现 `INVALID_STATUS_TRANSITION` 时，先读取当前状态，再按合法流转重试。

### 5) 阻塞/失败升级机制（强制）

1. 任务进入 `BLOCKED` 或 `FAILED` 后，机器人必须第一时间通知 `leader` 介入。
2. `leader` 处理后，若仍无法解除阻塞或修复失败，必须升级到管理员（admin）。
3. 升级信息必须包含：任务编号、当前状态、阻塞/失败原因、已尝试动作、下一步建议。

### 6) 机器人输出规范

- 分派后必须回执：`已创建任务 TASK-xxx（项目：xxx，负责人：xxx，状态：PENDING）`。
- 状态变更必须回执：`TASK-xxx 已更新为 RUNNING/BLOCKED/COMPLETED/FAILED`。
- 升级时必须明确：`已升级给 leader` 或 `已升级给管理员`。

---

## 作者介绍（详细）

### 霍钧城（分布式 AI 架构师）

具备多年企业级研发与架构经验，长期聚焦“高并发分布式系统 + 电商交易中台 + AI 应用落地”的融合架构实践。

联系方式：`howard_007@163.com`

**技术架构能力（Technical Architecture）**

- 具备从单体到微服务的架构演进经验，熟悉 Spring Cloud、网关、配置中心、任务调度、消息中间件与可观测体系建设。
- 擅长高并发与高可用设计，围绕缓存分层、异步解耦、分布式锁、最终一致性、熔断限流等方案提升系统稳定性。
- 有云原生工程化落地经验，能够基于 Docker/K8s/Jenkins/DevOps 构建持续交付与自动化发布体系。

**业务架构能力（Business Architecture）**

- 深度参与 B2B2C 电商与供应链场景，覆盖商品、订单、库存、支付、分账、结算、对账等核心链路。
- 具备统一支付中台设计经验，支持多支付渠道接入、多级分账与实时结算，保障交易链路一致性与可追踪性。
- 面向业务智能化升级，推动企业级 Agent 在客服、运营、风控、供应链协同等场景落地，形成“人+AI+系统”协作闭环。

**AI 架构能力（AI Architecture / Agent Engineering）**

- 具备大模型平台化搭建与使用能力，支持多模型接入、模型路由、推理参数治理与成本/延迟/效果平衡。
- 采用 RAG + Hybrid Search + Rerank 架构，提升企业知识问答准确率与可解释性。
- 结合 Function Calling / Tool Calling 实现“自然语言意图 -> 业务操作执行”闭环。
- 构建 MCP（Model Context Protocol）工具生态，沉淀 MCP Server 与标准化能力接口。
- 建设 Skills 资产体系，将高频业务能力封装为可复用 Skill，支持版本化与持续迭代。
