package com.munjie.omni.pojo.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author mwj
 * @since 2024-11-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@TableName("student_score")
public class TotalScoreEntity implements Serializable {


    /**
     * 主键
     */
    @ExcelIgnore
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 班级
     */
    @TableField("class_name")
    @ExcelProperty(value = "班级")
    private String className;

    /**
     * 学生姓名
     */
    @TableField("student_name")
    @ExcelProperty(value = "姓名")
    private String studentName;

    /**
     * 语文
     */
    @ExcelProperty(value = "语文")
    private Double chinese;

    /**
     * 数学
     */
    @ExcelProperty(value = "数学")
    private Double math;

    /**
     * 英语
     */
    @ExcelProperty(value = "英语")
    private Double english;

    /**
     * 物理
     */
    @ExcelProperty(value = "物理")
    private Double physics;

    /**
     * 化学
     */
    @ExcelProperty(value = "化学")
    private Double chemistry;


    /**
     * 政治
     */
    @ExcelProperty(value = "政治")
    private Double politics;

    /**
     * 历史
     */
    @ExcelProperty(value = "历史")
    private Double history;

    /**
     * 政治
     */
    @ExcelProperty(value = "地理")
    private Double geography;

    /**
     * 历史
     */
    @ExcelProperty(value = "生物")
    private Double biology;


    /**
     * 总分
     */
    @ExcelProperty(value = "总分")
    @TableField("total_score")
    private Double totalScore;


    /**
     * 排名
     */
    @ExcelProperty(value = "P")
    @TableField("ranking")
    private Integer ranking;

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


}
