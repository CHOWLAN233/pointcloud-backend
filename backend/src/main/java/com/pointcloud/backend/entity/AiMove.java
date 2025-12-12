package com.pointcloud.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data // Lombok 自动生成 Getter/Setter
@TableName("ai_moves") // 关键！告诉 MyBatis-Plus 对应数据库里的 ai_moves 表
public class AiMove {

    // 对应 SQL: move_id VARCHAR(64) PRIMARY KEY
    @TableId(type = IdType.ASSIGN_ID) // 启用雪花算法生成长数字 ID
    private String moveId;

    // 对应 SQL: board_state TEXT
    // 雖然数据库是 TEXT，但在 Java 里就是一个长长的 String
    private String boardState;

    // 对应 SQL: player_turn INT
    private Integer playerTurn;

    // 对应 SQL: ai_coordinate VARCHAR(20)
    private String aiCoordinate;

    // 对应 SQL: difficulty VARCHAR(20)
    private String difficulty;

    // 对应 SQL: create_time DATETIME
    private LocalDateTime createTime;
}
