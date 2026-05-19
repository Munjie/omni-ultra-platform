package com.munjie.omni.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@TableName("ai_model_info")
public class AiModelInfoEntity {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 模型展示名称
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 模型
     */
    @TableField("model_value")
    private String modelValue;

    /**
     * 模型描述
     */
    @TableField("model_desc")
    private String modelDesc;


    @TableField("base_url")
    private String baseUrl;

    @TableField("api_key")
    private String apiKey;

    @TableField("rate_limit")
    private Integer rateLimit ;


    @TableField("provider_type")
    private String providerType;

}
