package edu.northeastern.numad25su_group9.services;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad25su_group9.models.Budget;
import edu.northeastern.numad25su_group9.repositories.BudgetRepository;

public class BudgetService {

    public static final String TAG = "BudgetService";

    public interface OperationCallback { void onSuccess(); void onError(Exception e); }
    public interface BudgetCallback { void onSuccess(Budget b); void onError(Exception e); }
    public interface BudgetsCallback { void onSuccess(List<Budget> list); void onError(Exception e); }

    private final BudgetRepository repo;

    public BudgetService() {
        String userId = new AuthService().getCurrentUserId();
        this.repo = new BudgetRepository(userId);
    }

    public void addBudget(Budget b, OperationCallback cb) {
        repo.addBudget(b)
                .addOnSuccessListener(u -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void updateBudget(Budget updates, OperationCallback cb) {
        repo.updateBudget(updates)
                .addOnSuccessListener(u -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void deleteBudget(int year, int month, OperationCallback cb) {
        repo.deleteBudget(year, month)
                .addOnSuccessListener(u -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    /**
     * Get budget for a given month.
     */
    public void getBudgetForMonth(int year, int month, BudgetCallback cb) {
        Log.d(TAG, "Getting budget for month " + year + "-" + month);
        repo.getBudgetForMonth(year, month)
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        cb.onError(new Exception("Budget for given month does not exist"));
                        return;
                    }
                    Budget b = snap.getValue(Budget.class);
                    if (b != null) cb.onSuccess(b); else cb.onError(new Exception("Budget data invalid"));
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Get all budgets for user for a given year.
     * HINT: This is for plotting budget graph.
     */
    public void getAllBudgetsForYear(int year, BudgetsCallback cb) {
        // First day of the year at 00:00 UTC
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        // Last nanosecond of the year
        LocalDateTime end = start.plusYears(1).minusNanos(1);

        long startMillis = start.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
        long endMillis = end.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();

        repo.getBudgetsByDateRange(startMillis, endMillis)
                .addOnSuccessListener(snap -> {
                    // Return default value
                    if (!snap.exists() || !snap.hasChildren()) {
                        cb.onSuccess(new ArrayList<>());
                        return;
                    }
                    List<Budget> out = new ArrayList<>();
                    for (DataSnapshot child : snap.getChildren()) {
                        Budget b = child.getValue(Budget.class);
                        if (b != null) out.add(b);
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Get latest budget for user.
     * Throws an exception if no budget exists for the user.
     */
    public void getLatestBudget(BudgetCallback cb) {
        repo.getLatestBudget()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists() || !snap.hasChildren()) {
                        cb.onError(new Exception("Budget for given user does not exist"));
                        return;
                    }

                    DataSnapshot latestBudgetSnapshot = snap.getChildren().iterator().next();
                    Budget b = latestBudgetSnapshot.getValue(Budget.class);
                    if (b != null) cb.onSuccess(b); else cb.onError(new Exception("Budget data invalid"));
                })
                .addOnFailureListener(cb::onError);
    }
}
