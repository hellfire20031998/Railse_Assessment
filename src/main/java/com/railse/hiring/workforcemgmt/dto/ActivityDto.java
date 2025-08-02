package com.railse.hiring.workforcemgmt.dto;


import lombok.Data;

@Data
public class ActivityDto {


    private Long id;

    private Long taskId;

    private String description; // e.g., "User A assigned this task to User B"
    private Long timestamp;
}
