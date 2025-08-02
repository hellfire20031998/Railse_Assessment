package com.railse.hiring.workforcemgmt.service.impl;

import com.railse.hiring.workforcemgmt.common.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.mapper.IActivityMapper;
import com.railse.hiring.workforcemgmt.mapper.ICommentMapper;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.model.TaskActivity;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import com.railse.hiring.workforcemgmt.respository.InMemoryTaskRepository;
import com.railse.hiring.workforcemgmt.respository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class TaskManagementServiceImpl implements TaskManagementService {
    private final TaskRepository taskRepository;
    private final ITaskManagementMapper taskMapper;
    private final IActivityMapper activityMapper;
    private final ICommentMapper commentMapper;


    public TaskManagementServiceImpl(TaskRepository taskRepository, ITaskManagementMapper taskMapper,ICommentMapper commentMapper,IActivityMapper activityMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.commentMapper = commentMapper;
        this.activityMapper = activityMapper;
    }


@Override
public TaskManagementDto findTaskById(Long id) {
    TaskManagement task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

    TaskManagementDto dto = taskMapper.modelToDto(task);

    // Fetch comments and activities
    List<TaskComment> comments = taskRepository.getCommentsByTaskId(id);
    List<TaskActivity> activities = taskRepository.getActivitiesByTaskId(id);

    // Map to DTOs (assuming mappers exist)
    List<ActivityDto> activityDtos = activityMapper.modelListToActivityDtoList(activities);

    List<CommentDto> commentDtos = commentMapper.modelListToCommentDtoList(comments);


    // Combine and sort all items by timestamp
    List<Object> combinedLogs = new ArrayList<>();
    combinedLogs.addAll(commentDtos);
    combinedLogs.addAll(activityDtos);

    combinedLogs.sort(Comparator.comparing(obj -> {
        if (obj instanceof CommentDto) return ((CommentDto) obj).getTimestamp();
        else if (obj instanceof ActivityDto) return ((ActivityDto) obj).getTimestamp();
        else return 0L;
    }));

    // Separate again if needed
    dto.setComments(commentDtos);
    dto.setActivityLogs(activityDtos);

    return dto;
}



    @Override
    public List<TaskManagementDto> createTasks(TaskCreateRequest createRequest) {
        List<TaskManagement> createdTasks = new ArrayList<>();

        for (TaskCreateRequest.RequestItem item : createRequest.getRequests()) {
            TaskManagement newTask = new TaskManagement();
            newTask.setReferenceId(item.getReferenceId());
            newTask.setReferenceType(item.getReferenceType());
            newTask.setTask(item.getTask());
            newTask.setAssigneeId(item.getAssigneeId());
            newTask.setPriority(item.getPriority());
            newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
            newTask.setStatus(TaskStatus.ASSIGNED);
            newTask.setDescription("New task created.");
            taskRepository.save(newTask); // Save task first to get the ID

            // Save comment
            TaskComment comment = new TaskComment();
            comment.setTaskId(newTask.getId());
            comment.setAuthor("New tasks created by "+newTask.getReferenceId()); // You must pass this in the request
            comment.setComment("New task created.");
            comment.setTimestamp(System.currentTimeMillis());
          TaskComment savedComment=  taskRepository.addComment(newTask.getId(), comment.getAuthor(), comment.getComment());
            newTask.getComments().add(savedComment);

            // Save activity
            TaskActivity activity = new TaskActivity();
            activity.setTaskId(newTask.getId());
            activity.setDescription("Task assigned to user ID: " + item.getAssigneeId());
            activity.setTimestamp(System.currentTimeMillis());

            TaskActivity savedActivity= taskRepository.addActivityLog(newTask.getId(),activity);
            newTask.getActivities().add(savedActivity);
            createdTasks.add(newTask);
        }

        return taskMapper.modelListToDtoList(createdTasks);
    }



@Override
public List<TaskManagementDto> updateTasks(UpdateTaskRequest updateRequest) {
    List<TaskManagement> updatedTasks = new ArrayList<>();
    Long currentTimestamp = System.currentTimeMillis();

    for (UpdateTaskRequest.RequestItem item : updateRequest.getRequests()) {
        TaskManagement task = taskRepository.findById(item.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));

        StringBuilder activityLog = new StringBuilder("Updated: ");
        StringBuilder commentLog = new StringBuilder();

        // Update status unconditionally
        task.setStatus(item.getTaskStatus());
        activityLog.append("Status set to ").append(item.getTaskStatus()).append("; ");
        commentLog.append("Status updated. ");

        // Update description unconditionally
        task.setDescription(item.getDescription());
        activityLog.append("Description updated; ");
        commentLog.append("Description updated. ");

        // Save activity
        TaskActivity activity = new TaskActivity();
        activity.setTaskId(task.getId());
        activity.setDescription(activityLog.toString().trim());
        activity.setTimestamp(currentTimestamp);
        TaskActivity savedActivity = taskRepository.addActivityLog(task.getId(), activity);
        task.getActivities().add(savedActivity);

        // Save comment
        TaskComment comment = new TaskComment();
        comment.setTaskId(task.getId());
        comment.setAuthor("Task updated by " + task.getReferenceId()); // Ideally use logged-in user
        comment.setComment(commentLog.toString().trim());
        comment.setTimestamp(currentTimestamp);
        TaskComment savedComment = taskRepository.addComment(task.getId(), comment.getAuthor(), comment.getComment());
        task.getComments().add(savedComment);

        updatedTasks.add(taskRepository.save(task));
        List<TaskActivity> activities = taskRepository.getActivitiesByTaskId(task.getId());
        List<ActivityDto> activityDtos = activityMapper.modelListToActivityDtoList(activities);

    }

    return taskMapper.modelListToDtoList(updatedTasks);
}



    public String assignByReference(AssignByReferenceRequest request) {
        List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());
        List<TaskManagement> existingTasks = taskRepository
                .findByReferenceIdAndReferenceType(request.getReferenceId(), request.getReferenceType());

        for (Task taskType : applicableTasks) {
            // Filter existing tasks of this type that are not completed
            List<TaskManagement> tasksOfType = existingTasks.stream()
                    .filter(t -> t.getTask() == taskType && t.getStatus() != TaskStatus.COMPLETED)
                    .collect(Collectors.toList());

            boolean alreadyAssigned = false;

            for (TaskManagement task : tasksOfType) {
                if (!task.getAssigneeId().equals(request.getAssigneeId())) {
                    // Cancel tasks assigned to other users
                    task.setStatus(TaskStatus.CANCELLED);
                    taskRepository.save(task);
                } else {
                    // Found task already assigned to same user, no need to reassign
                    alreadyAssigned = true;
                }
            }

            if (!alreadyAssigned) {
                // No task assigned to this user yet, create a new one
                TaskManagement newTask = new TaskManagement();
                newTask.setReferenceId(request.getReferenceId());
                newTask.setReferenceType(request.getReferenceType());
                newTask.setTask(taskType);
                newTask.setAssigneeId(request.getAssigneeId());
                newTask.setStatus(TaskStatus.ASSIGNED);
                taskRepository.save(newTask);
            }
        }

        return "Tasks reassigned successfully for reference " + request.getReferenceId();
    }

    @Override
    public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
        List<TaskManagement> tasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());

        Long start = request.getStartDate();
        Long end = request.getEndDate();

        List<TaskManagement> filteredTasks = tasks.stream()
                .filter(task -> {
                    Long deadline = task.getTaskDeadlineTime();
                    if (deadline == null) return false;

                    boolean isActive = !(task.getStatus() == TaskStatus.CANCELLED || task.getStatus() == TaskStatus.COMPLETED);

                    // Case 1: Task deadline falls within the requested date range
                    boolean withinDateRange = (deadline >= start && deadline <= end);

                    // Case 2: Task deadline is before range but task is still open
                    boolean overdueOpenTask = (deadline < start) && isActive;

                    return isActive && (withinDateRange || overdueOpenTask);
                })
                .collect(Collectors.toList());

        return taskMapper.modelListToDtoList(filteredTasks);
    }


    public List<TaskManagementDto> getAllTasksByPriority(Priority priority) {
        List<TaskManagement> tasks = taskRepository.findByTaskPriority(priority);
        return taskMapper.modelListToDtoList(tasks);
    }

    public List<TaskManagementDto> updateTaskPriority(UpdateTaskPriorityRequest request) {
        List<TaskManagement> updatedTasks = new ArrayList<>();
        Optional<TaskManagement> existingTask = taskRepository.findById(request.getTaskId());
        if (existingTask.isEmpty()) {
            throw new ResourceNotFoundException("Task not found with id: " + request.getTaskId());
        }
        TaskManagement task = existingTask.get();
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
            ((InMemoryTaskRepository) taskRepository).logActivity(task.getId(), "Priority updated to " + request.getPriority(), "adminUser");
        }

        updatedTasks.add(task);
        return taskMapper.modelListToDtoList(updatedTasks);
    }


    public void addComment(Long taskId, String author, String commentText) {
        if (taskRepository.findById(taskId).isEmpty()) {
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }
        ((InMemoryTaskRepository) taskRepository).addComment(taskId, author, commentText);
        ((InMemoryTaskRepository) taskRepository).logActivity(taskId, author + " commented on task", author);
    }






}
