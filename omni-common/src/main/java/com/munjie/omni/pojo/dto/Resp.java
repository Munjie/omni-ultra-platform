package com.munjie.omni.pojo.dto;

import com.munjie.omni.pojo.entity.ScoreEntity;
import lombok.*;

import java.util.List;

/**
 * @Date 2023/12/18 17:07
 * @Author mwj
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
public class Resp {
    private List<ScoreEntity> importList;
}
