package DD.Test_Kurs.controller;

import DD.Test_Kurs.config.AuthenticationFacade;
import DD.Test_Kurs.entity.Car;
import DD.Test_Kurs.service.UserActionLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import DD.Test_Kurs.repository.CarRepository;

import java.util.Optional;

@Slf4j
@Controller
public class CarController {
    @Autowired
    private CarRepository carRepository;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private UserActionLogService userActionLogService;

    @GetMapping("/list-cars")
    public ModelAndView getAllCars() {
        log.info("/list -> connection");
        ModelAndView mav = new ModelAndView("list-cars");
        mav.addObject("cars", carRepository.findAll());
        return mav;
    }

    @GetMapping("/addCarForm")
    public ModelAndView addCarForm() {
        if (authenticationFacade.getAuthentication().getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("READ_ONLY"))) {
            return new ModelAndView("redirect:/list-cars");
        } else {
            ModelAndView mav = new ModelAndView("add-car-form");
            Car car = new Car();
            mav.addObject("car", car);
            return mav;
        }
    }

    @PostMapping("/saveCar")
    public String saveCar(@ModelAttribute Car car) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        car.setCreated(currentPrincipalName);
        carRepository.save(car);
        userActionLogService.logAction(currentPrincipalName, "Добавление авто");
        return "redirect:/list-cars";
    }

    @GetMapping("/showUpdateForm")
    public ModelAndView showUpdateForm(@RequestParam Long carId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Optional<Car> optionalCar = carRepository.findById(carId);
        if (optionalCar.isPresent()) {
            Car car = optionalCar.get();
            if (authentication.getAuthorities().stream()
                    .anyMatch(r -> r.getAuthority().equals("ADMIN")) ||
                    (car.getCreated().equals(currentPrincipalName))) {
                ModelAndView mav = new ModelAndView("add-car-form");
                mav.addObject("car", car);
                userActionLogService.logAction(currentPrincipalName, "Изменение авто");
                return mav;
            } else {
                return new ModelAndView("redirect:/list-cars");
            }
        } else {
            return new ModelAndView("redirect:/list-cars");
        }
    }

    @GetMapping("/deleteCar")
    public String deleteCar(@RequestParam Long carId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Optional<Car> optionalCar = carRepository.findById(carId);
        if (optionalCar.isPresent()) {
            Car car = optionalCar.get();
            if (authentication.getAuthorities().stream()
                    .anyMatch(r -> r.getAuthority().equals("ADMIN")) ||
                    (car.getCreated().equals(currentPrincipalName))) {
                carRepository.deleteById(carId);
                userActionLogService.logAction(currentPrincipalName, "Удаление авто");
            }
        }
        return "redirect:/list-cars";
    }
}