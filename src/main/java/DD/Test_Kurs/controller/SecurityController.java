package DD.Test_Kurs.controller;

import DD.Test_Kurs.config.AuthenticationFacade;
import DD.Test_Kurs.entity.User;
import DD.Test_Kurs.service.UserActionLogService;
import DD.Test_Kurs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import DD.Test_Kurs.dto.CarDto;
import DD.Test_Kurs.dto.UserDto;

import javax.validation.Valid;
import java.util.List;


@Controller
public class SecurityController {

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private UserActionLogService userActionLogService;

    private final UserService userService;

    @Autowired
    public SecurityController(UserService userService) {
        this.userService = userService;

    }

    @GetMapping("/index")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user")UserDto userDto,
                               BindingResult result,
                               Model model) {
        User existingUser = userService.findUserByUsername(userDto.getUsername());

        if (existingUser != null && existingUser.getUsername() != null && !existingUser.getUsername().isEmpty()) {
            result.rejectValue("username", null,
                    "Учетная запись уже существует!");
        }
        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "/register";
        }
        userService.saveUser(userDto);
        return "redirect:/register?success";
    }

    @GetMapping("/add-role")
    public String role(Model model) {
        return "add-role";
    }

    @PostMapping("/add-role")
    public String addRoleToUser(@RequestParam String username, @RequestParam String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        if (authenticationFacade.getAuthentication().getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ADMIN"))) {
            userService.addRoleToUser(username, role);
            userActionLogService.logAction(currentPrincipalName, "Изменение роли пользователя");
        }
        return "redirect:/users";
    }

    @GetMapping("/users")
    public String user(Model model) {
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping("/car-cost-form")
    public String showCarCostForm(Model model) {
        model.addAttribute("carDto", new CarDto());
        return "car-cost-form";
    }

    @PostMapping("/calculate-car-cost")
    public String calculateCarCost(@ModelAttribute("carDto") CarDto carDto, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        double totalCost = calculateTotalCarCost(carDto);
        model.addAttribute("carDto", carDto);
        model.addAttribute("totalCost", totalCost);
        userActionLogService.logAction(currentPrincipalName, "Расчёт стоимости авто");
        return "car-cost-form";
    }

    private double calculateTotalCarCost(CarDto car) {
        return (car.getProductionCosts() + car.getDistributionCosts() / car.getQuantity());
    }
}