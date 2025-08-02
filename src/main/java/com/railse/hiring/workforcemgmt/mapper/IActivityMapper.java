package com.railse.hiring.workforcemgmt.mapper;

import com.railse.hiring.workforcemgmt.dto.ActivityDto;
import com.railse.hiring.workforcemgmt.model.TaskActivity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IActivityMapper {

    ActivityDto modelToDto(TaskActivity model);

    TaskActivity dtoToModel(ActivityDto dto);

    List<ActivityDto> modelListToActivityDtoList(List<TaskActivity> models);

    List<TaskActivity> dtoListToModelList(List<ActivityDto> dtos);
}

