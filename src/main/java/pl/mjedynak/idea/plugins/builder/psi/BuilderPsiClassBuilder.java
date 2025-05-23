package pl.mjedynak.idea.plugins.builder.psi;

import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType; // Added
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
// Added
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import pl.mjedynak.idea.plugins.builder.settings.CodeStyleSettings;
import pl.mjedynak.idea.plugins.builder.verifier.PsiFieldVerifier;
import pl.mjedynak.idea.plugins.builder.writer.BuilderContext;

public class BuilderPsiClassBuilder {

    private static final String SPACE = " ";
    private static final String A_PREFIX = " a";
    private static final String AN_PREFIX = " an";
    private static final String SEMICOLON = ",";

    private final PsiHelper psiHelper = new PsiHelper();
    private final PsiFieldsModifier psiFieldsModifier = new PsiFieldsModifier();
    private final PsiFieldVerifier psiFieldVerifier = new PsiFieldVerifier();
    private final CodeStyleSettings codeStyleSettings = new CodeStyleSettings();
    private ButMethodCreator butMethodCreator;
    private CopyConstructorCreator copyConstructorCreator;
    private MethodCreator methodCreator;

    private PsiClass srcClass = null;
    private String builderClassName = null;

    private List<PsiField> psiFieldsForSetters = null;
    private List<PsiField> psiFieldsForConstructor = null;
    private List<PsiField> allSelectedPsiFields = null;
    private PsiMethod bestConstructor = null;

    private PsiClass builderClass = null;
    private PsiElementFactory elementFactory = null;
    private String srcClassName = null;
    private String srcClassFieldName = null;

    private boolean useSingleField = false;
    private boolean isInline = false;
    private boolean copyConstructor = false;

    public BuilderPsiClassBuilder aBuilder(BuilderContext context) {
        initializeFields(context);
        JavaDirectoryService javaDirectoryService = psiHelper.getJavaDirectoryService();
        builderClass = javaDirectoryService.createClass(context.getTargetDirectory(), builderClassName);
        PsiModifierList modifierList = builderClass.getModifierList();
        modifierList.setModifierProperty(PsiModifier.FINAL, true);
        return this;
    }

    public BuilderPsiClassBuilder anInnerBuilder(BuilderContext context) {
        initializeFields(context);
        builderClass = elementFactory.createClass(builderClassName);
        PsiModifierList modifierList = builderClass.getModifierList();
        modifierList.setModifierProperty(PsiModifier.FINAL, true);
        modifierList.setModifierProperty(PsiModifier.STATIC, true);
        return this;
    }

    private void initializeFields(BuilderContext context) {
        JavaPsiFacade javaPsiFacade = psiHelper.getJavaPsiFacade(context.getProject());
        elementFactory = javaPsiFacade.getElementFactory();
        srcClass = context.getPsiClassFromEditor();
        builderClassName = context.getClassName();
        srcClassName = context.getPsiClassFromEditor().getName();
        srcClassFieldName = StringUtils.uncapitalize(srcClassName);
        psiFieldsForSetters = context.getPsiFieldsForBuilder().getFieldsForSetters();
        psiFieldsForConstructor = context.getPsiFieldsForBuilder().getFieldsForConstructor();
        allSelectedPsiFields = context.getPsiFieldsForBuilder().getAllSelectedFields();
        useSingleField = context.useSingleField();
        bestConstructor = context.getPsiFieldsForBuilder().getBestConstructor();
        methodCreator = new MethodCreator(elementFactory, builderClassName);
        butMethodCreator = new ButMethodCreator(elementFactory);
        copyConstructorCreator = new CopyConstructorCreator(elementFactory);
        isInline = allSelectedPsiFields.size() == psiFieldsForConstructor.size();
        copyConstructor = context.hasAddCopyConstructor();
    }

    public BuilderPsiClassBuilder withFields() {
        if (useSingleField) {
            String fieldText = "private " + srcClassName + " " + srcClassFieldName + ";";
            PsiField singleField = elementFactory.createFieldFromText(fieldText, srcClass);
            builderClass.add(singleField);
        } else if (isInnerBuilder(builderClass)) {
            psiFieldsModifier.modifyFieldsForInnerClass(allSelectedPsiFields, builderClass);
        } else {
            psiFieldsModifier.modifyFields(psiFieldsForSetters, psiFieldsForConstructor, builderClass);
        }
        return this;
    }

    public BuilderPsiClassBuilder withConstructor() {
        PsiMethod constructor;
        if (useSingleField) {
            constructor = elementFactory.createMethodFromText(
                    builderClassName + "(){ " + srcClassFieldName + " = new " + srcClassName + "(); }", srcClass);
        } else {
            constructor = elementFactory.createConstructor();
        }

        constructor
                .getModifierList()
                .setModifierProperty(copyConstructor ? PsiModifier.PUBLIC : PsiModifier.PRIVATE, true);

        builderClass.add(constructor);
        return this;
    }

    public BuilderPsiClassBuilder withInitializingMethod() {
        String staticMethodName = StringUtils.uncapitalize(srcClassName);
        PsiMethod staticMethod = elementFactory.createMethodFromText(
                "public static " + builderClassName + " " + staticMethodName + "() { return new " + builderClassName
                        + "(); }",
                srcClass);
        builderClass.add(staticMethod);
        return this;
    }

    public BuilderPsiClassBuilder withSetMethods(String methodPrefix) {
        List<PsiField> fieldsToProcess;
        if (useSingleField || isInnerBuilder(builderClass)) {
            fieldsToProcess = allSelectedPsiFields;
        } else {
            fieldsToProcess = new java.util.ArrayList<>(psiFieldsForSetters);
            fieldsToProcess.addAll(psiFieldsForConstructor);
        }

        for (PsiField psiField : fieldsToProcess) {
            List<PsiMethod> methods =
                    methodCreator.createSetterMethods(psiField, methodPrefix, srcClassFieldName, useSingleField);
            methods.forEach(builderClass::add);
        }
        return this;
    }

    private boolean isInnerBuilder(PsiClass aClass) {
        return aClass.hasModifierProperty(PsiModifier.STATIC);
    }

    public BuilderPsiClassBuilder withButMethod() {
        PsiMethod method =
                butMethodCreator.butMethod(builderClassName, builderClass, srcClass, srcClassFieldName, useSingleField);
        builderClass.add(method);
        return this;
    }

    public BuilderPsiClassBuilder withCopyConstructor() {
        final PsiMethod method = copyConstructorCreator.copyConstructor(
                builderClass, srcClass, isInnerBuilder(builderClass), useSingleField);
        builderClass.add(method);
        return this;
    }

    public PsiClass build() {
        if (useSingleField) {
            return buildUseSingleField();
        } else if (isInline) {
            return buildIsInline();
        } else {
            return buildDefault();
        }
    }

    private PsiClass buildUseSingleField() {
        String buildMethodText = "public " + srcClassName + " build() { " + "return " + srcClassFieldName + ";" + " }";
        PsiMethod buildMethod = elementFactory.createMethodFromText(buildMethodText, srcClass);
        builderClass.add(buildMethod);
        return builderClass;
    }

    private PsiClass buildIsInline() {
        StringBuilder buildMethodText = new StringBuilder();
        buildMethodText.append("public ").append(srcClassName).append(" build() { ");
        buildMethodText.append("return ");
        appendConstructor(buildMethodText);
        buildMethodText.append(" }");
        PsiMethod buildMethod = elementFactory.createMethodFromText(buildMethodText.toString(), srcClass);
        builderClass.add(buildMethod);
        return builderClass;
    }

    private PsiClass buildDefault() {
        StringBuilder buildMethodText = new StringBuilder();
        buildMethodText.append("public ").append(srcClassName).append(" build() { ");
        buildMethodText
                .append(srcClassName)
                .append(SPACE)
                .append(srcClassFieldName)
                .append(" = ");
        appendConstructor(buildMethodText);
        appendSetMethodsOrAssignments(buildMethodText);
        buildMethodText.append("return ").append(srcClassFieldName).append(";");
        buildMethodText.append(" }");
        PsiMethod buildMethod = elementFactory.createMethodFromText(buildMethodText.toString(), srcClass);
        builderClass.add(buildMethod);
        return builderClass;
    }

    private void appendConstructor(StringBuilder buildMethodText) {
        String constructorParameters = createConstructorParameters();
        buildMethodText
                .append("new ")
                .append(srcClassName)
                .append("(")
                .append(constructorParameters)
                .append(");");
    }

    private void appendSetMethodsOrAssignments(StringBuilder buildMethodText) {
        appendSetMethods(buildMethodText, psiFieldsForSetters);
        if (isInnerBuilder(builderClass)) {
            Set<PsiField> fieldsSetViaAssignment = new HashSet<PsiField>(allSelectedPsiFields);
            fieldsSetViaAssignment.removeAll(psiFieldsForSetters);
            fieldsSetViaAssignment.removeAll(psiFieldsForConstructor);
            appendAssignments(buildMethodText, fieldsSetViaAssignment);
        }
    }

    private void appendSetMethods(StringBuilder buildMethodText, Collection<PsiField> fieldsToBeSetViaSetter) {
        for (PsiField psiFieldsForSetter : fieldsToBeSetViaSetter) {
            String fieldNamePrefix = codeStyleSettings.getFieldNamePrefix();
            String fieldName = psiFieldsForSetter.getName();
            String fieldNameWithoutPrefix = fieldName.replaceFirst(fieldNamePrefix, "");
            String fieldNameUppercase = StringUtils.capitalize(fieldNameWithoutPrefix);
            buildMethodText
                    .append(srcClassFieldName)
                    .append(".set")
                    .append(fieldNameUppercase)
                    .append("(")
                    .append(fieldName)
                    .append(");");
        }
    }

    private void appendAssignments(StringBuilder buildMethodText, Collection<PsiField> fieldsSetViaAssignment) {
        for (PsiField field : fieldsSetViaAssignment) {
            buildMethodText
                    .append(srcClassFieldName)
                    .append(".")
                    .append(field.getName())
                    .append("=")
                    .append("this.")
                    .append(field.getName())
                    .append(";");
        }
    }

    private String createConstructorParameters() {
        if (bestConstructor == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (PsiParameter psiParameter : bestConstructor.getParameterList().getParameters()) {
            boolean parameterHasMatchingField = false;
            for (PsiField psiField : psiFieldsForConstructor) {
                if (psiFieldVerifier.areNameAndTypeEqual(psiField, psiParameter)) {
                    sb.append(psiField.getName()).append(SEMICOLON);
                    parameterHasMatchingField = true;
                    break;
                }
            }
            if (!parameterHasMatchingField) {
                sb.append(getDefaultValue(psiParameter.getType())).append(SEMICOLON);
            }
        }
        removeLastSemicolon(sb);
        return sb.toString();
    }

    private String getDefaultValue(PsiType type) {
        if (type.equals(PsiType.BOOLEAN)) {
            return "false";
        } else if (type.equals(PsiType.BYTE) || type.equals(PsiType.SHORT) || type.equals(PsiType.INT)) {
            return "0";
        } else if (type.equals(PsiType.LONG)) {
            return "0L";
        } else if (type.equals(PsiType.FLOAT)) {
            return "0.0f";
        } else if (type.equals(PsiType.DOUBLE)) {
            return "0.0d";
        } else if (type.equals(PsiType.CHAR)) {
            return "'\\u0000'";
        } else if (type instanceof PsiClassType && type.getCanonicalText().startsWith("java.util.Optional")) {
            return "Optional.empty()";
        }
        return "null";
    }

    private void removeLastSemicolon(StringBuilder sb) {
        if (sb.toString().endsWith(SEMICOLON)) {
            sb.deleteCharAt(sb.length() - 1);
        }
    }
}
