package com.pointcloud.backend.controller;

import com.pointcloud.backend.entity.AiMove;
import com.pointcloud.backend.mapper.AiMoveMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Map;
import java.io.File;

@RestController
@RequestMapping("/othello")
public class OthelloController {

    @Autowired
    private AiMoveMapper aiMoveMapper;

    // 接收前端发来的棋盘，计算并返回下一步
    @PostMapping("/calculate")
    public Map<String, Object> calculateMove(@RequestBody Map<String, Object> request) {
        
        // 1. 解析参数 (从 JSON 里把数据取出来)
        // 这里的 board 我们先简单转成字符串存起来
        String boardJson = request.get("board").toString(); 
        Integer turn = (Integer) request.get("turn");
        String level = (String) request.get("level");

        // 2. 存入数据库 (留档)
        AiMove move = new AiMove();
        move.setBoardState(boardJson);
        move.setPlayerTurn(turn);
        move.setDifficulty(level);
        move.setCreateTime(LocalDateTime.now());
        
        aiMoveMapper.insert(move); // 插入数据库

        // 3. 【核心】调用 Python 脚本计算
        String result = callAiScript(boardJson, turn, level);
        
        // 4. 将计算结果更新回数据库
        move.setAiCoordinate(result);
        aiMoveMapper.updateById(move);

        // 5. 返回给前端
        return Map.of(
            "code", 0,
            "data", result, // 例如返回 "3,4"
            "moveId", move.getMoveId()
        );
    }

    /**
     * 这里是 Java 调用 Python 的桥梁
     * 类似于 C++ 的 system() 或 exec()
     */
    private String callAiScript(String board, int turn, String level) {
        try {
            // 获取项目根目录路径 (确保能找到脚本)
            String projectDir = System.getProperty("user.dir");
            // 假设脚本名叫 ai_engine.py，放在项目根目录下
            String scriptPath = projectDir + File.separator + "ai_engine.py";

            // 构建命令: python3 ai_engine.py [参数1] [参数2] [参数3]
            ProcessBuilder pb = new ProcessBuilder(
                "python3", 
                scriptPath, 
                board, 
                String.valueOf(turn), 
                level
            );
            
            // 启动进程
            Process process = pb.start();
            
            // 读取进程的标准输出 (Stdout)
            // 也就是读取 Python 脚本里 print() 打印的内容
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine(); // 读取第一行
            
            // 等待脚本执行结束
            process.waitFor();

            if (line == null || line.trim().isEmpty()) {
                return "ERROR: No Output";
            }
            return line; // 返回 AI 算出来的坐标
            
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }
}
