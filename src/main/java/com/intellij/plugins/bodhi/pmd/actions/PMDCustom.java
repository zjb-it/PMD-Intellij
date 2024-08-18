package com.intellij.plugins.bodhi.pmd.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.plugins.bodhi.pmd.PMDProjectComponent;
import org.jetbrains.annotations.NotNull;

public class PMDCustom extends DefaultActionGroup {

    public PMDCustom() {
        super(PMDProjectComponent.TITLE, true);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

}
