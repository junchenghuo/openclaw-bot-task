-- 统一默认项目编码为 DAILY_WORK（兼容旧编码 DEFAULT_TASK / DEMO）
UPDATE project
SET project_code = 'DAILY_WORK',
    project_name = '日常工作',
    status = 'ACTIVE',
    description = '系统启动后的默认项目'
WHERE project_code = 'DEFAULT_TASK'
  AND NOT EXISTS (SELECT 1 FROM project WHERE project_code = 'DAILY_WORK');

UPDATE project
SET project_code = 'DAILY_WORK',
    project_name = '日常工作',
    status = 'ACTIVE',
    description = '系统启动后的默认项目'
WHERE project_code = 'DEMO'
  AND NOT EXISTS (SELECT 1 FROM project WHERE project_code = 'DAILY_WORK');

INSERT INTO project (project_code, project_name, status, description)
SELECT 'DAILY_WORK', '日常工作', 'ACTIVE', '系统启动后的默认项目'
WHERE NOT EXISTS (SELECT 1 FROM project WHERE project_code = 'DAILY_WORK');

UPDATE project
SET project_name = '日常工作',
    status = 'ACTIVE',
    description = '系统启动后的默认项目',
    workspace_path = '/Users/imac/midCreate/openclaw-workspaces/ai-team/projects/daily-work-routine/work',
    memory_path = '/Users/imac/midCreate/openclaw-workspaces/ai-team/projects/daily-work-routine/memory'
WHERE project_code = 'DAILY_WORK';

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
