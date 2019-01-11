package com.hbt.todos.next;

import com.hbt.utils.MyLogger;
import com.intellij.ide.bookmarks.Bookmark;
import com.intellij.ide.bookmarks.BookmarkManager;
import com.intellij.ide.todo.AllTodosTreeBuilder;
import com.intellij.ide.todo.SmartTodoItemPointer;
import com.intellij.ide.todo.TodoTreeStructure;
import com.intellij.ide.todo.nodes.TodoItemNode;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;
import org.apache.log4j.Logger;

import java.util.*;


public class NextToDoAction extends ToDoCommonAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        log.debug("init next");

        Project project = e.getProject();
        ArrayList todosMap = todosMap = buildList(project);

        if (todosMap.size() == 0) {
            return;
        }

        // get last item
        int nextIndex = getLastItem(e);
        nextIndex++;

        // jump to next location
        if (nextIndex >= todosMap.size()) {
            nextIndex = 0;
        }
        
        if(log.isDebugEnabled())
        {
            viewTodos(todosMap);
        }

        jump(e, todosMap, nextIndex);


    }


}
