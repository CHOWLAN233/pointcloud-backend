package com.pointcloud.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data//Lombok automatically generates Getter and Setter
@TableName("ai_moves")//Tell MyBatis-Plus the ai_moves table in the corresponding database
public class AiMove {

    //Corresponding SQL: move_id VARCHAR(64) PRIMARY KEY
    @TableId(type = IdType.ASSIGN_ID)// Generate long numeric IDs using the Snowflake algorithm
    private String moveId;

    //Corresponding SQL: board_state TEXT
    private String boardState;

    //Corresponding SQL: player_turn INT
    private Integer playerTurn;

    //Corresponding SQL: ai_coordinate VARCHAR(20)
    private String aiCoordinate;

    //Corresponding SQL: difficulty VARCHAR(20)
    private String difficulty;

    //Corresponding SQL: create_time DATETIME
    private LocalDateTime createTime;
}
