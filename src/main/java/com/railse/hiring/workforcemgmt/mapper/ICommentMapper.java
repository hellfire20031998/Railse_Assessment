package com.railse.hiring.workforcemgmt.mapper;


import com.railse.hiring.workforcemgmt.dto.CommentDto;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ICommentMapper {

    CommentDto modelToDto(TaskComment comment);

    TaskComment dtoToModel(CommentDto dto);

    List<CommentDto> modelListToCommentDtoList(List<TaskComment> comments);

    List<TaskComment> dtoListToModelList(List<CommentDto> dtos);
}

