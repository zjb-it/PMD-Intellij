package com.intellij.plugins.bodhi.pmd;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.concurrency.AppExecutorUtil;
import git4idea.GitLocalBranch;
import git4idea.GitPushUtil;
import git4idea.GitRemoteBranch;
import git4idea.GitUtil;
import git4idea.actions.GitPull;
import git4idea.branch.GitBranchUtil;
import git4idea.branch.GitBranchesCollection;
import git4idea.changes.GitChangeUtils;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.crlf.GitCrlfUtil;
import git4idea.fetch.GitFetchResult;
import git4idea.fetch.GitFetchSupport;
import git4idea.merge.GitMerger;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import git4idea.stash.GitStashUtils;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class StartupListener implements ProjectActivity {
    private static final Log log = LogFactory.getLog(PMDInvoker.class);

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {

        DumbService.getInstance(project).smartInvokeLater(() -> {
            PMDProjectComponent component = project.getComponent(PMDProjectComponent.class);
            Task task = new Task.Backgroundable(project, "自动拉取代码", true) {
                public void run(ProgressIndicator indicator) {
                    indicator.setText("自动拉取代码");
                    indicator.setIndeterminate(false);
                    indicator.setFraction(0.0);
                    Collection<GitRepository> repositories = GitUtil.getRepositories(project);
                    GitFetchSupport fetchSupport = GitFetchSupport.fetchSupport(project);
                    if (CollectionUtils.isNotEmpty(repositories)) {
                        Set<GitRepository> gitRepositories = repositories.stream()
                                .filter(repository -> Objects.nonNull(fetchSupport.getDefaultRemoteToFetch(repository)))
                                .collect(Collectors.toSet());
                        if (CollectionUtils.isNotEmpty(gitRepositories)) {
//                            GitFetchResult gitFetchResult = fetchSupport.fetchAllRemotes(gitRepositories);
//                            gitFetchResult.showNotification();
                            gitRepositories.forEach(repository->{
                                GitRemote remoteToFetch = fetchSupport.getDefaultRemoteToFetch(repository);
                                fetchSupport.fetch(repository, remoteToFetch);
                                String currentBranch = repository.getCurrentBranchName();
                                String remoteName = remoteToFetch.getName();
//
//                                String currentRevision = repository.getCurrentRevision();
//                                GitBranchesCollection gitBranchesCollection = repository.getBranches();
//                                GitLocalBranch localBranch = gitBranchesCollection.findLocalBranch(currentBranch);
//
//                                GitRemoteBranch remoteBranch = gitBranchesCollection.findRemoteBranch(remoteName + "/" + currentBranch);
//                                GitChangeUtils.getDiff(repository,)
                                GitCommandResult mergeResult = Git.getInstance().merge(repository, remoteName+"/"+currentBranch, Collections.emptyList());
                                System.out.println();
                            });

                        }
                    }
                    indicator.setFraction(1.0);
                }
            }.setCancelText("取消拉取代码");

//            ProgressManager.getInstance().run(task);



//            AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> task.queue(), 0, 30, TimeUnit.SECONDS);
            AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> task.queue(),0, 60, TimeUnit.SECONDS);

        });

        return Unit.INSTANCE;
    }


}
