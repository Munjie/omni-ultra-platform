package com.munjie.omni.pojo.dto;

import lombok.*;

/**
 * @Description: TODO
 * @author: Munjie
 * @date: 2024/10/27日 21:13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskDTO {
    private String taskName;
    private String email;
    private String fileName;
}
