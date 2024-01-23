package DD.Test_Kurs.controller;

import DD.Test_Kurs.config.AuthenticationFacade;
import DD.Test_Kurs.entity.Car;
import DD.Test_Kurs.repository.ShowroomRepository;
import DD.Test_Kurs.service.UserActionLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import DD.Test_Kurs.entity.Showroom;
import DD.Test_Kurs.repository.CarRepository;

import java.util.List;
import java.util.Optional;
@Slf4j
@Controller
public class ShowroomController {
    @Autowired
    private ShowroomRepository showroomRepository;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private UserActionLogService userActionLogService;

    @Autowired
    private CarRepository carRepository;

    @GetMapping("/list-showroom")
    public ModelAndView getAllShowroom() {
        log.info("/list -> connection");
        ModelAndView mav = new ModelAndView("list-showroom");
        mav.addObject("showroom", showroomRepository.findAll());
        return mav;
    }

    @GetMapping("/addShowroomForm")
    public ModelAndView addShowroomForm() {
        if (authenticationFacade.getAuthentication().getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("READ_ONLY"))) {
            return new ModelAndView("redirect:/list-showrooms");
        } else {
            ModelAndView mav = new ModelAndView("add-showroom-form");
            Showroom showroom = new Showroom();
            mav.addObject("showroom", showroom);
            return mav;
        }
    }
    @PostMapping("/saveShowroom")
    public String saveShowroom(@ModelAttribute Showroom showroom) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        showroom.setCreated(currentPrincipalName);
        showroomRepository.save(showroom);
        userActionLogService.logAction(currentPrincipalName, "Добавление автосалона");
        return "redirect:/list-showroom";
    }
    @GetMapping("/showUpdateFormShowroom")
    public ModelAndView showUpdateForm(@RequestParam Long showroomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Optional<Showroom> optionalShowroom = showroomRepository.findById(showroomId);
        if (optionalShowroom.isPresent()) {
            Showroom showroom = optionalShowroom.get();
            if (authentication.getAuthorities().stream()
                    .anyMatch(r -> r.getAuthority().equals("ADMIN")) ||
                    (showroom.getCreated().equals(currentPrincipalName))) {
                ModelAndView mav = new ModelAndView("add-showroom-form");
                mav.addObject("showroom", showroom);
                userActionLogService.logAction(currentPrincipalName, "Изменение автосалона");
                return mav;
            } else {
                return new ModelAndView("redirect:/list-showroom");
            }
        } else {
            return new ModelAndView("redirect:/list-showroom");
        }
    }
    @GetMapping("/deleteShowroom")
    public String deleteShowroom(@RequestParam Long showroomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Optional<Showroom> optionalShowroom = showroomRepository.findById(showroomId);
        if (optionalShowroom.isPresent()) {
            Showroom showroom = optionalShowroom.get();
            if (authentication.getAuthorities().stream()
                    .anyMatch(r -> r.getAuthority().equals("ADMIN")) ||
                    (showroom.getCreated().equals(currentPrincipalName))) {
                showroomRepository.deleteById(showroomId);
                userActionLogService.logAction(currentPrincipalName, "Удаление автосалона");
            }
        }
        return "redirect:/list-showroom";
    }

    @GetMapping ("/showShowroomDetails")
    public ModelAndView showShowroomDetails(@RequestParam Long showroomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Optional<Showroom> optionalShowroom = showroomRepository.findById(showroomId);
        List<Car> allCars = carRepository.findAll();
        if (optionalShowroom.isPresent()) {
            Showroom showroom = optionalShowroom.get();
            if (authentication.getAuthorities().stream()
                    .anyMatch(r -> r.getAuthority().equals("ADMIN")) ||
                    (showroom.getCreated().equals(currentPrincipalName))) {
                ModelAndView mav = new ModelAndView("showroom-details");
                mav.addObject("showroom", showroom);
                mav.addObject("cars", showroom.getCars());
                mav.addObject("allCars", allCars);
                userActionLogService.logAction(currentPrincipalName, "Просмотр списка доступных авто");
                return mav;
            } else {
                return new ModelAndView("redirect:/list-showroom");
            }
        } else {
            return new ModelAndView("redirect:/list-showroom");
        }
    }

    @PostMapping("/addExistingCarToShowroom")
    public String addExistingCarToShowroom(@RequestParam Long showroomId, @RequestParam Long carId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Optional<Showroom> showroomOptional = showroomRepository.findById(showroomId);
        Optional<Car> carOptional = carRepository.findById(carId);

        if (showroomOptional.isPresent() && carOptional.isPresent()) {
            Showroom showroom = showroomOptional.get();
            Car car = carOptional.get();

            if (!showroom.getCars().contains(car)) {
                showroom.getCars().add(car);
                showroomRepository.save(showroom);
                userActionLogService.logAction(currentPrincipalName, "Добавление авто в список");
                return "redirect:/showShowroomDetails?showroomId=" + showroomId;
            } else {
                return "redirect:/list-showroom";
            }
        }
        return "redirect:/list-showroom";
    }
}