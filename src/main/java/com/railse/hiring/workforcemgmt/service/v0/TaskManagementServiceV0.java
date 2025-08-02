package com.railse.hiring.workforcemgmt.service.v0;

import com.railse.hiring.workforcemgmt.dto.*;


import java.util.List;


public interface TaskManagementServiceV0 {
    List<TaskManagementDto> createTasks(TaskCreateRequest request);
    List<TaskManagementDto> updateTasks(UpdateTaskRequest request);
    String assignByReference(AssignByReferenceRequest request);
    List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request);
    TaskManagementDto findTaskById(Long id);
}
