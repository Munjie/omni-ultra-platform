package com.munjie.omni.pojo.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author mwj
 * @since 2023-11-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@TableName("student_info")
public class ScoreEntity implements Serializable {


    /**
     * 主键
     */
    @ExcelIgnore
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学号
     */
    @TableField("student_id")
//    @ExcelProperty(index = 0,value = {"学号"})
    private String studentId;

    /**
     * 考号
     */
    @TableField("test_id")
//    @ExcelProperty(index = 1)
    private String testId;

    /**
     * 姓名
     */
    @TableField("name")
//    @ExcelProperty(index = 2)
    private String name;


    /**
     * 行政班级
     */
    @TableField("lesson")
//    @ExcelProperty(index = 3)
    private String lesson;



    /**
     * 学校
     */
    @TableField("school")
//    @ExcelProperty(index = 4)
    private String school;



    /**
     * 分数
     */
    @TableField("geography_core")
//    @ExcelProperty(index = 6)
    private Double geographyScore;

  /*  *//**
     * 等级
     *//*
    @TableField("level")
    @ExcelProperty(index = 8)
    private String level;*/

/*    *//**
     * 创建时间
     *//*
    @TableField("create_time")
    private Date createTime;

    *//**
     * 更新时间
     *//*
    @TableField("update_time")
    private Date updateTime;*/

    @ExcelIgnore
    @TableField("task_id")
    private Integer taskId;

    @ExcelIgnore
    @TableField("total_score")
    private Double totalScore;

}
