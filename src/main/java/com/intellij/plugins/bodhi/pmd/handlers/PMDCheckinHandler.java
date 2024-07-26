package com.intellij.plugins.bodhi.pmd.handlers;

import com.intellij.AbstractBundle;
import com.intellij.CommonBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.ex.ChangelistsLocalLineStatusTracker;
import com.intellij.openapi.vcs.ex.LineStatusTracker;
import com.intellij.openapi.vcs.ex.LocalRange;
import com.intellij.openapi.vcs.impl.LineStatusTrackerManager;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.plugins.bodhi.pmd.PMDInvoker;
import com.intellij.plugins.bodhi.pmd.PMDProjectComponent;
import com.intellij.plugins.bodhi.pmd.PMDResultPanel;
import com.intellij.plugins.bodhi.pmd.PMDUtil;
import com.intellij.plugins.bodhi.pmd.core.PMDResultCollector;
import com.intellij.plugins.bodhi.pmd.core.PMDViolation;
import com.intellij.plugins.bodhi.pmd.tree.*;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PairConsumer;
import com.intellij.util.ui.UIUtil;
import net.sourceforge.pmd.util.CollectionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PMDCheckinHandler extends CheckinHandler {

    private static final Log log = LogFactory.getLog(PMDCheckinHandler.class);
    public static final String BUNDLE = "messages.PMD-Intellij";

    @NonNls
    private final CheckinProjectPanel checkinProjectPanel;

    /* default */
    PMDCheckinHandler(CheckinProjectPanel checkinProjectPanel) {
        this.checkinProjectPanel = checkinProjectPanel;
    }

    @Nullable
    @Override
    public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
        final JCheckBox checkBox = new JCheckBox(message("handler.before.checkin.checkbox"));

        Project project = checkinProjectPanel.getProject();
        final PMDProjectComponent projectComponent = project.getComponent(PMDProjectComponent.class);

        return new RefreshableOnComponent() {
            @Override
            public JComponent getComponent() {
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(checkBox);
                return panel;
            }

            @Override
            public void saveState() {
                projectComponent.setScanFilesBeforeCheckin(checkBox.isSelected());
            }

            @Override
            public void restoreState() {
                checkBox.setSelected(projectComponent.isScanFilesBeforeCheckin());
            }
        };
    }

    @NotNull
    private String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return AbstractBundle.message(ResourceBundle.getBundle(BUNDLE), key, params);
    }

    @Override
    public ReturnResult beforeCheckin(@Nullable CommitExecutor executor,
                                      PairConsumer<Object, Object> additionalDataConsumer) {
        Project project = checkinProjectPanel.getProject();

        PMDProjectComponent plugin = project.getComponent(PMDProjectComponent.class);
        if (plugin == null) {
            log.error("Could not find the PMD plugin, skipping");
            return ReturnResult.COMMIT;
        }

        if (!plugin.isScanFilesBeforeCheckin()) {
            return ReturnResult.COMMIT;
        }
        List<PMDRuleSetNode> ruleSetResultNodes = new ArrayList<>();
        for (String ruleSetPath : plugin.getCustomRuleSetPaths()) {
            PMDRuleSetNode ruleSetResultNode = scanFiles(ruleSetPath, plugin);
            if (ruleSetResultNode != null) {
                ruleSetResultNodes.add(ruleSetResultNode);
            }
        }
        return processScanResults(ruleSetResultNodes, project);
    }

    private PMDRuleSetNode scanFiles(String ruleSetPath, PMDProjectComponent plugin) {
        PMDRuleSetNode ruleSetResultNode = null;
        PMDResultCollector collector = new PMDResultCollector();
        Collection<Change> selectedChanges = checkinProjectPanel.getSelectedChanges();
        List<File> files = new ArrayList<>(checkinProjectPanel.getFiles());
        // 扫描结果
        List<PMDRuleSetEntryNode> ruleSetResultNodes = collector.runPMDAndGetResults(files, ruleSetPath, plugin);

// 和selectedChanges匹配，匹配不上，即commit ，否则cancel
//        ((PMDViolationNode)ruleSetResultNodes.get(0).getFirstChild()).getPmdViolation().getEndLine()
        if (!ruleSetResultNodes.isEmpty()) {
            ruleSetResultNodes = checkCommitViolationNode(ruleSetResultNodes, selectedChanges, plugin.getCurrentProject());
            if (!ruleSetResultNodes.isEmpty()) {
                ruleSetResultNode = createRuleSetNodeWithResults(ruleSetPath, ruleSetResultNodes);
            }
        }
        return ruleSetResultNode;
    }

    private List<PMDRuleSetEntryNode> checkCommitViolationNode(List<PMDRuleSetEntryNode> ruleSetResultNodes, Collection<Change> selectedChanges, Project currentProject) {
        Map<String, Set<Integer>> changeLineMap = new HashMap<>();
        selectedChanges.forEach(change -> {
            VirtualFile virtualFile = change.getVirtualFile();
            if (Objects.nonNull(virtualFile)) {
                LineStatusTracker<?> lineStatusTracker = LineStatusTrackerManager.getInstance(currentProject).getLineStatusTracker(virtualFile);
                if (lineStatusTracker instanceof ChangelistsLocalLineStatusTracker changelistsLocalLineStatusTracker) {
                    List<LocalRange> ranges = changelistsLocalLineStatusTracker.getRanges();
                    if (CollectionUtils.isNotEmpty(ranges)) {
                        ranges.forEach(range->{
                            Set<Integer> collect = IntStream.range(range.getLine1(), range.getLine2() + 1)
                                    .boxed()
                                    .collect(Collectors.toSet());
                            PsiFile psiFile = PsiManager.getInstance(currentProject).findFile(virtualFile);
                            if (psiFile instanceof PsiJavaFile javaFile) {
                                String classPath = javaFile.getPackageName() + "." + javaFile.getName().replace(".java", "");
                                Set<Integer> lines = changeLineMap.getOrDefault(classPath, new HashSet<>());
                                lines.addAll(collect);
                                changeLineMap.put(classPath, lines);
                            }
                        });
                    }
                }
            }
        });
        List<PMDRuleSetEntryNode> result = new ArrayList<>();
        for (PMDRuleSetEntryNode node : ruleSetResultNodes) {
            int childCount = node.getChildCount();
            List<Integer> oldNodes = new ArrayList<>();
            for (int i = 0; i < childCount; i++) {
                TreeNode child = node.getChildAt(i);
                if (child instanceof PMDViolationNode violationNode) {
                    PMDViolation pmdViolation = violationNode.getPmdViolation();
                    Set<Integer> collect = IntStream.range(pmdViolation.getBeginLine(), pmdViolation.getEndLine() + 1)
                            .boxed()
                            .collect(Collectors.toSet());
                    String classPath = pmdViolation.getPackageName() + "." + pmdViolation.getClassName();
                    Set<Integer> changeLines = changeLineMap.getOrDefault(classPath, Set.of());
//                    忽略以前的violation
                    if (CollectionUtil.intersect(changeLines, collect).isEmpty()) {
                        oldNodes.add(i);
                    }
                }

            }
            oldNodes.forEach(node::remove);
            if (node.getChildCount() > 0) {
                result.add(node);
            }
        }
        return result;
    }

    private PMDRuleSetNode createRuleSetNodeWithResults(String ruleSetPath, List<PMDRuleSetEntryNode> ruleResultNodes) {
        ruleSetPath = PMDUtil.getFileNameFromPath(ruleSetPath) + ";" + ruleSetPath;
        PMDRuleSetNode ruleSetNode = PMDTreeNodeFactory.getInstance().createRuleSetNode(ruleSetPath);

        for (PMDRuleSetEntryNode ruleResultNode : ruleResultNodes) {
            ruleSetNode.add(ruleResultNode);
        }
        return ruleSetNode;
    }

    @NotNull
    private ReturnResult processScanResults(List<PMDRuleSetNode> ruleSetResultNodes, Project project) {
        int violations = toViolations(ruleSetResultNodes);
        if (violations > 0) {
            int answer = promptUser(project, violations);
//            todo 如果有violation不能提交，在这处理
            if (answer == Messages.OK) {
                showToolWindow(ruleSetResultNodes, project);
                return ReturnResult.CLOSE_WINDOW;
            }
            if (answer == Messages.CANCEL || answer == -1) {
                return ReturnResult.CANCEL;
            }
        }
        return ReturnResult.COMMIT;
    }

    private int toViolations(List<PMDRuleSetNode> ruleSetResultNodes) {
        int violations = 0;
        for (PMDRuleSetNode ruleSetResultNode : ruleSetResultNodes) {
            violations += ruleSetResultNode.getViolationCount();
        }
        return violations;
    }

    private int promptUser(Project project, int violations) {
        String[] buttons = new String[]{message("handler.before.checkin.error.review"),
                checkinProjectPanel.getCommitActionName(),
                CommonBundle.getCancelButtonText()};

        return Messages.showDialog(project, message("handler.before.checkin.error.text", violations),
                message("handler.before.checkin.error.title"), buttons, 0, UIUtil.getWarningIcon());
    }

    private void showToolWindow(List<PMDRuleSetNode> ruleSetResultNodes, Project project) {
        PMDProjectComponent plugin = project.getComponent(PMDProjectComponent.class);
        PMDResultPanel resultPanel = plugin.getResultPanel();
        plugin.setupToolWindow();

        PMDRootNode rootNode = resultPanel.getRootNode();
        for (PMDBranchNode ruleSetNode : ruleSetResultNodes) {
            resultPanel.addNode(rootNode, ruleSetNode);
        }
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(PMDProjectComponent.TOOL_ID);
        if (toolWindow != null) {
            toolWindow.activate(null);
        }
        plugin.setLastRunActionAndRules(null, String.join(PMDInvoker.RULE_DELIMITER, plugin.getCustomRuleSetPaths()), true);
    }
}
