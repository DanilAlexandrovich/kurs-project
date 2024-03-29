package DD.Test_Kurs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import DD.Test_Kurs.entity.UserActionLog;
import DD.Test_Kurs.service.UserActionLogService;

import java.util.List;

@Controller
public class LogsController {
    private final UserActionLogService userActionLogService;

    @Autowired
    public LogsController(UserActionLogService userActionLogService) {
        this.userActionLogService = userActionLogService;
    }

    @GetMapping("/user-action-logs")
    public String getUserActionLogs(Model model) {
        List<UserActionLog> userActionLogs = userActionLogService.getAllUserLogs();
        model.addAttribute("userActionLogs", userActionLogs);
        return "user-action-logs";
    }
}