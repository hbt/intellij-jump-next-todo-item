package com.hbt.todos.next;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

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

public abstract class ToDoCommonAction extends AnAction {

    public static Logger log = MyLogger.getLogger();
    public static HashMap<Project, Integer> last = new HashMap();


    public void jump(AnActionEvent e, ArrayList todosMap, int nextIndex) {
        Object[] nextItem = (Object[]) todosMap.get(nextIndex);
        VirtualFile file = (VirtualFile) nextItem[0];
        Integer lineNumber = (Integer) nextItem[1];


        BookmarkManager bookmarkManager = BookmarkManager.getInstance(e.getProject());
        bookmarkManager.addTextBookmark(file, lineNumber, "");
        List<Bookmark> validBookmarks = bookmarkManager.getValidBookmarks();
        Bookmark bookmark = validBookmarks.get(validBookmarks.size() - 1);
        bookmark.navigate(true);
        bookmarkManager.removeBookmark(bookmark);

        last.put(e.getProject(), nextIndex);
    }

    public int getLastItem(AnActionEvent e) {
        // Note(hbt) this uses the index. i.e if new todos are added, it might jump around. consider enhancing and using a hash of the virtual file + line number
        int nextIndex = 0;
        if (last.containsKey(e.getProject())) {
            nextIndex = last.get(e.getProject());
        }
        return nextIndex;
    }

    static HashMap<String, ArrayList<TodoItemNode>> cache = new HashMap<>();
    
    public void recursiveGet(Project p, AbstractTreeStructure structure, Object obj) {
        Object[] children = structure.getChildElements(obj);
        for(int i =0; i< children.length; i++) {
            //add 
            if(children[i] instanceof TodoItemNode)
            {

                ArrayList<TodoItemNode> todoItemNodes = cache.get(p.getLocationHash());
                todoItemNodes.add((TodoItemNode) children[i]);
            }
            recursiveGet(p, structure, children[i]);
        }
    }

    public ArrayList buildList(Project project) {
        ArrayList todosMap = new ArrayList();

        ArrayList<SmartTodoItemPointer> todos = new ArrayList();

        {

            AllTodosTreeBuilder all = new AllTodosTreeBuilder(new Tree(), project);
            all.init();
            AbstractTreeStructure structure = all.getTodoTreeStructure();
            ((TodoTreeStructure) structure).setFlattenPackages(true);
            
            cache.clear();
            cache.put(project.getLocationHash(),new ArrayList<TodoItemNode>());
            recursiveGet(project, structure , structure.getRootElement());
            

            ArrayList<TodoItemNode> todoItemNodes = cache.get(project.getLocationHash());
            
            todoItemNodes.forEach((todo) -> {
                todos.add(todo.getValue());
            });

        }

        ArrayList<SmartTodoItemPointer> nextTodos = new ArrayList();
        {
            // filter
            todos.forEach((todo) -> {
                String patternString = todo.getTodoItem().getPattern().getPatternString();

                if (patternString.equals("\\b.*todo\\b.*hbt\\b.*NEXT\\b.*")) {

                    nextTodos.add(todo);
                }

            });

        }

        ArrayList<SmartTodoItemPointer> sortedTodos = new ArrayList();
        {
            // filter with/without number

            ArrayList numberedTodos = new ArrayList();
            ArrayList<SmartTodoItemPointer> strTodos = new ArrayList();
            int maxNbDots = 0;
            {

                for (int i = 0; i < nextTodos.size(); i++) {
                    SmartTodoItemPointer todo = nextTodos.get(i);
                    String text = todo.getTodoItem().getFile().getText();
                    int startOffset = todo.getTodoItem().getTextRange().getStartOffset();
                    String strtodo = text.substring(startOffset, todo.getTodoItem().getTextRange().getEndOffset());
                    strtodo = strtodo.replace("TODO(hbt) NEXT", "").trim();

                    String[] parts = strtodo.split(" ");
                    if (parts.length > 0) {
                        String first = parts[0];
                        int nbDots = first.split("\\.").length - 1;
                        if (nbDots > maxNbDots) {
                            maxNbDots = nbDots;
                        }
                        String bigNumber = first.replace(".", "").trim();
                        int nb = -1;
                        try {

                            nb = Integer.parseInt(bigNumber);
                        } catch (Exception ex) {

                        }
                        if (nb == -1) {
                            strTodos.add(todo);
                            log.debug("strtodo: " + strtodo);
                        } else {
                            numberedTodos.add(new Object[]{todo, nb, nbDots});
//                                nb = nb * 10^nbDots;
//                                log.debug("" + nb);
                            log.debug("nb todo: " + strtodo);
                        }
                    }
                }


            }

            // sort list with numbers
            TreeMap<Integer, SmartTodoItemPointer> map = new TreeMap();
            {

                log.debug("" + maxNbDots);
                for (int i = 0; i < numberedTodos.size(); i++) {
                    Object[] items = (Object[]) numberedTodos.get(i);
                    SmartTodoItemPointer todo = (SmartTodoItemPointer) items[0];
                    Integer nb = (Integer) items[1];
                    Integer nbDots = (Integer) items[2];

                    int posi = (int) (nb * Math.pow(10, maxNbDots - nbDots));
                    log.debug("" + posi);

                    map.put(posi, todo);
                }

                Iterator<Integer> iterator = map.navigableKeySet().iterator();
                while (iterator.hasNext()) {
                    Integer next = iterator.next();
                    SmartTodoItemPointer todo = map.get(next);

                    String text = todo.getTodoItem().getFile().getText();
                    int startOffset = todo.getTodoItem().getTextRange().getStartOffset();
                    String strtodo = text.substring(startOffset, todo.getTodoItem().getTextRange().getEndOffset());
                    log.debug("ordered" + strtodo);

                    sortedTodos.add(todo);
                }

            }

            // add without numbers at the bottom
            {
                strTodos.forEach((todo) -> {
                    sortedTodos.add(todo);
                });
            }
        }

        {
            // map list
            for (int i = 0; i < sortedTodos.size(); i++) {
                SmartTodoItemPointer todo = sortedTodos.get(i);
                int startOffset = todo.getTodoItem().getTextRange().getStartOffset();
                int lineStartOffset = todo.getDocument().getLineNumber(startOffset);
                todosMap.add(new Object[]{todo.getTodoItem().getFile().getVirtualFile(), lineStartOffset});
            }
        }

        return todosMap;
    }

}
