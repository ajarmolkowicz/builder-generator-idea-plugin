package pl.mjedynak.idea.plugins.builder.psi;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times; // Added
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PsiFieldsModifierTest {

    private final PsiFieldsModifier psiFieldsModifier = new PsiFieldsModifier();

    @Mock
    private PsiClass builderClass;

    @Mock
    private Project project;

    @Mock
    private PsiElementFactory psiElementFactory;

    private List<PsiField> psiFieldsForSetters;
    private List<PsiField> psiFieldsForConstructor;

    @BeforeEach
    public void setUp() {
        psiFieldsForConstructor = new ArrayList<>();
        psiFieldsForSetters = new ArrayList<>();
    }

    @Test
    void shouldAddPrivateFieldsToBuilderClass() {
        // given
        PsiField psiFieldForSetters = mock(PsiField.class);
        given(psiFieldForSetters.getName()).willReturn("setterField");
        given(psiFieldForSetters.getType()).willReturn(PsiType.INT);
        psiFieldsForSetters.add(psiFieldForSetters);
        PsiField copyPsiFieldForSetter = mock(PsiField.class);
        given(psiElementFactory.createFieldFromText("private int setterField;", builderClass))
                .willReturn(copyPsiFieldForSetter);

        PsiField psiFieldForConstructor = mock(PsiField.class);
        given(psiFieldForConstructor.getName()).willReturn("constructorField");
        given(psiFieldForConstructor.getType()).willReturn(PsiType.BOOLEAN);
        psiFieldsForConstructor.add(psiFieldForConstructor);
        PsiField copyPsiFieldForConstructor = mock(PsiField.class);
        given(psiElementFactory.createFieldFromText("private boolean constructorField;", builderClass))
                .willReturn(copyPsiFieldForConstructor);

        given(builderClass.getProject()).willReturn(project);
        given(project.getService(PsiElementFactory.class)).willReturn(psiElementFactory);

        // when
        psiFieldsModifier.modifyFields(psiFieldsForSetters, psiFieldsForConstructor, builderClass);

        // then
        verify(builderClass).add(copyPsiFieldForSetter);
        verify(builderClass).add(copyPsiFieldForConstructor);
        verify(builderClass, times(2)).getProject(); // Verify calls to getProject()
        verifyNoMoreInteractions(builderClass);
    }
}
