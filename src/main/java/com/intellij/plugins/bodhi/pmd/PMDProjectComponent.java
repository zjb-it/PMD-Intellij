package com.intellij.plugins.bodhi.pmd;

import com.intellij.AbstractBundle;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.plugins.bodhi.pmd.actions.PMDCustom;
import com.intellij.plugins.bodhi.pmd.actions.PreDefinedMenuGroup;
import com.intellij.plugins.bodhi.pmd.core.PMDResultCollector;
import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.intellij.plugins.bodhi.pmd.actions.PreDefinedMenuGroup.RULESETS_FILENAMES_KEY;
import static com.intellij.plugins.bodhi.pmd.handlers.PMDCheckinHandler.BUNDLE;

/**
 * This is the Project Component of the PMD Plugin.
 *
 * @author bodhi
 * @version 1.0
 */

@State(
        name = "PDMPlugin",
        storages = {
                @Storage(
                        value = "$PROJECT_FILE$"
                )}
)
public class PMDProjectComponent implements ProjectComponent, PersistentStateComponent<PersistentData>, Disposable {

    /**
     * The Tool ID of the results panel.
     */
    public static final String TOOL_ID = "PMD";

    private static final String COMPONENT_NAME = "PMDProjectComponent";

    public static final String TITLE = AbstractBundle.message(ResourceBundle.getBundle(BUNDLE), "handler.before.checkin.error.title");

    private final Project currentProject;
    private static final AtomicInteger numProjectsOpen = new AtomicInteger();
    private PMDResultPanel resultPanel;
    private ToolWindow resultWindow;
    private String lastRunRuleSetPaths;
    private boolean lastRunRulesCustom;
    private AnActionEvent lastRunActionEvent;
    private Set<String> customRuleSetPaths = new LinkedHashSet<>(); // avoid duplicates, maintain order
    private Set<String> defaultCustomRuleSetPaths = new LinkedHashSet<>(); // avoid duplicates, maintain order
    private Map<ConfigOption, String> optionToValue = new EnumMap<>(ConfigOption.class);
    private final ToolWindowManager toolWindowManager;
    private boolean skipTestSources;
    private boolean scanFilesBeforeCheckin;
    private Set<String> inEditorAnnotationRuleSets = new LinkedHashSet<>(); // avoid duplicates, maintain order
    private List<String> deletedRuleSetPaths = Collections.emptyList();
    public static final String CUSTOM_RULESETS_PROPERTY_FILE = "rulesets/ruleset.properties";

    private boolean autoMerge ;

    private Integer autoPullInterval;
    /**
     * Creates a PMD Project component based on the project given.
     *
     * @param project The project on which to create the component.
     */
    public PMDProjectComponent(Project project) {
        this.currentProject = project;
        toolWindowManager = ToolWindowManager.getInstance(currentProject);
        numProjectsOpen.incrementAndGet();
    }

    public void initComponent() {
        //Add custom rules as menu items if defined.
        updateCustomRulesMenu();

        ActionGroup actionGroup = registerActions("PMDPredefined");
        if (actionGroup != null)
            ((PreDefinedMenuGroup) actionGroup).setComponent(this);
        registerActions(TITLE);
    }

    private ActionGroup registerActions(String actionName) {
        ActionManager actionMgr = ActionManager.getInstance();
        ActionGroup actionGroup = (ActionGroup) actionMgr.getAction(actionName);
        if (actionGroup != null) {
            for (AnAction act : actionGroup.getChildren(null)) {
                String actName = act.getTemplatePresentation().getText();
                if (actionMgr.getAction(actName) == null)
                    actionMgr.registerAction(actName, act);
            }
        }
        return actionGroup;
    }

    private boolean hasDuplicateBareFileName(Iterable<String> paths) {
        boolean duplicate = false;
        List<String> fileNames = new ArrayList<>();
        for (String path : paths) {
            String fileName = PMDUtil.getBareFileNameFromPath(path);
            if (fileNames.contains(fileName)) {
                duplicate = true;
                break;
            }
            fileNames.add(fileName);
        }
        return duplicate;
    }

    /**
     * Reflect customRuleSetPaths into actionGroup (ActionManager singleton instance)
     * Better solution is an ActionManager for each project and
     * one shared configuration/settings for all projects, as assumed expected by user
     * Now for > 1 projects open, merge the rule sets of shared actions (menu) and current project
     */
    void updateCustomRulesMenu() {

        try {
            Properties props = new Properties();
            props.load(getRuleResourceStream());
            String[] rulesetFilenames = props.getProperty(RULESETS_FILENAMES_KEY).split(PMDInvoker.RULE_DELIMITER);
            for (String rulesetFilename : rulesetFilenames) {
                customRuleSetPaths.add(rulesetFilename);
                defaultCustomRuleSetPaths.add(rulesetFilename);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PMDCustom actionGroup = (PMDCustom) ActionManager.getInstance().getAction("PMDCustom");
        if (numProjectsOpen.get() != 1) {
            // merge actions from menu and from settings to not lose any when switching between projects
            AnAction[] currentActions = actionGroup.getChildren(null);
            Set<String> ruleSetPathsFromMenu = new LinkedHashSet<>();
            for (AnAction action : currentActions) {
                if (action.getSynonyms().size() == 1) {
                    String ruleSetPath = action.getSynonyms().get(0).get();
                    ruleSetPathsFromMenu.add(ruleSetPath.trim());
                }
            }
            customRuleSetPaths.addAll(ruleSetPathsFromMenu);
            // remove the ones just explicitly deleted in config
            customRuleSetPaths.removeAll(deletedRuleSetPaths);
        }
        List<AnAction> newActionList = new ArrayList<>();
        boolean hasDuplicate = hasDuplicateBareFileName(customRuleSetPaths);
        for (final String ruleSetPath : customRuleSetPaths) {
            String ruleSetName = PMDResultCollector.getRuleSetName(ruleSetPath);
            String extFileName = PMDUtil.getExtendedFileNameFromPath(ruleSetPath);
            String bareFileName = PMDUtil.getBareFileNameFromPath(ruleSetPath);
            String actionText = ruleSetName;
//            if (!ruleSetName.equals(bareFileName) || hasDuplicate) {
            if (hasDuplicate) {
                actionText += " (" + extFileName + ")";
            }
            AnAction action = new AnAction(actionText) {
                public void actionPerformed(AnActionEvent e) {
                    PMDInvoker.getInstance().runPMD(e, ruleSetPath, true);
                    setLastRunActionAndRules(e, ruleSetPath, true);
                }
            };
            action.addSynonym(() -> ruleSetPath);
            newActionList.add(action);
        }
        actionGroup.removeAll();
        actionGroup.addAll(newActionList);
    }

    public void dispose() {
        numProjectsOpen.decrementAndGet();
    }
    private @Nullable InputStream getRuleResourceStream() {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(CUSTOM_RULESETS_PROPERTY_FILE);
        if (resourceAsStream == null) {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(CUSTOM_RULESETS_PROPERTY_FILE);
        }
        return resourceAsStream;
    }
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public void projectOpened() {
        resultPanel = new PMDResultPanel(this);
    }

    /**
     * Registers a tool window for showing PMD results.
     */
    private void registerToolWindow() {
        if (toolWindowManager.getToolWindow(TOOL_ID) == null) {
            resultWindow = toolWindowManager.registerToolWindow(TOOL_ID, true, ToolWindowAnchor.BOTTOM);
            Content content = ContentFactory.getInstance().createContent(resultPanel, "", false);
            resultWindow.getContentManager().addContent(content);
            resultWindow.setType(ToolWindowType.DOCKED, null);
        }
    }

    /**
     * Gets the result panel where the PMD results are shown.
     *
     * @return The panel where results are shown.
     */
    public PMDResultPanel getResultPanel() {
        return resultPanel;
    }

    /**
     * Set up the tool window and initializes the result tree.
     */
    public void setupToolWindow() {
        registerToolWindow();
        resultPanel.initializeTree();
    }

    /**
     * Close the result panel and unregister the tool window.
     */
    public void closeResultWindow() {
        resultWindow.hide(null);
        resultPanel.initializeTree();
        if (toolWindowManager.getToolWindow(TOOL_ID) != null)
            toolWindowManager.unregisterToolWindow(TOOL_ID);
    }

    /**
     * Get the current project.
     *
     * @return the current project
     */
    public Project getCurrentProject() {
        return currentProject;
    }

    /**
     * Get the last run PMD rule set paths on this project.
     *
     * @return the last run rule set paths.
     */
    public String getLastRunRuleSetPaths() {
        return lastRunRuleSetPaths;
    }

    /**
     * Return whether the last run PMD rules on this project are custom rules.
     *
     * @return whether the last run rules are custom rules.
     */
    public boolean isLastRunRulesCustom() {
        return lastRunRulesCustom;
    }

    /**
     * Get the last run action event on this project.
     *
     * @return the last run action.
     */
    public AnActionEvent getLastRunAction() {
        return lastRunActionEvent;
    }

    /**
     * Set the last run action event and PMD rule(s). Multiple rules should be delimited by
     * PMDInvoker.RULE_DELIMITER.
     *
     * @param lastActionEvent     the last run action event
     * @param lastRunRuleSetPaths The last run rule set paths
     * @param isCustom            whether the last run rules are custom rules
     */
    public void setLastRunActionAndRules(AnActionEvent lastActionEvent, String lastRunRuleSetPaths, boolean isCustom) {
        this.lastRunRuleSetPaths = lastRunRuleSetPaths;
        this.lastRunActionEvent = lastActionEvent;
        this.lastRunRulesCustom = isCustom;
    }

    public List<String> getCustomRuleSetPaths() {
        LinkedHashSet<String> result = new LinkedHashSet<>(customRuleSetPaths);
//        result.addAll(AbstractLuBanRule.ruleSetPath);
        return new ArrayList<>(result);
    }

    public Set<String> getDefaultCustomRuleSetPaths() {
        return defaultCustomRuleSetPaths;
    }

    public void setCustomRuleSetPaths(List<String> customRuleSetPaths) {
        this.customRuleSetPaths = new LinkedHashSet<>(customRuleSetPaths);
    }

    public void setDeletedRuleSetPaths(List<String> deletedRuleSetPaths) {
        this.deletedRuleSetPaths = deletedRuleSetPaths;
    }

    public Set<String> getInEditorAnnotationRuleSets() {
        return inEditorAnnotationRuleSets;
    }

    public void setInEditorAnnotationRuleSets(List<String> inEditorAnnotationRules) {
        this.inEditorAnnotationRuleSets = new LinkedHashSet<>(inEditorAnnotationRules);
    }

    public Map<ConfigOption, String> getOptionToValue() {
        return Map.copyOf(optionToValue); // unmodifiable
    }

    public void setOptionToValue(Map<ConfigOption, String> optionToValue) {
        this.optionToValue = optionToValue;
    }

    /**
     * Return fields in a PersistentData object
     *
     * @return
     */
    @NotNull
    public PersistentData getState() {
        final PersistentData persistentData = new PersistentData();
        for (String item : customRuleSetPaths) {
            persistentData.getCustomRuleSets().add(item);
        }
        for (ConfigOption option : optionToValue.keySet()) {
            persistentData.getOptionKeyToValue().put(option.getKey(), optionToValue.get(option));
        }
        persistentData.setSkipTestSources(skipTestSources);
        persistentData.setScanFilesBeforeCheckin(scanFilesBeforeCheckin);

        for (String item : inEditorAnnotationRuleSets) {
            persistentData.getInEditorAnnotationRules().add(item);
        }
        persistentData.setAutoPullInterval(this.autoPullInterval);
        persistentData.setAutoMerge(this.autoMerge);
        return persistentData;
    }

    /**
     * load state into fields
     *
     * @param state
     */
    public void loadState(PersistentData state) {
        customRuleSetPaths.clear();
        optionToValue.clear();
        customRuleSetPaths.addAll(state.getCustomRuleSets());
        for (String key : state.getOptionKeyToValue().keySet()) {
            if (key.equals("Encoding")) { // replace unused 'Encoding' by 'Statistics URL'
                optionToValue.put(ConfigOption.STATISTICS_URL, "");
            } else {
                optionToValue.put(ConfigOption.fromKey(key), state.getOptionKeyToValue().get(key));
            }
        }
        this.autoPullInterval = state.getAutoPullInterval();
        inEditorAnnotationRuleSets.clear();
        inEditorAnnotationRuleSets.addAll(state.getInEditorAnnotationRules());

        this.skipTestSources = state.isSkipTestSources();
        this.scanFilesBeforeCheckin = state.isScanFilesBeforeCheckin();
        this.autoMerge = state.isAutoMerge();
    }

    public void skipTestSources(boolean skipTestSources) {
        this.skipTestSources = skipTestSources;
    }

    public boolean isSkipTestSources() {
        return skipTestSources;
    }

    public void setScanFilesBeforeCheckin(boolean scanFilesBeforeCheckin) {
        this.scanFilesBeforeCheckin = scanFilesBeforeCheckin;
    }

    public boolean isScanFilesBeforeCheckin() {
        return scanFilesBeforeCheckin;
    }

    public Integer getAutoPullInterval() {
        return autoPullInterval;
    }

    public void setAutoPullInterval(String autoPullInterval) {
        if (StringUtils.isBlank(autoPullInterval)) {
            autoPullInterval = "60";
        }
        this.autoPullInterval = Integer.parseInt(autoPullInterval);
    }

    public boolean isAutoMerge() {
        return autoMerge;
    }

    public void setAutoMerge(boolean autoMerge) {
        this.autoMerge = autoMerge;
    }
}
