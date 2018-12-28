import com.hbt.utils.MyLogger;
import com.intellij.ide.todo.AllTodosTreeBuilder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.apache.log4j.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.intellij.ide.actions.ShowRecentlyEditedFilesAction;
import com.intellij.ide.actions.Switcher;
import com.intellij.ide.bookmarks.Bookmark;
import com.intellij.ide.bookmarks.BookmarkManager;
import com.intellij.ide.todo.AllTodosTreeBuilder;
import com.intellij.ide.todo.SmartTodoItemPointer;
import com.intellij.ide.todo.TodoTreeStructure;
import com.intellij.ide.todo.nodes.TodoFileNode;
import com.intellij.ide.todo.nodes.TodoItemNode;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.treeStructure.Tree;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.*;


// TODO(hbt) ENHANCE PreviousToDoAction
// TODO(hbt) ENHANCE github + readme + topics + jar

public class NextToDoAction extends AnAction {

    public static Logger log = MyLogger.getLogger();
    
    public static HashMap<Project, Integer> last = new HashMap();

    @Override
    public void actionPerformed(AnActionEvent e) {
        log.debug("init");

        Project project = e.getProject();
        ArrayList todosMap = new ArrayList();

        // build list
        {
            ArrayList<SmartTodoItemPointer> todos = new ArrayList();

            {

                AllTodosTreeBuilder all = new AllTodosTreeBuilder(new Tree(), project);
                all.init();
                AbstractTreeStructure structure = all.getTodoTreeStructure();
                ((TodoTreeStructure) structure).setFlattenPackages(true);

                TodoItemNode current = all.getFirstPointerForElement(structure.getRootElement());
                if (current != null) {
                    TodoItemNode next = current;
                    do {
//
//                String text = next.getValue().getTodoItem().getFile().getText();
////                logger.info(next.getTestPresentation());
////                logger.info("" + next.getValue().getTodoItem().getPattern().getPatternString());
////                logger.info("" + next.getValue().getTodoItem().getTextRange().toString());
//                int startOffset = next.getValue().getTodoItem().getTextRange().getStartOffset();
//                String strtodo = text.substring(startOffset, next.getValue().getTodoItem().getTextRange().getEndOffset());
////                logger.info("" + strtodo);
//
//                String strtodo2 = strtodo.trim().replace("TODO(hbt) NEXT", "").trim();
////                logger.info("" + strtodo2.split(" ")[0]);
////                logger.debug("" + strtodo2.split(" ")[0]);
//                int startOffset = next.getValue().getTodoItem().getTextRange().getStartOffset();
//                int lineStartOffset = next.getValue().getDocument().getLineNumber(startOffset);
////                logger.info("" + lineStartOffset);
//                log.debug(strtodo);
//
////        logger.info(""+ current.getValue().getTodoItem().getTextRange().getStartOffset());
////        logger.info(""+ current.getValue().getTodoItem().getTextRange().getEndOffset());

                        // filter next pattern

                        // add to list

                        // add 
//                    int startOffset = next.getValue().getTodoItem().getTextRange().getStartOffset();
                        todos.add(next.getValue());

                        next = all.getNextPointer(next);

                    } while (next != null);

                }

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
                            int nbDots = first.split("\\.").length-1;
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
                        
                        map.put(posi,todo);
                    }

                    Iterator<Integer> iterator = map.navigableKeySet().iterator();
                    while(iterator.hasNext())
                    {
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
                for(int i = 0; i < sortedTodos.size(); i++)
                {
                    SmartTodoItemPointer todo = sortedTodos.get(i);
                    int startOffset = todo.getTodoItem().getTextRange().getStartOffset();
                    int lineStartOffset = todo.getDocument().getLineNumber(startOffset);
                    todosMap.add(new Object[]{todo.getTodoItem().getFile().getVirtualFile(), lineStartOffset});
                }
            }
        }

        if(todosMap.size() == 0)
        {
            return;
        }

        // get last item
        int nextIndex = 0;
        {
            // TODO(hbt) NEXT fix
            if (last.containsKey(e.getProject())) {
                nextIndex = last.get(e.getProject());
                nextIndex++;
            }
        }
        
        // jump to next location
        
        {
            if (nextIndex >= todosMap.size()) {
                nextIndex = 0;
            }
            Object[] nextItem = (Object[]) todosMap.get(nextIndex);
            VirtualFile file = (VirtualFile) nextItem[0];
            Integer lineNumber = (Integer) nextItem[1];
                    

            BookmarkManager bookmarkManager = BookmarkManager.getInstance(e.getProject());
            bookmarkManager.addTextBookmark(file,lineNumber,"");
            List<Bookmark> validBookmarks = bookmarkManager.getValidBookmarks();
            Bookmark bookmark = validBookmarks.get(validBookmarks.size() - 1);
            bookmark.navigate(true);
            bookmarkManager.removeBookmark(bookmark);
            
            last.put(e.getProject(),nextIndex);
            
//            List<Bookmark> validBookmarks2 = bookmarkManager.getValidBookmarks();
//
//            Iterator<Bookmark> iterator = validBookmarks2.iterator();
//            while (iterator.hasNext()) {
//                Bookmark next1 = iterator.next();
//                bookmarkManager.removeBookmark(next1);
//            }

//            VirtualFile file = current.getValue().getTodoItem().getFile().getVirtualFile();
//            bookmarkManager.addFileBookmark(file,)

//            bookmarkManager.addTextBookmark(file, 2, "test");
//            Bookmark fileBookmark = bookmarkManager.findFileBookmark(file);
//            List<Bookmark> validBookmarks = bookmarkManager.getValidBookmarks();
//
//
//            validBookmarks.get(0).navigate(true);
            
        }

//        HashMap<String, Integer> m = new HashMap<String, Integer>();
//        ArrayList l = new ArrayList();
//        l.add(new Object[]{2,3});
//        l.add(new Object[]{2,3});

    }

    ArrayList buildList(Project project) {
        ArrayList ret = new ArrayList();

        AllTodosTreeBuilder all = new AllTodosTreeBuilder(new Tree(), project);
        all.init();

        AbstractTreeStructure structure = all.getTodoTreeStructure();
        ((TodoTreeStructure) structure).setFlattenPackages(true);

        TodoItemNode current = all.getFirstPointerForElement(structure.getRootElement());
        if (current != null) {
            TodoItemNode next = current;
            do {

                String text = next.getValue().getTodoItem().getFile().getText();
//                logger.info(next.getTestPresentation());
//                logger.info("" + next.getValue().getTodoItem().getPattern().getPatternString());
//                logger.info("" + next.getValue().getTodoItem().getTextRange().toString());
                int startOffset = next.getValue().getTodoItem().getTextRange().getStartOffset();
                String strtodo = text.substring(startOffset, next.getValue().getTodoItem().getTextRange().getEndOffset());
//                logger.info("" + strtodo);

                String strtodo2 = strtodo.trim().replace("TODO(hbt) NEXT", "").trim();
//                logger.info("" + strtodo2.split(" ")[0]);
//                logger.debug("" + strtodo2.split(" ")[0]);
                int lineStartOffset = next.getValue().getDocument().getLineNumber(startOffset);
//                logger.info("" + lineStartOffset);
                log.debug(strtodo);

//        logger.info(""+ current.getValue().getTodoItem().getTextRange().getStartOffset());
//        logger.info(""+ current.getValue().getTodoItem().getTextRange().getEndOffset());
                next = all.getNextPointer(next);

            } while (next != null);

        }

        return ret;
    }


}
