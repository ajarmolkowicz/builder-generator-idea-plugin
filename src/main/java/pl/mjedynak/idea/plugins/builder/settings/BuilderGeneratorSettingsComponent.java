package pl.mjedynak.idea.plugins.builder.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;

public class BuilderGeneratorSettingsComponent {

    private final JPanel myMainPanel;
    private final JBTextField defaultMethodPrefixText = new JBTextField();
    private final JBCheckBox innerBuilderCheckBox = new JBCheckBox("Inner builder");
    private final JBCheckBox addCopyConstructorCheckBox = new JBCheckBox("Add copy constructor");

    public BuilderGeneratorSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Default prefix: "), defaultMethodPrefixText, 1, false)
                .addComponent(innerBuilderCheckBox, 1)
                .addComponent(addCopyConstructorCheckBox, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return defaultMethodPrefixText;
    }

    @NotNull
    public String getDefaultMethodPrefixText() {
        return defaultMethodPrefixText.getText();
    }

    public void setDefaultMethodPrefixText(@NotNull String newText) {
        defaultMethodPrefixText.setText(newText);
    }

    public boolean isInnerBuilder() {
        return innerBuilderCheckBox.isSelected();
    }

    public void setInnerBuilder(boolean isInnerBuilder) {
        innerBuilderCheckBox.setSelected(isInnerBuilder);
    }

    public boolean isAddCopyConstructor() {
        return addCopyConstructorCheckBox.isSelected();
    }

    public void setAddCopyConstructor(boolean addCopyConstructor) {
        addCopyConstructorCheckBox.setSelected(addCopyConstructor);
    }
}
