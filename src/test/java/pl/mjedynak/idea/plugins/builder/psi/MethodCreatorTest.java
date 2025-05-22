package pl.mjedynak.idea.plugins.builder.psi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.lenient;
import static org.mockito.Mockito.mock;

import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MethodCreatorTest {

    private MethodCreator methodCreator;

    @Mock
    private PsiElementFactory elementFactory;

    @Mock
    private PsiField psiField;

    @Mock
    private PsiType stringType;

    @Mock
    private PsiClassType optionalStringType;

    @Mock
    private PsiMethod mockMethod;

    private final String srcClassFieldName = "className";
    private final String builderClassName = "BuilderClassName";
    private final String methodPrefix = "with";
    private final String fieldName = "name";
    private final String methodName = "withName";

    @BeforeEach
    public void setUp() {
        methodCreator = new MethodCreator(elementFactory, builderClassName);
        lenient().when(psiField.getName()).thenReturn(fieldName);
    }

    private void mockFieldType(PsiType type, String presentableText, String canonicalText) {
        lenient().when(psiField.getType()).thenReturn(type);
        lenient().when(type.getPresentableText()).thenReturn(presentableText);
        lenient().when(type.getCanonicalText()).thenReturn(canonicalText);
    }

    @Test
    void shouldCreateSingleMethodForNonOptionalField() {
        // given
        mockFieldType(stringType, "String", "java.lang.String");
        String expectedMethodText = "public " + builderClassName + " " + methodName + "(String " + fieldName
                + ") { this." + fieldName + " = " + fieldName + "; return this; }";
        given(elementFactory.createMethodFromText(expectedMethodText, psiField)).willReturn(mockMethod);

        // when
        List<PsiMethod> result = methodCreator.createSetterMethods(psiField, methodPrefix, srcClassFieldName);

        // then
        assertThat(result).containsExactly(mockMethod);
    }

    @Test
    void shouldCreateTwoMethodsForOptionalField() {
        // given
        mockFieldType(optionalStringType, "Optional<String>", "java.util.Optional<java.lang.String>");
        lenient().when(optionalStringType.getParameters()).thenReturn(new PsiType[] {stringType});
        lenient().when(stringType.getPresentableText()).thenReturn("String"); // For T in Optional<T>

        PsiMethod rawTypeMethod = mock(PsiMethod.class);
        PsiMethod optionalTypeMethod = mock(PsiMethod.class);

        String expectedRawMethodText = "public " + builderClassName + " " + methodName + "(String " + fieldName
                + ") { this." + fieldName + " = Optional.of(" + fieldName + "); return this; }";
        String expectedOptionalMethodText = "public " + builderClassName + " " + methodName + "(Optional<String> "
                + fieldName + ") { this." + fieldName + " = " + fieldName + "; return this; }";

        given(elementFactory.createMethodFromText(expectedRawMethodText, psiField))
                .willReturn(rawTypeMethod);
        given(elementFactory.createMethodFromText(expectedOptionalMethodText, psiField))
                .willReturn(optionalTypeMethod);

        // when
        List<PsiMethod> result = methodCreator.createSetterMethods(psiField, methodPrefix, srcClassFieldName);

        // then
        assertThat(result).containsExactlyInAnyOrder(rawTypeMethod, optionalTypeMethod);
    }

    @Test
    void shouldCreateSingleMethodForRawOptionalField() {
        // given
        mockFieldType(optionalStringType, "Optional", "java.util.Optional");
        lenient().when(optionalStringType.getParameters()).thenReturn(new PsiType[] {}); // No generic parameter

        String expectedMethodText = "public " + builderClassName + " " + methodName + "(Optional " + fieldName
                + ") { this." + fieldName + " = " + fieldName + "; return this; }";
        given(elementFactory.createMethodFromText(expectedMethodText, psiField)).willReturn(mockMethod);

        // when
        List<PsiMethod> result = methodCreator.createSetterMethods(psiField, methodPrefix, srcClassFieldName);

        // then
        assertThat(result).containsExactly(mockMethod);
    }
}
