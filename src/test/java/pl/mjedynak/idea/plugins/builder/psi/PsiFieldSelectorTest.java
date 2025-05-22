package pl.mjedynak.idea.plugins.builder.psi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.mock;

import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.mjedynak.idea.plugins.builder.factory.PsiElementClassMemberFactory;
import pl.mjedynak.idea.plugins.builder.verifier.PsiFieldVerifier;

@ExtendWith(MockitoExtension.class)
public class PsiFieldSelectorTest {

    @InjectMocks
    private PsiFieldSelector psiFieldSelector;

    @Mock(strictness = LENIENT)
    private PsiElementClassMemberFactory psiElementClassMemberFactory;

    @Mock(strictness = LENIENT)
    private PsiFieldVerifier psiFieldVerifier;

    @Mock
    private PsiClass psiClass;

    @Mock
    private PsiField psiField;

    @BeforeEach
    public void setUp() {
        PsiField[] fieldsArray = new PsiField[1];
        fieldsArray[0] = psiField;
        given(psiClass.getAllFields()).willReturn(fieldsArray);
        given(psiField.hasModifierProperty(PsiModifier.STATIC)).willReturn(false);
        given(psiElementClassMemberFactory.createPsiElementClassMember(any(PsiField.class)))
                .willReturn(mock(PsiElementClassMember.class));
    }

    @Test
    void shouldSelectFieldIfVerifierAcceptsItAsSetInSetter() {
        doTest(false, true, false, 1);
    }

    @Test
    void shouldSelectFieldIfVerifierAcceptsItAsSetInConstructor() {
        doTest(true, false, false, 1);
    }

    @Test
    void shouldSelectFieldEvenIfVerifierDoesNotAcceptsItAsSetInConstructorOrInSetter() {
        doTest(false, false, true, 1);
    }

    @Test
    void shouldSelectAllFieldsIfInnerBuilder() {
        doTest(false, false, false, 1);
    }

    @Test
    void shouldNeverSelectSerialVersionUIDField() {
        given(psiField.getName()).willReturn("serialVersionUID");
        doTest(true, true, true, 0);
    }

    @Test
    void shouldNeverSelectStaticField() {
        given(psiField.hasModifierProperty(PsiModifier.STATIC)).willReturn(true);
        doTest(true, true, true, 0);
    }

    private void doTest(boolean isSetInConstructor, boolean isSetInSetter, boolean hasGetter, int size) {
        // given
        given(psiFieldVerifier.isSetInConstructor(psiField, psiClass)).willReturn(isSetInConstructor);
        given(psiFieldVerifier.isSetInSetterMethod(psiField, psiClass)).willReturn(isSetInSetter);
        given(psiFieldVerifier.hasGetterMethod(psiField, psiClass)).willReturn(hasGetter);

        // when
        List<PsiElementClassMember<?>> result = psiFieldSelector.selectFieldsToIncludeInBuilder(psiClass);

        // then
        assertThat(result).hasSize(size);
    }
}
