package com.intellij.plugins.bodhi.pmd.annotator;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.DataManager;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.actions.RenameElementAction;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

final class SimpleAnnotator implements Annotator {

  // Define strings for the Simple language prefix - used for annotations, line markers, etc.
  public static final String SIMPLE_PREFIX_STR = "simple";
  public static final String SIMPLE_SEPARATOR_STR = ":";

  @Override
  public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
    // Ensure the PSI Element is an expression
//    if (!(element instanceof PsiLiteralExpression literalExpression)) {
//      return;
//    }

    if (!(element instanceof PsiClass psiClass)) {
      return;
    }
    String name = psiClass.getName();

      TextRange textRange = PsiTreeUtil.getChildOfType(element, PsiIdentifier.class).getTextRange();

      holder.newAnnotation(HighlightSeverity.ERROR, "name error")
            .range(textRange)
            .highlightType(ProblemHighlightType.ERROR)
            // ** Tutorial step 19. - Add a quick fix for the string containing possible properties
          .withFix(new BaseIntentionAction(){
            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
              return true;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
                AnAction refactor = ActionManager.getInstance().getAction("RenameElement");
                ;
                DataContext resultSync = DataManager.getInstance().getDataContextFromFocus().getResultSync();
                AnActionEvent  anActionEvent = new AnActionEvent(null, resultSync, "", new Presentation(), ActionManager.getInstance(), 0);
                new RenameElementAction().actionPerformed(anActionEvent);

//                WriteCommandAction.runWriteCommandAction(project, () -> {
//                    Document document = editor.getDocument();
//                    document.replaceString(textRange.getStartOffset(), textRange.getEndOffset(), "A" + name);
//
//
//                });

            }

            @Override
            public @NotNull @IntentionFamilyName String getFamilyName() {
              return "重命名";
            }

              @Override
              public @NotNull @IntentionName String getText() {
                  return "重命名-----";
              }
          })
            .create();


    // Ensure the PSI element contains a string that starts with the prefix and separator
//    String value = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
//    if (value == null || !value.startsWith(SIMPLE_PREFIX_STR + SIMPLE_SEPARATOR_STR)) {
//      return;
//    }

    // Define the text ranges (start is inclusive, end is exclusive)
    // "simple:key"
    //  01234567890
//    TextRange prefixRange = TextRange.from(element.getTextRange().getStartOffset(), SIMPLE_PREFIX_STR.length() + 1);
//    TextRange separatorRange = TextRange.from(prefixRange.getEndOffset(), SIMPLE_SEPARATOR_STR.length());
//    TextRange keyRange = new TextRange(separatorRange.getEndOffset(), element.getTextRange().getEndOffset() - 1);
//
//    // highlight "simple" prefix and ":" separator
//    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
//        .range(prefixRange).textAttributes(DefaultLanguageHighlighterColors.KEYWORD).create();
////    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
////        .range(separatorRange).textAttributes(SimpleSyntaxHighlighter.SEPARATOR).create();
//


    // Get the list of properties for given key
//    String key = value.substring(SIMPLE_PREFIX_STR.length() + SIMPLE_SEPARATOR_STR.length());
//    List<SimpleProperty> properties = SimpleUtil.findProperties(element.getProject(), key);
//    if (properties.isEmpty()) {
//      holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved property")
//          .range(keyRange)
//          .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
//          // ** Tutorial step 19. - Add a quick fix for the string containing possible properties
//          .withFix(new SimpleCreatePropertyQuickFix(key))
//          .create();
//    } else {
//      // Found at least one property, force the text attributes to Simple syntax value character
//      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
//          .range(keyRange).textAttributes(SimpleSyntaxHighlighter.VALUE).create();
//    }
  }


}