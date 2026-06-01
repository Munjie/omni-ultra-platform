package com.munjie.omni.pojo.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ExportRequestDTO {
    private Integer taskId;
    private String title;
    private String filePath;

}