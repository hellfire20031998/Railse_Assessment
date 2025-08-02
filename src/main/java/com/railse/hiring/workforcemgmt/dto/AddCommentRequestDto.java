package com.railse.hiring.workforcemgmt.dto;

import lombok.Data;

@Data
public class AddCommentRequestDto {
    private String author;
    private String commentText;
}
