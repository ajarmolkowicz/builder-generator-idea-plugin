package pl.mjedynak.idea.plugins.builder.psi;

import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import java.util.ArrayList;
import java.util.List;
import pl.mjedynak.idea.plugins.builder.settings.CodeStyleSettings;

public class MethodCreator {

    private final CodeStyleSettings codeStyleSettings = new CodeStyleSettings();
    private final MethodNameCreator methodNameCreator = new MethodNameCreator();
    private final PsiElementFactory elementFactory;
    private final String builderClassName;

    public MethodCreator(PsiElementFactory elementFactory, String builderClassName) {
        this.elementFactory = elementFactory;
        this.builderClassName = builderClassName;
    }

    public List<PsiMethod> createSetterMethods(PsiField psiField, String methodPrefix, String srcClassFieldName) {
        List<PsiMethod> methods = new ArrayList<>();
        PsiType fieldType = psiField.getType();

        if (fieldType instanceof PsiClassType && fieldType.getCanonicalText().startsWith("java.util.Optional")) {
            PsiClassType classType = (PsiClassType) fieldType;
            PsiType[] parameters = classType.getParameters();
            if (parameters.length == 1) {
                PsiType rawType = parameters[0];
                methods.add(createSetterMethod(
                        psiField,
                        methodPrefix,
                        srcClassFieldName,
                        rawType,
                        "Optional.of(" + getParameterName(psiField) + ")"));
                methods.add(createSetterMethod(
                        psiField, methodPrefix, srcClassFieldName, fieldType, getParameterName(psiField)));
            } else {
                methods.add(createSetterMethod(
                        psiField, methodPrefix, srcClassFieldName, fieldType, getParameterName(psiField)));
            }
        } else {
            methods.add(createSetterMethod(
                    psiField, methodPrefix, srcClassFieldName, fieldType, getParameterName(psiField)));
        }
        return methods;
    }

    private PsiMethod createSetterMethod(
            PsiField psiField,
            String methodPrefix,
            String srcClassFieldName,
            PsiType parameterType,
            String assignmentValue) {
        String fieldName = psiField.getName();
        String parameterTypeText = parameterType.getPresentableText();
        String fieldNamePrefix = codeStyleSettings.getFieldNamePrefix();
        String fieldNameWithoutPrefix = fieldName.replaceFirst(fieldNamePrefix, "");
        String parameterName = getParameterName(psiField);
        String methodName = methodNameCreator.createMethodName(methodPrefix, fieldNameWithoutPrefix);

        String methodBodyAssignment = "this." + fieldName + " = " + assignmentValue + ";";

        String methodText = "public " + builderClassName + " " + methodName + "(" + parameterTypeText + " "
                + parameterName + ") { " + methodBodyAssignment + " return this; }";
        return elementFactory.createMethodFromText(methodText, psiField);
    }

    private String getParameterName(PsiField psiField) {
        String fieldNamePrefix = codeStyleSettings.getFieldNamePrefix();
        String fieldNameWithoutPrefix = psiField.getName().replaceFirst(fieldNamePrefix, "");
        String parameterNamePrefix = codeStyleSettings.getParameterNamePrefix();
        return parameterNamePrefix + fieldNameWithoutPrefix;
    }
}
