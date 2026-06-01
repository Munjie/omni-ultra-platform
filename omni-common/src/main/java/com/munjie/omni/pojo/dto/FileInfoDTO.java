package com.munjie.omni.pojo.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FileInfoDTO {
    /**文件名*/
    private String fileName;

//    **文件路径或base64字符串*/
    private String file;
}
