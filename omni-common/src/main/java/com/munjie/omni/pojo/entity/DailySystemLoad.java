package com.munjie.omni.pojo.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("daily_system_load")
public class DailySystemLoad {

    @TableId(type = IdType.NONE)  // date 不是自增主键
    private LocalDate date;


    @TableField("average_cpu_load")
    private double averageCpuLoad = 0.0;  // 平均 CPU 使用率（%）


    @TableField("average_memory_load")
    private double averageMemoryLoad = 0.0;  // 平均内存使用率
}
