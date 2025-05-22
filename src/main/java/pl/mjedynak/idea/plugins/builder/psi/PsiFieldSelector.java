package pl.mjedynak.idea.plugins.builder.psi;

import static java.util.Arrays.stream;

import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import java.util.ArrayList;
import java.util.List;
import pl.mjedynak.idea.plugins.builder.factory.PsiElementClassMemberFactory;
import pl.mjedynak.idea.plugins.builder.verifier.PsiFieldVerifier;

public class PsiFieldSelector {

    private final PsiElementClassMemberFactory psiElementClassMemberFactory;
    private final PsiFieldVerifier psiFieldVerifier;

    public PsiFieldSelector(
            PsiElementClassMemberFactory psiElementClassMemberFactory, PsiFieldVerifier psiFieldVerifier) {
        this.psiElementClassMemberFactory = psiElementClassMemberFactory;
        this.psiFieldVerifier = psiFieldVerifier;
    }

    public List<PsiElementClassMember<?>> selectFieldsToIncludeInBuilder(PsiClass psiClass) {
        List<PsiElementClassMember<?>> result = new ArrayList<>();

        List<PsiField> psiFields = stream(psiClass.getAllFields())
                .filter(psiField -> !psiField.hasModifierProperty(PsiModifier.STATIC))
                .filter(psiField -> !"serialVersionUID".equals(psiField.getName()))
                .toList();

        for (PsiField psiField : psiFields) {
            result.add(psiElementClassMemberFactory.createPsiElementClassMember(psiField));
        }
        return result;
    }
}
