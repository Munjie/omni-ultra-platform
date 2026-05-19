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
 * @author muwenjie
 * @since 2024-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@TableName("excel_info")
public class ExcelInfoEntity implements Serializable {


    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ExcelIgnore
    private Integer id;

    /**
     * 任务id
     */
    @TableField("task_id")
    @ExcelIgnore
    private Integer taskId;

    /**
     * 序号
     */
//    @ExcelProperty(index = 0)
    @ExcelIgnore
    private Integer number;

    /**
     * 公里数
     */
    @ExcelProperty(value = "公里数")
    private String kilometres;

    /**
     * 始发城市
     */
    @TableField("starting_city")
    @ExcelProperty(value = "始发城市")
    private String startingCity;



    @TableField("starting_code")
    @ExcelProperty(value = "始发邮编")
    private String startingCode;

    /**
     * 到货编码
     */
    @TableField("arrival_code")
    @ExcelProperty(value = "到货邮编")
    private String arrivalCode;

    /**
     * 省份
     */
    @TableField("province")
    @ExcelProperty(value = "省份")
    private String province;

    /**
     * 城市
     */
    @TableField("city")
    @ExcelProperty(value = "城市")
    private String city;

    /**
     * 到货县区
     */
    @TableField("county")
    @ExcelProperty(value = "区县")
    private String county;

    /**
     * 详细地址
     */
    @TableField("address")
    @ExcelProperty(value = "详细地址")
    private String address;

    /**
     * sheet_name
     */
    @TableField("sheet_name")
    @ExcelIgnore
    private String sheetName;


    /**
     * 创建时间
     */
    @TableField("create_time")
    @ExcelIgnore
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @ExcelIgnore
    private Date updateTime;




}
