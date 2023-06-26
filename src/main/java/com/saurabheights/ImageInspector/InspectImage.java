package com.saurabheights.ImageInspector;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.pom.Navigatable;
import com.intellij.xdebugger.frame.XValueContainer;
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.jetbrains.python.debugger.ArrayChunk;
import com.jetbrains.python.debugger.PyDebugValue;
import com.jetbrains.python.debugger.PyDebuggerException;
import com.jetbrains.python.debugger.PyFrameAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

import static com.intellij.xdebugger.impl.ui.tree.actions.XDebuggerTreeActionBase.getSelectedNode;

public class InspectImage extends AnAction {

    private static final Logger LOG = Logger.getInstance(InspectImage.class);

    /**
     * Performs the action logic.
     * <p/>
     * It is called on the UI thread with all data in the provided {@link DataContext} instance.
     *
     * @param e
     * @see #beforeActionPerformedUpdate(AnActionEvent)
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Using the event, create and show a dialog
        Project currentProject = e.getProject();
        StringBuilder dlgMsg = new StringBuilder(e.getPresentation().getText() + " Selected!");
        String dlgTitle = e.getPresentation().getDescription();
        LOG.info("actionPerformed: " + dlgTitle);
        // If an element is selected in the editor, add info about it.
        Navigatable nav = e.getData(CommonDataKeys.NAVIGATABLE);
        if (nav != null) {
            dlgMsg.append(String.format("\nSelected Element: %s", nav.toString()));
        }
        Messages.showMessageDialog(currentProject, dlgMsg.toString(), "OpenCV Plugin", Messages.getInformationIcon());
    }

    /**
     * Performs the action logic.
     * <p/>
     * It is called on the UI thread with all data in the provided {@link DataContext} instance.
     *
     * @param e
     * @see #beforeActionPerformedUpdate(AnActionEvent)
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(false);

        XDebuggerTree xDebuggerTree = XDebuggerTree.getTree(e);
        if(xDebuggerTree.getSelectionPaths() == null) { // No variable selected
            return;
        }
        // ToDo - Allow multiple variables, if some of them are numpy variables. Change text accordingly.
        if (xDebuggerTree.getSelectionPaths().length>1) { // Multiple variables selected
            LOG.debug(Arrays.toString(xDebuggerTree.getSelectionPaths()) + " - length - " + xDebuggerTree.getSelectionPaths().length);
            return;
        }

        XValueNodeImpl node = getSelectedNode(e.getDataContext());
        if (node == null || !node.isComputed()) {
            LOG.debug("Node is not yet computed.");
            return;
        }

        XValueContainer xValueContainer = node.getValueContainer();
        PyDebugValue value = (PyDebugValue)xValueContainer;
        LOG.debug("DebugValue type " + value.getType());
        if (Objects.equals(value.getType(), "ndarray")) {
            // ToDo Check image type, shapes, etc.
            e.getPresentation().setEnabledAndVisible(true);
            value.getShape();
            PyFrameAccessor frameAccessor = value.getFrameAccessor();
            try {
                // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360003888080-Plugin-Development-Fetch-variable-value-numpy-array-
                ArrayChunk arrayChunk = frameAccessor.getArrayItems(value, 0, 0, -1, -1,"%d");
                Object[] arrayData = arrayChunk.getData();
                // See https://github.com/JetBrains/intellij-community/blob/master/python/src/com/jetbrains/python/debugger/containerview/PyDataView.java for an example.
                LOG.warn("HELLO");
            } catch (PyDebuggerException ex) {
                throw new RuntimeException(ex);
            }

        }
    }
}
