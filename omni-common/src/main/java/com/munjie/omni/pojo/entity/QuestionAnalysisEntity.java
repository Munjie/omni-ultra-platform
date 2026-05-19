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
 * 试题分析表
 * </p>
 *
 * @author muwenjie
 * @since 2024-06-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@TableName("question_analysis")
public class QuestionAnalysisEntity implements Serializable {


    /**
     * 主键
     */
    @ExcelIgnore
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 题号
     */
    @TableField("question_number")
    @ExcelProperty(index = 0)
    private String questionNumber;

    /**
     * 小题号
     */
    @TableField("sub_question_number")
    @ExcelIgnore
    private Integer subQuestionNumber;

    /**
     * 答案
     */
    @TableField("answer")
    @ExcelProperty(index = 2)
    private String answer;

    /**
     * 人数
     */
    @TableField("num_people")
    @ExcelProperty(index = 3)
    private Integer numPeople;

    /**
     * 最高分
     */
    @TableField("max_score")
    @ExcelProperty(index = 4)
    private Double maxScore;

    /**
     * 最低分
     */
    @TableField("min_score")
    @ExcelProperty(index = 5)
    private Double minScore;

    /**
     * 平均分
     */
    @TableField("average_score")
    @ExcelProperty(index = 6)
    private Double averageScore;

    /**
     * 标准差
     */
    @TableField("standard_deviation")
    @ExcelProperty(index = 7)
    private Double standardDeviation;

    /**
     * 得分率
     */
    @TableField("score_rate")
    @ExcelProperty(index = 8)
    private Double scoreRate;

    /**
     * 满分率
     */
    @TableField("full_score_rate")
    @ExcelProperty(index = 9)
    private Double fullScoreRate;

    /**
     * 零分率
     */
    @TableField("zero_score_rate")
    @ExcelProperty(index = 10)
    private Double zeroScoreRate;

    /**
     * 难度
     */
    @TableField("difficulty")
    @ExcelProperty(index = 11)
    private Double difficulty;

    /**
     * 班级
     */
    @ExcelIgnore
    @TableField("class_name")
    private String className;

    /**
     * 创建时间
     */
    @ExcelIgnore
    @TableField("create_time")
    private Date createTime;


    @TableField("task_id")
    @ExcelIgnore
    private Integer taskId;

}
