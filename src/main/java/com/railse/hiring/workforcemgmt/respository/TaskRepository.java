package com.railse.hiring.workforcemgmt.respository;

import com.railse.hiring.workforcemgmt.model.TaskActivity;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.ReferenceType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository {
    Optional<TaskManagement> findById(Long id);
    TaskManagement save(TaskManagement task);
    List<TaskManagement> findAll();
    List<TaskManagement> findByReferenceIdAndReferenceType(Long referenceId, ReferenceType referenceType);
    List<TaskManagement> findByAssigneeIdIn(List<Long> assigneeIds);

    List<TaskManagement> findByTaskPriority(Priority priority);

    void logActivity(Long taskId, String message, String author);

    List<TaskActivity> getActivitiesByTaskId(Long taskId);
    List<TaskComment> getCommentsByTaskId(Long taskId);
    TaskActivity addActivityLog(Long taskId, TaskActivity activity);
    TaskComment addComment(Long taskId, String author, String commentText);

}
