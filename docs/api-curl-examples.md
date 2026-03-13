# API 调用示例（curl）

默认服务地址：

```bash
BASE_URL="http://127.0.0.1:18080"
```

## 1. 查询项目列表

```bash
curl -s "$BASE_URL/api/projects" | jq
```

## 2. 创建任务

```bash
curl -s -X POST "$BASE_URL/api/tasks" \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": 1,
    "title": "整理需求文档",
    "taskType": "DOCUMENT",
    "priority": "HIGH",
    "detail": "需要完成需求文档初稿",
    "initiator": "张三",
    "ownerName": "李四",
    "plannedFinishAt": "2026-03-20T18:00:00",
    "input": {"source": "会议纪要"}
  }' | jq
```

## 3. 更新任务（PUT）

```bash
TASK_ID=1
curl -s -X PUT "$BASE_URL/api/tasks/$TASK_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "operatorName": "李四",
    "ownerName": "王五",
    "detail": "已补充执行方案",
    "priority": "URGENT"
  }' | jq
```

## 4. 开始任务

```bash
curl -s -X POST "$BASE_URL/api/tasks/$TASK_ID/start" \
  -H "Content-Type: application/json" \
  -d '{"operatorName":"王五"}' | jq
```

## 5. 阻塞任务

```bash
curl -s -X POST "$BASE_URL/api/tasks/$TASK_ID/block" \
  -H "Content-Type: application/json" \
  -d '{
    "operatorName":"王五",
    "blockerContact":"赵六",
    "blockReason":"上游接口未返回数据"
  }' | jq
```

## 6. 解除阻塞并继续执行（再次 start）

```bash
curl -s -X POST "$BASE_URL/api/tasks/$TASK_ID/start" \
  -H "Content-Type: application/json" \
  -d '{"operatorName":"王五"}' | jq
```

## 7. 完成任务

```bash
curl -s -X POST "$BASE_URL/api/tasks/$TASK_ID/complete" \
  -H "Content-Type: application/json" \
  -d '{
    "operatorName":"王五",
    "output":{"summary":"文档已完成"}
  }' | jq
```

## 8. 查询任务详情、日志、事件

```bash
curl -s "$BASE_URL/api/tasks/$TASK_ID" | jq
curl -s "$BASE_URL/api/tasks/$TASK_ID/logs" | jq
curl -s "$BASE_URL/api/tasks/$TASK_ID/events" | jq
```
