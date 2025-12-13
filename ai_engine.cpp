/**
 * Othello AI Engine (C++ Backend)
 * Feature: JSON Tree Serialization + Coordinate Fix
 */

#include <iostream>
#include <vector>
#include <random>
#include <string>
#include <cmath>
#include <ctime>
#include <algorithm>
#include <iomanip>

using namespace std;

const int DIR_X[] = {-1, 0, 1, -1, 1, -1, 0, 1};
const int DIR_Y[] = {-1, -1, -1, 0, 0, 1, 1, 1};
const int BOARD_SIZE = 8;

class Move {
public:
    Move() = default;
    Move(int _x, int _y, char _type) : x(_x), y(_y), type(_type) {};
    int x = -1; // Col
    int y = -1; // Row
    char type = ' '; 
};

enum class Result { WIN, LOSE, DRAW, CONTINUE };

class State {
public:
    vector<char> board; 
    char next_turn = ' '; 
    int width = BOARD_SIZE;
    int height = BOARD_SIZE;

    State() { board.resize(width * height, ' '); }

    void load_from_string(string boardStr) {
        for (int i = 0; i < height; ++i) {     
            for (int j = 0; j < width; ++j) {  
                int index = i * width + j;
                char val = ' ';
                if (index < boardStr.length()) {
                    char c = boardStr[index];
                    if (c == '1') val = 'X'; else if (c == '2') val = 'O'; 
                    else val = ' ';
                }
                board[index] = val;
            }
        }
    }

    char get(int x, int y) const { 
        if (x < 0 || x >= width || y < 0 || y >= height) return 'E'; 
        return board[y * width + x]; 
    }

    void set_raw(int x, int y, char val) {
        if (x >= 0 && x < width && y >= 0 && y < height) board[y * width + x] = val;
    }

    bool is_valid_move(int x, int y, char player) const {
        if (get(x, y) != ' ') return false; 
        char opponent = (player == 'X') ? 'O' : 'X';
        for (int d = 0; d < 8; d++) {
            int r = y + DIR_Y[d], c = x + DIR_X[d];
            bool saw_opponent = false;
            while (r >= 0 && r < height && c >= 0 && c < width) {
                char current = get(c, r);
                if (current == opponent) { saw_opponent = true; r += DIR_Y[d]; c += DIR_X[d]; } 
                else if (current == player) { if (saw_opponent) return true; else break; } 
                else break; 
            }
        }
        return false;
    }

    void apply_move(int x, int y) {
        if (x == -1 && y == -1) { next_turn = (next_turn == 'X') ? 'O' : 'X'; return; }
        set_raw(x, y, next_turn);
        char my_color = next_turn, opp_color = (next_turn == 'X') ? 'O' : 'X';
        for (int d = 0; d < 8; d++) {
            int r = y + DIR_Y[d], c = x + DIR_X[d];
            vector<pair<int, int>> potential_flips;
            while (r >= 0 && r < height && c >= 0 && c < width) {
                char current = get(c, r);
                if (current == opp_color) { potential_flips.push_back({c, r}); r += DIR_Y[d]; c += DIR_X[d]; } 
                else if (current == my_color) { for (auto p : potential_flips) set_raw(p.first, p.second, my_color); break; } 
                else break;
            }
        }
        next_turn = opp_color; 
    }

    vector<Move> get_avilable_list() {
        vector<Move> list;
        for (int i = 0; i < height; ++i) 
            for (int j = 0; j < width; ++j) 
                if (is_valid_move(j, i, next_turn)) list.push_back(Move(j, i, next_turn));
        return list;
    }

    bool is_game_over() {
        vector<Move> my_moves = get_avilable_list();
        if (!my_moves.empty()) return false;
        char original_turn = next_turn;
        next_turn = (next_turn == 'X') ? 'O' : 'X';
        bool opp_has_moves = !get_avilable_list().empty();
        next_turn = original_turn; 
        return !opp_has_moves; 
    }
};

class Node {
public:
    State state;
    Move action;
    Node* parent = nullptr;
    vector<Node*> children;
    vector<Move> available_moves; 
    int wins = 0;
    int visits = 0;

    Node(State _state, Node* _parent, Move _action) : state(_state), parent(_parent), action(_action) {
        available_moves = state.get_avilable_list();
    }
    ~Node() { for (Node* child : children) delete child; }
    bool is_fully_expanded() { return available_moves.empty(); }
    bool is_terminal() { return available_moves.empty() && children.empty() && state.get_avilable_list().empty(); }
};

class Opener {
public:
    void calculate_one_move(string boardStr, int turn, int simulation_limit);
private:
    Node* select(Node* node);
    Node* expand(Node* node);
    int simulate(Node* node); 
    void backpropagate(Node* node, int result);
    string serialize_tree(Node* node, int depth); // Recursion for Tree JSON

    double exploration_constant = 1.414;
    default_random_engine rng;
};

// 递归构建 JSON 树
string Opener::serialize_tree(Node* node, int depth) {
    if (depth > 3) return ""; // 限制深度
    string s = "{";
    // 输出 y,x (Row, Col) 以匹配前端习惯
    s += "\"move\":\"" + to_string(node->action.y) + "," + to_string(node->action.x) + "\",";
    s += "\"visits\":" + to_string(node->visits) + ",";
    double winRate = (node->visits > 0) ? (double)node->wins / node->visits : 0.0;
    s += "\"rate\":" + to_string(winRate).substr(0, 4) + ","; 
    s += "\"children\":[";
    
    // 拷贝并排序子节点用于展示
    vector<Node*> sortedChildren = node->children;
    sort(sortedChildren.begin(), sortedChildren.end(), [](Node* a, Node* b) {
        return a->visits > b->visits;
    });

    bool first = true;
    for (Node* child : sortedChildren) {
        // 只展示被访问过的节点
        if (child->visits > 0) {
            string childJson = serialize_tree(child, depth + 1);
            if (!childJson.empty()) {
                if (!first) s += ",";
                s += childJson;
                first = false;
            }
        }
    }
    s += "]}";
    return s;
}

void Opener::calculate_one_move(string boardStr, int turn, int simulation_limit) {
    rng.seed(time(NULL));
    State rootState;
    rootState.load_from_string(boardStr);
    rootState.next_turn = (turn == 1) ? 'X' : 'O';

    if (rootState.get_avilable_list().empty()) { cout << "-1,-1" << endl; return; }

    Node* root = new Node(rootState, nullptr, Move(-1, -1, ' '));
    int iterations = 0;
    while (iterations < simulation_limit) {
        Node* node = root;
        while (node->is_fully_expanded() && !node->children.empty()) node = select(node);
        if (!node->is_fully_expanded() && !node->state.is_game_over()) node = expand(node);
        int result = simulate(node);
        backpropagate(node, result);
        iterations++;
    }

    Node* bestChild = nullptr;
    int maxVisits = -1;
    for (Node* child : root->children) {
        if (child->visits > maxVisits) { maxVisits = child->visits; bestChild = child; }
    }

    // 生成可视化数据
    string visData = "";
    if (simulation_limit < 50) {
        // 详细模式：JSON Tree
        visData = "TREE:" + serialize_tree(root, 0);
    } else {
        // 简略模式：进度条 (BAR)
        // 排序
        vector<Node*> sortedChildren = root->children;
        sort(sortedChildren.begin(), sortedChildren.end(), [](Node* a, Node* b) {
            return a->visits > b->visits;
        });
        
        for (size_t i = 0; i < sortedChildren.size() && i < 5; ++i) { 
            Node* c = sortedChildren[i];
            if (c->visits > 0) {
                double rate = (double)c->wins / c->visits;
                visData += to_string(c->action.y) + "," + to_string(c->action.x) + ":" 
                         + to_string(c->visits) + ":" 
                         + to_string(rate) + ";";
            }
        }
        visData = "BAR:" + visData; 
    }

    if (bestChild == nullptr) { cout << "-1,-1" << endl; } 
    else { cout << bestChild->action.y << "," << bestChild->action.x << "|" << visData << endl; }
    delete root; 
}

Node* Opener::select(Node* node) {
    Node* bestNode = nullptr; double bestUCB = -999999.0;
    for (Node* child : node->children) {
        double ucb = ((double)child->wins / child->visits) + exploration_constant * sqrt(log(node->visits) / child->visits);
        if (ucb > bestUCB) { bestUCB = ucb; bestNode = child; }
    }
    return bestNode;
}
Node* Opener::expand(Node* node) {
    if (node->available_moves.empty()) return node; 
    uniform_int_distribution<int> dist(0, node->available_moves.size() - 1);
    int idx = dist(rng);
    Move move = node->available_moves[idx];
    node->available_moves.erase(node->available_moves.begin() + idx);
    State newState = node->state; newState.apply_move(move.x, move.y);
    Node* childNode = new Node(newState, node, move);
    node->children.push_back(childNode);
    return childNode;
}
int Opener::simulate(Node* node) {
    State simState = node->state; int depth = 0;
    while (!simState.is_game_over() && depth < 60) {
        vector<Move> moves = simState.get_avilable_list();
        if (moves.empty()) simState.apply_move(-1, -1);
        else {
            uniform_int_distribution<int> dist(0, moves.size() - 1);
            Move randomMove = moves[dist(rng)];
            simState.apply_move(randomMove.x, randomMove.y);
        }
        depth++;
    }
    int b=0, w=0; for(char c : simState.board) { if(c=='X') b++; else if(c=='O') w++; }
    if (b > w) return 1; if (w > b) return -1; return 0; 
}
void Opener::backpropagate(Node* node, int result) {
    while (node != nullptr) {
        node->visits++;
        if (node->action.type == 'X' && result == 1) node->wins++;
        else if (node->action.type == 'O' && result == -1) node->wins++; 
        node = node->parent;
    }
}

int main(int argc, char* argv[]) {
    if (argc < 3) return 1;
    string boardStr = argv[1];
    int turn = 1; try { turn = stoi(argv[2]); } catch(...) {}
    int sims = 500; 
    if(argc > 3) { try { sims = stoi(argv[3]); } catch(...) {} }
    Opener ai; ai.calculate_one_move(boardStr, turn, sims);
    return 0;
}