package GUI.Player;

abstract class SearchListener implements javax.swing.event.DocumentListener {
    @Override
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        update();
    }
    @Override
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        update();
    }
    @Override
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        update();
    }
    public abstract void update();
}