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
import com.fasterxml.jackson.databind.ObjectMapper; 
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;

@RestController
@RequestMapping("/othello")
@CrossOrigin(origins = "*") 
public class OthelloController {

    @Autowired
    private AiMoveMapper aiMoveMapper;

    @PostMapping("/calculate")
    public Map<String, Object> calculateMove(@RequestBody Map<String, Object> request) {
        try {
            String boardJson = request.get("board").toString(); 
            Integer turn = (Integer) request.get("turn");
            // 兼容 Range Slider 数值
            String level = request.get("level").toString();

            AiMove move = new AiMove();
            move.setBoardState(boardJson);
            move.setPlayerTurn(turn);
            move.setDifficulty(level);
            move.setCreateTime(LocalDateTime.now());
            aiMoveMapper.insert(move); 

            // 数据预处理 (JSON -> Flat String)
            String flatBoard = "";
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<List<Integer>> boardList = mapper.readValue(boardJson, new TypeReference<List<List<Integer>>>(){});
                StringBuilder sb = new StringBuilder();
                for (List<Integer> row : boardList) for (Integer cell : row) sb.append(cell);
                flatBoard = sb.toString();
            } catch (Exception e) {
                flatBoard = boardJson.replaceAll("[\\[\\],\\s]", "");
            }

            // 调用 C++ (格式: "3,4|TREE:{...}" 或 "3,4|BAR:...")
            String rawResult = callCppEngine(flatBoard, turn, level);
            
            String coordinate = rawResult;
            String debugInfo = ""; 

            if (rawResult.contains("|")) {
                String[] parts = rawResult.split("\\|");
                coordinate = parts[0];
                if (parts.length > 1) debugInfo = parts[1];
            }
            
            move.setAiCoordinate(coordinate);
            aiMoveMapper.updateById(move);

            return Map.of(
                "code", 0,
                "data", coordinate, 
                "visualization", debugInfo, 
                "moveId", move.getMoveId()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("code", 1, "msg", "Error: " + e.getMessage());
        }
    }

    private String callCppEngine(String flatBoard, int turn, String level) throws Exception {
        String projectDir = System.getProperty("user.dir");
        String exeName = System.getProperty("os.name").toLowerCase().contains("win") ? "ai_engine.exe" : "ai_engine";
        
        File executableFile = new File(projectDir, exeName);
        if (!executableFile.exists()) executableFile = new File(new File(projectDir).getParent(), exeName);
        if (!executableFile.exists()) throw new Exception("Cannot find " + exeName);

        ProcessBuilder pb = new ProcessBuilder(executableFile.getCanonicalPath(), flatBoard, String.valueOf(turn), level);
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine(); 
        int exitCode = process.waitFor();
        if (exitCode != 0) throw new Exception("C++ Error");
        return (line == null) ? "ERROR" : line.trim();
    }
}