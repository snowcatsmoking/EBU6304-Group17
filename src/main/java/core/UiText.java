package core;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;

public final class UiText {

    private UiText() {}

    public static String tr(String englishText) {
        return LanguageManager.getInstance().text(englishText);
    }

    public static void localize(Node node) {
        if (node == null) {
            return;
        }

        if (node instanceof Labeled labeled) {
            if (!labeled.textProperty().isBound()) {
                labeled.setText(tr(labeled.getText()));
            }
        }

        if (node instanceof TextInputControl textInput) {
            if (!textInput.promptTextProperty().isBound()) {
                textInput.setPromptText(tr(textInput.getPromptText()));
            }
        }

        if (node instanceof DatePicker datePicker) {
            if (!datePicker.promptTextProperty().isBound()) {
                datePicker.setPromptText(tr(datePicker.getPromptText()));
            }
            if (!datePicker.getEditor().promptTextProperty().isBound()) {
                datePicker.getEditor().setPromptText(tr(datePicker.getEditor().getPromptText()));
            }
        }

        if (node instanceof ComboBox<?> comboBox) {
            comboBox.setPromptText(tr(comboBox.getPromptText()));
            localizeComboBoxItems(comboBox);
        }

        if (node instanceof Control control) {
            Tooltip tooltip = control.getTooltip();
            if (tooltip != null) {
                tooltip.setText(tr(tooltip.getText()));
            }
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                localize(child);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void localizeComboBoxItems(ComboBox<?> comboBox) {
        ObservableList items = comboBox.getItems();
        if (items == null || items.isEmpty()) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            if (item instanceof String text) {
                items.set(i, tr(text));
            }
        }
    }
}
