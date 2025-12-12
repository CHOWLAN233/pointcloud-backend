package com.pointcloud.backend.controller;

import com.pointcloud.backend.entity.AiMove;
import com.pointcloud.backend.mapper.AiMoveMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/othello")
@CrossOrigin(origins = "*")
public class OthelloController {

    @Autowired
    private AiMoveMapper aiMoveMapper;

    /**
     * 核心接口：接收前端发来的棋盘，调用 C++ 算法，返回下一步
     */
    @PostMapping("/calculate")
    public Map<String, Object> calculateMove(@RequestBody Map<String, Object> request) {
        
        try {
            // 1. 解析参数
            // boardJson 原始格式是: "[[0,0,0...],[0,0...]]"
            String boardJson = request.get("board").toString(); 
            Integer turn = (Integer) request.get("turn");
            String level = (String) request.get("level");

            // 2. 存入数据库 (保留原始 JSON 格式以便回放)
            AiMove move = new AiMove();
            move.setBoardState(boardJson);
            move.setPlayerTurn(turn);
            move.setDifficulty(level);
            move.setCreateTime(LocalDateTime.now());
            aiMoveMapper.insert(move); 

            // 3. 【关键步骤】数据预处理
            // 将复杂的 JSON 二维数组字符串扁平化为纯数字字符串
            // 例如: "[[0, 0], [0, 1]]" -> "0001"
            // 这样 C++ 就不需要解析 JSON，直接读取 argv[1] 即可
            String flatBoard = boardJson.replaceAll("[\\[\\],\\s]", "");

            // 4. 调用 C++ 引擎计算
            // 注意：这里传入的是处理过的 flatBoard
            String result = callCppEngine(flatBoard, turn, level);
            
            // 5. 将计算结果更新回数据库
            move.setAiCoordinate(result);
            aiMoveMapper.updateById(move);

            // 6. 返回给前端
            return Map.of(
                "code", 0,
                "data", result, // 返回坐标，如 "3,4"
                "moveId", move.getMoveId()
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("code", 1, "msg", "Server Error: " + e.getMessage());
        }
    }

    /**
     * 辅助方法：寻找并调用 C++ 可执行文件
     */
    private String callCppEngine(String flatBoard, int turn, String level) throws Exception {
        
        // --- A. 自动寻找 ai_engine 文件路径 ---
        String projectDir = System.getProperty("user.dir");
        String os = System.getProperty("os.name").toLowerCase();
        String exeName = os.contains("win") ? "ai_engine.exe" : "ai_engine";
        
        File executableFile = null;
        
        // 尝试路径 1: 当前项目目录下 (backend/)
        File tryCurrent = new File(projectDir, exeName);
        // 尝试路径 2: 上级目录下 (项目根目录)
        File tryParent = new File(new File(projectDir).getParent(), exeName);

        if (tryCurrent.exists()) {
            executableFile = tryCurrent;
        } else if (tryParent.exists()) {
            executableFile = tryParent;
        } else {
            throw new Exception("找不到 C++ 程序: " + exeName + " (请检查文件是否在根目录或backend目录下)");
        }

        // --- B. 构建并执行命令 ---
        ProcessBuilder pb = new ProcessBuilder(
            executableFile.getCanonicalPath(), // C++ 程序路径
            flatBoard,                         // 参数1: 扁平化的棋盘
            String.valueOf(turn),              // 参数2: 轮次
            level                              // 参数3: 难度
        );

        // 启动进程
        Process process = pb.start();

        // --- C. 读取输出 (Stdout) ---
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine(); 

        // --- D. 读取错误日志 (Stderr) ---
        // 如果 C++ 那边有 std::cerr 输出，会在 Java 控制台打印出来，方便调试
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String errorLine;
        while ((errorLine = errorReader.readLine()) != null) {
            System.err.println("[C++ Log]: " + errorLine);
        }

        // 等待执行结束
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("C++ 进程异常退出，错误码: " + exitCode);
        }

        if (line == null || line.trim().isEmpty()) {
            return "ERROR: C++ 没有返回任何结果";
        }

        return line.trim();
    }
}
