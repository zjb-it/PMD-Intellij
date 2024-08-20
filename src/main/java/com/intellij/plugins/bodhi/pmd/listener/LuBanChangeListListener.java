package com.intellij.plugins.bodhi.pmd.listener;


import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.ex.ChangelistsLocalLineStatusTracker;
import com.intellij.openapi.vcs.ex.LineStatusTracker;
import com.intellij.openapi.vcs.ex.LocalRange;
import com.intellij.openapi.vcs.impl.LineStatusTrackerManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LuBanChangeListListener implements ChangeListListener {
    @Override
    public void changeListAdded(ChangeList list) {

        System.out.println("changeListAdded**********");
        ChangeListListener.super.changeListAdded(list);
    }

    @Override
    public void changeListDataChanged(@NotNull ChangeList list) {
        System.out.println("changeListDataChanged**********");
        ChangeListListener.super.changeListDataChanged(list);
    }

    @Override
    public void changeListChanged(ChangeList list) {

        if (list instanceof LocalChangeListImpl localChangeList) {
        }

//        Change change = new ArrayList<>(list.getChanges()).get(1);

//        try {
//            String content = change.getBeforeRevision().getContent();
//            VirtualFile virtualFile1 = change.getBeforeRevision().getFile().getVirtualFile();
//            VirtualFile virtualFile2 = change.getAfterRevision().getFile().getVirtualFile();
//
//            LineStatusTracker<?> lineStatusTracker = LineStatusTrackerManager.getInstance(currentProject).getLineStatusTracker(virtualFile1);
//
//
//            ReadAction.run(()->{
//                Document document = FileDocumentManager.getInstance().getDocument(virtualFile1);
//                if (lineStatusTracker instanceof ChangelistsLocalLineStatusTracker changelistsLocalLineStatusTracker) {
//                    List<LocalRange> ranges = changelistsLocalLineStatusTracker.getRanges();
//                    for (LocalRange range : ranges) {
//                        int startOffset = document.getLineStartOffset(range.getLine1());
//                        int endOffset = document.getLineEndOffset(range.getLine2());
//
//                        PsiFile psiFile = PsiManager.getInstance(currentProject).findFile(virtualFile1);
//
////                                        PsiTreeUtil.findElementOfClassAtRange(psiFile,)
////                                        PsiTreeUtil.getElementsOfRange()
////                                        DocumentUtil.
//
//                        System.out.println("---------start------");
//                        System.out.println(document.getText(new TextRange(startOffset,endOffset)));
//                        System.out.println("---------end------");
//                    }
//                }
//
//            });
//
//
//
////                            AbstractVcs vcsFor = ProjectLevelVcsManager.getInstance(currentProject).getVcsFor(virtualFile1);
////                            DiffProvider diffProvider = vcsFor.getDiffProvider();
////
////
////                            String afterContent = change.getAfterRevision().getContent();
////                            VirtualFile virtualFile = change.getVirtualFile();
//        } catch (VcsException e) {
//            throw new RuntimeException(e);
//        }


        ChangeListListener.super.changeListChanged(list);
    }

    @Override
    public void defaultListChanged(ChangeList oldDefaultList, ChangeList newDefaultList) {
        System.out.println("defaultListChanged**********");
        ChangeListListener.super.defaultListChanged(oldDefaultList, newDefaultList);
    }
}

