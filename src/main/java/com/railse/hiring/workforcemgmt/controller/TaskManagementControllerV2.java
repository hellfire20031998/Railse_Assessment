package com.railse.hiring.workforcemgmt.controller;


import com.railse.hiring.workforcemgmt.common.model.response.Response;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task-mgmt")
public class TaskManagementControllerV2 {

    private final TaskManagementService taskManagementService;


    public TaskManagementControllerV2(TaskManagementService taskManagementService) {
        this.taskManagementService = taskManagementService;
    }


    @GetMapping("/{id}")
    public Response<TaskManagementDto> getTaskById(@PathVariable Long id) {
        return new Response<>(taskManagementService.findTaskById(id));
    }


    @PostMapping("/create")
    public Response<List<TaskManagementDto>> createTasks(@RequestBody TaskCreateRequest request) {
        return new Response<>(taskManagementService.createTasks(request));
    }


    @PostMapping("/update")
    public Response<List<TaskManagementDto>> updateTasks(@RequestBody UpdateTaskRequest request) {
        return new Response<>(taskManagementService.updateTasks(request));
    }


    @PostMapping("/assign-by-ref")
    public Response<String> assignByReference(@RequestBody AssignByReferenceRequest request) {
        return new Response<>(taskManagementService.assignByReference(request));
    }



    @PostMapping("/fetch-by-date/v2")
    public Response<List<TaskManagementDto>> fetchByDate(@RequestBody TaskFetchByDateRequest request) {
        return new Response<>(taskManagementService.fetchTasksByDate(request));
    }

    @GetMapping("/tasks/priority/{priority}")
    public Response<List<TaskManagementDto>> getAllTasksByPriority(@PathVariable("priority") String priority) {
//        System.out.println("Received request to get tasks by priority: " + priority);
        return new Response<>(taskManagementService.getAllTasksByPriority(Priority.valueOf(priority.toUpperCase())));
    }


    @PutMapping("/update-task-priority")
    public Response<List<TaskManagementDto>> updateTaskPriority(@RequestBody UpdateTaskPriorityRequest request) {

        System.out.println("Received request to update task priority: " + request);
        return new Response<>(taskManagementService.updateTaskPriority(request));
    }

    @PostMapping("/tasks/{taskId}/comment")
    public ResponseEntity<Void> addComment(@PathVariable Long taskId, @RequestBody AddCommentRequestDto request) {
        taskManagementService.addComment(taskId, request.getAuthor(), request.getCommentText());
        return ResponseEntity.ok().build();
    }



}
