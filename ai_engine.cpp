#include <iostream>
#include <string>
#include <vector>
#include <cstdlib> // for rand()
#include <ctime>   // for time()

// 模拟 AI 引擎
int main(int argc, char* argv[]) {
    // 1. 接收参数 (对应 Java ProcessBuilder 传过来的参数)
    // argv[0] 是程序名
    if (argc < 4) {
        std::cerr << "Error: Missing arguments" << std::endl;
        return 1;
    }

    std::string boardJson = argv[1]; // 棋盘字符串
    std::string turn = argv[2];      // 轮次
    std::string level = argv[3];     // 难度

    // 2. 解析棋盘 (这里为了演示，只打印接收到的信息，实际项目中你需要解析 JSON)
    // std::cout << "Debug: C++ received turn " << turn << std::endl; 
    // 注意：不要输出任何多余的调试信息到 cout，否则 Java 会读错！调试信息请用 cerr

    // 3. 假装在思考 (AI 算法核心)
    // ... running alpha-beta pruning ...
    
    // 4. 生成结果
    // 初始化随机数种子
    std::srand(std::time(0));
    int x = std::rand() % 8;
    int y = std::rand() % 8;

    // 5. 输出结果 (这是 Java 唯一能读到的东西)
    std::cout << x << "," << y << std::endl;

    return 0;
}