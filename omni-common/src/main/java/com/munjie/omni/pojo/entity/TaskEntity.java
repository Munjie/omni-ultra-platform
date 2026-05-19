package com.munjie.omni.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.munjie.omni.enums.TaskStatusEnum;
import com.munjie.omni.enums.TypeEnum;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author muwenjie
 * @since 2024-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@TableName("task_info")
public class TaskEntity implements Serializable {


    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 任务名称
     */
    @TableField("task_name")
    private String taskName;

    @TableField("title")
    private String title;

    /**
     * 任务状态
     */
    private TaskStatusEnum status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;



    @TableField("email")
    private String email;

    @TableField("file_name")
    private String fileName;

    @TableField("user_id")
    private Integer userId;


    /**
     * 任务类型
     */
    private TypeEnum type;

}
