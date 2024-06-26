package com.niftyengineer.niftymeals.controller;

import com.niftyengineer.niftymeals.dto.responsemodels.CurrentCheckoutsResponse;
import com.niftyengineer.niftymeals.entity.Meal;
import com.niftyengineer.niftymeals.service.MealService;
import com.niftyengineer.niftymeals.utils.JWTExtraction;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("http://localhost:3000")
@RestController
@RequestMapping("/api/meals")
public class MealController {

    private MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @GetMapping("/member/currentcheckouts")
    public List<CurrentCheckoutsResponse> currentCheckouts(@RequestHeader(value = "Authorization") String token)
      throws Exception
    {
        String userEmail = JWTExtraction.extractJWTPayload(token, "\"sub\"");
        return mealService.currentCheckouts(userEmail);
    }

    @GetMapping("/member/currentcheckouts/count")
    public int countCurrentCheckoutsByUser(@RequestHeader(value = "Authorization") String token) {
        String userEmail = JWTExtraction.extractJWTPayload(token, "\"sub\"");
        return mealService.countCurrentCheckoutsByUser(userEmail);
    }

    @GetMapping("/member/ischeckedout/byuser")
    public Boolean checkoutMealByUser(@RequestHeader(value = "Authorization") String token,
                                      @RequestParam Long mealId) {
        String userEmail = JWTExtraction.extractJWTPayload(token, "\"sub\"");
        return mealService.isMealCheckedoutByUser(userEmail, mealId);
    }

    @PutMapping("/member/checkout")
    public Meal checkoutMeal (@RequestHeader(value = "Authorization") String token,
                              @RequestParam Long mealId) throws Exception {
        String userEmail = JWTExtraction.extractJWTPayload(token, "\"sub\"");
        return mealService.checkoutMeal(userEmail, mealId);
    }

    @PutMapping("/member/remove")
    public void removeMeal(@RequestHeader(value = "Authorization") String token,
                           @RequestParam("id") Long mealId) throws Exception {
        String userEmail = JWTExtraction.extractJWTPayload(token, "\"sub\"");
        mealService.removeMeal(userEmail, mealId);
    }
}
