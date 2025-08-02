package com.railse.hiring.workforcemgmt.dto;

import lombok.Data;

@Data
public class CommentDto {
    private Long id;

    private Long taskId;

    private String author; // or userId if linked to user table
    private String comment;

    private Long timestamp;
}
