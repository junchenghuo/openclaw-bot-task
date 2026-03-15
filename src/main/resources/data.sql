-- 统一默认项目编码为 DAILY_WORK（兼容旧编码 DEFAULT_TASK / DEMO）
UPDATE project
SET project_code = 'DAILY_WORK',
    project_name = '日常工作',
    status = '启用中',
    description = '系统启动后的默认项目'
WHERE project_code = 'DEFAULT_TASK'
  AND NOT EXISTS (SELECT 1 FROM project WHERE project_code = 'DAILY_WORK');

UPDATE project
SET project_code = 'DAILY_WORK',
    project_name = '日常工作',
    status = '启用中',
    description = '系统启动后的默认项目'
WHERE project_code = 'DEMO'
  AND NOT EXISTS (SELECT 1 FROM project WHERE project_code = 'DAILY_WORK');

INSERT INTO project (project_code, project_name, status, description)
SELECT 'DAILY_WORK', '日常工作', '启用中', '系统启动后的默认项目'
WHERE NOT EXISTS (SELECT 1 FROM project WHERE project_code = 'DAILY_WORK');

UPDATE project
SET project_name = '日常工作',
    status = '启用中',
    description = '系统启动后的默认项目',
    workspace_path = '/Users/imac/midCreate/openclaw-workspaces/ai-team/projects/daily-work-routine/work',
    memory_path = '/Users/imac/midCreate/openclaw-workspaces/ai-team/projects/daily-work-routine/memory'
WHERE project_code = 'DAILY_WORK';

-- 历史英文状态迁移为中文
UPDATE project SET status = '启用中' WHERE status = 'ACTIVE';
UPDATE project SET status = '未启用' WHERE status = 'INACTIVE';
UPDATE project SET status = '已归档' WHERE status = 'ARCHIVED';

UPDATE task SET status = '待处理' WHERE status = 'PENDING';
UPDATE task SET status = '进行中' WHERE status = 'RUNNING';
UPDATE task SET status = '阻塞' WHERE status = 'BLOCKED';
UPDATE task SET status = '已完成' WHERE status = 'COMPLETED';
UPDATE task SET status = '失败' WHERE status = 'FAILED';
UPDATE task SET status = '已取消' WHERE status = 'CANCELLED';

UPDATE task SET priority = '低' WHERE priority = 'LOW';
UPDATE task SET priority = '中' WHERE priority = 'MEDIUM';
UPDATE task SET priority = '高' WHERE priority = 'HIGH';
UPDATE task SET priority = '紧急' WHERE priority = 'URGENT';

UPDATE task_event SET from_status = '待处理' WHERE from_status = 'PENDING';
UPDATE task_event SET from_status = '进行中' WHERE from_status = 'RUNNING';
UPDATE task_event SET from_status = '阻塞' WHERE from_status = 'BLOCKED';
UPDATE task_event SET from_status = '已完成' WHERE from_status = 'COMPLETED';
UPDATE task_event SET from_status = '失败' WHERE from_status = 'FAILED';
UPDATE task_event SET from_status = '已取消' WHERE from_status = 'CANCELLED';

UPDATE task_event SET to_status = '待处理' WHERE to_status = 'PENDING';
UPDATE task_event SET to_status = '进行中' WHERE to_status = 'RUNNING';
UPDATE task_event SET to_status = '阻塞' WHERE to_status = 'BLOCKED';
UPDATE task_event SET to_status = '已完成' WHERE to_status = 'COMPLETED';
UPDATE task_event SET to_status = '失败' WHERE to_status = 'FAILED';
UPDATE task_event SET to_status = '已取消' WHERE to_status = 'CANCELLED';

UPDATE project_meeting SET status = '投票中' WHERE status = 'VOTING';
UPDATE project_meeting SET status = '已决策' WHERE status = 'DECIDED';
UPDATE project_meeting SET status = '已取消' WHERE status = 'CANCELLED';

-- 合并“日常工作”重复项目：任务统一归档到 DAILY_WORK，再删除重复项目
UPDATE task
SET project_id = (SELECT id FROM project WHERE project_code = 'DAILY_WORK' ORDER BY id LIMIT 1)
WHERE project_id IN (
    SELECT id FROM project
    WHERE (project_name = '日常工作' OR project_code IN ('DEFAULT_TASK', 'DEMO', 'DAILY_WORK'))
      AND id <> (SELECT id FROM project WHERE project_code = 'DAILY_WORK' ORDER BY id LIMIT 1)
);

DELETE FROM project
WHERE id IN (
    SELECT id FROM project
    WHERE (project_name = '日常工作' OR project_code IN ('DEFAULT_TASK', 'DEMO', 'DAILY_WORK'))
      AND id <> (SELECT id FROM project WHERE project_code = 'DAILY_WORK' ORDER BY id LIMIT 1)
);
