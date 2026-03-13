# 数字员工任务中台 API 文档（AI 友好版）

本文件是给 AI Agent / 自动化脚本使用的快速接口文档。

## 1. 基本信息

- Base URL: `http://127.0.0.1:18080`
- 无鉴权（当前版本）
- OpenAPI UI: `http://127.0.0.1:18080/swagger-ui.html`
- 统一请求追踪头：`X-Request-Id`（可选；不传则系统自动生成）

## 2. 统一返回结构

成功：

```json
{
  "success": true,
  "data": {},
  "error": null,
  "requestId": "req_xxx"
}
```

失败：

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_ARGUMENT",
    "message": "title: title 不能为空"
  },
  "requestId": "req_xxx"
}
```

## 3. 枚举与状态规则

### 3.1 任务状态 `TaskStatus`

- `PENDING` 待处理
- `RUNNING` 进行中
- `BLOCKED` 阻塞
- `COMPLETED` 已完成
- `FAILED` 失败
- `CANCELLED` 已取消

### 3.2 任务优先级 `TaskPriority`

- `LOW` / `MEDIUM` / `HIGH` / `URGENT`

### 3.3 允许的状态流转

- `PENDING -> RUNNING / CANCELLED`
- `RUNNING -> COMPLETED / FAILED / BLOCKED / CANCELLED`
- `BLOCKED -> RUNNING / FAILED / CANCELLED`
- `FAILED -> RUNNING`
- `COMPLETED`、`CANCELLED` 为终态

### 3.4 更新任务限制

- `PUT /api/tasks/{id}` 仅允许更新基础字段
- 终态任务（`COMPLETED`、`CANCELLED`）不可编辑
- `operatorName` 必填
- 至少要传一个可更新字段，否则报错

## 4. 错误码

- `NOT_FOUND`：资源不存在（常见 HTTP 404）
- `INVALID_ARGUMENT`：参数错误（HTTP 400）
- `INVALID_STATUS_TRANSITION`：状态流转非法（HTTP 400）
- `INTERNAL_ERROR`：服务内部错误（HTTP 500）

## 5. 核心数据结构

### 5.1 ProjectResponse

```json
{
  "id": 1,
  "projectCode": "DEMO",
  "projectName": "演示项目",
  "status": "ACTIVE",
  "description": "系统启动后的默认演示项目",
  "createdAt": "2026-03-13T17:00:00",
  "updatedAt": "2026-03-13T17:00:00"
}
```

### 5.2 TaskResponse

```json
{
  "id": 1,
  "taskCode": "TASK-20260313170000-1234",
  "projectId": 1,
  "parentTaskId": null,
  "title": "整理需求文档",
  "taskType": "DOCUMENT",
  "status": "PENDING",
  "priority": "HIGH",
  "detail": "需要完成需求文档初稿",
  "initiator": "张三",
  "ownerName": "李四",
  "blockerContact": null,
  "blockReason": null,
  "plannedFinishAt": "2026-03-20T18:00:00",
  "actualStartAt": null,
  "actualFinishAt": null,
  "input": "{\"source\":\"会议纪要\"}",
  "output": null,
  "createdAt": "2026-03-13T17:00:00",
  "updatedAt": "2026-03-13T17:00:00"
}
```

注：`input` / `output` 在响应中是 JSON 字符串。

## 6. 接口清单

### 6.1 项目

- `GET /api/projects`：项目列表
- `GET /api/projects/{id}`：项目详情

### 6.2 任务查询

- `GET /api/tasks`：任务列表（支持过滤：`projectId`、`status`）
- `GET /api/tasks/{id}`：任务详情
- `GET /api/tasks/{id}/logs`：任务日志
- `GET /api/tasks/{id}/events`：任务事件

### 6.3 任务写操作

- `POST /api/tasks`：创建任务（初始状态 `PENDING`）
- `PUT /api/tasks/{id}`：更新任务基础字段
- `POST /api/tasks/{id}/start`：开始/恢复执行
- `POST /api/tasks/{id}/block`：标记阻塞
- `POST /api/tasks/{id}/complete`：标记完成
- `POST /api/tasks/{id}/fail`：标记失败
- `POST /api/tasks/{id}/cancel`：标记取消

## 7. 请求样例（AI 调用最常用）

### 7.1 查询项目列表

```http
GET /api/projects
```

### 7.2 创建任务

```http
POST /api/tasks
Content-Type: application/json

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

### 7.3 更新任务

```http
PUT /api/tasks/{id}
Content-Type: application/json

{
  "operatorName": "李四",
  "ownerName": "王五",
  "detail": "已补充执行方案",
  "priority": "URGENT"
}
```

### 7.4 开始任务

```http
POST /api/tasks/{id}/start
Content-Type: application/json

{
  "operatorName": "李四"
}
```

### 7.5 阻塞任务

```http
POST /api/tasks/{id}/block
Content-Type: application/json

{
  "operatorName": "李四",
  "blockerContact": "王五",
  "blockReason": "缺少上游确认信息"
}
```

### 7.6 完成任务

```http
POST /api/tasks/{id}/complete
Content-Type: application/json

{
  "operatorName": "李四",
  "output": {
    "summary": "文档已完成"
  }
}
```

### 7.7 失败任务

```http
POST /api/tasks/{id}/fail
Content-Type: application/json

{
  "operatorName": "李四",
  "reason": "依赖数据异常"
}
```

### 7.8 取消任务

```http
POST /api/tasks/{id}/cancel
Content-Type: application/json

{
  "operatorName": "张三",
  "reason": "需求取消"
}
```

## 8. AI 调用建议（执行策略）

1. 先 `GET /api/projects` 确认 `projectId`。
2. 用 `POST /api/tasks` 创建任务，保存 `task.id`。
3. 按业务推进状态：`start -> block/start -> complete/fail/cancel`。
4. 每次动作后可 `GET /api/tasks/{id}` 验证状态。
5. 当收到 `INVALID_STATUS_TRANSITION` 时，先读当前任务状态再重试正确动作。
6. 需要审计链路时读取：`GET /api/tasks/{id}/logs` 和 `GET /api/tasks/{id}/events`。

## 9. 一组最小可跑通流程

```text
GET  /api/projects
POST /api/tasks
POST /api/tasks/{id}/start
POST /api/tasks/{id}/complete
GET  /api/tasks/{id}
GET  /api/tasks/{id}/events
```
