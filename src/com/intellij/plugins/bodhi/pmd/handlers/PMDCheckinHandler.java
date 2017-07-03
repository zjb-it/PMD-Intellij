package com.intellij.plugins.bodhi.pmd.handlers;

import com.intellij.CommonBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.plugins.bodhi.pmd.PMDProjectComponent;
import com.intellij.plugins.bodhi.pmd.PMDResultPanel;
import com.intellij.plugins.bodhi.pmd.PMDUtil;
import com.intellij.plugins.bodhi.pmd.core.PMDResultCollector;
import com.intellij.plugins.bodhi.pmd.tree.PMDRuleNode;
import com.intellij.util.PairConsumer;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.intellij.openapi.vcs.checkin.CheckinHandler.ReturnResult.COMMIT;

public class PMDCheckinHandler extends CheckinHandler {

    private static final Log LOG = LogFactory.getLog(PMDCheckinHandler.class);
    private static final String BUNDLE = "com.intellij.plugins.bodhi.pmd.PMD-Intellij";

    @NonNls
    private final CheckinProjectPanel checkinProjectPanel;

    public PMDCheckinHandler(CheckinProjectPanel checkinProjectPanel) {
        this.checkinProjectPanel = checkinProjectPanel;
    }

    @Nullable
    public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
        JCheckBox checkBox = new JCheckBox(message("handler.before.checkin.checkbox"));

        Project project = checkinProjectPanel.getProject();
        PMDProjectComponent projectComponent = project.getComponent(PMDProjectComponent.class);
        return new RefreshableOnComponent() {
            @Override
            public JComponent getComponent() {
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(checkBox);
                return panel;
            }

            @Override
            public void refresh() {
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
        return CommonBundle.message(ResourceBundle.getBundle(BUNDLE), key, params);
    }

    @Override
    public ReturnResult beforeCheckin(@Nullable CommitExecutor executor,
                                      PairConsumer<Object, Object> additionalDataConsumer) {
        Project project = checkinProjectPanel.getProject();
        if (project == null) {
            LOG.error("Could not get project for check-in panel, skipping");
            return ReturnResult.COMMIT;
        }

        PMDProjectComponent plugin = project.getComponent(PMDProjectComponent.class);
        if (plugin == null) {
            LOG.error("Could not get PMD Plug-in, skipping");
            return COMMIT;
        }

        if (!plugin.isScanFilesBeforeCheckin()) {
            return ReturnResult.COMMIT;
        }

        plugin.setupToolWindow();

        List<String> rules = plugin.getCustomRuleSets();
        PMDResultPanel resultPanel = plugin.getResultPanel();
        PMDRuleNode rootNodeData = ((PMDRuleNode) resultPanel.getRootNode().getUserObject());

        PMDResultCollector.report = null;
        for (String rule : rules) {
            PMDResultCollector collector = new PMDResultCollector(true);
            List<File> files = new ArrayList<>(checkinProjectPanel.getFiles());
            List<DefaultMutableTreeNode> results = collector.getResults(files, rule, plugin.getOptions());
            if (!results.isEmpty()) {
                //For custom rulesets, using a separate format for rendering
                rule = PMDUtil.getRuleNameFromPath(rule) + ";" + rule;

                DefaultMutableTreeNode node = resultPanel.addNode(rule);
                //Add all nodes to the tree
                int childCount = 0;
                for (DefaultMutableTreeNode pmdResult : results) {
                    resultPanel.addNode(node, pmdResult);
                    childCount += ((PMDRuleNode) pmdResult.getUserObject()).getChildCount();
                }
                ((PMDRuleNode) node.getUserObject()).addChildren(childCount);
                rootNodeData.addChildren(childCount);
            }
        }
        int errorCount = resultPanel.getRootNode().getChildCount();
        return processScanResults(project, errorCount);
    }

    @NotNull
    private ReturnResult processScanResults(Project project, int errorCount) {
        if (errorCount > 0) {
            int answer = promptUser(project, errorCount);
            if (answer == Messages.OK) {
                showToolWindow(project);
                return ReturnResult.CLOSE_WINDOW;
            }
            if (answer == Messages.CANCEL) {
                return ReturnResult.CANCEL;
            }
        }
        return ReturnResult.COMMIT;
    }

    private void showToolWindow(Project project) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(PMDProjectComponent.TOOL_ID);
        toolWindow.activate(null);
    }

    private int promptUser(Project project, int errorCount) {
        String[] buttons = new String[]{message("handler.before.checkin.error.review"),
                checkinProjectPanel.getCommitActionName(),
                CommonBundle.getCancelButtonText()};

        return Messages.showDialog(project, message("handler.before.checkin.error.text", errorCount),
                message("handler.before.checkin.error.title"), buttons, 0, UIUtil.getWarningIcon());
    }
}
