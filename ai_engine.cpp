//Othello AI Engine (C++ Backend)
//The core of the Othello AI is primarily responsible for calculating the optimal move for the next move

#include <iostream>
#include <vector>
#include <random>
#include <string>
#include <cmath>
#include <ctime>
#include <algorithm>
#include <iomanip>

using namespace std;

//Define an offset to check if the opponent's piece can be captured
const int DIR_X[] = {-1, 0, 1, -1, 1, -1, 0, 1};
const int DIR_Y[] = {-1, -1, -1, 0, 0, 1, 1, 1};

//Define the size of the chessboard
const int BOARD_SIZE = 8;

//A class used to store coordinates and actors
class Move{
    public:
        Move() = default;
        Move(int _x, int _y, char _type) : x(_x), y(_y), type(_type) {};
        int x = -1; int y = -1; char type = ' '; 
};

//A class used to determine the state of the chessboard
class State{
    public:
        vector<char> board; //Use an array to store the chessboard
        char next_turn = ' '; 
        int width = BOARD_SIZE; 
        int height = BOARD_SIZE;
        State() { board.resize(width * height, ' '); }

    //Parse the state of the chessboard from the array
        void load_from_string(string boardStr){
            for (int i = 0; i < height; ++i) 
                for (int j = 0; j < width; ++j) 
                {  
                    int index = i * width + j; 
                    char val = ' ';
                    if (index < boardStr.length()) 
                    {
                        char c = boardStr[index];
                            if (c == '1') 
                                val = 'X';
                            else if (c == '2') 
                                val = 'O'; 
                }
                board[index] = val;
            }
        }
    
        //Retrieve the piece at the specified position,return E if it exceeds the bounds
        char get(int x, int y) const { if (x < 0 || x >= width || y < 0 || y >= height) return 'E'; return board[y * width + x]; }

        //Set a chess piece at a specified position
        void set_raw(int x, int y, char val) { if (x >= 0 && x < width && y >= 0 && y < height) board[y * width + x] = val; }

        //Use the rules of Othello to determine if this move is legal
        bool is_valid_move(int x, int y, char player) const{
            if (get(x, y) != ' ') 
                return false; 
            char opponent = (player == 'X') ? 'O' : 'X';
        
            // Traverse 8 directions
            for (int d = 0; d < 8; d++) 
            {
                int r = y + DIR_Y[d], c = x + DIR_X[d]; 
                bool saw_opponent = false;// Continue moving in this direction until you go out of bounds or encounter a non-opponent piece
                while (r >= 0 && r < height && c >= 0 && c < width)
                    {
                        char current = get(c, r);
                        if (current == opponent) 
                        { 
                            saw_opponent = true; 
                            r += DIR_Y[d]; 
                            c += DIR_X[d];
                        } // If see our opponent's piece and then our own piece, it means we formed a pincer attack
                        else if (current == player) 
                        { 
                            if (saw_opponent) 
                                return true; 
                            else 
                                break; 
                        } 
                        else 
                            break; 
                }
            }
            return false;// This direction is invalid if an empty space is encountered
    }

    //The core code for playing chess and flipping pieces
    void apply_move(int x, int y) {
        if (x == -1 && y == -1) 
        { 
            next_turn = (next_turn == 'X') ? 'O' : 'X'; 
            return; 
        }
        set_raw(x, y, next_turn);
        char my_color = next_turn, opp_color = (next_turn == 'X') ? 'O' : 'X';
        for (int d = 0; d < 8; d++) 
        {
            int r = y + DIR_Y[d], c = x + DIR_X[d]; 
            vector<pair<int, int>> potential_flips;
            while (r >= 0 && r < height && c >= 0 && c < width) 
            {
                char current = get(c, r);
                if (current == opp_color) 
                { 
                    potential_flips.push_back({c, r}); 
                    r += DIR_Y[d]; 
                    c += DIR_X[d]; 
                } 
                else if (current == my_color) 
                { 
                    for (auto p : potential_flips) 
                        set_raw(p.first, p.second, my_color); 
                        break; 
                } 
                else 
                    break;
            }
        }
        next_turn = opp_color; 
    }

    //// Get all currently valid move positions
    vector<Move> get_avilable_list() {
        vector<Move> list;
        for (int i = 0; i < height; ++i) 
            for (int j = 0; j < width; ++j) 
                if (is_valid_move(j, i, next_turn)) 
                    list.push_back(Move(j, i, next_turn));
        return list;
    }

    // Determine if the game has ended
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

// MCTS algorithm
class Node {
public:
    State state;// The chessboard state corresponding to this node
    Move action;// Actions taken upon reaching this state
    Node* parent = nullptr; // Parent node
    vector<Node*> children;// List of child nodes
    vector<Move> available_moves; // Actions not yet attempted in this state
    int wins = 0;// Number of wins
    int visits = 0;// Number of simulations
    Node(State _state, Node* _parent, Move _action) : state(_state), parent(_parent), action(_action) {
        available_moves = state.get_avilable_list();
    }
    ~Node() { for (Node* child : children) delete child; }
    bool is_fully_expanded() { return available_moves.empty(); }//Determine if the node has been fully expanded
};

//AI Core Logic Controller
class Opener {
public:
    void calculate_one_move(string boardStr, int turn, int simulation_limit, double c_param);
private:
    // Four standard steps of MCTS
    Node* select(Node* node);//1.selection
    Node* expand(Node* node);//2.expansion
    int simulate(Node* node); //3.simulation
    void backpropagate(Node* node, int result);//4.backtracking
    string serialize_tree(Node* node, int depth);//Serialize the search tree into a JSON string
    double exploration_constant = 1.414; 
    default_random_engine rng;//Random number generator
};

// Recursively generate a JSON tree structure for front-end visualization, showing each branch of the AI's thought process.
string Opener::serialize_tree(Node* node, int depth) {
    if (depth > 60) return ""; //Depth limit to prevent JSON from becoming too large
    
    //Output the current node's actions, number of visits, and win rate
    string s = "{";
    s += "\"move\":\"" + to_string(node->action.y) + "," + to_string(node->action.x) + "\",";
    s += "\"visits\":" + to_string(node->visits) + ",";
    s += "\"wins\":" + to_string(node->wins) + ",";
    double winRate = (node->visits > 0) ? (double)node->wins / node->visits : 0.0;
    s += "\"rate\":" + to_string(winRate).substr(0, 4) + ",";
    s += "\"children\":[";
    //Sort child nodes by access frequency, placing important nodes at the top.
    vector<Node*> sortedChildren = node->children;
    sort(sortedChildren.begin(), sortedChildren.end(), [](Node* a, Node* b) { return a->visits > b->visits; });

    bool first = true;
    for (Node* child : sortedChildren) {
        if (child->visits > 0) {
            string childJson = serialize_tree(child, depth + 1);
            if (!childJson.empty()) {
                if (!first) s += ",";
                s += childJson; first = false;
            }
        }
    }
    s += "]}";
    return s;
}

// Main logic: MCTS loop
void Opener::calculate_one_move(string boardStr, int turn, int simulation_limit, double c_param) {
    rng.seed(time(NULL));
    exploration_constant = c_param;
    
    //1. Initialize the root node
    State rootState; 
    rootState.load_from_string(boardStr); 
    rootState.next_turn = (turn == 1) ? 'X' : 'O';
    if (rootState.get_avilable_list().empty()) 
    { 
        cout << "-1,-1" << endl; 
        return; 
    }

    Node* root = new Node(rootState, nullptr, Move(-1, -1, ' '));

    //2. Start simulating the loop
    int iterations = 0;
    while (iterations < simulation_limit) {
        Node* node = root;
        
        //Selection: Proceed down the tree until you find a node that is not fully expanded.
        while (node->is_fully_expanded() && !node->children.empty()) node = select(node);
        
        //Expansion: If the game is not over, expand to a new child node.
        if (!node->is_fully_expanded() && !node->state.is_game_over()) node = expand(node);
        
        //Simulation: Start playing chess quickly and randomly from this new node until a winner is determined.
        int result = simulate(node);
        
        //Backpropagation: Tells the result to all parent nodes on the path.
        backpropagate(node, result);
        iterations++;
    }

    //3. Choosing the best move
    Node* bestChild = nullptr; int maxVisits = -1;
    for (Node* child : root->children) {
        if (child->visits > maxVisits) 
        { 
            maxVisits = child->visits; 
            bestChild = child; 
        }
    }

    //4. Generate visualized data
    string visData = "";
    // If the number of simulations is small, generate a complete tree diagram JSON.
    if (simulation_limit < 7500) 
    {
        visData = "TREE:" + serialize_tree(root, 0);
    } 
    else // If there are too many simulations, the JSON will be too large and crash the frontend, so only the top five bar chart data will be generated.
    {
        vector<Node*> sortedChildren = root->children;
        sort(sortedChildren.begin(), sortedChildren.end(), [](Node* a, Node* b) { return a->visits > b->visits; });
        for (size_t i = 0; i < sortedChildren.size() && i < 5; ++i) { 
            Node* c = sortedChildren[i];
            if (c->visits > 0) {
                double rate = (double)c->wins / c->visits;
                visData += to_string(c->action.y) + "," + to_string(c->action.x) + ":" + to_string(c->visits) + ":" + to_string(rate) + ";";
            }
        }
        visData = "BAR:" + visData; 
    }

    //5. Output Results
    if (bestChild == nullptr) 
    { 
        cout << "-1,-1" << endl; 
    } 
    else 
    { 
        cout << bestChild->action.y << "," << bestChild->action.x << "|" << visData << endl; 
    }
    delete root; 
}

// MCTS Step 1: Selection
Node* Opener::select(Node* node) {
    Node* bestNode = nullptr; 
    double bestUCB = -999999.0;
    for (Node* child : node->children) {
        double ucb = ((double)child->wins / child->visits) + exploration_constant * sqrt(log(node->visits) / child->visits);// Use the UCB1 formula to select the most promising child nodes
        if (ucb > bestUCB) 
        { 
            bestUCB = ucb; 
            bestNode = child; 
        }
    }
    return bestNode;
}

// MCTS Step 2: Expansion
// Randomly select one step from the feasible steps of the current node and create a new child node.
Node* Opener::expand(Node* node) {
    if (node->available_moves.empty()) 
        return node; 
    uniform_int_distribution<int> dist(0, node->available_moves.size() - 1);
    int idx = dist(rng);
    Move move = node->available_moves[idx];
    node->available_moves.erase(node->available_moves.begin() + idx);
    State newState = node->state; newState.apply_move(move.x, move.y);
    Node* childNode = new Node(newState, node, move);
    node->children.push_back(childNode);
    return childNode;
}

// MCTS Step 3: Simulation
// Quickly play random chess moves until the game ends or the depth limit is reached.
int Opener::simulate(Node* node) {
    State simState = node->state; int depth = 0;
    while (!simState.is_game_over() && depth < 60) {
        vector<Move> moves = simState.get_avilable_list();
        if (moves.empty()) 
            simState.apply_move(-1, -1);
        else 
        {
            uniform_int_distribution<int> dist(0, moves.size() - 1);
            Move randomMove = moves[dist(rng)];
            simState.apply_move(randomMove.x, randomMove.y);
        }
        depth++;
    }
    int b=0, w=0; 
    for(char c : simState.board) 
    { 
        if(c=='X') 
            b++; 
         else if(c=='O') 
            w++; 
    }
    if (b > w) 
        return 1; 
    if (w > b) 
        return -1; 
    return 0; 
}

// MCTS Step 4: Backpropagation
// Update the simulation results to all nodes on the path
void Opener::backpropagate(Node* node, int result) {
    while (node != nullptr) {
        node->visits++;
        if (node->action.type == 'X' && result == 1) 
            node->wins++;
        else if (node->action.type == 'O' && result == -1) 
            node->wins++; 
        node = node->parent;
    }
}

int main(int argc, char* argv[]) 
{
    // Parameter check
    if (argc < 3) return 1;
    
    // Parse command line arguments
    string boardStr = argv[1];
    int turn = 1; try { turn = stoi(argv[2]); } catch(...) {}
    int sims = 500; if(argc > 3) { try { sims = stoi(argv[3]); } catch(...) {} }
    double c_val = 1.414; if(argc > 4) { try { c_val = stod(argv[4]); } catch(...) {} }

    // Start AI
    Opener ai; ai.calculate_one_move(boardStr, turn, sims, c_val);
    return 0;
}
