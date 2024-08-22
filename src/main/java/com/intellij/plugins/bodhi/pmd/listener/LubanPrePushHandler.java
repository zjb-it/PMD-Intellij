package com.intellij.plugins.bodhi.pmd.listener;

import com.intellij.dvcs.push.PrePushHandler;
import com.intellij.dvcs.push.PushInfo;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LubanPrePushHandler implements PrePushHandler {
    @NotNull
    @Override
    public Result handle(@NotNull Project project, @NotNull List<PushInfo> pushDetails, @NotNull ProgressIndicator indicator) {
//        Set<Module> collect = pushDetails.stream()
//                .map(detail -> ModuleUtil.findModuleForFile(detail.getRepository().getRoot(), project))
//                .collect(Collectors.toSet());
//        Module[] array = collect.toArray(new Module[0]);
//        CompilerManager.getInstance(project).compile(array);

        Set<VirtualFile> virtualFiles = pushDetails.stream().flatMap(detail ->
                detail.getCommits()
                        .stream()
                        .flatMap(commit -> commit.getChanges()
                                .stream()
                                .filter(change -> change.getType() != Change.Type.DELETED)
                                .map(Change::getVirtualFile)
                                .filter(file -> file.isValid() && file.getFileType() == JavaFileType.INSTANCE))
        ).collect(Collectors.toSet());
        VirtualFile[] array = virtualFiles.toArray(new VirtualFile[0]);

        AtomicInteger atomicInteger = new AtomicInteger(-1);
        ApplicationManager.getApplication().invokeAndWait(()->{
            CompilerManager compilerManager = CompilerManager.getInstance(project);
            compilerManager.compile(array, (aborted, errors, warnings, compileContext) -> atomicInteger.set(errors));

        });
        while (atomicInteger.get() < 0) {
            if (atomicInteger.get() > 0) {
                return Result.ABORT_AND_CLOSE;
            }
        }
        return Result.OK;
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getPresentableName() {
        return "校验通过才能push, 编译校验中... ";
    }




}
