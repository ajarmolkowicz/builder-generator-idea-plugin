package pl.mjedynak.idea.plugins.builder.psi;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import java.util.List;

public class PsiFieldsModifier {

    public void modifyFields(
            List<PsiField> psiFieldsForSetters, List<PsiField> psiFieldsForConstructor, PsiClass builderClass) {
        for (PsiField psiFieldsForSetter : psiFieldsForSetters) {
            addFieldToClass(psiFieldsForSetter, builderClass);
        }
        for (PsiField psiFieldForConstructor : psiFieldsForConstructor) {
            addFieldToClass(psiFieldForConstructor, builderClass);
        }
    }

    public void modifyFieldsForInnerClass(List<PsiField> allFields, PsiClass innerBuilderClass) {
        for (PsiField field : allFields) {
            addFieldToClass(field, innerBuilderClass);
        }
    }

    private void addFieldToClass(PsiField psiField, PsiClass builderClass) {
        PsiElement copy = copyField(psiField, builderClass);
        builderClass.add(copy);
    }

    private PsiElement copyField(final PsiField psiField, final PsiClass builderClass) {
        String fieldName = psiField.getName();
        PsiType fieldType = psiField.getType();
        String fieldTypeText = fieldType.getPresentableText();
        String fieldText;

        if (fieldType instanceof PsiClassType && fieldType.getCanonicalText().startsWith("java.util.Optional")) {
            fieldText = "private " + fieldTypeText + " " + fieldName + " = java.util.Optional.empty();";
        } else {
            fieldText = "private " + fieldTypeText + " " + fieldName + ";";
        }

        return PsiElementFactory.getInstance(builderClass.getProject()).createFieldFromText(fieldText, builderClass);
    }
}
