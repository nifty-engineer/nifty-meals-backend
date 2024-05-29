package com.niftyengineer.niftymeals.service;

import com.niftyengineer.niftymeals.dao.CurrentCheckoutsRepository;
import com.niftyengineer.niftymeals.dao.MealRepository;
import com.niftyengineer.niftymeals.dao.UserCheckoutHistoryRepository;
import com.niftyengineer.niftymeals.dto.responsemodels.CurrentCheckoutsResponse;
import com.niftyengineer.niftymeals.entity.Checkout;
import com.niftyengineer.niftymeals.entity.Meal;
import com.niftyengineer.niftymeals.entity.UserCheckoutHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class MealService {

    private MealRepository mealRepository;

    private CurrentCheckoutsRepository currentCheckoutsRepository;

    private UserCheckoutHistoryRepository userCheckoutHistoryRepository;

    @Autowired
    public MealService(MealRepository mealRepository, CurrentCheckoutsRepository currentCheckoutsRepository,
                       UserCheckoutHistoryRepository userCheckoutHistoryRepository) {
        this.mealRepository = mealRepository;
        this.currentCheckoutsRepository = currentCheckoutsRepository;
        this.userCheckoutHistoryRepository = userCheckoutHistoryRepository;
    }

    public Meal checkoutMeal (String userEmail, Long mealId) throws Exception {

        Optional<Meal> meal = mealRepository.findById(mealId);

        Checkout validateCheckout = currentCheckoutsRepository.findByUserEmailAndMealId(userEmail, mealId);

        if (meal.isEmpty()) {
            throw new Exception("Meal doesn't exist");
        } else if (validateCheckout != null) {
            throw new Exception("Meal has already been checked out");
        } else if (meal.get().getCount() <= 0) {
            throw new Exception("Meal is currently out of stock");
        }

        meal.get().setCount(meal.get().getCount() - 1);
        mealRepository.save(meal.get());

        Checkout checkout = new Checkout(
          userEmail,
          LocalDate.now().toString(),
          meal.get().getId()
        );

        currentCheckoutsRepository.save(checkout);

        UserCheckoutHistory history = new UserCheckoutHistory(
          userEmail,
          checkout.getCheckoutDate(),
          meal.get().getTitle(),
          meal.get().getDescription(),
          meal.get().getImg()
        );

        userCheckoutHistoryRepository.save(history);

        return meal.get();
    }

    public Boolean isMealCheckedoutByUser(String userEmail, Long mealId) {
        Checkout validateCheckout = currentCheckoutsRepository.findByUserEmailAndMealId(userEmail, mealId);
        return validateCheckout != null;
    }

    public int countCurrentCheckoutsByUser(String userEmail) {
        return currentCheckoutsRepository.findMealsByUserEmail(userEmail).size();
    }

    public List<CurrentCheckoutsResponse> currentCheckouts(String userEmail) throws Exception {

        List<CurrentCheckoutsResponse> currentCheckoutsResponses = new ArrayList<>();

        List<Checkout> checkoutList = currentCheckoutsRepository.findMealsByUserEmail(userEmail);
        List<Long> mealIdList = new ArrayList<>();

        for (Checkout checkout: checkoutList) {
            mealIdList.add(checkout.getMealId());
        }

        List<Meal> meals = mealRepository.findMealsByMealIds(mealIdList);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Meal meal : meals) {
            Optional<Checkout> checkout = checkoutList.stream()
              .filter(c -> c.getMealId().equals(meal.getId())).findFirst();

            if (checkout.isPresent()) {

                Date checkoutDate = sdf.parse(checkout.get().getCheckoutDate());
                Date currentDate = sdf.parse(LocalDate.now().toString());

                TimeUnit time = TimeUnit.DAYS;

                long difference_In_Time = time.convert(currentDate.getTime() - checkoutDate.getTime(),
                  TimeUnit.MILLISECONDS);

                currentCheckoutsResponses.add(new CurrentCheckoutsResponse(meal, (int)difference_In_Time));
            }
        }
        return currentCheckoutsResponses;
    }

    public void removeMeal (String userEmail, Long mealId) throws Exception {

        Optional<Meal> meal = mealRepository.findById(mealId);

        Checkout validateCheckout = currentCheckoutsRepository.findByUserEmailAndMealId(userEmail, mealId);

        if (!meal.isPresent() || validateCheckout == null) {
            throw new Exception("Meal does not exist or not checked out by user");
        }

        meal.get().setCount(meal.get().getCount() + 1);

        mealRepository.save(meal.get());
        currentCheckoutsRepository.deleteById(validateCheckout.getId());
    }
}
