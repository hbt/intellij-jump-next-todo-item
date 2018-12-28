import com.hbt.utils.MyLogger;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.apache.log4j.*;


// TODO(hbt) ENHANCE PreviousToDoAction
// TODO(hbt) ENHANCE github + readme + topics + jar

public class NextToDoAction extends AnAction {

    public static Logger log = MyLogger.getLogger();

    @Override
    public void actionPerformed(AnActionEvent e) {
        log.debug("init");
    }
    
    
    
    
}
