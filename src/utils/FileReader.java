package utils;

import model.Person;

import java.io.IOException;
import java.util.HashMap;

public interface FileReader<T> {
    T read() throws Exception;
    HashMap<Integer, Person> parse_player() throws IOException, ClassNotFoundException;
}
