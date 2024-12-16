package GUI.Player;

import GUI.GeneralText;

import java.net.URL;

import static main.principal.getProperty;

/**
 * The {@code PlayerText} class extends {@code GeneralText} and manages player-specific dialog configurations.
 * It utilizes a singleton pattern to ensure only one instance of the class exists at any time.
 * It initializes the player dialog configuration using a resource URL from property files.
 * @author SIN
 */
public class PlayerText extends GeneralText {
    private static PlayerText instance;

    /**
     * Initializes a new instance of the {@code PlayerText} class.
     * The constructor sets up the player-specific dialog configuration by locating
     * the resource file for player dialog identified by the {@code playerDialog} property.
     * It retrieves the file resource using {@code getClass().getResource()} and passes
     * the resource URL to {@code super.initialize()} for further processing and setup.
     * <p>
     * This method is part of the singleton design pattern implementation within the
     * {@code PlayerText} class to ensure a single instance manages the dialog behavior.
     *
     * @throws IllegalArgumentException if the resource URL for the player dialog configuration
     * is null during initialization in {@code super.initialize()}.
     */
    public PlayerText() {
        URL resource = getClass().getResource(getProperty("playerDialog"));
        super.initialize(resource);
    }

    public static PlayerText getDialog() {
        if (instance == null) {
            instance = new PlayerText();
        }
        return instance;
    }

}
