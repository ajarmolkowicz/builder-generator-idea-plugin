package pl.mjedynak.idea.plugins.builder.psi;

import static com.intellij.ide.util.EditSourceUtil.getDescriptor;

import com.intellij.ide.util.PackageUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.refactoring.util.RefactoringMessageUtil;

public class PsiHelper {

    public PsiFile getPsiFileFromEditor(Editor editor, Project project) {
        return getPsiFile(editor, project);
    }

    public PsiClass getPsiClassFromEditor(Editor editor, Project project) {
        PsiFile psiFile = getPsiFile(editor, project);
        if (psiFile == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        if (element == null && offset > 0) {
            // If the element at caret is null (e.g. whitespace at the end of the file, or between tokens),
            // try to get the element before the caret. This helps in cases where caret is at the end of a line.
            element = psiFile.findElementAt(offset - 1);
        }
        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }

    private PsiFile getPsiFile(Editor editor, Project project) {
        return PsiUtilBase.getPsiFileInEditor(editor, project);
    }

    public PsiShortNamesCache getPsiShortNamesCache(Project project) {
        return PsiShortNamesCache.getInstance(project);
    }

    public PsiDirectory getDirectoryFromModuleAndPackageName(Module module, String packageName) {
        PsiDirectory baseDir = PackageUtil.findPossiblePackageDirectoryInModule(module, packageName);
        return PackageUtil.findOrCreateDirectoryForPackage(module, packageName, baseDir, true);
    }

    public void navigateToClass(PsiClass psiClass) {
        if (psiClass != null) {
            Navigatable navigatable = getDescriptor(psiClass);
            if (navigatable != null) {
                navigatable.navigate(true);
            }
        }
    }

    public String checkIfClassCanBeCreated(PsiDirectory targetDirectory, String className) {
        return RefactoringMessageUtil.checkCanCreateClass(targetDirectory, className);
    }

    public JavaDirectoryService getJavaDirectoryService() {
        return JavaDirectoryService.getInstance();
    }

    public PsiPackage getPackage(PsiDirectory psiDirectory) {
        return getJavaDirectoryService().getPackage(psiDirectory);
    }

    public JavaPsiFacade getJavaPsiFacade(Project project) {
        return JavaPsiFacade.getInstance(project);
    }

    public CommandProcessor getCommandProcessor() {
        return CommandProcessor.getInstance();
    }

    public Application getApplication() {
        return ApplicationManager.getApplication();
    }

    public Module findModuleForPsiClass(PsiClass psiClass, Project project) {
        return ModuleUtil.findModuleForFile(psiClass.getContainingFile().getVirtualFile(), project);
    }
}
