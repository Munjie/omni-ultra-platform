package com.munjie.omni.pojo.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AiModelInfoVO {


    private Integer id;

    /**
     * 模型展示名称
     */
    @TableField("model_name")
    private String modelName;


    /**
     * 模型描述
     */
    @TableField("model_desc")
    private String modelDesc;




}
