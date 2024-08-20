package com.intellij.plugins.bodhi.pmd;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.NotificationsManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.vcs.VcsNotifier;
import com.intellij.util.concurrency.AppExecutorUtil;
import git4idea.GitUtil;
import git4idea.commands.Git;
import git4idea.commands.GitCommandResult;
import git4idea.fetch.GitFetchResult;
import git4idea.fetch.GitFetchSupport;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
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
                    indicator.setText("自动更新代码");
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
                            gitRepositories.forEach(repository -> {
                                GitRemote remoteToFetch = fetchSupport.getDefaultRemoteToFetch(repository);
                                GitFetchResult fetch = fetchSupport.fetch(repository, remoteToFetch);
                                fetch.showNotificationIfFailed();

                                if (component.isAutoMerge()) {
                                    String currentBranch = repository.getCurrentBranchName();
                                    String remoteName = remoteToFetch.getName();
//
//                                String currentRevision = repository.getCurrentRevision();
//                                GitBranchesCollection gitBranchesCollection = repository.getBranches();
//                                GitLocalBranch localBranch = gitBranchesCollection.findLocalBranch(currentBranch);
//
//                                GitRemoteBranch remoteBranch = gitBranchesCollection.findRemoteBranch(remoteName + "/" + currentBranch);

//                                GitChangeUtils.getDiff(repository,)
                                    GitCommandResult mergeResult = Git.getInstance().merge(repository, remoteName + "/" + currentBranch, Collections.emptyList());
                                    boolean conflict = mergeResult.getOutput().stream().anyMatch(msg -> msg.contains("CONFLICT"));
                                    if (!mergeResult.getErrorOutput().isEmpty() || conflict) {
                                        GitCommandResult merge = Git.getInstance().merge(repository, "--abort", Collections.emptyList());
                                        String collect = mergeResult.getOutput().stream().collect(Collectors.joining("\n"));
                                        Notification notification = VcsNotifier.STANDARD_NOTIFICATION
                                                .createNotification("有冲突，请手动合并 " + remoteName + "/" + currentBranch + "\n" + collect, NotificationType.ERROR);
                                        NotificationsManager.getNotificationsManager()
                                                .showNotification(notification, project);

                                    }
                                }
                            });

                        }
                    }
                    indicator.setFraction(1.0);
                }
            }.setCancelText("取消更新代码");
            AppExecutorUtil.getAppScheduledExecutorService()
                    .scheduleWithFixedDelay(() -> task.queue(), 0, component.getAutoPullInterval(), TimeUnit.HOURS);
        });

        return Unit.INSTANCE;
    }

    private static boolean promptUser(Project project, List<String> message) {
        String collect = message.stream().collect(Collectors.joining("\n"));
        MessageDialogBuilder.YesNo yesNo = MessageDialogBuilder.yesNo("是否合并", "有冲突" + "\n" + collect)
                .yesText("合并")
                .noText("不合并");
        return yesNo.ask(project);
    }

}
