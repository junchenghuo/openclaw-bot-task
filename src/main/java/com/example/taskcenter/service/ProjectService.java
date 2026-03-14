package com.example.taskcenter.service;

import com.example.taskcenter.dto.request.CreateProjectRequest;
import com.example.taskcenter.entity.Project;
import com.example.taskcenter.exception.BusinessException;
import com.example.taskcenter.exception.ErrorCodes;
import com.example.taskcenter.repository.ProjectMeetingRepository;
import com.example.taskcenter.repository.ProjectRepository;
import com.example.taskcenter.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectDirectoryService projectDirectoryService;
    private final TaskRepository taskRepository;
    private final ProjectMeetingRepository projectMeetingRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectDirectoryService projectDirectoryService,
                          TaskRepository taskRepository,
                          ProjectMeetingRepository projectMeetingRepository) {
        this.projectRepository = projectRepository;
        this.projectDirectoryService = projectDirectoryService;
        this.taskRepository = taskRepository;
        this.projectMeetingRepository = projectMeetingRepository;
    }

    public List<Project> listProjects() {
        return projectRepository.findAll().stream().map(this::ensureProjectDirectories).toList();
    }

    public Project getProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.NOT_FOUND,
                        "Project not found: " + projectId,
                        HttpStatus.NOT_FOUND
                ));
        return ensureProjectDirectories(project);
    }

    @Transactional
    public Project createProject(CreateProjectRequest request) {
        String code = normalizeProjectCode(request.getProjectCode());
        String name = request.getProjectName().trim();
        if (projectRepository.findByProjectCode(code).isPresent()) {
            throw new IllegalArgumentException("projectCode 已存在: " + code);
        }

        ProjectDirectoryService.ProjectPaths projectPaths =
                projectDirectoryService.createProjectDirectories(code, name);

        Project project = new Project();
        project.setProjectCode(code);
        project.setProjectName(name);
        project.setStatus(resolveStatus(request.getStatus()));
        project.setDescription(request.getDescription());
        project.setWorkspacePath(projectPaths.workspacePath());
        project.setMemoryPath(projectPaths.memoryPath());
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        Project project = getProject(projectId);
        if ("DAILY_WORK".equalsIgnoreCase(project.getProjectCode())
                || "日常工作".equals(project.getProjectName())
                || "异常工作".equals(project.getProjectName())) {
            throw new IllegalArgumentException("该项目为保留项目，不允许删除");
        }
        projectRepository.delete(project);
    }

    private Project ensureProjectDirectories(Project project) {
        boolean changed = false;
        String workspacePath = project.getWorkspacePath();
        String memoryPath = project.getMemoryPath();

        if (workspacePath == null || workspacePath.isBlank() || memoryPath == null || memoryPath.isBlank()) {
            ProjectDirectoryService.ProjectPaths paths =
                    projectDirectoryService.createProjectDirectories(project.getProjectCode(), project.getProjectName());
            if (workspacePath == null || workspacePath.isBlank()) {
                project.setWorkspacePath(paths.workspacePath());
                changed = true;
            }
            if (memoryPath == null || memoryPath.isBlank()) {
                project.setMemoryPath(paths.memoryPath());
                changed = true;
            }
        } else {
            projectDirectoryService.ensureDirectories(workspacePath, memoryPath);
        }

        if (changed) {
            return projectRepository.save(project);
        }
        return project;
    }

    private String normalizeProjectCode(String rawCode) {
        String code = rawCode == null ? "" : rawCode.trim().toUpperCase(Locale.ROOT);
        if (code.isEmpty()) {
            throw new IllegalArgumentException("projectCode 不能为空");
        }
        return code;
    }

    private String resolveStatus(String status) {
        String value = status == null ? "" : status.trim();
        if (value.isEmpty()) {
            return "ACTIVE";
        }
        return value.toUpperCase(Locale.ROOT);
    }
}
