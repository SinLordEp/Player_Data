package utils;

import model.Player;

import java.util.HashMap;

public interface FileReader {
    HashMap<Integer, Player> parse_player() throws Exception;
}
